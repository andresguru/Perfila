package mx.perfila.app.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import mx.perfila.app.domain.UserRole
import mx.perfila.app.ui.components.PerfilaMark
import mx.perfila.app.ui.components.PerfilaWordmark
import mx.perfila.app.ui.theme.PerfilaColors

@Composable
fun WelcomeScreen(onStart: () -> Unit) {
    Surface(Modifier.fillMaxSize(), color = PerfilaColors.Violet) {
        Column(
            Modifier
                .fillMaxSize()
                .safeDrawingPadding()
                .padding(horizontal = 28.dp, vertical = 32.dp),
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("México · LATAM", color = Color(0xFFE4DEFF), style = MaterialTheme.typography.labelMedium)
                Text("Remoto o presencial", color = Color(0xFFE4DEFF), style = MaterialTheme.typography.labelMedium)
            }
            Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(18.dp)) {
                PerfilaMark(size = 120.dp, background = PerfilaColors.Ink)
                PerfilaWordmark(color = Color.White, fontSize = 64.sp)
                Text(
                    "El perfil correcto para el puesto correcto.",
                    color = PerfilaColors.Lime,
                    style = MaterialTheme.typography.titleLarge,
                )
            }
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Button(
                    onClick = onStart,
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PerfilaColors.Lime, contentColor = PerfilaColors.Ink),
                ) { Text("Empezar", style = MaterialTheme.typography.labelLarge) }
                OutlinedButton(
                    onClick = onStart,
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    border = BorderStroke(1.5.dp, Color(0xFF8B76FF)),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                ) { Text("Ya tengo cuenta") }
            }
        }
    }
}

@Composable
fun RoleSelectScreen(onContinue: (UserRole) -> Unit) {
    var role by rememberSaveable { mutableStateOf(UserRole.CANDIDATE) }
    var phone by rememberSaveable { mutableStateOf("") }

    Column(
        Modifier
            .fillMaxSize()
            .safeDrawingPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 24.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            PerfilaMark(size = 36.dp)
            PerfilaWordmark(fontSize = 24.sp)
        }
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text("¿Qué buscas hoy?", style = MaterialTheme.typography.headlineLarge)
            Text("Puedes cambiar de modo cuando quieras.", color = PerfilaColors.Slate)
        }
        RoleOption(
            title = "Busco trabajo",
            subtitle = "Desliza vacantes con salario visible, remotas o cerca de ti.",
            badgeColor = PerfilaColors.Lime,
            selected = role == UserRole.CANDIDATE,
            onClick = { role = UserRole.CANDIDATE },
        )
        RoleOption(
            title = "Busco talento",
            subtitle = "Publica vacantes y filtra perfiles compatibles con un gesto.",
            badgeColor = PerfilaColors.Violet,
            selected = role == UserRole.RECRUITER,
            onClick = { role = UserRole.RECRUITER },
        )
        Spacer(Modifier.height(4.dp))
        OutlinedTextField(
            value = phone,
            onValueChange = { phone = it.filter(Char::isDigit).take(10) },
            label = { Text("Tu celular (10 dígitos)") },
            prefix = { Text("+52 ") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
            modifier = Modifier.fillMaxWidth(),
        )
        Button(
            onClick = { onContinue(role) },
            modifier = Modifier.fillMaxWidth().height(54.dp),
            colors = ButtonDefaults.buttonColors(containerColor = PerfilaColors.Ink, contentColor = Color.White),
        ) { Text("Enviar código por WhatsApp") }
        OutlinedButton(onClick = { onContinue(role) }, modifier = Modifier.fillMaxWidth().height(52.dp)) {
            Text("Continuar con Google", color = PerfilaColors.Ink)
        }
        Text(
            "Demo: la verificación por WhatsApp y Google se conecta en el siguiente sprint. " +
                "Al continuar aceptas los Términos y el Aviso de privacidad.",
            style = MaterialTheme.typography.bodyMedium,
            color = PerfilaColors.Slate,
        )
    }
}

@Composable
private fun RoleOption(
    title: String,
    subtitle: String,
    badgeColor: Color,
    selected: Boolean,
    onClick: () -> Unit,
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(22.dp),
        color = if (selected) PerfilaColors.White else PerfilaColors.Bone,
        border = BorderStroke(2.dp, if (selected) PerfilaColors.Violet else PerfilaColors.Line),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(Modifier.padding(18.dp), horizontalArrangement = Arrangement.spacedBy(14.dp), verticalAlignment = Alignment.CenterVertically) {
            Surface(Modifier.size(48.dp), shape = RoundedCornerShape(14.dp), color = badgeColor) { Box {} }
            Column(Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.titleLarge)
                Text(subtitle, style = MaterialTheme.typography.bodyMedium, color = PerfilaColors.Slate)
            }
        }
    }
}
