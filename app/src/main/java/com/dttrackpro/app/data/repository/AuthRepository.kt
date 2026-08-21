package com.dttrackpro.app.data.repository

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.dttrackpro.app.data.model.LoginRequest
import com.dttrackpro.app.data.remote.GpsWoxApiService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "dttrack_session")
private val KEY_API_HASH = stringPreferencesKey("user_api_hash")
private val KEY_USER_NAME = stringPreferencesKey("user_name")

class AuthRepository(
    private val context: Context,
    private val api: GpsWoxApiService
) {
    val sessionHash: Flow<String?> = context.dataStore.data.map { it[KEY_API_HASH] }
    val userName: Flow<String?> = context.dataStore.data.map { it[KEY_USER_NAME] }

    suspend fun login(email: String, password: String): Result<String> = runCatching {
        val response = api.login(LoginRequest(email, password))
        val hash = response.userApiHash ?: error(response.message ?: "Login failed")
        context.dataStore.edit {
            it[KEY_API_HASH] = hash
            it[KEY_USER_NAME] = response.user?.name ?: email
        }
        hash
    }

    suspend fun logout() {
        context.dataStore.edit { it.clear() }
    }
}
