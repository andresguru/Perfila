package mx.perfila.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

/** Paleta de marca Perfila. */
object PerfilaColors {
    val Violet = Color(0xFF5B3DF5)
    val VioletDark = Color(0xFF3F24C9)
    val VioletSoft = Color(0xFFEFEBFF)
    val Lime = Color(0xFFC5F04A)
    val Mandarina = Color(0xFFFF6A3D)
    val Ink = Color(0xFF16132E)
    val InkSoft = Color(0xFF26224A)
    val Bone = Color(0xFFF6F4EE)
    val Slate = Color(0xFF5E5A72)
    val Line = Color(0xFFE3DFEF)
    val White = Color(0xFFFFFFFF)
}

private val LightColors = lightColorScheme(
    primary = PerfilaColors.Violet,
    onPrimary = PerfilaColors.White,
    primaryContainer = PerfilaColors.VioletSoft,
    onPrimaryContainer = PerfilaColors.VioletDark,
    secondary = PerfilaColors.Lime,
    onSecondary = PerfilaColors.Ink,
    tertiary = PerfilaColors.Mandarina,
    onTertiary = PerfilaColors.Ink,
    background = PerfilaColors.Bone,
    onBackground = PerfilaColors.Ink,
    surface = PerfilaColors.White,
    onSurface = PerfilaColors.Ink,
    surfaceVariant = PerfilaColors.VioletSoft,
    onSurfaceVariant = PerfilaColors.Slate,
    outline = PerfilaColors.Line,
)

// TODO: sustituir por Bricolage Grotesque (títulos) y Figtree (texto) como recursos de fuente.
private val Display = FontFamily.SansSerif
private val Body = FontFamily.SansSerif

private val PerfilaTypography = Typography(
    displayLarge = TextStyle(fontFamily = Display, fontWeight = FontWeight.ExtraBold, fontSize = 56.sp, lineHeight = 56.sp, letterSpacing = (-1.5).sp),
    displayMedium = TextStyle(fontFamily = Display, fontWeight = FontWeight.ExtraBold, fontSize = 40.sp, lineHeight = 42.sp, letterSpacing = (-1).sp),
    headlineLarge = TextStyle(fontFamily = Display, fontWeight = FontWeight.ExtraBold, fontSize = 32.sp, lineHeight = 36.sp, letterSpacing = (-0.5).sp),
    headlineMedium = TextStyle(fontFamily = Display, fontWeight = FontWeight.ExtraBold, fontSize = 26.sp, lineHeight = 30.sp),
    headlineSmall = TextStyle(fontFamily = Display, fontWeight = FontWeight.Bold, fontSize = 22.sp, lineHeight = 26.sp),
    titleLarge = TextStyle(fontFamily = Body, fontWeight = FontWeight.Bold, fontSize = 18.sp, lineHeight = 24.sp),
    titleMedium = TextStyle(fontFamily = Body, fontWeight = FontWeight.Bold, fontSize = 16.sp, lineHeight = 22.sp),
    bodyLarge = TextStyle(fontFamily = Body, fontSize = 16.sp, lineHeight = 22.sp),
    bodyMedium = TextStyle(fontFamily = Body, fontSize = 14.sp, lineHeight = 20.sp),
    labelLarge = TextStyle(fontFamily = Body, fontWeight = FontWeight.Bold, fontSize = 15.sp),
    labelMedium = TextStyle(fontFamily = Body, fontWeight = FontWeight.SemiBold, fontSize = 13.sp),
    labelSmall = TextStyle(fontFamily = Body, fontWeight = FontWeight.Bold, fontSize = 11.sp, letterSpacing = 0.8.sp),
)

@Composable
fun PerfilaTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = LightColors,
        typography = PerfilaTypography,
        content = content,
    )
}
