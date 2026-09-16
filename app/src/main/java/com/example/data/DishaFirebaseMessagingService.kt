package com.example.data

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.R
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch

class DishaFirebaseMessagingService : FirebaseMessagingService() {

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d("DishaFCM", "New FCM token generated: $token")
        // In a production application, you would send this token to your app backend server.
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        Log.d("DishaFCM", "FCM payload received from sender: ${remoteMessage.from}")

        // Process data payload
        val data = remoteMessage.data
        if (data.isNotEmpty()) {
            val title = data["title"] ?: "New Job Matching Your Profile"
            val company = data["company"] ?: "Unknown Company"
            val location = data["location"] ?: "Remote"
            val salaryRange = data["salaryRange"] ?: "Negotiable"
            val scoreStr = data["matchScore"] ?: "90"
            val score = scoreStr.toIntOrNull() ?: 90

            val incomingJob = JobMatch(
                id = data["id"] ?: "fcm_${System.currentTimeMillis()}",
                title = title,
                company = company,
                logoUrl = data["logoUrl"] ?: "https://logo.clearbit.com/placeholder.com",
                location = location,
                isRemote = location.contains("Remote", ignoreCase = true),
                salaryRange = salaryRange,
                matchScore = score,
                sourcePlatform = data["sourcePlatform"] ?: "Indeed"
            )

            evaluateAndNotify(applicationContext, incomingJob)
        }
    }

    companion object {
        private const val CHANNEL_ID = "disha_job_alerts_channel"
        private const val CHANNEL_NAME = "Disha Job Match Alerts"
        private const val CHANNEL_DESC = "Real-time alerts for jobs matching your preference criteria"

        /**
         * Evaluates if a newly posted job matches the user's saved preferences/criteria.
         * If there is a match (based on saved preference roles or designation),
         * inserts the job into local Room DB and fires a real push notification.
         */
        fun evaluateAndNotify(context: Context, job: JobMatch) {
            val db = AppDatabase.getDatabase(context)
            val roleDao = db.preferenceRoleDao()
            val matchDao = db.jobMatchDao()

            CoroutineScope(Dispatchers.IO).launch {
                // Get user's saved preference criteria
                val preferences = roleDao.getAllRolesFlow().firstOrNull()?.map { it.roleName } ?: emptyList()
                val session = db.userSessionDao().getActiveSession()
                val designation = session?.designation ?: ""

                // Match criteria logic (case insensitive title contains roleName)
                val matchesCriteria = preferences.any { pref ->
                    job.title.contains(pref, ignoreCase = true) || job.company.contains(pref, ignoreCase = true)
                } || (designation.isNotEmpty() && job.title.contains(designation, ignoreCase = true))

                // Since we want to always alert for highly relevant jobs or direct FCM matches, 
                // we insert the new job match into user's match list if matchesCriteria is true or matchScore >= 85
                if (matchesCriteria || job.matchScore >= 85) {
                    // Update database to include this newly posted job match real-time
                    matchDao.insertMatches(listOf(job))

                    // Spark a real platform push notification
                    sendPushNotification(context, job)
                } else {
                    Log.d("DishaFCM", "Job ${job.title} at ${job.company} skipped. Did not match criteria.")
                }
            }
        }

        /**
         * Builds and triggers a standard system push notification on the device.
         */
        fun sendPushNotification(context: Context, job: JobMatch) {
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            // Ensure Android notification channel is set up
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val channel = NotificationChannel(
                    CHANNEL_ID,
                    CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    description = CHANNEL_DESC
                    enableLights(true)
                    enableVibration(true)
                }
                notificationManager.createNotificationChannel(channel)
            }

            // Intent to open MainActivity on click
            val intent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                putExtra("selected_job_id", job.id)
                putExtra("navigate_to_screen", "JobDetail")
            }

            val pendingIntentFlags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            } else {
                PendingIntent.FLAG_UPDATE_CURRENT
            }

            val pendingIntent = PendingIntent.getActivity(
                context,
                job.hashCode(),
                intent,
                pendingIntentFlags
            )

            // Let's use a standard default drawable if custom is not available
            val notificationIcon = android.R.drawable.ic_dialog_info

            val builder = NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(notificationIcon)
                .setContentTitle("New Match: ${job.title}")
                .setContentText("${job.company} is hiring in ${job.location}! (🔥 ${job.matchScore}% Match)")
                .setStyle(NotificationCompat.BigTextStyle()
                    .bigText("${job.company} is hiring a ${job.title} in ${job.location}.\nSalary: ${job.salaryRange}\nMatch Score: ${job.matchScore}% based on your saved criteria! Tap to apply.")
                )
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setCategory(NotificationCompat.CATEGORY_MESSAGE)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)

            notificationManager.notify(job.id.hashCode(), builder.build())
            Log.d("DishaFCM", "Push notification successfully posted for: ${job.title}")
        }
    }
}
