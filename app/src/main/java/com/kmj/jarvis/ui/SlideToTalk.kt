package com.kmj.jarvis.ui

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.kmj.jarvis.Branding
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

/**
 * A full-width pill slider instead of a tap button: drag the glossy 3D knob to the right to
 * start a session, drag it back to stop. Sliding past 55% of the travel commits; anything
 * less springs home. The track glows gold while the session is live.
 */
@Composable
fun SlideToTalk(
    active: Boolean,
    onSlideOn: () -> Unit,
    onSlideOff: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val trackColor = MaterialTheme.colorScheme.surfaceVariant
    val onTrack = MaterialTheme.colorScheme.onSurfaceVariant
    val gold = MaterialTheme.colorScheme.tertiary
    val density = LocalDensity.current
    val scope = rememberCoroutineScope()

    var trackWidthPx by remember { mutableFloatStateOf(0f) }
    var dragging by remember { mutableStateOf(false) }
    val knob = remember { Animatable(0f) }
    val padPx = with(density) { 6.dp.toPx() }
    val knobPx = with(density) { 52.dp.toPx() }
    val maxDrag = (trackWidthPx - knobPx - 2 * padPx).coerceAtLeast(1f)

    // When the session state changes from outside, settle the knob at the right end.
    LaunchedEffect(active, maxDrag) {
        if (!dragging) {
            knob.animateTo(if (active) 1f else 0f, spring(Spring.DampingRatioMediumBouncy, Spring.StiffnessLow))
        }
    }

    val chevrons = rememberInfiniteTransition(label = "chev")
    val wave by chevrons.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(1100)),
        label = "wave",
    )

    Column(modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            Modifier
                .fillMaxWidth()
                .height(64.dp)
                .shadow(if (active) 10.dp else 6.dp, RoundedCornerShape(32.dp))
                .onSizeChanged { trackWidthPx = it.width.toFloat() }
                .background(
                    if (active) Brush.horizontalGradient(listOf(trackColor, gold.copy(alpha = 0.30f), trackColor))
                    else Brush.verticalGradient(listOf(trackColor, trackColor.copy(alpha = 0.88f))),
                    RoundedCornerShape(32.dp),
                )
                .semantics {
                    contentDescription = if (active) "Slide to stop ${Branding.ASSISTANT_NAME}"
                    else "Slide to talk to ${Branding.ASSISTANT_NAME}"
                }
                .pointerInput(active, maxDrag) {
                    detectDragGestures(
                        onDragStart = { dragging = true },
                        onDragEnd = {
                            dragging = false
                            val crossed = knob.value >= 0.55f
                            val becameActive = crossed && !active
                            val becameInactive = !crossed && active
                            scope.launch {
                                knob.animateTo(if (crossed) 1f else 0f, spring(Spring.DampingRatioMediumBouncy))
                            }
                            if (becameActive) onSlideOn()
                            if (becameInactive) onSlideOff()
                        },
                        onDragCancel = {
                            dragging = false
                            scope.launch { knob.animateTo(if (active) 1f else 0f, spring()) }
                        },
                    ) { change, amount ->
                        change.consume()
                        val raw = knob.value + amount.x / maxDrag
                        scope.launch { knob.snapTo(raw.coerceIn(0f, 1f)) }
                    }
                },
        ) {
            // Labels behind the knob.
            Row(
                Modifier
                    .align(if (active) Alignment.CenterStart else Alignment.CenterEnd)
                    .padding(horizontal = 22.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                if (active) {
                    Text("‹", color = gold, style = MaterialTheme.typography.titleLarge)
                    Text(
                        "Slide to Stop",
                        style = MaterialTheme.typography.labelLarge,
                        color = onTrack,
                        modifier = Modifier.padding(start = 6.dp),
                    )
                } else {
                    Text(
                        "Slide to Talk",
                        style = MaterialTheme.typography.labelLarge,
                        color = onTrack,
                        modifier = Modifier.padding(end = 6.dp),
                    )
                    Text("›", color = onTrack, style = MaterialTheme.typography.titleLarge)
                    Text(
                        "›",
                        color = onTrack,
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.padding(start = 2.dp).alpha(wave.coerceIn(0f, 1f)),
                    )
                    Text(
                        "›",
                        color = onTrack,
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.alpha((wave - 0.4f).coerceIn(0f, 1f)),
                    )
                }
            }
            // The glossy 3D knob.
            val offPx = padPx + maxDrag * knob.value
            Box(
                Modifier
                    .offset { IntOffset(offPx.roundToInt(), 0) }
                    .align(Alignment.CenterStart)
                    .size(52.dp)
                    .shadow(8.dp, CircleShape)
                    .background(
                        Brush.radialGradient(
                            listOf(
                                if (active) Color(0xFFFFD98A) else Color(0xFFC9B4FF),
                                if (active) Color(0xFFE0912A) else Color(0xFF6D28D9),
                            ),
                        ),
                        CircleShape,
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Canvas(Modifier.fillMaxSize()) {
                    drawCircle(
                        Color.White.copy(alpha = 0.30f),
                        radius = size.minDimension / 2.2f,
                        center = Offset(size.width * 0.42f, size.height * 0.38f),
                    )
                }
                if (active) {
                    Icon(
                        Icons.Filled.Close,
                        contentDescription = null,
                        tint = Color(0xFF3D2A00),
                        modifier = Modifier.size(24.dp),
                    )
                } else {
                    MicIcon()
                }
            }
        }
    }
}

/** Simple white microphone drawn by hand so it sits crisp on the knob. */
@Composable
private fun MicIcon() {
    Canvas(Modifier.size(24.dp)) {
        val w = size.width
        val h = size.height
        drawRoundRect(
            Color.White,
            topLeft = Offset(w * 0.36f, h * 0.10f),
            size = Size(w * 0.28f, h * 0.40f),
            cornerRadius = CornerRadius(w * 0.14f, w * 0.14f),
        )
        drawArc(
            Color.White,
            startAngle = -15f,
            sweepAngle = 210f,
            useCenter = false,
            topLeft = Offset(w * 0.22f, h * 0.28f),
            size = Size(w * 0.56f, h * 0.56f),
            style = Stroke(2.4f),
        )
        drawLine(Color.White, Offset(w * 0.5f, h * 0.60f), Offset(w * 0.5f, h * 0.84f), strokeWidth = 2.4f)
        drawLine(Color.White, Offset(w * 0.36f, h * 0.86f), Offset(w * 0.64f, h * 0.86f), strokeWidth = 2.4f)
    }
}
