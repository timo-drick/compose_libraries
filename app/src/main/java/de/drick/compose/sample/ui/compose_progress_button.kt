package de.drick.compose.sample.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.RowScope
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import kotlinx.coroutines.launch

@Composable
fun ProgressButton(
    modifier: Modifier = Modifier,
    onClick: suspend () -> Unit,
    content: @Composable RowScope.() -> Unit
) {
    val inProgress = remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    Box {
        Button(
            modifier = modifier,
            enabled = inProgress.value.not(),
            onClick = {
                inProgress.value = true
                scope.launch {
                    onClick()
                    inProgress.value = false
                }
            },
            content = content
        )
        if (inProgress.value) {
            CircularProgressIndicator(Modifier.align(Alignment.Center))
        }
    }
}