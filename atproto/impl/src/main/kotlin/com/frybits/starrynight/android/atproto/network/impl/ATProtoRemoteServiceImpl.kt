/*
 * Starry Nights - A BlueSky Android Client
 * Copyright (C) 2026 pablo
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

package com.frybits.starrynight.android.atproto.network.impl

import com.frybits.starrynight.android.atproto.models.strings.Did
import com.frybits.starrynight.android.atproto.network.ATProtoNetworkApi
import com.frybits.starrynight.android.atproto.network.ATProtoRemoteService
import com.frybits.starrynight.network.DnsRecordRepository
import com.frybits.starrynight.network.models.TXT
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import java.util.logging.Logger

private val LOGGER = Logger.getLogger("ATProtoRepository")

@ContributesBinding(AppScope::class)
@Inject
internal class ATProtoRemoteServiceImpl(
    private val atProtoServicesApi: ATProtoNetworkApi,
    private val dnsRecordRepository: DnsRecordRepository
): ATProtoRemoteService {

    override suspend fun resolveHandle(handle: String): Did {
        return resolveHandleViaDNS(handle).recoverCatching {
            currentCoroutineContext().ensureActive()
            LOGGER.warning("Failed resolution via dns: ${it.message}")

            return@recoverCatching Did(atProtoServicesApi.resolveHandleViaWellKnown(handle))
        }.getOrThrow()
    }

    private suspend fun resolveHandleViaDNS(handle: String): Result<Did> {
        return dnsRecordRepository.fetchDnsMessage(handle).mapCatching { message ->
            val didTxt = message.answer.map { it.data }
                .filterIsInstance<TXT>()
                .flatMap { it.txt }
                .firstOrNull { it.trim('"').startsWith("did=") } ?: throw Exception("No did object found")

            return@mapCatching Did(didTxt.removePrefix("did="))
        }
    }
}
