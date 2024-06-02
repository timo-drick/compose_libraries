package de.drick.compose.sample.ui.audio

import android.annotation.TargetApi
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapShader
import android.graphics.Color
import android.graphics.RuntimeShader
import android.graphics.Shader
import android.media.MediaPlayer
import android.media.audiofx.Visualizer
import android.net.Uri
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ShaderBrush
import androidx.compose.ui.platform.LocalContext
import androidx.core.math.MathUtils
import de.drick.common.LogConfig
import de.drick.common.log
import de.drick.compose.permission.ManifestPermission
import de.drick.compose.permission.rememberPermissionState
import de.drick.compose.sample.R
import de.drick.compose.sample.theme.SampleTheme
import de.drick.compose.wrapper.rememberFilePicker
import org.intellij.lang.annotations.Language
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.ln
import kotlin.math.log10
import kotlin.math.sin

@Composable
fun MainAudioScreen(
    modifier: Modifier = Modifier
) {
    Box(modifier) {
        val recordAudioPermission = rememberPermissionState(ManifestPermission.RECORD_AUDIO)
        if (recordAudioPermission.hasPermission) {
            if (Build.VERSION.SDK_INT < 33) {
                Text("Only supported in api level >= 33!")
            } else {
                val filePicker = rememberFilePicker()
                val selectedAudioUri = filePicker.selectedUri
                if (selectedAudioUri != null) {
                    AudioPane(
                        modifier = Modifier.fillMaxSize(),
                        audioUri = selectedAudioUri
                    )
                } else {
                    Button(onClick = { filePicker.launchFilePickerIntent(arrayOf("audio/*")) }) {
                        Text("Select music file")
                    }
                }
            }
        } else {
            Button(onClick = { recordAudioPermission.launchPermissionRequest() }) {
                Text("Allow audio recording")
            }
        }
    }
}

@TargetApi(33)
@Composable
fun AudioPane(
    audioUri: Uri,
    modifier: Modifier = Modifier
) {
    @Language("AGSL")
    val shaderSrc = """
    uniform float2 iResolution;
    uniform float iTime;
    uniform shader music;
    
    half4 main(vec2 fragCoord) {
        // create pixel coordinates
        vec2 uv = fragCoord.xy / iResolution.xy;
        
        
        //vec4 color = music.eval(vec2(fragCoord.x, 0));
        float dist = music.eval(vec2(uv.x * 256.0, 0)).x;
        float intensity = 0.0;
        if (dist > 1.0 - uv.y) {
            intensity = 1.0;
        }
        //float wave = music.eval(vec2(uv.x * 512.0, 1)).x;
        //vec3 waveColor = vec3(0.001, 0.01, 0.04) / abs(wave - uv.y);
        // output final color 
        return vec4(vec3(intensity), 1);
    }
""".trimIndent()

    val ctx = LocalContext.current
    val infiniteTransition = rememberInfiniteTransition("animation")
    val animation = infiniteTransition.animateFloat(
        label = "loop_0_1",
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            tween(1000, easing = LinearEasing),
        )
    )
    var shader by remember { mutableStateOf<RuntimeShader?>(null) }
    DisposableEffect(shaderSrc) {
        if (Build.VERSION.SDK_INT < 33)
            throw UnsupportedOperationException("Needs min api 33!")
        val analyzer = AudioVisualizer(ctx, audioUri)
        val bitmap = analyzer.startMusic()
        val musicShader = BitmapShader(bitmap, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP)
        try {
            shader = RuntimeShader(spectrumShader).apply {
                //setInputShader("music", musicShader)
                setInputBuffer("music", musicShader)
            }
        } catch (err: Throwable) {
            log(err)
        }
        onDispose {
            analyzer.stopMusic()
        }
    }
    Column(modifier) {

        Canvas(modifier = modifier) {
            shader?.let { shader ->
                shader.setFloatUniform("iTime", animation.value)
                shader.setFloatUniform("iResolution", size.width, size.height)
                val brush = ShaderBrush(shader)
                drawRect(brush)
            }
        }
    }
}



