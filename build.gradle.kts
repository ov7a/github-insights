plugins {
    kotlin("js") version "1.5.30"
    kotlin("plugin.serialization") version "1.5.30"
}

repositories {
    mavenCentral()
}

val ktorVersion = "1.6.3"
val coroutinesVersion = "1.5.2"
val kotlinxHtmlVersion = "0.7.3"
val kotestVersion = "4.6.3"

dependencies {
    implementation(kotlin("stdlib-js"))
    implementation(kotlin("reflect"))
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:$coroutinesVersion")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core-js:$coroutinesVersion")

    implementation("org.jetbrains.kotlinx:kotlinx-html:$kotlinxHtmlVersion")
    implementation("org.jetbrains.kotlinx:kotlinx-html-js:$kotlinxHtmlVersion")

    implementation("io.ktor:ktor-client-core:$ktorVersion")
    implementation("io.ktor:ktor-client-js:$ktorVersion")
    implementation("io.ktor:ktor-client-serialization:$ktorVersion")

    implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.2.1")

    implementation(npm("js-cookie", "2.2.1"))
    implementation(npm("@types/js-cookie", "2.2.7", generateExternals = true))

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
