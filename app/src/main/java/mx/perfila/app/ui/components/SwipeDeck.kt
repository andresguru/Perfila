package mx.perfila.app.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import mx.perfila.app.domain.SwipeDecision
import mx.perfila.app.ui.theme.PerfilaColors
import kotlin.math.abs

/** Estado de la tarjeta superior: desplazamiento y animación de salida. */
class SwipeCardState {
    val offsetX = Animatable(0f)
    val offsetY = Animatable(0f)
    /** Ancho de la tarjeta en px; se actualiza al medir. No es estado observable. */
    var widthPx: Float = 1000f

    suspend fun animateOut(decision: SwipeDecision) {
        val spec = tween<Float>(durationMillis = 260)
        when (decision) {
            SwipeDecision.LIKE -> offsetX.animateTo(widthPx * 1.6f, spec)
            SwipeDecision.PASS -> offsetX.animateTo(-widthPx * 1.6f, spec)
            SwipeDecision.SUPER -> offsetY.animateTo(-widthPx * 2.2f, spec)
        }
    }

    suspend fun reset() = coroutineScope {
        val back = spring<Float>(dampingRatio = Spring.DampingRatioMediumBouncy)
        launch { offsetX.animateTo(0f, back) }
        launch { offsetY.animateTo(0f, back) }
    }
}

/**
 * Mazo de tarjetas deslizables con botones Descartar, Destacar y Me interesa.
 * Derecha = LIKE, izquierda = PASS, arriba = SUPER.
 */
@Composable
fun <T> SwipeDeck(
    items: List<T>,
    itemKey: (T) -> String,
    onSwiped: (T, SwipeDecision) -> Unit,
    modifier: Modifier = Modifier,
    superEnabled: Boolean = true,
    emptyContent: @Composable () -> Unit,
    cardContent: @Composable (T) -> Unit,
) {
    val top = items.firstOrNull()
    val next = items.getOrNull(1)
    val scope = rememberCoroutineScope()
    val state = remember(top?.let(itemKey)) { SwipeCardState() }
    var busy by remember(top?.let(itemKey)) { mutableStateOf(false) }

    fun commit(decision: SwipeDecision) {
        val item = top ?: return
        if (busy) return
        busy = true
        scope.launch {
            state.animateOut(decision)
            onSwiped(item, decision)
        }
    }

    Column(modifier, verticalArrangement = Arrangement.spacedBy(14.dp)) {
        BoxWithConstraints(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
        ) {
            val density = LocalDensity.current
            val widthPx = with(density) { maxWidth.toPx() }
            val thresholdPx = widthPx * 0.28f
            state.widthPx = widthPx

            if (top == null) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { emptyContent() }
            } else {
                if (next != null) {
                    Box(
                        Modifier
                            .fillMaxSize()
                            .padding(horizontal = 14.dp)
                            .padding(top = 12.dp)
                            .graphicsLayer {
                                val progress = (abs(state.offsetX.value) / widthPx).coerceIn(0f, 1f)
                                scaleX = 0.94f + 0.06f * progress
                                scaleY = 0.94f + 0.06f * progress
                            },
                    ) { cardContent(next) }
                }
                Box(
                    Modifier
                        .fillMaxSize()
                        .graphicsLayer {
                            translationX = state.offsetX.value
                            translationY = state.offsetY.value
                            rotationZ = (state.offsetX.value / widthPx) * 16f
                        }
                        .pointerInput(top?.let(itemKey)) {
                            detectDragGestures(
                                onDragEnd = {
                                    when {
                                        state.offsetX.value > thresholdPx -> commit(SwipeDecision.LIKE)
                                        state.offsetX.value < -thresholdPx -> commit(SwipeDecision.PASS)
                                        superEnabled && state.offsetY.value < -thresholdPx -> commit(SwipeDecision.SUPER)
                                        else -> scope.launch { state.reset() }
                                    }
                                },
                                onDragCancel = { scope.launch { state.reset() } },
                                onDrag = { change, drag ->
                                    change.consume()
                                    scope.launch {
                                        state.offsetX.snapTo(state.offsetX.value + drag.x)
                                        state.offsetY.snapTo(state.offsetY.value + drag.y)
                                    }
                                },
                            )
                        },
                ) {
                    cardContent(top)
                    SwipeStamps(state, thresholdPx)
                }
            }
        }

        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(22.dp, Alignment.CenterHorizontally),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            RoundAction(Icons.Filled.Close, "Descartar", PerfilaColors.Mandarina, PerfilaColors.Ink, 66.dp, enabled = top != null) {
                commit(SwipeDecision.PASS)
            }
            if (superEnabled) {
                RoundAction(Icons.Filled.Star, "Destacar", PerfilaColors.Violet, Color.White, 52.dp, enabled = top != null) {
                    commit(SwipeDecision.SUPER)
                }
            }
            RoundAction(Icons.Filled.Check, "Me interesa", PerfilaColors.Lime, PerfilaColors.Ink, 66.dp, enabled = top != null) {
                commit(SwipeDecision.LIKE)
            }
        }
    }
}

@Composable
private fun SwipeStamps(state: SwipeCardState, thresholdPx: Float) {
    val x = state.offsetX.value
    val y = state.offsetY.value
    Box(Modifier.fillMaxSize().padding(24.dp)) {
        Stamp(
            text = "ME INTERESA",
            color = PerfilaColors.Lime,
            modifier = Modifier.align(Alignment.TopStart).rotate(-12f).alpha((x / thresholdPx).coerceIn(0f, 1f)),
        )
        Stamp(
            text = "DESCARTAR",
            color = PerfilaColors.Mandarina,
            modifier = Modifier.align(Alignment.TopEnd).rotate(12f).alpha((-x / thresholdPx).coerceIn(0f, 1f)),
        )
        Stamp(
            text = "DESTACAR",
            color = PerfilaColors.Violet,
            textColor = Color.White,
            modifier = Modifier.align(Alignment.BottomCenter).alpha((-y / thresholdPx).coerceIn(0f, 1f)),
        )
    }
}

@Composable
private fun Stamp(text: String, color: Color, modifier: Modifier = Modifier, textColor: Color = PerfilaColors.Ink) {
    Surface(modifier = modifier, color = color, shape = RoundedCornerShape(10.dp)) {
        Text(
            text,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
            style = MaterialTheme.typography.titleLarge,
            color = textColor,
        )
    }
}

@Composable
fun RoundAction(
    icon: ImageVector,
    label: String,
    background: Color,
    tint: Color,
    size: Dp,
    enabled: Boolean = true,
    onClick: () -> Unit,
) {
    Surface(
        onClick = onClick,
        enabled = enabled,
        shape = CircleShape,
        color = background,
        shadowElevation = 4.dp,
        modifier = Modifier
            .size(size)
            .semantics { contentDescription = label },
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(size * 0.45f))
        }
    }
}
