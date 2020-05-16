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

This syntax is possible because Hood is available in [Gradle Plugin Portal](https://plugins.gradle.org/plugin/com.47deg.hood):

<fortyseven-codetab data-languages='["Groovy", "Kotlin"]' markdown="block">
```groovy
plugins {
  id "com.47deg.hood" version "0.8.1"
}
```

```kotlin
plugins {
  id("com.47deg.hood") version "0.8.1"
}
```
</fortyseven-codetab>

#### Imperative syntax

To use plugin through imperative syntax, you need to first add the dependency on the `buildscript`:

<fortyseven-codetab data-languages='["Groovy", "Kotlin"]' markdown="block">
```groovy
buildscript {
  repositories {
     url "https://plugins.gradle.org/m2/"
  }

  dependencies {
    classpath "com.47deg:hood:0.8.1"
  }
}
```

```kotlin
buildscript {
  repositories {
    maven("https://plugins.gradle.org/m2/")
  }

  dependencies {
    classpath("com.47deg:hood:0.8.1")
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

In case of using a SNAPSHOT version, the dependency should use a different repository:

<fortyseven-codetab data-languages='["Groovy", "Kotlin"]' markdown="block">
```groovy
buildscript {
  repositories {
     url "https://oss.jfrog.org/artifactory/oss-snapshot-local/"
  }

  dependencies {
    classpath "com.47deg:hood:0.8.2-SNAPSHOT"
  }
}
```

```kotlin
buildscript {
  repositories {
    maven("https://oss.jfrog.org/artifactory/oss-snapshot-local/")
  }

  dependencies {
    classpath("com.47deg:hood:0.8.2-SNAPSHOT")
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
