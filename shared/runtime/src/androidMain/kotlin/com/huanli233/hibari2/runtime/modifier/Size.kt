package com.huanli233.hibari2.runtime.modifier

import androidx.annotation.FloatRange
import androidx.compose.runtime.Stable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.node.ModifierNodeElement
import androidx.compose.ui.platform.InspectorInfo
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.constrain
import androidx.compose.ui.unit.constrainHeight
import androidx.compose.ui.unit.constrainWidth
import androidx.compose.ui.unit.isSpecified
import androidx.compose.ui.util.fastCoerceAtLeast
import androidx.compose.ui.util.fastCoerceIn
import kotlin.math.roundToInt
import com.huanli233.hibari2.runtime.layout.IntrinsicMeasureScope
import com.huanli233.hibari2.runtime.layout.MeasureResult
import com.huanli233.hibari2.runtime.layout.MeasureScope
import com.huanli233.hibari2.runtime.layout.Measurable

@Stable
fun Modifier.width(width: Dp) =
    this.then(
        SizeElement(
            minWidth = width,
            maxWidth = width,
            minHeight = Dp.Unspecified,
            maxHeight = Dp.Unspecified,
            enforceIncoming = true,
        )
    )

@Stable
fun Modifier.height(height: Dp) =
    this.then(
        SizeElement(
            minWidth = Dp.Unspecified,
            maxWidth = Dp.Unspecified,
            minHeight = height,
            maxHeight = height,
            enforceIncoming = true
        )
    )

@Stable
fun Modifier.size(size: Dp) =
    this.then(
        SizeElement(
            minWidth = size,
            maxWidth = size,
            minHeight = size,
            maxHeight = size,
            enforceIncoming = true
        )
    )

@Stable
fun Modifier.size(width: Dp, height: Dp) =
    this.then(
        SizeElement(
            minWidth = width,
            maxWidth = width,
            minHeight = height,
            maxHeight = height,
            enforceIncoming = true
        )
    )

@Stable
fun Modifier.size(size: DpSize) = size(size.width, size.height)

@Stable
fun Modifier.widthIn(min: Dp = Dp.Unspecified, max: Dp = Dp.Unspecified) =
    this.then(
        SizeElement(
            minWidth = min,
            maxWidth = max,
            minHeight = Dp.Unspecified,
            maxHeight = Dp.Unspecified,
            enforceIncoming = true
        )
    )

@Stable
fun Modifier.heightIn(min: Dp = Dp.Unspecified, max: Dp = Dp.Unspecified) =
    this.then(
        SizeElement(
            minWidth = Dp.Unspecified,
            maxWidth = Dp.Unspecified,
            minHeight = min,
            maxHeight = max,
            enforceIncoming = true
        )
    )

@Stable
fun Modifier.sizeIn(
    minWidth: Dp = Dp.Unspecified,
    minHeight: Dp = Dp.Unspecified,
    maxWidth: Dp = Dp.Unspecified,
    maxHeight: Dp = Dp.Unspecified,
) =
    this.then(
        SizeElement(
            minWidth = minWidth,
            minHeight = minHeight,
            maxWidth = maxWidth,
            maxHeight = maxHeight,
            enforceIncoming = true
        )
    )

@Stable
fun Modifier.requiredWidth(width: Dp) =
    this.then(
        SizeElement(
            minWidth = width,
            maxWidth = width,
            minHeight = Dp.Unspecified,
            maxHeight = Dp.Unspecified,
            enforceIncoming = false
        )
    )

@Stable
fun Modifier.requiredHeight(height: Dp) =
    this.then(
        SizeElement(
            minWidth = Dp.Unspecified,
            maxWidth = Dp.Unspecified,
            minHeight = height,
            maxHeight = height,
            enforceIncoming = false
        )
    )

@Stable
fun Modifier.requiredSize(size: Dp) =
    this.then(
        SizeElement(
            minWidth = size,
            maxWidth = size,
            minHeight = size,
            maxHeight = size,
            enforceIncoming = false
        )
    )

@Stable
fun Modifier.requiredSize(width: Dp, height: Dp) =
    this.then(
        SizeElement(
            minWidth = width,
            maxWidth = width,
            minHeight = height,
            maxHeight = height,
            enforceIncoming = false
        )
    )

