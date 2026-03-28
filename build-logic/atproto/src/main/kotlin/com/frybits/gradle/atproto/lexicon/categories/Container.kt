package com.frybits.gradle.atproto.lexicon.categories

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal sealed interface ContainerField: LexiconType

@Serializable
@SerialName("array")
internal data class ArrayField(
    override val description: String? = null,
    val items: LexiconType,
    val minLength: Int? = null,
    val maxLength: Int? = null
): ContainerField

@Serializable
@SerialName("object")
internal data class ObjectField(
    override val description: String? = null,
    val properties: Map<String, LexiconType>,
    val required: List<String>? = null,
    val nullable: List<String>? = null
): ContainerField
