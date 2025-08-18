package com.example.ronda.presentation

import android.util.Log
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.ui.graphics.ImageBitmap
import androidx.lifecycle.ViewModel


class RondaViewModel() : ViewModel() {
    companion object {
        private const val TAG = "RondaViewModel"
    }
    val loadedCards = mutableStateListOf<ImageBitmap?>()

//    private val _loadedCards= mutableStateListOf<Piece>()
//    val loadedCards: List<Piece> get() = _loadedCards

    override fun onCleared() {
        super.onCleared()
        System.gc()
        Log.d(TAG, "Cleared cached deck in ViewModel")
    }
}
