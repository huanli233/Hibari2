package com.huanli233.hibari2.material

import android.widget.LinearLayout
import android.widget.TextView
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.google.android.material.textview.MaterialTextView

@Composable
fun NativeLinearLayout(
    modifier: Modifier = Modifier,
    orientation: Int = LinearLayout.VERTICAL,
    update: (LinearLayout) -> Unit = {},
    content: @Composable () -> Unit
) {
    NativeViewGroup(
        factory = { context -> LinearLayout(context) },
        modifier = modifier,
        update = {
            it.orientation = orientation
            update(it)
        },
        content = content
    )
}

@Composable
fun NativeText(
    text: String,
    modifier: Modifier = Modifier,
    update: (TextView) -> Unit = {}
) {
    NativeView(
        factory = { context -> MaterialTextView(context) },
        modifier = modifier,
        update = {
            it.text = text
            update(it)
        }
    )
}