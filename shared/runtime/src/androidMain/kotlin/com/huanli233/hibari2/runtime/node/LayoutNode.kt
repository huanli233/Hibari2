package com.huanli233.hibari2.runtime.node

import androidx.compose.ui.Modifier
import androidx.compose.ui.node.ModifierNodeElement
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import com.huanli233.hibari2.runtime.ComposeHibariView
import com.huanli233.hibari2.runtime.layout.MeasurePolicy
import com.huanli233.hibari2.runtime.layout.MeasureResult
import com.huanli233.hibari2.runtime.layout.MeasureScope
import com.huanli233.hibari2.runtime.layout.Measurable
import com.huanli233.hibari2.runtime.layout.Placeable
import com.huanli233.hibari2.runtime.modifier.GraphicsLayerData
import com.huanli233.hibari2.runtime.modifier.LayoutModifierNode
import com.huanli233.hibari2.runtime.modifier.ParentDataModifierNode

class LayoutNode(
    var measurePolicy: MeasurePolicy,
    override var modifier: Modifier = Modifier,
    var isLookaheadRoot: Boolean = false
) : HibariNode {
    enum class LayoutState { Idle, LookaheadMeasuring, Measuring, LookaheadLayingOut, LayingOut }


    override var parent: HibariNode? = null
        set(value) {
            if (field != value) {
                field = value
                updateDepth((value as? LayoutNode)?.depth ?: -1)
            }
        }

    var depth: Int = 0
        private set

    private fun updateDepth(parentDepth: Int) {
        val newDepth = parentDepth + 1
        if (depth != newDepth) {
            depth = newDepth
            children.forEach { child ->
                (child as? LayoutNode)?.updateDepth(newDepth)
            }
        }
    }
    override val children: MutableList<HibariNode> = mutableListOf()
    override val graphicsLayerData = GraphicsLayerData()

    private var _owner: ComposeHibariView? = null
    val owner: ComposeHibariView? get() = _owner
    private fun findOwner(): ComposeHibariView? {
        var p = parent
        while (p != null) {
            if (p is LayoutNode && p._owner != null) return p._owner
            p = p.parent
        }
        return null
    }

    fun attach(owner: ComposeHibariView) {
        _owner = owner
        if (lookaheadRoot != null) {
            lookaheadDelegate = LookaheadDelegate(this)
        }
        children.forEach { if (it is LayoutNode) it.attach(owner) }
    }

    fun detach() {
        owner?.measureAndLayoutDelegate?.onNodeDetached(this)
        _owner = null
        children.forEach { if (it is LayoutNode) it.detach() }
    }

    var isPlaced: Boolean = false
    var isPlacedInLookahead: Boolean = false

    var lookaheadMeasurePending = false
    var measurePending = false
    var lookaheadLayoutPending = false
    var layoutPending = false

    var layoutState: LayoutState = LayoutState.Idle
    val isAttached: Boolean get() = owner != null

    private var _lookaheadRoot: LayoutNode? = null
    val lookaheadRoot: LayoutNode?
        get() {
            if (isLookaheadRoot) return this
            if (_lookaheadRoot == null) {
                var p = parent
                while (p != null) {
                    if (p is LayoutNode) {
                        if (p.isLookaheadRoot) {
                            _lookaheadRoot = p
                            return p
                        }
                        if (p._lookaheadRoot != null) {
                            _lookaheadRoot = p._lookaheadRoot
                            return p._lookaheadRoot
                        }
                    }
                    p = p.parent
                }
            }
            return _lookaheadRoot
        }

    var lookaheadDelegate: LookaheadDelegate? = null
    var placeable: Placeable? = null
    lateinit var density: Density
    var layoutDirection: LayoutDirection = LayoutDirection.Ltr

    internal var modifierNodes: List<Modifier.Node> = emptyList()

    private val measureScopeImpl = object : MeasureScope {
        override var isLookingAhead: Boolean = false
        override var density: Float = 1f
        override var fontScale: Float = 1f
        override val layoutDirection: LayoutDirection
            get() = this@LayoutNode.layoutDirection
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

    init {
        populateModifierNodes(modifier)
    }

    override fun measure(constraints: Constraints, density: Density): Placeable {
        this.density = density
        this.isPlaced = false
        val result = measurePass(constraints, isLookingAhead = false)
        placeable = object : Placeable() {
            init { measuredSize = IntSize(result.width, result.height) }
            override fun placeAt(
                position: IntOffset,
                zIndex: Float,
                layerBlock: ((androidx.compose.ui.graphics.GraphicsLayerScope) -> Unit)?
            ) {
                this@LayoutNode.isPlaced = true
                val scope = PlacementScope(position, width, layoutDirection)
                result.placeChildren(scope)
            }
        }
        return placeable!!
    }

    internal fun measurePass(constraints: Constraints, isLookingAhead: Boolean): MeasureResult {
        measureScopeImpl.density = density.density
        measureScopeImpl.fontScale = density.fontScale
        measureScopeImpl.isLookingAhead = isLookingAhead
        val measurables = children.map { child -> ChildMeasurable(child, density, isLookingAhead) }
        val measureResult = with(measurePolicy) {
            measureScopeImpl.measure(measurables, constraints)
        }
        val measurable = object : Measurable {
            override val parentData: Any? get() = null
            override fun measure(constraints: Constraints): Placeable {
                return object : Placeable() {
                    init { measuredSize = IntSize(measureResult.width, measureResult.height) }
                    override fun placeAt(
                        position: IntOffset,
                        zIndex: Float,
                        layerBlock: ((androidx.compose.ui.graphics.GraphicsLayerScope) -> Unit)?
                    ) {
                        val scope = PlacementScope(position, width, layoutDirection)
                        measureResult.placeChildren(scope)
                    }
                }
            }
            override fun minIntrinsicWidth(height: Int): Int = this@LayoutNode.minIntrinsicWidth(height, density)
            override fun maxIntrinsicWidth(height: Int): Int = this@LayoutNode.maxIntrinsicWidth(height, density)
            override fun minIntrinsicHeight(width: Int): Int = this@LayoutNode.minIntrinsicHeight(width, density)
            override fun maxIntrinsicHeight(width: Int): Int = this@LayoutNode.maxIntrinsicHeight(width, density)
        }
        val layoutModifiers = modifierNodes.filterIsInstance<LayoutModifierNode>()
        val finalMeasurable = layoutModifiers.fold(measurable as Measurable) { m, modifierNode ->
            LayoutModifierMeasurable(modifierNode, m, measureScopeImpl)
        }
        val finalPlaceable = finalMeasurable.measure(constraints)
        return object : MeasureResult {
            override val width: Int = finalPlaceable.width
            override val height: Int = finalPlaceable.height
            override fun placeChildren(scope: Placeable.PlacementScope) {
                with(scope) {
                    finalPlaceable.place(IntOffset.Zero, 0f)
                }
            }
        }
    }

    fun lookaheadRemeasure(constraints: Constraints): Boolean {
        if (lookaheadDelegate == null) return false
        val oldSize = lookaheadDelegate?.size
        layoutState = LayoutState.LookaheadMeasuring
        isPlacedInLookahead = false
        lookaheadDelegate!!.performMeasure(constraints)
        layoutState = LayoutState.Idle
        lookaheadMeasurePending = false
        return oldSize != lookaheadDelegate?.size
    }

    fun remeasure(constraints: Constraints): Boolean {
        val oldSize = placeable?.measuredSize
        layoutState = LayoutState.Measuring
        measure(constraints, this.density)
        layoutState = LayoutState.Idle
        measurePending = false
        return oldSize != placeable?.measuredSize
    }

    fun lookaheadReplace() {
        if (lookaheadDelegate == null) return
        layoutState = LayoutState.LookaheadLayingOut
        lookaheadDelegate!!.placeChildren()
        layoutState = LayoutState.Idle
        lookaheadLayoutPending = false
    }

    fun replace() {
        if (placeable == null) return
        layoutState = LayoutState.LayingOut
        placeable!!.placeAt(IntOffset.Zero, 0f, null)
        layoutState = LayoutState.Idle
        layoutPending = false
    }

    fun requestRemeasure() {
        owner?.measureAndLayoutDelegate?.requestRemeasure(this)
    }

    fun requestRelayout() {
        owner?.measureAndLayoutDelegate?.requestRelayout(this)
    }

    fun requestLookaheadRemeasure() {
        if (lookaheadRoot == null) {
            requestRemeasure()
        } else {
            owner?.measureAndLayoutDelegate?.requestLookaheadRemeasure(this)
        }
    }

    fun requestLookaheadRelayout() {
        if (lookaheadRoot == null) {
            requestRelayout()
        } else {
            owner?.measureAndLayoutDelegate?.requestLookaheadRelayout(this)
        }
    }

    override fun placeAt(x: Int, y: Int, zIndex: Float) {
        placeable?.placeAt(IntOffset(x, y), zIndex, null)
    }

    private fun populateModifierNodes(newModifier: Modifier) {
        this.graphicsLayerData.reset()
        val newNodes = newModifier.foldIn(mutableListOf<Modifier.Node>()) { acc, element ->
            if (element is ModifierNodeElement<*>) {
                acc.add(element.create())
            } else if (element is Modifier.Node) {
                acc.add(element)
            }
            acc
        }
        modifierNodes = newNodes
    }

    override fun applyModifier(newModifier: Modifier) {
        modifier = newModifier
        populateModifierNodes(newModifier)
        updateGraphics()
        requestRemeasure()
    }

    override fun updateGraphics() {
        children.forEach { it.updateGraphics() }
    }

    override var onInvalidate: (() -> Unit)? = null
        set(value) {
            field = value
            value?.invoke()
        }

    override fun onDisposed() {
        detach()
        children.forEach { it.onDisposed() }
    }

    override fun minIntrinsicWidth(height: Int, density: Density): Int { val measurables = children.map { child -> ChildMeasurable(child, density, false) }; return measurePolicy.minIntrinsicWidth(measurables, height) }
    override fun maxIntrinsicWidth(height: Int, density: Density): Int { val measurables = children.map { child -> ChildMeasurable(child, density, false) }; return measurePolicy.maxIntrinsicWidth(measurables, height) }
    override fun minIntrinsicHeight(width: Int, density: Density): Int { val measurables = children.map { child -> ChildMeasurable(child, density, false) }; return measurePolicy.minIntrinsicHeight(measurables, width) }
    override fun maxIntrinsicHeight(width: Int, density: Density): Int { val measurables = children.map { child -> ChildMeasurable(child, density, false) }; return measurePolicy.maxIntrinsicHeight(measurables, width) }
}

internal class LayoutModifierMeasurable(
    private val modifier: LayoutModifierNode,
    private val inner: Measurable,
    private val scope: MeasureScope
) : Measurable {
    override val parentData: Any? get() = inner.parentData
    override fun measure(constraints: Constraints): Placeable {
        val result = with(modifier) { scope.measure(inner, constraints) }
        return object : Placeable() {
            init {
                measuredSize = IntSize(result.width, result.height)
                measurementConstraints = constraints
            }
            override fun placeAt(position: IntOffset, zIndex: Float, layerBlock: ((androidx.compose.ui.graphics.GraphicsLayerScope) -> Unit)?) {
                val scope = PlacementScope(parentPosition = position, parentWidth = result.width, parentLayoutDirection = scope.layoutDirection)
                result.placeChildren(scope)
            }
        }
    }
    override fun minIntrinsicWidth(height: Int): Int { val intrinsicScope = object : com.huanli233.hibari2.runtime.layout.IntrinsicMeasureScope, Density by scope { override val layoutDirection: LayoutDirection get() = scope.layoutDirection }; return with(modifier) { intrinsicScope.minIntrinsicWidth(inner, height) } }
    override fun maxIntrinsicWidth(height: Int): Int { val intrinsicScope = object : com.huanli233.hibari2.runtime.layout.IntrinsicMeasureScope, Density by scope { override val layoutDirection: LayoutDirection get() = scope.layoutDirection }; return with(modifier) { intrinsicScope.maxIntrinsicWidth(inner, height) } }
    override fun minIntrinsicHeight(width: Int): Int { val intrinsicScope = object : com.huanli233.hibari2.runtime.layout.IntrinsicMeasureScope, Density by scope { override val layoutDirection: LayoutDirection get() = scope.layoutDirection }; return with(modifier) { intrinsicScope.minIntrinsicHeight(inner, width) } }
    override fun maxIntrinsicHeight(width: Int): Int { val intrinsicScope = object : com.huanli233.hibari2.runtime.layout.IntrinsicMeasureScope, Density by scope { override val layoutDirection: LayoutDirection get() = scope.layoutDirection }; return with(modifier) { intrinsicScope.maxIntrinsicHeight(inner, width) } }
}

class ChildMeasurable(private val child: HibariNode, private val density: Density, private val isLookingAhead: Boolean) : Measurable {
    override val parentData: Any? by lazy(LazyThreadSafetyMode.NONE) {
        val nodes = when (child) {
            is LayoutNode -> child.modifierNodes
            is ViewNode<*> -> child.modifierNodes
            else -> emptyList()
        }
        nodes.filterIsInstance<ParentDataModifierNode>().fold(null as Any?) { current, modifier ->
            modifier.modifyParentData(current)
        }
    }

    override fun measure(constraints: Constraints): Placeable {
        if (isLookingAhead) {
            val layoutNode = child as? LayoutNode
            return layoutNode?.lookaheadDelegate?.performMeasure(constraints)
                ?: child.measure(constraints, density)
        }
        return child.measure(constraints, density)
    }

    override fun minIntrinsicWidth(height: Int): Int = child.minIntrinsicWidth(height, density)
    override fun maxIntrinsicWidth(height: Int): Int = child.maxIntrinsicWidth(height, density)
    override fun minIntrinsicHeight(width: Int): Int = child.minIntrinsicHeight(width, density)
    override fun maxIntrinsicHeight(width: Int): Int = child.maxIntrinsicHeight(width, density)
}