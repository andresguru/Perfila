package mx.perfila.app.data

import mx.perfila.app.domain.CandidateProfile
import mx.perfila.app.domain.Job
import mx.perfila.app.domain.SalaryRange
import mx.perfila.app.domain.WorkMode

/**
 * Datos de muestra para demo y desarrollo sin backend.
 * Empresas y personas ficticias.
 */
object SampleData {

    val demoCandidate = CandidateProfile(
        id = "me",
        displayName = "Mariana Solís",
        initials = "MS",
        headline = "Analista de Datos",
        yearsExperience = 6,
        skills = listOf("SQL", "Power BI", "Python", "Figma", "Inglés B2", "ETL"),
        expectedSalary = SalaryRange(32_000, 48_000),
        acceptedModes = setOf(WorkMode.REMOTE, WorkMode.HYBRID, WorkMode.ONSITE),
        city = "Chihuahua",
        maxCommuteMinutes = 35,
        availabilityWeeks = 2,
        verified = true,
    )

    /** Vacantes con el tiempo de traslado estimado desde la ubicación de la persona demo. */
    val jobs: List<Pair<Job, Int?>> = listOf(
        Job(
            id = "job-nubia-ux",
            title = "Analista de Producto y UX",
            company = "Nubia Labs",
            companyInitials = "NL",
            salary = SalaryRange(45_000, 55_000),
            mode = WorkMode.REMOTE,
            city = "Remoto MX",
            requiredSkills = listOf("Figma", "SQL", "Inglés B2"),
            niceToHaveSkills = listOf("Python"),
            minYearsExperience = 4,
            benefits = listOf("Prestaciones superiores", "Equipo de cómputo"),
            responseTimeHours = 24,
            accentArgb = 0xFFC5F04A,
        ) to null,
        Job(
            id = "job-planta-bi",
            title = "Analista BI de Producción",
            company = "Planta Norte Automotriz",
            companyInitials = "PN",
            salary = SalaryRange(30_000, 36_000),
            mode = WorkMode.ONSITE,
            city = "Chihuahua",
            requiredSkills = listOf("Power BI", "SQL", "Excel"),
            niceToHaveSkills = listOf("Lean"),
            minYearsExperience = 3,
            benefits = listOf("Transporte de personal", "Vales de despensa", "Fondo de ahorro"),
            responseTimeHours = 48,
            accentArgb = 0xFFFF6A3D,
        ) to 12,
        Job(
            id = "job-rumbo-data",
            title = "Ingeniería de Datos",
            company = "Rumbo Fintech",
            companyInitials = "RF",
            salary = SalaryRange(46_000, 58_000),
            mode = WorkMode.HYBRID,
            city = "CDMX",
            requiredSkills = listOf("Python", "SQL", "Airflow"),
            niceToHaveSkills = listOf("AWS"),
            minYearsExperience = 5,
            benefits = listOf("Acciones", "Seguro de gastos médicos"),
            responseTimeHours = 72,
            accentArgb = 0xFF8B76FF,
        ) to null,
        Job(
            id = "job-sierra-ops",
            title = "Coordinación de Operaciones",
            company = "Logística Sierra",
            companyInitials = "LS",
            salary = SalaryRange(26_000, 30_000),
            mode = WorkMode.ONSITE,
            city = "Chihuahua",
            requiredSkills = listOf("Excel", "Liderazgo"),
            minYearsExperience = 3,
            accentArgb = 0xFFEFEBFF,
        ) to 40,
        Job(
            id = "job-delicias-analytics",
            title = "Analista de Datos Comerciales",
            company = "Grupo Delicias Retail",
            companyInitials = "GD",
            salary = SalaryRange(35_000, 42_000),
            mode = WorkMode.HYBRID,
            city = "Chihuahua",
            requiredSkills = listOf("SQL", "Power BI"),
            niceToHaveSkills = listOf("Python", "ETL"),
            minYearsExperience = 3,
            benefits = listOf("Home office 3 días", "Bono trimestral"),
            responseTimeHours = 24,
            accentArgb = 0xFFC5F04A,
        ) to 18,
    )

    val demoRecruiterJob = Job(
        id = "job-mine",
        title = "Analista de Datos",
        company = "Distribuidora del Norte",
        companyInitials = "DN",
        salary = SalaryRange(30_000, 38_000),
        mode = WorkMode.HYBRID,
        city = "Chihuahua",
        requiredSkills = listOf("SQL", "Power BI"),
        niceToHaveSkills = listOf("Python", "Inglés B1"),
        minYearsExperience = 3,
        responseTimeHours = 24,
        accentArgb = 0xFF5B3DF5,
    )

    /** Candidatos con el tiempo de traslado estimado hacia la vacante del reclutador demo. */
    val candidates: List<Pair<CandidateProfile, Int?>> = listOf(
        CandidateProfile(
            id = "cand-mariana", displayName = "Mariana S.", initials = "MS",
            headline = "Analista de Datos", yearsExperience = 6,
            skills = listOf("SQL", "Power BI", "Python", "Inglés B2", "ETL"),
            expectedSalary = SalaryRange(32_000, 38_000),
            acceptedModes = setOf(WorkMode.HYBRID, WorkMode.REMOTE),
            city = "Chihuahua", availabilityWeeks = 2, verified = true, accentArgb = 0xFFC5F04A,
        ) to 18,
        CandidateProfile(
            id = "cand-a2291", displayName = "Candidato #A-2291", initials = "A",
            headline = "Analista BI", yearsExperience = 4,
            skills = listOf("SQL", "Excel avanzado", "Tableau", "Inglés B1", "Power BI"),
            expectedSalary = SalaryRange(30_000, 34_000),
            acceptedModes = setOf(WorkMode.ONSITE, WorkMode.HYBRID),
            city = "Chihuahua", availabilityWeeks = 0, verified = true, blindMode = true, accentArgb = 0xFF8B76FF,
        ) to 12,
        CandidateProfile(
            id = "cand-jorge", displayName = "Jorge R.", initials = "JR",
            headline = "Ingeniero de Datos Jr.", yearsExperience = 3,
            skills = listOf("Python", "SQL", "Airflow", "AWS"),
            expectedSalary = SalaryRange(28_000, 33_000),
            acceptedModes = setOf(WorkMode.REMOTE, WorkMode.HYBRID),
            city = "Delicias", maxCommuteMinutes = 60, availabilityWeeks = 4, accentArgb = 0xFFFF6A3D,
        ) to 55,
        CandidateProfile(
            id = "cand-lucia", displayName = "Lucía M.", initials = "LM",
            headline = "Analista Financiera", yearsExperience = 5,
            skills = listOf("Excel", "Power BI", "SAP"),
            expectedSalary = SalaryRange(40_000, 50_000),
            acceptedModes = setOf(WorkMode.ONSITE),
            city = "Chihuahua", availabilityWeeks = 1, accentArgb = 0xFFEFEBFF,
        ) to 10,
        CandidateProfile(
            id = "cand-daniel", displayName = "Daniel T.", initials = "DT",
            headline = "Analista de Datos", yearsExperience = 2,
            skills = listOf("SQL", "Power BI", "Python"),
            expectedSalary = SalaryRange(25_000, 31_000),
            acceptedModes = setOf(WorkMode.HYBRID, WorkMode.ONSITE),
            city = "Chihuahua", availabilityWeeks = 0, verified = true, accentArgb = 0xFFC5F04A,
        ) to 22,
    )
}
