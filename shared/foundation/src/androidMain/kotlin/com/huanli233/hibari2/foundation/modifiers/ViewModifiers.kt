package com.huanli233.hibari2.foundation.modifiers

import android.annotation.SuppressLint
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.drawable.Drawable
import android.os.Build
import android.view.MotionEvent
import android.view.View
import android.view.ViewOutlineProvider
import androidx.annotation.DrawableRes
import androidx.annotation.IdRes
import androidx.annotation.RequiresApi
import androidx.compose.runtime.snapshots.Snapshot
import androidx.compose.ui.Modifier
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.huanli233.hibari2.runtime.modifier.HibariModifier
import com.huanli233.hibari2.runtime.modifier.ViewAttributeModifier
import com.huanli233.hibari2.runtime.node.HibariNode
import kotlin.reflect.KClass

data class InnerPaddingModifier(
    val left: Int, val top: Int, val right: Int, val bottom: Int
) : ViewAttributeModifier<View> {
    override fun apply(view: View) { view.setPadding(left, top, right, bottom) }
    override fun reset(view: View) { view.setPadding(0, 0, 0, 0) }
}

data class MinWidthModifier(val minWidth: Int) : ViewAttributeModifier<View> {
    override fun apply(view: View) { view.minimumWidth = minWidth }
    override fun reset(view: View) { view.minimumWidth = 0 }
}

data class MinHeightModifier(val minHeight: Int) : ViewAttributeModifier<View> {
    override fun apply(view: View) { view.minimumHeight = minHeight }
    override fun reset(view: View) { view.minimumHeight = 0 }
}

data class ElevationModifier(val elevation: Float) : ViewAttributeModifier<View> {
    override fun apply(view: View) { view.elevation = elevation }
    override fun reset(view: View) { view.elevation = 0f }
}

data class TranslationModifier(val x: Float, val y: Float, val z: Float = 0f) : ViewAttributeModifier<View> {
    override fun apply(view: View) {
        view.translationX = x
        view.translationY = y
        view.translationZ = z
    }
    override fun reset(view: View) {
        view.translationX = 0f
        view.translationY = 0f
        view.translationZ = 0f
    }
}

data class AlphaModifier(val alpha: Float) : Modifier.Element, HibariModifier {
    override fun applyToNode(node: HibariNode) {
        node.graphicsLayerData.alpha = alpha
    }
}

data class ScaleModifier(val scaleX: Float, val scaleY: Float) : Modifier.Element, HibariModifier {
    override fun applyToNode(node: HibariNode) {
        node.graphicsLayerData.scaleX = scaleX
        node.graphicsLayerData.scaleY= scaleY
    }
}

data class RotationModifier(val rotation: Float, val rotationX: Float = 0f, val rotationY: Float = 0f) : ViewAttributeModifier<View> {
    override fun apply(view: View) {
        view.rotation = rotation
        view.rotationX = rotationX
        view.rotationY = rotationY
    }
    override fun reset(view: View) {
        view.rotation = 0f
        view.rotationX = 0f
        view.rotationY = 0f
    }
}

data class PivotModifier(val pivotX: Float, val pivotY: Float) : ViewAttributeModifier<View> {
    override fun apply(view: View) {
        view.pivotX = pivotX
        view.pivotY = pivotY
    }
    override fun reset(view: View) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            view.resetPivot()
        }
    }
}

data class ClickableModifier(val enabled: Boolean, val onClick: (() -> Unit)?) : ViewAttributeModifier<View> {
    override fun apply(view: View) {
        view.isClickable = enabled
        view.setOnClickListener(if (onClick != null) { { Snapshot.withMutableSnapshot {
            onClick()
        } } } else null)
    }
    override fun reset(view: View) {
        view.isClickable = false
        view.setOnClickListener(null)
    }
}

data class LongClickableModifier(val enabled: Boolean, val onLongClick: (() -> Boolean)?) : ViewAttributeModifier<View> {
    override fun apply(view: View) {
        view.isLongClickable = enabled
        view.setOnLongClickListener(if (onLongClick != null) { { Snapshot.withMutableSnapshot {
            onLongClick()
        } } } else null)
    }
    override fun reset(view: View) {
        view.isLongClickable = false
        view.setOnLongClickListener(null)
    }
}

data class OnTouchModifier(val onTouch: (view: View, event: MotionEvent) -> Boolean) : ViewAttributeModifier<View> {
    @SuppressLint("ClickableViewAccessibility")
    override fun apply(view: View) { view.setOnTouchListener { view, event ->
        Snapshot.withMutableSnapshot {
            onTouch(view, event)
        }
    } }
    override fun reset(view: View) { view.setOnTouchListener(null) }
}

