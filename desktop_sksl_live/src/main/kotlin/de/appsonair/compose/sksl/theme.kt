package de.appsonair.compose.sksl

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val primary = Color(0xFFF49F0D)
private val primaryVariant = Color(0xFFFABE5D)
private val activeInverted = Color.White

private val secondary = Color(0xFF894E34)
private val secondaryVariant = Color(0xFF915E36)
private val foreground = Color(0xFF894E34)
private val background = Color(0xFFFEEED2)
private val backgroundVariant = Color(0xFFFAC367)

val lightThemeColors = lightColorScheme(
    primary = primary,
    //primaryVariant = primaryVariant,
    onPrimary = foreground,
    secondary = secondary,
    //secondaryVariant = secondaryVariant,
    onSecondary = Color.White,
    background = backgroundVariant,
    onBackground = foreground,
    surface = background,
    onSurface = foreground,
    error = Color(0xFFD00036),
    onError = Color.White,
)

@Composable
fun EbonTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = lightThemeColors,
        content = content
    )
}
