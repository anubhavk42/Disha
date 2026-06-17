package com.example.data

import android.content.Context
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import java.util.UUID

class JobRepository(private val context: Context) {

    private val db = AppDatabase.getDatabase(context)
    private val sessionDao = db.userSessionDao()
    private val roleDao = db.preferenceRoleDao()
    private val matchDao = db.jobMatchDao()
    private val firebaseService = FirebaseService()

    val activeSessionFlow: Flow<UserSession?> = sessionDao.getActiveSessionFlow()
    val allPreferencesFlow: Flow<List<PreferenceRole>> = roleDao.getAllRolesFlow()
    val allMatchesFlow: Flow<List<JobMatch>> = matchDao.getAllMatchesFlow()

    suspend fun getActiveSession(): UserSession? = sessionDao.getActiveSession()

    // Seeds default matches shown in screen snapshots
    suspend fun seedInitialData() {
        val existing = matchDao.getAllMatchesFlow().firstOrNull() ?: emptyList()
        if (existing.isNotEmpty()) return

        val defaultJobs = listOf(
            JobMatch(
                id = "job_1",
                title = "Senior Product Designer",
                company = "Spotify",
                logoUrl = "https://logo.clearbit.com/spotify.com",
                location = "Stockholm, SE (Remote)",
                isRemote = true,
                salaryRange = "₹35,00,000 - ₹45,00,000/yr",
                matchScore = 94,
                sourcePlatform = "Spotify",
                isFullTime = true,
                experienceLevel = "Mid-Senior Level",
                responsibilitiesJson = """[
                    "Lead end-to-end design for core listening experiences, ensuring a seamless user journey across mobile and desktop platforms.",
                    "Collaborate tightly with cross-functional teams including PMs, Engineering, and Data Science to define product vision.",
                    "Mentor junior designers and contribute to the evolution of the Encore design system."
                ]""",
                recruiterName = "Priya Sharma",
                recruiterRole = "Lead Recruiter"
            ),
            JobMatch(
                id = "job_2",
                title = "Senior Product Designer",
                company = "TechMantra India",
                logoUrl = "https://logo.clearbit.com/techmantra.com",
                location = "Bangalore, India",
                isRemote = false,
                salaryRange = "₹35L - ₹45L",
                matchScore = 98,
                sourcePlatform = "LinkedIn"
            ),
            JobMatch(
                id = "job_3",
                title = "Lead UX Researcher",
                company = "InnovateBharat Labs",
                logoUrl = "https://logo.clearbit.com/innovatebharat.com",
                location = "Remote",
                isRemote = true,
                salaryRange = "₹40L - ₹52L",
                matchScore = 95,
                sourcePlatform = "Indeed"
            ),
            JobMatch(
                id = "job_4",
                title = "Interaction Designer",
                company = "Swiggy",
                logoUrl = "https://logo.clearbit.com/swiggy.com",
                location = "Bangalore (Hybrid)",
                isRemote = false,
                salaryRange = "₹28L - ₹38L",
                matchScore = 92,
                sourcePlatform = "LinkedIn"
            ),
            JobMatch(
                id = "job_5",
                title = "Frontend Architect",
                company = "Zomato",
                logoUrl = "https://logo.clearbit.com/zomato.com",
                location = "Gurgaon, India",
                isRemote = false,
                salaryRange = "₹45L - ₹55L",
                matchScore = 89,
                sourcePlatform = "Indeed"
            ),
            JobMatch(
                id = "job_6",
                title = "AI Prompt Engineer",
                company = "Infosys",
                logoUrl = "https://logo.clearbit.com/infosys.com",
                location = "Remote",
                isRemote = true,
                salaryRange = "₹25L - ₹32L",
                matchScore = 86,
                sourcePlatform = "Company Site"
            ),
            JobMatch(
                id = "job_7",
                title = "Associate Product Manager",
                company = "CredHive",
                logoUrl = "https://logo.clearbit.com/credhive.com",
                location = "Noida, Uttar Pradesh",
                isRemote = false,
                salaryRange = "15 LPA - 20 LPA",
                matchScore = 92,
                sourcePlatform = "LinkedIn"
            ),
            JobMatch(
                id = "job_8",
                title = "Associate Product Manager",
                company = "MakeMyTrip",
                logoUrl = "https://logo.clearbit.com/makemytrip.com",
                location = "Gurugram, Haryana",
                isRemote = false,
                salaryRange = "15 LPA - 20 LPA",
                matchScore = 88,
                sourcePlatform = "Naukri"
            ),
            JobMatch(
                id = "job_9",
                title = "Product Manager",
                company = "Sumo Logic",
                logoUrl = "https://logo.clearbit.com/sumologic.com",
                location = "Gurugram, Haryana",
                isRemote = false,
                salaryRange = "15 LPA - 20 LPA",
                matchScore = 85,
                sourcePlatform = "Naukri"
            )
        )
        matchDao.insertMatches(defaultJobs)

        // Seed some default roles tags
        val defaultRoles = listOf("Software Engineer", "Product Manager", "UX Designer", "Data Scientist")
        defaultRoles.forEach { roleDao.insertRole(PreferenceRole(it)) }
    }

    suspend fun signUp(email: String, name: String, mobile: String): Result<UserSession> {
        val result = firebaseService.signUpWithEmail(email)
        return if (result.error != null) {
            Result.failure(Exception(result.error))
        } else {
            val session = UserSession(
                uid = result.localId,
                email = result.email,
                name = name,
                mobile = mobile,
                isSetupComplete = false
            )
            sessionDao.clearSession()
            sessionDao.insertSession(session)
            // Seed defaults when a session is created
            seedInitialData()
            Result.success(session)
        }
    }

    suspend fun login(email: String, pin: String): Result<UserSession> {
        // Log in using firebase service
        val result = firebaseService.loginWithEmail(email, "password123")
        return if (result.error != null) {
            Result.failure(Exception(result.error))
        } else {
            // Check if there is already a setup session locally
            val existing = sessionDao.getActiveSession()
            val session = if (existing != null && existing.email == email) {
                existing
            } else {
                UserSession(
                    uid = result.localId,
                    email = result.email,
                    name = "Anubhav Kapoor",
                    mobile = "+1 (555) 123-4567",
                    isSetupComplete = true
                )
            }
            sessionDao.clearSession()
            sessionDao.insertSession(session)
            seedInitialData()
            Result.success(session)
        }
    }

    suspend fun updateSession(session: UserSession) {
        sessionDao.updateSession(session)
    }

    suspend fun addPreferenceRole(roleName: String) {
        roleDao.insertRole(PreferenceRole(roleName))
    }

    suspend fun toggleJobBookmark(id: String, isBookmarked: Boolean) {
        matchDao.updateBookmark(id, isBookmarked)
    }

    suspend fun updateJobApplicationStatus(id: String, status: String?) {
        matchDao.updateApplicationStatus(id, status)
    }

    suspend fun removePreferenceRole(roleName: String) {
        roleDao.deleteRole(PreferenceRole(roleName))
    }

    suspend fun logout() {
        sessionDao.clearSession()
    }
}
