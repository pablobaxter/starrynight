package com.frybits.gradle.atproto.lexicon

import com.frybits.gradle.atproto.lexicon.categories.LexiconType
import com.frybits.gradle.atproto.lexicon.categories.RecordField
import com.frybits.gradle.atproto.lexicon.categories.XRPCField
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class Lexicon(
    val lexicon: Int,
    val id: String,
    val description: String? = null,
    val defs: Map<String, LexiconType>,
    @SerialName($$"$type") val type: String? = null
) {
    init {
        require(lexicon == 1) { "Unexpected lexicon version: $lexicon" }

        defs.forEach { (name, lexiconType) ->
            when (lexiconType) {
                is RecordField, is XRPCField -> {
                    require(name == "main") {
                        "Records and XRPCFields must be the main definition"
                    }
                }
                else -> Unit
            }
        }
    }
}
