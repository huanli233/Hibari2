package com.huanli233.hibari2

import android.os.Bundle
import android.widget.FrameLayout
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.expandIn
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.togetherWith
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.google.android.material.card.MaterialCardView
import com.huanli233.hibari2.foundation.layout.Box
import com.huanli233.hibari2.foundation.layout.Column
import com.huanli233.hibari2.foundation.modifiers.clickable
import com.huanli233.hibari2.foundation.modifiers.clipToOutline
import com.huanli233.hibari2.material.Button
import com.huanli233.hibari2.material.Card
import com.huanli233.hibari2.material.Text
import com.huanli233.hibari2.runtime.ComposeHibariView
import com.huanli233.hibari2.runtime.composable.AndroidViewGroup
import com.huanli233.hibari2.runtime.modifier.fillMaxSize
import com.huanli233.hibari2.runtime.modifier.padding

class ComposeActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val root = ComposeHibariView(this)
        setContentView(root)
        root.setContent {
            MyViewComposeScreen()
        }
    }

}

@Composable
fun MyViewComposeScreen() {
    var showContent by remember { mutableStateOf(true) }
    val context = LocalContext.current
    val alpha by animateFloatAsState(targetValue = if (showContent) 1f else 0f)
    Box(Modifier.fillMaxSize().clickable { showContent = !showContent }) {
        Column(Modifier.align(Alignment.CenterStart)) {
            Button("Click me", onClick = { Toast.makeText(context, "Hello, World!", Toast.LENGTH_SHORT).show() })
            Card {
                Column(Modifier.padding(16.dp)) {
                    AnimatedVisibility(showContent) {
                        Text("Hello, World!")
                    }
                    AnimatedContent(
                        targetState = showContent,
                        transitionSpec = { expandVertically() togetherWith shrinkVertically() }
                    ) {
                        when (it) {
                            false -> Text("Test1")
                            true -> Text("Test12345")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun Frame(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    AndroidViewGroup(
        factory = { context ->
            FrameLayout(context)
        },
        modifier = modifier,
        content = content
    )
}