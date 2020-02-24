# Hood

**Hood** is a `Gradle` plugin to compare benchmarks and set the result as a `Github` status for a `Pull Request`.
**Hood** is built on [Arrow](https://arrow-kt.io/), a Functional companion to Kotlin's Standard Library.

## How to use it

**Hood** has two tasks to compare benchmarks:
 - `compareBenchmarks`: Compare two benchmarks and print the result.
 - `compareBenchmarksCI`: Compare two benchmarks and upload a `Github` status for a `Pull Request`.

Both tasks have common parameters:
 - **previousBenchmarkPath**: File with previous or master benchmark location. By default: `master.csv`.
 - **currentBenchmarkPath**: List of files with current or pull request benchmark location. By default is an `empty list`.
 - **keyColumnName**: Column name to distinct each benchmark on the comparison. By default: `Benchmark`.
 - **compareColumnName**: Column name of the column to compare (the values must to be a `Double`). By default: `Score`.
 - **thresholdColumnName**: Column name to get the threshold per benchmark. By default: `Score Error (99.9%)`.
 - **generalThreshold**: Common threshold to all benchmarks overriding the value coming from `thresholdColumnName`. Optional.
 - **benchmarkThreshold**: `Map` with a custom threshold per benchmark key overriding the value coming from `thresholdColumnName` or `generalThreshold`. Optional.
 - **include**: Regular expression to include only the benchmarks with a matching key. Optional.
 - **exclude**: Regular expression to exclude the benchmarks using its key. Optional.
 
The `include/exclude` feature and `benchmarkThreshold` param use the cleaned key from benchmarks. 
This means the key for `hood.comparing` will be `Comparing` with the capitalization.

The `compareBenchmarksCI` task also needs some extra parameters:
 - **token**: The `Github` access token.
 - **repositoryOwner**: The repository owner.
 - **repositoryName**: The repository name.
 - **pullRequestSha**: The sha for the Pull Request. The environment variable `TRAVIS_PULL_REQUEST_SHA` on Travis CI.
 - **pullRequestNumber**: The number of the Pull Request. The environment variable `TRAVIS_PULL_REQUEST` on Travis CI.
 - **statusTargetUrl**: The URL to the CI job. The environment variable `TRAVIS_JOB_WEB_URL` on Travis CI. Optional.

***Note***: Currently **Hood** only supports `CSV` and `JSON` based benchmarks with cross comparison available. 
***Note 2***: If the `CI` integration is not available because one the requested fields above is not defined, 
  the task `compareBenchmarksCI` will be executed on the same way as `compareBenchmarks`.

### Send output to a file

Both task can send the result to a file, just need to fulfill the following parameters:
 - **outputToFile**: If send the output to a file. By default: `false`.
 - **outputPath**: The path to the output file. By default: `./hood/comparison`.
 - **outputFormat**: The output file format, we support two formats `MD` and `JSON`. By default: `MD`.

***Note***: To print a `JSON` output file, all the benchmarks must to be in `JSON` format. `CSV` benchmarks will be ignored.

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