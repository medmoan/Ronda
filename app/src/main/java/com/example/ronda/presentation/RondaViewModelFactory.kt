package com.example.ronda.presentation


import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class RondaViewModelFactory(
    private val applicationContext: Context
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        // Check if the requested ViewModel class is our RondaViewModel
        if (modelClass.isAssignableFrom(RondaViewModel::class.java)) {
            // If it is, create and return an instance of RondaViewModel,
            // passing the applicationContext to its constructor.
            @Suppress("UNCHECKED_CAST") // This cast is safe due to the isAssignableFrom check
            return RondaViewModel(applicationContext) as T
        }
        // If it's some other ViewModel class that this factory doesn't know how to create,
        // throw an exception.
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}
