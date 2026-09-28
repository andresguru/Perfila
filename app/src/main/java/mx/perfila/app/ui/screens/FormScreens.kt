package mx.perfila.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RangeSlider
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import mx.perfila.app.domain.CandidateProfile
import mx.perfila.app.domain.Job
import mx.perfila.app.domain.SalaryRange
import mx.perfila.app.domain.WorkMode
import mx.perfila.app.ui.components.SectionLabel
import mx.perfila.app.ui.theme.PerfilaColors
import kotlin.math.roundToInt

private const val SALARY_MIN = 10_000f
private const val SALARY_MAX = 120_000f
private const val SALARY_STEP = 1_000f

private fun Float.toThousands(): Int = ((this / SALARY_STEP).roundToInt() * SALARY_STEP).toInt()

private fun parseSkills(text: String): List<String> =
    text.split(",").map { it.trim() }.filter { it.isNotEmpty() }.distinctBy { it.lowercase() }

/** Paso de onboarding del candidato: experiencia y expectativas. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CandidateOnboardingScreen(
    initial: CandidateProfile,
    onDone: (CandidateProfile) -> Unit,
) {
    var name by rememberSaveable { mutableStateOf(initial.displayName) }
    var headline by rememberSaveable { mutableStateOf(initial.headline) }
    var years by remember { mutableFloatStateOf(initial.yearsExperience.toFloat()) }
    var skills by rememberSaveable { mutableStateOf(initial.skills.joinToString(", ")) }
    var salary by remember {
        mutableStateOf(initial.expectedSalary.min.toFloat()..initial.expectedSalary.max.toFloat())
    }
    var modes by remember { mutableStateOf(initial.acceptedModes) }
    var commute by remember { mutableFloatStateOf(initial.maxCommuteMinutes.toFloat()) }
    var city by rememberSaveable { mutableStateOf(initial.city) }

    FormScaffold(
        step = "Paso 3 de 4",
        progress = 0.75f,
        title = "Tu experiencia y lo que esperas",
        cta = "Empezar a descubrir vacantes",
        ctaEnabled = name.isNotBlank() && modes.isNotEmpty(),
        onCta = {
            val parts = name.trim().split(" ").filter { it.isNotEmpty() }
            onDone(
                initial.copy(
                    displayName = name.trim(),
                    initials = parts.take(2).joinToString("") { it.first().uppercase() },
                    headline = headline.trim(),
                    yearsExperience = years.roundToInt(),
                    skills = parseSkills(skills),
                    expectedSalary = SalaryRange(salary.start.toThousands(), salary.endInclusive.toThousands()),
                    acceptedModes = modes,
                    maxCommuteMinutes = commute.roundToInt(),
                    city = city.trim(),
                ),
            )
        },
    ) {
        OutlinedTextField(name, { name = it }, label = { Text("Nombre") }, singleLine = true, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(headline, { headline = it }, label = { Text("Puesto actual o deseado") }, singleLine = true, modifier = Modifier.fillMaxWidth())

        FormCard {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                SectionLabel("Años de experiencia")
                Text("${years.roundToInt()}", style = MaterialTheme.typography.titleMedium)
            }
            Slider(value = years, onValueChange = { years = it }, valueRange = 0f..25f, steps = 24)
        }

        OutlinedTextField(
            skills, { skills = it },
            label = { Text("Habilidades (separadas por coma)") },
            modifier = Modifier.fillMaxWidth(),
        )

        FormCard {
            SectionLabel("Salario mensual neto esperado")
            Text(
                "$${"%,d".format(salary.start.toThousands())} a $${"%,d".format(salary.endInclusive.toThousands())} MXN",
                style = MaterialTheme.typography.headlineSmall,
            )
            RangeSlider(value = salary, onValueChange = { salary = it }, valueRange = SALARY_MIN..SALARY_MAX)
            Text(
                "Solo te mostramos vacantes cuyo rango se cruza con el tuyo.",
                style = MaterialTheme.typography.bodyMedium,
                color = PerfilaColors.Slate,
            )
        }

        SectionLabel("Modalidad (elige varias)")
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            WorkMode.entries.forEach { mode ->
                val on = mode in modes
                FilterChip(
                    selected = on,
                    onClick = { modes = if (on) modes - mode else modes + mode },
                    label = { Text(mode.label) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = PerfilaColors.Ink,
                        selectedLabelColor = PerfilaColors.Lime,
                    ),
                )
            }
        }

        OutlinedTextField(city, { city = it }, label = { Text("Ciudad") }, singleLine = true, modifier = Modifier.fillMaxWidth())

        FormCard {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                SectionLabel("Traslado máximo")
                Text("${commute.roundToInt()} min", style = MaterialTheme.typography.titleMedium)
            }
            Slider(value = commute, onValueChange = { commute = it }, valueRange = 10f..90f, steps = 15)
        }
    }
}

/** Publicación de vacante para reclutadores. El rango salarial es obligatorio. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PostJobScreen(
    initial: Job,
    onPublish: (Job) -> Unit,
) {
    var title by rememberSaveable { mutableStateOf(initial.title) }
    var company by rememberSaveable { mutableStateOf(initial.company) }
    var city by rememberSaveable { mutableStateOf(initial.city) }
    var required by rememberSaveable { mutableStateOf(initial.requiredSkills.joinToString(", ")) }
    var nice by rememberSaveable { mutableStateOf(initial.niceToHaveSkills.joinToString(", ")) }
    var salary by remember { mutableStateOf(initial.salary.min.toFloat()..initial.salary.max.toFloat()) }
    var mode by remember { mutableStateOf(initial.mode) }
    var years by remember { mutableFloatStateOf(initial.minYearsExperience.toFloat()) }

    FormScaffold(
        step = "Vacante",
        progress = 1f,
        title = "Publica tu vacante",
        cta = "Publicar y empezar a descubrir talento",
        ctaEnabled = title.isNotBlank() && company.isNotBlank(),
        onCta = {
            val initials = company.trim().split(" ").filter { it.isNotEmpty() }.take(2)
                .joinToString("") { it.first().uppercase() }
            onPublish(
                initial.copy(
                    id = "job-mine",
                    title = title.trim(),
                    company = company.trim(),
                    companyInitials = initials,
                    city = city.trim(),
                    mode = mode,
                    salary = SalaryRange(salary.start.toThousands(), salary.endInclusive.toThousands()),
                    requiredSkills = parseSkills(required),
                    niceToHaveSkills = parseSkills(nice),
                    minYearsExperience = years.roundToInt(),
                ),
            )
        },
    ) {
        OutlinedTextField(title, { title = it }, label = { Text("Puesto") }, singleLine = true, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(company, { company = it }, label = { Text("Empresa") }, singleLine = true, modifier = Modifier.fillMaxWidth())

        FormCard(highlight = true) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                SectionLabel("Rango salarial mensual neto")
                Text("OBLIGATORIO", style = MaterialTheme.typography.labelSmall, color = PerfilaColors.VioletDark)
            }
            Text(
                "$${"%,d".format(salary.start.toThousands())} a $${"%,d".format(salary.endInclusive.toThousands())} MXN",
                style = MaterialTheme.typography.headlineSmall,
            )
            RangeSlider(value = salary, onValueChange = { salary = it }, valueRange = SALARY_MIN..SALARY_MAX)
            Text(
                "Sin salario no hay publicación. Así llegan solo perfiles que sí aceptarían.",
                style = MaterialTheme.typography.bodyMedium,
                color = PerfilaColors.Slate,
            )
        }

        SectionLabel("Modalidad")
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            WorkMode.entries.forEach { m ->
                FilterChip(
                    selected = m == mode,
                    onClick = { mode = m },
                    label = { Text(m.label) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = PerfilaColors.Ink,
                        selectedLabelColor = PerfilaColors.Lime,
                    ),
                )
            }
        }
        OutlinedTextField(city, { city = it }, label = { Text("Ciudad o zona") }, singleLine = true, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(required, { required = it }, label = { Text("Habilidades imprescindibles (coma)") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(nice, { nice = it }, label = { Text("Habilidades deseables (coma)") }, modifier = Modifier.fillMaxWidth())
        FormCard {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                SectionLabel("Experiencia mínima")
                Text("${years.roundToInt()} años", style = MaterialTheme.typography.titleMedium)
            }
            Slider(value = years, onValueChange = { years = it }, valueRange = 0f..15f, steps = 14)
        }
    }
}

@Composable
private fun FormScaffold(
    step: String,
    progress: Float,
    title: String,
    cta: String,
    ctaEnabled: Boolean,
    onCta: () -> Unit,
    content: @Composable () -> Unit,
) {
    Column(
        Modifier
            .fillMaxSize()
            .safeDrawingPadding(),
    ) {
        Column(
            Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 22.dp, vertical = 20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier.weight(1f),
                    color = PerfilaColors.Violet,
                    trackColor = PerfilaColors.Line,
                )
                Text(step, style = MaterialTheme.typography.labelMedium, color = PerfilaColors.Slate)
            }
            Text(title, style = MaterialTheme.typography.headlineLarge)
            content()
        }
        Button(
            onClick = onCta,
            enabled = ctaEnabled,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 22.dp, vertical = 14.dp)
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(containerColor = PerfilaColors.Violet, contentColor = Color.White),
        ) { Text(cta, style = MaterialTheme.typography.labelLarge) }
    }
}

@Composable
private fun FormCard(highlight: Boolean = false, content: @Composable () -> Unit) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = PerfilaColors.White,
        border = if (highlight) androidx.compose.foundation.BorderStroke(2.dp, PerfilaColors.Violet) else null,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) { content() }
    }
}
