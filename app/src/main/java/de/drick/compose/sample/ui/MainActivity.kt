package de.drick.compose.sample.ui

import android.app.Application
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
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
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
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
import de.drick.compose.sample.ui.audio.MainAudioScreen
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.light(
                android.graphics.Color.TRANSPARENT,
                android.graphics.Color.TRANSPARENT
            )
        )
        LogConfig.isActivated = true //BuildConfig.DEBUG
        setContent {
            SampleTheme(darkTheme = false) {
                MainScreen()
            }
        }
    }
}

enum class Screens {
    SimpleShader,
    ChartShader,
    AnimationShader,
    BugDroidShader,
    //FlameShader,
    CurtainTransitionSample,
    AudioSpectrum,
    LiveEdit
}

enum class LoadingShader(val src: String, val loopDuration: Int = 2000) {
    WHEEL(SHADER_WHEEL),
    KITT(SHADER_KITT, 1400),
    SPHERE_3D(SHADER_SPINNER_SPHERE_3D, 3000)
}

class MainVM(val ctx: Application) : AndroidViewModel(ctx) {
    var currentScreen: Screens? by mutableStateOf(null)
        private set
    fun setScreen(screen: Screens?) {
        currentScreen = screen
    }
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun MainScreen(vm: MainVM = viewModel()) {
    val scope = rememberCoroutineScope()

    val snackbarHostState = remember { SnackbarHostState() }

    var loadingShader: LoadingShader? by remember { mutableStateOf(null) }
    val isLoading by remember { derivedStateOf { loadingShader != null }}

    val backNavigationEnabled by remember {
        derivedStateOf { vm.currentScreen != null }
    }
    val topBarName by remember {
        derivedStateOf { vm.currentScreen?.name ?: "Compose shader" }
    }
    BackHandler(
        enabled = backNavigationEnabled,
        onBack = {
            vm.setScreen(null)
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
            /*TopAppBar(
                title = {
                    Text(topBarName)
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
                navigationIcon = {
                    if (backNavigationEnabled) {
                        IconButton(onClick = { vm.setScreen(null) }) {
                            Icon(
                                imageVector = Icons.Default.ArrowBack,
                                contentDescription = "navigate back"
                            )
                        }
                    }
                }
            )*/
        }
    ) { padding ->
        when (vm.currentScreen) {
            Screens.SimpleShader -> PixelShaderSamples()
            Screens.ChartShader -> PieChart(modifier = Modifier
                .padding(padding)
                .fillMaxSize())
            Screens.AnimationShader -> ShaderAnimation(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize(),
                shaderSrc = rememberAssetString("shader/spinner_sphere_3d.agsl")
            )
            Screens.BugDroidShader -> BugDroidView(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize()
            )
            //Screens.FlameShader -> FlameScreen()
            Screens.CurtainTransitionSample -> TransitionScreen(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize()
            )
            Screens.AudioSpectrum -> MainAudioScreen(
                Modifier
                    .padding(padding)
                    .fillMaxSize())
            Screens.LiveEdit -> LiveEditShaderScreen(
                modifier = Modifier.fillMaxSize(),
                padding = padding
            )
            //Screens.AttitudeSample -> AttitudeArrow(modifier = Modifier.fillMaxSize())
            null -> {
                LazyColumn(
                    modifier = Modifier.padding(horizontal = 8.dp),
                    contentPadding = padding,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(Screens.entries) { screen ->
                        if (screen == Screens.CurtainTransitionSample) {
                            ShinyButton(onClick = { vm.setScreen(screen) }) {
                                Text(text = screen.name)
                            }
                        } else {
                            Button(onClick = { vm.setScreen(screen) }) {
                                Text(text = screen.name)
                            }
                        }
                    }
                    items(LoadingShader.entries) { shader ->
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
