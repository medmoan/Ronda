package com.example.ronda.domain.rondaGame

import android.util.Log

import com.example.ronda.domain.Cards
import com.example.ronda.domain.card.Card
import com.example.ronda.domain.card.CardType
import com.example.ronda.domain.card.User
import com.example.ronda.domain.grid.CardGridManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.buffer
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlin.coroutines.cancellation.CancellationException
import kotlin.coroutines.coroutineContext


private typealias Rows = Int
private typealias Cols = Int
private typealias CardId = Int
private typealias CellId = Int


class RondaGame() {
    companion object {
        private const val INVALID_CELL = -1
    }
    private val cardGridManager by lazy {
        CardGridManager()
    }
    private val occupiedDeckCells get() = cardGridManager.getOccupiedDeckCells()
    private val playingCells = cardGridManager.playingCells

    var playerFlag = MutableStateFlow(mapOf<User, Pair<Flag, Int>>())
        private set
    var rondaState = MutableStateFlow(RondaState(User.entries.filter { it != User.NONE }.random(), GameState.Ready))
        private set
    var playerAction: MutableStateFlow<LinkedHashMap<User, Actions>> = MutableStateFlow(linkedMapOf())
        private set
    var playerScore = MutableStateFlow(mapOf<User, Int>())
        private set
    var cards = MutableStateFlow(Cards(emptyList()))
        private set
    private val playedCards: MutableList<Card.Front> = mutableListOf()
    private val capturedCards: MutableList<Card.Front> = mutableListOf()
    private val CARDNUMS = listOf(1, 2, 3, 4, 5, 6, 7, 10, 11, 12)
    val totalGridRows: Int get() = cardGridManager.totalRows
    val totalGridColumns: Int get() = cardGridManager.totalColumns
    var playJob: Job? = null
    private var previousCard: Card.Front? = null
    var endResult = MutableStateFlow<EndResult>(EndResult.None)
        private set


    fun getCellFromCard(card: Card): CellId {
        return cardGridManager.getCellFromCard(card)?: INVALID_CELL
    }
    fun getCardFromCell(cellId: Int): Card? {
       return cardGridManager.getCardFromCell(cellId)
     }
    fun getCoordsFromCell(cell: CellId): Pair<Float, Float>?{
        if (cell == INVALID_CELL) return null
        return cardGridManager.getCoordsFromCellId(cell)
    }
    private fun placeCardIntoDeckCell(card: Card.Front, emptyDeck: Boolean) {
        if (emptyDeck) {
            removePlayedCardFromHand(card)
        }
        if (getEmptyDeckCells().isEmpty()) return
        val chosenDeckCell = getEmptyDeckCells().random()
        addCardToCell(chosenDeckCell, card)
    }
    fun getEmptyDeckCells(): List<CellId> {
        return cardGridManager.getEmptyDeckCells()
    }
    fun removeCardFromCell(cellId: CellId) {
        cardGridManager.removeCardFromCell(cellId)
    }
    fun addCardToCell(cell: Int, card: Card) {
        cardGridManager.addCardToCell(cell, card)
    }
    private fun isLastPlay(): Boolean {
        return playedCards.size == 4
    }
    fun checkIfNeededNewCards() {
        if (isLastPlay()) {
            val countCardsRemainingInDeck = occupiedDeckCells.size
            val targetActions = setOf(
                Actions.Makla,
                Actions.Darba,
                Actions.Messa,
                Actions.Taawida1,
                Actions.Taawida2
            )
            val whoScoredLast: User? = getLastUserWithRelevantAction(targetActions)
            whoScoredLast?.let { user ->
                updatePlayerScore(user, countCardsRemainingInDeck)
                updatePlayerAction(user, Actions.Last_hand)
            }
            checkGameState()
            return
        }
        val playingCards = getPlayingCards()
        if (playingCards.isEmpty()) {
            updateScoreFromFlags(false)
            generateNewUniqueCards()
        }
    }

    fun getPlayingCards(): List<Card.Front> {
        return playingCells.mapNotNull { getCardFromCell(it) as? Card.Front }
    }

