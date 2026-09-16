package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable

@Entity(tableName = "user_sessions")
data class UserSession(
    @PrimaryKey val uid: String,
    val email: String,
    val name: String,
    val mobile: String,
    val designation: String = "Product · Growth",
    val isSetupComplete: Boolean = false,
    val dailyDigest: Boolean = true,
    val secureBiometrics: Boolean = false,
    val autoApplyAlerts: Boolean = false,
    val linkedinConnected: Boolean = true,
    val indeedConnected: Boolean = false,
    val naukriConnected: Boolean = true,
    val hasUploadedResume: Boolean = false,
    val resumeFileName: String? = null,
    val isDarkMode: Boolean = true,
    val isHighContrastMode: Boolean = false,
    val referralsCount: Int = 0
) : Serializable

@Entity(tableName = "preference_roles")
data class PreferenceRole(
    @PrimaryKey val roleName: String
) : Serializable

@Entity(tableName = "job_matches")
data class JobMatch(
    @PrimaryKey val id: String,
    val title: String,
    val company: String,
    val logoUrl: String,
    val location: String,
    val isRemote: Boolean,
    val salaryRange: String,
    val matchScore: Int,
    val sourcePlatform: String, // LinkedIn, Indeed, Naukri, Company Site
    val isFullTime: Boolean = true,
    val experienceLevel: String = "Mid-Senior Level",
    val responsibilitiesJson: String = "[]", // List of bullet points stored as serial string
    val recruiterName: String = "Priya Sharma",
    val recruiterRole: String = "Lead Recruiter",
    val isBookmarked: Boolean = false,
    val applicationStatus: String? = null
) : Serializable
