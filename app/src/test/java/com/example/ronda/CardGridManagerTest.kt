package com.example.ronda

import com.example.ronda.domain.grid.GridManager
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test


class CardGridManagerTest {

    private lateinit var gridManager: GridManager
    private val testRows = 4
    private val testColumns = 4


    @Before
    fun setUp() {
        // Initialize CardGridManager before each test
        // If your CardGridManager takes a Grid instance, you'd create and pass it here.
        // For simplicity, assuming CardGridManager creates its own internal Grid based on rows/cols.
        gridManager = GridManager(testRows, testColumns)


        // Initialize canvas for coordinate tests (assuming 0-based Grid)
        // If your Grid is 1-based for getCellFromCoordinates output, adjust assertions
        gridManager.updateCanvasSize(300f, 300f) // Cell size will be 100x100
    }

    @Test
    fun updateCanvasSize_calculatesCellDimensionsCorrectly() {
        gridManager.updateCanvasSize(600f, 300f)
        // Assuming CardGridManager exposes cellWidth/Height from its internal Grid
        // or has its own properties updated.
        // This assertion depends on how CardGridManager exposes these values.
        // If it delegates to an internal Grid, you might not directly test them on CardGridManager,
        // but rather test the Grid class separately for this.
        // For now, let's assume it has some way to verify, or we test via coordinate conversion.

        // A better test for this might be via getCellForCoordinates
        val cell1 = gridManager.getCellIdFromCoords(50f, 50f)
        assertEquals(1, cell1)

        gridManager.updateCanvasSize(300f, 600f)
        val cell_1 = gridManager.getCellIdFromCoords(50f, 50f) // Should still be (0,0)
        assertEquals(1, cell_1)
    }
    @Test
    fun `getcellFromCoordinates returns correct cell 16 for given coordinates`() {
        gridManager.updateCanvasSize(100f, 100f)
        val cell16 = gridManager.getCellIdFromCoords(95f, 95f)
        assertEquals(16, cell16)
    }
    @Test
    fun `getcellFromCoordinates returns correct cell 11 for given coordinates`() {
        gridManager.updateCanvasSize(100f, 100f)
        val cell11 = gridManager.getCellIdFromCoords(50f, 50f)
        assertEquals(11, cell11)
    }

}
