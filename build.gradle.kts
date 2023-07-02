plugins {
    // 1.8.22+ has type generation disabled: https://youtrack.jetbrains.com/issue/KT-54445
    kotlin("js") version "1.8.10"
    kotlin("plugin.serialization") version "1.8.10"
}

repositories {
    mavenCentral()
}

val ktorVersion = "2.3.2"
val coroutinesVersion = "1.7.2"
val kotlinxHtmlVersion = "0.9.0"
val dateTimeVersion = "0.4.0"
val jsCookieVersion = "2.2.1" // 3.0+ has problems with generated definitions
val kotestVersion = "5.6.2"

dependencies {
    implementation(kotlin("stdlib-js"))
    implementation(kotlin("reflect"))
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:$coroutinesVersion")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core-js:$coroutinesVersion")

    implementation("org.jetbrains.kotlinx:kotlinx-html:$kotlinxHtmlVersion")
    implementation("org.jetbrains.kotlinx:kotlinx-html-js:$kotlinxHtmlVersion")

    implementation("io.ktor:ktor-client-core:$ktorVersion")
    implementation("io.ktor:ktor-serialization-kotlinx-json:$ktorVersion")
    implementation("io.ktor:ktor-client-content-negotiation:$ktorVersion")

    implementation("org.jetbrains.kotlinx:kotlinx-datetime:$dateTimeVersion")

    implementation(npm("js-cookie", jsCookieVersion))
    implementation(npm("@types/js-cookie", jsCookieVersion, generateExternals = true))

    testImplementation(kotlin("test-js"))
    testImplementation("io.kotest:kotest-assertions-core:$kotestVersion")
    testImplementation("io.kotest:kotest-assertions-core-js:$kotestVersion")
    testImplementation("io.ktor:ktor-client-mock:$ktorVersion")
}

kotlin {
    js(IR) {
        browser {
            binaries.executable()

            testTask {
                useKarma {
                    useChromiumHeadless()
                }
            }
        }
        useCommonJs()
    }
}
