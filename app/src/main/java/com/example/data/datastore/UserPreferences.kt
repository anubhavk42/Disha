package com.example.data.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "disha_user_preferences")

class UserPreferences(private val context: Context) {

    companion object {
        val PREF_THEME = stringPreferencesKey("pref_theme")
        val PREF_REDUCE_MOTION = booleanPreferencesKey("pref_reduce_motion")
        val PREF_FONT_SIZE = booleanPreferencesKey("pref_font_size")
    }

    val themeFlow: Flow<String> = context.dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            preferences[PREF_THEME] ?: "System"
        }

    val reduceMotionFlow: Flow<Boolean> = context.dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            preferences[PREF_REDUCE_MOTION] ?: false
        }

    val fontSizeFlow: Flow<Boolean> = context.dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            preferences[PREF_FONT_SIZE] ?: true
        }

    suspend fun setTheme(themeValue: String) {
        context.dataStore.edit { preferences ->
            preferences[PREF_THEME] = themeValue
        }
    }

    suspend fun setReduceMotion(reduce: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PREF_REDUCE_MOTION] = reduce
        }
    }

    suspend fun setUseSystemFontSize(useSystem: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PREF_FONT_SIZE] = useSystem
        }
    }

    suspend fun clear() {
        context.dataStore.edit { preferences ->
            preferences.clear()
        }
    }
}
