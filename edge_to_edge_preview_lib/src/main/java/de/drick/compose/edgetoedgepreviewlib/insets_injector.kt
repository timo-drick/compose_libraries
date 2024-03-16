@file:Suppress("INVISIBLE_MEMBER", "INVISIBLE_REFERENCE")

package de.drick.compose.edgetoedgepreviewlib

import androidx.compose.foundation.layout.WindowInsetsHolder
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
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
    fun setInset(pos: InsetPos, @InsetsType type: Int, size: Int, isVisible: Boolean)
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
            override fun setInset(pos: InsetPos, @InsetsType type: Int, size: Int, isVisible: Boolean) {
                val insets = when (pos) {
                    InsetPos.LEFT -> Insets.of(size, 0,0,0)
                    InsetPos.RIGHT -> Insets.of(0, 0,size,0)
                    InsetPos.TOP -> Insets.of(0, size,0,0)
                    InsetPos.BOTTOM -> Insets.of(0, 0,0,size)
                }
                if (isVisible) {
                    builder.setInsets(type, insets)
                }
                builder.setInsetsIgnoringVisibility(type, insets)
                builder.setVisible(type, isVisible)
            }
        }
        block(dsl)
        holder.update(dsl.builder.build(), 0)
    }
}

@Composable
fun rememberWindowInsetsState(): WindowInsetsState {
    val insetsHolder = WindowInsetsHolder.current()
    return remember { WindowInsetsStateImpl(insetsHolder) }
}
