package com.example.ronda.domain.rondaGame


import com.example.ronda.domain.card.Card
import com.example.ronda.domain.card.CardType
import com.example.ronda.domain.card.User
import com.example.ronda.domain.grid.CardGridManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

private typealias Rows = Int
private typealias Cols = Int
private typealias CardId = Int
private typealias CellId = Int


enum class Actions {
    None, Darba, Taawida, Messa, Last_hand
}
enum class RondaStatus {
    Ready, Play, Game_over
}
enum class PlayerStatus {
    Just_played, Ronda, Tringa
}
class RondaGame() {
    private lateinit var cardGridManager: CardGridManager
    private val playerStatus = mutableMapOf<User, PlayerStatus>()
    var rondaStatus = MutableStateFlow(RondaStatus.Ready)
    val playerAction = MutableStateFlow(
        Pair(User.NONE, Actions.None)
    )
    val playerScore = MutableStateFlow(mapOf<User, Int>())
    var loadedCards = MutableStateFlow(emptyList<Card.Front>())
    private val usedCards = emptyList<Card.Front>()
    private var userturn = (1..2).random()
    //var youWin =
    val v = MutableStateFlow(mapOf<User, Card.Front>())
    // Private MutableStateFlow that holds the mutable map
    private val _userCardsMap = MutableStateFlow<MutableMap<User, Card>>(mutableMapOf())

    // Publicly exposed StateFlow (immutable) for observers
    val userCardsMap: StateFlow<Map<User, Card>> = _userCardsMap.asStateFlow() // Expose as immutable Map

    // --- Methods to update the map ---

    /**
     * Assigns or updates a card for a specific user.
     * If the card is Card.Back, it might represent clearing the user's card.
     */
    fun assignCardToUser(user: User, card: Card) {
        _userCardsMap.update { currentMap ->
            // Create a new mutable map based on the current one to ensure StateFlow emits a new instance
            val newMap = currentMap.toMutableMap()
            newMap[user] = card
            newMap // Return the updated map
        }
    }

    // 1 10 dhab
    // 11 20 twajn
    // 21 30 syufa
    // 31 40 zrawet