    private fun checkGameState() {
        rondaState.update { rondaState ->
            rondaState.copy(userturn = User.NONE, gameState = GameState.End)
        }
        val scoreOfMyUser = playerScore.value[User.MYUSER] ?: 0
        val scoreOfOtherUser = playerScore.value[User.OTHERUSER] ?: 0
        if (scoreOfMyUser == scoreOfOtherUser) {
            endResult.update {
              EndResult.Draw(scoreOfMyUser)
            }
        }
        if (scoreOfMyUser > scoreOfOtherUser) {
            endResult.update {
                EndResult.Win( scoreOfMyUser, scoreOfOtherUser)
            }
        }
        else {
            endResult.update {
                EndResult.Lose(scoreOfMyUser, scoreOfOtherUser)
            }
        }
    }


    private fun isDeckCellsEmpty(): Boolean {
        return cardGridManager.isDeckCellsEmpty()
    }

    fun updateCanvasSize(width: Float, height: Float) {
        cardGridManager.updateCanvasSize(width, height)
    }
    fun getCellWidth(): Float {
        return cardGridManager.cellWidth
    }

    fun getCellHeight(): Float {
        return cardGridManager.cellHeight
    }
    private fun getLastUserWithRelevantAction(relevantActions: Set<Actions>): User? {

        val currentActions = playerAction.value

        val lastMatchingEntry = currentActions
            .entries
            .asSequence()
            .filter { entry -> entry.value in relevantActions }
            .lastOrNull()

        return lastMatchingEntry?.key
    }
    private suspend fun placeOrScore(card: Card.Front): Boolean {
        if (card.num != previousCard?.num && playJob?.isActive == true) {
            return false
        }

        playJob?.cancelAndJoin()

        playJob = CoroutineScope(coroutineContext).launch {
            try {
                if (handleEmptyDeck(card)) {
                    return@launch
                }
                val matchResult = findMatchAndCaptureSequence(card)
                if (matchResult.determinedAction != null) {
                    val numOfCaptures = matchResult.capturedDeckCells.size
                    val cardsCurrentlyOnDeck = occupiedDeckCells.size
                    handleAction(
                        card.owner!!,
                        matchResult.determinedAction,
                        numOfCaptures,
                        cardsCurrentlyOnDeck
                    )
                }
                removePlayedCardFromHand(card)

                if (matchResult.foundMatch) {
                    processCapturedCards(matchResult.capturedDeckCells, card)
                } else {
                    placeCardIntoDeckCell(card, false)
                    updatePlayerAction(card.owner!!, Actions.Just_played)
                }

                previousCard = card
                println("Finished")

            } catch (_: CancellationException) {
                println("Interrupted")
            }
        }
        return true
    }
    private fun updatePlayerScore(
        user: User,
        score: Int) {
        if (score > 0) {
            playerScore.update { currentScore ->
                val mutableScore = currentScore.toMutableMap()
                val existingScore = mutableScore[user] ?: 0
                mutableScore[user] = existingScore + score
                mutableScore.toMap()
            }
        }
    }
    private fun updatePlayerAction(user: User, actions: Actions) {
        playerAction.update { currentActions ->
            val newMap = LinkedHashMap(currentActions)
            newMap.apply { this[user] = actions }
        }
    }
    private fun handleEmptyDeck(card: Card.Front): Boolean {
        if (isDeckCellsEmpty()) {
            updatePlayerAction(card.owner!!, Actions.Just_played)
            placeCardIntoDeckCell(card, true)
            previousCard = card
            return true
        }
        return false
    }

    private data class MatchResult(
        val foundMatch: Boolean,
        val capturedDeckCells: List<CellId>,
        val determinedAction: Actions? = null,
    )

