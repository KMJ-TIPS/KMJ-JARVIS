package com.kmj.jarvis.ui

import android.content.Context
import android.content.pm.PackageManager
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.kmj.jarvis.Branding
import com.kmj.jarvis.R
import kotlinx.coroutines.delay

/**
 * Launch splash: the logo springs in over a breathing glow, the name and tagline follow,
 * three dots race, then [onDone] moves on. Kept short (~1.9 s) so it never annoys.
 */
@Composable
fun Splash(onDone: () -> Unit) {
    val logo = remember { Animatable(0.55f) }
    val text = remember { Animatable(0f) }
    val glow = rememberInfiniteTransition(label = "glow").animateFloat(
        initialValue = 0.5f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(1200), RepeatMode.Reverse),
        label = "glow",
    )
    val dots = rememberInfiniteTransition(label = "dots").animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(900), RepeatMode.Restart),
        label = "dots",
    )

    LaunchedEffect(Unit) {
        logo.animateTo(1f, spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow))
        text.animateTo(1f, tween(260))
        delay(900)
        onDone()
    }

    val bg = MaterialTheme.colorScheme.background
    Box(Modifier.fillMaxSize().background(bg), contentAlignment = Alignment.Center) {
        // breathing radial glow
        Box(
            Modifier
                .size(300.dp)
                .alpha(glow.value)
                .background(
                    Brush.radialGradient(listOf(Accent.copy(alpha = 0.30f), Color.Transparent)),
                ),
        )
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Image(
                painterResource(R.drawable.ic_logo),
                contentDescription = "${Branding.ASSISTANT_NAME} logo",
                modifier = Modifier.size(132.dp).scale(logo.value),
            )
            Column(Modifier.alpha(text.value), horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    Branding.ASSISTANT_NAME,
                    style = MaterialTheme.typography.displaySmall,
                    modifier = Modifier.padding(top = 28.dp),
                )
                Text(
                    "Your phone. Your AI. One voice.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Muted,
                    modifier = Modifier.padding(top = 8.dp),
                )
                Row(
                    Modifier.padding(top = 24.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    repeat(3) { i ->
                        val lit = ((dots.value * 3f - i) % 1f).let { if (it < 0) it + 1f else it }
                        Box(
                            Modifier
                                .size(8.dp)
                                .alpha(0.25f + 0.75f * lit)
                                .background(Accent, androidx.compose.foundation.shape.CircleShape),
                        )
                    }
                }
            }
        }
        Version(Modifier.align(Alignment.BottomCenter).padding(bottom = 32.dp))
    }
}

@Composable
private fun Version(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val version = remember {
        runCatching {
            context.packageManager.getPackageInfo(context.packageName, 0).versionName
        }.getOrNull() ?: ""
    }
    Text("v$version · Gemini powered", style = MaterialTheme.typography.labelMedium, color = Muted, modifier = modifier)
}
