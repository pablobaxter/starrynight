package com.frybits.gradle.atproto.generator

import com.frybits.gradle.atproto.lexicon.Lexicon
import com.frybits.gradle.atproto.lexicon.categories.BooleanField
import com.frybits.gradle.atproto.lexicon.categories.BytesField
import com.frybits.gradle.atproto.lexicon.categories.IntegerField
import com.frybits.gradle.atproto.lexicon.categories.StringField
import com.squareup.kotlinpoet.FileSpec
import org.gradle.api.Project

internal fun Project.generateLexicon() {

}

internal fun generateLexiconDefinitions(schema: Lexicon) {
    schema.defs.forEach { (name, def) ->
        val fileBuilder = FileSpec.builder(schema.id, name)
        if (schema.description != null) {
            fileBuilder.addFileComment("""
                GENERATED FILE. DO NOT MODIFY!
                Schema description: ${schema.description}
            """.trimIndent())
        } else {
            fileBuilder.addFileComment("GENERATED FILE. DO NOT MODIFY!")
        }

        when (def) {
            is BooleanField -> {
                println(def.generateClass(name))
            }
            is IntegerField -> {
                println(def.generateClass(name))
            }
            is StringField -> {
                println(def.generateClass(name))
            }
            is BytesField -> {
                println(def.generateClass(name))
            }
            else -> {}
        }
    }
}