data class EnabledModifier(val enabled: Boolean) : ViewAttributeModifier<View> {
    override fun apply(view: View) { view.isEnabled = enabled }
    override fun reset(view: View) { view.isEnabled = true }
}

data class FocusableModifier(val focusable: Boolean, val focusableInTouchMode: Boolean = false) : ViewAttributeModifier<View> {
    override fun apply(view: View) {
        view.isFocusable = focusable
        view.isFocusableInTouchMode = focusableInTouchMode
    }
    override fun reset(view: View) {
        view.isFocusable = false
        view.isFocusableInTouchMode = false
    }
}

data class OnFocusChangedModifier(val onFocusChanged: (hasFocus: Boolean) -> Unit) : ViewAttributeModifier<View> {
    override fun apply(view: View) { view.setOnFocusChangeListener { _, hasFocus -> Snapshot.withMutableSnapshot { onFocusChanged(hasFocus) } } }
    override fun reset(view: View) { view.setOnFocusChangeListener(null) }
}

data class HapticFeedbackModifier(val enabled: Boolean) : ViewAttributeModifier<View> {
    override fun apply(view: View) { view.isHapticFeedbackEnabled = enabled }
    override fun reset(view: View) { view.isHapticFeedbackEnabled = true }
}

data class SoundEffectsModifier(val enabled: Boolean) : ViewAttributeModifier<View> {
    override fun apply(view: View) { view.isSoundEffectsEnabled = enabled }
    override fun reset(view: View) { view.isSoundEffectsEnabled = true }
}

data class VisibilityModifier(val visibility: Int) : ViewAttributeModifier<View> {
    override fun apply(view: View) { view.visibility = visibility }
    override fun reset(view: View) { view.visibility = View.VISIBLE }
}

data class SelectedModifier(val selected: Boolean) : ViewAttributeModifier<View> {
    override fun apply(view: View) { view.isSelected = selected }
    override fun reset(view: View) { view.isSelected = false }
}

data class ActivatedModifier(val activated: Boolean) : ViewAttributeModifier<View> {
    override fun apply(view: View) { view.isActivated = activated }
    override fun reset(view: View) { view.isActivated = false }
}

data class PressedModifier(val pressed: Boolean) : ViewAttributeModifier<View> {
    override fun apply(view: View) { view.isPressed = pressed }
    override fun reset(view: View) { view.isPressed = false }
}

data class KeepScreenOnModifier(val keepScreenOn: Boolean) : ViewAttributeModifier<View> {
    override fun apply(view: View) { view.keepScreenOn = keepScreenOn }
    override fun reset(view: View) { view.keepScreenOn = false }
}

data class BackgroundResourceModifier(@DrawableRes val resId: Int) : ViewAttributeModifier<View> {
    override fun apply(view: View) { view.setBackgroundResource(resId) }
    override fun reset(view: View) { view.background = null }
}

data class BackgroundTintModifier(val tint: ColorStateList?, val mode: PorterDuff.Mode? = null) : ViewAttributeModifier<View> {
    override fun apply(view: View) {
        view.backgroundTintList = tint
        mode?.let { view.backgroundTintMode = it }
    }
    override fun reset(view: View) {
        view.backgroundTintList = null
        view.backgroundTintMode = PorterDuff.Mode.SRC_IN
    }
}

@RequiresApi(Build.VERSION_CODES.M)
data class ForegroundModifier(val foreground: Drawable?) : ViewAttributeModifier<View> {
    override fun apply(view: View) { view.foreground = foreground }
    override fun reset(view: View) { view.foreground = null }
}

@RequiresApi(Build.VERSION_CODES.M)
data class ForegroundTintModifier(val tint: ColorStateList?, val mode: PorterDuff.Mode? = null) : ViewAttributeModifier<View> {
    override fun apply(view: View) {
        view.foregroundTintList = tint
        mode?.let { view.foregroundTintMode = it }
    }
    override fun reset(view: View) {
        view.foregroundTintList = null
        view.foregroundTintMode = PorterDuff.Mode.SRC_IN
    }
}

data class ContentDescriptionModifier(val contentDescription: CharSequence?) : ViewAttributeModifier<View> {
    override fun apply(view: View) { view.contentDescription = contentDescription }
    override fun reset(view: View) { view.contentDescription = null }
}

