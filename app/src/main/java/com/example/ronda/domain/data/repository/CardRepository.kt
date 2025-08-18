//package com.example.ronda.domain.data.repository
//
//// In your data layer (e.g., com.example.ronda.data)
//import android.content.Context
//import androidx.compose.ui.geometry.Offset
//import androidx.compose.ui.geometry.Size
//import androidx.compose.ui.graphics.ImageBitmap
//import androidx.compose.ui.graphics.asImageBitmap
//import androidx.compose.ui.unit.IntSize
//import kotlinx.coroutines.Dispatchers
//import kotlinx.coroutines.withContext
//
//interface CardRepository {
//    suspend fun getCardImage(index: Int): ImageBitmap
//}
//
//class CardRepositoryImpl(
//    private val applicationContext: Context
//): CardRepository {
//
//    override suspend fun getCardImage(index: Int): ImageBitmap {
//        // The core logic of your getCard function is now here
//        return withContext(Dispatchers.IO) {
//            val assetPath = if (index in 1..40) "spanish_deck/$index.png" else "spanish_deck/back.png"
//            val reqWidth = 50 // Define these based on your needs, maybe pass them or configure them
//            val reqHeight = 70
//
//            // Assuming loadOptimizedBitmap is accessible here or passed/injected
//            // For this example, let's assume it's a top-level function or in a utility class
//            val bmp = loadOptimizedBitmap(
//                applicationContext,
//                assetPath,
//                reqWidth,
//                reqHeight
//            )
//            bmp.asImageBitmap()
//        }
//    }
//
//    // Your loadOptimizedBitmap function would also live here or be accessible
//    // It's good practice to keep it close to where it's used if it's specific to this repo
//    private fun loadOptimizedBitmap(context: Context, assetPath: String, reqWidth: Int, reqHeight: Int): android.graphics.Bitmap {
//        // ... (your existing implementation of loadOptimizedBitmap)
//        val options = android.graphics.BitmapFactory.Options().apply { inJustDecodeBounds = true }
//        context.assets.open(assetPath).use {
//            android.graphics.BitmapFactory.decodeStream(it, null, options)
//        }
//        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight)
//        options.inJustDecodeBounds = false
//        return context.assets.open(assetPath).use {
//            android.graphics.BitmapFactory.decodeStream(it, null, options)!!
//        }
//    }
//
//    private fun calculateInSampleSize(options: android.graphics.BitmapFactory.Options, reqWidth: Int, reqHeight: Int): Int {
//        // ... (your existing implementation)
//        val (height, width) = options.outHeight to options.outWidth
//        var inSampleSize = 1
//        if (height > reqHeight || width > reqWidth) {
//            val halfHeight = height / 2
//            val halfWidth = width / 2
//            while ((halfHeight / inSampleSize) >= reqHeight &&
//                (halfWidth / inSampleSize) >= reqWidth
//            ) {
//                inSampleSize *= 2
//            }
//        }
//        return inSampleSize
//    }
//}