    private fun findMatchAndCaptureSequence(playedCard: Card.Front): MatchResult {
        var cellDeckIndex = 0
        val currentDeckCells = occupiedDeckCells

        while (cellDeckIndex < currentDeckCells.size) {
            val currentDeckCell = currentDeckCells[cellDeckIndex]
            val cardOnDeck = getCardFromCell(currentDeckCell)?: run {
                cellDeckIndex++
                return@run null
            }
            cardOnDeck as? Card.Front ?: continue
            if (cardOnDeck.num == playedCard.num) {
                val determinedAction: Actions? = if (playedCard.num == previousCard?.num) {
                    determineActionBasedOnOpponentAction(playedCard.owner!!)
                } else {
                    Actions.Makla
                }
                val capturedCells: List<CellId> =
                    captureSequenceFrom(cellDeckIndex, cardOnDeck, currentDeckCells)
                return MatchResult(true, capturedCells, determinedAction)
            }
            cellDeckIndex++
        }
        return MatchResult(false, emptyList())
    }
    private fun determineActionBasedOnOpponentAction(currentPlayer: User): Actions? {
        val oppositeUser = if (currentPlayer == User.MYUSER) User.OTHERUSER else User.MYUSER
        val playerAction = playerAction.value[oppositeUser]

        return when (playerAction) {
            Actions.Just_played -> {
                Actions.Darba.also { println("darba") }
            }
            Actions.Darba -> {
                Actions.Taawida1.also { println("taawida1") }
            }
            Actions.Taawida1 -> {
                Actions.Taawida2.also { println("taawida2") }
            }
            else -> null
        }
    }

    private suspend inline fun handleAction(user: User, action: Actions, countOfCaptures: Int, cardsOnDeckBeforeCapture: Int) {

        val willDeckBeEmptyAfterCapture = (cardsOnDeckBeforeCapture - countOfCaptures) <= 0
        val delayForAction = 4_000L
        val countOfCapturesSeq = countOfCaptures - 1
        updatePlayerAction(user, action)
        when(action) {
            Actions.Makla -> {
                if (willDeckBeEmptyAfterCapture && isLastPlay()) {
                    updatePlayerAction(user, Actions.Messa)
                    updatePlayerAction(user, Actions.Last_hand)
                    val lastNumOfCards = occupiedDeckCells.size
                    val score = 3 + countOfCaptures + lastNumOfCards
                    updatePlayerScore(user, score)
                }
                else if (willDeckBeEmptyAfterCapture) {
                    updatePlayerAction(user, Actions.Messa)
                    val score = 3 + countOfCapturesSeq
                    updatePlayerScore(user, score)
                }
                else if (isLastPlay()) {
                    updatePlayerAction(user, Actions.Last_hand)
                    val lastNumOfCards = occupiedDeckCells.size
                    val score = 2 + countOfCapturesSeq + lastNumOfCards
                    updatePlayerScore(user, score)
                }
                else updatePlayerScore(user, 1 + countOfCapturesSeq)
                return
            }
            Actions.Taawida2 -> {
                if (willDeckBeEmptyAfterCapture && isLastPlay()) {
                    updatePlayerAction(user, Actions.Messa)
                    updatePlayerAction(user, Actions.Last_hand)
                    val lastNumOfCards = occupiedDeckCells.size
                    val score = 15 + countOfCapturesSeq + lastNumOfCards

                    updatePlayerScore(user, score)
                }
                else if (willDeckBeEmptyAfterCapture) {
                    updatePlayerAction(user, Actions.Messa)
                    val score = 15 + countOfCapturesSeq
                    updatePlayerScore(user, score)
                }
                else if (isLastPlay()) {
                    updatePlayerAction(user, Actions.Last_hand)
                    val lastNumOfCards = occupiedDeckCells.size
                    val score = 14 + countOfCapturesSeq + lastNumOfCards

                    updatePlayerScore(user, score)
                }
                else updatePlayerScore(user, 14 + countOfCapturesSeq)
                return
            }
//            Actions.Darba -> {
//                val num = previousCard?.num ?: 0
//                val numList = playedCards.filter { it.owner == user }.filter { it.num == num }
//                if (numList.isEmpty()) return
//            }
            else -> Unit
        }
        delay(delayForAction)
        val playerAction = action
        when(playerAction) {
            Actions.Darba -> {
                if (willDeckBeEmptyAfterCapture && isLastPlay()) {
                    updatePlayerAction(user, Actions.Messa)
                    updatePlayerAction(user, Actions.Last_hand)
                    val lastNumOfCards = occupiedDeckCells.size
                    val score = 3 + countOfCapturesSeq + lastNumOfCards
                    println("lasthand and messa $user")
                    updatePlayerScore(user, score)
                }
                else if (willDeckBeEmptyAfterCapture) {
                    updatePlayerAction(user, Actions.Messa)
                    val score = 2  + countOfCapturesSeq
                    println("darba and messa $user")
                    updatePlayerScore(user, score)
                }
                else if (isLastPlay()) {
                    updatePlayerAction(user, Actions.Last_hand)
                    val lastNumOfCards = occupiedDeckCells.size
                    val score = 2 + countOfCapturesSeq + lastNumOfCards

                    updatePlayerScore(user, score)
                }
            }
            Actions.Taawida1 -> {
                if (willDeckBeEmptyAfterCapture && isLastPlay()) {
                    updatePlayerAction(user, Actions.Messa)
                    updatePlayerAction(user, Actions.Last_hand)
                    val lastNumOfCards = occupiedDeckCells.size
                    val score = 7 + countOfCapturesSeq + lastNumOfCards

                    updatePlayerScore(user, score)
                }
                else if (willDeckBeEmptyAfterCapture) {
                    updatePlayerAction(user, Actions.Messa)
                    val score = 9 + countOfCapturesSeq
                    updatePlayerScore(user, score)
                }
                else if (isLastPlay()) {
                    updatePlayerAction(user, Actions.Last_hand)
                    val lastNumOfCards = occupiedDeckCells.size
                    val score = 8 + countOfCaptures + lastNumOfCards

                    updatePlayerScore(user, score)
                }
            }
            else -> Unit
        }
    }


