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

import android.net.Uri
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.frybits.starrynight.android.theme.StarryNightTheme
import com.frybits.starrynight.home.HomeNavigation
import com.frybits.starrynight.navigation.LocalNavigator

@Composable
internal fun LoginScreen(
    viewModel: LoginViewModel,
    oAuthUri: Uri? = null
) {
    LaunchedEffect(oAuthUri) {
        viewModel.handleOAuthUri(oAuthUri)
    }

    val context = LocalContext.current
    val currentState by viewModel.currentState.collectAsStateWithLifecycle()
    LoginScreenImpl(
        currentState = currentState,
        onLogin = viewModel::onLogin,
        onLaunchUri = { viewModel.onLaunchUri(context, it) }
    )
}

@Composable
private fun LoginScreenImpl(
    currentState: LoginCurrentState,
    onLogin: (username: String) -> Unit = {},
    onLaunchUri: (uri: Uri) -> Unit = {}
) {
    Scaffold(
        content = { paddingValues ->
            val navigator = LocalNavigator.current
            var inProgress by remember { mutableStateOf(false) }

            LaunchedEffect(currentState) {
                when (currentState) {
                    is LoginCurrentState.None -> {
                        inProgress = false
                    }
                    is LoginCurrentState.OAuth -> {
                        onLaunchUri(currentState.uri.toUri())
                        inProgress = false
                    }
                    is LoginCurrentState.InProgress -> {
                        inProgress = true
                    }
                    is LoginCurrentState.LoggedIn -> {
                        inProgress = false
                        navigator.clearBackStack()
                        navigator.navigateTo(HomeNavigation.HomeKey)
                    }
                }
            }

            Column(
                modifier = Modifier
                    .padding(paddingValues)
                    .fillMaxSize()
                    .imePadding(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier.weight(1F),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Starry Night",
                        style = MaterialTheme.typography.titleLarge
                    )
                }

                Box(
                    modifier = Modifier.weight(1F),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        modifier = Modifier,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        val textState = rememberTextFieldState()
                        OutlinedTextField(
                            state = textState,
                            label = { Text("Username") },
                            lineLimits = TextFieldLineLimits.SingleLine,
                            enabled = !inProgress
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Button(
                            enabled = !inProgress,
                            onClick = {
                                onLogin(textState.text.toString())
                            }
                        ) {
                            Text("Login")
                        }
                    }
                }
            }

            if (inProgress) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
        }
    )
}

@Preview
@Composable
private fun LoginScreenPreview() {
    StarryNightTheme {
        LoginScreenImpl(LoginCurrentState.None)
    }
}
