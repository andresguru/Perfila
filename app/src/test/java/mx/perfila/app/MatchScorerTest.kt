package mx.perfila.app

import mx.perfila.app.data.SampleData
import mx.perfila.app.domain.MatchScorer
import mx.perfila.app.domain.SalaryRange
import mx.perfila.app.domain.WorkMode
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class MatchScorerTest {

    private val candidate = SampleData.demoCandidate
    private val job = SampleData.demoRecruiterJob

    @Test
    fun salaryRangesThatDoNotOverlapFailHardFilter() {
        val result = MatchScorer.score(
            job.copy(salary = SalaryRange(10_000, 15_000)),
            candidate,
            commuteMinutes = 10,
        )
        assertFalse(result.passesHardFilters)
        assertEquals(0, result.score)
        assertTrue(result.blockers.any { it.contains("salario", ignoreCase = true) })
    }

    @Test
    fun unacceptedWorkModeFailsHardFilter() {
        val remoteOnly = candidate.copy(acceptedModes = setOf(WorkMode.REMOTE))
        val result = MatchScorer.score(job.copy(mode = WorkMode.ONSITE), remoteOnly, commuteMinutes = 10)
        assertFalse(result.passesHardFilters)
    }

    @Test
    fun commuteAboveLimitFailsButRemoteIgnoresCommute() {
        val onsite = MatchScorer.score(job.copy(mode = WorkMode.ONSITE), candidate, commuteMinutes = 90)
        assertFalse(onsite.passesHardFilters)
        val remote = MatchScorer.score(job.copy(mode = WorkMode.REMOTE), candidate, commuteMinutes = 90)
        assertTrue(remote.passesHardFilters)
    }

    @Test
    fun strongProfileScoresHigherThanWeakProfile() {
        val strong = MatchScorer.score(job, candidate, commuteMinutes = 10)
        val weak = MatchScorer.score(
            job,
            candidate.copy(skills = listOf("Excel"), yearsExperience = 1, availabilityWeeks = 8),
            commuteMinutes = 30,
        )
        assertTrue(strong.passesHardFilters && weak.passesHardFilters)
        assertTrue("strong=${strong.score} weak=${weak.score}", strong.score > weak.score)
        assertTrue(strong.score in 0..100)
    }

    @Test
    fun skillMatchingIsCaseInsensitiveAndExplained() {
        val result = MatchScorer.score(
            job.copy(requiredSkills = listOf("sql", "POWER BI")),
            candidate,
            commuteMinutes = 10,
        )
        assertTrue(result.reasons.contains("2 de 2 habilidades clave"))
    }

    @Test
    fun salaryCoverage() {
        val offer = SalaryRange(30_000, 40_000)
        assertEquals(1.0, offer.coverageOf(SalaryRange(32_000, 38_000)), 0.0001)
        assertEquals(0.5, offer.coverageOf(SalaryRange(35_000, 45_000)), 0.0001)
        assertEquals(0.0, offer.coverageOf(SalaryRange(41_000, 50_000)), 0.0001)
    }
}