    private fun captureSequenceFrom(startIndex: Int, initialMatchCard: Card.Front, currentDeckCards: List<CellId>): List<CellId> {
        val captured = mutableListOf<CellId>()
        captured.add(currentDeckCards[startIndex])

        var lastCapturedNum = initialMatchCard.num
        var currentCardNumIndex = CARDNUMS.indexOf(lastCapturedNum)
        if (currentCardNumIndex == -1) return captured

        var nextDeckCellToCheckIndex = startIndex + 1
        while (currentCardNumIndex < CARDNUMS.size - 1 && nextDeckCellToCheckIndex < currentDeckCards.size) {

            currentCardNumIndex++
            val expectedNextNum = CARDNUMS[currentCardNumIndex]
            val nextDeckCardCellId = currentDeckCards[nextDeckCellToCheckIndex]
            val nextCardOnDeck = getCardFromCell(nextDeckCardCellId) as? Card.Front

            if (nextCardOnDeck != null && nextCardOnDeck.num == expectedNextNum) {
                lastCapturedNum = expectedNextNum
                captured.add(nextDeckCardCellId)
                nextDeckCellToCheckIndex++
            } else {
                break
            }
        }
        return captured
    }

    private fun removePlayedCardFromHand(card: Card.Front) {
        val playingCell = getCellFromCard(card)
        if (playingCell != INVALID_CELL) {
            removeCardFromCell(playingCell)
        }
    }

    private fun processCapturedCards(capturedDeckCells: List<CellId>, playedCard: Card.Front) {
        capturedCards.add(playedCard)
        for (deckCell in capturedDeckCells) {
            val capturedCard = getCardFromCell(deckCell) as? Card.Front ?: continue
            capturedCards.add(capturedCard)
            removeCardFromCell(deckCell)
            // TODO: Add 'capturedCard' (and the 'playedCard' itself if it's part of the capture) to player's score/captured list.
        }
        // TODO: Add the 'playedCard' to the player's score as well, as it initiated the capture.
    }


    suspend fun pickDropCard(card: Card.Front) {
        if (card.owner != rondaState.value.userturn) return
        println("Played card: ${card.type} ${card.num} user: ${card.owner}")
        if (cards.value.areCardsJustGenerated) {
            cards.update { it.copy(areCardsJustGenerated = false) }
        }

        val played = placeOrScore(card)
        if (!played) return

        playedCards.add(card)
        checkIfNeededNewCards()
        if (rondaState.value.gameState == GameState.Play) {
            changeUserTurn()
            auto()
        }
    }
    suspend fun auto() {
        if (rondaState.value.userturn == User.OTHERUSER) {
            autoPlay()
        }
    }
    fun setFlag(user: User) {
        val cardsOfUser = cards.value.cards.filter { it.owner == user}
        if (cardsOfUser.size < 3) return

        val num1 = cardsOfUser[0].num
        val num2 = cardsOfUser[1].num
        val num3 = cardsOfUser[2].num

        if (num1 == num2 && num2 == num3) {
            updatePlayerFlag(user, Flag.Tringa, num1)
        } else if (num1 == num2 || num1 == num3 || num2 == num3) {
            updatePlayerFlag(user, Flag.Ronda, if (num1 == num2) num1 else num3)
        }
    }

