package com.huanli233.hibari2.runtime.composable

import android.content.Context
import android.view.View
import android.view.ViewGroup
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ComposeNode
import androidx.compose.runtime.Updater
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Density
import com.huanli233.hibari2.runtime.HibariApplier
import com.huanli233.hibari2.runtime.layout.MeasurePolicy
import com.huanli233.hibari2.runtime.modifier.requiresViewInstance
import com.huanli233.hibari2.runtime.node.HibariNode
import com.huanli233.hibari2.runtime.node.LayoutNode
import com.huanli233.hibari2.runtime.node.ViewNode
import com.huanli233.hibari2.runtime.utils.createConstraintsFromMeasureSpecs

@Composable
fun ComposeHibariNode(factory: () -> HibariNode, update: Updater<HibariNode>.() -> Unit, content: @Composable () -> Unit = {}) {
    ComposeNode<HibariNode, HibariApplier>(factory = factory, update = { update(this) }) {
        content()
    }
}

class ComposeInteroperabilityViewGroup(context: Context) : ViewGroup(context) {
    internal var hostedNode: HibariNode? = null
    private val density by lazy { Density(context) }
    private var lastMeasuredWidth: Int = -1
    private var lastMeasuredHeight: Int = -1

    private fun specToString(measureSpec: Int): String {
        val mode = when (MeasureSpec.getMode(measureSpec)) {
            MeasureSpec.EXACTLY -> "EXACTLY"
            MeasureSpec.AT_MOST -> "AT_MOST"
            else -> "UNSPECIFIED"
        }
        return "$mode ${MeasureSpec.getSize(measureSpec)}"
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        if (hostedNode == null) {
            setMeasuredDimension(0, 0)
            return
        }

        val constraints = createConstraintsFromMeasureSpecs(widthMeasureSpec, heightMeasureSpec)
        val placeable = hostedNode?.measure(constraints, density)

        val measuredWidth = placeable?.width ?: 0
        val measuredHeight = placeable?.height ?: 0
        setMeasuredDimension(measuredWidth, measuredHeight)

        if (measuredWidth != lastMeasuredWidth || measuredHeight != lastMeasuredHeight) {
            requestLayout()
            lastMeasuredWidth = measuredWidth
            lastMeasuredHeight = measuredHeight
        }
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        if (hostedNode == null) {
            return
        }
        hostedNode?.placeAt(0, 0)
    }
}

@Composable
fun HibariLayout(modifier: Modifier = Modifier, measurePolicy: MeasurePolicy, content: @Composable () -> Unit) {
    val needsViewWrapper = modifier.requiresViewInstance()
    if (needsViewWrapper) {
        val context = LocalContext.current
        val wrapperView = remember { ComposeInteroperabilityViewGroup(context) }
        ComposeHibariNode(factory = {
            ViewNode(wrapperView, modifier)
        }, update = { set(modifier) { applyModifier(modifier) } }) {
            ComposeHibariNode(factory = {
                LayoutNode(measurePolicy)
            }, update = {}, content = content)
        }
    } else {
        ComposeHibariNode(factory = {
            LayoutNode(measurePolicy, modifier)
        }, update = { set(modifier) { applyModifier(modifier) } }, content = content)
    }
}

@Composable
fun <T : View> HibariView(
    factory: (Context) -> T,
    modifier: Modifier = Modifier,
    update: (T) -> Unit = {},
) {
    val context = LocalContext.current
    val view = remember { factory(context) }
    @Suppress("UNCHECKED_CAST")
    ComposeHibariNode(factory = { ViewNode(view) }, update = { set(update) { ((this as ViewNode<*>).view as? T)?.let { p1 -> update(p1 ) } } })
}

@Composable
fun <T : ViewGroup> AndroidViewGroup(
    factory: (Context) -> T,
    modifier: Modifier = Modifier,
    hostComposableContent: Boolean = false,
    update: (T) -> Unit = {},
    content: @Composable () -> Unit = {}
) {
    if (hostComposableContent) {
        val context = LocalContext.current
        val viewGroup = remember { factory(context) }
        val interopView = remember {
            ComposeInteroperabilityViewGroup(context).apply {
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
            }
        }

        ComposeHibariNode(
            factory = { ViewNode(viewGroup) },
            update = {
                set(modifier) { this.applyModifier(modifier) }
                set(update) { update(viewGroup) }
            }
        ) {
            ComposeHibariNode(
                factory = { ViewNode(interopView) },
                update = { }
            ) {
                content()
            }
        }
    } else {
        val context = LocalContext.current
        val viewGroup = remember { factory(context) }

        ComposeHibariNode(
            factory = { ViewNode(viewGroup) },
            update = {
                set(modifier) { this.applyModifier(modifier) }
                set(update) { update(viewGroup) }
            }
        ) {
            content()
        }
    }
}

@Composable
fun ComposableContentHost(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    val interopView = remember { ComposeInteroperabilityViewGroup(context) }

    ComposeHibariNode(
        factory = { ViewNode(interopView) },
        update = {
            set(modifier) { this.applyModifier(modifier) }
        }
    ) {
        content()
    }
}