import Libraries.coroutinesMt
import Libraries.kotlinStdLib

/*
 * Copyright (C) 2019 Maksym Oliinyk.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

plugins {
    kotlin("multiplatform")
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
    kotlinOptions {
        // disables warning about usage of experimental Kotlin features
        @Suppress("SuspiciousCollectionReassignment")
        freeCompilerArgs += listOf(
            "-XXLanguage:+NewInference",
            "-XXLanguage:+InlineClasses",
            "-Xopt-in=kotlin.RequiresOptIn",
            "-Xopt-in=kotlinx.coroutines.ExperimentalCoroutinesApi"
        )
    }
}

kotlin {

    jvm {
        withJava()
    }

    sourceSets {
        val commonMain by getting {
            dependencies {
                api(coroutinesMt)
                implementation(kotlinStdLib)
            }
        }

        val commonTest by getting {
            dependencies {

            }
        }

        val jvmMain by getting {

        }

        val jvmTest by getting {
            dependencies {
                implementation(project(":tea-test"))
            }
        }
    }
}