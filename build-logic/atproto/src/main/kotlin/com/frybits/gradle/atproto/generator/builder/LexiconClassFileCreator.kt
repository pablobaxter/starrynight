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

package com.frybits.gradle.atproto.generator.builder

import com.frybits.gradle.atproto.generator.context.LexiconContext
import com.frybits.gradle.atproto.generator.context.LexiconEnvironment
import com.frybits.gradle.atproto.lexicon.categories.ArrayField
import com.frybits.gradle.atproto.lexicon.categories.BlobField
import com.frybits.gradle.atproto.lexicon.categories.BodyField
import com.frybits.gradle.atproto.lexicon.categories.BooleanField
import com.frybits.gradle.atproto.lexicon.categories.BytesField
import com.frybits.gradle.atproto.lexicon.categories.CidLinkField
import com.frybits.gradle.atproto.lexicon.categories.ErrorBodyField
import com.frybits.gradle.atproto.lexicon.categories.IntegerField
import com.frybits.gradle.atproto.lexicon.categories.LexiconType
import com.frybits.gradle.atproto.lexicon.categories.MessageField
import com.frybits.gradle.atproto.lexicon.categories.ObjectField
import com.frybits.gradle.atproto.lexicon.categories.ParamsField
import com.frybits.gradle.atproto.lexicon.categories.PermissionSetField
import com.frybits.gradle.atproto.lexicon.categories.ProcedureField
import com.frybits.gradle.atproto.lexicon.categories.QueryField
import com.frybits.gradle.atproto.lexicon.categories.RecordField
import com.frybits.gradle.atproto.lexicon.categories.RefField
import com.frybits.gradle.atproto.lexicon.categories.RepoPermissionField
import com.frybits.gradle.atproto.lexicon.categories.RpcPermissionField
import com.frybits.gradle.atproto.lexicon.categories.StringField
import com.frybits.gradle.atproto.lexicon.categories.SubscriptionField
import com.frybits.gradle.atproto.lexicon.categories.TokenField
import com.frybits.gradle.atproto.lexicon.categories.UnionField
import com.frybits.gradle.atproto.lexicon.categories.UnknownField

internal class LexiconClassFileCreator(
    private val environment: LexiconEnvironment
) {

    fun createClass(context: LexiconContext) {

    }

    private fun generateClass(lexiconType: LexiconType, context: LexiconContext) {

        when (lexiconType) {
            is BodyField -> TODO()
            is BlobField -> TODO()
            is BooleanField -> TODO()
            is BytesField -> TODO()
            is CidLinkField -> TODO()
            is IntegerField -> TODO()
            is StringField -> TODO()
            is ArrayField -> TODO()
            is ObjectField -> TODO()
            is ErrorBodyField -> TODO()
            is MessageField -> TODO()
            is RefField -> TODO()
            is TokenField -> TODO()
            is UnionField -> TODO()
            is UnknownField -> TODO()
            is PermissionSetField -> TODO()
            is RecordField -> TODO()
            is ProcedureField -> generateClass(lexiconType, context, environment)
            is QueryField -> TODO()
            is SubscriptionField -> TODO()
            is ParamsField -> TODO()
            is RepoPermissionField -> TODO()
            is RpcPermissionField -> TODO()
        }
    }
}