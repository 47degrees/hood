plugins {
  `kotlin-dsl`
  `java-gradle-plugin`
}

repositories {
  jcenter()
}

dependencies {
  val arrow_version = "0.8.2"
  implementation("org.apache.commons:commons-csv:1.5")
  implementation("io.arrow-kt:arrow-core:$arrow_version")
  implementation("io.arrow-kt:arrow-syntax:$arrow_version")
  implementation("io.arrow-kt:arrow-typeclasses:$arrow_version")
  implementation("io.arrow-kt:arrow-effects:$arrow_version")
}

gradlePlugin {
  plugins {
    register("hood-plugin") {
      id = "com.adrianrafo.hood"
      implementationClass = "com.adrianrafo.hood.GreetPlugin"
    }
  }
}