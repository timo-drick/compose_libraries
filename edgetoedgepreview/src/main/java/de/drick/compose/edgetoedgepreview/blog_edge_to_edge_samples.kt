package de.drick.compose.edgetoedgepreview

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.exclude
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.onConsumedWindowInsetsChanged
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import de.drick.compose.edgetoedgepreview.templates.EdgeToEdgeTemplate
import de.drick.compose.edgetoedgepreview.templates.NavigationMode
import de.drick.compose.edgetoedgepreview.ui.theme.ComposeLibrariesTheme


@Preview(
    widthDp = 411,
    heightDp = 838
)
annotation class GridPreviewScreen

@Composable
fun BaseLayout(
    content: @Composable () -> Unit
) {
    EdgeToEdgeTemplate(
        navMode = NavigationMode.ThreeButton
    ) {
        Box(
            Modifier
                .fillMaxSize()
                .background(Color.White)) {
            ComposeLibrariesTheme(
                darkTheme = false,
                content = content
            )
        }
    }
}

@GridPreviewScreen
@Composable
fun TestLayoutNoPadding() {
    BaseLayout {
        Column {
            ItemList(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            )
            AppNavigationBar()
        }
    }
}

@GridPreviewScreen
@Composable
fun TestLayoutSimplePadding() {
    BaseLayout {
        Column(
            Modifier.windowInsetsPadding(WindowInsets.systemBars)
        ) {
            LazyColumn() {

            }
            ItemList(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            )
            AppNavigationBar()
        }
    }
}

@GridPreviewScreen
@Composable
fun TestLayoutListContentPadding() {
    BaseLayout {
        Column(
            modifier = Modifier
                .background(color = Color.LightGray)
            //.windowInsetsPadding(WindowInsets.systemBars)
        ) {
            val contentPadding = WindowInsets.systemBars.asPaddingValues()
            ItemListContentPadding(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentPadding = contentPadding
            )
            TestComponent(
                modifier = Modifier
                    .background(bgStripedRed)
                    .height(contentPadding.calculateTopPadding())
                    .fillMaxWidth(),
                title = "Navigation Bar top padding",
                style = MaterialTheme.typography.headlineSmall
            )
            AppNavigationBar()
            TestComponent(
                modifier = Modifier
                    .background(bgStripedGreen)
                    .height(contentPadding.calculateBottomPadding())
                    .fillMaxWidth(),
                title = "Navigation Bar bottom padding",
                style = MaterialTheme.typography.headlineSmall
            )
        }
    }
}


@Composable
fun ItemList(
    modifier: Modifier
) {
    TestComponent(
        modifier = modifier.background(bgStripedGrey),
        title = "Item List"
    )
}

private val bgStripedGreen = Brush.linearGradient(
    colors = listOf(Color.Green, Color.White),
    start = Offset(0f, 20f),
    end = Offset(20f, 0f),
    tileMode = TileMode.Repeated
)
private val bgStripedRed = Brush.linearGradient(
    colors = listOf(Color.Red, Color.White),
    start = Offset(0f, 20f),
    end = Offset(20f, 0f),
    tileMode = TileMode.Repeated
)
private val bgStripedGrey = Brush.linearGradient(
    colors = listOf(Color.LightGray, Color.White),
    start = Offset(0f, 20f),
    end = Offset(20f, 0f),
    tileMode = TileMode.Repeated
)

@Composable
fun ItemListContentPadding(
    modifier: Modifier,
    contentPadding: PaddingValues
) {
    Column(modifier) {
        TestComponent(
            modifier = Modifier
                .background(bgStripedGreen)
                .height(contentPadding.calculateTopPadding())
                .fillMaxWidth(),
            title = "Item List top padding",
            style = MaterialTheme.typography.headlineSmall
        )
        TestComponent(
            modifier = Modifier
                .background(bgStripedGrey)
                .weight(1f)
                .fillMaxWidth(),
            title = "Item List"
        )
        TestComponent(
            modifier = Modifier
                .background(bgStripedRed)
                .height(contentPadding.calculateBottomPadding())
                .fillMaxWidth(),
            title = "Item List bottom padding",
            style = MaterialTheme.typography.headlineSmall
        )
    }
}


@Composable
fun AppNavigationBar(
    modifier: Modifier = Modifier
) {
    TestComponent(
        modifier = modifier
            .background(bgStripedGrey)
            .height(120.dp)
            .fillMaxWidth(),
        title = "App Navigation Bar"
    )
}


@Composable
fun insetsPaddingValues(insets: WindowInsets): PaddingValues {
    val density = LocalDensity.current
    var paddingValues by remember { mutableStateOf(insets.asPaddingValues(density)) }
    Spacer(modifier = Modifier.onConsumedWindowInsetsChanged(
        block = {
            paddingValues = insets.exclude(it).asPaddingValues(density)
        }
    ))
    return paddingValues
}