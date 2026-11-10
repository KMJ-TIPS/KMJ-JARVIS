package com.kmj.jarvis.ui

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.VectorConverter
import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.drawscope.rotate
import com.kmj.jarvis.assistant.Phase
import kotlinx.coroutines.delay
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

// KMJ JARVIS avatar palette.
private val Violet = Color(0xFFB79CFF)
private val Iris = Color(0xFF6D28D9)
private val Deep = Color(0xFF3B2A6B)
private val Pink = Color(0xFFF472B6)
private val Gold = Color(0xFFFFC24B)
private val Glow = Color(0xFFEDE8FF)

/**
 * The assistant as a little 3D character: a glossy sphere with a face. The eyes blink and
 * wander, the mouth reacts to the phase (equalizer while speaking, pulsing ring while
 * listening, dots while connecting, a smile on standby), and two tilted rings with
 * satellites orbit the body with real depth. Standby is one still frame (no battery cost)
 * apart from the blinks. [level] (0..1) drives the voice reactions.
 */
@Composable
fun Orb(phase: Phase, modifier: Modifier = Modifier, level: Float = 0f) {
    val accent = Accent
    val voice by animateFloatAsState(level, spring(stiffness = Spring.StiffnessLow), label = "voice")
    val energy by animateFloatAsState(
        when (phase) {
            Phase.Off -> 0f
            Phase.Connecting -> 0.35f
            Phase.Listening -> 0.55f
            Phase.Speaking -> 0.9f
        },
        tween(700),
        label = "energy",
    )
    // Eye mood: squint while speaking, widen while listening.
    val mood by animateFloatAsState(
        when (phase) {
            Phase.Speaking -> 0.72f
            Phase.Listening -> 1.14f
            else -> 1f
        },
        tween(320),
        label = "mood",
    )

    // Blinking: quick close-open every 1.8-4.8 s, in every phase.
    var blink by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        while (true) {
            delay(Random.nextLong(1_800, 4_800))
            blink = true
            delay(90)
            blink = false
        }
    }
    // Wandering gaze: the eyes look around every 1.4-3.4 s.
    val gaze = remember { Animatable(Offset.Zero, Offset.VectorConverter) }
    LaunchedEffect(Unit) {
        while (true) {
            delay(Random.nextLong(1_400, 3_400))
            gaze.animateTo(
                Offset(Random.nextFloat() * 2 - 1, Random.nextFloat() * 1.4f - 0.6f),
                tween(650),
            )
        }
    }
    // Blink openness eases rather than snaps.
    val open by animateFloatAsState(if (blink) 0.08f else 1f, tween(if (blink) 40 else 80), label = "open")

    val t = if (phase == Phase.Off) {
        0f
    } else {
        rememberInfiniteTransition(label = "orb").animateFloat(
            initialValue = 0f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(tween(CYCLE_MS, easing = LinearEasing), RepeatMode.Restart),
            label = "t",
        ).value
    }
    val path = remember { Path() }

    Canvas(modifier) {
        val c = center
        val base = size.minDimension / 2 * 0.52f * (1 + 0.05f * voice)
        val a = t * 2 * PI.toFloat()
        // Floating bob (frozen on standby) and floor shadow.
        val bob = if (phase == Phase.Off) 0f else sin(a * 2.2f) * 0.05f * base
        val bodyC = Offset(c.x, c.y + bob)
        val shadowR = base * (1.05f - 0.18f * (bob / (0.05f * base) + 1f) / 2f).coerceIn(0.72f, 1.05f)
        drawCircle(
            Brush.radialGradient(listOf(Color.Black.copy(alpha = 0.30f), Color.Transparent), Offset(c.x, c.y + base * 1.28f), shadowR),
            radius = shadowR,
            center = Offset(c.x, c.y + base * 1.28f),
        )

        val wobble = 0.02f + 0.04f * energy + 0.09f * voice
        path.reset()
        for (i in 0..POINTS) {
            val th = i * 2 * PI.toFloat() / POINTS
            val r = base * (
                1 + wobble * (
                    0.55f * sin(3 * th + 3 * a) +
                        0.30f * sin(5 * th - 2 * a) +
                        0.15f * sin(2 * th + 5 * a)
                    )
                )
            val x = bodyC.x + r * cos(th)
            val y = bodyC.y + r * sin(th)
            if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
        }
        path.close()

        // ---- orbit rings: far halves and far satellites first (behind the body) ----
        // far satellites
        for (i in 0..1) {
            val rx = base * if (i == 0) 1.45f else 1.72f
            val ry = rx * if (i == 0) 0.30f else 0.20f
            val speed = if (i == 0) 1f else -0.7f
            val theta = a * speed + i * 2.1f
            val wobbleDeg = 12f * sin(a * 0.5f + i * 1.3f)
            val ex = rx * cos(theta)
            val ey = ry * sin(theta)
            val wr = Math.toRadians(wobbleDeg.toDouble())
            val px = bodyC.x + (ex * cos(wr) - ey * sin(wr)).toFloat()
            val py = bodyC.y + (ex * sin(wr) + ey * cos(wr)).toFloat()
            val front = sin(theta) >= 0
            if (!front) {
                drawCircle(if (i == 0) Gold.copy(alpha = 0.5f) else Violet.copy(alpha = 0.45f), base * 0.055f, Offset(px, py))
            }
        }
        // far ring halves (top half of each tilted ellipse)
        for (i in 0..1) {
            val rx = base * if (i == 0) 1.45f else 1.72f
            val ry = rx * if (i == 0) 0.30f else 0.20f
            val wobbleDeg = 12f * sin(a * 0.5f + i * 1.3f)
            rotate(wobbleDeg, bodyC) {
                drawArc(
                    color = if (i == 0) Violet.copy(alpha = 0.28f) else Violet.copy(alpha = 0.20f),
                    startAngle = 180f,
                    sweepAngle = 180f,
                    useCenter = false,
                    topLeft = Offset(bodyC.x - rx, bodyC.y - ry),
                    size = Size(rx * 2, ry * 2),
                    style = Stroke(width = 2.4f * density),
                )
            }
        }

        // ---- soft glow behind the body ----
        drawCircle(
            Brush.radialGradient(
                listOf(accent.copy(alpha = 0.20f + 0.22f * voice + 0.1f * energy), Color.Transparent),
                bodyC,
                base * 1.9f,
            ),
            radius = base * 1.9f,
            center = bodyC,
        )

        // ---- body: glossy sphere ----
        clipPath(path) {
            drawCircle(Brush.radialGradient(listOf(Iris, Deep), bodyC, base * 1.1f), radius = base * 1.15f, center = bodyC)
            // drifting light spots
            val spots = listOf(accent, Violet, Pink)
            spots.forEachIndexed { i, color ->
                val angle = a * (i + 1) * if (i % 2 == 0) 1 else -1
                val spot = Offset(bodyC.x + base * 0.45f * cos(angle + i * 2.1f), bodyC.y + base * 0.45f * sin(angle + i * 2.1f))
                drawCircle(Brush.radialGradient(listOf(color.copy(alpha = 0.5f), color.copy(alpha = 0f)), spot, base * 0.9f), radius = base * 1.1f, center = bodyC)
            }
            // inner shadow, bottom right
            drawCircle(
                Brush.radialGradient(
                    listOf(Color.Transparent, Color.Black.copy(alpha = 0.45f)),
                    center = Offset(bodyC.x - base * 0.35f, bodyC.y - base * 0.35f),
                    radius = base * 1.55f,
                ),
                radius = base * 1.2f,
                center = bodyC,
            )
            // specular highlight, top left
            drawCircle(
                Brush.radialGradient(
                    listOf(Color.White.copy(alpha = 0.55f), Color.Transparent),
                    center = Offset(bodyC.x - base * 0.34f, bodyC.y - base * 0.42f),
                    radius = base * 0.72f,
                ),
                radius = base * 1.0f,
                center = bodyC,
            )
        }

        // ---- face ----
        val eyeY = bodyC.y - base * 0.10f
        val eyeDx = base * 0.30f
        val eyeW = base * 0.13f
        val eyeH = base * 0.20f * mood * open
        val gazeX = gaze.value.x * base * 0.07f
        val gazeY = gaze.value.y * base * 0.05f
        for (side in listOf(-1f, 1f)) {
            val ex = bodyC.x + side * eyeDx + gazeX
            val ey = eyeY + gazeY
            // glow halo
            drawCircle(accent.copy(alpha = 0.30f), eyeW * 1.9f, Offset(ex, ey))
            // capsule eye
            drawRoundRect(
                Brush.verticalGradient(listOf(Glow, Violet)),
                topLeft = Offset(ex - eyeW / 2, ey - eyeH / 2),
                size = Size(eyeW, eyeH.coerceAtLeast(base * 0.012f)),
                cornerRadius = CornerRadius(eyeW, eyeW),
            )
            // white specular dot
            drawCircle(Color.White, eyeW * 0.22f, Offset(ex - eyeW * 0.15f, ey - eyeH * 0.22f))
        }

        val mouthY = bodyC.y + base * 0.30f
        when (phase) {
            Phase.Speaking -> {
                // 5 equalizer bars driven by the voice level.
                val bw = base * 0.085f
                val gap = base * 0.055f
                val total = 5 * bw + 4 * gap
                for (i in 0..4) {
                    val wave = abs(sin(a * (9 + i) * (if (i % 2 == 0) 1f else -1f) + i * 0.9f))
                    val h = base * (0.10f + 0.42f * (0.25f + 0.75f * voice) * (0.35f + 0.65f * wave))
                    val bx = bodyC.x - total / 2 + i * (bw + gap)
                    drawRoundRect(
                        Brush.verticalGradient(listOf(Gold, Violet)),
                        topLeft = Offset(bx, mouthY - h / 2),
                        size = Size(bw, h),
                        cornerRadius = CornerRadius(bw / 2, bw / 2),
                    )
                }
            }
            Phase.Listening -> {
                // pulsing gold ring
                val pulse = 0.5f + 0.5f * sin(a * 6f)
                drawCircle(
                    Gold.copy(alpha = 0.55f + 0.35f * pulse),
                    base * (0.14f + 0.05f * pulse),
                    Offset(bodyC.x, mouthY),
                    style = Stroke(width = 3f * density),
                )
            }
            Phase.Connecting -> {
                // 3 blinking dots
                val dw = base * 0.055f
                for (i in 0..2) {
                    val alpha = 0.35f + 0.65f * (0.5f + 0.5f * sin(a * 10f - i * 1.1f))
                    drawCircle(Glow.copy(alpha = alpha), dw, Offset(bodyC.x + (i - 1) * dw * 2.6f, mouthY))
                }
            }
            Phase.Off -> {
                // gentle smile
                drawArc(
                    Glow.copy(alpha = 0.9f),
                    startAngle = 25f,
                    sweepAngle = 130f,
                    useCenter = false,
                    topLeft = Offset(bodyC.x - base * 0.16f, mouthY - base * 0.10f),
                    size = Size(base * 0.32f, base * 0.20f),
                    style = Stroke(width = 3.4f * density, cap = StrokeCap.Round),
                )
            }
        }

        // ---- near ring halves + near satellites (in front of the body) ----
        for (i in 0..1) {
            val rx = base * if (i == 0) 1.45f else 1.72f
            val ry = rx * if (i == 0) 0.30f else 0.20f
            val wobbleDeg = 12f * sin(a * 0.5f + i * 1.3f)
            rotate(wobbleDeg, bodyC) {
                drawArc(
                    Brush.horizontalGradient(listOf(if (i == 0) Gold else Violet, Glow.copy(alpha = 0.9f))),
                    startAngle = 0f,
                    sweepAngle = 180f,
                    useCenter = false,
                    topLeft = Offset(bodyC.x - rx, bodyC.y - ry),
                    size = Size(rx * 2, ry * 2),
                    style = Stroke(width = 3.2f * density),
                )
            }
        }
        for (i in 0..1) {
            val rx = base * if (i == 0) 1.45f else 1.72f
            val ry = rx * if (i == 0) 0.30f else 0.20f
            val speed = if (i == 0) 1f else -0.7f
            val theta = a * speed + i * 2.1f
            if (sin(theta) >= 0) {
                val wobbleDeg = 12f * sin(a * 0.5f + i * 1.3f)
                val ex = rx * cos(theta)
                val ey = ry * sin(theta)
                val wr = Math.toRadians(wobbleDeg.toDouble())
                val px = bodyC.x + (ex * cos(wr) - ey * sin(wr)).toFloat()
                val py = bodyC.y + (ex * sin(wr) + ey * cos(wr)).toFloat()
                drawCircle(Glow.copy(alpha = 0.5f), base * 0.085f, Offset(px, py))
                drawCircle(if (i == 0) Gold else Violet, base * 0.055f, Offset(px, py))
            }
        }

        // connecting: a chase arc around everything
        if (phase == Phase.Connecting) {
            val ring = base * 1.32f
            rotate(t * 360f * 8, bodyC) {
                drawArc(
                    Brush.sweepGradient(listOf(Color.Transparent, accent, Color.Transparent), bodyC),
                    startAngle = 0f,
                    sweepAngle = 140f,
                    useCenter = false,
                    topLeft = Offset(bodyC.x - ring, bodyC.y - ring),
                    size = Size(ring * 2, ring * 2),
                    style = Stroke(width = 3 * density),
                )
            }
        }
    }
}

private const val CYCLE_MS = 12_000
private const val POINTS = 96
