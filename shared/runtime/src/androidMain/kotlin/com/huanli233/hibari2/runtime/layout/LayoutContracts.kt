package com.huanli233.hibari2.runtime.layout

import androidx.compose.ui.graphics.GraphicsLayerScope
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection

interface Measurable {
    val parentData: Any?
    fun measure(constraints: Constraints): Placeable
    fun minIntrinsicWidth(height: Int): Int
    fun maxIntrinsicWidth(height: Int): Int
    fun minIntrinsicHeight(width: Int): Int
    fun maxIntrinsicHeight(width: Int): Int
}

fun interface MeasurePolicy {
    fun MeasureScope.measure(measurables: List<Measurable>, constraints: Constraints): MeasureResult

    fun minIntrinsicWidth(measurables: List<Measurable>, height: Int): Int {
        val mapped = measurables.map { it.minIntrinsicWidth(height) }
        return mapped.maxOrNull() ?: 0
    }

    fun minIntrinsicHeight(measurables: List<Measurable>, width: Int): Int {
        val mapped = measurables.map { it.minIntrinsicHeight(width) }
        return mapped.maxOrNull() ?: 0
    }

    fun maxIntrinsicWidth(measurables: List<Measurable>, height: Int): Int {
        val mapped = measurables.map { it.maxIntrinsicWidth(height) }
        return mapped.maxOrNull() ?: 0
    }

    fun maxIntrinsicHeight(measurables: List<Measurable>, width: Int): Int {
        val mapped = measurables.map { it.maxIntrinsicHeight(width) }
        return mapped.maxOrNull() ?: 0
    }
}

interface IntrinsicMeasureScope : Density {
    val layoutDirection: LayoutDirection
}

interface MeasureScope : Density {
    val isLookingAhead: Boolean
    val layoutDirection: LayoutDirection

    fun layout(
        width: Int,
        height: Int,
        placementBlock: Placeable.PlacementScope.() -> Unit
    ): MeasureResult
}

interface MeasureResult {
    val width: Int
    val height: Int
    fun placeChildren(scope: Placeable.PlacementScope)
}

abstract class Placeable {
    var width: Int = 0
        private set
    var height: Int = 0
        private set

    var measuredSize: IntSize = IntSize(0, 0)
        set(value) {
            if (field != value) {
                field = value
                width = measuredSize.width
                height = measuredSize.height
            }
        }

    protected var measurementConstraints: Constraints = Constraints()

    abstract fun placeAt(
        position: IntOffset,
        zIndex: Float,
        layerBlock: (GraphicsLayerScope.() -> Unit)?
    )

    open class PlacementScope internal constructor(
        val parentPosition: IntOffset,
        val parentWidth: Int,
        val parentLayoutDirection: LayoutDirection
    ) {

        fun Placeable.place(x: Int, y: Int, zIndex: Float = 0f) =
            place(IntOffset(x, y), zIndex)

        fun Placeable.place(position: IntOffset, zIndex: Float = 0f) {
            val finalPosition = parentPosition + position
            placeAt(finalPosition, zIndex, null)
        }

        fun Placeable.placeWithLayer(
            x: Int,
            y: Int,
            zIndex: Float = 0f,
            layerBlock: GraphicsLayerScope.() -> Unit = {}
        ) = placeWithLayer(IntOffset(x, y), zIndex, layerBlock)

        fun Placeable.placeWithLayer(
            position: IntOffset,
            zIndex: Float = 0f,
            layerBlock: GraphicsLayerScope.() -> Unit = {}
        ) {
            placeAt(parentPosition + position, zIndex, layerBlock)
        }

        fun Placeable.placeRelative(x: Int, y: Int, zIndex: Float = 0f) =
            placeRelative(IntOffset(x, y), zIndex)

        fun Placeable.placeRelative(position: IntOffset, zIndex: Float = 0f) {
            val adjustedPosition = if (parentLayoutDirection == LayoutDirection.Ltr || parentWidth == 0) {
                position
            } else {
                IntOffset(parentWidth - width - position.x, position.y)
            }
            placeAt(parentPosition + adjustedPosition, zIndex, null)
        }

        fun Placeable.placeRelativeWithLayer(
            position: IntOffset,
            zIndex: Float = 0f,
            layerBlock: GraphicsLayerScope.() -> Unit = {}
        ) {
            val adjustedPosition = if (parentLayoutDirection == LayoutDirection.Ltr || parentWidth == 0) {
                position
            } else {
                IntOffset(parentWidth - width - position.x, position.y)
            }
            placeAt(parentPosition + adjustedPosition, zIndex, layerBlock)
        }
    }
}