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

<fortyseven-codetab data-languages='["Groovy", "Kotlin"]' markdown="block">
```groovy
plugins {
  id "com.47deg.hood" version "0.8.0"
}
```

```kotlin
plugins {
  id("com.47deg.hood") version "0.8.0"
}
```
</fortyseven-codetab>

Don't forget to add the `pluginManagement` block at the top of your `settings.gradle/.kts` if you are not able to find it:

<fortyseven-codetab data-languages='["Groovy", "Kotlin"]' markdown="block">
```groovy
pluginManagement {
  repositories {
    maven { url "https://dl.bintray.com/47deg/hood" }
    gradlePluginPortal()
  }
}
```

```kotlin
pluginManagement {
  repositories {
    maven("https://dl.bintray.com/47deg/hood")
    gradlePluginPortal()
  }
}
```
</fortyseven-codetab>

#### Imperative syntax

To use plugin through imperative syntax, you need to first add the dependency on the `buildscript`:

<fortyseven-codetab data-languages='["Groovy", "Kotlin"]' markdown="block">
```groovy
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
buildscript {
  repositories {
    maven("https://dl.bintray.com/47deg/hood")
  }

  dependencies {
    classpath("com.47deg:hood:0.8.0")
  }
}
```
</fortyseven-codetab>

and then you will be able to add it with `apply`:

<fortyseven-codetab data-languages='["Groovy", "Kotlin"]' markdown="block">
```groovy
apply plugin: "com.47deg.hood"
```

```kotlin
apply(plugin = "com.47deg.hood")
```
</fortyseven-codetab>

## The visualizer companion

**Hood** has a companion called [Robeen](https://github.com/47degrees/robeen) to show the benchmark comparison in a visual way.

## Projects using Hood

 - [Helios](https://47degrees.github.io/helios/)
