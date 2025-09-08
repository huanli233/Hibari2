package com.huanli233.hibari2

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.unit.dp
import com.google.android.material.card.MaterialCardView
import com.huanli233.hibari2.foundation.layout.Arrangement
import com.huanli233.hibari2.foundation.layout.Box
import com.huanli233.hibari2.foundation.layout.Column
import com.huanli233.hibari2.foundation.layout.Spacer
import com.huanli233.hibari2.foundation.layout.fillMaxSize
import com.huanli233.hibari2.foundation.layout.fillMaxWidth
import com.huanli233.hibari2.foundation.layout.height
import com.huanli233.hibari2.foundation.layout.padding
import com.huanli233.hibari2.material.NativeText
import com.huanli233.hibari2.core.ViewBacked
import com.huanli233.hibari2.core.ViewLayoutMode
import com.huanli233.hibari2.core.shape.RoundedCornerShape
import com.huanli233.hibari2.core.viewBackground
import com.huanli233.hibari2.core.viewBorder
import com.huanli233.hibari2.core.viewGraphicsLayer

class ComposeActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d("HibariDebug", "ComposeActivity.onCreate")
        val root = ComposeView(this)
        setContentView(root)
        root.setContent {
            HibariExampleScreen()
        }
    }

}

@Composable
fun HibariExampleScreen() {
    var rotationZ by remember { mutableFloatStateOf(0f) }
    var alpha by remember { mutableFloatStateOf(1f) }
    var elevation by remember { mutableFloatStateOf(8f) }
    var scale by remember { mutableFloatStateOf(1f) }
    var layoutMode by remember { mutableStateOf(ViewLayoutMode.NATIVE) }

    val resources = LocalResources.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        ViewBacked(
            Modifier
                .viewBackground(Color(0xFF81D4FA))
                .viewBorder(2.dp, Color(0xFF01579B), RoundedCornerShape(16.dp))
                .viewGraphicsLayer(
                    rotationZ = rotationZ,
                    alpha = alpha,
                    shadowElevation = elevation * resources.displayMetrics.density,
                    scaleX = scale,
                    scaleY = scale
                )
                .viewGraphicsLayer()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp),
            ) {
                NativeText("This is a native TextView")
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        ControlPanel(
            rotationZ = rotationZ, onRotationZChange = { rotationZ = it },
            alpha = alpha, onAlphaChange = { alpha = it },
            elevation = elevation, onElevationChange = { elevation = it },
            scale = scale, onScaleChange = { scale = it },
            layoutMode = layoutMode, onLayoutModeChange = { layoutMode = it }
        )
    }
}

@Composable
fun ControlPanel(
    rotationZ: Float, onRotationZChange: (Float) -> Unit,
    alpha: Float, onAlphaChange: (Float) -> Unit,
    elevation: Float, onElevationChange: (Float) -> Unit,
    scale: Float, onScaleChange: (Float) -> Unit,
    layoutMode: ViewLayoutMode, onLayoutModeChange: (ViewLayoutMode) -> Unit
) {
    NativeCard {
        Column(Modifier.padding(16.dp)) {
            NativeText("Rotation Z: ${rotationZ.toInt()}°")
        }
    }
}

@Composable
fun NativeCard(
    modifier: Modifier = Modifier,
    update: (MaterialCardView) -> Unit = {},
    content: @Composable () -> Unit = {}
) {
    NativeViewGroup(
        factory = { context -> MaterialCardView(context) },
        modifier = modifier,
        update = {
            update(it)
        },
        content = content
    )
}