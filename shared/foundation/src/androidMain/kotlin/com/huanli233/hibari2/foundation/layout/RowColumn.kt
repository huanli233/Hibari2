package com.huanli233.hibari2.foundation.layout

import androidx.annotation.FloatRange
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.node.ModifierNodeElement
import androidx.compose.ui.platform.InspectorInfo
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.LayoutDirection
import com.huanli233.hibari2.runtime.composable.HibariLayout
import com.huanli233.hibari2.runtime.layout.MeasurePolicy
import com.huanli233.hibari2.runtime.layout.Placeable
import com.huanli233.hibari2.runtime.modifier.ParentDataModifierNode
import kotlin.math.max
import kotlin.math.roundToInt

object Arrangement {
    val Top: Vertical = VerticalAlignment.Top
    val Bottom: Vertical = VerticalAlignment.Bottom
    val Center: Vertical = VerticalAlignment.Center
    val SpaceEvenly: Vertical = VerticalArrangement.SpaceEvenly
    val SpaceBetween: Vertical = VerticalArrangement.SpaceBetween
    val SpaceAround: Vertical = VerticalArrangement.SpaceAround
    val Start: Horizontal = HorizontalArrangement.Start
    val End: Horizontal = HorizontalArrangement.End
    fun Center(bias: Float = 0f): Horizontal = HorizontalArrangement.Center
    val SpaceEvenlyHorizontally: Horizontal = HorizontalArrangement.SpaceEvenly
    val SpaceBetweenHorizontally: Horizontal = HorizontalArrangement.SpaceBetween
    val SpaceAroundHorizontally: Horizontal = HorizontalArrangement.SpaceAround

    interface Vertical {
        fun arrange(totalSize: Int, sizes: IntArray, outPositions: IntArray)
    }
    interface Horizontal {
        fun arrange(totalSize: Int, sizes: IntArray, layoutDirection: LayoutDirection, outPositions: IntArray)
    }
    private enum class VerticalAlignment : Vertical {
        Top { override fun arrange(totalSize: Int, sizes: IntArray, outPositions: IntArray) { var current = 0; sizes.forEachIndexed { i, size -> outPositions[i] = current; current += size } } },
        Bottom { override fun arrange(totalSize: Int, sizes: IntArray, outPositions: IntArray) { var current = totalSize; sizes.reversed().forEachIndexed { i, size -> current -= size; outPositions[sizes.size - 1 - i] = current } } },
        Center { override fun arrange(totalSize: Int, sizes: IntArray, outPositions: IntArray) { val consumedSize = sizes.sum(); var current = (totalSize - consumedSize).toFloat() / 2; sizes.forEachIndexed { i, size -> outPositions[i] = current.roundToInt(); current += size } } }
    }
    private object VerticalArrangement {
        val SpaceEvenly = object : Vertical { override fun arrange(totalSize: Int, sizes: IntArray, outPositions: IntArray) { val consumedSize = sizes.sum(); val gapSize = (totalSize - consumedSize).toFloat() / (sizes.size + 1); var current = gapSize; sizes.forEachIndexed { i, size -> outPositions[i] = current.roundToInt(); current += size + gapSize } } }
        val SpaceBetween = object : Vertical { override fun arrange(totalSize: Int, sizes: IntArray, outPositions: IntArray) { val consumedSize = sizes.sum(); val gapSize = if (sizes.size > 1) { (totalSize - consumedSize).toFloat() / (sizes.size - 1) } else { 0f }; var current = 0f; sizes.forEachIndexed { i, size -> outPositions[i] = current.roundToInt(); current += size + gapSize } } }
        val SpaceAround = object : Vertical { override fun arrange(totalSize: Int, sizes: IntArray, outPositions: IntArray) { val consumedSize = sizes.sum(); val gapSize = if (sizes.isNotEmpty()) { (totalSize - consumedSize).toFloat() / sizes.size } else { 0f }; var current = gapSize / 2; sizes.forEachIndexed { i, size -> outPositions[i] = current.roundToInt(); current += size + gapSize } } }
    }
    private object HorizontalArrangement {
        val Start = object : Horizontal { override fun arrange(totalSize: Int, sizes: IntArray, layoutDirection: LayoutDirection, outPositions: IntArray) { if (layoutDirection == LayoutDirection.Ltr) { VerticalAlignment.Top.arrange(totalSize, sizes, outPositions) } else { VerticalAlignment.Bottom.arrange(totalSize, sizes, outPositions) } } }
        val End = object : Horizontal { override fun arrange(totalSize: Int, sizes: IntArray, layoutDirection: LayoutDirection, outPositions: IntArray) { if (layoutDirection == LayoutDirection.Ltr) { VerticalAlignment.Bottom.arrange(totalSize, sizes, outPositions) } else { VerticalAlignment.Top.arrange(totalSize, sizes, outPositions) } } }
        val Center = object : Horizontal { override fun arrange(totalSize: Int, sizes: IntArray, layoutDirection: LayoutDirection, outPositions: IntArray) { VerticalAlignment.Center.arrange(totalSize, sizes, outPositions) } }
        val SpaceEvenly = object : Horizontal { override fun arrange(totalSize: Int, sizes: IntArray, layoutDirection: LayoutDirection, outPositions: IntArray) { VerticalArrangement.SpaceEvenly.arrange(totalSize, sizes, outPositions) } }
        val SpaceBetween = object : Horizontal { override fun arrange(totalSize: Int, sizes: IntArray, layoutDirection: LayoutDirection, outPositions: IntArray) { VerticalArrangement.SpaceBetween.arrange(totalSize, sizes, outPositions) } }
        val SpaceAround = object : Horizontal { override fun arrange(totalSize: Int, sizes: IntArray, layoutDirection: LayoutDirection, outPositions: IntArray) { VerticalArrangement.SpaceAround.arrange(totalSize, sizes, outPositions) } }
    }
}

