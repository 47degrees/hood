import com.fortysevendeg.hood.CompareBenchmark
import com.jfrog.bintray.gradle.BintrayExtension

plugins {
  kotlin("jvm") version "1.3.20"
  id("com.fortysevendeg.hood")
  id("com.jfrog.bintray") version "1.8.4"
}

repositories {
  mavenCentral()
  maven("https://plugins.gradle.org/m2/")
  jcenter()
}

dependencies {
  implementation(kotlin("stdlib-jdk8"))
}

tasks.getByName<CompareBenchmark>("compareBenchmark") {
  previousBenchmarkPath = file("./buildSrc/src/main/resources/master_benchmark.csv")
  currentBenchmarkPath = listOf(file("./buildSrc/src/main/resources/current_benchmark.csv"))
}

bintray {
  fun findProperty(s: String) = project.findProperty(s) as String?

  publish = true
  user = findProperty("bintrayUser") ?: System.getenv("BINTRAY_USER")
  key = findProperty("bintrayApiKey") ?: System.getenv("BINTRAY_API_KEY")
  pkg(delegateClosureOf<BintrayExtension.PackageConfig> {
    repo = "hood"
    name = project.name
    userOrg = System.getenv("POM_DEVELOPER_ID")
    setConfigurations("archives")
    setLicenses("Apache-2.0")
    vcsUrl = "https://github.com/47deg/hood.git"
  })
}
