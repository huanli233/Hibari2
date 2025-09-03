package com.huanli233.hibari2.runtime

import androidx.compose.runtime.Applier
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReusableComposeNode
import com.huanli233.hibari2.runtime.node.LayoutNode

@Composable
fun LookaheadScope(content: @Composable () -> Unit) {
    ReusableComposeNode<LayoutNode, Applier<Any>>(
        factory = { LayoutNode(measurePolicy = { _, _ -> layout(0, 0) {} }, isLookaheadRoot = true) },
        update = {},
        content = content
    )
}