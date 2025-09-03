package com.huanli233.hibari2.foundation.layout

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.node.ModifierNodeElement
import androidx.compose.ui.platform.InspectorInfo
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.IntSize
import com.huanli233.hibari2.runtime.composable.HibariLayout
import com.huanli233.hibari2.runtime.layout.MeasurePolicy
import com.huanli233.hibari2.runtime.layout.Placeable
import com.huanli233.hibari2.runtime.modifier.ParentDataModifierNode
import kotlin.math.max

// region: ParentData Modifiers

private data class BoxParentData(
    var alignment: Alignment? = null,
    var matchParentSize: Boolean = false
)

private class BoxChildDataNode(
    var alignment: Alignment?,
    var matchParentSize: Boolean
) : Modifier.Node(), ParentDataModifierNode {
    override fun modifyParentData(parentData: Any?): Any {
        val data = parentData as? BoxParentData ?: BoxParentData()
        alignment?.let { data.alignment = it }
        if (matchParentSize) data.matchParentSize = true
        return data
    }
}

private data class BoxChildDataElement(
    val alignment: Alignment?,
    val matchParentSize: Boolean
) : ModifierNodeElement<BoxChildDataNode>() {
    override fun create() = BoxChildDataNode(alignment, matchParentSize)

    override fun update(node: BoxChildDataNode) {
        node.alignment = alignment
        node.matchParentSize = matchParentSize
    }

    override fun InspectorInfo.inspectableProperties() {
        name = if (matchParentSize) "matchParentSize" else "align"
        properties["alignment"] = alignment
        properties["matchParentSize"] = matchParentSize
    }
}

// endregion

interface BoxScope {
    fun Modifier.align(alignment: Alignment): Modifier
    fun Modifier.matchParentSize(): Modifier
}

private object BoxScopeInstance : BoxScope {
    override fun Modifier.align(alignment: Alignment): Modifier {
        return this.then(BoxChildDataElement(alignment = alignment, matchParentSize = false))
    }

    override fun Modifier.matchParentSize(): Modifier {
        return this.then(BoxChildDataElement(alignment = null, matchParentSize = true))
    }
}

@Composable
fun Box(
    modifier: Modifier = Modifier,
    contentAlignment: Alignment = Alignment.TopStart,
    propagateMinConstraints: Boolean = false,
    content: @Composable BoxScope.() -> Unit
) {
    val measurePolicy = remember(contentAlignment, propagateMinConstraints) {
        createBoxMeasurePolicy(contentAlignment, propagateMinConstraints)
    }
    HibariLayout(
        content = { BoxScopeInstance.content() },
        measurePolicy = measurePolicy,
        modifier = modifier
    )
}

private fun createBoxMeasurePolicy(
    alignment: Alignment,
    propagateMinConstraints: Boolean
): MeasurePolicy {
    return MeasurePolicy { measurables, constraints ->
        if (measurables.isEmpty()) {
            return@MeasurePolicy layout(constraints.minWidth, constraints.minHeight) {}
        }

        val childConstraints = if (propagateMinConstraints) {
            constraints
        } else {
            constraints.copy(minWidth = 0, minHeight = 0)
        }

        val placeables = arrayOfNulls<Placeable>(measurables.size)
        var hasMatchParentSizeChildren = false
        var boxWidth = constraints.minWidth
        var boxHeight = constraints.minHeight

        measurables.forEachIndexed { index, measurable ->
            val parentData = measurable.parentData as? BoxParentData ?: BoxParentData()
            if (!parentData.matchParentSize) {
                val placeable = measurable.measure(childConstraints)
                placeables[index] = placeable
                boxWidth = max(boxWidth, placeable.width)
                boxHeight = max(boxHeight, placeable.height)
            } else {
                hasMatchParentSizeChildren = true
            }
        }

        if (hasMatchParentSizeChildren) {
            val matchParentSizeConstraints = Constraints.fixed(boxWidth, boxHeight)
            measurables.forEachIndexed { index, measurable ->
                val parentData = measurable.parentData as? BoxParentData ?: BoxParentData()
                if (parentData.matchParentSize) {
                    placeables[index] = measurable.measure(matchParentSizeConstraints)
                }
            }
        }

        layout(boxWidth, boxHeight) {

            placeables.forEachIndexed { index, placeable ->
                placeable as Placeable
                val measurable = measurables[index]
                val parentData = measurable.parentData as? BoxParentData ?: BoxParentData()
                val childAlignment = parentData.alignment ?: alignment
                val position = childAlignment.align(
                    IntSize(placeable.width, placeable.height),
                    IntSize(boxWidth, boxHeight),
                    layoutDirection
                )

                placeable.place(position.x, position.y)
            }
        }
    }
}