package com.huanli233.hibari2.runtime.node

import androidx.compose.ui.graphics.GraphicsLayerScope
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import com.huanli233.hibari2.runtime.layout.Measurable
import com.huanli233.hibari2.runtime.layout.MeasureResult
import com.huanli233.hibari2.runtime.layout.Placeable
import com.huanli233.hibari2.runtime.modifier.ParentDataModifierNode

class LookaheadDelegate(val layoutNode: LayoutNode) : Placeable(), Measurable {
    var position: IntOffset = IntOffset.Zero
    private var _measureResult: MeasureResult? = null
    val measureResult: MeasureResult
        get() = _measureResult ?: error("LookaheadDelegate has not been measured yet")

    val isPlaced: Boolean
        get() = layoutNode.isPlacedInLookahead

    val size: IntSize
        get() = measuredSize

    fun performMeasure(constraints: Constraints): Placeable {
        measurementConstraints = constraints
        _measureResult = layoutNode.measurePass(constraints, isLookingAhead = true)
        measuredSize = IntSize(_measureResult!!.width, _measureResult!!.height)
        return this
    }

    override fun placeAt(
        position: IntOffset,
        zIndex: Float,
        layerBlock: ((GraphicsLayerScope) -> Unit)?
    ) {
        if (this.position != position) {
            this.position = position
        }
        layoutNode.isPlacedInLookahead = true
    }

    fun placeChildren() {
        measureResult.placeChildren(PlacementScope(position, width, layoutNode.layoutDirection))
    }

    override val parentData: Any? by lazy(LazyThreadSafetyMode.NONE) {
        val nodes = layoutNode.modifierNodes
        nodes.filterIsInstance<ParentDataModifierNode>().fold(null as Any?) { current, modifier ->
            modifier.modifyParentData(current)
        }
    }

    override fun measure(constraints: Constraints): Placeable = performMeasure(constraints)

    override fun minIntrinsicWidth(height: Int): Int = layoutNode.minIntrinsicWidth(height, layoutNode.density)
    override fun maxIntrinsicWidth(height: Int): Int = layoutNode.maxIntrinsicWidth(height, layoutNode.density)
    override fun minIntrinsicHeight(width: Int): Int = layoutNode.minIntrinsicHeight(width, layoutNode.density)
    override fun maxIntrinsicHeight(width: Int): Int = layoutNode.maxIntrinsicHeight(width, layoutNode.density)
}