    fun start() {
//        v.update {
//            it.toMutableMap().apply {
//                put(User.MYUSER, Card.Front(CardType.Dhab, 1))
//                put(User.OTHERUSER, Card.Front(CardType.Dhab, 1))
//            }
//        }
//        v.update {
//            it.toMutableMap().apply {
//                clear()
//                put(User.MYUSER, Card.Front(CardType.Dhab, 3))
//            }
//        }
        v.value = mapOf(User.MYUSER to Card.Front(CardType.Dhab, 1),
            User.OTHERUSER to Card.Front(CardType.Dhab, 1),
            User.MYUSER to Card.Front(CardType.Dhab, 3)
            )
                //mapOf(User.OTHERUSER to Card.Front(CardType.Dhab, 1))
        //v.value = mapOf(User.MYUSER to Card.Front(CardType.Dhab, 1))
    }
    fun hit() {

    }
    fun generateUniqueCardsForPlayers(
        countPerPlayer: Int = 3
    ) {
        val totalCardsToGenerate = countPerPlayer * 2

        // Get all CardType values. If some are not meant for Front cards, filter them.
        val cardTypes = CardType.entries
        if (cardTypes.isEmpty()) {
            throw IllegalStateException("No card types available for generation.")
        }

        val validNumbers = (1..7).toList() + (10..12).toList()

        val maxPossibleUniqueCards = cardTypes.size * validNumbers.size
        if (totalCardsToGenerate > maxPossibleUniqueCards) {
            throw IllegalArgumentException(
                "Cannot generate $totalCardsToGenerate unique Cards. " +
                        "Only $maxPossibleUniqueCards unique type/num combinations are possible."
            )
        }

        val generatedCards = mutableListOf<Card.Front>()
        // Use a set to track the combination of type and num to ensure uniqueness
        val usedIdentities = mutableSetOf<Pair<CardType, Int>>()

        while (generatedCards.size < totalCardsToGenerate) {
            val randomType = cardTypes.random()
            val randomNumber = validNumbers.random()
            val currentIdentity = Pair(randomType, randomNumber)

            if (usedIdentities.add(currentIdentity)) {
                // Create the Card.Front object. Owner will be assigned after shuffling.
                generatedCards.add(Card.Front(type = randomType, num = randomNumber))
            }
        }

        // Shuffle the successfully generated unique cards
        generatedCards.shuffle()

        // Assign ownership/user
        for (i in generatedCards.indices) {
            generatedCards[i].owner = if (i < countPerPlayer) User.MYUSER else User.OTHERUSER
        }
        loadedCards.value = generatedCards
        loadedCards.update {
            it.apply {
                generatedCards
            }
        }
    }
//    fun start() {
//        val user = User.entries.toTypedArray().random()
//        val type = CardType.entries.random()
//        val num = (1..12).random()
//        usedCards.add()
//        usedCards. = Card.Front(CardType.Dhab, 1)
//    }
//    fun addCard(user: User, cellId: Int, card: Card) {
//        loadedCards[card.cardId] = Triple(user, cellId, card)
//    }
//
//    fun getCard(cardId: Int): Card? = loadedCards[cardId]?.third
//
//    fun getCellIdFromCardId(cardId: Int): CellId? = loadedCards[cardId]?.second
//
//    fun getPlacedCards(): List<Card> {
//        return cellOccupancy.values.mapNotNull { data ->
//            val card = data.second
//            val cellId = data.first
//            loadedCards[card.cardId]?.third
//        }
//    }
//    fun isCellEmpty(row: Int, col: Int): Boolean {
//        return !cellOccupancy.containsKey(Pair(row, col))
//    }
//    fun getCellForCoordinates(x: Float, y: Float): Int? {
//        val cell = gridManager.getCellIdFromCoords(x, y)
//        if (cell == null) return null
//        return cell
//    }
//    fun placeCardInCell(cardId: Int, row: Int, col: Int): Boolean {
//        val card = loadedCards[cardId] ?: return false // Card doesn't exist
//        if (!isCellEmpty(row, col)) return false // Cell is occupied
//
//        // Remove card from its old cell if it was placed
//        card.currentCell?.let { oldCell ->
//            cellOccupancy.remove(oldCell)
//        }
//
//        // Place card in new cell
//        cellOccupancy[Pair(row, col)] = card
//        card.currentCell = Pair(row, col)
//        return true
//    }
//    fun removeCardFromLoadedCards(cardId: Int): Card? = loadedCards.remove(cardId)
//
//    fun removeCardFromCell(row: Int, col: Int): Card? {
//        val cell = Pair(row, col)
//        val cardRemoved = cellOccupancy.remove(cell) ?: return null
//        val card = loadedCards[cardRemoved.cardId]
//        card?.currentCell = null
//        return card
//    }
//    fun getCardInCell(row: Int, col: Int): Card? {
//        val card = cellOccupancy[Pair(row, col)] ?: return null
//        return loadedCards[card.cardId]
//    }
//    fun clearGrid() {
//        cellOccupancy.clear()
//        loadedCards.values.forEach { it.currentCell = null }
//    }

}

fun main() {
    val rondaGame = RondaGame()
    rondaGame.start()
    rondaGame.generateUniqueCardsForPlayers()
    println(rondaGame.loadedCards.value)
//    CardGridManager().apply {
//        updateCanvasSize(100f, 100f)
//        populateGridWithCards(cards)
//        val card2 = getCardFromCellId(4) as? Card.Front
//        println(grid)
//        println("card id = ${card2?.cardId} and card owner = ${card2?.owner} and num = ${card2?.num} and type = ${card2?.type}")
//    }

}