    fun changeUserTurn() {
        rondaState.update { state ->
            state.copy(userturn = if (state.userturn == User.MYUSER) User.OTHERUSER else User.MYUSER)
        }
    }
    suspend fun autoPlay() {
        val playingCards = cards.value.cards.filter { it.owner == User.OTHERUSER }
        if (playingCards.isEmpty()) return

        val cardToPlay = playedCards.random()
        pickDropCard(cardToPlay)
    }
    suspend fun start(diff: Difficulty = Difficulty.Easy) {
        rondaState.update { state -> state.copy(gameState = GameState.Play) }
        generateNewUniqueCards()
        // auto()
    }
    suspend fun fakeStart() {
        rondaState.update { state -> state.copy(gameState = GameState.Play) }
        val player1Cells = playingCells.subList(3, 6)
        val player2Cells = playingCells.subList(0, 3)
        cards.update {
            it.copy(cards = listOf(
                Card.Front(type = CardType.Dhab, num = 10, owner = User.MYUSER),
                Card.Front(type = CardType.Twajen, num = 10, owner = User.MYUSER),
                Card.Front(type = CardType.Dhab, num = 7, owner = User.MYUSER),
                Card.Front(type = CardType.Zrawet, num = 1, owner = User.OTHERUSER),
                Card.Front(type = CardType.Zrawet, num = 10, owner = User.OTHERUSER),
                Card.Front(type = CardType.Syufa, num = 10, owner = User.OTHERUSER),
                ), areCardsJustGenerated = true
            )
        }
        val myUserNewCards = cards.value.cards.filter { it.owner == User.MYUSER }
        val otherUserNewCards = cards.value.cards.filter { it.owner == User.OTHERUSER }
        myUserNewCards.forEachIndexed { index, card ->
            if (index < player1Cells.size) {
                cardGridManager.addCardToCell(cell = player1Cells[index], card = card)
            }
        }
        otherUserNewCards.forEachIndexed { index, card ->
            if (index < player2Cells.size) {
                cardGridManager.addCardToCell(cell = player2Cells[index], card = card)
            }
        }
        setFlag(User.MYUSER); setFlag(User.OTHERUSER)
        updateScoreFromFlags(initialCheck = true)
    }

    private fun generateNewUniqueCards() {
        val countPerPlayer = 3
        val totalNewCardsToGenerate = countPerPlayer * 2


        if (!canGenerateNewCards(totalNewCardsToGenerate)) {
            return
        }


        val newRawCards = tryGenerateRawUniqueCards(totalNewCardsToGenerate)
        if (newRawCards.isEmpty()) {

            return
        }


        val (myUserNewCards, otherUserNewCards) = distributeCardsToPlayers(newRawCards, countPerPlayer)


        placeCardsOnPlayingCells(myUserNewCards, User.MYUSER)
        placeCardsOnPlayingCells(otherUserNewCards, User.OTHERUSER)


        cards.update {
            it.copy(
                cards = myUserNewCards + otherUserNewCards,
                areCardsJustGenerated = true
            )
        }

        updatePlayerFlag(User.MYUSER, Flag.None); updatePlayerFlag(User.OTHERUSER, Flag.None)
        setFlag(User.MYUSER);setFlag(User.OTHERUSER)
        updateScoreFromFlags(initialCheck = true)
    }

    private fun updatePlayerFlag(
        user: User,
        flag: Flag,
        num: Int = 0
    ) {
        if (flag == Flag.None) {
            playerFlag.update { flagMap ->
                val newFlagMap = flagMap.toMutableMap()
                newFlagMap.apply {
                    this[user] = Pair(Flag.None, 0)
                }.toMap()
            }
        }
        else {
            playerFlag.update { flagMap ->
                val newFlagMap = flagMap.toMutableMap()
                newFlagMap.apply {
                    this[user] = Pair(flag, num)
                }.toMap()
            }
        }
    }

