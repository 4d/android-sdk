# SDK

Generate a set of libraries that will be used to build a generated app.

## Prerequisites

You can build the SDK with either Windows or macOS operating systems.

To build Android SDK, you should download Android Studio:

- [Android Studio](https://developer.android.com/studio) : version requirements in [documentation](https://github.com/doc4d/go-mobile/blob/main/docs/getting-started/requirements.md#android)
- Java 11 is required for latest versions of the sdk. Java 11 is embedded in recent Android Studio versions.

## Build

Run `./gradlew clean --refresh-dependencies mavenDependencyExport`

And get the libraries in the `dependencies/` directory


## Deploy

The SDK used by "4D Mobile App" will be copyed by project from cache folder.

By default the Android SDK must be installed into: `/Library/Caches/com.4D.mobile/sdk/<version>/Android/sdk`

- with `version`, the 4d version represented as 4 digits (for instance v20=2000 , v20R2=2020)

If no SDK found it will be downloaded from latest release of this project.

## SDK frameworks

### SDK ones (ie. QMobile)

| Name | License | Usefulness |
|-|-|-|
| [QMobileAPI](https://github.com/4d/android-QMobileAPI) | [4D](https://github.com/4d/android-QMobileAPI/blob/master/LICENSE.md) | Network api |
| [QMobileDataStore](https://github.com/4d/android-QMobileDataStore) | [4D](https://github.com/4d/android-QMobileDataStore/blob/master/LICENSE.md) | Store data |
| [QMobileDataSync](https://github.com/4d/android-QMobileDataSync) | [4D](https://github.com/4d/android-QMobileDataSync/blob/master/LICENSE.md) | Synchronize data |
| [QMobileUI](https://github.com/4d/android-QMobileUI) | [4D](https://github.com/4d/android-QMobileUI/blob/master/LICENSE.md) | Graphic, Application, Features |

### 3rd parties


#### Core

| Name | License | Usefulness |
|-|-|-|
| [Glide](https://github.com/bumptech/glide) | [Apache 2.0](https://github.com/bumptech/glide/blob/master/LICENSE) | Image loading | 
| [Kotlin Coroutines](https://github.com/Kotlin/kotlinx.coroutines) | [Apache 2.0](https://github.com/Kotlin/kotlinx.coroutines/blob/master/LICENSE.txt) | Kotlin coroutines | 
| [Jackson](https://github.com/FasterXML/jackson-module-kotlin) | [Apache 2.0](https://github.com/FasterXML/jackson-module-kotlin/blob/2.15/LICENSE) | JSON parser | 
| [RxJava](https://github.com/ReactiveX/RxAndroid) | [Apache 2.0](https://github.com/ReactiveX/RxAndroid/blob/2.x/LICENSE) | RxJava bindings | 
| [Timber](https://github.com/JakeWharton/timber) | [Apache 2.0](https://github.com/JakeWharton/timber/blob/master/LICENSE.txt) | Logger | 

#### Network/API

| Name | License | Usefulness |
|-|-|-|
| [Retrofit](https://github.com/square/retrofit) | [Apache 2.0](https://github.com/square/retrofit/blob/master/LICENSE.txt) | Type-safe HTTP client |
| [OkHttp](https://github.com/square/okhttp) | [Apache 2.0](https://github.com/square/okhttp/blob/master/LICENSE.txt) | HTTP client |

#### UI

| Name | License | Usefulness |
|-|-|-|
| [Material Components](https://github.com/material-components/material-components-android) | [Apache 2.0](https://github.com/material-components/material-components-android/blob/master/LICENSE) | Material Design UI |WindowInsets library
| [Insetter](https://github.com/chrisbanes/insetter) | [Apache 2.0](https://github.com/chrisbanes/insetter/blob/main/LICENSE) | WindowInsets library | 
| [Signature Pad](https://github.com/gcacace/android-signaturepad) | [Apache 2.0](https://github.com/gcacace/android-signaturepad/blob/master/LICENSE) | Signature pad component | 
| [Glide Transformations](https://github.com/wasabeef/glide-transformations) | [Apache 2.0](https://github.com/wasabeef/glide-transformations/blob/main/LICENSE) | 	Image transformations |

#### Others

##### Testing

| Name | License | Usefulness |
|-|-|-|
| [Mockito](https://github.com/mockito/mockito) | [MIT](https://github.com/mockito/mockito/blob/release/3.x/LICENSE) | Mocking framework | 
| [Robolectric](https://github.com/robolectric/robolectric) | [MIT](https://github.com/robolectric/robolectric/blob/master/LICENSE) | Unit testing framework |

##### Dev Tools

| Name | License | Usefulness |
|-|-|-|
| [Detekt](https://github.com/arturbosch/detekt) | [Apache 2.0](https://github.com/arturbosch/detekt/blob/master/LICENSE) | Static code analysis |
| [Ktlint](https://github.com/pinterest/ktlint) | [MIT](https://github.com/pinterest/ktlint/blob/master/LICENSE) | Code Coverage |
| [JaCoCo](https://github.com/jacoco/jacoco) | [EPL 2.0](https://github.com/jacoco/jacoco/blob/master/LICENSE.md) | Linter/Formatter |
| [Gradle Versions Plugin](https://github.com/ben-manes/gradle-versions-plugin) | [Apache 2.0](https://github.com/ben-manes/gradle-versions-plugin/blob/master/LICENSE.txt) | Dependency updates |

##### Project Generator

| Name | License | Usefulness |
|-|-|-|
| [Clikt](https://github.com/ajalt/clikt) | [Apache 2.0](https://github.com/ajalt/clikt/blob/master/LICENSE.txt) | Command line tool |
| [Kscript](https://github.com/kscripting/kscript) | [MIT](https://github.com/kscripting/kscript/blob/master/LICENSE.txt) | Kotlin script library |
| [SQLite JDBC](https://github.com/xerial/sqlite-jdbc) | [Apache 2.0](https://github.com/xerial/sqlite-jdbc/blob/master/LICENSE) | Sqlite library |

