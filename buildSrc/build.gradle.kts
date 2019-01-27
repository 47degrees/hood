plugins {
  `kotlin-dsl`
  `java-gradle-plugin`
}

repositories {
  jcenter()
}

dependencies {
  val arrowVersion = "0.8.2"
  val http4kVersion = "3.107.0"
  implementation("org.apache.commons:commons-csv:1.5")
  implementation("io.arrow-kt:arrow-core:$arrowVersion")
  implementation("io.arrow-kt:arrow-data:$arrowVersion")
  implementation("io.arrow-kt:arrow-syntax:$arrowVersion")
  implementation("io.arrow-kt:arrow-typeclasses:$arrowVersion")
  implementation("io.arrow-kt:arrow-effects:$arrowVersion")
  implementation("io.arrow-kt:arrow-effects-instances:$arrowVersion")
  implementation("io.arrow-kt:arrow-data-instances-effects:$arrowVersion")

  implementation("org.http4k:http4k-core:$http4kVersion")
  implementation("org.http4k:http4k-client-apache-async:$http4kVersion")
}

gradlePlugin {
  plugins {
    register("hood-plugin") {
      id = "com.adrianrafo.hood"
      implementationClass = "com.adrianrafo.hood.HoodPlugin"
    }
  }
}