    private fun updateScoreFromFlags(initialCheck: Boolean) {
        val myFlag = playerFlag.value[User.MYUSER]?.first ?: return
        val otherFlag = playerFlag.value[User.OTHERUSER]?.first ?: return
        val myUser = User.MYUSER
        val otherUser = User.OTHERUSER
        if (initialCheck) {
            // Possibilities that we get score from and how much in initial face
            // so they have to be different flags to score
            when(myFlag) {
                Flag.None -> {
                    if (otherFlag == Flag.Ronda) {
                        updatePlayerScore(otherUser, 1)
                    }
                    else if (otherFlag == Flag.Tringa) {
                        updatePlayerScore(otherUser, 5)
                    }
                }
                Flag.Ronda -> {
                    if (otherFlag == Flag.None) {
                        updatePlayerScore(myUser, 1)
                    }
                    else if (otherFlag == Flag.Tringa) {
                        updatePlayerScore(otherUser, 6)
                    }
                }
                Flag.Tringa -> {
                    if (otherFlag == Flag.None) {
                        updatePlayerScore(myUser, 5)
                    }
                    else if (otherFlag == Flag.Ronda) {
                        updatePlayerScore(myUser, 6)
                    }
                }
            }
        }
        else {
            if (playerFlag.value[User.MYUSER]?.first == Flag.None
                || playerFlag.value[User.OTHERUSER]?.first == Flag.None
                || playerFlag.value[User.MYUSER]?.first != playerFlag.value[User.OTHERUSER]?.first) return
            // Possibilities here will be equal flags so we check big card num
            val myCardNum = playerFlag.value[User.MYUSER]?.second ?: return
            val otherCardNum = playerFlag.value[User.OTHERUSER]?.second ?: return
            when (myFlag) {
                Flag.Ronda -> {
                    if (myCardNum > otherCardNum) {
                        updatePlayerScore(myUser, 2)
                    } else if (myCardNum < otherCardNum) {
                        updatePlayerScore(otherUser, 2)
                    }
                }
                Flag.Tringa -> {
                    if (myCardNum > otherCardNum) {
                        updatePlayerScore(myUser, 10)
                    } else if (myCardNum < otherCardNum) {
                        updatePlayerScore(otherUser, 10)
                    }
                }

                else -> Unit
            }
        }
    }


    private fun canGenerateNewCards(totalRequired: Int): Boolean {
        val cardTypes = CardType.entries
        if (cardTypes.isEmpty()) {
            return false
        }

        val validNumbers = (1..7).toList() + (10..12).toList()
        val usedCardsIdentities = playedCards.map { Pair(it.type, it.num) }.toSet()
        val maxPossibleUniqueCards = cardTypes.size * validNumbers.size
        val availableSlotsForNewCards = maxPossibleUniqueCards - usedCardsIdentities.size

        if (availableSlotsForNewCards < totalRequired) {
            if (availableSlotsForNewCards <= 0) {
                cards.update { it.copy(cards = emptyList(), areCardsJustGenerated = false) }
                return false
            }
        }
        return true
    }

    private fun tryGenerateRawUniqueCards(countToGenerate: Int): List<Card.Front> {
        val generatedCards = mutableListOf<Card.Front>()
        if (countToGenerate <= 0) return generatedCards

        val cardTypes = CardType.entries
        val validNumbers = (1..7).toList() + (10..12).toList()
        val usedIdentities = playedCards.map { Pair(it.type, it.num) }.toSet()
        val currentBatchIdentities = mutableSetOf<Pair<CardType, Int>>()

        var attempts = 0
        val maxAttempts = (cardTypes.size * validNumbers.size - usedIdentities.size).coerceAtLeast(1) * 2 + countToGenerate * 5


        while (generatedCards.size < countToGenerate && attempts < maxAttempts) {
            val randomType = cardTypes.random()
            val randomNumber = validNumbers.random()
            val currentIdentity = Pair(randomType, randomNumber)

            if (!usedIdentities.contains(currentIdentity) && currentBatchIdentities.add(currentIdentity)) {
                generatedCards.add(Card.Front(type = randomType, num = randomNumber, owner = User.NONE))
            }
            attempts ++
        }

        if (generatedCards.size < countToGenerate) {
            Log.w("RondaGame", "tryGenerateRawUniqueCards: Generated only ${generatedCards.size} out of $countToGenerate requested cards after $attempts attempts.")
        }
        return generatedCards
    }


