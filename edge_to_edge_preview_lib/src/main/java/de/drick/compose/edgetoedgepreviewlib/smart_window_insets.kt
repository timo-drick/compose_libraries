package de.drick.compose.edgetoedgepreviewlib

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.UiComposable
import androidx.compose.ui.composed
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.onPlaced
import androidx.compose.ui.layout.positionInWindow
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import kotlin.math.max


data class PaddingPx(
    val left: Int,
    val top: Int,
    val right: Int,
    val bottom: Int
)

@Composable
fun SmartInsetsProvider(
    modifier: Modifier = Modifier,
    insets: WindowInsets,
    content: @Composable @UiComposable (insetsPadding: PaddingValues) -> Unit
) {
    val density = LocalDensity.current
    val layoutDirection = LocalLayoutDirection.current

    // Mutable PaddingValues needed because of animations.
    val insetPaddingValues = remember { MutableFixedIntPaddingValues() }
    val windowSize = getWindowSize()
    val insetPadding = remember(density, layoutDirection) {
        val left = insets.getLeft(density, layoutDirection)
        val top = insets.getTop(density)
        val bottom = insets.getBottom(density)
        val right = insets.getRight(density, layoutDirection)
        PaddingPx(
            left = left,
            top = top,
            right = right,
            bottom = bottom
        )
    }

    Layout(
        modifier = modifier,
        content = { content(insetPaddingValues) }
    ) { measurable, constraints ->
        if (measurable.size > 1) throw IllegalArgumentException("Only one child composable allowed!")
        val placeable = measurable.first().measure(constraints)

        layout(placeable.width, placeable.height) {
            coordinates?.positionInWindow()?.let { posInWindow ->
                val topPaddingPx = max(0, insetPadding.top - posInWindow.y.toInt())
                val bottomDistance = windowSize.height - posInWindow.y.toInt() - placeable.height
                val bottomPaddingPx = max(0, insetPadding.bottom - bottomDistance)
                val leftPaddingPx = max(0, insetPadding.left - posInWindow.x.toInt())
                val rightDistance = windowSize.width - posInWindow.x.toInt() - placeable.width
                val rightPaddingPx = max(0, insetPadding.right - rightDistance)
                with(density) {
                    insetPaddingValues.top = topPaddingPx.toDp()
                    insetPaddingValues.bottom = bottomPaddingPx.toDp()
                    insetPaddingValues.left = leftPaddingPx.toDp()
                    insetPaddingValues.right = rightPaddingPx.toDp()
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
    val consumedWindowInsets = remember { MutableFixedIntPaddingValues() }
    val windowSize = getWindowSize()

    Layout(
        modifier = modifier.consumeWindowInsets(consumedWindowInsets),
        content = content
    ) { measurable, constraints ->
        if (measurable.size > 1) throw IllegalArgumentException("Only one child composable allowed!")
        val placeable = measurable.firstOrNull()?.measure(constraints)

        val width = placeable?.width ?: 0
        val height = placeable?.height ?: 0
        layout(width, height) {
            coordinates?.positionInWindow()?.let { posInWindow ->
                val leftConsumed = posInWindow.x.toInt()
                val topConsumed = posInWindow.y.toInt()
                val rightConsumed = windowSize.width - posInWindow.x.toInt() - width
                val bottomConsumed = windowSize.height - posInWindow.y.toInt() - height
                consumedWindowInsets.top = max(0, topConsumed).toDp()
                consumedWindowInsets.bottom = max(0, bottomConsumed).toDp()
                consumedWindowInsets.left = max(0, leftConsumed).toDp()
                consumedWindowInsets.right = max(0, rightConsumed).toDp()
            }

            placeable?.place(IntOffset.Zero)
        }
    }
}

@Composable
fun getWindowSize(): IntSize {
    val view = LocalView.current
    val windowWidth = view.rootView.width
    val windowHeight = view.rootView.height
    return remember(windowWidth, windowHeight) {
        IntSize(windowWidth, windowHeight)
    }
}

/**
 * This modifier works but not for animations. Not sure why
 */
fun Modifier.consumeNonOverlappingInsets() = composed {
    val density = LocalDensity.current
    val consumedWindowInsets = remember { MutableFixedIntPaddingValues() }
    val insets = remember { MutableFixedIntWindowInsets() }
    //var mutableInsets = remember { MutableWindowInsets() }
    val windowSize = getWindowSize()
    this.onPlaced { coordinates ->
        val width = coordinates.size.width
        val height = coordinates.size.height

        val posInWindow = coordinates.positionInWindow()
        val leftConsumed = posInWindow.x.toInt()
        val topConsumed = posInWindow.y.toInt()
        val rightConsumed = windowSize.width - posInWindow.x.toInt() - width
        val bottomConsumed = windowSize.height - posInWindow.y.toInt() - height
        insets.left = max(0, leftConsumed)
        insets.top = max(0, topConsumed)
        insets.right = max(0, rightConsumed)
        insets.bottom = max(0, bottomConsumed)
        with (density) {
            consumedWindowInsets.left = max(0, leftConsumed).toDp()
            consumedWindowInsets.top = max(0, topConsumed).toDp()
            consumedWindowInsets.right = max(0, rightConsumed).toDp()
            consumedWindowInsets.bottom = max(0, bottomConsumed).toDp()

        }
    }.consumeWindowInsets(consumedWindowInsets)
}


/**
 * This modifier works but not for animations. Not sure why
 */
fun Modifier.windowInsetsPaddingNonOverlapping() = composed {
    //val density = LocalDensity.current
    //var consumedWindowInsets by remember { mutableStateOf(PaddingValues()) }
    var insets by remember { mutableStateOf(WindowInsets(left = 0)) }
    val windowSize = getWindowSize()
    this
        .onPlaced { coordinates ->
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

class MutableFixedIntPaddingValues: PaddingValues {
    var left by mutableStateOf(0.dp)
    var top by mutableStateOf(0.dp)
    var right by mutableStateOf(0.dp)
    var bottom by mutableStateOf(0.dp)

    override fun calculateBottomPadding() = bottom
    override fun calculateLeftPadding(layoutDirection: LayoutDirection) = left
    override fun calculateRightPadding(layoutDirection: LayoutDirection) = right
    override fun calculateTopPadding() = top

    override fun toString() = "Padding left: $left top: $top right: $right bottom: $bottom"
}

class MutableFixedIntWindowInsets(): WindowInsets {
    var left by mutableIntStateOf(0)
    var top by mutableIntStateOf(0)
    var right by mutableIntStateOf(0)
    var bottom by mutableIntStateOf(0)

    override fun getBottom(density: Density) = bottom
    override fun getLeft(density: Density, layoutDirection: LayoutDirection) = left
    override fun getRight(density: Density, layoutDirection: LayoutDirection) = right
    override fun getTop(density: Density) = top
    override fun toString() = "WindowInsets(left: $left top: $top right: $right bottom: $bottom"
}