// region: ParentData Modifiers

private data class RowColumnParentData(
    var weight: Float = 0f,
    var isFilled: Boolean = true,
    var crossAxisAlignment: Any? = null
)

private class LayoutWeightNode(
    var weight: Float,
    var fill: Boolean
) : Modifier.Node(), ParentDataModifierNode {
    override fun modifyParentData(parentData: Any?): Any {
        val data = parentData as? RowColumnParentData ?: RowColumnParentData()
        data.weight = weight
        data.isFilled = fill
        return data
    }
}

private data class LayoutWeightElement(
    val weight: Float,
    val fill: Boolean
) : ModifierNodeElement<LayoutWeightNode>() {
    override fun create() = LayoutWeightNode(weight, fill)
    override fun update(node: LayoutWeightNode) {
        node.weight = weight
        node.fill = fill
    }
    override fun InspectorInfo.inspectableProperties() {
        name = "weight"
        properties["weight"] = weight
        properties["fill"] = fill
    }
}

private class CrossAxisAlignmentNode(
    var alignment: Any
) : Modifier.Node(), ParentDataModifierNode {
    override fun modifyParentData(parentData: Any?): Any {
        val data = parentData as? RowColumnParentData ?: RowColumnParentData()
        data.crossAxisAlignment = alignment
        return data
    }
}

private data class CrossAxisAlignmentElement(
    val alignment: Any
) : ModifierNodeElement<CrossAxisAlignmentNode>() {
    override fun create() = CrossAxisAlignmentNode(alignment)
    override fun update(node: CrossAxisAlignmentNode) {
        node.alignment = alignment
    }
    override fun InspectorInfo.inspectableProperties() {
        name = "align"
        properties["alignment"] = alignment
    }
}

// endregion

interface ColumnScope {
    fun Modifier.weight(
        @FloatRange(from = 0.0, fromInclusive = false)
        weight: Float,
        fill: Boolean = true
    ): Modifier
    fun Modifier.align(alignment: Alignment.Horizontal): Modifier
}

