package de.drick.compose.sample.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import de.drick.common.LogConfig
import de.drick.compose.opengl.PixelShaderSamples
import de.drick.compose.progress_indication.ProgressOverlay
import de.drick.compose.sample.BuildConfig
import de.wurst.formularwizardchallenge.ui.theme.FormularTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.intellij.lang.annotations.Language

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        LogConfig.isActivated = BuildConfig.DEBUG
        setContent {
            FormularTheme {
                MainScreen()
            }
        }
    }
}


@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun MainScreen() {
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    var isLoading by remember { mutableStateOf(false) }

    // A surface container using the 'background' color from the theme
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        PixelShaderSamples()
        ProgressOverlay(isVisible = isLoading, progressIndication = { CircularProgressIndicator() })
        Column(
            modifier = Modifier
                .padding(padding)
        ) {
            Button(
                onClick = {
                    isLoading = true
                    scope.launch {
                        delay(2000)
                        isLoading = false
                    }
                }
            ) {
                Text("Load")
            }
        }
    }
}
