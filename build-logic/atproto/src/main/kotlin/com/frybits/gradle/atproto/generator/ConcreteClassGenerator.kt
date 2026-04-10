package com.frybits.gradle.atproto.generator

import com.frybits.gradle.atproto.lexicon.categories.ArrayField
import com.frybits.gradle.atproto.lexicon.categories.BlobField
import com.frybits.gradle.atproto.lexicon.categories.BodyField
import com.frybits.gradle.atproto.lexicon.categories.BooleanField
import com.frybits.gradle.atproto.lexicon.categories.BytesField
import com.frybits.gradle.atproto.lexicon.categories.CidLinkField
import com.frybits.gradle.atproto.lexicon.categories.ErrorBodyField
import com.frybits.gradle.atproto.lexicon.categories.IntegerField
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
import com.frybits.gradle.atproto.lexicon.categories.StringFormat
import com.frybits.gradle.atproto.lexicon.categories.SubscriptionField
import com.frybits.gradle.atproto.lexicon.categories.TokenField
import com.frybits.gradle.atproto.lexicon.categories.UnionField
import com.frybits.gradle.atproto.lexicon.categories.UnknownField
import com.frybits.gradle.atproto.utils.camelToSnakeCase
import com.squareup.kotlinpoet.AnnotationSpec
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.ParameterSpec
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.asClassName
import com.squareup.kotlinpoet.asTypeName
import com.squareup.kotlinpoet.withIndent
import kotlinx.serialization.Serializable
import org.gradle.api.GradleException
import org.gradle.internal.extensions.stdlib.capitalized
import java.net.URI
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

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
    if (!isRequired) {
        outerCodeBlock.beginControlFlow("if (%L != null)", name)
    }
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
    if (!isRequired) {
        outerCodeBlock.endControlFlow()
    }
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
            .addModifiers(KModifier.PUBLIC, KModifier.CONST)
            .initializer("%L", const)

        if (description != null) {
            property.addKdoc(description)
        }

        companionBuilder.addProperty(property.build())
        return // Return early as nothing else can be done here
    }

    val typeName = Boolean::class.asTypeName().copy(nullable = !isRequired)
    val property = PropertySpec.builder(name, typeName)
        .initializer(name)

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
    val typeName = ByteArray::class.asTypeName().copy(nullable = !isRequired)

    val property = PropertySpec.builder(name, typeName)
        .initializer(name)
        .addAnnotation(AnnotationSpec.builder(Serializable::class).addMember("%T::class", ClassName("com.frybits.starrynight.atproto.serializers", "ATBytesSerializer")).build())

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
    if (!isRequired) {
        outerCodeBlock.beginControlFlow("if (%L != null)", name)
    }
    if (minLength != null) {
        innerCodeBlock.addStatement("require(%L.size >= %L) { %P }", name, minLength, $$"Expected $$name to be greater than or equal to $$minLength. Current size ${$$name.size}")
    }
    if (maxLength != null) {
        innerCodeBlock.addStatement("require(%L.size <= %L) { %P }", name, maxLength, $$"Expected $$name to be less than or equal to $$maxLength. Current size ${$$name.size}")
    }
    if (innerCodeBlock.isNotEmpty()) {
        outerCodeBlock.add(innerCodeBlock.build())
    }
    if (!isRequired) {
        outerCodeBlock.endControlFlow()
    }
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
            .addModifiers(KModifier.PUBLIC, KModifier.CONST)
            .initializer("%L", const)

        if (description != null) {
            property.addKdoc(description)
        }

        companionBuilder.addProperty(property.build())
        return // Return early as nothing else can be done here
    }

    val typeName = Int::class.asTypeName().copy(nullable = !isRequired)
    val property = PropertySpec.builder(name, typeName)
        .initializer(name)

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
    if (!isRequired) {
        outerCodeBlock.beginControlFlow("if (%L != null)", name)
    }
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
    if (!isRequired) {
        outerCodeBlock.endControlFlow()
    }
    outerCodeBlock.addStatement("// End $name requirements")

    if (innerCodeBlock.isNotEmpty()) {
        initCodeBlockBuilder.add(outerCodeBlock.build())
    }

    constructorBuilder.addParameter(parameter.build())
    typeSpecBuilder.addProperty(property.build())
}

