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

package com.frybits.gradle.atproto.generator.builder.utils

import com.frybits.gradle.atproto.generator.builder.generateClass
import com.frybits.gradle.atproto.generator.builder.generateDataClass
import com.frybits.gradle.atproto.generator.context.LexiconContext
import com.frybits.gradle.atproto.generator.context.LexiconEnvironment
import com.frybits.gradle.atproto.lexicon.categories.ObjectField
import com.frybits.gradle.atproto.lexicon.categories.RecordField
import com.frybits.gradle.atproto.lexicon.categories.RefField
import com.frybits.gradle.atproto.utils.LexiconRef
import com.frybits.gradle.atproto.utils.titleCaseFirstChar

internal fun RefField.handleRefGeneration(context: LexiconContext, environment: LexiconEnvironment) {
    val ref = LexiconRef(ref)
    val lexicon = if (ref.schemaId.isBlank()) {
        context.lexicon
    } else {
        environment.loadLexicon(ref.schemaId)
    }
    when (val lexiconType = environment.loadReference(lexicon, ref)) {
        is ObjectField -> generateDataClass(lexiconType, LexiconContext(ref.objectRef.titleCaseFirstChar(), lexicon), environment)
        is RecordField -> generateClass(lexiconType, LexiconContext(ref.schemaId.split('.').last().titleCaseFirstChar(), lexicon), environment)
        else -> Unit
    }
}
