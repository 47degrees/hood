import com.fortysevendeg.hood.CompareBenchmark

plugins {
  kotlin("jvm") version "1.3.20"
  id("com.fortysevendeg.hood")
}

repositories {
  mavenCentral()
}

group = "com.fortysevendeg"
version = "0.0.1-SNAPSHOT"

dependencies {
  implementation(kotlin("stdlib-jdk8"))
}

tasks.getByName<CompareBenchmark>("compareBenchmark") {
  previousBenchmarkPath = file("./buildSrc/src/main/resources/master_benchmark.csv")
  currentBenchmarkPath = listOf(file("./buildSrc/src/main/resources/current_benchmark.csv"))
}