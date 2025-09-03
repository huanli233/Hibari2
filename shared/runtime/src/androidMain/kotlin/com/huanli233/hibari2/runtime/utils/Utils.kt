package com.huanli233.hibari2.runtime.utils

import android.view.View
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import com.huanli233.hibari2.runtime.layout.*
import com.huanli233.hibari2.runtime.modifier.LayoutModifierNode

internal fun createConstraintsFromMeasureSpecs(widthMeasureSpec: Int, heightMeasureSpec: Int): Constraints {
    val widthMode = View.MeasureSpec.getMode(widthMeasureSpec)
    val widthSize = View.MeasureSpec.getSize(widthMeasureSpec)
    val heightMode = View.MeasureSpec.getMode(heightMeasureSpec)
    val heightSize = View.MeasureSpec.getSize(heightMeasureSpec)
    val minWidth: Int
    val maxWidth: Int
    when (widthMode) {
        View.MeasureSpec.EXACTLY -> { minWidth = widthSize; maxWidth = widthSize }
        View.MeasureSpec.AT_MOST -> { minWidth = 0; maxWidth = widthSize }
        else -> { minWidth = 0; maxWidth = Constraints.Infinity }
    }
    val minHeight: Int
    val maxHeight: Int
    when (heightMode) {
        View.MeasureSpec.EXACTLY -> { minHeight = heightSize; maxHeight = heightSize }
        View.MeasureSpec.AT_MOST -> { minHeight = 0; maxHeight = heightSize }
        else -> { minHeight = 0; maxHeight = Constraints.Infinity }
    }
    return Constraints(minWidth, maxWidth, minHeight, maxHeight)
}

internal fun createMeasureSpecsFromConstraints(constraints: Constraints): Pair<Int, Int> {
    val widthMeasureSpec = when {
        constraints.hasFixedWidth -> View.MeasureSpec.makeMeasureSpec(constraints.minWidth, View.MeasureSpec.EXACTLY)
        constraints.hasBoundedWidth -> View.MeasureSpec.makeMeasureSpec(constraints.maxWidth, View.MeasureSpec.AT_MOST)
        else -> View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
    }
    val heightMeasureSpec = when {
        constraints.hasFixedHeight -> View.MeasureSpec.makeMeasureSpec(constraints.minHeight, View.MeasureSpec.EXACTLY)
        constraints.hasBoundedHeight -> View.MeasureSpec.makeMeasureSpec(constraints.maxHeight, View.MeasureSpec.AT_MOST)
        else -> View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
    }
    return Pair(widthMeasureSpec, heightMeasureSpec)
}

internal fun <T> MutableList<T>.move(from: Int, to: Int, count: Int) {
    val fromIndex = from
    val toIndex = if (from > to) to else to - count
    if (fromIndex == toIndex) return
    val sublist = subList(fromIndex, fromIndex + count)
    val moving = ArrayList(sublist)
    sublist.clear()
    addAll(toIndex, moving)
}

internal object NodeMeasuringIntrinsics {
    fun minWidth(node: LayoutModifierNode, measurable: Measurable, height: Int): Int {
        val constraints = Constraints(maxHeight = height)
        val scope = object : MeasureScope {
            override val density: Float = 1f
            override val fontScale: Float = 1f
            override val isLookingAhead: Boolean
                get() = false
            override val layoutDirection: LayoutDirection = LayoutDirection.Ltr
            override fun layout(width: Int, height: Int, placementBlock: Placeable.PlacementScope.() -> Unit): MeasureResult {
                return object : MeasureResult {
                    override val width: Int = width
                    override val height: Int = height
                    override fun placeChildren(scope: Placeable.PlacementScope) {}
                }
            }
        }
        val measurableWrapper = DefaultIntrinsicMeasurable(measurable, IntrinsicMinMax.Min, IntrinsicWidthHeight.Width)
        // This call is now deprecated and will not work with new modifiers.
        // val result = with(node) { scope.measure(measurableWrapper, constraints) }
        // This should be replaced with a call that provides an IntrinsicMeasureScope.
        // For now, returning 0 to satisfy compilation for old code paths.
        return 0
    }

    fun minHeight(node: LayoutModifierNode, measurable: Measurable, width: Int): Int {
        val constraints = Constraints(maxWidth = width)
        val scope = object : MeasureScope {
            override val density: Float = 1f
            override val fontScale: Float = 1f
            override val isLookingAhead: Boolean
                get() = false
            override val layoutDirection: LayoutDirection = LayoutDirection.Ltr
            override fun layout(width: Int, height: Int, placementBlock: Placeable.PlacementScope.() -> Unit): MeasureResult {
                return object : MeasureResult {
                    override val width: Int = width
                    override val height: Int = height
                    override fun placeChildren(scope: Placeable.PlacementScope) {}
                }
            }
        }
        val measurableWrapper = DefaultIntrinsicMeasurable(measurable, IntrinsicMinMax.Min, IntrinsicWidthHeight.Height)
        return 0
    }

