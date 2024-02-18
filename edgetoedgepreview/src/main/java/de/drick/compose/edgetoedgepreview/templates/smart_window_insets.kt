package de.drick.compose.edgetoedgepreview.templates

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.safeContent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.UiComposable
import androidx.compose.ui.composed
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.onPlaced
import androidx.compose.ui.layout.positionInWindow
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import kotlin.math.max


@Composable
fun SmartInsetsProvider(
    modifier: Modifier = Modifier,
    insets: WindowInsets,
    content: @Composable @UiComposable (insetsPadding: PaddingValues) -> Unit
) {
    val density = LocalDensity.current
    val layoutDirection = LocalLayoutDirection.current

    var insetPaddingValues by remember { mutableStateOf(PaddingValues()) }

    val screenWidth = LocalConfiguration.current.screenWidthDp.dp
    val screenWidthPx = with(density) { screenWidth.toPx().toInt() }
    val screenHeight = LocalConfiguration.current.screenHeightDp.dp
    val screenHeightPx = with(density) { screenHeight.toPx().toInt() }

    Layout(
        modifier = modifier,
        content = { content(insetPaddingValues) }
    ) { measurable, constraints ->
        if (measurable.size > 1) throw IllegalArgumentException("Only one child composable allowed!")
        val placeable = measurable.first().measure(constraints)
        val left = insets.getLeft(density, layoutDirection)
        val top = insets.getTop(density)
        val bottom = insets.getBottom(density)
        val right = insets.getRight(density, layoutDirection)

        layout(placeable.width, placeable.height) {
            coordinates?.positionInWindow()?.let { posInWindow ->
                val topPaddingPx = max(0, top - posInWindow.y.toInt())
                val bottomDistance = screenHeightPx + top + bottom - posInWindow.y.toInt() - placeable.height
                val bottomPaddingPx = max(0, bottom - bottomDistance)
                val leftPaddingPx = max(0, left - posInWindow.x.toInt())
                val rightDistance = screenWidthPx + left + right - posInWindow.x.toInt() - placeable.width
                val rightPaddingPx = max(0, right - rightDistance)
                with(density) {
                    insetPaddingValues = PaddingValues.Absolute(
                        top = topPaddingPx.toDp(),
                        bottom = bottomPaddingPx.toDp(),
                        left = leftPaddingPx.toDp(),
                        right = rightPaddingPx.toDp()
                    )
                }
            }

            placeable.place(IntOffset.Zero)
        }
    }
}

/**
 * This component will consume the window insets that are not overlapping with this component
 */
@Composable
fun SmartInsetsConsumer(
    modifier: Modifier = Modifier,
    content: @Composable @UiComposable () -> Unit
) {
    val density = LocalDensity.current
    val layoutDirection = LocalLayoutDirection.current

    var consumedWindowInsets by remember { mutableStateOf(WindowInsets(left = 0)) }

    val screenWidth = LocalConfiguration.current.screenWidthDp.dp
    val screenWidthPx = with(density) { screenWidth.toPx().toInt() }
    val screenHeight = LocalConfiguration.current.screenHeightDp.dp
    val screenHeightPx = with(density) { screenHeight.toPx().toInt() }

    val insets = WindowInsets.safeContent

    Layout(
        modifier = modifier.consumeWindowInsets(consumedWindowInsets),
        content = { content() }
    ) { measurable, constraints ->
        if (measurable.size > 1) throw IllegalArgumentException("Only one child composable allowed!")
        val placeable = measurable.first().measure(constraints)

        val left = insets.getLeft(density, layoutDirection)
        val top = insets.getTop(density)
        val bottom = insets.getBottom(density)
        val right = insets.getRight(density, layoutDirection)
        val windowHeight = screenHeightPx + top + bottom // not sure if this is really correct in all cases
        val windowWidth = screenWidthPx + left + right   // not sure if this is really correct in all cases
        val width = placeable.width
        val height = placeable.height
        layout(width, height) {
            coordinates?.positionInWindow()?.let { posInWindow ->
                val leftConsumed = posInWindow.x.toInt()
                val topConsumed = posInWindow.y.toInt()
                val rightConsumed = windowWidth - posInWindow.x.toInt() - width
                val bottomConsumed = windowHeight - posInWindow.y.toInt() - height
                consumedWindowInsets = WindowInsets(
                    top = max(0, topConsumed),
                    bottom = max(0, bottomConsumed),
                    left = max(0, leftConsumed),
                    right = max(0, rightConsumed)
                )
            }

            placeable.place(IntOffset.Zero)
        }
    }
}

@Composable
fun getWindowSize(): IntSize {
    val density = LocalDensity.current
    val layoutDirection = LocalLayoutDirection.current
    val insets = WindowInsets.safeContent
    val left = insets.getLeft(density, layoutDirection)
    val top = insets.getTop(density)
    val bottom = insets.getBottom(density)
    val right = insets.getRight(density, layoutDirection)
    val screenWidth = LocalConfiguration.current.screenWidthDp.dp
    val screenWidthPx = with(density) { screenWidth.toPx().toInt() }
    val screenHeight = LocalConfiguration.current.screenHeightDp.dp
    val screenHeightPx = with(density) { screenHeight.toPx().toInt() }
    val windowHeight = screenHeightPx + top + bottom // not sure if this is really correct in all cases
    val windowWidth = screenWidthPx + left + right   // not sure if this is really correct in all cases
    return IntSize(windowWidth, windowHeight)
}

fun Modifier.consumeNonOverlappingInsets() = composed {
    //val density = LocalDensity.current
    //var consumedWindowInsets by remember { mutableStateOf(PaddingValues()) }
    var insets by remember { mutableStateOf(WindowInsets(left = 0)) }
    val windowSize = getWindowSize()
    this.onPlaced { coordinates ->
        val width = coordinates.size.width
        val height = coordinates.size.height

        val posInWindow = coordinates.positionInWindow()
        val leftConsumed = posInWindow.x.toInt()
        val topConsumed = posInWindow.y.toInt()
        val rightConsumed = windowSize.width - posInWindow.x.toInt() - width
        val bottomConsumed = windowSize.height - posInWindow.y.toInt() - height

        insets = WindowInsets(
            top = max(0, topConsumed),
            bottom = max(0, bottomConsumed),
            left = max(0, leftConsumed),
            right = max(0, rightConsumed)
        )
    }
    .consumeWindowInsets(insets)
}