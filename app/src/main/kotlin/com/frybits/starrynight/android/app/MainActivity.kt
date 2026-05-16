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

package com.frybits.starrynight.android.app

import android.app.Activity
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.frybits.starrynight.atproto.ATProtoRepository
import com.frybits.starrynight.auth.AuthRepository
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesIntoMap
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.binding
import dev.zacsweers.metrox.android.ActivityKey
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

private val TAG = MainActivity::class.java.simpleName

@ContributesIntoMap(AppScope::class, binding<Activity>())
@ActivityKey
@Inject
internal class MainActivity(
    private val authRepository: AuthRepository,
    private val atProtoRepository: ATProtoRepository
) : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.RESUMED) {
                lifecycleScope.launch {
                    authRepository.getCurrentUserFlow().onEach {
                        Log.d("Blah", "got new user: $it")
                    }.launchIn(this)

                    atProtoRepository.resolveHandle("pablobaxter.com").onSuccess {
                        atProtoRepository.resolveDid(it).onSuccess {
                            Log.d("Blah", "Got resolved data: $it")
                        }.onFailure {
                            Log.d("Blah", "Failed to resolve did", it)
                        }
                    }.onFailure {
                        Log.d("Blah", "Failed to resolve handle", it)
                    }
                }
            }
        }
    }
}