private object ColumnScopeInstance : ColumnScope {
    override fun Modifier.weight(weight: Float, fill: Boolean): Modifier {
        require(weight > 0.0) { "invalid weight $weight; must be greater than zero" }
        return this.then(LayoutWeightElement(weight, fill))
    }
    override fun Modifier.align(alignment: Alignment.Horizontal): Modifier = this.then(CrossAxisAlignmentElement(alignment))
}

interface RowScope {
    fun Modifier.weight(
        @FloatRange(from = 0.0, fromInclusive = false)
        weight: Float,
        fill: Boolean = true
    ): Modifier
    fun Modifier.align(alignment: Alignment.Vertical): Modifier
}

private object RowScopeInstance : RowScope {
    override fun Modifier.weight(weight: Float, fill: Boolean): Modifier {
        require(weight > 0.0) { "invalid weight $weight; must be greater than zero" }
        return this.then(LayoutWeightElement(weight, fill))
    }
    override fun Modifier.align(alignment: Alignment.Vertical): Modifier = this.then(CrossAxisAlignmentElement(alignment))
}

private fun rowColumnMeasurePolicy(
    orientation: LayoutOrientation,
    arrangement: Any,
    crossAxisAlignment: Any,
    crossAxisSize: SizeMode
): MeasurePolicy {
    return MeasurePolicy { measurables, constraints ->

        val horizontal = orientation == LayoutOrientation.Horizontal
        val arrangementSpacing = 0
        val placeables = arrayOfNulls<Placeable>(measurables.size)
        val childrenCount = measurables.size
        var totalWeight = 0f
        var fixedSpace = 0
        var crossAxisSpace = 0
        var weightedCount = 0

        for (i in 0 until childrenCount) {
            val child = measurables[i]
            val childParentData = child.parentData as? RowColumnParentData ?: RowColumnParentData()
            val weight = childParentData.weight
            if (weight > 0f) {
                totalWeight += weight
                weightedCount++
            } else {
                val childConstraints = if (horizontal) {
                    Constraints(0, constraints.maxWidth, constraints.minHeight, constraints.maxHeight)
                } else {
                    Constraints(constraints.minWidth, constraints.maxWidth, 0, constraints.maxHeight)
                }
                val placeable = child.measure(childConstraints)
                placeables[i] = placeable
                val mainAxisSize = if (horizontal) placeable.width else placeable.height
                val crossAxisSizeValue = if (horizontal) placeable.height else placeable.width
                fixedSpace += mainAxisSize
                crossAxisSpace = max(crossAxisSpace, crossAxisSizeValue)
            }
        }

        val arrangementSpacingSum = (arrangementSpacing * (childrenCount - 1)).coerceAtLeast(0)
        fixedSpace += arrangementSpacingSum

        val remainingSpace = (if (horizontal) constraints.maxWidth else constraints.maxHeight) - fixedSpace
        val spacePerWeight = if (totalWeight > 0f && remainingSpace > 0) remainingSpace.toFloat() / totalWeight else 0f

        if (weightedCount > 0) {
            for (i in 0 until childrenCount) {
                val child = measurables[i]
                val childParentData = child.parentData as? RowColumnParentData ?: RowColumnParentData()
                val weight = childParentData.weight
                if (weight > 0f) {
                    val childMainAxisSize = (weight * spacePerWeight).roundToInt()
                    val childConstraints = if (horizontal) {
                        Constraints(childMainAxisSize, childMainAxisSize, constraints.minHeight, constraints.maxHeight)
                    } else {
                        Constraints(constraints.minWidth, constraints.maxWidth, childMainAxisSize, childMainAxisSize)
                    }
                    val placeable = child.measure(childConstraints)
                    placeables[i] = placeable
                    val crossAxisSizeValue = if (horizontal) placeable.height else placeable.width
                    crossAxisSpace = max(crossAxisSpace, crossAxisSizeValue)
                }
            }
        }

        val mainAxisLayoutSize = if (horizontal) {
            fixedSpace.coerceIn(constraints.minWidth, constraints.maxWidth)
        } else {
            fixedSpace.coerceIn(constraints.minHeight, constraints.maxHeight)
        }

        val crossAxisLayoutSize = if ((if (horizontal) constraints.hasBoundedHeight else constraints.hasBoundedWidth) && crossAxisSize == SizeMode.Expand) {
            if (horizontal) constraints.maxHeight else constraints.maxWidth
        } else {
            crossAxisSpace
        }

        val mainAxisPositions = IntArray(childrenCount) { 0 }
        val childrenMainAxisSizes = IntArray(childrenCount) { i ->
            val placeable = placeables[i]!!
            if (horizontal) placeable.width else placeable.height
        }

        if (horizontal) {
            (arrangement as Arrangement.Horizontal).arrange(mainAxisLayoutSize, childrenMainAxisSizes, this.layoutDirection, mainAxisPositions)
        } else {
            (arrangement as Arrangement.Vertical).arrange(mainAxisLayoutSize, childrenMainAxisSizes, mainAxisPositions)
        }

        val finalWidth = if (horizontal) mainAxisLayoutSize else crossAxisLayoutSize
        val finalHeight = if (horizontal) crossAxisLayoutSize else mainAxisLayoutSize

        layout(finalWidth, finalHeight) {
            for (i in 0 until childrenCount) {
                val placeable = placeables[i]!!
                val childParentData = measurables[i].parentData as? RowColumnParentData ?: RowColumnParentData()
                val childCrossAxisAlignment = childParentData.crossAxisAlignment
                val crossAxisPosition = if (childCrossAxisAlignment != null) {
                    if (horizontal) {
                        (childCrossAxisAlignment as Alignment.Vertical).align(0, finalHeight - placeable.height)
                    } else {
                        (childCrossAxisAlignment as Alignment.Horizontal).align(0, finalWidth - placeable.width, layoutDirection)
                    }
                } else {
                    if (horizontal) {
                        (crossAxisAlignment as Alignment.Vertical).align(0, finalHeight - placeable.height)
                    } else {
                        (crossAxisAlignment as Alignment.Horizontal).align(0, finalWidth - placeable.width, layoutDirection)
                    }
                }
                if (horizontal) {
                    placeable.place(mainAxisPositions[i], crossAxisPosition)
                } else {
                    placeable.place(crossAxisPosition, mainAxisPositions[i])
                }
            }
        }
    }
}

