package com.huanli233.hibari2.runtime.modifier

import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.compose.ui.Modifier

interface LayoutParamsModifier : Modifier.Element {
    fun createLayoutParams(parent: ViewGroup): ViewGroup.LayoutParams
}

private class LinearLayoutParamsModifier(
    private val width: Int,
    private val height: Int,
    private val weight: Float = 0f,
    private val gravity: Int = -1
) : LayoutParamsModifier {
    override fun createLayoutParams(parent: ViewGroup): ViewGroup.LayoutParams {
        val params = if (parent is LinearLayout) {
            LinearLayout.LayoutParams(width, height, weight)
        } else {
            ViewGroup.LayoutParams(width, height)
        }
        if (params is LinearLayout.LayoutParams) {
            params.gravity = gravity
        }
        return params
    }
}

fun Modifier.linearLayoutParams(
    width: Int,
    height: Int,
    weight: Float = 0f,
    gravity: Int = -1
): Modifier {
    return this.then(LinearLayoutParamsModifier(width, height, weight, gravity))
}

fun Modifier.weight(weight: Float, fill: Boolean = true): Modifier {
    val width = if (fill) 0 else ViewGroup.LayoutParams.WRAP_CONTENT
    val height = if (fill) 0 else ViewGroup.LayoutParams.WRAP_CONTENT
    return this.then(LinearLayoutParamsModifier(width, height, weight))
}