@Language("AGSL")
val spectrumShader = """
    //https://www.shadertoy.com/view/Msl3zr
    uniform float2 iResolution;
    uniform float iTime;
    uniform shader music;
    
    const float PI = 3.14159;
    
    // http://github.prideout.net/barrel-distortion/
    vec2 distort(vec2 p, float power)
    {
        float a  = atan(p.y, p.x);
        float r = length(p);
        r = pow(r, power);
        return vec2(r * cos(a), r*sin(a));
    }
    
    float getFFT(float x) {
        return music.eval(vec2((x) * 20.0,0.0)).x;
    }
    float getWave(float x) {
        return music.eval(vec2(x * 512.0,1.0)).x;
    }
    
    half4 main(vec2 fragCoord) {
        // create pixel coordinates
        vec2 uv = fragCoord.xy / iResolution.xy;
        uv.y = 1.0-uv.y;    
        // distort
        float bass = getFFT(0.0);		
        //uv = distort(uv*2.0-1.0, 0.5+bass)*0.5+0.5;
            
        // quantize coordinates
        const float bands = 20.0;
        const float segs = 20.0;
        vec2 p;
        p.x = floor(uv.x*bands)/bands;
        p.y = floor(uv.y*segs)/segs;
        
        // read frequency data from first row of texture
        float fft = getFFT(p.x);	
    
        // led color
        vec3 color = mix(vec3(0.0, 2.0, 0.0), vec3(2.0, 0.0, 0.0), sqrt(uv.y));
        
        // mask for bar graph
        float mask = (p.y < fft) ? 1.0 : 0.0;
        
        // led shape
        vec2 d = fract((uv - p)*vec2(bands, segs)) - 0.5;
        float led = smoothstep(0.5, 0.3, abs(d.x)) *
                    smoothstep(0.5, 0.3, abs(d.y));
        vec3 ledColor = led*color*mask;
    
        // second texture row is the sound wave
        float wave = getWave(uv.x);
        vec3 waveColor = vec3(0.001, 0.01, 0.04) / abs(wave - uv.y);
            
        // output final color 
        return vec4(ledColor + waveColor, 1.0);
    }
""".trimIndent()


