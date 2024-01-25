package de.drick.compose.edgetoedgepreview

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.captionBar
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContent
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.safeGestures
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.systemGestures
import androidx.compose.foundation.layout.tappableElement
import androidx.compose.foundation.layout.waterfall
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import de.drick.compose.edgetoedgepreview.templates.EdgeToEdgeTemplate
import de.drick.compose.edgetoedgepreview.ui.theme.ComposeLibrariesTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ComposeLibrariesTheme {
                // A surface container using the 'background' color from the theme
                InsetsTest()
            }
        }
    }
}

@Composable
fun TestComponent(
    modifier: Modifier,
    title: String,
    style: TextStyle = MaterialTheme.typography.headlineLarge,
    childContent: @Composable (BoxScope.() -> Unit)? = null
) {
    Box(
        modifier = modifier
            .border(1.dp, Color.Black)
            .padding(2.dp),
    ) {
        if (childContent == null) {
            Text(title, modifier = Modifier.align(Alignment.Center), style = style)
        } else {
            Text(title, modifier = Modifier.align(Alignment.Center), style = style)
            childContent()
        }
    }
}

@Composable
fun InsetsTest() {
    val focusRequester = remember { FocusRequester() }
    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }
    Box(
        Modifier
            .fillMaxSize()
            .background(Color.Red)) {
        Box(
            Modifier
                .windowInsetsPadding(WindowInsets.systemBars)
                .fillMaxSize()
        ) {
            Spacer(
                Modifier
                    .fillMaxSize()
                    .background(Color.Green)
            )
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row {
                    Spacer(Modifier.weight(1f))
                    var text by remember { mutableStateOf("") }
                    TextField(
                        modifier = Modifier
                            .focusRequester(focusRequester),
                        value = text,
                        onValueChange = { text = it }
                    )
                }
                val screenWidthDp = LocalConfiguration.current.screenWidthDp
                val screenHeightDp = LocalConfiguration.current.screenHeightDp
                val pixelDensity = LocalDensity.current.density
                Text("screen size: $screenWidthDp dp x $screenHeightDp dp density: $pixelDensity")
                InsetValues(WindowInsets.statusBars)
                InsetValues(WindowInsets.navigationBars)
                InsetValues(WindowInsets.captionBar)
                InsetValues(WindowInsets.ime)
                InsetValues(WindowInsets.displayCutout)
                InsetValues(WindowInsets.tappableElement)
                InsetValues(WindowInsets.systemGestures)
                InsetValues(WindowInsets.waterfall)
            }
            /*Text(
            text = "Hello $name!",
            modifier = modifier
        )*/
        }
    }
}

@Composable
fun InsetValues(
    insets: WindowInsets
) {
    Text("insets: $insets")
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    EdgeToEdgeTemplate {
        ComposeLibrariesTheme {
            InsetsTest()
        }
    }
}