---
layout: docs
title: Upload
permalink: docs/upload/

---

## Upload benchmarks

**Hood** allow you to upload automatically the benchmarks 
you want to maintain updated in your code thought commits during the CI.

The task `uploadBenchmarks` has the following parameters:
 - **benchmarkFiles**: The list of benchmark files you want to upload. By default is an `empty list`.
 - **uploadDirectory**: The path for the folder where you want to keep them, from project root directory. By default: `benchmarks`.
 - **commitMessage**: The message for the task commit uploading the benchmark. By default: `Upload benchmark`.
 - **token**: the `Github` access token.
 - **repositoryOwner**: The repository owner.
 - **repositoryName**: The repository name.
 - **branch**: The branch where you want to upload those benchmarks. By default: `master`.
 
 ### Gradle configuration example
 
 ```groovy
uploadBenchmark {
    benchmarkFiles = [file("$rootDir/build/reports/master_benchmark.json"), file("$rootDir/build/reports/libraries_benchmark.json")]
    token = System.getenv("GITHUB_ACCESS_TOKEN")
    repositoryOwner = "47deg"
    repositoryName = "hood"
    commitMessage = "[ci skip] - Upload benchmark"
}
```