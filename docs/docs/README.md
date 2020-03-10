---
layout: docs
title: Quick Start
permalink: gradle/
---
# Hood

**Hood** is a `Gradle` plugin to compare benchmarks and set the result as a `Github` status for a `Pull Request`.
**Hood** is built on [Arrow](https://arrow-kt.io/), a Functional companion to Kotlin's Standard Library.

## Tasks

**Hood** has two tasks to compare benchmarks:
 - `compareBenchmarks`: Compare two or more benchmarks and print the result.
 - `compareBenchmarksCI`: Compare two or more benchmarks and upload a `Github` status for a `Pull Request`.

and an extra task `uploadBenchmarks` to upload Benchmarks files and keep them up to date.

## Adding the Hood dependency

To add the Hood plugin dependency on Gradle, you can use:

#### Declarative syntax (especially recommended for the Kotlin DSL)

```groovy
//Groovy
plugins {
  id "com.47deg.hood" version "0.8.0"
}
```

```kotlin
//Kotlin
plugins {
  id("com.47deg.hood") version "0.8.0"
}
```

Don't forget to add the `pluginManagement` block at the top of your `settings.gradle/.kts` if you are not able to find it:

```groovy
//Groovy
pluginManagement {
  repositories {
    maven { url "https://dl.bintray.com/47deg/hood" }
    gradlePluginPortal()
  }
}
```

```kotlin
//Kotlin
pluginManagement {
  repositories {
    maven("https://dl.bintray.com/47deg/hood")
    gradlePluginPortal()
  }
}
```

#### Imperative syntax

To use plugin through imperative syntax, you need to first add the dependency on the `buildscript`:

```groovy
//Groovy
buildscript {
  repositories {
    maven { url "https://dl.bintray.com/47deg/hood" }
  }

  dependencies {
    classpath "com.47deg:hood:0.8.0"
  }
}
```

```kotlin
//Kotlin
buildscript {
  repositories {
    maven("https://dl.bintray.com/47deg/hood")
  }

  dependencies {
    classpath("com.47deg:hood:0.8.0")
  }
}
```

and then you will be able to add it with `apply`:

```groovy
//Groovy
apply plugin: "com.47deg.hood"
```

```kotlin
//Kotlin
apply(plugin = "com.47deg.hood")
```

## The visualizer companion

**Hood** has a companion called [Robeen](https://github.com/47deg/robeen) to show the benchmark comparison in a visual way.

## Projects using Hood

 - [Helios](https://47deg.github.io/helios/)
