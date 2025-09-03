package com.huanli233.hibari2.material

import android.graphics.Typeface
import android.view.Gravity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Updater
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.isSpecified
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.isSpecified
import com.google.android.material.textview.MaterialTextView
import com.huanli233.hibari2.runtime.composable.ComposeHibariNode
import com.huanli233.hibari2.runtime.node.ViewNode

@Composable
fun Text(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = Color.Unspecified,
    fontSize: TextUnit = TextUnit.Unspecified,
    fontWeight: FontWeight? = null,
    textAlign: TextAlign? = null,
    maxLines: Int = Int.MAX_VALUE
) {
    val context = LocalContext.current
    val density = LocalDensity.current

    val textView = remember {
        MaterialTextView(context)
    }

    ComposeHibariNode(
        factory = { ViewNode(textView) },
        update = {
            @Suppress("UNCHECKED_CAST")
            (this as Updater<ViewNode<MaterialTextView>>).apply {
                set(modifier) {
                    applyModifier(modifier)
                }
                set(text) {
                    view.text = it
                }
                set(color) {
                    if (color.isSpecified) {
                        view.setTextColor(color.toArgb())
                    }
                }
                set(fontSize) {
                    if (fontSize.isSpecified) {
                        view.textSize = with (density) { fontSize.toPx() }
                    }
                }
                set(fontWeight) {
                    if (it != null) {
                        val currentTypeface = view.typeface ?: Typeface.DEFAULT
                        if (currentTypeface.isBold != (it >= FontWeight.Bold)) {
                            view.setTypeface(currentTypeface, if (it >= FontWeight.Bold) Typeface.BOLD else Typeface.NORMAL)
                        }
                    }
                }
                set(textAlign) {
                    val newGravity = when (it) {
                        TextAlign.Left, TextAlign.Start -> Gravity.START
                        TextAlign.Right, TextAlign.End -> Gravity.END
                        TextAlign.Center -> Gravity.CENTER_HORIZONTAL
                        else -> Gravity.NO_GRAVITY
                    }
                    if ((view.gravity and Gravity.HORIZONTAL_GRAVITY_MASK) != newGravity) {
                        view.gravity = (view.gravity and Gravity.VERTICAL_GRAVITY_MASK) or newGravity
                    }
                }
                set(maxLines) {
                    view.maxLines = it
                }
            }
        }
    )
}