# Starry Night

Although the end result (hopefully) is an app client that can be used with BlueSky, the objective is to allow for a playground of the latest build tools, plugins, and libraries on an active app.

## Project Setup

Each project (aka "module") is declared in the [all-projects.txt](gradle/all-projects.txt) file, with a single Gradle Settings plugin consuming this list. The plugin ([FrybitsSettingsPlugin.kt](build-logic/plugins/src/main/kotlin/com/frybits/gradle/plugins/FrybitsSettingsPlugin.kt)) handles the bulk of the configuration for the root project.
All sub-project configuration is found in the individual `build.toml` files declared. No `build.gradle(.kts)` files are allowed, nor are the `build.gradle.dcl` files (yet).

### Project Build File
All projects can have (but may not enable) the following plugins:
```toml
# Enables compose for the project. Not available for "atprotoLibrary" project types.
enableCompose = true

# Enables metro for the project.
enableMetro = true

# Enables the Room plugin for the project. Not available for "atprotoLibrary" project types.
enableRoom = true

# Enables the Square/Wire plugin for the project. Not available for "atprotoLibrary" project types.
enableWire = true
```

After all other configurations are declared, the `build.toml` files allow dependencies to be declared as such:
```toml
# Libraries to be used (maven address or version catalog name)
[dependencies]

# This declares the configuration to use. Can be any valid configuration already created by the build-logic. Configurations not already registered will result in build failure.
implementation = [
    # Basic dependency types are: "library", "project", and "platform"
    
    # Library types will only take names declared in the `libs.versions.toml` file
    { type = "library", name = "okhttp" },
    
    # Project types will only take the full Gradle path
    { type = "project", name = ":atproto:api" },
    
    # Platforms can be declared and require another internal type to define the BOM or version catalog, which must be either "library" or "project" types.
    { type = "platform", module = { type = "library", name = "androidx.compose.bom" } },
]

# Variant specific configuraitons are also allowed
debugApi = [
    { type = "library", name = "retrofit" },
]
```

The following are project type specific settings beyond those listed above:

#### Java Library
This library type is the basic Java library. It allows all the common configurations listed above to be used.

Example:
```toml
# Project type
type = "javaLibrary"

# Libraries to be used (maven address or version catalog name)
[dependencies]
api = [
    { type = "library", name = "kotlinx.coroutines.core" },
]
```

#### ATProto Library
This library type is specific for the ATProto configuration and plugins. This is also a basic Java library, but with certain restrictions. Only dependencies, the Metro plugin, a custom Lexicon list are allowed for this project type. At the moment, only one project has this type, though that may change in the future.

Example:
```toml
# Project type
type = "atprotoLibrary"

# Lexicons to download and compile
lexicons = [
    "com.atproto.server.createSession",
]

enableMetro = true

# Libraries to be used (maven address or version catalog name)
[dependencies]
implementation = [
    { type = "library", name = "okhttp" },
]
```

#### Android
There are currently only 2 Android project types: Library and Application. Each share the common configurations also found in the Java library type, as well as the following:
```toml
# Namespace to use for the app/library. This is the same as calling `android.namespace` in build.gradle files.
namespace = "com.frybits.starrynight.android"

# Build configs are only enabled when the following field is declared
[buildConfigs]

# The build configs are split in map and processed based on the build type. The `default` build configs are applied to all build types.
default = [
    
    # A build config field has the following setup:
    #  Type - The variable type of the field
    #  Key - The name of the field
    #  Property - The value of the field.
    
    # The build config properties can be of types: "literal", "gradle", "environmental", and "system".
    # type = "literal" - These types will read the `value` field directly 
    { type = "String", key = "APP_NAME", property = { type = "literal", value = "\"Starry Night\"" } },
    
    # type = "gradle" - These types will read the `value` from the Gradle properties, via the `providers.gradleProperty()` function.
    { type = "String", key = "APP_NAME", property = { type = "gradle", value = "com.frybits.name" } },

    # type = "environmental" - These types will read the `value` from the environmental variables, via the `providers.environmentVariable()` function.
    { type = "String", key = "IS_CI", property = { type = "environmental", value = "IS_CI" } },

    # type = "system" - These types will read the `value` from the system properties passed into the JVM, via the `providers.systemProperty()` function.
    { type = "String", key = "USER_NAME", property = { type = "system", value = "user.name" } },
]
```

##### Android Application
The following configurations are available only on the Android application project types:
```toml
# The Android applcation ID
applicationId = "com.frybits.starrynight.android"

# The app target SDK
targetSdk = 33

# The preview app target, if developing against the preview SDKs.
# NOTE: This will override the above target SDK.
previewTargetSdk = "CinnamonBun"
```

All configurations can be found in [BuildFile.kt](build-logic/core/src/main/kotlin/com/frybits/gradle/core/definitions/BuildFile.kt).

## How to Build/Configure

This project can be easily built by running `./gradlew :starrynight:app:assembleDebug`. There are some Gradle properties you can pass to change specifics of how the project is built.
Many of the properties that can be manipulated are found in the root [gradle.properties](./gradle.properties) file.


