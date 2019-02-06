# Hood

***Hood*** is a `Gradle` plugin to compare benchmarks and set the result as a `Github` status for a `Pull Request`.

## How to use it

***Hood*** has two task:
 - `compareBenchmarks`: Compare two benchmarks and print the result.
 - `compareBenchmarksCI`: Compare two benchmarks and upload a `Github` status for a `Pull Request`.
 
 Both tasks have common parameters:
  - previousBenchmarkPath: File with previous or master benchmark location.
  - currentBenchmarkPath: List of files with current or pull request benchmark location.
  - keyColumnName: Column name to distinct each benchmark on the comparison.
  - compareColumnName: Column name of the column to compare(the values must to be a Double).
  - threshold: Maximum differentiation for negative result.
  
 The `compareBenchmarksCI` task also needs as extra parameter the `Github` access token and only works with `travis-ci`.
 
 *Note*: Currently ***Hood*** only supports `csv` based benchmarks.