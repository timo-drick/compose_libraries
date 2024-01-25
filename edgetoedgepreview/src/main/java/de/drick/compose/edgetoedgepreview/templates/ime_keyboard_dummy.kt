package de.drick.compose.edgetoedgepreview.templates

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Preview(name = "IME keyboard")
@Composable
fun ImeKeyboardDummy(
    modifier: Modifier = Modifier,
    isDarkMode: Boolean = false
) {
    val contentColor = if (isDarkMode) Color.White else Color.Black
    CompositionLocalProvider(
        LocalContentColor provides contentColor
    ) {
        Row(modifier.padding(4.dp)) {
            Text("11:52")
            Spacer(Modifier.weight(1f))
            Icon(
                imageVector = Icons.Default.Wifi,
                contentDescription = "Wifi icon"
            )
            Icon(
                imageVector = Icons.Default.BatteryChargingFull,
                contentDescription = "Wifi icon",

                )
        }
    }
}
