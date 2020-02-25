---
layout: docs
title: Quick Start
permalink: docs/
---
# Hood

**Hood** is a `Gradle` plugin to compare benchmarks and set the result as a `Github` status for a `Pull Request`.
**Hood** is built on [Arrow](https://arrow-kt.io/), a Functional companion to Kotlin's Standard Library.

## Tasks

**Hood** has two tasks to compare benchmarks:
 - `compareBenchmarks`: Compare two or more benchmarks and print the result.
 - `compareBenchmarksCI`: Compare two or more benchmarks and upload a `Github` status for a `Pull Request`.
 
and an extra task `uploadBenchmarks` to upload Benchmarks files and keep them up to date.

## The visualizer companion

**Hood** has a companion called [Robeen](https://github.com/47deg/robeen) to show the benchmark comparison on a visual way.

## Projects using Hood
 
 - [Helios](https://47deg.github.io/helios/)