---
layout: docs
title: Continuous integration
permalink: docs/ci/

---

# Compare Benchmarks with CI

**Hood** gives you the ability to integrate the benchmark comparison into your `CI`
 and integrate the result into a `Github` pull request.

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
 - **pullRequestSha**: The sha for the Pull Request. The environment variable `TRAVIS_PULL_REQUEST_SHA` on Travis CI.
 - **pullRequestNumber**: The number of the Pull Request. The environment variable `TRAVIS_PULL_REQUEST` on Travis CI.
 - **statusTargetUrl**: The URL to the CI job. The environment variable `TRAVIS_JOB_WEB_URL` on Travis CI. Optional.

***Note***: Currently, **Hood** only supports `CSV` and `JSON` based benchmarks with cross comparison available. 
***Note 2***: If the `CI` integration is not available because one of the requested fields above is not defined, 
  the task `compareBenchmarksCI` will be executed in the same way as `compareBenchmarks`.

### Send output to a file

The task can send the result to a file with the following parameters:
 - **outputToFile**: Sends the output to a file. By default: `false`.
 - **outputPath**: The path to the output file. By default: `./hood/comparison`.
 - **outputFormat**: The output file format; we support two formats `MD` and `JSON`. By default: `MD`.

***Note***: To print a `JSON` output file, all the benchmarks must be in `JSON` format. `CSV` benchmarks will be ignored.

### Configuration example

```groovy
compareBenchmarksCI {
  previousBenchmarkPath = file("$rootDir/hood_master/build/reports/master_benchmark.json")
  currentBenchmarkPath = [file("$rootDir/build/reports/hood_benchmark.json")]
  outputToFile = true
  outputFormat = "json"
  benchmarkThreshold = ["Parsing": 250.00, "Decodingfromraw": 250.00]
  token = System.getenv("GITHUB_ACCESS_TOKEN")
  repositoryOwner = "47deg"
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
  repositoryOwner = "47deg"
  repositoryName = "hood"
  pullRequestSha = System.getenv("TRAVIS_PULL_REQUEST_SHA")
  pullRequestNumber =
    if (System.getenv("TRAVIS_PULL_REQUEST") != "false") System.getenv("TRAVIS_PULL_REQUEST").toInt() else -1
  statusTargetUrl = (System.getenv("TRAVIS_JOB_WEB_URL") ?: URI.create(System.getenv("TRAVIS_JOB_WEB_URL"))) as URI?
}
```
