package mx.perfila.app.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import mx.perfila.app.PerfilaUiState
import mx.perfila.app.ScoredCandidate
import mx.perfila.app.ScoredJob
import mx.perfila.app.domain.Match
import mx.perfila.app.domain.Sender
import mx.perfila.app.domain.SwipeDecision
import mx.perfila.app.domain.UserRole
import mx.perfila.app.ui.components.CandidateCard
import mx.perfila.app.ui.components.InitialsAvatar
import mx.perfila.app.ui.components.JobCard
import mx.perfila.app.ui.components.PerfilaMark
import mx.perfila.app.ui.components.Pill
import mx.perfila.app.ui.components.SectionLabel
import mx.perfila.app.ui.components.SwipeDeck
import mx.perfila.app.ui.components.asColor
import mx.perfila.app.ui.theme.PerfilaColors
import kotlin.math.max

private enum class Tab(val label: String) { DISCOVER("Descubrir"), MATCHES("Matches"), PROFILE("Perfil") }

@Composable
fun MainScreen(
    state: PerfilaUiState,
    onSwipeJob: (ScoredJob, SwipeDecision) -> Unit,
    onSwipeCandidate: (ScoredCandidate, SwipeDecision) -> Unit,
    onOpenChat: (String) -> Unit,
    onDismissMatch: () -> Unit,
    onEditProfile: () -> Unit,
    onEditJob: () -> Unit,
    onSwitchRole: (UserRole) -> Unit,
    onResetDecks: () -> Unit,
) {
    var tab by rememberSaveable { mutableIntStateOf(Tab.DISCOVER.ordinal) }

    Box(Modifier.fillMaxSize()) {
        Scaffold(
            containerColor = PerfilaColors.Bone,
            bottomBar = {
                NavigationBar(containerColor = PerfilaColors.White) {
                    Tab.entries.forEach { t ->
                        val icon = when (t) {
                            Tab.DISCOVER -> Icons.Filled.Home
                            Tab.MATCHES -> Icons.Filled.Email
                            Tab.PROFILE -> Icons.Filled.Person
                        }
                        NavigationBarItem(
                            selected = tab == t.ordinal,
                            onClick = { tab = t.ordinal },
                            icon = { Icon(icon, contentDescription = null) },
                            label = { Text(if (t == Tab.MATCHES && state.matches.isNotEmpty()) "${t.label} (${state.matches.size})" else t.label) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = PerfilaColors.Violet,
                                selectedTextColor = PerfilaColors.Violet,
                                indicatorColor = PerfilaColors.VioletSoft,
                            ),
                        )
                    }
                }
            },
        ) { padding ->
            when (Tab.entries[tab]) {
                Tab.DISCOVER -> DiscoverTab(state, padding, onSwipeJob, onSwipeCandidate, onResetDecks)
                Tab.MATCHES -> MatchesTab(state, padding, onOpenChat)
                Tab.PROFILE -> ProfileTab(state, padding, onEditProfile, onEditJob, onSwitchRole)
            }
        }

        state.pendingMatch?.let { match ->
            MatchOverlay(
                match = match,
                role = state.role,
                onMessage = {
                    onDismissMatch()
                    onOpenChat(match.id)
                },
                onKeepSwiping = onDismissMatch,
            )
        }
    }
}

