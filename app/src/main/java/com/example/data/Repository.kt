package com.example.data

import android.app.Activity
import android.content.Context
import com.google.firebase.auth.PhoneAuthCredential
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
                salaryRange = "₹35L - ₹45L",
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
                salaryRange = "₹15L - ₹20L",
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
                salaryRange = "₹15L - ₹20L",
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
                salaryRange = "₹15L - ₹20L",
                matchScore = 85,
                sourcePlatform = "Naukri"
            ),
            JobMatch(id = "job_10", title = "Data Scientist", company = "Flipkart", logoUrl = "https://logo.clearbit.com/flipkart.com", location = "Bangalore, India", isRemote = false, salaryRange = "₹30L - ₹42L", matchScore = 91, sourcePlatform = "LinkedIn"),
            JobMatch(id = "job_11", title = "ML Engineer", company = "Razorpay", logoUrl = "https://logo.clearbit.com/razorpay.com", location = "Bangalore, India", isRemote = true, salaryRange = "₹32L - ₹48L", matchScore = 93, sourcePlatform = "Naukri"),
            JobMatch(id = "job_12", title = "Growth Marketing Manager", company = "Meesho", logoUrl = "https://logo.clearbit.com/meesho.com", location = "Bangalore, India", isRemote = false, salaryRange = "₹22L - ₹32L", matchScore = 87, sourcePlatform = "LinkedIn"),
            JobMatch(id = "job_13", title = "Backend Engineer (Java)", company = "PhonePe", logoUrl = "https://logo.clearbit.com/phonepe.com", location = "Pune, India", isRemote = false, salaryRange = "₹25L - ₹38L", matchScore = 90, sourcePlatform = "Indeed"),
            JobMatch(id = "job_14", title = "Enterprise Sales Manager", company = "Freshworks", logoUrl = "https://logo.clearbit.com/freshworks.com", location = "Chennai, India", isRemote = false, salaryRange = "₹18L - ₹28L", matchScore = 84, sourcePlatform = "Naukri"),
            JobMatch(id = "job_15", title = "FP&A Analyst", company = "HDFC Bank", logoUrl = "https://logo.clearbit.com/hdfcbank.com", location = "Mumbai, India", isRemote = false, salaryRange = "₹14L - ₹20L", matchScore = 79, sourcePlatform = "Company Site"),
            JobMatch(id = "job_16", title = "People Operations Lead", company = "Zoho", logoUrl = "https://logo.clearbit.com/zoho.com", location = "Chennai, India", isRemote = false, salaryRange = "₹20L - ₹28L", matchScore = 85, sourcePlatform = "LinkedIn"),
            JobMatch(id = "job_17", title = "Supply Chain Analyst", company = "BigBasket", logoUrl = "https://logo.clearbit.com/bigbasket.com", location = "Bangalore, India", isRemote = false, salaryRange = "₹15L - ₹22L", matchScore = 81, sourcePlatform = "Indeed"),
            JobMatch(id = "job_18", title = "Corporate Counsel", company = "CRED", logoUrl = "https://logo.clearbit.com/cred.club", location = "Bangalore, India", isRemote = false, salaryRange = "₹28L - ₹40L", matchScore = 86, sourcePlatform = "Naukri"),
            JobMatch(id = "job_19", title = "Customer Success Manager", company = "Freshdesk", logoUrl = "https://logo.clearbit.com/freshdesk.com", location = "Remote", isRemote = true, salaryRange = "₹16L - ₹24L", matchScore = 83, sourcePlatform = "LinkedIn"),
            JobMatch(id = "job_20", title = "Clinical Research Associate", company = "Dr. Reddy's Laboratories", logoUrl = "https://logo.clearbit.com/drreddys.com", location = "Hyderabad, India", isRemote = false, salaryRange = "₹12L - ₹18L", matchScore = 88, sourcePlatform = "Company Site"),
            JobMatch(id = "job_21", title = "Regulatory Affairs Manager", company = "Cipla", logoUrl = "https://logo.clearbit.com/cipla.com", location = "Mumbai, India", isRemote = false, salaryRange = "₹18L - ₹26L", matchScore = 90, sourcePlatform = "Naukri"),
            JobMatch(id = "job_22", title = "Content Strategist", company = "Netflix India", logoUrl = "https://logo.clearbit.com/netflix.com", location = "Mumbai, India", isRemote = false, salaryRange = "₹20L - ₹30L", matchScore = 82, sourcePlatform = "LinkedIn"),
            JobMatch(id = "job_23", title = "Site Reliability Engineer", company = "Groww", logoUrl = "https://logo.clearbit.com/groww.in", location = "Bangalore, India", isRemote = true, salaryRange = "₹28L - ₹40L", matchScore = 89, sourcePlatform = "Indeed"),
            JobMatch(id = "job_24", title = "Security Engineer", company = "Paytm", logoUrl = "https://logo.clearbit.com/paytm.com", location = "Noida, India", isRemote = false, salaryRange = "₹24L - ₹36L", matchScore = 87, sourcePlatform = "Naukri"),
            JobMatch(id = "job_25", title = "Management Consultant", company = "upGrad", logoUrl = "https://logo.clearbit.com/upgrad.com", location = "Mumbai, India", isRemote = false, salaryRange = "₹18L - ₹26L", matchScore = 80, sourcePlatform = "LinkedIn"),
            JobMatch(id = "job_26", title = "Category Manager", company = "Nykaa", logoUrl = "https://logo.clearbit.com/nykaa.com", location = "Mumbai, India", isRemote = false, salaryRange = "₹16L - ₹24L", matchScore = 83, sourcePlatform = "Indeed"),
            JobMatch(id = "job_27", title = "Mobile Engineer (Android)", company = "Ola", logoUrl = "https://logo.clearbit.com/olacabs.com", location = "Bangalore, India", isRemote = false, salaryRange = "₹26L - ₹38L", matchScore = 92, sourcePlatform = "LinkedIn"),
            JobMatch(id = "job_28", title = "Operations Manager", company = "Urban Company", logoUrl = "https://logo.clearbit.com/urbancompany.com", location = "Delhi NCR, India", isRemote = false, salaryRange = "₹15L - ₹22L", matchScore = 78, sourcePlatform = "Naukri"),
            JobMatch(id = "job_29", title = "Game Designer", company = "Nazara Technologies", logoUrl = "https://logo.clearbit.com/nazara.com", location = "Mumbai, India", isRemote = false, salaryRange = "₹14L - ₹20L", matchScore = 76, sourcePlatform = "Company Site")
        )
        matchDao.insertMatches(defaultJobs)

        // Seed some default roles tags
        val defaultRoles = listOf("Software Engineer", "Product Manager", "UX Designer", "Data Scientist")
        defaultRoles.forEach { roleDao.insertRole(PreferenceRole(it)) }
    }

    suspend fun signUp(email: String, password: String, name: String, mobile: String): Result<UserSession> {
        val result = firebaseService.signUpWithEmail(email, password)
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

    suspend fun login(email: String, password: String): Result<UserSession> {
        // Log in using firebase service
        val result = firebaseService.loginWithEmail(email, password)
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

    /** Kicks off real Firebase SMS OTP verification for [phoneNumber] (E.164 format, e.g. "+919988776655"). */
    suspend fun sendOtp(phoneNumber: String, activity: Activity): FirebaseService.PhoneAuthResult {
        return firebaseService.sendOtp(phoneNumber, activity)
    }

    /** Completes phone sign-in with a credential from either auto-verification or a user-entered OTP code. */
    suspend fun verifyPhoneCredentialAndLogin(
        credential: PhoneAuthCredential,
        email: String,
        name: String,
        mobile: String
    ): Result<UserSession> {
        val result = firebaseService.signInWithPhoneCredential(credential)
        return if (result.error != null) {
            Result.failure(Exception(result.error))
        } else {
            val existing = sessionDao.getActiveSession()
            val session = if (existing != null && existing.uid == result.localId) {
                existing
            } else {
                UserSession(
                    uid = result.localId,
                    email = email,
                    name = name,
                    mobile = mobile,
                    isSetupComplete = false
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
