package com.frybits.gradle.atproto.lexicon

import kotlinx.serialization.Serializable

@Serializable
internal data class RecordResponse(
    val uri: String,
    val cid: String,
    val value: Lexicon
)
