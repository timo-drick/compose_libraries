# Shader Live Coding plugin

This is an Android Studio plugin that can be used to update AGSL shader code without compiling and uploading the Android app every time. Of course there is live edit. But unfortunately it is not working with shaders.

![](/home/timo/projects/compose/github/compose_libraries/live_code_plugin/docs/plugin_android_studio.png)

This plugin does monitor files on your local development machine. There is also a client part needed inside your App to make it working. First you need to copy this file into your project:

[remote_live_shader.kt](../app/src/main/java/de/drick/compose/sample/ui/remote_live_shader.kt)

Specify the asset folder of your project in this file.
```kotlin
// Asset folder inside of the project.
const val ASSET_SRC_FOLDER = "app/src/main/assets"
```

Than you can use it every where you are using shader files.

```diff
-val ctx = LocalContext.current
-val backgroundShader = remember {
-    ctx.assets.open("shader_test.agsl").bufferedReader().readText()
-}
+val backgroundShader = remoteAssetAsState("shader_test.agsl")
```
