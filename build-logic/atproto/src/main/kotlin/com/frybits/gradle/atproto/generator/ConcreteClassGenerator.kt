package com.frybits.gradle.atproto.generator

import com.frybits.gradle.atproto.lexicon.categories.BlobField
import com.frybits.gradle.atproto.lexicon.categories.BooleanField
import com.frybits.gradle.atproto.lexicon.categories.BytesField
import com.frybits.gradle.atproto.lexicon.categories.CidLinkField
import com.frybits.gradle.atproto.lexicon.categories.IntegerField
import com.frybits.gradle.atproto.lexicon.categories.StringField
import com.frybits.gradle.atproto.lexicon.categories.StringFormat
import com.frybits.gradle.atproto.utils.camelToSnakeCase
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.ParameterSpec
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.asTypeName
import org.gradle.internal.extensions.stdlib.capitalized
import java.net.URI
import java.time.ZonedDateTime

internal fun BlobField.generateField(
    name: String,
    typeSpecBuilder: TypeSpec.Builder,
    constructorBuilder: FunSpec.Builder,
    initCodeBlockBuilder: CodeBlock.Builder,
    isRequired: Boolean
) {
    val typeName = ClassName(packageName = "com.frybits.starrynight.atproto.models.blob", "Blob").copy(nullable = !isRequired)
    val standardBlobClassName = ClassName(packageName = "com.frybits.starrynight.atproto.models.blob", "StandardBlob")

    val property = PropertySpec.builder(name, typeName)
        .initializer(name)

    val parameter = ParameterSpec.builder(name, typeName)

    if (description != null) {
        parameter.addKdoc(description)
        property.addKdoc(description)
    }

    val outerCodeBlock = CodeBlock.builder()
    val innerCodeBlock = CodeBlock.builder()

    if (initCodeBlockBuilder.isNotEmpty()) {
        outerCodeBlock.addStatement("")
    }

    outerCodeBlock.addStatement("// Begin $name requirements")
    outerCodeBlock.beginControlFlow("if (%L != null)", name)
    if (!accept.isNullOrEmpty()) {
        innerCodeBlock.addStatement("val accept = setOf(%L)", accept.joinToString(", ") { "\"$it\"" })
        innerCodeBlock.addStatement("require(%L.mimeType in accept) { %P }", name, $$"$$name does not allow mimeType $$$name")
    }
    if (maxSize != null) {
        innerCodeBlock.beginControlFlow("if (%L is %T)", name, standardBlobClassName)
            .addStatement("require(%L.size <= %L) { %P }", name, maxSize, $$"$$name cannot be bigger than $$maxSize bytes. Current size: ${$$name.size} bytes")
            .endControlFlow()
    }
    if (innerCodeBlock.isNotEmpty()) {
        outerCodeBlock.add(innerCodeBlock.build())
    }
    outerCodeBlock.endControlFlow()
    outerCodeBlock.addStatement("// End $name requirements")

    if (innerCodeBlock.isNotEmpty()) {
        initCodeBlockBuilder.add(outerCodeBlock.build())
    }

    constructorBuilder.addParameter(parameter.build())
    typeSpecBuilder.addProperty(property.build())
}

internal fun BooleanField.generateField(
    name: String,
    typeSpecBuilder: TypeSpec.Builder,
    constructorBuilder: FunSpec.Builder,
    companionBuilder: TypeSpec.Builder,
    isRequired: Boolean
) {
    // Fixed value for this property
    if (const != null) {
        val property = PropertySpec.builder(name.camelToSnakeCase().uppercase(), Boolean::class)
            .addModifiers(KModifier.PUBLIC)
            .addModifiers(KModifier.CONST)
            .initializer("%L", const)

        if (description != null) {
            property.addKdoc(description)
        }

        companionBuilder.addProperty(property.build())
        return // Return early as nothing else can be done here
    }

    val typeName = Boolean::class.asTypeName().copy(nullable = !isRequired)
    val property = PropertySpec.builder(name, typeName)

    property.initializer(name)

    val parameter = ParameterSpec.builder(name, typeName)

    if (description != null) {
        parameter.addKdoc(description)
        property.addKdoc(description)
    }

    if (default != null) {
        parameter.defaultValue("%L", default)
    }

    constructorBuilder.addParameter(parameter.build())
    typeSpecBuilder.addProperty(property.build())
}

internal fun BytesField.generateField(
    name: String,
    typeSpecBuilder: TypeSpec.Builder,
    constructorBuilder: FunSpec.Builder,
    initCodeBlockBuilder: CodeBlock.Builder,
    isRequired: Boolean
) {
    val typeName = ClassName(packageName = "com.frybits.starrynight.atproto.models.bytes", "ATBytes").copy(nullable = !isRequired)

    val property = PropertySpec.builder(name, typeName)
        .initializer(name)

    val parameter = ParameterSpec.builder(name, typeName)

    if (description != null) {
        parameter.addKdoc(description)
        property.addKdoc(description)
    }

    val outerCodeBlock = CodeBlock.builder()
    val innerCodeBlock = CodeBlock.builder()

    if (initCodeBlockBuilder.isNotEmpty()) {
        outerCodeBlock.addStatement("")
    }

    outerCodeBlock.addStatement("// Begin $name requirements")
    outerCodeBlock.beginControlFlow("if (%L != null)", name)
    if (minLength != null) {
        innerCodeBlock.addStatement("require(%L.bytes.size >= %L) { %P }", name, minLength, $$"Expected $$name to be greater than or equal to $$minLength. Current size $$$name")
    }
    if (maxLength != null) {
        innerCodeBlock.addStatement("require(%L.bytes.size <= %L) { %P }", name, maxLength, $$"Expected $$name to be less than or equal to $$maxLength. Current size $$$name")
    }
    if (innerCodeBlock.isNotEmpty()) {
        outerCodeBlock.add(innerCodeBlock.build())
    }
    outerCodeBlock.endControlFlow()
    outerCodeBlock.addStatement("// End $name requirements")

    if (innerCodeBlock.isNotEmpty()) {
        initCodeBlockBuilder.add(outerCodeBlock.build())
    }

    constructorBuilder.addParameter(parameter.build())
    typeSpecBuilder.addProperty(property.build())
}

