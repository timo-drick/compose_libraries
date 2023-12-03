package de.drick.compose.sample.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import de.drick.common.LogConfig
import de.drick.compose.opengl.PixelShaderSamples
import de.drick.compose.progress_indication.ProgressOverlay
import de.drick.compose.sample.theme.SampleTheme
import de.drick.compose.sample.theme.shaderBackground
import de.drick.compose.sample.theme.shaderPrimary
import de.drick.compose.sample.ui.animation.SHADER_KITT
import de.drick.compose.sample.ui.animation.SHADER_SPINNER_SPHERE_3D
import de.drick.compose.sample.ui.animation.SHADER_WHEEL
import de.drick.compose.sample.ui.animation.ShaderAnimation
import de.drick.compose.sample.ui.animation.ShinyButton
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        LogConfig.isActivated = true //BuildConfig.DEBUG
        setContent {
            SampleTheme {
                MainScreen()
            }
        }
    }
}

enum class Screens {
    SimpleShader,
    ChartShader,
    AnimationShader,
    FlameShader,
    CurtainTransitionSample
}

enum class LoadingShader(val src: String, val loopDuration: Int = 2000) {
    WHEEL(SHADER_WHEEL),
    KITT(SHADER_KITT, 1400),
    SPHERE_3D(SHADER_SPINNER_SPHERE_3D, 3000)
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun MainScreen() {
    val scope = rememberCoroutineScope()

    val snackbarHostState = remember { SnackbarHostState() }

    var loadingShader: LoadingShader? by remember { mutableStateOf(null) }
    val isLoading by remember { derivedStateOf { loadingShader != null }}

    var currentScreen: Screens? by remember { mutableStateOf(null) }

    val backNavigationEnabled by remember {
        derivedStateOf { currentScreen != null }
    }
    val topBarName by remember {
        derivedStateOf { currentScreen?.name ?: "Compose shader" }
    }
    BackHandler(
        enabled = backNavigationEnabled,
        onBack = {
            currentScreen = null
        }
    )
    ProgressOverlay(isVisible = isLoading) {
        loadingShader?.let { shader ->
            ShaderAnimation(
                modifier = Modifier.fillMaxSize(),
                shaderSrc = shader.src,
                durationMillis = shader.loopDuration
            )
        }
    }
    BackgroundShader(
        modifier = Modifier.fillMaxSize(),
        shaderSrc = remoteAssetAsState("shader/aoa_background.agsl"),
        background = shaderBackground,
        primary = shaderPrimary
    )
    // A surface container using the 'background' color from the theme
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = Color.Transparent,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Text(topBarName)
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
                navigationIcon = {
                    if (backNavigationEnabled) {
                        IconButton(onClick = { currentScreen = null }) {
                            Icon(
                                imageVector = Icons.Default.ArrowBack,
                                contentDescription = "navigate back"
                            )
                        }
                    }
                }
            )
        }
    ) { padding ->
        when (currentScreen) {
            Screens.SimpleShader -> PixelShaderSamples()
            Screens.ChartShader -> PieChart(modifier = Modifier
                .padding(padding)
                .fillMaxSize())
            Screens.AnimationShader -> ShaderAnimation(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize(),
                shaderSrc = remoteAssetAsState("shader/spinner_sphere_3d.agsl")
            )
            Screens.FlameShader -> FlameScreen()
            Screens.CurtainTransitionSample -> TransitionScreen(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize()
            )
            //Screens.AttitudeSample -> AttitudeArrow(modifier = Modifier.fillMaxSize())
            null -> {
                LazyColumn(
                    modifier = Modifier.padding(horizontal = 8.dp),
                    contentPadding = padding,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(Screens.values()) { screen ->
                        if (screen == Screens.CurtainTransitionSample) {
                            ShinyButton(onClick = { currentScreen = screen }) {
                                Text(text = screen.name)
                            }
                        } else {
                            Button(onClick = { currentScreen = screen }) {
                                Text(text = screen.name)
                            }
                        }
                    }
                    items(LoadingShader.values()) { shader ->
                        Button(onClick = {
                            scope.launch {
                                loadingShader = shader
                                delay(5000)
                                loadingShader = null
                            }
                        }) {
                            Text(text = "Loading simulation ${shader.name}")
                        }
                    }
                }
            }
        }
    }
}