data class AccessibilityModifier(
    val importantForAccessibility: Int? = null,
    val isAccessibilityHeading: Boolean? = null,
    val accessibilityLiveRegion: Int? = null
) : ViewAttributeModifier<View> {
    override fun apply(view: View) {
        importantForAccessibility?.let { view.importantForAccessibility = it }
        isAccessibilityHeading?.let {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                view.isAccessibilityHeading = it
            }
        }
        accessibilityLiveRegion?.let { view.accessibilityLiveRegion = it }
    }
    override fun reset(view: View) {
        view.importantForAccessibility = View.IMPORTANT_FOR_ACCESSIBILITY_AUTO
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            view.isAccessibilityHeading = false
        }
        view.accessibilityLiveRegion = View.ACCESSIBILITY_LIVE_REGION_NONE
    }
}

data class TooltipModifier(val tooltipText: CharSequence?) : ViewAttributeModifier<View> {
    override fun apply(view: View) { ViewCompat.setTooltipText(view, tooltipText) }
    override fun reset(view: View) { ViewCompat.setTooltipText(view, null) }
}

data class LayoutDirectionModifier(val layoutDirection: Int) : ViewAttributeModifier<View> {
    override fun apply(view: View) { view.layoutDirection = layoutDirection }
    override fun reset(view: View) { view.layoutDirection = View.LAYOUT_DIRECTION_INHERIT }
}

data class TextDirectionModifier(val textDirection: Int) : ViewAttributeModifier<View> {
    override fun apply(view: View) { view.textDirection = textDirection }
    override fun reset(view: View) { view.textDirection = View.TEXT_DIRECTION_INHERIT }
}

data class TextAlignmentModifier(val textAlignment: Int) : ViewAttributeModifier<View> {
    override fun apply(view: View) { view.textAlignment = textAlignment }
    override fun reset(view: View) { view.textAlignment = View.TEXT_ALIGNMENT_GRAVITY }
}

data class ScrollableModifier(
    val horizontalScrollBarEnabled: Boolean = true,
    val verticalScrollBarEnabled: Boolean = true,
    val fadeScrollbars: Boolean = true,
    val scrollbarStyle: Int = View.SCROLLBARS_INSIDE_OVERLAY
) : ViewAttributeModifier<View> {
    override fun apply(view: View) {
        view.isHorizontalScrollBarEnabled = horizontalScrollBarEnabled
        view.isVerticalScrollBarEnabled = verticalScrollBarEnabled
        view.isScrollbarFadingEnabled = fadeScrollbars
        view.scrollBarStyle = scrollbarStyle
    }
    override fun reset(view: View) {
        view.isHorizontalScrollBarEnabled = true
        view.isVerticalScrollBarEnabled = true
        view.isScrollbarFadingEnabled = true
        view.scrollBarStyle = View.SCROLLBARS_INSIDE_OVERLAY
    }
}

data class NestedScrollingModifier(val enabled: Boolean) : ViewAttributeModifier<View> {
    override fun apply(view: View) { view.isNestedScrollingEnabled = enabled }
    override fun reset(view: View) { view.isNestedScrollingEnabled = false }
}

data class OverScrollModeModifier(val mode: Int) : ViewAttributeModifier<View> {
    override fun apply(view: View) { view.overScrollMode = mode }
    override fun reset(view: View) { view.overScrollMode = View.OVER_SCROLL_IF_CONTENT_SCROLLS }
}

data class LayerTypeModifier(val layerType: Int, val paint: Paint? = null) : ViewAttributeModifier<View> {
    override fun apply(view: View) { view.setLayerType(layerType, paint) }
    override fun reset(view: View) { view.setLayerType(View.LAYER_TYPE_NONE, null) }
}

data class ClipToOutlineModifier(val clipToOutline: Boolean) : ViewAttributeModifier<View> {
    override fun apply(view: View) { view.clipToOutline = clipToOutline }
    override fun reset(view: View) { view.clipToOutline = false }
}

data class OutlineProviderModifier(val provider: ViewOutlineProvider?) : ViewAttributeModifier<View> {
    override fun apply(view: View) { view.outlineProvider = provider }
    override fun reset(view: View) { view.outlineProvider = ViewOutlineProvider.BACKGROUND }
}

@RequiresApi(Build.VERSION_CODES.P)
data class ShadowColorModifier(val ambientShadowColor: Int? = null, val spotShadowColor: Int? = null) : ViewAttributeModifier<View> {
    override fun apply(view: View) {
        ambientShadowColor?.let { view.outlineAmbientShadowColor = it }
        spotShadowColor?.let { view.outlineSpotShadowColor = it }
    }
    override fun reset(view: View) {
        view.outlineAmbientShadowColor = Color.BLACK
        view.outlineSpotShadowColor = Color.BLACK
    }
}