private enum class LayoutOrientation { Horizontal, Vertical }
private enum class SizeMode { Wrap, Expand }

@Composable
fun Row(
    modifier: Modifier = Modifier,
    horizontalArrangement: Arrangement.Horizontal = Arrangement.Start,
    verticalAlignment: Alignment.Vertical = Alignment.Top,
    content: @Composable RowScope.() -> Unit
) {
    val measurePolicy = remember(horizontalArrangement, verticalAlignment) {
        rowColumnMeasurePolicy(LayoutOrientation.Horizontal, horizontalArrangement, verticalAlignment, SizeMode.Wrap)
    }
    HibariLayout(
        modifier = modifier,
        measurePolicy = measurePolicy,
        content = { RowScopeInstance.content() }
    )
}

@Composable
fun Column(
    modifier: Modifier = Modifier,
    verticalArrangement: Arrangement.Vertical = Arrangement.Top,
    horizontalAlignment: Alignment.Horizontal = Alignment.Start,
    content: @Composable ColumnScope.() -> Unit
) {
    val measurePolicy = remember(verticalArrangement, horizontalAlignment) {
        rowColumnMeasurePolicy(LayoutOrientation.Vertical, verticalArrangement, horizontalAlignment, SizeMode.Wrap)
    }
    HibariLayout(
        modifier = modifier,
        measurePolicy = measurePolicy,
        content = { ColumnScopeInstance.content() }
    )
}