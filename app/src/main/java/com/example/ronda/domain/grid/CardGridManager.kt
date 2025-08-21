package com.example.ronda.domain.grid

import com.example.ronda.domain.card.Card
import com.example.ronda.domain.card.CardType
import com.example.ronda.domain.card.User


private typealias CellId = Int
class CardGridManager(): GridManager(5, 5) {
    val grid = mutableMapOf<CellId, Card>()
    // The specific cell IDs to to populate
    val playingCellIds = listOf<CellId>(2, 3, 4, 22, 23, 24)
    val celIdsOnDeck = listOf<CellId>(7, 8, 9, 12, 13, 14, 17, 18, 19)
    val backCellIdOfPlayers = listOf<CellId>(1, 21)
    val backCellId = 6


    /**
     * To populate Back card of each player if they have any score
     * @param player1FirstScore if player 1 possess any score, then Back card will be in specified cell provided in the list
     * @param player2FirstScore the same for player2.
     */
    fun updateBackCellIds(player1FirstScore: Boolean = false, player2FirstScore: Boolean = false) {
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

    fun populateGridWithCards(cards: List<Card.Front>) {
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
        val countPerPlayer = 3
        // Populate cards of user 1
        var index = 0
        for (i in countPerPlayer until playingCellIds.size) {
            val myCards = shuffledCards.filter { it.owner == User.MYUSER }
            val cellIdToPopulate = playingCellIds[i]
            val cardToPlace = myCards[index]
            index ++
            // Before placing, you might want to check if the cellId is valid for the grid dimensions
            if (isValidCellId(cellIdToPopulate)) {
                grid[cellIdToPopulate] = cardToPlace
                println("User1 placed card ${cardToPlace.cardId} in cell $cellIdToPopulate")
            } else {
                println("Warning: Cell ID $cellIdToPopulate is not valid for this grid and was skipped.")
            }
            if (isValidCellId(backCellId)) {
                grid[backCellId] = Card.Back
            }
        }
        index = 0
        for (i in 0 until countPerPlayer) {
            val itsCards = shuffledCards.filter { it.owner == User.OTHERUSER }
            val cellIdToPopulate = playingCellIds[i]
            val cardToPlace = itsCards[index]
            index ++

            // Before placing, you might want to check if the cellId is valid for the grid dimensions
            if (isValidCellId(cellIdToPopulate)) {
                grid[cellIdToPopulate] = cardToPlace
                println("User2 placed card ${cardToPlace.cardId} in cell $cellIdToPopulate")
            } else {
                println("Warning: Cell ID $cellIdToPopulate is not valid for this grid and was skipped.")
            }
        }

    }


}


