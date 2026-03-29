package com.frybits.gradle.atproto.lexicon

import com.frybits.gradle.atproto.lexicon.categories.LexiconType
import kotlinx.serialization.Serializable

@Serializable
internal data class Lexicon(
    val lexicon: Int,
    val id: String,
    val description: String? = null,
    val defs: Map<String, LexiconType>
)
