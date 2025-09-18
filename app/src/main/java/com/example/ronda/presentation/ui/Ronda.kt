package com.example.ronda.presentation.ui

// Import Context if your Composable needs it for other reasons, but not for bitmap loading now
// import android.content.Context
// import android.graphics.Bitmap // Not directly used by Composable for loading
// import android.graphics.BitmapFactory // Not directly used by Composable for loading
import android.util.Log
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
// import androidx.compose.ui.graphics.asImageBitmap // Not needed if ViewModel provides ImageBitmap
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
// import androidx.compose.ui.platform.LocalContext // Not needed for bitmap loading
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ronda.domain.card.Card
import com.example.ronda.presentation.RondaViewModel // Import your ViewModel

// Assuming Card related domain classes are correctly imported
// import com.example.ronda.domain.card.User
// import com.example.ronda.domain.rondaGame.Flags
// import com.example.ronda.domain.rondaGame.RondaState

private const val TAG = "RondaPlayComposable"

@Composable
fun Ronda(
    modifier: Modifier = Modifier,
    viewModel: RondaViewModel // Pass the ViewModel instance

) {
    var composableSize by remember { mutableStateOf(IntSize.Zero) }
    val cardToImageMap by viewModel.cardToImageMap.collectAsStateWithLifecycle()
    LaunchedEffect(cardToImageMap) {

            cardToImageMap.forEach { (cardKey, valueImg) ->
                val coords = viewModel.getCoordsFromCard(cardKey) // Assuming this is your chain
                Log.d(TAG, "the map key is $cardKey and coords are $coords")
        }
    }



    Box(
        modifier = modifier
            .onGloballyPositioned { coordinates ->
                val newSize = coordinates.size
                if (newSize != composableSize) {
                    composableSize = newSize
                    viewModel.updateCanvasSize(newSize.width.toFloat(), newSize.height.toFloat())
                    Log.d(TAG, "Canvas container size: ${newSize.width} x ${newSize.height}")
                }
            }
            .fillMaxSize()
    ) {
        if (composableSize != IntSize.Zero) {
            val totalRows = viewModel.totalGridRows
            val totalColumns = viewModel.totalGridColumns
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(Unit) {
                        detectTapGestures {

                            if (cardToImageMap.size == 6) {
                                val randomCard =
                                    cardToImageMap.keys.last()
                                val randomCardImage = cardToImageMap[randomCard]

                            }
                        }
                    }
            ) {
                clipRect {
                    if (totalRows <= 0 || totalColumns <= 0) return@clipRect

                    val cellWidth = viewModel.cellWidth
                    val cellHeight = viewModel.cellHeight


                    if (cellWidth <= 0f || cellHeight <= 0f) {

                        return@clipRect
                    }

                    for (row in 0 until totalRows) {
                        for (col in 0 until totalColumns) {
                            val cell = row * totalColumns + col + 1

                            val x = col * cellWidth
                            val y = row * cellHeight
                            val cellTopLeft = Offset(x, y)


                            val cardInCell =
                                viewModel.getCardFromCell(cell)

                            if (cardInCell != null && cardInCell is Card.Front) {
                                val imageBitmap =
                                    cardToImageMap[cardInCell]
                                if (imageBitmap != null) {
                                    if (col == 1 && row == 0) {
                                        drawImage(
                                            image = imageBitmap,
                                            dstSize = IntSize(
                                                (cellWidth * 0.4f).toInt(), // Example: 90% of cell
                                                (cellHeight * 0.9f).toInt()
                                            ),
                                            dstOffset = IntOffset(
                                                (cellTopLeft.x).toInt() + cellWidth.toInt() / 2, // Centered
                                                (cellTopLeft.y + cellHeight * 0.05f).toInt()
                                            )
                                        )
                                    }
                                    else if (col == 2 && row == 0) {
                                        drawImage(
                                            image = imageBitmap,
                                            dstSize = IntSize(
                                                (cellWidth * 0.4f).toInt(), // Example: 90% of cell
                                                (cellHeight * 0.9f).toInt()
                                            ),
                                            dstOffset = IntOffset(
                                                (cellTopLeft.x).toInt() + (viewModel.cellWidth).toInt() / 2 - imageBitmap.width /2, // Centered
                                                (cellTopLeft.y + cellHeight * 0.05f).toInt()
                                            )
                                        )
                                    }
                                    else {
                                        drawImage(
                                            image = imageBitmap,
                                            dstSize = IntSize(
                                                (cellWidth * 0.4f).toInt(), // Example: 90% of cell
                                                (cellHeight * 0.9f).toInt()
                                            ),
                                            dstOffset = IntOffset(
                                                (cellTopLeft.x + cellWidth * 0.05f).toInt(), // Centered
                                                (cellTopLeft.y + cellHeight * 0.05f).toInt()
                                            )
                                        )
                                    }

                                } else {
                                    // Card exists in grid model, but no image loaded (shouldn't happen with your current VM logic)
                                    // Draw a placeholder for "image loading" or "image error"
                                    drawRect(
                                        color = Color.Magenta, // Error/missing image placeholder
                                        topLeft = cellTopLeft.plus(
                                            Offset(
                                                cellWidth * 0.1f,
                                                cellHeight * 0.1f
                                            )
                                        ),
                                        size = Size(
                                            cellWidth * 0.8f,
                                            cellHeight * 0.8f
                                        )
                                    )
                                }
//                            } else if (cardInCell != null && cardInCell is Card.Back) {
//                                // Handle drawing Card.Back if it has a specific image
//                                val backImageBitmap =
//                                    cardToImageMap[cardInCell] // Or a specific back image
//                                if (backImageBitmap != null) { /* drawImage */
//                                }
                            } else if (cardInCell != null && cardInCell is Card.Flag) {
                                // Handle drawing Card.Flag
                                // You'd need a way to get an ImageBitmap for flags too
                            } else {
                                // Cell is empty
                                // --- Option 3: Draw something for empty cells ---
                                drawRect(
                                    color = Color.LightGray.copy(alpha = 0.3f),
                                    topLeft = cellTopLeft,
                                    size = Size(cellWidth, cellHeight)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}