@OptIn(ExperimentalTime::class)
internal fun StringField.generateField(
    name: String,
    typeSpecBuilder: TypeSpec.Builder,
    constructorBuilder: FunSpec.Builder,
    initCodeBlockBuilder: CodeBlock.Builder,
    companionBuilder: TypeSpec.Builder,
    isRequired: Boolean
) {
    val stringPackage = "com.frybits.starrynight.atproto.models.strings"
    val stringTypeName = when (format) {
        StringFormat.AT_IDENTIFIER -> ClassName(stringPackage, "ATIdentifier")
        StringFormat.DATETIME -> Instant::class.asTypeName()
        StringFormat.DID -> ClassName(stringPackage, "Did")
        StringFormat.HANDLE -> ClassName(stringPackage, "Handle")
        StringFormat.NSID -> ClassName(stringPackage, "Nsid")
        StringFormat.TID -> ClassName(stringPackage, "Tid")
        StringFormat.RECORD_KEY -> ClassName(stringPackage, "RecordKey")
        StringFormat.URI, StringFormat.AT_URI, StringFormat.CID -> URI::class.asTypeName()
        StringFormat.LANGUAGE -> ClassName(stringPackage, "Language")
        null -> String::class.asTypeName()
    }

    // Fixed value for this property
    if (const != null) {
        val property = PropertySpec.builder(name, stringTypeName)
            .addModifiers(KModifier.PUBLIC)

        when (format) {
            StringFormat.DATETIME -> property.initializer("%T.parse(%S)", Instant::class, const)
            StringFormat.URI, StringFormat.AT_URI, StringFormat.CID -> property.initializer("%T.create(%S)", URI::class, const)
            null -> property.initializer("%S", const).addModifiers(KModifier.CONST)
            StringFormat.AT_IDENTIFIER -> {
                if (const.startsWith("did:")) {
                    property.initializer("%T(%S)", ClassName(stringPackage, "Did"), const)
                } else {
                    property.initializer("%T(%S)", ClassName(stringPackage, "Handle"), const)
                }
            }
            else -> property.initializer("%T(%S)", stringTypeName, const)
        }

        if (description != null) {
            property.addKdoc(description)
        }

        companionBuilder.addProperty(property.build())
        return // Return early as nothing else can be done here
    }

    val typeName = stringTypeName.copy(nullable = !isRequired)

    val property = PropertySpec.builder(name, typeName)
        .initializer(name)

    val parameter = ParameterSpec.builder(name, typeName)

    when (format) {
        StringFormat.DATETIME -> parameter.addAnnotation(AnnotationSpec.builder(Serializable::class).addMember("%T::class", ClassName("com.frybits.starrynight.atproto.serializers", "DateTimeSerializer")).build())
        StringFormat.URI, StringFormat.AT_URI, StringFormat.CID -> parameter.addAnnotation(AnnotationSpec.builder(Serializable::class).addMember("%T::class", ClassName("com.frybits.starrynight.atproto.serializers", "URISerializer")).build())
        else -> { /* Do nothing */ }
    }

    if (description != null) {
        parameter.addKdoc(description)
        property.addKdoc(description)
    }

  // TODO Figure out what to do with token references
//    knownValues?.forEach { knownValue ->
//
//    }

    if (default != null) {
        when (format) {
            StringFormat.DATETIME -> parameter.defaultValue("%T.parse(%S)", Instant::class, default)
            StringFormat.URI, StringFormat.AT_URI, StringFormat.CID -> parameter.defaultValue("%T.create(%S)", URI::class, default)
            StringFormat.AT_IDENTIFIER -> {
                if (default.startsWith("did:")) {
                    parameter.defaultValue("%T(%S)", ClassName(stringPackage, "Did"), default)
                } else {
                    parameter.defaultValue("%T(%S)", ClassName(stringPackage, "Handle"), default)
                }
            }
            null -> parameter.defaultValue("%S", default)
            else -> parameter.defaultValue("%T(%S)", stringTypeName, default)
        }
    }

    val outerCodeBlock = CodeBlock.builder()
    val innerCodeBlock = CodeBlock.builder()

    if (initCodeBlockBuilder.isNotEmpty()) {
        outerCodeBlock.addStatement("")
    }

    outerCodeBlock.addStatement("// Begin $name requirements")
    if (!isRequired) {
        outerCodeBlock.beginControlFlow("if (%L != null)", name)
    }
    if (minLength != null) {
        innerCodeBlock.addStatement("require(%L.length >= %L) { %P }", name, minLength, $$"Expected $$name to be greater than or equal to $$minLength. Current size ${$$name.size}")
    }
    if (maxLength != null) {
        innerCodeBlock.addStatement("require(%L.length <= %L) { %P }", name, maxLength, $$"Expected $$name to be less than or equal to $$maxLength. Current size ${$$name.size}")
    }
    if (maxGraphemes != null || minGraphemes != null) {
        innerCodeBlock.addStatement("val graphemes = \"\\\\X\".toRegex().findAll(%L).count()", name)
        if (maxGraphemes != null) {
            innerCodeBlock.addStatement("require(graphemes <= %L) { %P }", maxGraphemes, $$"Expected graphemes length to be less than or equal to $$maxGraphemes")
        }
        if (minGraphemes != null) {
            innerCodeBlock.addStatement("require(graphemes >= %L) { %P }", minGraphemes, $$"Expected graphemes length to be greater than or equal to $$minGraphemes")
        }
    }
    if (enum != null) {
        innerCodeBlock.addStatement("val enumList = setOf(")
        innerCodeBlock.withIndent {
            when (format) {
                StringFormat.DATETIME -> enum.forEach {
                    addStatement("%T.parse(%S),", Instant::class, it)
                }
                StringFormat.URI, StringFormat.AT_URI, StringFormat.CID -> enum.forEach {
                    addStatement("%T.create(%S),", URI::class, it)
                }
                StringFormat.AT_IDENTIFIER -> {
                    enum.forEach {
                        if (it.startsWith("did:")) {
                            addStatement("%T(%S),", ClassName(stringPackage, "Did"), it)
                        } else {
                            addStatement("%T(%S),", ClassName(stringPackage, "Handle"), it)
                        }
                    }
                }
                null -> enum.forEach { addStatement("%S,", it) }
                else -> enum.forEach { addStatement("%T(%S),", typeName, it) }
            }
        }
        innerCodeBlock.addStatement(")")
        innerCodeBlock.addStatement("require(%L in enumList) { %P }", name, $$"Expected $$$name to be in $enumList")
    }
    if (innerCodeBlock.isNotEmpty()) {
        outerCodeBlock.add(innerCodeBlock.build())
    }
    if (!isRequired) {
        outerCodeBlock.endControlFlow()
    }
    outerCodeBlock.addStatement("// End $name requirements")

    if (innerCodeBlock.isNotEmpty()) {
        initCodeBlockBuilder.add(outerCodeBlock.build())
    }

    constructorBuilder.addParameter(parameter.build())
    typeSpecBuilder.addProperty(property.build())
}

