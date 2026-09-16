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
                sourcePlatform = "LinkedIn",
                responsibilitiesJson = """[
                    "Own end-to-end product design for TechMantra's flagship consumer app, from wireframes to developer-ready specs.",
                    "Partner directly with founders and engineering leads to shape the roadmap from a design-first perspective.",
                    "Establish and maintain the company's first formal design system as the team scales."
                ]""",
                recruiterName = "Ananya Reddy",
                recruiterRole = "Senior Talent Partner"
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
                sourcePlatform = "Indeed",
                responsibilitiesJson = """[
                    "Lead qualitative and quantitative research studies to validate product decisions before engineering investment.",
                    "Build a repeatable research operations practice, including participant recruitment and insight repositories.",
                    "Present findings directly to leadership to influence quarterly product strategy."
                ]""",
                recruiterName = "Vikram Singh",
                recruiterRole = "UX Hiring Lead"
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
                sourcePlatform = "LinkedIn",
                responsibilitiesJson = """[
                    "Design micro-interactions and motion patterns for Swiggy's ordering and delivery-tracking flows.",
                    "Collaborate with product and engineering to prototype and test interaction models at scale.",
                    "Contribute to Swiggy's design system, ensuring consistency across the consumer app."
                ]""",
                recruiterName = "Neha Kulkarni",
                recruiterRole = "Design Recruiter"
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
                sourcePlatform = "Indeed",
                responsibilitiesJson = """[
                    "Define frontend architecture and technical standards across Zomato's consumer-facing web and app surfaces.",
                    "Lead performance optimization initiatives to improve load times for high-traffic markets.",
                    "Mentor a team of frontend engineers and drive adoption of modern component patterns."
                ]""",
                recruiterName = "Rahul Mehta",
                recruiterRole = "Engineering Hiring Manager"
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
                sourcePlatform = "Company Site",
                responsibilitiesJson = """[
                    "Design, test, and refine prompts for client-facing generative AI products across industries.",
                    "Collaborate with data science teams to evaluate model outputs and reduce hallucination rates.",
                    "Document prompt patterns and best practices for reuse across client engagements."
                ]""",
                recruiterName = "Ritu Bansal",
                recruiterRole = "Technical Recruiter"
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
                sourcePlatform = "LinkedIn",
                responsibilitiesJson = """[
                    "Own a feature area end-to-end, from discovery through launch and post-launch iteration.",
                    "Work closely with design and engineering to translate user research into shipped features.",
                    "Track and report on key product metrics to guide prioritization decisions."
                ]""",
                recruiterName = "Karan Malhotra",
                recruiterRole = "People Partner"
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
                sourcePlatform = "Naukri",
                responsibilitiesJson = """[
                    "Manage the roadmap for a core travel-booking feature used by millions of monthly users.",
                    "Run A/B tests to validate hypotheses before full rollout.",
                    "Coordinate cross-functionally with engineering, design, and customer support teams."
                ]""",
                recruiterName = "Sanjana Iyer",
                recruiterRole = "Talent Acquisition Lead"
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
                sourcePlatform = "Naukri",
                responsibilitiesJson = """[
                    "Define and prioritize the roadmap for a core observability product used by enterprise customers.",
                    "Gather and synthesize customer feedback from enterprise accounts to shape product direction.",
                    "Partner with engineering leadership on technical trade-offs and release planning."
                ]""",
                recruiterName = "Arjun Nair",
                recruiterRole = "Recruiting Manager"
            ),
            JobMatch(
                id = "job_10",
                title = "Data Scientist",
                company = "Flipkart",
                logoUrl = "https://logo.clearbit.com/flipkart.com",
                location = "Bangalore, India",
                isRemote = false,
                salaryRange = "₹30L - ₹42L",
                matchScore = 91,
                sourcePlatform = "LinkedIn",
                responsibilitiesJson = """[
                    "Build and deploy machine learning models to improve product recommendations at scale.",
                    "Partner with engineering to productionize models and monitor their performance over time.",
                    "Present analysis and model results to cross-functional stakeholders to guide business decisions."
                ]""",
                recruiterName = "Divya Rao",
                recruiterRole = "Data Science Hiring Lead"
            ),
            JobMatch(
                id = "job_11",
                title = "ML Engineer",
                company = "Razorpay",
                logoUrl = "https://logo.clearbit.com/razorpay.com",
                location = "Bangalore, India",
                isRemote = true,
                salaryRange = "₹32L - ₹48L",
                matchScore = 93,
                sourcePlatform = "Naukri",
                responsibilitiesJson = """[
                    "Build and maintain fraud-detection models that process millions of transactions daily.",
                    "Own the ML pipeline from feature engineering through deployment and monitoring.",
                    "Collaborate with the risk team to continuously tune model thresholds against real-world outcomes."
                ]""",
                recruiterName = "Aditya Kapoor",
                recruiterRole = "ML Talent Partner"
            ),
            JobMatch(
                id = "job_12",
                title = "Growth Marketing Manager",
                company = "Meesho",
                logoUrl = "https://logo.clearbit.com/meesho.com",
                location = "Bangalore, India",
                isRemote = false,
                salaryRange = "₹22L - ₹32L",
                matchScore = 87,
                sourcePlatform = "LinkedIn",
                responsibilitiesJson = """[
                    "Own performance marketing campaigns across paid and organic channels to drive user acquisition.",
                    "Analyze funnel data to identify and fix drop-off points in the growth loop.",
                    "Partner with product on referral and retention mechanics."
                ]""",
                recruiterName = "Meera Pillai",
                recruiterRole = "Growth Recruiter"
            ),
            JobMatch(
                id = "job_13",
                title = "Backend Engineer (Java)",
                company = "PhonePe",
                logoUrl = "https://logo.clearbit.com/phonepe.com",
                location = "Pune, India",
                isRemote = false,
                salaryRange = "₹25L - ₹38L",
                matchScore = 90,
                sourcePlatform = "Indeed",
                responsibilitiesJson = """[
                    "Design and build backend services that support high-throughput payment transactions.",
                    "Own reliability and scalability of core services, including on-call rotation.",
                    "Collaborate with product and QA to ship features that meet strict compliance requirements."
                ]""",
                recruiterName = "Suresh Kumar",
                recruiterRole = "Backend Hiring Lead"
            ),
            JobMatch(
                id = "job_14",
                title = "Enterprise Sales Manager",
                company = "Freshworks",
                logoUrl = "https://logo.clearbit.com/freshworks.com",
                location = "Chennai, India",
                isRemote = false,
                salaryRange = "₹18L - ₹28L",
                matchScore = 84,
                sourcePlatform = "Naukri",
                responsibilitiesJson = """[
                    "Own a book of enterprise accounts, from prospecting through contract negotiation and close.",
                    "Partner with solutions engineering to tailor demos to enterprise buyer needs.",
                    "Consistently meet or exceed quarterly revenue targets."
                ]""",
                recruiterName = "Pooja Agarwal",
                recruiterRole = "Sales Recruiter"
            ),
            JobMatch(
                id = "job_15",
                title = "FP&A Analyst",
                company = "HDFC Bank",
                logoUrl = "https://logo.clearbit.com/hdfcbank.com",
                location = "Mumbai, India",
                isRemote = false,
                salaryRange = "₹14L - ₹20L",
                matchScore = 79,
                sourcePlatform = "Company Site",
                responsibilitiesJson = """[
                    "Build and maintain financial models supporting quarterly forecasting and budgeting cycles.",
                    "Analyze variance between actuals and forecasts, and present findings to finance leadership.",
                    "Support ad hoc financial analysis for strategic initiatives across the bank."
                ]""",
                recruiterName = "Rohan Desai",
                recruiterRole = "Finance Hiring Manager"
            ),
            JobMatch(
                id = "job_16",
                title = "People Operations Lead",
                company = "Zoho",
                logoUrl = "https://logo.clearbit.com/zoho.com",
                location = "Chennai, India",
                isRemote = false,
                salaryRange = "₹20L - ₹28L",
                matchScore = 85,
                sourcePlatform = "LinkedIn",
                responsibilitiesJson = """[
                    "Own core people operations processes, from onboarding through offboarding, for a fast-growing team.",
                    "Partner with leadership on org design and workforce planning.",
                    "Improve HR systems and processes to scale with headcount growth."
                ]""",
                recruiterName = "Kavita Joshi",
                recruiterRole = "HR Business Partner"
            ),
            JobMatch(
                id = "job_17",
                title = "Supply Chain Analyst",
                company = "BigBasket",
                logoUrl = "https://logo.clearbit.com/bigbasket.com",
                location = "Bangalore, India",
                isRemote = false,
                salaryRange = "₹15L - ₹22L",
                matchScore = 81,
                sourcePlatform = "Indeed",
                responsibilitiesJson = """[
                    "Analyze inventory and fulfillment data to reduce stockouts and delivery delays.",
                    "Partner with warehouse operations teams to optimize replenishment cycles.",
                    "Build dashboards to track supply chain KPIs across regional hubs."
                ]""",
                recruiterName = "Manish Tiwari",
                recruiterRole = "Operations Recruiter"
            ),
            JobMatch(
                id = "job_18",
                title = "Corporate Counsel",
                company = "CRED",
                logoUrl = "https://logo.clearbit.com/cred.club",
                location = "Bangalore, India",
                isRemote = false,
                salaryRange = "₹28L - ₹40L",
                matchScore = 86,
                sourcePlatform = "Naukri",
                responsibilitiesJson = """[
                    "Advise on regulatory and compliance matters across CRED's fintech product lines.",
                    "Draft and negotiate commercial contracts with vendors and partners.",
                    "Partner with product teams to ensure new features meet legal and regulatory requirements."
                ]""",
                recruiterName = "Aisha Khan",
                recruiterRole = "Legal Recruiter"
            ),
            JobMatch(
                id = "job_19",
                title = "Customer Success Manager",
                company = "Freshdesk",
                logoUrl = "https://logo.clearbit.com/freshdesk.com",
                location = "Remote",
                isRemote = true,
                salaryRange = "₹16L - ₹24L",
                matchScore = 83,
                sourcePlatform = "LinkedIn",
                responsibilitiesJson = """[
                    "Own renewal and expansion outcomes for a portfolio of mid-market customer accounts.",
                    "Proactively identify at-risk accounts and drive retention initiatives.",
                    "Partner with product on customer feedback to influence the roadmap."
                ]""",
                recruiterName = "Varun Chopra",
                recruiterRole = "Customer Success Hiring Lead"
            ),
            JobMatch(
                id = "job_20",
                title = "Clinical Research Associate",
                company = "Dr. Reddy's Laboratories",
                logoUrl = "https://logo.clearbit.com/drreddys.com",
                location = "Hyderabad, India",
                isRemote = false,
                salaryRange = "₹12L - ₹18L",
                matchScore = 88,
                sourcePlatform = "Company Site",
                responsibilitiesJson = """[
                    "Monitor clinical trial sites to ensure protocol compliance and data integrity.",
                    "Coordinate with investigators and site staff to resolve query and documentation issues.",
                    "Prepare monitoring visit reports and escalate risks to the clinical operations team."
                ]""",
                recruiterName = "Shreya Verma",
                recruiterRole = "Clinical Recruiter"
            ),
            JobMatch(
                id = "job_21",
                title = "Regulatory Affairs Manager",
                company = "Cipla",
                logoUrl = "https://logo.clearbit.com/cipla.com",
                location = "Mumbai, India",
                isRemote = false,
                salaryRange = "₹18L - ₹26L",
                matchScore = 90,
                sourcePlatform = "Naukri",
                responsibilitiesJson = """[
                    "Prepare and submit regulatory filings for new drug approvals across key markets.",
                    "Track evolving regulatory requirements and advise cross-functional teams on compliance impact.",
                    "Liaise directly with regulatory authorities during the review process."
                ]""",
                recruiterName = "Nikhil Saxena",
                recruiterRole = "Regulatory Hiring Manager"
            ),
            JobMatch(
                id = "job_22",
                title = "Content Strategist",
                company = "Netflix India",
                logoUrl = "https://logo.clearbit.com/netflix.com",
                location = "Mumbai, India",
                isRemote = false,
                salaryRange = "₹20L - ₹30L",
                matchScore = 82,
                sourcePlatform = "LinkedIn",
                responsibilitiesJson = """[
                    "Shape content strategy for regional storytelling initiatives aimed at Indian audiences.",
                    "Analyze viewership data to inform content acquisition and commissioning decisions.",
                    "Partner with marketing to position new titles for regional launches."
                ]""",
                recruiterName = "Tanvi Bhatt",
                recruiterRole = "Content Recruiter"
            ),
            JobMatch(
                id = "job_23",
                title = "Site Reliability Engineer",
                company = "Groww",
                logoUrl = "https://logo.clearbit.com/groww.in",
                location = "Bangalore, India",
                isRemote = true,
                salaryRange = "₹28L - ₹40L",
                matchScore = 89,
                sourcePlatform = "Indeed",
                responsibilitiesJson = """[
                    "Own uptime and reliability for Groww's trading and investment platforms.",
                    "Build monitoring, alerting, and incident-response tooling to reduce mean time to resolution.",
                    "Lead postmortems and drive systemic fixes for recurring reliability issues."
                ]""",
                recruiterName = "Aman Gupta",
                recruiterRole = "SRE Hiring Lead"
            ),
            JobMatch(
                id = "job_24",
                title = "Security Engineer",
                company = "Paytm",
                logoUrl = "https://logo.clearbit.com/paytm.com",
                location = "Noida, India",
                isRemote = false,
                salaryRange = "₹24L - ₹36L",
                matchScore = 87,
                sourcePlatform = "Naukri",
                responsibilitiesJson = """[
                    "Conduct security reviews and penetration tests across Paytm's payment infrastructure.",
                    "Respond to and triage security incidents, coordinating remediation across teams.",
                    "Build automated tooling to detect vulnerabilities before they reach production."
                ]""",
                recruiterName = "Ishaan Mishra",
                recruiterRole = "Security Recruiter"
            ),
            JobMatch(
                id = "job_25",
                title = "Management Consultant",
                company = "upGrad",
                logoUrl = "https://logo.clearbit.com/upgrad.com",
                location = "Mumbai, India",
                isRemote = false,
                salaryRange = "₹18L - ₹26L",
                matchScore = 80,
                sourcePlatform = "LinkedIn",
                responsibilitiesJson = """[
                    "Lead client engagements from problem framing through recommendation and delivery.",
                    "Build structured analyses and frameworks to support strategic decision-making.",
                    "Manage stakeholder relationships across client and internal teams."
                ]""",
                recruiterName = "Priyanka Menon",
                recruiterRole = "Consulting Recruiter"
            ),
            JobMatch(
                id = "job_26",
                title = "Category Manager",
                company = "Nykaa",
                logoUrl = "https://logo.clearbit.com/nykaa.com",
                location = "Mumbai, India",
                isRemote = false,
                salaryRange = "₹16L - ₹24L",
                matchScore = 83,
                sourcePlatform = "Indeed",
                responsibilitiesJson = """[
                    "Own P&L for a beauty product category, from vendor negotiation to pricing strategy.",
                    "Analyze sales and margin data to identify growth opportunities within the category.",
                    "Partner with marketing on category-specific campaigns and promotions."
                ]""",
                recruiterName = "Rajesh Pillai",
                recruiterRole = "Category Hiring Manager"
            ),
            JobMatch(
                id = "job_27",
                title = "Mobile Engineer (Android)",
                company = "Ola",
                logoUrl = "https://logo.clearbit.com/olacabs.com",
                location = "Bangalore, India",
                isRemote = false,
                salaryRange = "₹26L - ₹38L",
                matchScore = 92,
                sourcePlatform = "LinkedIn",
                responsibilitiesJson = """[
                    "Build and maintain features across Ola's Android rider and driver apps.",
                    "Optimize app performance and stability for a wide range of Android devices.",
                    "Collaborate with backend and design teams to ship end-to-end mobile features."
                ]""",
                recruiterName = "Sneha Rao",
                recruiterRole = "Mobile Hiring Lead"
            ),
            JobMatch(
                id = "job_28",
                title = "Operations Manager",
                company = "Urban Company",
                logoUrl = "https://logo.clearbit.com/urbancompany.com",
                location = "Delhi NCR, India",
                isRemote = false,
                salaryRange = "₹15L - ₹22L",
                matchScore = 78,
                sourcePlatform = "Naukri",
                responsibilitiesJson = """[
                    "Manage day-to-day service operations across a cluster of city markets.",
                    "Analyze operational metrics to identify and resolve service-quality bottlenecks.",
                    "Partner with central operations teams to roll out process improvements locally."
                ]""",
                recruiterName = "Vivek Anand",
                recruiterRole = "Operations Hiring Manager"
            ),
            JobMatch(
                id = "job_29",
                title = "Game Designer",
                company = "Nazara Technologies",
                logoUrl = "https://logo.clearbit.com/nazara.com",
                location = "Mumbai, India",
                isRemote = false,
                salaryRange = "₹14L - ₹20L",
                matchScore = 76,
                sourcePlatform = "Company Site",
                responsibilitiesJson = """[
                    "Design core game mechanics and progression systems for a mobile title in active development.",
                    "Balance gameplay economies using data from player testing and live-game analytics.",
                    "Collaborate with engineering and art teams to bring design concepts to life."
                ]""",
                recruiterName = "Ayesha Siddiqui",
                recruiterRole = "Game Studio Recruiter"
            )
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
