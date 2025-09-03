package com.huanli233.hibari2.runtime.modifier

import androidx.annotation.VisibleForTesting
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.DefaultCameraDistance
import androidx.compose.ui.graphics.DefaultShadowColor
import androidx.compose.ui.graphics.GraphicsLayerScope
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.RenderEffect
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.node.ModifierNodeElement
import androidx.compose.ui.platform.InspectorInfo
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import com.huanli233.hibari2.runtime.layout.Measurable
import com.huanli233.hibari2.runtime.layout.MeasureResult
import com.huanli233.hibari2.runtime.layout.MeasureScope
import com.huanli233.hibari2.runtime.node.HibariNode

data class GraphicsLayerData(
    var scaleX: Float = 1f,
    var scaleY: Float = 1f,
    var alpha: Float = 1f,
    var translationX: Float = 0f,
    var translationY: Float = 0f,
    var shadowElevation: Float = 0f,
    var rotationX: Float = 0f,
    var rotationY: Float = 0f,
    var rotationZ: Float = 0f,
    var cameraDistance: Float = 8f,
    var clip: Boolean = false
) {
    fun reset() {
        scaleX = 1f
        scaleY = 1f
        alpha = 1f
        translationX = 0f
        translationY = 0f
        shadowElevation = 0f
        rotationX = 0f
        rotationY = 0f
        rotationZ = 0f
        cameraDistance = 8f
        clip = false
    }

    fun combineWith(childData: GraphicsLayerData): GraphicsLayerData {
        return GraphicsLayerData(
            scaleX = this.scaleX * childData.scaleX,
            scaleY = this.scaleY * childData.scaleY,
            alpha = this.alpha * childData.alpha,
            translationX = this.translationX + childData.translationX,
            translationY = this.translationY + childData.translationY,
            shadowElevation = this.shadowElevation + childData.shadowElevation,
            rotationX = this.rotationX + childData.rotationX,
            rotationY = this.rotationY + childData.rotationY,
            rotationZ = this.rotationZ + childData.rotationZ,
            cameraDistance = childData.cameraDistance,
            clip = this.clip || childData.clip
        )
    }
}

internal data class GraphicsLayerModifier(
    val layerBlock: GraphicsLayerScope.() -> Unit
) : ModifierNodeElement<GraphicsLayerModifierNode>() {

    override fun create(): GraphicsLayerModifierNode {
        return GraphicsLayerModifierNode(layerBlock)
    }

    override fun update(node: GraphicsLayerModifierNode) {
        node.layerBlock = layerBlock
    }

    override fun InspectorInfo.inspectableProperties() {
        name = "graphicsLayer"
        properties["block"] = layerBlock
    }
}

internal class GraphicsLayerModifierNode(
    var layerBlock: GraphicsLayerScope.() -> Unit
) : Modifier.Node()

fun Modifier.graphicsLayer(
    block: GraphicsLayerScope.() -> Unit
): Modifier = this.then(SimpleGraphicsLayerModifier(block))

interface HibariModifier {
    fun applyToNode(node: HibariNode) {}
}

class SimpleGraphicsLayerModifier(
    val layerBlock: GraphicsLayerScope.() -> Unit
) : Modifier.Element, HibariModifier {

    private val scope = ReusableGraphicsLayerScope()

    override fun applyToNode(node: HibariNode) {
        scope.reset()
        scope.apply {
            scaleX = node.graphicsLayerData.scaleX
            scaleY = node.graphicsLayerData.scaleY
            alpha = node.graphicsLayerData.alpha
            translationX = node.graphicsLayerData.translationX
            translationY = node.graphicsLayerData.translationY
            shadowElevation = node.graphicsLayerData.shadowElevation
            rotationX = node.graphicsLayerData.rotationX
            rotationY = node.graphicsLayerData.rotationY
            rotationZ = node.graphicsLayerData.rotationZ
            cameraDistance = node.graphicsLayerData.cameraDistance
            clip = node.graphicsLayerData.clip
        }
        layerBlock(scope)
        node.graphicsLayerData.apply {
            scaleX = scope.scaleX
            scaleY = scope.scaleY
            alpha = scope.alpha
            translationX = scope.translationX
            translationY = scope.translationY
            shadowElevation = scope.shadowElevation
            rotationX = scope.rotationX
            rotationY = scope.rotationY
            rotationZ = scope.rotationZ
            cameraDistance = scope.cameraDistance
            clip = scope.clip
        }
    }
}

internal class ReusableGraphicsLayerScope : GraphicsLayerScope {
    internal var mutatedFields: Int = 0

    override var scaleX: Float = 1f
        set(value) {
            if (field != value) {
                mutatedFields = mutatedFields or Fields.ScaleX
                field = value
            }
        }

    override var scaleY: Float = 1f
        set(value) {
            if (field != value) {
                mutatedFields = mutatedFields or Fields.ScaleY
                field = value
            }
        }

    override var alpha: Float = 1f
        set(value) {
            if (field != value) {
                mutatedFields = mutatedFields or Fields.Alpha
                field = value
            }
        }

    override var translationX: Float = 0f
        set(value) {
            if (field != value) {
                mutatedFields = mutatedFields or Fields.TranslationX
                field = value
            }
        }

