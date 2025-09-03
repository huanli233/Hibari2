package com.huanli233.hibari2.runtime

import android.content.Context
import android.util.AttributeSet
import android.view.ViewGroup
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Composition
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Recomposer
import androidx.compose.ui.platform.AndroidUiDispatcher
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import com.huanli233.hibari2.runtime.layout.MeasureAndLayoutDelegate
import com.huanli233.hibari2.runtime.node.HibariNode
import com.huanli233.hibari2.runtime.node.LayoutNode
import com.huanli233.hibari2.runtime.utils.createConstraintsFromMeasureSpecs
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

class ComposeHibariView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : ViewGroup(context, attrs, defStyleAttr) {
    private val applier = HibariApplier(this)
    private val rootNode: LayoutNode get() = applier.rootNode
    private var content: (@Composable () -> Unit)? = null
    private var composition: Composition? = null
    private var recomposer: Recomposer? = null
    private var recomposerScope: CoroutineScope? = null

    internal val measureAndLayoutDelegate = MeasureAndLayoutDelegate(rootNode)

    private val density = Density(context)

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val constraints = createConstraintsFromMeasureSpecs(widthMeasureSpec, heightMeasureSpec)

        if (rootNode.owner == null) {
            rootNode.attach(this)
        }

        val placeable = rootNode.measure(constraints, density)

        setMeasuredDimension(placeable.width, placeable.height)

//        measureAndLayoutDelegate.measureAndLayout(constraints)
//        val placeable = rootNode.placeable
//        if (placeable != null) {
//            setMeasuredDimension(placeable.width, placeable.height)
//        } else {
//            val lookaheadPlaceable = rootNode.lookaheadDelegate
//            if (lookaheadPlaceable != null) {
//                setMeasuredDimension(lookaheadPlaceable.width, lookaheadPlaceable.height)
//            } else {
//                super.onMeasure(widthMeasureSpec, heightMeasureSpec)
//            }
//        }
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        rootNode.placeAt(0, 0)
    }

    fun setContent(content: @Composable () -> Unit) {
        val finalContent = @Composable {
            CompositionLocalProvider(
                LocalContext provides context,
                LocalView provides this,
                LocalLayoutDirection provides when (layoutDirection) {
                    LAYOUT_DIRECTION_LTR -> LayoutDirection.Ltr
                    LAYOUT_DIRECTION_RTL -> LayoutDirection.Rtl
                    else -> LayoutDirection.Ltr
                },
                LocalDensity provides density,
            ) {
                content()
            }
        }
        this.content = finalContent
        composition?.setContent(finalContent)

        rootNode.onInvalidate = {
            requestLayout()
        }
        rootNode.requestLookaheadRemeasure()
        rootNode.requestRemeasure()
        requestLayout()
    }

    private fun createComposition() {
        if (composition == null && isAttachedToWindow) {
            recomposer = Recomposer(AndroidUiDispatcher.CurrentThread)
            composition = Composition(applier, recomposer!!).also { composition ->
                content?.let { composition.setContent(it) }
            }
            recomposerScope = CoroutineScope(AndroidUiDispatcher.CurrentThread)
            recomposerScope?.launch { recomposer?.runRecomposeAndApplyChanges() }
        }
    }

    private fun disposeComposition() {
        composition?.dispose()
        recomposerScope?.cancel()
        composition = null
        recomposerScope = null
        recomposer = null
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        createComposition()
        (rootNode as LayoutNode).attach(this)
    }

    override fun onDetachedFromWindow() {
        disposeComposition()
        (rootNode as LayoutNode).detach()
        super.onDetachedFromWindow()
    }
}