@Composable
private fun DiscoverTab(
    state: PerfilaUiState,
    padding: PaddingValues,
    onSwipeJob: (ScoredJob, SwipeDecision) -> Unit,
    onSwipeCandidate: (ScoredCandidate, SwipeDecision) -> Unit,
    onResetDecks: () -> Unit,
) {
    Column(
        Modifier
            .fillMaxSize()
            .padding(padding)
            .padding(horizontal = 18.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            PerfilaMark(size = 36.dp)
            Column(Modifier.weight(1f)) {
                if (state.role == UserRole.CANDIDATE) {
                    Text("Buscando en", style = MaterialTheme.typography.labelMedium, color = PerfilaColors.Slate)
                    Text("${state.me.city} · ${state.me.acceptedModes.sortedBy { it.ordinal }.joinToString(", ") { it.label }}", style = MaterialTheme.typography.titleMedium)
                } else {
                    Text("Vacante activa · ${state.candidateDeck.size} perfiles", style = MaterialTheme.typography.labelMedium, color = PerfilaColors.Slate)
                    Text(state.myJob.title, style = MaterialTheme.typography.titleMedium)
                }
            }
            if (state.role == UserRole.CANDIDATE) {
                Pill("Destacar: ${state.superLikesLeft}")
            }
        }

        val empty: @Composable () -> Unit = {
            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Ya viste todo por hoy", style = MaterialTheme.typography.headlineSmall)
                Text(
                    "Ajusta tus filtros o vuelve más tarde para ver nuevos perfiles compatibles.",
                    color = PerfilaColors.Slate,
                    style = MaterialTheme.typography.bodyMedium,
                )
                OutlinedButton(onClick = onResetDecks) { Text("Volver a ver el mazo (demo)") }
            }
        }

        if (state.role == UserRole.CANDIDATE) {
            SwipeDeck(
                items = state.jobDeck,
                itemKey = { it.job.id },
                onSwiped = onSwipeJob,
                modifier = Modifier.weight(1f),
                superEnabled = state.superLikesLeft > 0,
                emptyContent = empty,
            ) { JobCard(it) }
        } else {
            SwipeDeck(
                items = state.candidateDeck,
                itemKey = { it.candidate.id },
                onSwiped = onSwipeCandidate,
                modifier = Modifier.weight(1f),
                emptyContent = empty,
            ) { CandidateCard(it) }
        }
    }
}

