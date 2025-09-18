//import com.example.ronda.domain.card.Card
//import com.example.ronda.domain.card.CardType
//import com.example.ronda.domain.card.User
//import kotlinx.coroutines.flow.update
//
//private suspend fun generateNewUniqueCards() {
//    val countPerPlayer = 3
//    val player1Cells = playingCells.subList(3, 6)
//    val player2Cells = playingCells.subList(0, 3)
//    val totalNewCardsToGenerate = countPerPlayer * 2
//
//    val cardTypes = CardType.entries
//    if (cardTypes.isEmpty()) {
//        println("Error: No card types available.")
//        return
//    }
//    val validNumbers = (1..7).toList() + (10..12).toList()
//    val usedCardsIdentities = usedCards.map { Pair(it.type, it.num) }.toSet()
//    val maxPossibleUniqueCards = cardTypes.size * validNumbers.size
//    val availableSlotsForNewCards = maxPossibleUniqueCards - usedCardsIdentities.size
//
//    if (totalNewCardsToGenerate > availableSlotsForNewCards && availableSlotsForNewCards <= 0) {
//        println("No new unique cards can be generated.")
//        // Potentially end game or handle differently
//        cards.update{ it.copy(cards=emptyList(), areCardsJustGenerated = false)}
//        return
//    }
//
//    val generatedNewCards = mutableListOf<Card.Front>()
//    val currentBatchIdentities = mutableSetOf<Pair<CardType, Int>>()
//    var attempts = 0
//    val maxAttempts = maxPossibleUniqueCards * 2
//
//    while (generatedNewCards.size < totalNewCardsToGenerate && attempts < maxAttempts) {
//        val randomType = cardTypes.random()
//        val randomNumber = validNumbers.random()
//        val currentIdentity = Pair(randomType, randomNumber)
//        if (!usedCardsIdentities.contains(currentIdentity) && currentBatchIdentities.add(currentIdentity)) {
//            generatedNewCards.add(Card.Front(type = randomType, num = randomNumber, owner = User.NONE))
//        }
//        attempts++
//    }
//
//    if (generatedNewCards.isEmpty()) {
//        println("No new cards were generated in this batch.")
//        // cards.update { it.copy(areCardsJustGenerated = false) } // Keep existing cards, just signal not new
//        return
//    }
//
//    generatedNewCards.shuffle()
//    val myUserNewCards = mutableListOf<Card.Front>()
//    val otherUserNewCards = mutableListOf<Card.Front>()
//
//    generatedNewCards.forEachIndexed { idx, newCard ->
//        if (idx < countPerPlayer) {
//            if (myUserNewCards.size < player1Cells.size) {
//                newCard.owner = User.MYUSER
//                myUserNewCards.add(newCard)
//            }
//        } else if (idx < totalNewCardsToGenerate) {
//            if (otherUserNewCards.size < player2Cells.size) {
//                newCard.owner = User.OTHERUSER
//                otherUserNewCards.add(newCard)
//            }
//        }
//    }
//
//    myUserNewCards.forEachIndexed { index, card ->
//        if (index < player1Cells.size) {
//            cardGridManager.addCardToCell(cell = player1Cells[index], card = card)
//        }
//    }
//    otherUserNewCards.forEachIndexed { index, card ->
//        if (index < player2Cells.size) {
//            cardGridManager.addCardToCell(cell = player2Cells[index], card = card)
//        }
//    }
//
//    cards.update { it.copy(cards = myUserNewCards + otherUserNewCards, areCardsJustGenerated = true) }
//    println("Finished generating: ${myUserNewCards.size + otherUserNewCards.size} cards.")
//    setFlag(User.MYUSER); setFlag(User.OTHERUSER)
//}
//import com.example.ronda.domain.card.Card
//import com.example.ronda.domain.card.User
//import com.example.ronda.domain.rondaGame.Actions
//import com.example.ronda.domain.rondaGame.CellId
//import com.example.ronda.domain.rondaGame.RondaGame.Companion.INVALID_CELL
//import kotlinx.coroutines.CoroutineScope
//import kotlinx.coroutines.delay
//import kotlinx.coroutines.ensureActive
//import kotlinx.coroutines.flow.update
//import kotlinx.coroutines.launch
//import kotlin.collections.set
//import kotlin.coroutines.cancellation.CancellationException
//import kotlin.coroutines.coroutineContext
//
//private suspend fun placeOrScore(card: Card.Front): Boolean {
//
//    if (card.num != previousCardNum && playJob?.isActive == true) {
//        return false
//    }
//    playJob?.cancel()
//    playJob = CoroutineScope(coroutineContext).launch {
//
//        val initialPlayerActionForThisUser = playerAction.value[card.owner!!] ?: Actions.None
//        var actionSuccessfullySetAndDelayCompleted = false
//        var determinedPlayerActionForUser: Actions? = null
//        try {
//            val isDeckEmpty = isDeckCellsEmpty()
//            if (isDeckEmpty) {
//                playerAction.update {
//                    it.toMutableMap().apply {
//                        this[card.owner!!] = Actions.Just_played
//                    }.toMap()
//                }
//                placeCardIntoDeckCell(card, true)
//                previousCardNum = card.num
//                return@launch
//            }
//
//            var cellDeckIndex = 0
//            var foundMatch = false
//            val capturedDeckCells = mutableListOf<CellId>()
//            var lastCapturedCardNum = -1
//
//            // Find match and potential sequence
//            while (cellDeckIndex < deckCells.size) {
//                ensureActive()
//                val currentDeckCell = deckCells[cellDeckIndex]
//                val cardFromCell = getCardFromCell(currentDeckCell)
//                    ?: continue
//                cardFromCell as Card.Front
//
//                if (cardFromCell.num == card.num) {
//                    foundMatch = true
//                    val oppositeUser = if (card.owner!! == User.MYUSER) User.OTHERUSER else User.MYUSER
//                    val actualUser = card.owner!!
//                    val delayForDeterminedAction = 4_000L
//
//
//
//                    when (playerAction.value[oppositeUser]) {
//                        Actions.Just_played -> {
//                            println("ddarba")
//                            determinedPlayerActionForUser = Actions.Darba
//                        }
//
//                        Actions.Darba -> {
//                            determinedPlayerActionForUser = Actions.Taawida1
//                            println("taawida1")
//                        }
//
//                        Actions.Taawida1 -> {
//                            determinedPlayerActionForUser = Actions.Taawida2
//                            println("taawida2")
//                        }
//
//                        null, Actions.None, Actions.Taawida2, Actions.Messa, Actions.Last_hand -> {
//                        }
//                    }
//
//                    if (determinedPlayerActionForUser != null) {
//                        playerAction.update {
//                            it.toMutableMap().apply {
//                                this[actualUser] = determinedPlayerActionForUser
//                            }.toMap()
//                        }
//
//                        delay(delayForDeterminedAction)
//                        actionSuccessfullySetAndDelayCompleted = true
//
//                    }
//
//
//                    capturedDeckCells.add(currentDeckCell)
//                    lastCapturedCardNum = cardFromCell.num
//                    var currentIndexInCardsOrder = CARDNUMS.indexOf(lastCapturedCardNum)
//                    if (currentIndexInCardsOrder == -1) break
//
//                    var nextCellInDeckIndex = cellDeckIndex + 1
//                    while (currentIndexInCardsOrder < CARDNUMS.size - 1 && nextCellInDeckIndex < deckCells.size) {
//                        ensureActive()
//                        currentIndexInCardsOrder++
//                        val nextNumExpected = CARDNUMS[currentIndexInCardsOrder]
//                        val nextDeckCardCellId = deckCells[nextCellInDeckIndex]
//                        val nextCardFromDeck =
//                            cardGridManager.getCardFromCell(nextDeckCardCellId)
//                        if (nextCardFromDeck is Card.Front && nextCardFromDeck.num == nextNumExpected) {
//                            lastCapturedCardNum = nextNumExpected
//                            capturedDeckCells.add(nextDeckCardCellId)
//                            nextCellInDeckIndex++
//                        } else {
//                            break
//                        }
//                    }
//                    break
//                }
//                cellDeckIndex++
//            }
//
//            // Remove played card from its original cell in player's hand grid
//            val playingCell = getCellFromCard(card)
//            if (playingCell != INVALID_CELL) {
//                removeCardFromCell(playingCell)
//            }
//
//            if (foundMatch) {
//
//                for (deckCell in capturedDeckCells) {
//                    addCardToCell(
//                        deckCell,
//                        card
//                    )
//                    removeCardFromCell(deckCell)
//                    // TODO: Add captured cards to player's score/captured list
//                }
//
//                // TODO: Add the played card to player's score/captured list
//            } else {
//
//                placeCardIntoDeckCell(card, false)
//            }
//            previousCardNum = card.num
//            println("Finished")
//        } catch (e: CancellationException) {
//            //e.printStackTrace()
//            println("Interrupted")
//            val currentPotentiallySetAction = playerAction.value[card.owner!!]
//            if (determinedPlayerActionForUser != null && // An attempt to set a special action was made
//                currentPotentiallySetAction == determinedPlayerActionForUser && // The action in the state IS the one we were setting
//                !actionSuccessfullySetAndDelayCompleted) {
////                    playerAction.update {
////                        it.toMutableMap().apply { this[card.owner!!] = initialPlayerActionForThisUser }.toMap()
////                    }
//            }
//        }
//
//    }
//    return true
//}
////package com.example.ronda.domain.rondaGame
////
////// In RondaGame class:
////var currentCardOperationJob: Job? = null
////// ... (playerAction StateFlow is defined as before) ...
////
////private suspend fun placeOrScore(card: Card.Front) {
////    currentCardOperationJob?.cancelAndJoin()
////
////    currentCardOperationJob = CoroutineScope(coroutineContext).launch {
////        Log.d("RondaGame", "[Job: ${this.coroutineContext[Job]}] placeOrScore Task: Main operation started for card: ${card.num}")
////
////        // Store initial player action for potential revert if this part is cancelled
////        val initialPlayerActionForThisUser = playerAction.value[card.owner!!] ?: Actions.None
////        var actionSuccessfullySetAndDelayCompleted = false
////
////        try {
////            // ... (Handle Empty Deck) ...
////
////            // --- TASK 3: Iterate Deck and Find Match ---
////            while (cellDeckIndex < deckCells.size) { // Assuming cellDeckIndex and other vars are defined earlier
////                ensureActive()
////                // ... (your card matching logic) ...
////
////                if (/* cardFromCell.num == card.num - your match condition */ true) {
////                    Log.d("RondaGame", "[Job: ${this.coroutineContext[Job]}] placeOrScore Task: Match found...")
////                    foundMatch = true
////
////                    val oppositeUser = if (card.owner!! == User.MYUSER) User.OTHERUSER else User.MYUSER
////                    val actualUser = card.owner!!
////                    var determinedPlayerActionForUser: Actions? = null
////                    val delayForDeterminedAction = 4_000L
////
////                    when (playerAction.value[oppositeUser]) {
////                        Actions.Just_played -> {
////                            Log.d("RondaGame", "Opponent Just_played, setting Darba for $actualUser")
////                            determinedPlayerActionForUser = Actions.Darba
////                        }
////                        Actions.Darba -> {
////                            Log.d("RondaGame", "Opponent had Darba, setting Taawida1 for $actualUser")
////                            determinedPlayerActionForUser = Actions.Taawida1
////                        }
////                        Actions.Taawida1 -> {
////                            Log.d("RondaGame", "Opponent had Taawida1, setting Taawida2 for $actualUser")
////                            determinedPlayerActionForUser = Actions.Taawida2
////                        }
////                        else -> { /* No special action based on opposite player */ }
////                    }
////
////                    if (determinedPlayerActionForUser != null) {
////                        Log.d("RondaGame", "[Job: ${this.coroutineContext[Job]}] placeOrScore Task: Setting action: $determinedPlayerActionForUser for $actualUser")
////                        playerAction.update {
////                            it.toMutableMap().apply { this[actualUser] = determinedPlayerActionForUser }.toMap()
////                        }
////                        // This is the critical delay where cancellation might occur
////                        delay(delayForDeterminedAction)
////
////                        // If delay completes without cancellation, mark it
////                        actionSuccessfullySetAndDelayCompleted = true
////                        Log.d("RondaGame", "[Job: ${this.coroutineContext[Job]}] Delay completed for action $determinedPlayerActionForUser")
////                    }
////                    // ... (rest of match/sequence capture) ...
////                    break
////                }
////                cellDeckIndex++
////            }
////            // ... (TASK 4: Process Results) ...
////            playerAction.update { currentActions ->
////                val mutableActions = currentActions.toMutableMap()
////                // If no specific action (Darba, etc.) was set and completed,
////                // or if it's just a normal play, set Just_played.
////                // This ensures playerAction is updated even if the 'determinedPlayerActionForUser' block wasn't entered or completed.
////                if (!actionSuccessfullySetAndDelayCompleted && foundMatch) { // Or other conditions for Just_played
////                    mutableActions[card.owner!!] = Actions.Just_played
////                } else if (!foundMatch) { // If no match, card is just placed
////                    mutableActions[card.owner!!] = Actions.Just_played // Or Actions.None if appropriate after placing
////                }
////                // If actionSuccessfullySetAndDelayCompleted is true, the Darba/Taawida action is already set.
////                mutableActions.toMap()
////            }
////
////
////        } catch (e: CancellationException) {
////            Log.d("RondaGame", "[Job: ${this.coroutineContext[Job]}] placeOrScore Task: Main operation CANCELLED: ${e.message}")
////
////            // --- Revert playerAction if cancellation happened during the critical delay ---
////            // Check if the action was set but the delay for it was not completed
////            val currentPotentiallySetAction = playerAction.value[card.owner!!]
////            if (determinedPlayerActionForUser != null && // An attempt to set a special action was made
////                currentPotentiallySetAction == determinedPlayerActionForUser && // The action in the state IS the one we were setting
////                !actionSuccessfullySetAndDelayCompleted) { // AND the delay for it didn't complete
////
////                Log.d("RondaGame", "[Job: ${this.coroutineContext[Job]}] Reverting action for ${card.owner!!} from $currentPotentiallySetAction to $initialPlayerActionForThisUser due to cancellation during delay.")
////                playerAction.update {
////                    it.toMutableMap().apply { this[card.owner!!] = initialPlayerActionForThisUser }.toMap()
////                }
////            }
////            throw e // Re-throw CancellationException
////        } finally {
////            Log.d("RondaGame", "[Job: ${this.coroutineContext[Job]}] placeOrScore Task: Finally block executing.")
////        }
////    }
////}
////
