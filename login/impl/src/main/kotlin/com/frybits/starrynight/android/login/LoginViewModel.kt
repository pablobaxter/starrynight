/*
 * Starry Nights - A BlueSky Android Client
 * Copyright (C) 2026 Pablo Baxter
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.frybits.starrynight.android.login

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.browser.customtabs.CustomTabsIntent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.frybits.starrynight.auth.AuthRepository
import com.frybits.starrynight.utils.core.errors.StarryNightException
import com.frybits.starrynight.utils.core.errors.WrappedException
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesIntoMap
import dev.zacsweers.metro.Inject
import dev.zacsweers.metrox.viewmodel.ViewModelKey
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@Inject
@ViewModelKey
@ContributesIntoMap(AppScope::class)
internal class LoginViewModel(
    private val authRepository: AuthRepository
): ViewModel() {

    private val _currentState = MutableStateFlow<LoginCurrentState>(LoginCurrentState.None)

    internal val currentState = _currentState.asStateFlow()

    init {
        viewModelScope.launch {
            authRepository.getCurrentUserFlow().collect { loggedInUserData ->
                if (loggedInUserData.token.isNotBlank()) {
                    _currentState.update {
                        LoginCurrentState.LoggedIn
                    }
                }
            }
        }
    }

    fun onLogin(userName: String) {
        viewModelScope.launch {
            _currentState.update { LoginCurrentState.InProgress }
            authRepository.loginWithOAuth(userName).onSuccess { uri ->
                _currentState.update {
                    LoginCurrentState.OAuth(uri)
                }
            }.onFailure { e ->
                val exception = (e as? StarryNightException) ?: WrappedException("External exception", e)
                _currentState.update {
                    LoginCurrentState.Error(exception)
                }
            }
        }
    }

    fun onLaunchUri(context: Context, uri: Uri) {
        runCatching {
            val tabIntent = CustomTabsIntent.Builder()
                .setShowTitle(true)
                .setUrlBarHidingEnabled(true)
                .build()
            tabIntent.launchUrl(context, uri)
        }
    }

    fun handleOAuthUri(oAuthUri: Uri?) {
        if (oAuthUri != null) {
            viewModelScope.launch {
                _currentState.update { LoginCurrentState.InProgress }
                authRepository.handleOAuth(oAuthUri.toString()).onSuccess {
                    Log.d("Foobar", it.toString())
                }.onFailure {
                    Log.d("Foobar", "Got a failure: $it")
                }
            }
        }
    }
}
