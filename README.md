# Hood

**Hood** is a `Gradle` plugin to compare benchmarks and set the result as a `Github` status for a `Pull Request`.

## How to use it

**Hood** has two tasks to compare benchmarks:
 - `compareBenchmarks`: Compare two benchmarks and print the result.
 - `compareBenchmarksCI`: Compare two benchmarks and upload a `Github` status for a `Pull Request`.
 
 Both tasks have common parameters:
  - **previousBenchmarkPath**: File with previous or master benchmark location.
  - **currentBenchmarkPath**: List of files with current or pull request benchmark location.
  - **keyColumnName**: Column name to distinct each benchmark on the comparison.
  - **compareColumnName**: Column name of the column to compare(the values must to be a `Double`).
  - **threshold**: Maximum differentiation for negative result. By default: `50`
  
 The `compareBenchmarksCI` task also needs an extra parameter, **token**, the `Github` access token. 
 At this moment, only `travis-ci` is supported.
 
 ***Note***: Currently **Hood** only supports `csv` based benchmarks.
 
 ### Send output to a file
 Both task can send the result to a file, just need to fulfill the following parameters:
  - **outputToFile**: If send the output to a file. By default: `false`
  - **outputPath**: The path to the output file. By default: `hood/comparison`
  - **outputFormat**: The output file format, currently we just support `md`. By default: `md`
 
 ## Upload benchmarks
 
 **Hood** allow you to upload automatically the benchmark 
 you want to maintain updated in your code thought commits during the CI.
 
 The task `uploadBenchmark` has the following parameters:
  - **benchmarkFile**: The benchmark file you want to upload.
  - **benchmarkDestinationFromProjectRoot**: The path for the file where you want to keep it, from project root directory.
  - **commitMessage**: The message for the task commit uploading the benchmark. By default: `Upload benchmark`.
  - **token**: the `Github` access token.