internal fun CidLinkField.generateField(
    name: String,
    typeSpecBuilder: TypeSpec.Builder,
    constructorBuilder: FunSpec.Builder,
    isRequired: Boolean
) {
    val typeName = URI::class.asTypeName().copy(nullable = !isRequired)
    val property = PropertySpec.builder(name, typeName)
        .initializer(name)
        .addAnnotation(AnnotationSpec.builder(Serializable::class).addMember("%T::class", ClassName("com.frybits.starrynight.atproto.serializers", "URISerializer")).build())

    val parameter = ParameterSpec.builder(name, typeName)

    if (description != null) {
        parameter.addKdoc(description)
        property.addKdoc(description)
    }

    constructorBuilder.addParameter(parameter.build())
    typeSpecBuilder.addProperty(property.build())
}

internal fun ArrayField.generateField(
    name: String,
    typeSpecBuilder: TypeSpec.Builder,
    constructorBuilder: FunSpec.Builder,
    initCodeBlockBuilder: CodeBlock.Builder,
    companionBuilder: TypeSpec.Builder,
    isRequired: Boolean
) {
    val typeName = List::class.asTypeName()

    val parameterizedType = when (items) {
        is BlobField -> ClassName(packageName = "com.frybits.starrynight.atproto.models.blob", "Blob")
        is BooleanField -> Boolean::class.asTypeName()
        is BytesField -> ByteArray::class.asTypeName()
        is CidLinkField -> URI::class.asTypeName()
        is IntegerField -> Int::class.asTypeName()
        is StringField -> TODO()
        is ArrayField -> TODO()
        is ObjectField -> Map::class.asTypeName().parameterizedBy(String::class.asTypeName(), Any::class.asTypeName())
        is RefField -> {
            val ref = items.ref
            if (ref.contains('#')) {
                val (packageName, className) = ref.split('#')
                ClassName(packageName, className.capitalized())
            } else {
                ClassName(ref, ref.split('.').last().capitalized())
            }
        }
        is TokenField -> TODO("Find out what to do with token references")
        is UnionField -> TODO()
        is UnknownField -> Any::class.asTypeName()
        is PermissionSetField -> TODO()
        is RecordField -> TODO()
        is ProcedureField -> TODO()
        is QueryField -> TODO()
        is SubscriptionField -> TODO()
        is ParamsField -> TODO()
        is RepoPermissionField -> TODO()
        is RpcPermissionField -> TODO()
        else -> throw GradleException("Unable to parameterize array with $items")
    }

    val property = PropertySpec.builder(name, typeName)
        .initializer(name)

    val parameter = ParameterSpec.builder(name, typeName)

    if (description != null) {
        parameter.addKdoc(description)
        property.addKdoc(description)
    }
}
