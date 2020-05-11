---
layout: docs
title: Continuous integration
permalink: gradle/ci/

---

# Compare Benchmarks with CI

**Hood** gives you the ability to integrate the benchmark comparison into your `CI`
 and integrate the result into a `Github` pull request.

It provides `compareBenchmarksCI` Gradle task which compares 2 reports from a benchmark and then it creates a new comment with the comparison result in the pull request. For instance, [look at this comment](https://github.com/47degrees/helios/pull/137#issuecomment-597753181). If new commits are added to the same pull request, the comment will be updated with the new comparison result.

The `compareBenchmarksCI` task has parameters in common with the `compareBenchmarks` task:
 - **previousBenchmarkPath**: File with previous or master benchmark location. By default: `master.csv`.
 - **currentBenchmarkPath**: List of files with current or pull request benchmark location. By default: an `empty list`.
 - **keyColumnName**: Column name to distinguish each benchmark on the comparison. By default: `Benchmark`.
 - **compareColumnName**: Column name of the column to compare (the values must be a `Double`). By default: `Score`.
 - **thresholdColumnName**: Column name to get the threshold per benchmark. By default: `Score Error (99.9%)`.
 - **generalThreshold**: Common threshold for all benchmarks overriding the value coming from `thresholdColumnName`. Optional.
 - **benchmarkThreshold**: `Map` with a custom threshold per benchmark key overriding the value coming from `thresholdColumnName` or `generalThreshold`. Optional.
 - **include**: Regular expression to include only the benchmarks with a matching key. Optional.
 - **exclude**: Regular expression to exclude the benchmarks using its key. Optional.

The `include/exclude` feature and `benchmarkThreshold` param use the cleaned key from benchmarks.
This means the key for `hood.comparing` will be `Comparing` with the capitalization.

These extra parameters are necessary for the `CI` integration:
 - **token**: The `Github` access token.
 - **repositoryOwner**: The repository owner.
 - **repositoryName**: The repository name.
 - **pullRequestSha**: The sha for the Pull Request.
 - **pullRequestNumber**: The number of the Pull Request.
 - **statusTargetUrl**: The URL to the CI job. Optional.

***Note***: Currently, **Hood** only supports `CSV` and `JSON` based benchmarks with cross comparison available.

***Note 2***: If the `CI` integration is not available because one of the requested fields above is not defined,
  the task `compareBenchmarksCI` will be executed in the same way as `compareBenchmarks`.

## Send output to a file

The task can send the result to a file with the following parameters:
 - **outputToFile**: Sends the output to a file. By default: `false`.
 - **outputPath**: The path to the output file. By default: `./hood/comparison`.
 - **outputFormat**: The output file format; we support two formats `MD` and `JSON`. By default: `MD`.

***Note***: To print a `JSON` output file, all the benchmarks must be in `JSON` format. `CSV` benchmarks will be ignored.

## Configuration examples

### For GitHub Actions

There isnt't an environment variable for the pull request number when writing this documentation. Please, check [the default environment variables list](https://help.github.com/en/actions/configuring-and-managing-workflows/using-environment-variables#default-environment-variables) in case that environment variable already exists. Meanwhile here a trick to get it before calling the Gradle task in the GitHub Action:

```
export PULL_REQUEST_NUMBER=$(echo $GITHUB_REF | cut -d/ -f3)
./gradlew :<module>:compareBenchmarksCI
```

An example of the configuration for the `compareBenchmarksCI` task:


<fortyseven-codetab data-languages='["Groovy", "Kotlin"]' markdown="block">
```groovy
compareBenchmarksCI {
  previousBenchmarkPath = file("$rootDir/hood_master/build/reports/master_benchmark.json")
  currentBenchmarkPath = [file("$rootDir/build/reports/hood_benchmark.json")]
  outputToFile = true
  outputFormat = "json"
  benchmarkThreshold = ["Parsing": 250.00, "Decodingfromraw": 250.00]
  token = System.getenv("GITHUB_ACCESS_TOKEN")
  repositoryOwner = "47degrees"
  repositoryName = "hood"
  pullRequestSha = System.getenv("GITHUB_SHA")
  pullRequestNumber = (System.getenv("PULL_REQUEST_NUMBER") != "false") ? System.getenv("PULL_REQUEST_NUMBER")?.toInteger() : -1
}
```

```kotlin
tasks.compareBenchmarksCI {
  previousBenchmarkPath = file("$rootDir/hood_master/build/reports/master_benchmark.json")
  currentBenchmarkPath = listOf(file("$rootDir/build/reports/hood_benchmark.json"))
  outputToFile = true
  outputFormat = "json"
  benchmarkThreshold = mapOf("Parsing" to 250.00, "Decodingfromraw" to 250.00)
  token = System.getenv("GITHUB_ACCESS_TOKEN")
  repositoryOwner = "47degrees"
  repositoryName = "hood"
  pullRequestSha = System.getenv("GITHUB_SHA")
  pullRequestNumber =
    if (System.getenv("PULL_REQUEST_NUMBER") != "false") System.getenv("PULL_REQUEST_NUMBER").toInt() else -1
}
```
</fortyseven-codetab>

### For Travis CI

<fortyseven-codetab data-languages='["Groovy", "Kotlin"]' markdown="block">
```groovy
compareBenchmarksCI {
  previousBenchmarkPath = file("$rootDir/hood_master/build/reports/master_benchmark.json")
  currentBenchmarkPath = [file("$rootDir/build/reports/hood_benchmark.json")]
  outputToFile = true
  outputFormat = "json"
  benchmarkThreshold = ["Parsing": 250.00, "Decodingfromraw": 250.00]
  token = System.getenv("GITHUB_ACCESS_TOKEN")
  repositoryOwner = "47degrees"
  repositoryName = "hood"
  pullRequestSha = System.getenv("TRAVIS_PULL_REQUEST_SHA")
  pullRequestNumber = (System.getenv("TRAVIS_PULL_REQUEST") != "false") ? System.getenv("TRAVIS_PULL_REQUEST")?.toInteger() : -1
  statusTargetUrl = System.getenv("TRAVIS_JOB_WEB_URL") ? URI.create(System.getenv("TRAVIS_JOB_WEB_URL")) : null
}
```

```kotlin
tasks.compareBenchmarksCI {
  previousBenchmarkPath = file("$rootDir/hood_master/build/reports/master_benchmark.json")
  currentBenchmarkPath = listOf(file("$rootDir/build/reports/hood_benchmark.json"))
  outputToFile = true
  outputFormat = "json"
  benchmarkThreshold = mapOf("Parsing" to 250.00, "Decodingfromraw" to 250.00)
  token = System.getenv("GITHUB_ACCESS_TOKEN")
  repositoryOwner = "47degrees"
  repositoryName = "hood"
  pullRequestSha = System.getenv("TRAVIS_PULL_REQUEST_SHA")
  pullRequestNumber =
    if (System.getenv("TRAVIS_PULL_REQUEST") != "false") System.getenv("TRAVIS_PULL_REQUEST").toInt() else -1
  statusTargetUrl = (System.getenv("TRAVIS_JOB_WEB_URL") ?: URI.create(System.getenv("TRAVIS_JOB_WEB_URL"))) as URI?
}
```
</fortyseven-codetab>
