package com.huanli233.hibari2.runtime.layout

import androidx.compose.ui.unit.Constraints
import com.huanli233.hibari2.runtime.node.LayoutNode
import java.util.TreeSet

internal class MeasureAndLayoutDelegate(private val root: LayoutNode) {
    private val lookaheadRelayoutNodes = TreeSet<LayoutNode>(compareBy { it.depth })
    private val relayoutNodes = TreeSet<LayoutNode>(compareBy { it.depth })

    private var duringMeasureLayout = false

    fun requestLookaheadRemeasure(node: LayoutNode) {
        if (duringMeasureLayout) return
        if (node.isAttached && !node.lookaheadMeasurePending) {
            node.lookaheadMeasurePending = true
            lookaheadRelayoutNodes.add(node)
        }
    }

    fun requestRemeasure(node: LayoutNode) {
        if (duringMeasureLayout) return
        if (node.isAttached && !node.measurePending) {
            node.measurePending = true
            relayoutNodes.add(node)
        }
    }

    fun requestLookaheadRelayout(node: LayoutNode) {
        if (duringMeasureLayout) return
        if (node.isAttached && !node.lookaheadLayoutPending) {
            node.lookaheadLayoutPending = true
            lookaheadRelayoutNodes.add(node)
        }
    }

    fun requestRelayout(node: LayoutNode) {
        if (duringMeasureLayout) return
        if (node.isAttached && !node.layoutPending) {
            node.layoutPending = true
            relayoutNodes.add(node)
        }
    }

    fun measureAndLayout(constraints: Constraints) {
        if (duringMeasureLayout) return
        duringMeasureLayout = true

        try {
            while (lookaheadRelayoutNodes.isNotEmpty()) {
                val node = lookaheadRelayoutNodes.pollFirst()!!
                if (node.isAttached) {
                    if (node.lookaheadMeasurePending) {
                        node.lookaheadRemeasure(constraints)
                    }
                    if (node.lookaheadLayoutPending) {
                        node.lookaheadReplace()
                    }
                }
            }

            while (relayoutNodes.isNotEmpty()) {
                val node = relayoutNodes.pollFirst()!!
                if (node.isAttached) {
                    if (node.measurePending) {
                        if (node === root) {
                            node.remeasure(constraints)
                        } else {
                            node.requestRemeasure()
                        }
                    }
                    if (node.layoutPending) {
                        if (node === root) {
                            node.replace()
                        } else {
                            node.requestRelayout()
                        }
                    }
                }
            }

            if (root.measurePending) {
                root.remeasure(constraints)
            }
            if (root.layoutPending) {
                root.replace()
            }

        } finally {
            duringMeasureLayout = false
        }
    }

    fun onNodeDetached(node: LayoutNode) {
        lookaheadRelayoutNodes.remove(node)
        relayoutNodes.remove(node)
    }
}