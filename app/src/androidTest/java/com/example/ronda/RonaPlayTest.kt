package com.example.ronda.presentation.ui

import androidx.activity.result.launch
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

// A helper or mock image loading function for tests.
// In a real app, you might use a Test Dispatcher or a fake/mock image loader.
suspend fun loadTestImageBitmap(width: Int = 100, height: Int = 150, delayMs: Long = 50): ImageBitmap {
    delay(delayMs) // Simulate network/disk load time
    return ImageBitmap(width, height) // Create a dummy ImageBitmap
}

@RunWith(AndroidJUnit4::class)
class RondaPlayDrawingTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun canvasIsDisplayedAndInitialCardStateIsSet() {
        // This state variable is to capture the ImageBitmap's hash from within setContent
        // to verify it was set by LaunchedEffect. This is a bit of a workaround for
        // directly checking the Canvas content.
        var displayedBitmapHash: Int? = null

        composeTestRule.setContent {
            // Re-implement a minimal version of RondaPlay's state logic for this test
            // to observe the cardToDisplay state change.
            var cardToDisplayForTest by remember { mutableStateOf<ImageBitmap?>(null) }

            // Update our test-observable variable when cardToDisplayForTest changes
            displayedBitmapHash = cardToDisplayForTest?.hashCode()

            // Simulate the LaunchedEffect that loads the initial card
            LaunchedEffect(Unit) {
                val image = loadTestImageBitmap(width = 70, height = 100) // Simulate back card
                cardToDisplayForTest = image
            }

            // Your actual RondaPlay or a simplified version focusing on the Canvas
            // For simplicity, we'll just put the Canvas here.
            // In a real scenario, you'd call RondaPlay() and it would use its internal state.
            // The challenge is observing that internal 'cardToDisplay' from the test.
            // That's why the above 'cardToDisplayForTest' and 'displayedBitmapHash' are used.
            Canvas(
                modifier = Modifier
                    .fillMaxSize() // Ensure fillMaxSize is present if RondaPlay uses it
                    .testTag("RondaCardCanvas")
            ) {
                // This drawing logic should match your actual RondaPlay
                cardToDisplayForTest?.let { imageToDraw ->
                    drawImage(
                        image = imageToDraw,
                        dstSize = IntSize(imageToDraw.width, imageToDraw.height),
                        dstOffset = IntOffset(
                            (size.width.toInt() / 2) - (imageToDraw.width / 2),
                            (size.height.toInt() / 2) - (imageToDraw.height / 2)
                        )
                    )
                }
            }
        }

        // 1. Assert the Canvas composable exists and is displayed
        composeTestRule.onNodeWithTag("RondaCardCanvas").assertIsDisplayed()

        // 2. Wait until the cardToDisplay state is expected to be populated
        //    (i.e., our test's displayedBitmapHash is non-null)
        composeTestRule.waitUntil(timeoutMillis = 5000) {
            displayedBitmapHash != null
        }

        // 3. Assert that the state variable was indeed set (implying drawing setup)
        assert(displayedBitmapHash != null) {
            "cardToDisplay state was not set after initial load, so a card is likely not being drawn."
        }

        // If you had more specific properties (e.g., if you could set a
        // content description or a semantic property on the Canvas based on the card ID),
        // you could assert that here.
    }

    @Test
    fun tappingCanvas_updatesCardState_implyingNewCardIsDrawn() {
        var initialBitmapHash: Int? = null
        var tappedBitmapHash: Int? = null

        composeTestRule.setContent {
            var cardToDisplayForTest by remember { mutableStateOf<ImageBitmap?>(null) }
            val scope = rememberCoroutineScope()

            // Update test-observable variables
            if (initialBitmapHash == null && cardToDisplayForTest != null) {
                initialBitmapHash = cardToDisplayForTest?.hashCode()
            } else if (initialBitmapHash != null && cardToDisplayForTest?.hashCode() != initialBitmapHash) {
                tappedBitmapHash = cardToDisplayForTest?.hashCode()
            }


            LaunchedEffect(Unit) {
                val image = loadTestImageBitmap(width = 70, height = 100, delayMs = 20) // Initial card
                cardToDisplayForTest = image
            }

            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .testTag("RondaCardCanvas")
                    .pointerInput(Unit) {
                        detectTapGestures {
                            scope.launch {
                                val newImage = loadTestImageBitmap(width = 50, height = 70, delayMs = 20) // New card
                                cardToDisplayForTest = newImage
                            }
                        }
                    }
            ) {
                cardToDisplayForTest?.let { imageToDraw ->
                    drawImage(imageToDraw) // Simplified draw for example
                }
            }
        }

        val canvasNode = composeTestRule.onNodeWithTag("RondaCardCanvas")
        canvasNode.assertIsDisplayed()

        // Wait for the initial card state to be set
        composeTestRule.waitUntil(timeoutMillis = 3000) {
            initialBitmapHash != null
        }
        val firstHash = initialBitmapHash // Capture the hash of the first "drawn" card state

        // Perform a tap
        canvasNode.performClick()

        // Wait for the card state to change after the tap
        composeTestRule.waitUntil(timeoutMillis = 3000) {
            tappedBitmapHash != null && tappedBitmapHash != firstHash
        }

        // Assert that the state changed
        assert(tappedBitmapHash != null && tappedBitmapHash != firstHash) {
            "Card state (bitmap hash) did not change after tap, " +
                    "implying a new card might not be drawn. " +
                    "Initial: $firstHash, Tapped: $tappedBitmapHash"
        }
    }
}