class AudioVisualizer(
    private val ctx: Context,
    private val uri: Uri
) {
    private lateinit var mediaPlayer: MediaPlayer
    private var audioSession: Int = 0
    private lateinit var visualizer: Visualizer
    private lateinit var visualizerBitmap: Bitmap

    private val onDataCaptureListener = object : Visualizer.OnDataCaptureListener {
        //var waveformBuffer: IntArray
        override fun onWaveFormDataCapture(
            visualizer: Visualizer,
            waveform: ByteArray,
            samplingRate: Int
        ) {
            val buffer  = IntArray(waveform.size) { (0xff shl 24) or (waveform[it].toInt() shl 16) }
            visualizerBitmap.setPixels(buffer, 0, buffer.size, 0, 1, buffer.size, 1)



        }
        private val dB_min = -44f
        private val dB_max = 12f
        private var oldMagnitudes = FloatArray(0)
        override fun onFftDataCapture(visualizer: Visualizer, fft: ByteArray, samplingRate: Int) {
            // https://developer.android.com/reference/android/media/audiofx/Visualizer#getFft(byte[])
            val n = fft.size
            val magnitudes = IntArray(n / 2 + 1)
            if (oldMagnitudes.size != magnitudes.size) {
                oldMagnitudes = FloatArray(magnitudes.size)
            }
            for (k in 0 until (n/2) - 1) {
                val i = (k + 1) * 2
                // hypot = sqrt(x^2 + y^2) -> adds real and imaginary part of complex number
                //val mag = sqrt(fft[i].toFloat().pow(2) + fft[i + 1].toFloat().pow(2))
                //val mag = hypot(fft[i].toFloat(), fft[i + 1].toFloat()) * 8f
                val mag = hypot(fft[i].toFloat() / 128f,   fft[i + 1].toFloat() / 128f)
                val dB = 20f * log10(mag)
                val p = MathUtils.clamp(255f / (dB_max - dB_min) * (dB - dB_min), 0f, 255f)
                val p_smooth = .9f * p + .1f * oldMagnitudes[k]
                oldMagnitudes[k] = p_smooth
                //val mag = atan2(fft[i + 1].toUByte().toFloat(), fft[i].toUByte().toFloat())
                //log("mag: $k $mag")
                magnitudes[k] = (0xff shl 24) or (p_smooth.toInt() shl 16)
            }

            visualizerBitmap.setPixels(magnitudes, 0, magnitudes.size, 0, 0, magnitudes.size, 1)
        }
    }

    fun startMusic(): Bitmap {
        mediaPlayer = MediaPlayer.create(ctx, uri)
        mediaPlayer.isLooping = true
        audioSession = mediaPlayer.audioSessionId
        mediaPlayer.start()
        visualizer = Visualizer(audioSession)
        val samples = Visualizer.getCaptureSizeRange()[1] / 2
        val listener = object : Visualizer.OnDataCaptureListener {
            val fft = FFT(samples)
            val intWaveBuffer = IntArray(samples)
            val intFFTBuffer = IntArray(samples/2)
            val doubleWaveBuffer = DoubleArray(samples)
            val doubleFFTBuffer = DoubleArray(samples)
            private val dB_min = 0f
            private val dB_max = 60f
            private var oldMagnitudes = DoubleArray(samples/2)

            //var waveformBuffer: IntArray
            override fun onWaveFormDataCapture(
                visualizer: Visualizer,
                waveform: ByteArray,
                samplingRate: Int
            ) {
                for (i in 0 until samples) {
                    intWaveBuffer[i] = (0xff shl 24) or (waveform[i].toInt() shl 16)
                    doubleWaveBuffer[i] = waveform[i].toDouble() / 256.0
                    doubleFFTBuffer[i] = 0.0
                }

                visualizerBitmap.setPixels(intWaveBuffer, 0, intWaveBuffer.size, 0, 1, intWaveBuffer.size, 1)
                fft.fft(doubleWaveBuffer, doubleFFTBuffer)
                for (i in 0 until samples / 2) {
                    val mag = hypot(doubleFFTBuffer[i], doubleWaveBuffer[i])
                    val p = MathUtils.clamp(mag * 1.5 - 20, 0.0, 255.0)
                    //val dB = 20f * log10(mag)
                    //val p = MathUtils.clamp(255.0 / (dB_max - dB_min) * (dB - dB_min), 0.0, 255.0)
                    val p_smooth = .8f * p + .2f * oldMagnitudes[i]
                    oldMagnitudes[i] = p_smooth

                    intFFTBuffer[i] = (0xff shl 24) or (p_smooth.toInt() shl 16)
                }
                visualizerBitmap.setPixels(intFFTBuffer, 0, intFFTBuffer.size, 0, 0, intFFTBuffer.size, 1)
            }
            override fun onFftDataCapture(visualizer: Visualizer?, fft: ByteArray?, samplingRate: Int) {}
        }
        visualizer.setDataCaptureListener(listener, Visualizer.getMaxCaptureRate(), true, false)
        visualizer.setCaptureSize(samples)
        visualizerBitmap = Bitmap
            .createBitmap(samples, 2, Bitmap.Config.ARGB_8888)
            .copy(Bitmap.Config.ARGB_8888, true)
        visualizerBitmap.eraseColor(Color.BLACK)
        visualizer.setScalingMode(Visualizer.SCALING_MODE_AS_PLAYED)
        visualizer.setEnabled(true)
        return visualizerBitmap
    }

    fun stopMusic() {
        mediaPlayer.release()
        visualizer.release()
    }
}