    fun maxWidth(node: LayoutModifierNode, measurable: Measurable, height: Int): Int {
        val constraints = Constraints(maxHeight = height)
        val scope = object : MeasureScope {
            override val density: Float = 1f
            override val fontScale: Float = 1f
            override val isLookingAhead: Boolean
                get() = false
            override val layoutDirection: LayoutDirection = LayoutDirection.Ltr
            override fun layout(width: Int, height: Int, placementBlock: Placeable.PlacementScope.() -> Unit): MeasureResult {
                return object : MeasureResult {
                    override val width: Int = width
                    override val height: Int = height
                    override fun placeChildren(scope: Placeable.PlacementScope) {}
                }
            }
        }
        val measurableWrapper = DefaultIntrinsicMeasurable(measurable, IntrinsicMinMax.Max, IntrinsicWidthHeight.Width)
        return 0
    }

    fun maxHeight(node: LayoutModifierNode, measurable: Measurable, width: Int): Int {
        val constraints = Constraints(maxWidth = width)
        val scope = object : MeasureScope {
            override val density: Float = 1f
            override val fontScale: Float = 1f
            override val layoutDirection: LayoutDirection = LayoutDirection.Ltr
            override val isLookingAhead: Boolean
                get() = false
            override fun layout(width: Int, height: Int, placementBlock: Placeable.PlacementScope.() -> Unit): MeasureResult {
                return object : MeasureResult {
                    override val width: Int = width
                    override val height: Int = height
                    override fun placeChildren(scope: Placeable.PlacementScope) {}
                }
            }
        }
        val measurableWrapper = DefaultIntrinsicMeasurable(measurable, IntrinsicMinMax.Max, IntrinsicWidthHeight.Height)
        return 0
    }

    private class DefaultIntrinsicMeasurable(
        val measurable: Measurable,
        val minMax: IntrinsicMinMax,
        val widthHeight: IntrinsicWidthHeight
    ) : Measurable {
        override val parentData: Any? get() = measurable.parentData

        // This method also needs Density to be correct
        override fun measure(constraints: Constraints): Placeable {
            val density = Density(1f, 1f)
            return measure(constraints, density)
        }

        fun measure(constraints: Constraints, density: Density): Placeable {
            val (w, h) = if (widthHeight == IntrinsicWidthHeight.Width) {
                val width = if (minMax == IntrinsicMinMax.Max) {
                    measurable.maxIntrinsicWidth(constraints.maxHeight)
                } else {
                    measurable.minIntrinsicWidth(constraints.maxHeight)
                }
                width to (constraints.maxHeight.takeIf { it != Constraints.Infinity } ?: 0)
            } else { // Height
                val height = if (minMax == IntrinsicMinMax.Max) {
                    measurable.maxIntrinsicHeight(constraints.maxWidth)
                } else {
                    measurable.minIntrinsicHeight(constraints.maxWidth)
                }
                (constraints.maxWidth.takeIf { it != Constraints.Infinity } ?: 0) to height
            }
            return EmptyPlaceable(w, h)
        }

        // These methods now require density
        override fun minIntrinsicWidth(height: Int): Int = 0 // Needs density
        override fun maxIntrinsicWidth(height: Int): Int = 0 // Needs density
        override fun minIntrinsicHeight(width: Int): Int = 0 // Needs density
        override fun maxIntrinsicHeight(width: Int): Int = 0 // Needs density

        // Implement methods with density
        fun minIntrinsicWidth(height: Int, density: Density): Int = measurable.minIntrinsicWidth(height)
        fun maxIntrinsicWidth(height: Int, density: Density): Int = measurable.maxIntrinsicWidth(height)
        fun minIntrinsicHeight(width: Int, density: Density): Int = measurable.minIntrinsicHeight(width)
        fun maxIntrinsicHeight(width: Int, density: Density): Int = measurable.maxIntrinsicHeight(width)
    }

    private class EmptyPlaceable(width: Int, height: Int) : Placeable() {
        init {
            measuredSize = IntSize(width, height)
        }
        override fun placeAt(position: IntOffset, zIndex: Float, layerBlock: ((androidx.compose.ui.graphics.GraphicsLayerScope) -> Unit)?) {}
    }

    private enum class IntrinsicMinMax { Min, Max }
    private enum class IntrinsicWidthHeight { Width, Height }
}