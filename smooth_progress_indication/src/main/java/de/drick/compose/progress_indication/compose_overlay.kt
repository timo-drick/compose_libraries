package de.drick.compose.progress_indication

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.zIndex
import kotlinx.coroutines.delay

/**
 * Overlay to prevent clicking area behind this overlay.
 * Also when the state is changed to Loading it will darken the background after 300 ms and
 * shows a circular loading spinner
 * When the state is Ready it will show the content with darkened background
 */
@Composable
fun ProgressOverlay(
    isVisible: Boolean,
    progressIndication: @Composable AnimatedVisibilityScope.() -> Unit
) {
    var visible by remember { mutableStateOf(isVisible) }
    var loadingOverlay by remember { mutableStateOf(false) }
    LaunchedEffect(isVisible) {
        visible = isVisible
        loadingOverlay = if (isVisible) {
            delay(300)
            true
        } else {
            false
        }
    }
    val alpha: Float by animateFloatAsState(targetValue = if (loadingOverlay) 0.7f else 0.0f)
    if (visible) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .zIndex(100f)
                .background(Color.Black.copy(alpha))
                .then( // catches all pointer events to prevent clicking
                    Modifier.pointerInput(visible) {
                        awaitPointerEventScope {
                            val event = awaitPointerEvent() //Captcher any event
                            //Do nothing
                        }
                    }
                ),
            contentAlignment = Alignment.Center
        ) {
            AnimatedVisibility(
                visible = loadingOverlay,
                enter = fadeIn(),
                exit = fadeOut(),
                content = progressIndication
            )
        }
    }
}