@Composable
private fun MatchesTab(state: PerfilaUiState, padding: PaddingValues, onOpenChat: (String) -> Unit) {
    val now = System.currentTimeMillis()
    LazyColumn(
        Modifier
            .fillMaxSize()
            .padding(padding),
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        item { Text("Matches", style = MaterialTheme.typography.headlineLarge) }
        if (state.matches.isEmpty()) {
            item {
                Text(
                    "Aún no tienes matches. Cuando ambos digan que sí, aparecerán aquí.",
                    color = PerfilaColors.Slate,
                    modifier = Modifier.padding(top = 8.dp),
                )
            }
        }
        items(state.matches, key = { it.id }) { match ->
            val counterpart = if (state.role == UserRole.CANDIDATE) match.job.company else match.candidate.displayName
            val initials = if (state.role == UserRole.CANDIDATE) match.job.companyInitials else match.candidate.initials
            val color = if (state.role == UserRole.CANDIDATE) match.job.accentArgb.asColor() else match.candidate.accentArgb.asColor()
            val companyWrote = match.messages.any { it.sender == Sender.THEM }
            val hoursLeft = max(0L, (match.responseDeadlineMillis() - now) / 3_600_000L)
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = PerfilaColors.White,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onOpenChat(match.id) },
            ) {
                Row(Modifier.padding(14.dp), horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    InitialsAvatar(initials, color, 48.dp)
                    Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                        Text(counterpart, style = MaterialTheme.typography.titleMedium)
                        Text(
                            match.messages.lastOrNull()?.text ?: match.job.title,
                            style = MaterialTheme.typography.bodyMedium,
                            color = PerfilaColors.Slate,
                            maxLines = 1,
                        )
                        if (companyWrote) {
                            Pill("${match.score}% match")
                        } else {
                            Pill("Quedan $hoursLeft h", background = Color(0xFFFFE3D8), textColor = Color(0xFF9A3412))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ProfileTab(
    state: PerfilaUiState,
    padding: PaddingValues,
    onEditProfile: () -> Unit,
    onEditJob: () -> Unit,
    onSwitchRole: (UserRole) -> Unit,
) {
    Column(
        Modifier
            .fillMaxSize()
            .padding(padding)
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Text(if (state.role == UserRole.CANDIDATE) "Tu perfil" else "Tu vacante", style = MaterialTheme.typography.headlineLarge)
        Surface(shape = RoundedCornerShape(20.dp), color = PerfilaColors.White, modifier = Modifier.fillMaxWidth()) {
            Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                if (state.role == UserRole.CANDIDATE) {
                    val me = state.me
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        InitialsAvatar(me.initials, PerfilaColors.Lime, 56.dp)
                        Column {
                            Text(me.displayName, style = MaterialTheme.typography.titleLarge)
                            Text("${me.headline} · ${me.yearsExperience} años", color = PerfilaColors.Slate)
                        }
                    }
                    SectionLabel("Expectativa")
                    Text("${me.expectedSalary.label()} al mes · traslado máx. ${me.maxCommuteMinutes} min")
                    SectionLabel("Habilidades")
                    Text(me.skills.joinToString(", "))
                    TextButton(onClick = onEditProfile) { Text("Editar perfil y expectativas") }
                } else {
                    val job = state.myJob
                    Text(job.title, style = MaterialTheme.typography.titleLarge)
                    Text("${job.company} · ${job.mode.label} · ${job.city}", color = PerfilaColors.Slate)
                    SectionLabel("Salario")
                    Text("${job.salary.label()} al mes")
                    SectionLabel("Imprescindibles")
                    Text(job.requiredSkills.joinToString(", "))
                    TextButton(onClick = onEditJob) { Text("Editar vacante") }
                }
            }
        }
        Spacer(Modifier.weight(1f))
        val other = if (state.role == UserRole.CANDIDATE) UserRole.RECRUITER else UserRole.CANDIDATE
        OutlinedButton(onClick = { onSwitchRole(other) }, modifier = Modifier.fillMaxWidth().height(52.dp)) {
            Text(if (other == UserRole.RECRUITER) "Cambiar a modo reclutador" else "Cambiar a modo candidato", color = PerfilaColors.Ink)
        }
    }
}

@Composable
private fun MatchOverlay(match: Match, role: UserRole, onMessage: () -> Unit, onKeepSwiping: () -> Unit) {
    Surface(Modifier.fillMaxSize(), color = PerfilaColors.Ink) {
        Column(
            Modifier
                .fillMaxSize()
                .padding(horizontal = 26.dp, vertical = 48.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                SectionLabel("Match nuevo")
                Text("¡Hicieron", color = PerfilaColors.Lime, fontSize = 56.sp, style = MaterialTheme.typography.displayLarge)
                Text("match!", color = PerfilaColors.Lime, fontSize = 56.sp, style = MaterialTheme.typography.displayLarge)
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                InitialsAvatar(match.candidate.initials, PerfilaColors.Violet, 120.dp, textColor = Color.White)
                Box(Modifier.size(56.dp), contentAlignment = Alignment.Center) {
                    PerfilaMark(size = 56.dp, background = PerfilaColors.Lime)
                }
                InitialsAvatar(match.job.companyInitials, PerfilaColors.Lime, 120.dp)
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    if (role == UserRole.CANDIDATE) "Tú y ${match.job.company} dijeron que sí a ${match.job.title}."
                    else "${match.candidate.displayName} también mostró interés en ${match.job.title}.",
                    color = Color.White,
                    style = MaterialTheme.typography.bodyLarge,
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Pill("${match.score}% compatible", background = PerfilaColors.InkSoft, textColor = Color.White)
                    Pill(match.job.salary.label(), background = PerfilaColors.InkSoft, textColor = Color.White)
                    Pill(match.job.mode.label, background = PerfilaColors.InkSoft, textColor = Color.White)
                }
                Surface(shape = RoundedCornerShape(16.dp), color = PerfilaColors.InkSoft) {
                    Text(
                        "Garantía de respuesta: la empresa tiene 72 h para escribir. Si no lo hace, el match se cierra y te avisamos.",
                        color = Color(0xFFE4E1F5),
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(14.dp),
                    )
                }
            }
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Button(
                    onClick = onMessage,
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PerfilaColors.Lime, contentColor = PerfilaColors.Ink),
                ) { Text("Enviar mensaje", style = MaterialTheme.typography.labelLarge) }
                TextButton(onClick = onKeepSwiping, modifier = Modifier.fillMaxWidth()) {
                    Text("Seguir descubriendo", color = Color.White)
                }
            }
        }
    }
}
