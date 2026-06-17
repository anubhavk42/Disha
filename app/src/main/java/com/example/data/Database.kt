package com.example.data

import android.content.Context
import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface UserSessionDao {
    @Query("SELECT * FROM user_sessions LIMIT 1")
    fun getActiveSessionFlow(): Flow<UserSession?>

    @Query("SELECT * FROM user_sessions LIMIT 1")
    suspend fun getActiveSession(): UserSession?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: UserSession)

    @Update
    suspend fun updateSession(session: UserSession)

    @Query("DELETE FROM user_sessions")
    suspend fun clearSession()
}

@Dao
interface PreferenceRoleDao {
    @Query("SELECT * FROM preference_roles")
    fun getAllRolesFlow(): Flow<List<PreferenceRole>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRole(role: PreferenceRole)

    @Delete
    suspend fun deleteRole(role: PreferenceRole)

    @Query("DELETE FROM preference_roles")
    suspend fun clearAll()
}

@Dao
interface JobMatchDao {
    @Query("SELECT * FROM job_matches ORDER BY matchScore DESC")
    fun getAllMatchesFlow(): Flow<List<JobMatch>>

    @Query("SELECT * FROM job_matches WHERE sourcePlatform = :platform")
    fun getMatchesByPlatformFlow(platform: String): Flow<List<JobMatch>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMatches(matches: List<JobMatch>)

    @Query("UPDATE job_matches SET isBookmarked = :isBookmarked WHERE id = :id")
    suspend fun updateBookmark(id: String, isBookmarked: Boolean)

    @Query("UPDATE job_matches SET applicationStatus = :status WHERE id = :id")
    suspend fun updateApplicationStatus(id: String, status: String?)

    @Query("DELETE FROM job_matches")
    suspend fun clearAll()
}

@Database(
    entities = [UserSession::class, PreferenceRole::class, JobMatch::class],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userSessionDao(): UserSessionDao
    abstract fun preferenceRoleDao(): PreferenceRoleDao
    abstract fun jobMatchDao(): JobMatchDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "disha_scout_db"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