    override var translationY: Float = 0f
        set(value) {
            if (field != value) {
                mutatedFields = mutatedFields or Fields.TranslationY
                field = value
            }
        }

    override var shadowElevation: Float = 0f
        set(value) {
            if (field != value) {
                mutatedFields = mutatedFields or Fields.ShadowElevation
                field = value
            }
        }

    override var ambientShadowColor: Color = DefaultShadowColor
        set(value) {
            if (field != value) {
                mutatedFields = mutatedFields or Fields.AmbientShadowColor
                field = value
            }
        }

    override var spotShadowColor: Color = DefaultShadowColor
        set(value) {
            if (field != value) {
                mutatedFields = mutatedFields or Fields.SpotShadowColor
                field = value
            }
        }

    override var rotationX: Float = 0f
        set(value) {
            if (field != value) {
                mutatedFields = mutatedFields or Fields.RotationX
                field = value
            }
        }

    override var rotationY: Float = 0f
        set(value) {
            if (field != value) {
                mutatedFields = mutatedFields or Fields.RotationY
                field = value
            }
        }

    override var rotationZ: Float = 0f
        set(value) {
            if (field != value) {
                mutatedFields = mutatedFields or Fields.RotationZ
                field = value
            }
        }

    override var cameraDistance: Float = DefaultCameraDistance
        set(value) {
            if (field != value) {
                mutatedFields = mutatedFields or Fields.CameraDistance
                field = value
            }
        }

    override var transformOrigin: TransformOrigin = TransformOrigin.Center
        set(value) {
            if (field != value) {
                mutatedFields = mutatedFields or Fields.TransformOrigin
                field = value
            }
        }

    override var shape: Shape = RectangleShape
        set(value) {
            if (field != value) {
                mutatedFields = mutatedFields or Fields.Shape
                field = value
            }
        }

    override var clip: Boolean = false
        set(value) {
            if (field != value) {
                mutatedFields = mutatedFields or Fields.Clip
                field = value
            }
        }

    override var compositingStrategy: CompositingStrategy = CompositingStrategy.Auto
        set(value) {
            if (field != value) {
                mutatedFields = mutatedFields or Fields.CompositingStrategy
                field = value
            }
        }

    override var size: Size = Size.Unspecified

    internal var graphicsDensity: Density = Density(1.0f)

    internal var layoutDirection: LayoutDirection = LayoutDirection.Ltr

    override val density: Float
        get() = graphicsDensity.density

    override val fontScale: Float
        get() = graphicsDensity.fontScale

    override var renderEffect: RenderEffect? = null
        set(value) {
            if (field != value) {
                mutatedFields = mutatedFields or Fields.RenderEffect
                field = value
            }
        }

    override var colorFilter: ColorFilter? = null
        set(value) {
            if (field != value) {
                mutatedFields = mutatedFields or Fields.ColorFilter
                field = value
            }
        }

    override var blendMode: BlendMode = BlendMode.SrcOver
        set(value) {
            if (field != value) {
                mutatedFields = mutatedFields or Fields.BlendMode
                field = value
            }
        }

    internal var outline: Outline? = null
        @VisibleForTesting internal set

    fun reset() {
        scaleX = 1f
        scaleY = 1f
        alpha = 1f
        translationX = 0f
        translationY = 0f
        shadowElevation = 0f
        ambientShadowColor = DefaultShadowColor
        spotShadowColor = DefaultShadowColor
        rotationX = 0f
        rotationY = 0f
        rotationZ = 0f
        cameraDistance = DefaultCameraDistance
        transformOrigin = TransformOrigin.Center
        shape = RectangleShape
        clip = false
        renderEffect = null
        colorFilter = null
        blendMode = BlendMode.SrcOver
        compositingStrategy = CompositingStrategy.Auto
        size = Size.Unspecified
        outline = null
        // mutatedFields should be reset last as all the setters above modify it.
        mutatedFields = 0
    }

    internal fun updateOutline() {
        outline = shape.createOutline(size, layoutDirection, graphicsDensity)
    }
}

internal object Fields {
    const val ScaleX: Int = 0b1 shl 0
    const val ScaleY: Int = 0b1 shl 1
    const val Alpha: Int = 0b1 shl 2
    const val TranslationX: Int = 0b1 shl 3
    const val TranslationY: Int = 0b1 shl 4
    const val ShadowElevation: Int = 0b1 shl 5
    const val AmbientShadowColor: Int = 0b1 shl 6
    const val SpotShadowColor: Int = 0b1 shl 7
    const val RotationX: Int = 0b1 shl 8
    const val RotationY: Int = 0b1 shl 9
    const val RotationZ: Int = 0b1 shl 10
    const val CameraDistance: Int = 0b1 shl 11
    const val TransformOrigin: Int = 0b1 shl 12
    const val Shape: Int = 0b1 shl 13
    const val Clip: Int = 0b1 shl 14
    const val CompositingStrategy: Int = 0b1 shl 15
    const val RenderEffect: Int = 0b1 shl 17
    const val ColorFilter: Int = 0b1 shl 18
    const val BlendMode: Int = 0b1 shl 19

    const val MatrixAffectingFields =
        ScaleX or
                ScaleY or
                TranslationX or
                TranslationY or
                TransformOrigin or
                RotationX or
                RotationY or
                RotationZ or
                CameraDistance
}