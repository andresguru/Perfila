package mx.perfila.app

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import mx.perfila.app.data.SampleData
import mx.perfila.app.domain.CandidateProfile
import mx.perfila.app.domain.ChatMessage
import mx.perfila.app.domain.Job
import mx.perfila.app.domain.Match
import mx.perfila.app.domain.MatchResult
import mx.perfila.app.domain.MatchScorer
import mx.perfila.app.domain.Sender
import mx.perfila.app.domain.SwipeDecision
import mx.perfila.app.domain.UserRole

data class ScoredJob(val job: Job, val commuteMinutes: Int?, val result: MatchResult)
data class ScoredCandidate(val candidate: CandidateProfile, val commuteMinutes: Int?, val result: MatchResult)

data class PerfilaUiState(
    val role: UserRole = UserRole.CANDIDATE,
    val me: CandidateProfile = SampleData.demoCandidate,
    val myJob: Job = SampleData.demoRecruiterJob,
    val jobDeck: List<ScoredJob> = emptyList(),
    val candidateDeck: List<ScoredCandidate> = emptyList(),
    val matches: List<Match> = emptyList(),
    val pendingMatch: Match? = null,
    val superLikesLeft: Int = 3,
)

/**
 * Estado de la app en memoria con datos de muestra.
 * Siguiente paso: sustituir SampleData por un repositorio respaldado por Supabase.
 */
class PerfilaViewModel : ViewModel() {

    /** Reloj inyectable para pruebas. */
    internal var clock: () -> Long = { System.currentTimeMillis() }

    private val seenJobs = mutableSetOf<String>()
    private val seenCandidates = mutableSetOf<String>()

    private val _state = MutableStateFlow(PerfilaUiState())
    val state: StateFlow<PerfilaUiState> = _state.asStateFlow()

    init {
        rebuildDecks()
    }

    fun setRole(role: UserRole) {
        _state.update { it.copy(role = role) }
    }

    fun updateProfile(profile: CandidateProfile) {
        _state.update { it.copy(me = profile) }
        rebuildDecks()
    }

    fun publishJob(job: Job) {
        seenCandidates.clear()
        _state.update { it.copy(myJob = job) }
        rebuildDecks()
    }

    fun swipeJob(item: ScoredJob, decision: SwipeDecision) {
        seenJobs += item.job.id
        val current = _state.value
        val usedSuper = decision == SwipeDecision.SUPER && current.superLikesLeft > 0
        // Demo: la empresa "dice que sí" cuando la compatibilidad es alta o hubo un Destacar.
        val companyLikesBack = item.result.score >= 80 || usedSuper
        val created = if (decision != SwipeDecision.PASS && companyLikesBack) {
            newMatch(item.job, current.me, item.result.score, fromCompanyFirst = true)
        } else null
        _state.update {
            it.copy(
                jobDeck = it.jobDeck.filterNot { s -> s.job.id == item.job.id },
                superLikesLeft = if (usedSuper) it.superLikesLeft - 1 else it.superLikesLeft,
                matches = if (created != null) listOf(created) + it.matches else it.matches,
                pendingMatch = created ?: it.pendingMatch,
            )
        }
    }

    fun swipeCandidate(item: ScoredCandidate, decision: SwipeDecision) {
        seenCandidates += item.candidate.id
        val current = _state.value
        // Demo: el candidato "dice que sí" cuando la compatibilidad es alta.
        val candidateLikesBack = item.result.score >= 75
        val created = if (decision != SwipeDecision.PASS && candidateLikesBack) {
            newMatch(current.myJob, item.candidate, item.result.score, fromCompanyFirst = false)
        } else null
        _state.update {
            it.copy(
                candidateDeck = it.candidateDeck.filterNot { s -> s.candidate.id == item.candidate.id },
                matches = if (created != null) listOf(created) + it.matches else it.matches,
                pendingMatch = created ?: it.pendingMatch,
            )
        }
    }

    fun dismissPendingMatch() {
        _state.update { it.copy(pendingMatch = null) }
    }

    fun sendMessage(matchId: String, text: String) {
        val clean = text.trim()
        if (clean.isEmpty()) return
        _state.update { s ->
            s.copy(matches = s.matches.map { m ->
                if (m.id == matchId) m.copy(messages = m.messages + ChatMessage(Sender.ME, clean, clock())) else m
            })
        }
    }

    fun resetDecks() {
        seenJobs.clear()
        seenCandidates.clear()
        rebuildDecks()
    }

    private fun newMatch(job: Job, candidate: CandidateProfile, score: Int, fromCompanyFirst: Boolean): Match {
        val now = clock()
        val opener = if (fromCompanyFirst) {
            ChatMessage(Sender.THEM, "¡Hola! Nos interesa tu perfil para ${job.title}. ¿Te gustaría platicar esta semana?", now)
        } else {
            ChatMessage(Sender.SYSTEM, "Hicieron match. Tienes 72 h para escribirle a ${candidate.displayName}.", now)
        }
        return Match(
            id = "${job.id}:${candidate.id}",
            job = job,
            candidate = candidate,
            score = score,
            createdAtMillis = now,
            messages = listOf(opener),
        )
    }

    private fun rebuildDecks() {
        val s = _state.value
        val jobs = SampleData.jobs
            .filterNot { (job, _) -> job.id in seenJobs }
            .map { (job, commute) -> ScoredJob(job, commute, MatchScorer.score(job, s.me, commute)) }
            .filter { it.result.passesHardFilters }
            .sortedByDescending { it.result.score }
        val candidates = SampleData.candidates
            .filterNot { (c, _) -> c.id in seenCandidates }
            .map { (c, commute) -> ScoredCandidate(c, commute, MatchScorer.score(s.myJob, c, commute)) }
            .filter { it.result.passesHardFilters }
            .sortedByDescending { it.result.score }
        _state.update { it.copy(jobDeck = jobs, candidateDeck = candidates) }
    }
}
