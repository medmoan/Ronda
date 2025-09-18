package com.example.ronda.presentation

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ronda.domain.card.Card // Your Card data class (ensure it has correct equals/hashCode)
import com.example.ronda.domain.rondaGame.GameState
import com.example.ronda.domain.rondaGame.RondaGame // Your game logic class
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*

class RondaViewModel(
    private val applicationContext: Context
) : ViewModel() {
    companion object {
        private const val TAG = "RondaViewModel"
    }
    private val rondaGame = RondaGame()

    private val cards = rondaGame.cards
    var cardToImageMap = MutableStateFlow<Map<Card.Front, ImageBitmap>>(emptyMap())
        private set
    val rondaState = rondaGame.rondaState
    val scores = rondaGame.playerScore
    val playerFlags = rondaGame.playerFlag

    val cellWidth get() = rondaGame.getCellWidth()
    val cellHeight get() = rondaGame.getCellHeight()
    private var isGameStartedInternally = false
    private var imageLoadingJob: Job? = null
    val totalGridRows: Int get() = rondaGame.totalGridRows
    val totalGridColumns: Int get() = rondaGame.totalGridColumns
    var backCardImage = MutableStateFlow<ImageBitmap?>(null)
        private set
    init {
        viewModelScope.launch {
            cards.collect { cards ->
                if (cards.cards.isEmpty()) {
                    clearCardToImageMap()
                }
                else {
                    if (cards.areCardsJustGenerated) loadImagesAndBuildMap(cards.cards)
                    else reconcileCardMapWithoutLoading(cards.cards)
                }
            }
            backCardImage.update { getCardBitmapFromAsset(applicationContext, 0) }
        }

    }
    private fun reconcileCardMapWithoutLoading(newCardList: List<Card.Front>) {
        imageLoadingJob?.cancel(CancellationException("Reconciling map, cancelling full load if active."))
        if (newCardList.size == cardToImageMap.value.size) return
        val cardsToKeep = newCardList
        cardToImageMap.update { currentMap ->
            val newMap = mutableMapOf<Card.Front, ImageBitmap>()
            for (card in cardsToKeep) {
                currentMap[card]?.let { existingBitmap ->
                    newMap[card] = existingBitmap
                }
            }
            if (newMap.size != cardsToKeep.size) {
                Log.w(TAG, "Reconcile: Some cards in the new list (${cardsToKeep.size}) did not have existing images in the map. Resulting map size: ${newMap.size}")
            }
            newMap
        }
        Log.d(TAG, "Reconciled card map. New map size: ${cardToImageMap.value.size}")
    }
    fun startGameIfNecessary() { // Renamed for clarity
        if (!isGameStartedInternally && rondaGame.rondaState.value.gameState == GameState.Ready) {
            Log.d(TAG, "startGameIfNecessary: Game not started internally, or is Ready. Calling rondaGame.start().")
            viewModelScope.launch {
                rondaGame.start() // This should set GameState to Play and areCardsGenerated to true
            }
            isGameStartedInternally = true
        } else {
            Log.d(TAG, "startGameIfNecessary: Game already started internally (isGameStartedInternally=$isGameStartedInternally, gameState=${rondaGame.rondaState.value.gameState}). Not calling start().")
        }
    }
    private fun loadImagesAndBuildMap(cardsToProcess: List<Card.Front>) {

        imageLoadingJob?.cancel(CancellationException("New card data arrived; cancelling previous image loading."))
        Log.d(TAG, "loadImagesAndBuildMap: START for ${cardsToProcess.size} cards. First card: ${cardsToProcess.firstOrNull()}")

        cardToImageMap.update {
            emptyMap()
        }

        imageLoadingJob = viewModelScope.launch(Dispatchers.IO) {
            val newCardImageMap = mutableMapOf<Card.Front, ImageBitmap>()
            val jobStartTime = System.currentTimeMillis()
            Log.d(TAG, "imageLoadingJob [${this.coroutineContext[Job]}] SPAWNED for map construction with ${cardsToProcess.size} cards.")

            cardsToProcess.forEachIndexed { index, card ->
                if (!isActive) { // Check if the current coroutine (imageLoadingJob) has been cancelled
                    Log.w(TAG, "imageLoadingJob [${this.coroutineContext[Job]}] CANCELLED during loop at index $index for card: $card.")
                    return@launch
                }
                try {
                    Log.d(TAG, "imageLoadingJob [${this.coroutineContext[Job]}] Loading image for card (ID: ${card.cardId}): $card")
                    val imageBitmap = getCardBitmapFromAsset(applicationContext, card.cardId)
                    newCardImageMap[card] = imageBitmap
                    } catch (e: Exception) {
                        if (e is CancellationException) {
                            Log.w(TAG, "imageLoadingJob [${this.coroutineContext[Job]}] Actively caught CancellationException for card: $card (ID: ${card.cardId})")
                            throw e
                        }
                    }
            }

            if (isActive) {
                cardToImageMap.update { newCardImageMap }
                val duration = System.currentTimeMillis() - jobStartTime
                Log.i(TAG, "imageLoadingJob [${this.coroutineContext[Job]}] COMPLETED SUCCESSFULLY in ${duration}ms. Constructed map with ${newCardImageMap.size} Card->ImageBitmap pairs for ${cardsToProcess.size} initial cards.")
            } else {
                Log.w(TAG, "imageLoadingJob [${this.coroutineContext[Job]}] Was CANCELLED before final map update. Loaded image count in temp map: ${newCardImageMap.size}")
            }
        }
    }

    private fun clearCardToImageMap() {
        imageLoadingJob?.cancel(CancellationException("Clearing card-to-image map explicitly requested."))
        if (cardToImageMap.value.isNotEmpty()) {
            cardToImageMap.update { emptyMap() }
            Log.d(TAG, "ViewModel's card-to-image map cleared.")
        } else {
            Log.d(TAG, "ViewModel's card-to-image map already empty, no action taken on clear request.")
        }
    }
    private suspend fun getCardBitmapFromAsset(context: Context, cardIdForAsset: Int): ImageBitmap {
        return withContext(Dispatchers.IO) {
            Log.d(TAG, "getCardBitmapFromAssetInternal: Loading asset for card ID: $cardIdForAsset")
            val assetPath = if (cardIdForAsset in 1..40) "spanish_deck/$cardIdForAsset.png" else "spanish_deck/back.png"
            val reqWidth = if (cardIdForAsset in 1..40) 50 else 70
            val reqHeight = if (cardIdForAsset in 1..40) 70 else 100
            val bmp = loadOptimizedBitmapInternal(context, assetPath, reqWidth, reqHeight)
            bmp.asImageBitmap()
        }
    }

    private fun loadOptimizedBitmapInternal(context: Context, assetPath: String, reqWidth: Int, reqHeight: Int): Bitmap {
        val options = BitmapFactory.Options().apply {
            inJustDecodeBounds = true
        }
        try {
            context.assets.open(assetPath).use { BitmapFactory.decodeStream(it, null, options) }
        } catch (e: Exception) {
            Log.e(TAG, "Error decoding bounds for $assetPath", e)
            throw e
        }


        options.inSampleSize = calculateInSampleSizeInternal(options, reqWidth, reqHeight)
        options.inJustDecodeBounds = false

        return try {
            context.assets.open(assetPath).use { BitmapFactory.decodeStream(it, null, options)!! }
        } catch (e: Exception) {
            Log.e(TAG, "Error decoding bitmap for $assetPath with inSampleSize=${options.inSampleSize}", e)
            throw e
        }
    }

    private fun calculateInSampleSizeInternal(options: BitmapFactory.Options, reqWidth: Int, reqHeight: Int): Int {
        val (height: Int, width: Int) = options.outHeight to options.outWidth
        var inSampleSize = 1
        if (height == 0 || width == 0) return 1

        if (height > reqHeight || width > reqWidth) {
            val halfHeight: Int = height / 2
            val halfWidth: Int = width / 2
            while (halfHeight / inSampleSize >= reqHeight && halfWidth / inSampleSize >= reqWidth) {
                inSampleSize *= 2
            }
        }
        Log.d(TAG, "Calculated inSampleSize: $inSampleSize for reqW=$reqWidth, reqH=$reqHeight from W=$width, H=$height")
        return inSampleSize
    }

    fun updateCanvasSize(width: Float, height: Float) {
        rondaGame.updateCanvasSize(width, height)
    }

    fun getCardFromCell(cell: Int): Card? {
        return rondaGame.getCardFromCell(cell)
    }

    fun pickDropCard(card: Card.Front) {
        Log.d(TAG, "pickDropCard action called by UI for card: $card")
        viewModelScope.launch {
            rondaGame.pickDropCard(card)
        }
    }

    fun getCoordsFromCard(card: Card): Pair<Float, Float>? {
        val cell = rondaGame.getCellFromCard(card)
        return rondaGame.getCoordsFromCell(cell)
    }
    override fun onCleared() {
        super.onCleared()
        imageLoadingJob?.cancel(CancellationException("ViewModel is getting cleared."))
        cardToImageMap.update { it.toMutableMap().also { it.clear() }.toMap() }
        Log.d(TAG, "ViewModel cleared. Image loading job cancelled if it was active.")
    }
}
