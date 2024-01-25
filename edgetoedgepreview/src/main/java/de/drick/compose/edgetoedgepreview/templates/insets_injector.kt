@file:Suppress("INVISIBLE_MEMBER", "INVISIBLE_REFERENCE")

package de.drick.compose.edgetoedgepreview.templates

import androidx.compose.foundation.layout.WindowInsetsHolder
import androidx.compose.runtime.Composable
import androidx.core.graphics.Insets
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsCompat.Type.InsetsType

interface WindowInsetsState {
    fun update(block: InsetsDsl.() -> Unit)
}

enum class InsetPos {
    LEFT, RIGHT, TOP, BOTTOM
}

interface InsetsDsl {
    fun setInset(pos: InsetPos, @InsetsType type: Int, size: Int)
}

/**
 * This is a hack and may fail in future compose versions!
 */
private class WindowInsetsStateImpl(
    private val holder: WindowInsetsHolder
): WindowInsetsState {
    override fun update(block: InsetsDsl.() -> Unit) {
        val dsl = object : InsetsDsl {
            val builder = WindowInsetsCompat.Builder()
            override fun setInset(pos: InsetPos, @InsetsType type: Int, size: Int) {
                val rect = android.graphics.Rect()
                when (pos) {
                    InsetPos.LEFT -> rect.left = size
                    InsetPos.RIGHT -> rect.right = size
                    InsetPos.TOP -> rect.top = size
                    InsetPos.BOTTOM -> rect.bottom = size
                }
                builder.setInsets(type, Insets.of(rect))
            }
        }
        block(dsl)
        holder.update(dsl.builder.build(), 0)
    }
}

@Composable
fun rememberWindowInsetsState(): WindowInsetsState =
    WindowInsetsStateImpl(WindowInsetsHolder.current())
