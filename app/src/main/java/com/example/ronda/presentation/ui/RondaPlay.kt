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
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.withContext

const val TAG = "RondaPlay"

@Composable
fun RondaPlay(modifier: Modifier = Modifier) {
    var composableSize by remember { mutableStateOf(IntSize.Zero) }
    val context = LocalContext.current

    // Keep loaded images in a remembered list
    val cardImgBtmpListState = remember {
        mutableStateListOf<ImageBitmap>()
    }

    // Load images only once
    LaunchedEffect(Unit) {
        if (cardImgBtmpListState.isEmpty()) {
            val loadedImgBtmps = withContext(Dispatchers.IO) {
                val imgBtmps = mutableListOf<ImageBitmap>()
                for (i in 1..40) {
                    ensureActive()
                    loadOptimizedBitmap(context, "spanish_deck/$i.png", 50, 70)
                        .also { bmp ->
                            imgBtmps.add(bmp.asImageBitmap())
                            //bmp.recycle()
                        }
                }
                loadOptimizedBitmap(context, "spanish_deck/back.png", 50, 70)
                    .also { bmp ->
                        imgBtmps.add(bmp.asImageBitmap())
                        //bmp.recycle()
                    }
                imgBtmps
            }
            cardImgBtmpListState.addAll(loadedImgBtmps)
            Log.d(TAG, "Loaded ${cardImgBtmpListState.size} bitmaps")
        }
    }

    // Cleanup when leaving composition
    DisposableEffect(Unit) {
        onDispose {
            cardImgBtmpListState.clear()
            System.gc() // Not strictly needed, just a hint
            Log.d(TAG, "Card images cleared from memory")
        }
    }

    Box(
        modifier = modifier
            .onGloballyPositioned { coordinates ->
                val newSize = coordinates.size
                if (newSize != composableSize) {
                    composableSize = newSize
                    Log.d(TAG, "Logged once: ${newSize.width} x ${newSize.height}")
                }
            }
            .fillMaxSize()
    ) {
        val allCardsLoaded = cardImgBtmpListState.size == 41

        if (allCardsLoaded && composableSize != IntSize.Zero) {
            val currentSize = composableSize
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(Unit) {
                        detectTapGestures(onTap = { tapOffset ->
                            // handle tap
                        })
                    }
            ) {
                clipRect {
                    Log.d(TAG, "Canvas size: ${size.width} x ${size.height}")
                    val firstCard = cardImgBtmpListState.first()
                    drawImage(
                        image = firstCard,
                        dstSize = IntSize(50, 70),
                        dstOffset = IntOffset(0, (currentSize.height / 2) - (70 / 2))
                    )
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
    val (height: Int, width: Int) = options.outHeight to options.outWidth
    var inSampleSize = 1
    if (height > reqHeight || width > reqWidth) {
        val halfHeight = height / 2
        val halfWidth = width / 2
        while ((halfHeight / inSampleSize) >= reqHeight && (halfWidth / inSampleSize) >= reqWidth) {
            inSampleSize *= 2
        }
    }
    return inSampleSize
}