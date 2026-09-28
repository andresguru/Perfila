package mx.perfila.app.domain

import kotlin.math.roundToInt

/**
 * Algoritmo de match v1: reglas explicables.
 *
 * 1. Filtros duros: rangos salariales que se cruzan, modalidad aceptada y
 *    tiempo de traslado dentro del límite del candidato (si no es remoto).
 * 2. Puntaje ponderado: habilidades 30, salario 25, ubicación/modalidad 20,
 *    experiencia 15, disponibilidad 10.
 */
object MatchScorer {

    const val WEIGHT_SKILLS = 0.30
    const val WEIGHT_SALARY = 0.25
    const val WEIGHT_LOCATION = 0.20
    const val WEIGHT_EXPERIENCE = 0.15
    const val WEIGHT_AVAILABILITY = 0.10

    /**
     * @param commuteMinutes tiempo estimado de traslado entre candidato y vacante;
     * `null` si no se conoce. Se ignora en vacantes remotas.
     */
    fun score(job: Job, candidate: CandidateProfile, commuteMinutes: Int?): MatchResult {
        val blockers = mutableListOf<String>()
        val reasons = mutableListOf<String>()

        if (!job.salary.overlaps(candidate.expectedSalary)) blockers += "El salario no coincide"
        if (job.mode !in candidate.acceptedModes) blockers += "Modalidad ${job.mode.label.lowercase()} no aceptada"
        val needsCommute = job.mode != WorkMode.REMOTE
        if (needsCommute && commuteMinutes != null && commuteMinutes > candidate.maxCommuteMinutes) {
            blockers += "Traslado de $commuteMinutes min supera el límite"
        }
        if (blockers.isNotEmpty()) {
            return MatchResult(score = 0, passesHardFilters = false, reasons = emptyList(), blockers = blockers)
        }

        // Habilidades
        val have = candidate.skills.map { it.normalized() }.toSet()
        val required = job.requiredSkills.map { it.normalized() }
        val nice = job.niceToHaveSkills.map { it.normalized() }
        val requiredHit = required.count { it in have }
        val requiredCoverage = if (required.isEmpty()) 1.0 else requiredHit.toDouble() / required.size
        val niceCoverage = if (nice.isEmpty()) 1.0 else nice.count { it in have }.toDouble() / nice.size
        val skillsScore = 0.75 * requiredCoverage + 0.25 * niceCoverage
        if (required.isNotEmpty()) reasons += "$requiredHit de ${required.size} habilidades clave"

        // Salario
        val salaryScore = job.salary.coverageOf(candidate.expectedSalary).coerceAtLeast(0.4)
        reasons += if (job.salary.min >= candidate.expectedSalary.min) {
            "Salario dentro o arriba de tu rango"
        } else {
            "Salario se cruza con tu rango"
        }

        // Ubicación y modalidad
        val locationScore = when {
            job.mode == WorkMode.REMOTE -> 1.0
            commuteMinutes == null -> 0.7
            commuteMinutes <= 15 -> 1.0
            else -> {
                val limit = candidate.maxCommuteMinutes.coerceAtLeast(16)
                (1.0 - 0.5 * (commuteMinutes - 15).toDouble() / (limit - 15)).coerceIn(0.5, 1.0)
            }
        }
        reasons += when {
            job.mode == WorkMode.REMOTE -> "Remoto, como pediste"
            commuteMinutes != null -> "${job.mode.label}, a $commuteMinutes min"
            else -> "${job.mode.label} en ${job.city}"
        }

        // Experiencia
        val experienceScore = if (job.minYearsExperience <= 0) 1.0
        else (candidate.yearsExperience.toDouble() / job.minYearsExperience).coerceIn(0.0, 1.0)
        if (job.minYearsExperience > 0 && candidate.yearsExperience >= job.minYearsExperience) {
            reasons += "${candidate.yearsExperience} años de experiencia"
        }

        // Disponibilidad
        val availabilityScore = when {
            candidate.availabilityWeeks <= 0 -> 1.0
            candidate.availabilityWeeks <= 2 -> 0.8
            candidate.availabilityWeeks <= 4 -> 0.6
            else -> 0.4
        }

        val total = WEIGHT_SKILLS * skillsScore +
            WEIGHT_SALARY * salaryScore +
            WEIGHT_LOCATION * locationScore +
            WEIGHT_EXPERIENCE * experienceScore +
            WEIGHT_AVAILABILITY * availabilityScore

        return MatchResult(
            score = (total * 100).roundToInt().coerceIn(0, 100),
            passesHardFilters = true,
            reasons = reasons,
            blockers = emptyList(),
        )
    }

    private fun String.normalized(): String = trim().lowercase()
}
