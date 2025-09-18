package com.example.ronda.domain.grid

import com.example.ronda.domain.card.Card
import com.example.ronda.domain.rondaGame.Flag


private typealias Cell = Int
class CardGridManager(): GridManager(5, 5) {
    val grid = mutableMapOf<Cell, Card>()

    val playingCells = listOf<Cell>(2, 3, 4, 22, 23, 24)
    val deckCells = listOf<Cell>(7, 8, 9, 12, 13, 14, 17, 18, 19)
    val playingBackCells = listOf<Cell>(1, 21)
    val playingFlagCells = listOf<Cell>(5 ,25) // Ronda or Tringa
    val backCell = 6


    fun updateFlagPlayerCells(player1Flag: Flag, player2Flag: Flag) {
        if (playingFlagCells.size != 2) return
        val player1FlagCellId = playingFlagCells.last()
        if (isValidCellId(player1FlagCellId))
            grid[player1FlagCellId] = Card.Flag(player1Flag)
        val player2FlagCellId = playingFlagCells.first()
        if (isValidCellId(player2FlagCellId))
            grid[player1FlagCellId] = Card.Flag(player2Flag)
    }
    fun getFlagFromCell(cell: Int): Card.Flag {
        return grid[cell] as Card.Flag
    }

    /**
     * To populate Back card of each player if they have any score
     * @param player1FirstScore if player 1 possess any score, then Back card will be in specified cell provided in the list
     * @param player2FirstScore the same for player2.
     */
    fun updateBackPlayerCells(player1FirstScore: Boolean = false, player2FirstScore: Boolean = false) {
        if (playingCells.size != 2) return
        if (player1FirstScore) {
            val player1BackCellId = playingBackCells.last()
            if (isValidCellId(player1BackCellId))
                grid[player1BackCellId] = Card.Back
        }
        if (player2FirstScore) {
            val player2BackCellId = playingBackCells.first()
            if (isValidCellId(player2BackCellId))
                grid[player2BackCellId] = Card.Back
        }
    }
    fun addCardToCell(cell: Int, card: Card) {
        if (isValidCellId(cell)) {
            grid[cell] = card
        } else {
            throw IllegalArgumentException("Invalid cell: $cell")
        }
    }

    fun isCellOccupied(cell: Int): Boolean {
        return grid.containsKey(cell)
    }
    fun getOccupiedDeckCells(): List<Cell> {
        return deckCells.filter { grid.containsKey(it) }
    }
    fun isPlayingCellsEmpty(): Boolean {
        return playingCells.none { grid.containsKey(it) }
    }
    fun isDeckCellsEmpty(): Boolean {
        return deckCells.none { grid.containsKey(it) }
    }
    fun getEmptyDeckCells(): List<Cell> {
        return deckCells.filter { !grid.containsKey(it) }
    }
    fun removeCardFromCell(cell: Cell) {
        if (isValidCellId(cell)) {
            grid.remove(cell)
        } else {
            throw IllegalArgumentException("Invalid cell: $cell")
        }
    }
    fun getCardFromCell(cell: Int): Card? {
        if (cell > getTotalCellCount()) throw IndexOutOfBoundsException("Cell $cell doesn't exist")
        return grid[cell]
    }
    fun getCellFromCard(card: Card): Cell? {
        return grid.entries.find { it.value == card }?.key
    }
}


