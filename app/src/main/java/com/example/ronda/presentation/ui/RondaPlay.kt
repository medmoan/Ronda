package com.example.ronda.presentation.ui

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import com.example.ronda.presentation.RondaViewModel
import kotlinx.coroutines.*
import androidx.lifecycle.viewmodel.compose.viewModel

private const val TAG = "RondaPlay"

// LRU cache for ImageBitmap

@Composable
fun RondaPlay(modifier: Modifier = Modifier) {
    var composableSize by remember { mutableStateOf(IntSize.Zero) }
    val context = LocalContext.current
    val appContext = context.applicationContext
    val scope = rememberCoroutineScope()





    suspend fun getCard(index: Int): ImageBitmap {
        return withContext(Dispatchers.IO) {
            val assetPath = if (index in 1..40) "spanish_deck/$index.png" else "spanish_deck/back.png"
            val reqWidth = if (index in 1..40) 50 else 70
            val reqHeight = if (index in 1..40) 70 else 100
            val bmp = loadOptimizedBitmap(
                context,
                assetPath,
                reqWidth,
                reqHeight
            )
            val img = bmp.asImageBitmap()
            Log.d(TAG, "Loaded card $index into cache")
            img
        }
    }

    // Preload cards in background
//    fun preloadCards(indices: List<Int>) {
//        scope.launch {
//            indices.forEach { idx ->
//                if (!loadedCards.containsKey(idx)) {
//                    try {
//                        getCard(idx)
//                    } catch (e: Exception) {
//                        if (e is CancellationException) throw e
//                        Log.e(TAG, "Error preloading card $idx", e)
//
//                    }
//                }
//            }
//        }
//    }



    Box(
        modifier = modifier
            .onGloballyPositioned { coordinates ->
                val newSize = coordinates.size
                if (newSize != composableSize) {
                    composableSize = newSize
                    Log.d(TAG, "Canvas size: ${newSize.width} x ${newSize.height}")
                }
            }
            .fillMaxSize()
    ) {
        if (composableSize != IntSize.Zero) {

            val height = composableSize.height.toFloat()
            val loadedCards = remember { mutableStateListOf<ImageBitmap?>() }
            val rondaViewModel = viewModel<RondaViewModel>()
            LaunchedEffect(Unit) {
                //if (launched) return@LaunchedEffect
                // Load first card immediately
                loadedCards.add(getCard(41))

                //ondaViewModel.getCard(appContext, 41)
                // Preload a few next cards in background
                //preloadCards(listOf(2, 3, 4, 5).shuffled())
            }

            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .testTag("RonaCardCanvas")
                    .pointerInput(Unit) {
                        detectTapGestures {
                            // Example: load another card when tapped
                            scope.launch {
                                loadedCards.add(getCard((2..40).random()))
                                //preloadCards(listOf((2..40).random()))
                            }
                        }
                    }
            ) {
                clipRect {
                    if (loadedCards.isEmpty()) return@clipRect
                    val image = loadedCards.last() ?: return@clipRect
                    drawImage(
                        image = image,
                        dstSize = IntSize(image.width, image.height),
                        dstOffset = IntOffset(
                            0,
                            (composableSize.height / 2) - (image.height / 2)
                        )
                    )

//                    deckCard?.let {
//                        drawImage(
//                            image = it,
//                            dstSize = IntSize(50, 70),
//                            dstOffset = IntOffset(
//                                0,
//                                (composableSize.height / 2) - (70 / 2)
//                            )
//                        )
//                    }
                }
            }
        }
    }
}

fun loadOptimizedBitmap(context: Context, assetPath: String, reqWidth: Int, reqHeight: Int): Bitmap {
    val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
    context.assets.open(assetPath).use {
        BitmapFactory.decodeStream(it, null, options)
    }
    options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight)
    options.inJustDecodeBounds = false
    return context.assets.open(assetPath).use {
        BitmapFactory.decodeStream(it, null, options)!!
    }
}

fun calculateInSampleSize(options: BitmapFactory.Options, reqWidth: Int, reqHeight: Int): Int {
    val (height, width) = options.outHeight to options.outWidth
    var inSampleSize = 1
    if (height > reqHeight || width > reqWidth) {
        val halfHeight = height / 2
        val halfWidth = width / 2
        while ((halfHeight / inSampleSize) >= reqHeight &&
            (halfWidth / inSampleSize) >= reqWidth
        ) {
            inSampleSize *= 2
        }
    }
    return inSampleSize
}
