package mx.perfila.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import mx.perfila.app.ScoredCandidate
import mx.perfila.app.ScoredJob
import mx.perfila.app.domain.WorkMode
import mx.perfila.app.ui.theme.PerfilaColors

private val CardShape = RoundedCornerShape(28.dp)

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun JobCard(item: ScoredJob, modifier: Modifier = Modifier) {
    val job = item.job
    Surface(modifier.fillMaxSize(), shape = CardShape, color = PerfilaColors.White, shadowElevation = 6.dp) {
        Column(Modifier.fillMaxSize()) {
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(120.dp)
                    .background(job.accentArgb.asColor())
                    .padding(18.dp),
            ) {
                InitialsAvatar(job.companyInitials, PerfilaColors.White, 58.dp, Modifier.align(Alignment.TopStart))
                ScoreBadge(item.result.score, Modifier.align(Alignment.TopEnd))
            }
            Column(
                Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Column {
                    Text(job.title, style = MaterialTheme.typography.headlineMedium)
                    Text(
                        job.company + if (job.verified) " · empresa verificada" else "",
                        style = MaterialTheme.typography.bodyMedium,
                        color = PerfilaColors.Slate,
                    )
                }
                Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(job.salary.label(), style = MaterialTheme.typography.headlineSmall)
                    Text("al mes", style = MaterialTheme.typography.bodyMedium, color = PerfilaColors.Slate)
                }
                FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Pill(if (job.mode == WorkMode.REMOTE) job.city else "${job.mode.label} · ${job.city}")
                    item.commuteMinutes?.takeIf { job.mode != WorkMode.REMOTE }?.let { Pill("A $it min") }
                    job.requiredSkills.forEach { Pill(it) }
                }
                Column(
                    Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(PerfilaColors.Bone)
                        .padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(7.dp),
                ) {
                    SectionLabel("Por qué hacen match")
                    item.result.reasons.forEach { ReasonRow(it) }
                }
                if (job.benefits.isNotEmpty()) {
                    Text(
                        job.benefits.joinToString(" · "),
                        style = MaterialTheme.typography.bodyMedium,
                        color = PerfilaColors.Slate,
                    )
                }
                Text(
                    "Responde en ~${job.responseTimeHours} h",
                    style = MaterialTheme.typography.labelMedium,
                    color = PerfilaColors.VioletDark,
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CandidateCard(item: ScoredCandidate, modifier: Modifier = Modifier) {
    val c = item.candidate
    Surface(modifier.fillMaxSize(), shape = CardShape, color = PerfilaColors.White, shadowElevation = 6.dp) {
        Column(Modifier.fillMaxSize()) {
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(170.dp)
                    .background(c.accentArgb.asColor())
                    .padding(18.dp),
            ) {
                if (c.verified) {
                    Pill("Identidad verificada", Modifier.align(Alignment.TopStart), PerfilaColors.White, PerfilaColors.Ink)
                }
                ScoreBadge(item.result.score, Modifier.align(Alignment.TopEnd))
                Text(
                    if (c.blindMode) "Modo ciego" else c.initials,
                    modifier = Modifier.align(Alignment.BottomStart),
                    style = if (c.blindMode) MaterialTheme.typography.headlineMedium else MaterialTheme.typography.displayLarge,
                    color = PerfilaColors.Ink,
                )
            }
            Column(
                Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Column {
                    Text(c.displayName, style = MaterialTheme.typography.headlineMedium)
                    Text(
                        "${c.headline} · ${c.yearsExperience} años de experiencia",
                        style = MaterialTheme.typography.bodyMedium,
                        color = PerfilaColors.Slate,
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Fact("Espera", c.expectedSalary.label(), Modifier.weight(1f))
                    Fact("Disponible", availabilityLabel(c.availabilityWeeks), Modifier.weight(1f))
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Fact("Modalidad", c.acceptedModes.sortedBy { it.ordinal }.joinToString(", ") { it.label }, Modifier.weight(1f))
                    Fact("Traslado", item.commuteMinutes?.let { "$it min" } ?: c.city, Modifier.weight(1f))
                }
                FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    c.skills.forEach { Pill(it) }
                }
                Column(verticalArrangement = Arrangement.spacedBy(7.dp)) {
                    SectionLabel("Por qué hacen match")
                    item.result.reasons.forEach { ReasonRow(it) }
                }
            }
        }
    }
}

@Composable
private fun Fact(label: String, value: String, modifier: Modifier = Modifier) {
    Column(
        modifier
            .clip(RoundedCornerShape(14.dp))
            .background(PerfilaColors.Bone)
            .padding(horizontal = 12.dp, vertical = 10.dp),
    ) {
        SectionLabel(label)
        Text(value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
    }
}

fun availabilityLabel(weeks: Int): String = when {
    weeks <= 0 -> "Inmediata"
    weeks == 1 -> "1 semana"
    else -> "$weeks semanas"
}
