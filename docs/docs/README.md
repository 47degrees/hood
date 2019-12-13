---
layout: docs
title: Quick Start
permalink: docs/
---

# Quick Start h1
Lorem ipsum dolor sit amet, consectetur adipisicing elit. Quae sapiente quidem culpa sit ab ipsam alias repellat sequi. Quae tempora nulla consequuntur quibusdam aut cupiditate eum ratione, voluptatum error repellat.

## Titlte h2
Lorem ipsum dolor sit amet, consectetur adipisicing elit. Quae sapiente quidem culpa sit ab ipsam alias repellat sequi. Quae tempora nulla consequuntur quibusdam aut cupiditate eum ratione, voluptatum error repellat.

### Titlte h3
Lorem ipsum dolor sit amet, consectetur adipisicing elit. Quae sapiente quidem culpa sit ab ipsam alias repellat sequi. Quae tempora nulla consequuntur quibusdam aut cupiditate eum ratione, voluptatum error repellat.

#### Titlte h4
Lorem ipsum dolor sit amet, consectetur adipisicing elit. Quae sapiente quidem culpa sit ab ipsam alias repellat sequi. Quae tempora nulla consequuntur quibusdam aut cupiditate eum ratione, voluptatum error repellat.

##### Titlte h5
Lorem ipsum dolor sit amet, consectetur adipisicing elit. Quae sapiente quidem culpa sit ab ipsam alias repellat sequi. Quae tempora nulla consequuntur quibusdam aut cupiditate eum ratione, voluptatum error repellat.

###### Titlte h6
Lorem ipsum dolor sit amet, consectetur adipisicing elit. Quae sapiente quidem culpa sit ab ipsam alias repellat sequi. Quae tempora nulla consequuntur quibusdam aut cupiditate eum ratione, voluptatum error repellat.

* Lorem ipsum dolor sit amet, consectetur adipisicing elit
* Lorem ipsum dolor sit amet, consectetur
* Lorem ipsum dolor sit amet, consectetur adipisicing elit quae sapiente

# Inline text elements
* Lorem ipsum dolor sit amet, *consectetur* adipisicing _elitquae_ sapiente
* Lorem ipsum dolor sit amet, **consectetur** adipisicing __elitquae__ sapiente
* Lorem ipsum dolor sit amet, **consectetur adipisicing _elitquae_** sapiente
* Lorem ipsum dolor sit amet, consectetur ~~adipisicing elitquae~~ sapiente

# Code
Lorem `ipsum dolor` sit amet, consectetur `adipisicing elitquae` sapiente

```
repositories {
    maven { url = uri("https://dl.bintray.com/47deg/helios") }
}

dependencies {
    compile "com.47deg:helios-core:0.2.0"
    compile "com.47deg:helios-parser:0.2.0"
    compile "com.47deg:helios-optics:0.2.0"
    kapt "com.47deg:helios-meta:0.2.0"
    kapt "com.47deg:helios-dsl-meta:0.2.0"
}
```

```
val jsonStr =
"""{
     "name": "Simon",
     "age": 30
   }"""

val jsonFromString : Json =
  Json.parseFromString(jsonStr).getOrHandle {
    println("Failed creating the Json ${it.localizedMessage}, creating an empty one")
    JsString("")
  }

val personOrError: Either<DecodingError, Person> = Person.decoder().decode(jsonFromString)

personOrError.fold({
  "Something went wrong during decoding: $it"
}, {
  "Successfully decode the json: $it"
})
```
