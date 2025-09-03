package com.huanli233.hibari2.material

import android.graphics.drawable.Drawable
import androidx.annotation.DrawableRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import com.google.android.material.button.MaterialButton
import com.huanli233.hibari2.runtime.composable.ComposeHibariNode
import com.huanli233.hibari2.runtime.composable.HibariView
import com.huanli233.hibari2.runtime.modifier.ViewAttributeModifier

interface ButtonModifier : ViewAttributeModifier<MaterialButton>

private class TextModifier(val text: CharSequence) : ButtonModifier {
    override fun apply(view: MaterialButton) { view.text = text }
    override fun reset(view: MaterialButton) { view.text = null }
    override fun hashCode(): Int = text.hashCode()
    override fun equals(other: Any?): Boolean = other is TextModifier && other.text == text
}

private class IconModifier(
    @DrawableRes val resId: Int?,
    val drawable: Drawable?
) : ButtonModifier {
    override fun apply(view: MaterialButton) {
        val icon = drawable ?: resId?.let { ContextCompat.getDrawable(view.context, it) }
        view.icon = icon
    }
    override fun reset(view: MaterialButton) { view.icon = null }
    override fun hashCode(): Int = resId.hashCode() + drawable.hashCode()
    override fun equals(other: Any?): Boolean = other is IconModifier && other.resId == resId && other.drawable == drawable
}

fun Modifier.text(text: CharSequence): Modifier = this.then(TextModifier(text))
fun Modifier.icon(@DrawableRes resId: Int): Modifier = this.then(IconModifier(resId, null))
fun Modifier.icon(drawable: Drawable): Modifier = this.then(IconModifier(null, drawable))

@Composable
fun Button(
    text: CharSequence,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: Any? = null,
    enabled: Boolean = true,
) {
    HibariView(
        factory = { context ->
            MaterialButton(context)
        },
        modifier = modifier,
        update = { button ->
            button.isEnabled = enabled
            button.setOnClickListener { onClick() }
            button.text = text
            val (drawable, resId) = when (icon) {
                null -> null to null
                is Int -> null to icon
                is Drawable -> icon to null
                else -> throw IllegalArgumentException("Unsupported icon type: $icon")
            }
            val icon = drawable ?: resId?.let { ContextCompat.getDrawable(button.context, it) }
            button.icon = icon
        }
    )
}