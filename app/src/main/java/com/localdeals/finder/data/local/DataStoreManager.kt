package com.localdeals.finder.data.local

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore by preferencesDataStore(name = "session_prefs")

@Singleton
class DataStoreManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        val LOGGED_IN_USER_ID = stringPreferencesKey("logged_in_user_id")
        val MANUAL_LOCATION = stringPreferencesKey("manual_location")
    }

    val loggedInUserId: Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[LOGGED_IN_USER_ID]
    }

    suspend fun saveSession(userId: String) {
        context.dataStore.edit { preferences ->
            preferences[LOGGED_IN_USER_ID] = userId
        }
    }

    suspend fun clearSession() {
        context.dataStore.edit { preferences ->
            preferences.remove(LOGGED_IN_USER_ID)
        }
    }

    val manualLocation: Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[MANUAL_LOCATION]
    }

    suspend fun saveManualLocation(latLngName: String) {
        context.dataStore.edit { preferences ->
            preferences[MANUAL_LOCATION] = latLngName
        }
    }
}
