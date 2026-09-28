package mx.perfila.app.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import mx.perfila.app.ui.theme.PerfilaColors

/** Isotipo: tarjeta de perfil con palomita y punto. */
@Composable
fun PerfilaMark(size: Dp, modifier: Modifier = Modifier, background: Color = PerfilaColors.Violet) {
    Canvas(modifier.size(size)) {
        val s = this.size.minDimension
        drawRoundRect(background, cornerRadius = CornerRadius(s * 0.28f))
        val cardInset = s * 0.2f
        drawRoundRect(
            color = PerfilaColors.Ink,
            topLeft = Offset(cardInset, cardInset),
            size = Size(s - cardInset * 2, s - cardInset * 2),
            cornerRadius = CornerRadius(s * 0.08f),
        )
        val check = Path().apply {
            moveTo(s * 0.33f, s * 0.51f)
            lineTo(s * 0.46f, s * 0.63f)
            lineTo(s * 0.64f, s * 0.40f)
        }
        drawPath(
            check,
            color = PerfilaColors.Lime,
            style = Stroke(width = s * 0.07f, cap = StrokeCap.Round, join = StrokeJoin.Round),
        )
        drawCircle(PerfilaColors.Lime, radius = s * 0.045f, center = Offset(s * 0.68f, s * 0.31f))
    }
}

@Composable
fun PerfilaWordmark(modifier: Modifier = Modifier, color: Color = PerfilaColors.Ink, fontSize: TextUnit = 28.sp) {
    Text(
        "perfila",
        modifier = modifier,
        color = color,
        fontSize = fontSize,
        fontWeight = FontWeight.ExtraBold,
        letterSpacing = (-1).sp,
    )
}

@Composable
fun Pill(
    text: String,
    modifier: Modifier = Modifier,
    background: Color = PerfilaColors.VioletSoft,
    textColor: Color = PerfilaColors.VioletDark,
) {
    Surface(modifier = modifier, color = background, shape = RoundedCornerShape(50)) {
        Text(
            text,
            modifier = Modifier.padding(horizontal = 11.dp, vertical = 6.dp),
            style = MaterialTheme.typography.labelMedium,
            color = textColor,
        )
    }
}

@Composable
fun ScoreBadge(score: Int, modifier: Modifier = Modifier) {
    Pill("$score% match", modifier, background = PerfilaColors.Ink, textColor = PerfilaColors.Lime)
}

@Composable
fun InitialsAvatar(
    initials: String,
    background: Color,
    size: Dp,
    modifier: Modifier = Modifier,
    textColor: Color = PerfilaColors.Ink,
) {
    Box(
        modifier
            .size(size)
            .clip(RoundedCornerShape(size * 0.3f))
            .background(background),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            initials,
            color = textColor,
            fontWeight = FontWeight.ExtraBold,
            fontSize = (size.value * 0.36f).sp,
        )
    }
}

@Composable
fun SectionLabel(text: String, modifier: Modifier = Modifier) {
    Text(
        text.uppercase(),
        modifier = modifier,
        style = MaterialTheme.typography.labelSmall,
        color = PerfilaColors.Slate,
    )
}

@Composable
fun ReasonRow(text: String) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
        Box(
            Modifier
                .size(8.dp)
                .clip(RoundedCornerShape(50))
                .background(PerfilaColors.Violet),
        )
        Text(text, style = MaterialTheme.typography.bodyMedium)
    }
}

fun Long.asColor(): Color = Color(this)