@Stable
fun Modifier.requiredSize(size: DpSize) = requiredSize(size.width, size.height)


@Stable
fun Modifier.fillMaxWidth(@FloatRange(from = 0.0, to = 1.0) fraction: Float = 1f) =
    this.then(FillElement(Direction.Horizontal, fraction))

@Stable
fun Modifier.fillMaxHeight(@FloatRange(from = 0.0, to = 1.0) fraction: Float = 1f) =
    this.then(FillElement(Direction.Vertical, fraction))

@Stable
fun Modifier.fillMaxSize(@FloatRange(from = 0.0, to = 1.0) fraction: Float = 1f) =
    this.then(FillElement(Direction.Both, fraction))

@Stable
fun Modifier.wrapContentWidth(
    align: Alignment.Horizontal = Alignment.CenterHorizontally,
    unbounded: Boolean = false,
) = this.then(WrapContentElement(Direction.Horizontal, unbounded) { size, layoutDirection ->
    IntOffset(align.align(0, size.width, layoutDirection), 0)
})

@Stable
fun Modifier.wrapContentHeight(
    align: Alignment.Vertical = Alignment.CenterVertically,
    unbounded: Boolean = false,
) = this.then(WrapContentElement(Direction.Vertical, unbounded) { size, _ ->
    IntOffset(0, align.align(0, size.height))
})

@Stable
fun Modifier.wrapContentSize(
    align: Alignment = Alignment.Center,
    unbounded: Boolean = false
) = this.then(WrapContentElement(Direction.Both, unbounded) { size, layoutDirection ->
    align.align(IntSize.Zero, size, layoutDirection)
})

private enum class Direction {
    Vertical,
    Horizontal,
    Both
}

private class SizeElement(
    private val minWidth: Dp,
    private val maxWidth: Dp,
    private val minHeight: Dp,
    private val maxHeight: Dp,
    private val enforceIncoming: Boolean,
) : ModifierNodeElement<SizeNode>() {
    override fun create(): SizeNode =
        SizeNode(minWidth, minHeight, maxWidth, maxHeight, enforceIncoming)

    override fun update(node: SizeNode) {
        node.minWidth = minWidth
        node.minHeight = minHeight
        node.maxWidth = maxWidth
        node.maxHeight = maxHeight
        node.enforceIncoming = enforceIncoming
    }

    override fun InspectorInfo.inspectableProperties() {
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is SizeElement) return false
        if (minWidth != other.minWidth) return false
        if (minHeight != other.minHeight) return false
        if (maxWidth != other.maxWidth) return false
        if (maxHeight != other.maxHeight) return false
        if (enforceIncoming != other.enforceIncoming) return false
        return true
    }

    override fun hashCode(): Int {
        var result = minWidth.hashCode()
        result = 31 * result + minHeight.hashCode()
        result = 31 * result + maxWidth.hashCode()
        result = 31 * result + maxHeight.hashCode()
        result = 31 * result + enforceIncoming.hashCode()
        return result
    }
}

