plugins {
    // 1.8.22+ has type generation disabled: https://youtrack.jetbrains.com/issue/KT-54445
    kotlin("multiplatform") version "1.8.10"
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
val cliVersion = "0.3.5"
val okioVersion = "3.4.0"

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
    val hostOs = System.getProperty("os.name")
    val isArm64 = System.getProperty("os.arch") == "aarch64"
    val isMingwX64 = hostOs.startsWith("Windows")
    val nativeTarget = when {
        hostOs == "Mac OS X" && isArm64 -> macosArm64("native")
        hostOs == "Mac OS X" && !isArm64 -> macosX64("native")
        hostOs == "Linux" && isArm64 -> linuxArm64("native")
        hostOs == "Linux" && !isArm64 -> linuxX64("native")
        isMingwX64 -> mingwX64("native")
        else -> throw GradleException("Host OS is not supported in Kotlin/Native.")
    }
    nativeTarget.apply {
        binaries {
            executable {
                entryPoint = "ru.ov7a.github.insights.main"
            }
        }
    }

    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(kotlin("stdlib-common"))
                implementation(kotlin("reflect"))
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:$coroutinesVersion")

                implementation("io.ktor:ktor-client-core:$ktorVersion")
                implementation("io.ktor:ktor-client-content-negotiation:$ktorVersion")
                implementation("io.ktor:ktor-serialization-kotlinx-json:$ktorVersion")

                implementation("org.jetbrains.kotlinx:kotlinx-datetime:$dateTimeVersion")
            }
        }
        val commonTest by getting {
            dependencies {
                implementation(kotlin("test"))
                implementation("io.kotest:kotest-assertions-core:$kotestVersion")
                implementation("io.ktor:ktor-client-mock:$ktorVersion")
            }
        }

        val jsMain by getting {
            dependencies {
                implementation("org.jetbrains.kotlinx:kotlinx-html:$kotlinxHtmlVersion")

                implementation(npm("js-cookie", jsCookieVersion))
                implementation(npm("@types/js-cookie", jsCookieVersion, generateExternals = true))
            }
        }
        val jsTest by getting

        val nativeMain by getting {
            dependencies {
                implementation("io.ktor:ktor-client-curl:$ktorVersion")
                implementation("org.jetbrains.kotlinx:kotlinx-cli:$cliVersion")
            }
        }
        val nativeTest by getting {
            dependencies {
                implementation("com.squareup.okio:okio:$okioVersion")
            }
        }
    }
}
