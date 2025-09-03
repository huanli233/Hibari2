package com.huanli233.hibari2.runtime.node

import android.util.Log
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.Modifier
import androidx.compose.ui.node.ModifierNodeElement
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import com.huanli233.hibari2.runtime.layout.Measurable
import com.huanli233.hibari2.runtime.layout.MeasureResult
import com.huanli233.hibari2.runtime.layout.MeasureScope
import com.huanli233.hibari2.runtime.layout.Placeable
import com.huanli233.hibari2.runtime.modifier.GraphicsLayerData
import com.huanli233.hibari2.runtime.modifier.HibariModifier
import com.huanli233.hibari2.runtime.modifier.LayoutModifierNode
import com.huanli233.hibari2.runtime.modifier.ViewAttributeModifier
import com.huanli233.hibari2.runtime.utils.createMeasureSpecsFromConstraints
import kotlin.reflect.KClass

class ViewNode<T : View>(
    val view: T,
    override var modifier: Modifier = Modifier
) : HibariNode {
    companion object {
        private var nextId = 0
        private fun specToString(measureSpec: Int): String {
            val mode = when (View.MeasureSpec.getMode(measureSpec)) {
                View.MeasureSpec.EXACTLY -> "EXACTLY"
                View.MeasureSpec.AT_MOST -> "AT_MOST"
                else -> "UNSPECIFIED"
            }
            return "$mode ${View.MeasureSpec.getSize(measureSpec)}"
        }
    }
    private val nodeId = nextId++
    override fun toString(): String = "ViewNode(id=$nodeId, view=${view::class.java.simpleName})"

    override var parent: HibariNode? = null
    override val children: MutableList<HibariNode> = mutableListOf()
    override val graphicsLayerData = GraphicsLayerData()
    private var placeable: Placeable? = null
    private var appliedViewModifier: Modifier = Modifier
    internal var modifierNodes: List<Modifier.Node> = emptyList()
        private set

    private val measureScopeImpl by lazy {
        object : MeasureScope {
            override var density: Float = 1f
            override var fontScale: Float = 1f
            override var isLookingAhead: Boolean = false
            override val layoutDirection: LayoutDirection
                get() = when (view.layoutDirection) {
                    android.view.View.LAYOUT_DIRECTION_RTL -> LayoutDirection.Rtl
                    else -> LayoutDirection.Ltr
                }

            override fun layout(width: Int, height: Int, placementBlock: Placeable.PlacementScope.() -> Unit): MeasureResult {
                return object : MeasureResult {
                    override val width: Int = width
                    override val height: Int = height
                    override fun placeChildren(scope: Placeable.PlacementScope) {
                        scope.placementBlock()
                    }
                }
            }
        }
    }

    init {
        populateModifierNodes(modifier)
    }

    private fun populateModifierNodes(newModifier: Modifier) {
        modifierNodes = newModifier.foldIn(mutableListOf()) { acc, element ->
            if (element is HibariModifier) {
                element.applyToNode(this)
            }
            if (element is ModifierNodeElement<*>) acc.add(element.create())
            else if (element is Modifier.Node) acc.add(element)
            acc
        }
    }

    override fun measure(constraints: Constraints, density: Density): Placeable {
        measureScopeImpl.density = density.density
        measureScopeImpl.fontScale = density.fontScale
        measureScopeImpl.isLookingAhead = false

        val viewMeasurable = object : Measurable {
            override val parentData: Any? = null
            override fun measure(constraints: Constraints): Placeable {
                val (widthMeasureSpec, heightMeasureSpec) = createMeasureSpecsFromConstraints(constraints)

                view.measure(widthMeasureSpec, heightMeasureSpec)

                return object : Placeable() {
                    init {
                        measuredSize = IntSize(view.measuredWidth, view.measuredHeight)
                    }

                    override fun placeAt(
                        position: IntOffset,
                        zIndex: Float,
                        layerBlock: ((androidx.compose.ui.graphics.GraphicsLayerScope) -> Unit)?
                    ) {
                        val right = position.x + width
                        val bottom = position.y + height
                        view.layout(position.x, position.y, right, bottom)
                        updateGraphics()
                    }
                }
            }
            override fun minIntrinsicWidth(height: Int): Int = 0
            override fun maxIntrinsicWidth(height: Int): Int = 0
            override fun minIntrinsicHeight(width: Int): Int = 0
            override fun maxIntrinsicHeight(width: Int): Int = 0
        }

        val layoutModifiers = modifierNodes.filterIsInstance<LayoutModifierNode>()
        val finalMeasurable = layoutModifiers.fold(viewMeasurable as Measurable) { m, modifierNode ->
            LayoutModifierMeasurable(modifierNode, m, measureScopeImpl)
        }

        placeable = finalMeasurable.measure(constraints)
        return placeable!!
    }

    fun applyGraphicsLayer(view: View) {
        var effectiveData = this.graphicsLayerData
        var p = this.parent
        while (p != null) {
            effectiveData = p.graphicsLayerData.combineWith(effectiveData)
            p = p.parent
        }
        view.alpha = effectiveData.alpha
        view.scaleX = effectiveData.scaleX
        view.scaleY = effectiveData.scaleY
        view.translationX = effectiveData.translationX
        view.translationY = effectiveData.translationY
        view.elevation = effectiveData.shadowElevation
        view.rotation = effectiveData.rotationZ
        view.rotationX = effectiveData.rotationX
        view.rotationY = effectiveData.rotationY
        view.cameraDistance = effectiveData.cameraDistance * view.resources.displayMetrics.density
        view.clipToOutline = effectiveData.clip
    }

    override fun placeAt(x: Int, y: Int, zIndex: Float) {
        placeable?.placeAt(IntOffset(x, y), zIndex, null)
    }

    override fun applyModifier(newModifier: Modifier) {
        modifier = newModifier
        val oldModifierNodes = modifierNodes
        populateModifierNodes(newModifier)
        applyViewAttributeModifiers(newModifier)
        updateGraphics()
        if ((modifierNodes - oldModifierNodes).any { it is LayoutModifierNode }) {
            view.requestLayout()
        } else {
            view.invalidate()
        }
    }

    override fun updateGraphics() {
        applyGraphicsLayer(view)
        children.forEach { it.updateGraphics() }
    }

    override var onInvalidate: (() -> Unit)? = null

    override fun minIntrinsicWidth(height: Int, density: Density): Int = 0
    override fun maxIntrinsicWidth(height: Int, density: Density): Int = 0
    override fun minIntrinsicHeight(width: Int, density: Density): Int = 0
    override fun maxIntrinsicHeight(width: Int, density: Density): Int = 0

    @Suppress("UNCHECKED_CAST")
    private fun applyViewAttributeModifiers(newModifier: Modifier) {
        if (newModifier == appliedViewModifier) return
        val oldModifiers = appliedViewModifier.foldIn(mutableMapOf<KClass<*>, ViewAttributeModifier<*>>()) { map, element -> if (element is ViewAttributeModifier<*>) map[element.key] = element; map }
        val newModifiers = newModifier.foldIn(mutableMapOf<KClass<*>, ViewAttributeModifier<*>>()) { map, element -> if (element is ViewAttributeModifier<*>) map[element.key] = element; map }
        newModifiers.forEach { (key, newMod) -> val oldMod = oldModifiers[key]; if (oldMod == null || oldMod != newMod) { (newMod as ViewAttributeModifier<T>).apply(view) } }
        oldModifiers.forEach { (key, oldMod) -> if (!newModifiers.containsKey(key)) { (oldMod as ViewAttributeModifier<T>).reset(view) } }
        appliedViewModifier = newModifier
    }

    override fun onDisposed() {
        val parentVg = view.parent as? ViewGroup
        parentVg?.removeView(view)
        children.forEach { it.onDisposed() }
    }
}