## OpenGL Pixel Shader

In Android 13 (API 33)
[RuntimeShader](https://developer.android.com/reference/android/graphics/RuntimeShader) where introduced.
This shaders can be programmed with the [AGSL Shading language](https://developer.android.com/develop/ui/views/graphics/agsl/using-agsl). 
This composable can use pixel shaders also in older Android versions with some modifications.
It is not possible to create a RuntimeShader. So you can not use this shader for drawing in a Canvas.

# How it works
To be able to use the same shader code for this opengl composable and for the agsl version it is necessary to convert the agsl shader code into a glsl compatible version.
There are many new data type aliases in AGSL which has to be converted to the GLSL versions. And also the main function needs to be changed.
