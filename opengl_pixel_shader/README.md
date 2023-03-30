## OpenGL Pixel Shader

Since Android 13 (API 33) there are 
[RuntimeShader](https://developer.android.com/reference/android/graphics/RuntimeShader) in Android.
This shaders can be programmed with the AGSL Shading language [AGSL](https://developer.android.com/develop/ui/views/graphics/agsl/using-agsl). 
This composable can use pixel shaders also in older Android versions.
It is not possible to create a RuntimeShader. So you can not use this shader for drawing in a Canvas.
It is only possible to use this shader in the composable.
