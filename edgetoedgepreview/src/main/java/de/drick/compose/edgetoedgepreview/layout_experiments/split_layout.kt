package de.drick.compose.edgetoedgepreview.layout_experiments

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.exclude
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.onConsumedWindowInsetsChanged
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.layoutId
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import de.drick.compose.edgetoedgepreview.TestComponentWindowInsets
import de.drick.compose.edgetoedgepreviewlib.CameraCutoutMode
import de.drick.compose.edgetoedgepreviewlib.EdgeToEdgeTemplate
import de.drick.compose.edgetoedgepreviewlib.NavigationMode
import kotlin.math.min

@Preview(device = "spec:parent=pixel_5,orientation=landscape")
@Composable
fun PreviewSplitLayoutLandscape() {
    EdgeToEdgeTemplate(
        navMode = NavigationMode.ThreeButton,
        cameraCutoutMode = CameraCutoutMode.Middle,
        showInsetsBorder = true,
    ) {
        SplitLayoutSample()
        SplitLayoutRowSample()
    }
}

@Composable
fun SplitLayoutSample() {
    SplitLayoutVerticalSimple(
        modifier = Modifier
            .background(Color.LightGray)
            .fillMaxSize(),
        first = {
            TestComponentWindowInsets(
                modifier = Modifier.fillMaxSize(),
                title = "Item list"
            )
        },
        second = {
            TestComponentWindowInsets(
                modifier = Modifier.fillMaxSize(),
                title = "Detail view")
        }
    )
}

@Composable
fun SplitLayoutRowSample() {
    val insets = WindowInsets.safeDrawing
    SplitLayoutVerticalNaive(
        modifier = Modifier
            .background(Color.LightGray)
            .fillMaxSize(),
        windowInsets = insets,
        first = { modifier ->
            TestComponentWindowInsets(
                modifier = modifier
                    .width(120.dp)
                    .fillMaxHeight(),
                title = "App Navigation Bar",
                rotatedText = true
            )
        },
        second = { modifier ->
            val density = LocalDensity.current
            var insetsPadding by remember {
                mutableStateOf(PaddingValues())
            }
            //val insetsPadding = WindowInsets.safeDrawing.asPaddingValues()
            TestComponentWindowInsets(
                modifier = modifier
                    .onConsumedWindowInsetsChanged {
                        insetsPadding = insets
                            .exclude(it)
                            .asPaddingValues(density)
                    }
                    .weight(1f)
                    .fillMaxHeight(),
                title = "Item list",
                contentPadding = insetsPadding
            )
        }
    )
}

@Composable
fun SplitLayoutVerticalNaive(
    modifier: Modifier = Modifier,
    windowInsets: WindowInsets = WindowInsets.safeDrawing,
    first: @Composable RowScope.(Modifier) -> Unit,
    second: @Composable RowScope.(Modifier) -> Unit
) {
    Row(modifier) {
        val consumedFirst = windowInsets.only(WindowInsetsSides.End)
        first(Modifier.consumeWindowInsets(consumedFirst))
        val consumedSecond = windowInsets.only(WindowInsetsSides.Start)
        second(Modifier.consumeWindowInsets(consumedSecond))
    }
}


@Composable
fun SplitLayoutVerticalSimple(
    modifier: Modifier = Modifier,
    first: @Composable () -> Unit,
    second: @Composable () -> Unit
) {
    Layout(
        modifier = modifier.clipToBounds(),
        content = {
            Box(
                Modifier
                    .layoutId("first")
                    .consumeWindowInsets(
                        WindowInsets.safeDrawing.only(WindowInsetsSides.End)
                    )
            ) {
                first()
            }
            Box(
                Modifier
                    .layoutId("second")
                    .consumeWindowInsets(
                        WindowInsets.safeDrawing.only(WindowInsetsSides.Start)
                    )
            ) {
                second()
            }
        }
    ) { measurable, constraints ->
        val firstMeasurable = measurable.first { it.layoutId == "first" }
        val secondMeasurable = measurable.first { it.layoutId == "second" }
        val width = constraints.maxWidth
        val height = constraints.maxHeight
        val midPoint = width - 100
        val firstWidth = midPoint
        val secondWidth = width - midPoint
        val firstContraints = constraints.copy(
            maxWidth = firstWidth,
            minWidth = min(firstWidth, constraints.minWidth)
        )
        val firstPlaceable = firstMeasurable.measure(firstContraints)

        val secondConstraints = constraints.copy(
            maxWidth = secondWidth,
            minWidth = min(secondWidth, constraints.minWidth)
        )
        val secondPlaceable = secondMeasurable.measure(secondConstraints)
        layout(width, height) {
            firstPlaceable.placeRelative(0, 0)
            secondPlaceable.placeRelative(firstWidth, 0)
        }
    }
}

@Composable
fun SplitLayoutVertical(
    modifier: Modifier = Modifier,
    first: @Composable () -> Unit,
    second: @Composable () -> Unit
) {
    val density = LocalDensity.current
    val consumedInsetsFirst = remember { MutablePaddingValues() }
    val consumedInsetsSecond = remember { MutablePaddingValues() }
    Layout(
        modifier = modifier.clipToBounds(),
        content = {
            Box(
                Modifier
                    .layoutId("first")
                    .consumeWindowInsets(consumedInsetsFirst)
            ) {
                first()
            }
            Box(
                Modifier
                    .layoutId("second")
                    .consumeWindowInsets(consumedInsetsSecond)
            ) {
                second()
            }
        }
    ) { measurable, constraints ->
        val firstMeasurable = measurable.first { it.layoutId == "first" }
        val secondMeasurable = measurable.first { it.layoutId == "second" }
        val width = constraints.maxWidth
        val height = constraints.maxHeight
        val midPoint = width / 2
        val firstWidth = midPoint
        val secondWidth = width - midPoint
        with(density) {
            consumedInsetsFirst.end = secondWidth.toDp()
            consumedInsetsSecond.start = firstWidth.toDp()
        }
        val firstContraints = constraints.copy(
            maxWidth = firstWidth,
            minWidth = min(firstWidth, constraints.minWidth)
        )
        val firstPlaceable = firstMeasurable.measure(firstContraints)

        val secondConstraints = constraints.copy(
            maxWidth = secondWidth,
            minWidth = min(secondWidth, constraints.minWidth)
        )
        val secondPlaceable = secondMeasurable.measure(secondConstraints)
        layout(width, height) {
            firstPlaceable.place(0, 0)
            secondPlaceable.place(firstWidth, 0)
        }
    }
}

class MutablePaddingValues: PaddingValues {
    var start by mutableStateOf(0.dp)
    var top by mutableStateOf(0.dp)
    var end by mutableStateOf(0.dp)
    var bottom by mutableStateOf(0.dp)

    override fun calculateBottomPadding() = bottom
    override fun calculateLeftPadding(layoutDirection: LayoutDirection) =
        if (layoutDirection == LayoutDirection.Ltr) start else end
    override fun calculateRightPadding(layoutDirection: LayoutDirection) =
        if (layoutDirection == LayoutDirection.Ltr) end else start
    override fun calculateTopPadding() = top

    override fun toString() = "Padding start: $start top: $top end: $end bottom: $bottom"
}