private class SizeNode(
    var minWidth: Dp,
    var minHeight: Dp,
    var maxWidth: Dp,
    var maxHeight: Dp,
    var enforceIncoming: Boolean,
) : LayoutModifierNode, Modifier.Node() {

    private val Density.targetConstraints: Constraints
        get() {
            val maxWidth =
                if (maxWidth.isSpecified) {
                    maxWidth.roundToPx().fastCoerceAtLeast(0)
                } else {
                    Constraints.Infinity
                }
            val maxHeight =
                if (maxHeight.isSpecified) {
                    maxHeight.roundToPx().fastCoerceAtLeast(0)
                } else {
                    Constraints.Infinity
                }
            val minWidth =
                if (minWidth.isSpecified) {
                    minWidth.roundToPx().fastCoerceIn(0, maxWidth).let {
                        if (it != Constraints.Infinity) it else 0
                    }
                } else {
                    0
                }
            val minHeight =
                if (minHeight.isSpecified) {
                    minHeight.roundToPx().fastCoerceIn(0, maxHeight).let {
                        if (it != Constraints.Infinity) it else 0
                    }
                } else {
                    0
                }
            return Constraints(
                minWidth = minWidth,
                minHeight = minHeight,
                maxWidth = maxWidth,
                maxHeight = maxHeight,
            )
        }

    override fun MeasureScope.measure(
        measurable: Measurable,
        constraints: Constraints,
    ): MeasureResult {
        val wrappedConstraints =
            targetConstraints.let { targetConstraints ->
                if (enforceIncoming) {
                    constraints.constrain(targetConstraints)
                } else {
                    val resolvedMinWidth =
                        if (minWidth.isSpecified) targetConstraints.minWidth
                        else constraints.minWidth.coerceAtMost(targetConstraints.maxWidth)
                    val resolvedMaxWidth =
                        if (maxWidth.isSpecified) targetConstraints.maxWidth
                        else constraints.maxWidth.coerceAtLeast(targetConstraints.minWidth)
                    val resolvedMinHeight =
                        if (minHeight.isSpecified) targetConstraints.minHeight
                        else constraints.minHeight.coerceAtMost(targetConstraints.maxHeight)
                    val resolvedMaxHeight =
                        if (maxHeight.isSpecified) targetConstraints.maxHeight
                        else constraints.maxHeight.coerceAtLeast(targetConstraints.minHeight)
                    Constraints(resolvedMinWidth, resolvedMaxWidth, resolvedMinHeight, resolvedMaxHeight)
                }
            }
        val placeable = measurable.measure(wrappedConstraints)
        return layout(placeable.width, placeable.height) { placeable.place(0, 0) }
    }

    override fun IntrinsicMeasureScope.minIntrinsicWidth(measurable: Measurable, height: Int): Int {
        val constraints = targetConstraints
        return if (constraints.hasFixedWidth) {
            constraints.maxWidth
        } else {
            val childHeight = if (enforceIncoming) height else constraints.constrainHeight(height)
            constraints.constrainWidth(measurable.minIntrinsicWidth(childHeight))
        }
    }

    override fun IntrinsicMeasureScope.minIntrinsicHeight(measurable: Measurable, width: Int): Int {
        val constraints = targetConstraints
        return if (constraints.hasFixedHeight) {
            constraints.maxHeight
        } else {
            val childWidth = if (enforceIncoming) width else constraints.constrainWidth(width)
            constraints.constrainHeight(measurable.minIntrinsicHeight(childWidth))
        }
    }

    override fun IntrinsicMeasureScope.maxIntrinsicWidth(measurable: Measurable, height: Int): Int {
        val constraints = targetConstraints
        return if (constraints.hasFixedWidth) {
            constraints.maxWidth
        } else {
            val childHeight = if (enforceIncoming) height else constraints.constrainHeight(height)
            constraints.constrainWidth(measurable.maxIntrinsicWidth(childHeight))
        }
    }

    override fun IntrinsicMeasureScope.maxIntrinsicHeight(measurable: Measurable, width: Int): Int {
        val constraints = targetConstraints
        return if (constraints.hasFixedHeight) {
            constraints.maxHeight
        } else {
            val childWidth = if (enforceIncoming) width else constraints.constrainWidth(width)
            constraints.constrainHeight(measurable.maxIntrinsicHeight(childWidth))
        }
    }
}

private class FillElement(
    private val direction: Direction,
    private val fraction: Float,
) : ModifierNodeElement<FillNode>() {
    override fun create(): FillNode = FillNode(direction, fraction)
    override fun update(node: FillNode) {
        node.direction = direction
        node.fraction = fraction
    }
    override fun InspectorInfo.inspectableProperties() {}
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is FillElement) return false
        if (direction != other.direction) return false
        if (fraction != other.fraction) return false
        return true
    }
    override fun hashCode(): Int {
        var result = direction.hashCode()
        result = 31 * result + fraction.hashCode()
        return result
    }
}

