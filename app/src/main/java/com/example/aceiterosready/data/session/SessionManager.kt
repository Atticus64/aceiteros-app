package com.example.aceiterosready.data.session

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.aceiterosready.data.model.TokensDto
import com.example.aceiterosready.data.model.UserDto
import com.google.gson.Gson
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import java.io.IOException

private val Context.authDataStore by preferencesDataStore(name = "auth_session")

data class Session(
    val accessToken: String,
    val refreshToken: String,
    val user: UserDto,
)

class SessionManager(context: Context) {
    private val dataStore = context.applicationContext.authDataStore
    private val _session = MutableStateFlow<Session?>(null)

    val session: StateFlow<Session?> = _session.asStateFlow()

    suspend fun restore(): Session? {
        val prefs = try {
            dataStore.data.first()
        } catch (_: IOException) {
            return null
        }

        val accessToken = prefs[ACCESS_TOKEN]
        val refreshToken = prefs[REFRESH_TOKEN]
        val userJson = prefs[USER_JSON]

        if (accessToken == null || refreshToken == null || userJson == null) {
            _session.value = null
            return null
        }

        val user = runCatching { gson.fromJson(userJson, UserDto::class.java) }.getOrNull()
        if (user == null) {
            clear()
            return null
        }

        val restored = Session(accessToken, refreshToken, user)
        _session.value = restored
        return restored
    }

    suspend fun save(tokens: TokensDto, user: UserDto) {
        val newSession = Session(tokens.accessToken, tokens.refreshToken, user)
        _session.value = newSession

        dataStore.edit { prefs ->
            prefs[ACCESS_TOKEN] = tokens.accessToken
            prefs[REFRESH_TOKEN] = tokens.refreshToken
            prefs[USER_JSON] = gson.toJson(user)
        }
    }

    suspend fun updateTokens(tokens: TokensDto) {
        val current = _session.value ?: return
        _session.value = current.copy(
            accessToken = tokens.accessToken,
            refreshToken = tokens.refreshToken,
        )

        dataStore.edit { prefs ->
            prefs[ACCESS_TOKEN] = tokens.accessToken
            prefs[REFRESH_TOKEN] = tokens.refreshToken
        }
    }

    suspend fun updateUser(user: UserDto) {
        val current = _session.value ?: return
        _session.value = current.copy(user = user)

        dataStore.edit { prefs ->
            prefs[USER_JSON] = gson.toJson(user)
        }
    }

    suspend fun clear() {
        _session.value = null
        runCatching { dataStore.edit { it.clear() } }
    }

    private companion object {
        val ACCESS_TOKEN = stringPreferencesKey("access_token")
        val REFRESH_TOKEN = stringPreferencesKey("refresh_token")
        val USER_JSON = stringPreferencesKey("user_json")
        val gson = Gson()
    }
}
