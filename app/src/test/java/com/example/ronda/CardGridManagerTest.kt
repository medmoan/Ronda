package com.example.ronda

import com.example.ronda.domain.card.Card
import com.example.ronda.domain.card.CardType
import com.example.ronda.domain.card.getCell
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import com.example.ronda.domain.grid.CardGridManager

@Suppress("UNCHECKED_CAST")
class CardGridManagerTest {

    private lateinit var cardGridManager: CardGridManager



    @Before
    fun setUp() {
        // Initialize CardGridManager before each test
        // If your CardGridManager takes a Grid instance, you'd create and pass it here.
        // For simplicity, assuming CardGridManager creates its own internal Grid based on rows/cols.
        val cards = mutableListOf<Card.Front>()
        for (i in 0..5) {
            val type = CardType.Dhab
            val num = i + 1
            cards.add(Card.Front(type, num))
        }
        cardGridManager = CardGridManager(cards)


        // Initialize canvas for coordinate tests (assuming 0-based Grid)
        // If your Grid is 1-based for getCellFromCoordinates output, adjust assertions
        cardGridManager.updateCanvasSize(300f, 300f) // Cell size will be 100x100
    }

    @Test
    fun updateCanvasSize_calculatesCellDimensionsCorrectly() {
        cardGridManager.updateCanvasSize(600f, 300f)
        // Assuming CardGridManager exposes cellWidth/Height from its internal Grid
        // or has its own properties updated.
        // This assertion depends on how CardGridManager exposes these values.
        // If it delegates to an internal Grid, you might not directly test them on CardGridManager,
        // but rather test the Grid class separately for this.
        // For now, let's assume it has some way to verify, or we test via coordinate conversion.

        // A better test for this might be via getCellForCoordinates
        val cell1 = cardGridManager.getCellIdFromCoords(50f, 50f)
        assertEquals(1, cell1)

        cardGridManager.updateCanvasSize(300f, 600f)
        val cell_1 = cardGridManager.getCellIdFromCoords(50f, 50f) // Should still be (0,0)
        assertEquals(1, cell_1)
    }
    @Test
    fun `getCellIdFromCoords returns correct cell 25 for given coordinates`() {
        cardGridManager.updateCanvasSize(100f, 100f)
        val cell25 = cardGridManager.getCellIdFromCoords(95f, 95f)
        assertEquals(25, cell25)
    }
    @Test
    fun `getCellIdFromCoords returns correct cell 11 for given coordinates`() {
        cardGridManager.updateCanvasSize(100f, 100f)
        val cell13 = cardGridManager.getCellIdFromCoords(50f, 50f)
        assertEquals(13, cell13)
    }
    @Test
    fun `CellId for Back card should return 6`() {
        cardGridManager.updateCanvasSize(100f, 100f)
        val card = Card.Back
        assertEquals(6, cardGridManager.getCellIdFromCard(card))
    }



}
