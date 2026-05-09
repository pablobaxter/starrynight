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

package com.frybits.starrynight.android.atproto.models.strings

import kotlinx.serialization.Serializable

private val HANDLE_REGEX = "^([a-zA-Z0-9]([a-zA-Z0-9-]{0,61}[a-zA-Z0-9])?\\.)+[a-zA-Z]([a-zA-Z0-9-]{0,61}[a-zA-Z0-9])?$".toRegex()
private val RESTRICTED_DOMAINS = setOf(
    "alt",
    "arpa",
    "example",
    "internal",
    "invalid",
    "local",
    "localhost",
    "onion",
)

@Serializable
@JvmInline
internal value class Handle(override val prop: String): ATString, ATIdentifier {

    init {
        require(prop.matches(HANDLE_REGEX)) { "$prop is not valid" }
        val domain = prop.split('.').last()
        require(domain !in RESTRICTED_DOMAINS) { "$prop must not use the domains $RESTRICTED_DOMAINS" }
    }

    override fun toString(): String = prop
}
