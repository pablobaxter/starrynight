package com.frybits.gradle.atproto.generator

import com.frybits.gradle.atproto.lexicon.categories.BlobField
import com.frybits.gradle.atproto.lexicon.categories.BooleanField
import com.frybits.gradle.atproto.lexicon.categories.BytesField
import com.frybits.gradle.atproto.lexicon.categories.CidLinkField
import com.frybits.gradle.atproto.lexicon.categories.IntegerField
import com.frybits.gradle.atproto.lexicon.categories.StringField
import com.frybits.gradle.atproto.lexicon.categories.StringFormat
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.ParameterSpec
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeSpec
import org.gradle.internal.extensions.stdlib.capitalized
import java.net.URI
import java.time.ZonedDateTime

internal fun BooleanField.generateClass(name: String): TypeSpec {
    val parameter = ParameterSpec.builder("prop", Boolean::class)

    if (default != null) {
        parameter.defaultValue(default.toString())
    }

    val typeSpecBuilder = TypeSpec.classBuilder(name.capitalized())
        .addAnnotation(JvmInline::class)
        .addModifiers(KModifier.VALUE)
        .primaryConstructor(FunSpec.constructorBuilder()
            .addParameter(parameter.build())
            .build())
        .addProperty(
            PropertySpec.builder("prop", Boolean::class)
                .initializer("prop")
                .build()
        )

    val codeBlock = CodeBlock.builder()

    if (const != null) {
        codeBlock.addStatement("require(prop == %L) { %P }", const, $$"Expected $$const but got $prop")
    }

    if (codeBlock.isNotEmpty()) {
        typeSpecBuilder.addInitializerBlock(codeBlock.build())
    }

    if (description != null) {
        typeSpecBuilder.addKdoc(description)
    }

    return typeSpecBuilder.build()
}

internal fun IntegerField.generateClass(name: String): TypeSpec {
    val parameter = ParameterSpec.builder("prop", Int::class)

    if (default != null) {
        parameter.defaultValue(default.toString())
    }

    val typeSpecBuilder = TypeSpec.classBuilder(name.capitalized())
        .addAnnotation(JvmInline::class)
        .addModifiers(KModifier.VALUE)
        .primaryConstructor(FunSpec.constructorBuilder()
            .addParameter(parameter.build())
            .build())
        .addProperty(
            PropertySpec.builder("prop", Int::class)
                .initializer("prop")
                .build()
        )

    val codeBlock = CodeBlock.builder()

    if (minimum != null) {
        codeBlock.addStatement("require(prop >= %L) { %P }", minimum, $$"Expected $prop to be greater than or equal to $$minimum")
    }

    if (maximum != null) {
        codeBlock.addStatement("require(prop <= %L) { %P }", maximum, $$"Expected $prop to be less than or equal to $$maximum")
    }

    if (enum != null) {
        codeBlock.addStatement("val enumList = setOf(${enum.joinToString(", ")})")
        codeBlock.addStatement("require(prop in enumList) { %P }", $$"Expected $prop to be in $enumList")
    }

    if (const != null) {
        codeBlock.addStatement("require(prop == %L) { %P }", const, $$"Expected $$const but got $prop")
    }

    if (codeBlock.isNotEmpty()) {
        typeSpecBuilder.addInitializerBlock(codeBlock.build())
    }

    if (description != null) {
        typeSpecBuilder.addKdoc(description)
    }

    return typeSpecBuilder.build()
}

