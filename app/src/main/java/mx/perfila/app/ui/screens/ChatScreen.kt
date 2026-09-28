package mx.perfila.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.unit.dp
import mx.perfila.app.domain.Match
import mx.perfila.app.domain.Sender
import mx.perfila.app.domain.UserRole
import mx.perfila.app.ui.components.InitialsAvatar
import mx.perfila.app.ui.components.Pill
import mx.perfila.app.ui.components.asColor
import mx.perfila.app.ui.theme.PerfilaColors

@Composable
fun ChatScreen(
    match: Match?,
    role: UserRole,
    onBack: () -> Unit,
    onSend: (String) -> Unit,
) {
    var draft by rememberSaveable { mutableStateOf("") }

    Column(
        Modifier
            .fillMaxSize()
            .safeDrawingPadding(),
    ) {
        Surface(color = PerfilaColors.White, modifier = Modifier.fillMaxWidth()) {
            Row(
                Modifier.padding(horizontal = 8.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Regresar")
                }
                if (match != null) {
                    val isCandidate = role == UserRole.CANDIDATE
                    InitialsAvatar(
                        if (isCandidate) match.job.companyInitials else match.candidate.initials,
                        if (isCandidate) match.job.accentArgb.asColor() else match.candidate.accentArgb.asColor(),
                        44.dp,
                    )
                    Column {
                        Text(
                            if (isCandidate) match.job.company else match.candidate.displayName,
                            style = MaterialTheme.typography.titleMedium,
                        )
                        Text(match.job.title, style = MaterialTheme.typography.bodyMedium, color = PerfilaColors.Slate)
                    }
                }
            }
        }

        if (match == null) {
            Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                Text("Este match ya no está disponible.", color = PerfilaColors.Slate)
            }
        } else {
            LazyColumn(
                Modifier.weight(1f).fillMaxWidth(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                item {
                    Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        Pill("Match · ${match.score}% compatible")
                    }
                }
                items(match.messages) { msg ->
                    when (msg.sender) {
                        Sender.SYSTEM -> Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                            Text(msg.text, style = MaterialTheme.typography.bodyMedium, color = PerfilaColors.Slate)
                        }
                        else -> {
                            val mine = msg.sender == Sender.ME
                            Box(Modifier.fillMaxWidth(), contentAlignment = if (mine) Alignment.CenterEnd else Alignment.CenterStart) {
                                Surface(
                                    color = if (mine) PerfilaColors.Violet else PerfilaColors.White,
                                    shape = RoundedCornerShape(
                                        topStart = 18.dp, topEnd = 18.dp,
                                        bottomStart = if (mine) 18.dp else 6.dp,
                                        bottomEnd = if (mine) 6.dp else 18.dp,
                                    ),
                                    modifier = Modifier.widthIn(max = 300.dp),
                                ) {
                                    Text(
                                        msg.text,
                                        color = if (mine) Color.White else PerfilaColors.Ink,
                                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 11.dp),
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        Surface(color = PerfilaColors.White, modifier = Modifier.fillMaxWidth()) {
            Row(
                Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                OutlinedTextField(
                    value = draft,
                    onValueChange = { draft = it },
                    placeholder = { Text("Escribe un mensaje") },
                    modifier = Modifier.weight(1f),
                    maxLines = 4,
                    enabled = match != null,
                )
                IconButton(
                    onClick = {
                        onSend(draft)
                        draft = ""
                    },
                    enabled = match != null && draft.isNotBlank(),
                ) {
                    Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Enviar", tint = PerfilaColors.Violet)
                }
            }
        }
    }
}
