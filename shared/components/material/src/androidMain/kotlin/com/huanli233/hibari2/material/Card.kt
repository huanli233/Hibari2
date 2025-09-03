package com.huanli233.hibari2.material

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.google.android.material.card.MaterialCardView
import com.huanli233.hibari2.runtime.composable.AndroidViewGroup

@Composable
fun Card(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    AndroidViewGroup(
        factory = { context ->
            MaterialCardView(context)
        },
        modifier = modifier,
        content = content
    )
}