internal fun StringField.generateClass(name: String): TypeSpec {
    val type = when (format) {
        StringFormat.AT_URI, StringFormat.URI -> URI::class
        StringFormat.DATETIME -> ZonedDateTime::class
        else -> String::class
    }

    val parameter = ParameterSpec.builder("prop", type)

    if (default != null) {
        when (type) {
            URI::class -> parameter.defaultValue("%T.create(%S)", URI::class, default)
            ZonedDateTime::class -> parameter.defaultValue("%T.parse(%S)", ZonedDateTime::class, default)
            else -> parameter.defaultValue("%S", default)
        }
    }

    val typeSpecBuilder = TypeSpec.classBuilder(name.capitalized())
        .addAnnotation(JvmInline::class)
        .addModifiers(KModifier.VALUE)
        .primaryConstructor(FunSpec.constructorBuilder()
            .addParameter(parameter.build())
            .build())
        .addProperty(
            PropertySpec.builder("prop", type)
                .initializer("prop")
                .build()
        )

    val codeBlock = CodeBlock.builder()

    if (type == String::class) {
        if (maxLength != null) {
            codeBlock.addStatement("require(prop.length <= %L) { %P }", maxLength, $$"Expected string length to be less than or equal to $$maxLength")
        }

        if (minLength != null) {
            codeBlock.addStatement("require(prop.length >= %L) { %P }", minLength, $$"Expected string length to be greater than or equal $$minLength")
        }

        if (maxGraphemes != null || minGraphemes != null) {
            codeBlock.addStatement("val graphemes = \"\\\\X\".toRegex().findAll(prop).count()")
            if (maxGraphemes != null) {
                codeBlock.addStatement("require(graphemes <= %L) { %P }", maxGraphemes, $$"Expected graphemes length to be less than or equal to $$maxGraphemes")
            }
            if (minGraphemes != null) {
                codeBlock.addStatement("require(graphemes >= %L) { %P }", minGraphemes, $$"Expected graphemes length to be greater than or equal to $$minGraphemes")
            }
        }
    }

    if (enum != null) {
        val list = when (type) {
            URI::class -> enum.map { CodeBlock.of("%T.create(%S)", URI::class, it) }
            ZonedDateTime::class -> enum.map { CodeBlock.of("%T.parse(%S)", ZonedDateTime::class, it) }
            else -> enum.map { CodeBlock.of("%S", it) }
        }
        codeBlock.addStatement("val enumList = setOf(${list.joinToString(", ")})")
        codeBlock.addStatement("require(prop in enumList) { %P }", $$"Expected $prop to be in $enumList")
    }

    if (const != null) {
        val block = when (type) {
            URI::class -> CodeBlock.of("%T.create(%S)", URI::class, const)
            ZonedDateTime::class -> CodeBlock.of("%T.parse(%S)", ZonedDateTime::class, const)
            else -> CodeBlock.of("%S", const)
        }
        codeBlock.addStatement("val expected = %L", block)
        codeBlock.addStatement("require(prop == expected) { %P }", $$"Expected $expected but got $prop")
    }

    if (codeBlock.isNotEmpty()) {
        typeSpecBuilder.addInitializerBlock(codeBlock.build())
    }

    return typeSpecBuilder.build()
}

internal fun BytesField.generateClass(name: String): TypeSpec {
    val parameter = ParameterSpec.builder("prop", ByteArray::class)

    val typeSpecBuilder = TypeSpec.classBuilder(name.capitalized())
        .addAnnotation(JvmInline::class)
        .addModifiers(KModifier.VALUE)
        .primaryConstructor(FunSpec.constructorBuilder()
            .addParameter(parameter.build())
            .build())
        .addProperty(
            PropertySpec.builder("prop", ByteArray::class)
                .initializer("prop")
                .build()
        )

    val codeBlock = CodeBlock.builder()

    if (minLength != null) {
        codeBlock.addStatement("require(prop.size >= %L) { %P }", minLength, $$"Expected bytes size to be greater than or equal to $$minLength")
    }

    if (maxLength != null) {
        codeBlock.addStatement("require(prop.size <= %L) { %P }", maxLength, $$"Expected bytes size to be less than or equal to $$maxLength")
    }

    if (codeBlock.isNotEmpty()) {
        typeSpecBuilder.addInitializerBlock(codeBlock.build())
    }

    if (description != null) {
        typeSpecBuilder.addKdoc(description)
    }

    return typeSpecBuilder.build()
}

internal fun CidLinkField.generateClass(name: String): TypeSpec {
    val parameter = ParameterSpec.builder("prop", String::class)

    val typeSpecBuilder = TypeSpec.classBuilder(name.capitalized())
        .addAnnotation(JvmInline::class)
        .addModifiers(KModifier.VALUE)
        .primaryConstructor(FunSpec.constructorBuilder()
            .addParameter(parameter.build())
            .build())
        .addProperty(
            PropertySpec.builder("prop", String::class)
                .initializer("prop")
                .build()
        )

    if (description != null) {
        typeSpecBuilder.addKdoc(description)
    }

    return typeSpecBuilder.build()
}
