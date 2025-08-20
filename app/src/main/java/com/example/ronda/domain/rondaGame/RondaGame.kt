package com.example.ronda.domain.rondaGame


import com.example.ronda.domain.card.Card
import com.example.ronda.domain.card.UserCard
import com.example.ronda.domain.grid.CardGridManager
import com.example.ronda.domain.grid.GridManager
import kotlinx.coroutines.flow.MutableStateFlow

private typealias Rows = Int
private typealias Cols = Int
private typealias CardId = Int
private typealias CellId = Int


class RondaGame() {
    lateinit var cardGridManager: CardGridManager
    val loadedCards = mutableListOf<UserCard>()
    val usedCards = mutableSetOf<Card>()

    var playedCards = MutableStateFlow<List<Card>>(emptyList())
    var playerCards = MutableStateFlow<List<Card>>(emptyList())
    var playerScore = MutableStateFlow<Int>(0)
    var opponentScore = MutableStateFlow<Int>(0)
    var playerTurn = MutableStateFlow<Boolean>(true)
    var rondaState = MutableStateFlow<Pair<User, RondaState>>(
        value = TODO()
    )

    //var winner =
    // 1 10 dhab
    // 11 20 twajn
    // 21 30 syufa
    // 31 40 zrawet
    fun start() {
        val user = User.entries.toTypedArray().random()
    }
    fun addCard(user: User, cellId: Int, card: Card) {
        loadedCards[card.cardId] = Triple(user, cellId, card)
    }

    fun getCard(cardId: Int): Card? = loadedCards[cardId]?.third

    fun getCellIdFromCardId(cardId: Int): CellId? = loadedCards[cardId]?.second

    fun getPlacedCards(): List<Card> {
        return cellOccupancy.values.mapNotNull { data ->
            val card = data.second
            val cellId = data.first
            loadedCards[card.cardId]?.third
        }
    }
    fun isCellEmpty(row: Int, col: Int): Boolean {
        return !cellOccupancy.containsKey(Pair(row, col))
    }
    fun getCellForCoordinates(x: Float, y: Float): Int? {
        val cell = gridManager.getCellIdFromCoords(x, y)
        if (cell == null) return null
        return cell
    }
    fun placeCardInCell(cardId: Int, row: Int, col: Int): Boolean {
        val card = loadedCards[cardId] ?: return false // Card doesn't exist
        if (!isCellEmpty(row, col)) return false // Cell is occupied

        // Remove card from its old cell if it was placed
        card.currentCell?.let { oldCell ->
            cellOccupancy.remove(oldCell)
        }

        // Place card in new cell
        cellOccupancy[Pair(row, col)] = card
        card.currentCell = Pair(row, col)
        return true
    }
    fun removeCardFromLoadedCards(cardId: Int): Card? = loadedCards.remove(cardId)

    fun removeCardFromCell(row: Int, col: Int): Card? {
        val cell = Pair(row, col)
        val cardRemoved = cellOccupancy.remove(cell) ?: return null
        val card = loadedCards[cardRemoved.cardId]
        card?.currentCell = null
        return card
    }
    fun getCardInCell(row: Int, col: Int): Card? {
        val card = cellOccupancy[Pair(row, col)] ?: return null
        return loadedCards[card.cardId]
    }
    fun clearGrid() {
        cellOccupancy.clear()
        loadedCards.values.forEach { it.currentCell = null }
    }

init {

}
}