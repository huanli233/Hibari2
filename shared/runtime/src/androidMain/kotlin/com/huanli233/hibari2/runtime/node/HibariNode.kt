package com.huanli233.hibari2.runtime.node

import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Density
import com.huanli233.hibari2.runtime.layout.Placeable
import com.huanli233.hibari2.runtime.modifier.GraphicsLayerData

interface HibariNode {
    var parent: HibariNode?
    val children: MutableList<HibariNode>
    val modifier: Modifier
    val graphicsLayerData: GraphicsLayerData

    fun measure(constraints: Constraints, density: Density): Placeable
    fun minIntrinsicWidth(height: Int, density: Density): Int
    fun maxIntrinsicWidth(height: Int, density: Density): Int
    fun minIntrinsicHeight(width: Int, density: Density): Int
    fun maxIntrinsicHeight(width: Int, density: Density): Int

    fun placeAt(x: Int, y: Int, zIndex: Float = 0f)
    fun applyModifier(newModifier: Modifier)
    fun updateGraphics()

    fun onDisposed()
    var onInvalidate: (() -> Unit)?
}