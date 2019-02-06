plugins {
  `kotlin-dsl`
  `java-gradle-plugin`
}

repositories {
  jcenter()
}

dependencies {
  val arrowVersion = "0.8.2"
  val http4kVersion = "3.112.0"
  implementation("io.arrow-kt:arrow-core:$arrowVersion")
  implementation("io.arrow-kt:arrow-data:$arrowVersion")
  implementation("io.arrow-kt:arrow-syntax:$arrowVersion")
  implementation("io.arrow-kt:arrow-typeclasses:$arrowVersion")
  implementation("io.arrow-kt:arrow-effects:$arrowVersion")
  implementation("io.arrow-kt:arrow-effects-instances:$arrowVersion")
  implementation("io.arrow-kt:arrow-data-instances-effects:$arrowVersion")

  implementation("org.http4k:http4k-core:$http4kVersion")
  implementation("org.http4k:http4k-client-okhttp:$http4kVersion")
  implementation("org.http4k:http4k-format-jackson:$http4kVersion")

  implementation("org.apache.commons:commons-csv:1.5")
}

gradlePlugin {
  plugins {
    register("hood-plugin") {
      id = "com.fortysevendeg.hood"
      implementationClass = "com.fortysevendeg.hood.HoodPlugin"
    }
  }
}