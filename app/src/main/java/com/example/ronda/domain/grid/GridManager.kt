package com.example.ronda.domain.grid


/**
 * Represents a generic grid structure.
 * Manages the dimensions of the grid, individual cell sizes,
 * and provides utilities for coordinate and cell conversions.
 *
 * @property totalRows The total number of rows in the grid.
 * @property totalColumns The total number of columns in the grid.
 */
abstract class GridManager(
    val totalRows: Int,
    val totalColumns: Int
) {
    private var canvasWidth: Float = 0f
    private var canvasHeight: Float = 0f

    var cellWidth: Float = 0f
        private set
    var cellHeight: Float = 0f
        private set

    init {
        require(totalRows > 0) { "Grid must have at least one row." }
        require(totalColumns > 0) { "Grid must have at least one column." }
    }
    fun getTotalCellCount(): Int {
        return totalRows * totalColumns
    }
    fun updateCanvasSize(newWidth: Float, newHeight: Float) {
        if (newWidth < 0f || newHeight < 0f) {
            canvasWidth = 0f
            canvasHeight = 0f
            cellWidth = 0f
            cellHeight = 0f
            return
        }
        canvasWidth = newWidth
        canvasHeight = newHeight

        cellWidth = if (totalColumns > 0) {
            newWidth / totalColumns
        } else {
            0f
        }
        if (totalRows > 0) {
            cellHeight = newHeight / totalRows
        } else {
            cellHeight = 0f
        }
    }

    /**
     * Converts screen coordinates (e.g., from a touch event) to a 1-based linear cell number.
     * Cell numbers are in row-major order (e.g., for a 3x3 grid: 1,2,3 for the first row,
     * 4,5,6 for the second, etc.).
     *
     * @param x The x-coordinate on the canvas.
     * @param y The y-coordinate on the canvas.
     * @return An Int representing the 1-based cell number if the coordinates are valid, otherwise null.
     */
    fun getCellIdFromCoords(x: Float, y: Float): Int? {
        if (cellWidth == 0f || cellHeight == 0f) return null
        if (x < 0f || x >= canvasWidth || y < 0f || y >= canvasHeight) return null

        // Calculate 0-based row and column indices first
        val zeroBasedCol = (x / cellWidth).toInt().coerceIn(0, totalColumns - 1)
        val zeroBasedRow = (y / cellHeight).toInt().coerceIn(0, totalRows - 1)

        // Calculate 0-based linear cell index (row-major order)
        // For a cell at (r, c) in a grid with C columns, the 0-based index is r * C + c
        val zeroBasedLinearIndex = zeroBasedRow * totalColumns + zeroBasedCol

        // Convert to 1-based cell number for the return value
        return zeroBasedLinearIndex + 1
    }

    /**
     * Get a cell map (Row, Col).
     * This is useful if you need to work with row/column concepts after getting a cell Id.
     *
     * @param cellId The 1-based linear cell number (1 to totalRows * totalColumns).
     * @return A Pair representing (0-based row, 0-based col), or null if the cell number is invalid.
     */
    fun getRowColFromCellId(cellId: Int?): Pair<Int, Int>? {
        if (cellId == null) return null
        if (cellId < 1 || cellId > totalRows * totalColumns) {
            return null // Invalid cell number
        }
        // Convert 1-based cell number to 0-based linear index
        val zeroBasedLinearIndex = cellId - 1

        // Calculate 0-based row and column
        val zeroBasedRow = zeroBasedLinearIndex / totalColumns
        val zeroBasedCol = zeroBasedLinearIndex % totalColumns

        return Pair(zeroBasedRow + 1, zeroBasedCol + 1)
    }


    /**
     * Calculates the top-left (x, y) screen coordinates for a given 1-based linear cell number.
     *
     * @param cellId The 1-based linear cell number.
     * @return A Pair representing (x, y) screen coordinates, or null if cell number is invalid or grid not initialized.
     */
    fun getCoordsFromCellId(cellId: Int): Pair<Float, Float>? {
        val rowCol = getRowColFromCellId(cellId) ?: return null // Get 0-based row/col

        // Reuse existing logic (or reimplement if you remove the row/col based one)
        // For this, we need a way to get coordinates from 0-based row/col
        return getCoordsFromZeroBasedCell(rowCol.first + 1, rowCol.second + 1)
    }

    /**
     * Helper: Calculates top-left for a 0-based cell. (Kept for internal use or direct 0-based access)
     */
    private fun getCoordsFromZeroBasedCell(zeroBasedRow: Int, zeroBasedCol: Int): Pair<Float, Float>? {
        if (zeroBasedRow < 0 || zeroBasedRow >= totalRows || zeroBasedCol < 0 || zeroBasedCol >= totalColumns) {
            return null
        }
        if (cellWidth == 0f || cellHeight == 0f) {
            return null
        }
        val x = zeroBasedCol * cellWidth
        val y = zeroBasedRow * cellHeight
        return Pair(x, y)
    }


    /**
     * Calculates the center (x, y) screen coordinates for a given 1-based linear cell number.
     *
     * @param cellId The 1-based linear cell number.
     * @return A Pair representing (x, y) screen coordinates of the cell's center, or null.
     */
    fun getCellCenterCoordsFromCellId(cellId: Int): Pair<Float, Float>? {
        val topLeft = getCoordsFromCellId(cellId) ?: return null
        return Pair(topLeft.first + cellWidth / 2, topLeft.second + cellHeight / 2)
    }

    /**
     * Checks if the given 1-based linear cell number is valid for this grid.
     *
     * @param cellId The 1-based linear cell number.
     * @return True if the cell number is within the grid's bounds, false otherwise.
     */
    fun isValidCellId(cellId: Int): Boolean {
        return cellId in 1..(totalRows * totalColumns)
    }

    override fun toString(): String {
        return "GridWithCellId(rows=$totalRows, cols=$totalColumns, canvasW=$canvasWidth, canvasH=$canvasHeight, cellW=$cellWidth, cellH=$cellHeight)"
    }
}