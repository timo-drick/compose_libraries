package de.drick.compose.progress_indication

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
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
    progressIndication: @Composable () -> Unit
) {
    val visible by rememberUpdatedState(isVisible)
    var loadingOverlay by remember { mutableStateOf(false) }
    LaunchedEffect(visible) {
        if (visible) { // delay for 300 ms before show the loading overlay
            delay(300)
        }
        loadingOverlay = visible
    }
    val alpha: Float by animateFloatAsState(
        targetValue = if (loadingOverlay) 1.0f else 0.0f,
        label = "alpha transparency",
        animationSpec = tween(1000)
    )
    if (visible) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .zIndex(100f)
                .alpha(alpha)
                .background(Color.Black.copy(alpha = alpha * 0.7f))
                .then( // catches all pointer events to prevent clicking
                    Modifier.pointerInput(true) {
                        awaitPointerEventScope {
                            val event = awaitPointerEvent() //Captcher any event
                            //Do nothing
                        }
                    }
                ),
            contentAlignment = Alignment.Center
        ) {
            if (loadingOverlay) {
                progressIndication()
            }
        }
    }
}
