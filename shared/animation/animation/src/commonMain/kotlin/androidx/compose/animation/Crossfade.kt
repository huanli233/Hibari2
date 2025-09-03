package androidx.compose.animation

import androidx.collection.mutableScatterMapOf
import androidx.compose.animation.core.FiniteAnimationSpec
import androidx.compose.animation.core.Transition
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.updateTransition
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.util.fastForEach
import com.huanli233.hibari2.foundation.layout.Box
import com.huanli233.hibari2.foundation.modifiers.alpha

@OptIn(ExperimentalAnimationApi::class)
@Composable
public fun <T> Crossfade(
    targetState: T,
    modifier: Modifier = Modifier,
    animationSpec: FiniteAnimationSpec<Float> = tween(),
    label: String = "Crossfade",
    content: @Composable (T) -> Unit,
) {
    val transition = updateTransition(targetState, label)
    transition.Crossfade(modifier, animationSpec, content = content)
}

@Deprecated("Crossfade API now has a new label parameter added.", level = DeprecationLevel.HIDDEN)
@OptIn(ExperimentalAnimationApi::class)
@Composable
public fun <T> Crossfade(
    targetState: T,
    modifier: Modifier = Modifier,
    animationSpec: FiniteAnimationSpec<Float> = tween(),
    content: @Composable (T) -> Unit,
) {
    val transition = updateTransition(targetState)
    transition.Crossfade(modifier, animationSpec, content = content)
}

@ExperimentalAnimationApi
@Composable
public fun <T> Transition<T>.Crossfade(
    modifier: Modifier = Modifier,
    animationSpec: FiniteAnimationSpec<Float> = tween(),
    contentKey: (targetState: T) -> Any? = { it },
    content: @Composable (targetState: T) -> Unit,
) {
    val currentlyVisible = remember { mutableStateListOf<T>().apply { add(currentState) } }
    val contentMap = remember { mutableScatterMapOf<T, @Composable () -> Unit>() }
    if (currentState == targetState) {
        // If not animating, just display the current state
        if (currentlyVisible.size != 1 || currentlyVisible[0] != targetState) {
            // Remove all the intermediate items from the list once the animation is finished.
            currentlyVisible.removeAll { it != targetState }
            contentMap.clear()
        }
    }
    if (targetState !in contentMap) {
        // Replace target with the same key if any
        val replacementId =
            currentlyVisible.indexOfFirst { contentKey(it) == contentKey(targetState) }
        if (replacementId == -1) {
            currentlyVisible.add(targetState)
        } else {
            currentlyVisible[replacementId] = targetState
        }
        contentMap.clear()
        currentlyVisible.fastForEach { stateForContent ->
            contentMap[stateForContent] = {
                val alpha by
                animateFloat(transitionSpec = { animationSpec }) {
                    if (it == stateForContent) 1f else 0f
                }
                Box(Modifier.alpha(alpha)) { content(stateForContent) }
            }
        }
    }

    Box(modifier) {
        currentlyVisible.fastForEach { key(contentKey(it)) { contentMap[it]?.invoke() } }
    }
}