package com.example.ronda.domain.grid

import com.example.ronda.domain.card.Card
import com.example.ronda.domain.card.CardType

private typealias CellId = Int
class CardGridManager(
    val cards: List<Card.Front>
): GridManager(5, 5) {
    val grid = mutableMapOf<CellId, Card>()
    // The specific cell IDs to to populate
    val playingCellIds = listOf<CellId>(2, 3, 4, 22, 23, 24)
    val celIdsOnDeck = listOf<CellId>(7, 8, 9, 12, 13, 14, 17, 18, 19)
    val backCellIdOfPlayers = listOf<CellId>(1, 21)
    val backCellId = 6
    init {
        require(cards.size == 6) {
            "CardGridManager requires exactly 6 cards. Provided: ${cards.size}"
        }
        populateGridWithCards()
    }

    /**
     * To populate Back card of each player if they have any score
     * @param player1FirstScore if player 1 possess any score, then Back card will be in specified cell provided in the list
     * @param player2FirstScore the same for player2.
     */
    fun updatePlayerBackCells(player1FirstScore: Boolean = false, player2FirstScore: Boolean = false) {
        if (playingCellIds.size != 2) return
        if (player1FirstScore) {
            val player1BackCellId = backCellIdOfPlayers.last()
            if (isValidCellId(player1BackCellId))
                grid[player1BackCellId] = Card.Back
        }
        if (player2FirstScore) {
            val player2BackCellId = backCellIdOfPlayers.first()
            if (isValidCellId(player2BackCellId))
                grid[player2BackCellId] = Card.Back
        }
    }

    fun getCardFromCellId(cellId: Int): Card? {
        if (cellId > getTotalCellCount()) throw IndexOutOfBoundsException("Cell $cellId doesn't exist")
        return grid[cellId]
    }
    fun getCellIdFromCard(card: Card): CellId? {
        return grid.entries.find { it.value == card }?.key
    }

    private fun populateGridWithCards() {
        // Ensure the grid is empty before populating in case this method is being called multiple times
        grid.clear()
        // Ensure we have the same sizes of both target cells and the cards (should be 6 in this case)
        require(playingCellIds.size == cards.size) {
            "The number of target cell IDs (${playingCellIds.size}) must match the number of cards (${cards.size})."
        }

        // Shuffle the cards to place them randomly in the target cells
        val shuffledCards = cards.toMutableList()
        shuffledCards.shuffle()

        // Iterate over the target cell IDs and assign one card to each
        for (index in playingCellIds.indices) { //  0 to 5
            val cellIdToPopulate = playingCellIds[index]
            val cardToPlace = shuffledCards[index]

            // Before placing, you might want to check if the cellId is valid for the grid dimensions
            if (isValidCellId(cellIdToPopulate)) {
                grid[cellIdToPopulate] = cardToPlace
                println("Placed card ${cardToPlace.cardId} in cell $cellIdToPopulate")
            } else {
                println("Warning: Cell ID $cellIdToPopulate is not valid for this grid and was skipped.")
            }
        }
        if (isValidCellId(backCellId)) grid[backCellId] = Card.Back
    }


}

fun main() {
    val cards = mutableListOf<Card.Front>()
    for (i in 0..5) {
        val type = CardType.Dhab
        val num = i + 1
        cards.add(Card.Front(type, num))
    }



    val cardGridManager = CardGridManager( cards).apply {
        updateCanvasSize(100f, 100f)
        println(grid)
        println("\nTotal cells in grid (5x5): ${getTotalCellCount()}") // Should be 25
        println("Number of cards placed: ${grid.size}") // Should be 6
        println(getCardFromCellId(25))
        println("${getCellIdFromCard(cards[4])} and cardId is ${cards[4].cardId}")
    }
}