class FFT(private val n: Int) {
    var m: Int = (ln(n.toDouble()) / ln(2.0)).toInt()

    // Lookup tables. Only need to recompute when size of FFT changes.
    var cos: DoubleArray
    var sin: DoubleArray

    init {
        // Make sure n is a power of 2
        if (n != (1 shl m)) throw RuntimeException("FFT length must be power of 2")

        // precompute tables
        cos = DoubleArray(n / 2)
        sin = DoubleArray(n / 2)

        for (i in 0 until n / 2) {
            cos[i] = cos(-2 * Math.PI * i / n)
            sin[i] = sin(-2 * Math.PI * i / n)
        }
    }

    fun fft(x: DoubleArray, y: DoubleArray) {
        var j: Int
        var k: Int
        var n1: Int
        var n2: Int
        var a: Int
        var c: Double
        var s: Double
        var t1: Double
        var t2: Double

        // Bit-reverse
        j = 0
        n2 = n / 2
        var i = 1
        while (i < n - 1) {
            n1 = n2
            while (j >= n1) {
                j -= n1
                n1 /= 2
            }
            j += n1

            if (i < j) {
                t1 = x[i]
                x[i] = x[j]
                x[j] = t1
                t1 = y[i]
                y[i] = y[j]
                y[j] = t1
            }
            i++
        }

        // FFT
        n1 = 0
        n2 = 1

        i = 0
        while (i < m) {
            n1 = n2
            n2 += n2
            a = 0

            j = 0
            while (j < n1) {
                c = cos[a]
                s = sin[a]
                a += 1 shl (m - i - 1)

                k = j
                while (k < n) {
                    t1 = c * x[k + n1] - s * y[k + n1]
                    t2 = s * x[k + n1] + c * y[k + n1]
                    x[k + n1] = x[k] - t1
                    y[k + n1] = y[k] - t2
                    x[k] = x[k] + t1
                    y[k] = y[k] + t2
                    k += n2
                }
                j++
            }
            i++
        }
    }
}


/*@RequiresApi(24)
class AudioAnalyzer() {
    private val SAMPLE_RATE = 44100
    private val bufferSize: Int = AudioRecord.getMinBufferSize(
        SAMPLE_RATE,
        AudioFormat.CHANNEL_IN_MONO,
        AudioFormat.ENCODING_PCM_16BIT
    ).let {
        if (it == AudioRecord.ERROR || it == AudioRecord.ERROR_BAD_VALUE) {
            log("Problem with buffer size! $it")
            SAMPLE_RATE * 2
        } else {
            it
        }
    }
    private val audioFormat = AudioFormat.Builder()
        .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
        .setSampleRate(SAMPLE_RATE)
        .setChannelMask(AudioFormat.CHANNEL_IN_MONO)
        .build()
    @SuppressLint("MissingPermission")
    private val audioRecorder = AudioRecord.Builder()
        .setAudioSource(MediaRecorder.AudioSource.UNPROCESSED)
        .setAudioFormat(audioFormat)
        .setBufferSizeInBytes(bufferSize)
        .build()

    private var runningRecordJob: Job? = null

    fun start() {
        val audioPriorityDispatcher = newSingleThreadContext("audio_thread")
        runningRecordJob = scope.launch(audioPriorityDispatcher) {
            android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_AUDIO)
            record()
        }
    }

    fun stop() {
        runningRecordJob?.cancel()
    }

    private fun CoroutineScope.record() {
        val audioBuffer = ShortArray(bufferSize / 2)
        if (audioRecorder.state != AudioRecord.STATE_INITIALIZED) {
            log("Audio recorder not initialized!")
            return
        }
        audioRecorder.startRecording()
        while (isActive) {
            val readCount = audioRecorder.read(audioBuffer, 0, audioBuffer.size)
            log("data ${audioBuffer[0]} ${audioBuffer[1]}")
        }
    }
}

 */