private class FillNode(
    var direction: Direction,
    var fraction: Float
) : LayoutModifierNode, Modifier.Node() {
    override fun MeasureScope.measure(measurable: Measurable, constraints: Constraints): MeasureResult {
        val minWidth: Int
        val maxWidth: Int
        if (constraints.hasBoundedWidth && direction != Direction.Vertical) {
            val width = (constraints.maxWidth * fraction).roundToInt().coerceIn(constraints.minWidth, constraints.maxWidth)
            minWidth = width
            maxWidth = width
        } else {
            minWidth = constraints.minWidth
            maxWidth = constraints.maxWidth
        }
        val minHeight: Int
        val maxHeight: Int
        if (constraints.hasBoundedHeight && direction != Direction.Horizontal) {
            val height = (constraints.maxHeight * fraction).roundToInt().coerceIn(constraints.minHeight, constraints.maxHeight)
            minHeight = height
            maxHeight = height
        } else {
            minHeight = constraints.minHeight
            maxHeight = constraints.maxHeight
        }
        val placeable = measurable.measure(Constraints(minWidth, maxWidth, minHeight, maxHeight))
        return layout(placeable.width, placeable.height) { placeable.place(0, 0) }
    }
    override fun IntrinsicMeasureScope.minIntrinsicWidth(m: Measurable, h: Int) = if (direction != Direction.Vertical) 0 else m.minIntrinsicWidth(h)
    override fun IntrinsicMeasureScope.maxIntrinsicWidth(m: Measurable, h: Int) = if (direction != Direction.Vertical) 0 else m.maxIntrinsicWidth(h)
    override fun IntrinsicMeasureScope.minIntrinsicHeight(m: Measurable, w: Int) = if (direction != Direction.Horizontal) 0 else m.minIntrinsicHeight(w)
    override fun IntrinsicMeasureScope.maxIntrinsicHeight(m: Measurable, w: Int) = if (direction != Direction.Horizontal) 0 else m.maxIntrinsicHeight(w)
}

private class WrapContentElement(
    private val direction: Direction,
    private val unbounded: Boolean,
    private val alignmentCallback: (IntSize, LayoutDirection) -> IntOffset,
) : ModifierNodeElement<WrapContentNode>() {
    override fun create() = WrapContentNode(direction, unbounded, alignmentCallback)
    override fun update(node: WrapContentNode) {
        node.direction = direction
        node.unbounded = unbounded
        node.alignmentCallback = alignmentCallback
    }
    override fun InspectorInfo.inspectableProperties() {}
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is WrapContentElement) return false
        if (direction != other.direction) return false
        if (unbounded != other.unbounded) return false
        return true
    }
    override fun hashCode(): Int {
        var result = direction.hashCode()
        result = 31 * result + unbounded.hashCode()
        return result
    }
}

private class WrapContentNode(
    var direction: Direction,
    var unbounded: Boolean,
    var alignmentCallback: (IntSize, LayoutDirection) -> IntOffset,
) : LayoutModifierNode, Modifier.Node() {
    override fun MeasureScope.measure(measurable: Measurable, constraints: Constraints): MeasureResult {
        val wrappedConstraints = Constraints(
            minWidth = if (direction != Direction.Vertical) 0 else constraints.minWidth,
            minHeight = if (direction != Direction.Horizontal) 0 else constraints.minHeight,
            maxWidth = if (direction != Direction.Vertical && unbounded) Constraints.Infinity else constraints.maxWidth,
            maxHeight = if (direction != Direction.Horizontal && unbounded) Constraints.Infinity else constraints.maxHeight
        )
        val placeable = measurable.measure(wrappedConstraints)
        val wrapperWidth = placeable.width.coerceIn(constraints.minWidth, constraints.maxWidth)
        val wrapperHeight = placeable.height.coerceIn(constraints.minHeight, constraints.maxHeight)

        return layout(wrapperWidth, wrapperHeight) {
            val position = alignmentCallback(
                IntSize(wrapperWidth - placeable.width, wrapperHeight - placeable.height),
                layoutDirection
            )
            placeable.place(position)
        }
    }
    override fun IntrinsicMeasureScope.minIntrinsicWidth(m: Measurable, h: Int) = m.minIntrinsicWidth(h)
    override fun IntrinsicMeasureScope.maxIntrinsicWidth(m: Measurable, h: Int) = m.maxIntrinsicWidth(h)
    override fun IntrinsicMeasureScope.minIntrinsicHeight(m: Measurable, w: Int) = m.minIntrinsicHeight(w)
    override fun IntrinsicMeasureScope.maxIntrinsicHeight(m: Measurable, w: Int) = m.maxIntrinsicHeight(w)
}