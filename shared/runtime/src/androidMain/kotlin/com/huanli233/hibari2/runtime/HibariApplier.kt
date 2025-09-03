package com.huanli233.hibari2.runtime

import android.util.Log
import android.view.View
import android.view.ViewGroup
import androidx.compose.runtime.Applier
import com.huanli233.hibari2.runtime.composable.ComposeInteroperabilityViewGroup
import com.huanli233.hibari2.runtime.layout.MeasurePolicy
import com.huanli233.hibari2.runtime.layout.MeasureScope
import com.huanli233.hibari2.runtime.layout.Measurable
import com.huanli233.hibari2.runtime.node.HibariNode
import com.huanli233.hibari2.runtime.node.LayoutNode
import com.huanli233.hibari2.runtime.node.ViewNode
import com.huanli233.hibari2.runtime.utils.move
import androidx.compose.ui.unit.Constraints

class HibariApplier(private val rootViewGroup: ViewGroup) : Applier<HibariNode> {
    internal val rootNode = LayoutNode(object : MeasurePolicy {
        override fun MeasureScope.measure(measurables: List<Measurable>, constraints: Constraints): com.huanli233.hibari2.runtime.layout.MeasureResult {
            var width = constraints.minWidth
            var height = constraints.minHeight
            val placeables = measurables.map {
                val placeable = (it as com.huanli233.hibari2.runtime.node.ChildMeasurable).measure(constraints)
                width = kotlin.math.max(width, placeable.width)
                height = kotlin.math.max(height, placeable.height)
                placeable
            }
            return layout(width, height) {
                measurables.zip(placeables).forEach { (node, placeable) ->
                    placeable.place(0, 0)
                }
            }
        }
    }).apply { parent = null }

    override val current: HibariNode get() = nodeStack.lastOrNull() ?: rootNode
    private val nodeStack = mutableListOf<HibariNode>()

    private fun findClosestViewGroupContainer(): ViewGroup {
        for (i in nodeStack.lastIndex downTo 0) {
            val node = nodeStack[i]
            if (node is ViewNode<*> && node.view is ViewGroup) {
                return node.view
            }
        }
        return rootViewGroup
    }

    override fun down(node: HibariNode) {
        nodeStack.add(node)
    }

    override fun up() {
        nodeStack.removeAt(nodeStack.lastIndex)
    }

    override fun insertTopDown(index: Int, instance: HibariNode) {}

    override fun insertBottomUp(index: Int, instance: HibariNode) {
        val parent = current
        parent.children.add(index, instance)
        instance.parent = parent

        if (instance is ViewNode<*>) {
            if (instance.view.parent == null) {
                val container = findClosestViewGroupContainer()
                val realIndex = parent.children.subList(0, index).count { it is ViewNode<*> }
                container.addView(instance.view, realIndex)
            }
        } else if (instance is LayoutNode) {
            val parentNode = instance.parent
            if (parentNode is ViewNode<*> && parentNode.view is ComposeInteroperabilityViewGroup) {
                parentNode.view.hostedNode = instance
            }
        }

        rootViewGroup.post { rootViewGroup.requestLayout() }
    }

    override fun remove(index: Int, count: Int) {
        val parent = current
        val sublist = parent.children.subList(index, index + count)
        sublist.forEach { node ->
            node.parent = null
            node.onDisposed()
        }
        sublist.clear()
        rootViewGroup.post { rootViewGroup.requestLayout() }
    }

    override fun move(from: Int, to: Int, count: Int) {
        val parent = current
        val container = findClosestViewGroupContainer()

        val viewsToMove = parent.children.subList(from, from + count)
            .filterIsInstance<ViewNode<*>>()
            .map { it.view }

        parent.children.move(from, to, count)

        val newViewOrder = parent.children.filterIsInstance<ViewNode<*>>().map { it.view }

        viewsToMove.forEach { view ->
            if (view.parent == container) {
                container.removeView(view)
            }
        }

        viewsToMove.forEach { view ->
            val newIndex = newViewOrder.indexOf(view)
            if (newIndex != -1) {
                container.addView(view, newIndex)
            }
        }

        rootViewGroup.post { rootViewGroup.requestLayout() }
    }

    override fun clear() {
        rootNode.children.forEach { it.onDisposed() }
        rootNode.children.clear()
        rootViewGroup.removeAllViews()
    }
}