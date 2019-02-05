import com.adrianrafo.hood.CompareBenchmark

plugins {
  kotlin("jvm") version "1.3.20"
  id("com.adrianrafo.hood")
}

repositories {
  mavenCentral()
}

group = "com.adrianrafo"
version = "0.0.1-SNAPSHOT"

dependencies {
  implementation(kotlin("stdlib-jdk8"))
}

tasks.getByName<CompareBenchmark>("compareBenchmark") {
  previousBenchmarkPath = "./buildSrc/src/main/resources/master_benchmark.csv"
  currentBenchmarkPath = "./buildSrc/src/main/resources/current_benchmark.csv"
}