internal fun IntegerField.generateField(
    name: String,
    typeSpecBuilder: TypeSpec.Builder,
    constructorBuilder: FunSpec.Builder,
    initCodeBlockBuilder: CodeBlock.Builder,
    companionBuilder: TypeSpec.Builder,
    isRequired: Boolean
) {
    // Fixed value for this property
    if (const != null) {
        val property = PropertySpec.builder(name.camelToSnakeCase().uppercase(), Int::class)
            .addModifiers(KModifier.PUBLIC)
            .addModifiers(KModifier.CONST)
            .initializer("%L", const)

        if (description != null) {
            property.addKdoc(description)
        }

        companionBuilder.addProperty(property.build())
        return // Return early as nothing else can be done here
    }

    val typeName = Int::class.asTypeName().copy(nullable = !isRequired)
    val property = PropertySpec.builder(name, typeName)

    property.initializer(name)

    val parameter = ParameterSpec.builder(name, typeName)

    if (description != null) {
        parameter.addKdoc(description)
        property.addKdoc(description)
    }

    if (default != null) {
        parameter.defaultValue("%L", default)
    }

    val outerCodeBlock = CodeBlock.builder()
    val innerCodeBlock = CodeBlock.builder()

    if (initCodeBlockBuilder.isNotEmpty()) {
        outerCodeBlock.addStatement("")
    }

    outerCodeBlock.addStatement("// Begin $name requirements")
    if (minimum != null) {
        innerCodeBlock.addStatement("require(%L >= %L) { %P }", name, minimum, $$"Expected $$name to be greater than or equal to $$minimum. Currently $$$name")
    }
    if (maximum != null) {
        innerCodeBlock.addStatement("require(%L <= %L) { %P }", name, maximum, $$"Expected $$name to be less than or equal to $$maximum. Currently $$$name")
    }
    if (!enum.isNullOrEmpty()) {
        innerCodeBlock.addStatement("val enums = setOf(%L)", enum.joinToString(", "))
        innerCodeBlock.addStatement("require(%L in enums) { %P }", name, $$"$$name must be one of $enums")
    }
    if (innerCodeBlock.isNotEmpty()) {
        outerCodeBlock.add(innerCodeBlock.build())
    }
    outerCodeBlock.endControlFlow()
    outerCodeBlock.addStatement("// End $name requirements")

    if (innerCodeBlock.isNotEmpty()) {
        initCodeBlockBuilder.add(outerCodeBlock.build())
    }

    constructorBuilder.addParameter(parameter.build())
    typeSpecBuilder.addProperty(property.build())
}

//@OptIn(ExperimentalTime::class)
//internal fun StringField.generateField(
//    name: String,
//    typeSpecBuilder: TypeSpec.Builder,
//    constructorBuilder: FunSpec.Builder,
//    initCodeBlockBuilder: CodeBlock.Builder,
//    isRequired: Boolean
//) {
//    val stringPackage = "com.frybits.starrynight.atproto.models.strings"
//    val propertyTypeName = when (format) {
//        StringFormat.AT_IDENTIFIER -> ClassName(stringPackage, "ATIdentifier")
//        StringFormat.AT_URI -> ClassName(stringPackage, "ATUri")
//        StringFormat.CID -> ClassName(stringPackage, "Cid")
//        StringFormat.DATETIME -> Instant::class.asTypeName()
//        StringFormat.DID -> ClassName(stringPackage, "Did")
//        StringFormat.HANDLE -> ClassName(stringPackage, "Handle")
//        StringFormat.NSID -> ClassName(stringPackage, "Nsid")
//        StringFormat.TID -> ClassName(stringPackage, "Tid")
//        StringFormat.RECORD_KEY -> ClassName(stringPackage, "RecordKey")
//        StringFormat.URI -> URI::class.asTypeName()
//        StringFormat.LANGUAGE -> ClassName(stringPackage, "Language")
//        null -> String::class.asTypeName()
//    }
//
//    // Fixed value for this property
//    if (const != null) {
//        val property = PropertySpec.builder(name, propertyTypeName)
//        property.initializer("%L", const)
//        privateProperty.addModifiers(KModifier.PRIVATE)
//        privateProperty.addAnnotation(Transient::class)
//
//        if (description != null) {
//            property.addKdoc(description)
//        }
//
//        typeSpecBuilder.addProperty(privateProperty.build())
//        return // Return early as nothing else can be done here
//    }
//}

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
