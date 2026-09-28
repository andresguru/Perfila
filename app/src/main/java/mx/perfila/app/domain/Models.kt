package mx.perfila.app.domain

/** Qué busca la persona usuaria en la app. */
enum class UserRole { CANDIDATE, RECRUITER }

enum class WorkMode(val label: String) {
    ONSITE("Presencial"),
    HYBRID("Híbrido"),
    REMOTE("Remoto"),
}

/** Gesto sobre una tarjeta: izquierda, derecha o hacia arriba. */
enum class SwipeDecision { PASS, LIKE, SUPER }

/** Rango salarial mensual neto en MXN. */
data class SalaryRange(val min: Int, val max: Int) {
    init {
        require(min in 0..max) { "Rango salarial inválido: $min a $max" }
    }

    fun overlaps(other: SalaryRange): Boolean = min <= other.max && other.min <= max

    /** Cuánto del rango [other] queda cubierto por este rango (0.0 a 1.0). */
    fun coverageOf(other: SalaryRange): Double {
        if (!overlaps(other)) return 0.0
        val width = (other.max - other.min).toDouble()
        if (width == 0.0) return 1.0
        val start = maxOf(min, other.min)
        val end = minOf(max, other.max)
        return ((end - start) / width).coerceIn(0.0, 1.0)
    }

    fun label(): String = "${formatK(min)} a ${formatK(max)}"

    private fun formatK(value: Int): String =
        if (value % 1000 == 0) "$${value / 1000}k" else "$${"%,d".format(value)}"
}

data class Job(
    val id: String,
    val title: String,
    val company: String,
    val companyInitials: String,
    val salary: SalaryRange,
    val mode: WorkMode,
    val city: String,
    val requiredSkills: List<String>,
    val niceToHaveSkills: List<String> = emptyList(),
    val minYearsExperience: Int = 0,
    val benefits: List<String> = emptyList(),
    val responseTimeHours: Int = 48,
    val verified: Boolean = true,
    val accentArgb: Long = 0xFFC5F04A,
)

data class CandidateProfile(
    val id: String,
    val displayName: String,
    val initials: String,
    val headline: String,
    val yearsExperience: Int,
    val skills: List<String>,
    val expectedSalary: SalaryRange,
    val acceptedModes: Set<WorkMode>,
    val city: String,
    val maxCommuteMinutes: Int = 45,
    val availabilityWeeks: Int = 2,
    val verified: Boolean = false,
    val blindMode: Boolean = false,
    val accentArgb: Long = 0xFF8B76FF,
)

/** Resultado explicable del cálculo de compatibilidad. */
data class MatchResult(
    val score: Int,
    val passesHardFilters: Boolean,
    val reasons: List<String>,
    val blockers: List<String>,
)

enum class Sender { ME, THEM, SYSTEM }

data class ChatMessage(val sender: Sender, val text: String, val sentAtMillis: Long)

data class Match(
    val id: String,
    val job: Job,
    val candidate: CandidateProfile,
    val score: Int,
    val createdAtMillis: Long,
    val messages: List<ChatMessage> = emptyList(),
) {
    /** Garantía anti-ghosting: la empresa tiene 72 h para escribir. */
    fun responseDeadlineMillis(): Long = createdAtMillis + RESPONSE_WINDOW_MILLIS

    companion object {
        const val RESPONSE_WINDOW_MILLIS: Long = 72L * 60 * 60 * 1000
    }
}