data class CameraDistanceModifier(val distance: Float) : ViewAttributeModifier<View> {
    override fun apply(view: View) { view.cameraDistance = distance }
    override fun reset(view: View) {
        val displayMetrics = view.context.resources.displayMetrics
        view.cameraDistance = 1280f * displayMetrics.density
    }
}

data class IdModifier(@IdRes val id: Int) : ViewAttributeModifier<View> {
    override fun apply(view: View) { view.id = id }
    override fun reset(view: View) { view.id = View.NO_ID }
}

data class TagModifier(val tag: Any?) : ViewAttributeModifier<View> {
    override fun apply(view: View) { view.tag = tag }
    override fun reset(view: View) { view.tag = null }
}

data class FitsSystemWindowsModifier(val fit: Boolean) : ViewAttributeModifier<View> {
    override fun apply(view: View) { view.fitsSystemWindows = fit }
    override fun reset(view: View) { view.fitsSystemWindows = false }
}

data class OnApplyWindowInsetsModifier(val listener: (View, WindowInsetsCompat) -> WindowInsetsCompat) : ViewAttributeModifier<View> {
    override fun apply(view: View) {
        ViewCompat.setOnApplyWindowInsetsListener(view) { v, insets -> Snapshot.withMutableSnapshot { listener(v, insets) } }
    }
    override fun reset(view: View) {
        ViewCompat.setOnApplyWindowInsetsListener(view, null)
    }
}

fun Modifier.innerPadding(all: Int): Modifier = this.then(InnerPaddingModifier(all, all, all, all))
fun Modifier.innerPadding(horizontal: Int = 0, vertical: Int = 0): Modifier =
    this.then(InnerPaddingModifier(horizontal, vertical, horizontal, vertical))
fun Modifier.innerPadding(left: Int = 0, top: Int = 0, right: Int = 0, bottom: Int = 0): Modifier =
    this.then(InnerPaddingModifier(left, top, right, bottom))

fun Modifier.size(width: Int, height: Int): Modifier =
    this.then(MinWidthModifier(width)).then(MinHeightModifier(height))
fun Modifier.size(size: Int): Modifier = this.size(size, size)
fun Modifier.width(width: Int): Modifier = this.then(MinWidthModifier(width))
fun Modifier.height(height: Int): Modifier = this.then(MinHeightModifier(height))

fun Modifier.elevation(elevation: Float): Modifier = this.then(ElevationModifier(elevation))

fun Modifier.offset(x: Float, y: Float, z: Float = 0f): Modifier =
    this.then(TranslationModifier(x, y, z))

fun Modifier.alpha(alpha: Float): Modifier = this.then(AlphaModifier(alpha))

fun Modifier.scale(scale: Float): Modifier = this.then(ScaleModifier(scale, scale))
fun Modifier.scale(scaleX: Float, scaleY: Float): Modifier = this.then(ScaleModifier(scaleX, scaleY))

fun Modifier.rotate(degrees: Float): Modifier = this.then(RotationModifier(degrees, 0f, 0f))
fun Modifier.rotate(rotation: Float = 0f, rotationX: Float = 0f, rotationY: Float = 0f): Modifier =
    this.then(RotationModifier(rotation, rotationX, rotationY))

fun Modifier.transformOrigin(pivotX: Float, pivotY: Float): Modifier =
    this.then(PivotModifier(pivotX, pivotY))

fun Modifier.clickable(onClick: () -> Unit): Modifier =
    this.then(ClickableModifier(true, onClick))
fun Modifier.clickable(enabled: Boolean = true, onClick: (() -> Unit)? = null): Modifier =
    this.then(ClickableModifier(enabled, onClick))

fun Modifier.longClickable(onLongClick: () -> Boolean): Modifier =
    this.then(LongClickableModifier(true, onLongClick))
fun Modifier.longClickable(enabled: Boolean = true, onLongClick: (() -> Boolean)? = null): Modifier =
    this.then(LongClickableModifier(enabled, onLongClick))

fun Modifier.onTouch(action: (view: View, event: MotionEvent) -> Boolean): Modifier = this.then(OnTouchModifier(action))

fun Modifier.enabled(enabled: Boolean = true): Modifier = this.then(EnabledModifier(enabled))

fun Modifier.focusable(focusable: Boolean = true): Modifier =
    this.then(FocusableModifier(focusable))
fun Modifier.focusableInTouchMode(focusable: Boolean = true): Modifier =
    this.then(FocusableModifier(focusable, focusable))
fun Modifier.onFocusChanged(action: (hasFocus: Boolean) -> Unit): Modifier = this.then(OnFocusChangedModifier(action))