    private fun distributeCardsToPlayers(
        rawCards: List<Card.Front>,
        countPerPlayer: Int,
    ): Pair<List<Card.Front>, List<Card.Front>> {
        val shuffledCards = rawCards.shuffled().toMutableList()

        val myUserCards = mutableListOf<Card.Front>()
        val otherUserCards = mutableListOf<Card.Front>()

        // Distribute to MYUSER first
        repeat(countPerPlayer) {
            if (shuffledCards.isEmpty()) return@repeat
            val card = shuffledCards.removeAt(0)
            card.owner = User.MYUSER
            myUserCards.add(card)
        }

        // Distribute to OTHERUSER
        repeat(countPerPlayer) {
            if (shuffledCards.isEmpty()) return@repeat
            val card = shuffledCards.removeAt(0)
            card.owner = User.OTHERUSER
            otherUserCards.add(card)
        }
        return Pair(myUserCards, otherUserCards)
    }

    private fun placeCardsOnPlayingCells(cardsToPlace: List<Card.Front>, player: User) {
        val playerCells = when (player) {
            User.MYUSER -> playingCells.subList(3, (3 + cardsToPlace.size).coerceAtMost(playingCells.size.coerceAtMost(6)) )
            User.OTHERUSER -> playingCells.subList(0, (cardsToPlace.size).coerceAtMost(playingCells.size.coerceAtMost(3)))
            else -> return
        }

        if (playerCells.size < cardsToPlace.size) {
            Log.w("RondaGame", "placeCardsOnGridForPlayer: Not enough cells (${playerCells.size}) for player $player to place ${cardsToPlace.size} cards.")
        }

        cardsToPlace.forEachIndexed { index, card ->
            if (index < playerCells.size) {
                removeCardFromCell(playerCells[index])
                addCardToCell(cell = playerCells[index], card = card)
            } else {
                Log.w("RondaGame", "placeCardsOnGridForPlayer: Ran out of cells for player $player while placing card $index.")
            }
        }
    }



}


fun main() {
    val rondaGame = RondaGame()
    runBlocking {
    launch {
        rondaGame.playerScore
            .buffer(capacity = 2)
            .collect {
                println("Player score: $it")
        }
    }

        rondaGame.fakeStart()
        val playingCellsOfMyUser = rondaGame.getPlayingCards().filter { it.owner == User.MYUSER}
        val playingCellsOfOtherUser = rondaGame.getPlayingCards().filter { it.owner == User.OTHERUSER}
        println(
            playingCellsOfMyUser + rondaGame.playerFlag.value[User.MYUSER]
        )
        println(
            playingCellsOfOtherUser + rondaGame.playerFlag.value[User.OTHERUSER]
        )
        val card1OfMyUser = playingCellsOfMyUser.filter { it.num == 10 && it.type == CardType.Dhab}
        val card2OfOtherUser = playingCellsOfOtherUser.filter { it.num == 10 && it.type == CardType.Zrawet}
        val card3OfMyUser = playingCellsOfMyUser.filter { it.num == 10 && it.type == CardType.Twajen}
        val card4OfOtherUser = playingCellsOfOtherUser.filter { it.num == 10 && it.type == CardType.Syufa}
        rondaGame.pickDropCard(card1OfMyUser[0])
        delay(500)
        rondaGame.pickDropCard(card2OfOtherUser[0])
        delay(4500)
        rondaGame.pickDropCard(card3OfMyUser[0])
        delay(500)
        rondaGame.pickDropCard(card4OfOtherUser[0])
//    rondaGame.start()
//        val playingCellsOfMyUser = rondaGame.getPlayingCards().filter { it.owner == User.MYUSER}
//        val playingCellsOfOtherUser = rondaGame.getPlayingCards().filter { it.owner == User.OTHERUSER}
//        println(
//            playingCellsOfMyUser + rondaGame.playerFlags.value[User.MYUSER]
//        )
//        println(
//            playingCellsOfOtherUser + rondaGame.playerFlags.value[User.OTHERUSER]
//        )
//        rondaGame.pickDropCard(playingCellsOfMyUser[0])
//        delay(500)
//        rondaGame.pickDropCard(playingCellsOfOtherUser[0])
    }
}