package com.frybits.gradle.atproto.lexicon.categories

import kotlinx.serialization.Serializable

@Serializable
internal sealed interface LexiconType {
    val description: String?
}