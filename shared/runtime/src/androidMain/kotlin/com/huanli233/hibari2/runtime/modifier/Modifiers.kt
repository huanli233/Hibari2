package com.huanli233.hibari2.runtime.modifier

import android.util.Log
import android.view.View
import androidx.compose.ui.Modifier
import androidx.compose.ui.node.DelegatableNode
import androidx.compose.ui.node.ModifierNodeElement
import androidx.compose.ui.platform.InspectorInfo
import androidx.compose.ui.unit.Constraints
import com.huanli233.hibari2.runtime.layout.IntrinsicMeasureScope
import com.huanli233.hibari2.runtime.layout.Measurable
import com.huanli233.hibari2.runtime.layout.MeasureResult
import com.huanli233.hibari2.runtime.layout.MeasureScope
import com.huanli233.hibari2.runtime.utils.NodeMeasuringIntrinsics
import kotlin.reflect.KClass

interface LayoutModifierNode : DelegatableNode {
    fun MeasureScope.measure(
        measurable: Measurable,
        constraints: Constraints
    ): MeasureResult

    fun IntrinsicMeasureScope.minIntrinsicWidth(measurable: Measurable, height: Int): Int =
        NodeMeasuringIntrinsics.minWidth(this@LayoutModifierNode, measurable, height)
    fun IntrinsicMeasureScope.minIntrinsicHeight(measurable: Measurable, width: Int): Int =
        NodeMeasuringIntrinsics.minHeight(this@LayoutModifierNode, measurable, width)
    fun IntrinsicMeasureScope.maxIntrinsicWidth(measurable: Measurable, height: Int): Int =
        NodeMeasuringIntrinsics.maxWidth(this@LayoutModifierNode, measurable, height)
    fun IntrinsicMeasureScope.maxIntrinsicHeight(measurable: Measurable, width: Int): Int =
        NodeMeasuringIntrinsics.maxHeight(this@LayoutModifierNode, measurable, width)
}

private class LayoutModifierNodeImpl(
    var measureBlock: MeasureScope.(Measurable, Constraints) -> MeasureResult
) : Modifier.Node(), LayoutModifierNode {
    override fun MeasureScope.measure(
        measurable: Measurable,
        constraints: Constraints
    ): MeasureResult = measureBlock(measurable, constraints)
}

private data class LayoutModifierElement(
    val measure: MeasureScope.(Measurable, Constraints) -> MeasureResult
) : ModifierNodeElement<LayoutModifierNodeImpl>() {
    override fun create(): LayoutModifierNodeImpl = LayoutModifierNodeImpl(measure)
    override fun update(node: LayoutModifierNodeImpl) { node.measureBlock = measure }
    override fun InspectorInfo.inspectableProperties() {
        name = "layout"
        properties["measure"] = measure
    }
}

fun Modifier.layout(
    measure: MeasureScope.(Measurable, Constraints) -> MeasureResult
): Modifier = this then LayoutModifierElement(measure)

interface ParentDataModifierNode : DelegatableNode {
    fun modifyParentData(parentData: Any?): Any?
}

interface ViewAttributeModifier<V : View> : Modifier.Element {
    val key: KClass<out ViewAttributeModifier<*>> get() = this::class
    fun apply(view: V)
    fun reset(view: V)
}

fun Modifier.requiresViewInstance(): Boolean =
    this.any {
        it is ViewAttributeModifier<*> || it is SimpleGraphicsLayerModifier
    }