fun Modifier.visible(visible: Boolean = true): Modifier =
    this.then(VisibilityModifier(if (visible) View.VISIBLE else View.GONE))
fun Modifier.visibility(visibility: Int): Modifier = this.then(VisibilityModifier(visibility))

fun Modifier.selected(selected: Boolean = true): Modifier = this.then(SelectedModifier(selected))
fun Modifier.activated(activated: Boolean = true): Modifier = this.then(ActivatedModifier(activated))
fun Modifier.pressed(pressed: Boolean = true): Modifier = this.then(PressedModifier(pressed))

fun Modifier.keepScreenOn(keepScreenOn: Boolean = true): Modifier =
    this.then(KeepScreenOnModifier(keepScreenOn))

fun Modifier.background(@DrawableRes resId: Int): Modifier = this.then(BackgroundResourceModifier(resId))
fun Modifier.backgroundTint(tint: ColorStateList?, mode: PorterDuff.Mode = PorterDuff.Mode.SRC_IN): Modifier =
    this.then(BackgroundTintModifier(tint, mode))

@RequiresApi(Build.VERSION_CODES.P)
fun Modifier.foreground(drawable: Drawable?): Modifier = this.then(ForegroundModifier(drawable))
@RequiresApi(Build.VERSION_CODES.P)
fun Modifier.foregroundTint(tint: ColorStateList?, mode: PorterDuff.Mode = PorterDuff.Mode.SRC_IN): Modifier =
    this.then(ForegroundTintModifier(tint, mode))

fun Modifier.contentDescription(description: CharSequence?): Modifier =
    this.then(ContentDescriptionModifier(description))
fun Modifier.semantics(
    importantForAccessibility: Int? = null,
    isHeading: Boolean? = null,
    liveRegion: Int? = null
): Modifier = this.then(AccessibilityModifier(importantForAccessibility, isHeading, liveRegion))

fun Modifier.tooltip(text: CharSequence?): Modifier = this.then(TooltipModifier(text))

fun Modifier.layoutDirection(direction: Int): Modifier = this.then(LayoutDirectionModifier(direction))
fun Modifier.textDirection(direction: Int): Modifier = this.then(TextDirectionModifier(direction))
fun Modifier.textAlignment(alignment: Int): Modifier = this.then(TextAlignmentModifier(alignment))

fun Modifier.scrollable(
    horizontalScrollbar: Boolean = true,
    verticalScrollbar: Boolean = true,
    fadeScrollbars: Boolean = true,
    scrollbarStyle: Int = View.SCROLLBARS_INSIDE_OVERLAY
): Modifier = this.then(ScrollableModifier(horizontalScrollbar, verticalScrollbar, fadeScrollbars, scrollbarStyle))

fun Modifier.nestedScrolling(enabled: Boolean = true): Modifier =
    this.then(NestedScrollingModifier(enabled))

fun Modifier.overScrollMode(mode: Int): Modifier = this.then(OverScrollModeModifier(mode))

fun Modifier.layerType(type: Int, paint: Paint? = null): Modifier =
    this.then(LayerTypeModifier(type, paint))

fun Modifier.clipToOutline(clip: Boolean = true): Modifier =
    this.then(ClipToOutlineModifier(clip))

fun Modifier.outlineProvider(provider: ViewOutlineProvider?): Modifier =
    this.then(OutlineProviderModifier(provider))

@RequiresApi(Build.VERSION_CODES.P)
fun Modifier.shadowColor(
    ambientColor: Int,
    spotColor: Int
): Modifier = this.then(ShadowColorModifier(ambientColor, spotColor))

fun Modifier.cameraDistance(distance: Float): Modifier =
    this.then(CameraDistanceModifier(distance))

fun Modifier.hapticFeedback(enabled: Boolean = true): Modifier =
    this.then(HapticFeedbackModifier(enabled))

fun Modifier.soundEffects(enabled: Boolean = true): Modifier =
    this.then(SoundEffectsModifier(enabled))

fun Modifier.id(@IdRes id: Int): Modifier = this.then(IdModifier(id))

fun Modifier.tag(tag: Any?): Modifier = this.then(TagModifier(tag))

@Deprecated("Use onApplyWindowInsets for more granular control on API 20+")
fun Modifier.fitsSystemWindows(fit: Boolean = true): Modifier = this.then(FitsSystemWindowsModifier(fit))

fun Modifier.onApplyWindowInsets(listener: (View, WindowInsetsCompat) -> WindowInsetsCompat): Modifier = this.then(OnApplyWindowInsetsModifier(listener))