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

import com.jfrog.bintray.gradle.BintrayExtension
import io.gitlab.arturbosch.detekt.Detekt
import io.gitlab.arturbosch.detekt.DetektCreateBaselineTask
import org.jetbrains.dokka.gradle.DokkaTask
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import java.net.URL
import java.text.SimpleDateFormat
import java.util.*

installGitHooks()

// Top-level build file where you can add configuration options common to all sub-projects/modules.
buildscript {

    repositories {
        mavenLocal()
        google()
        jcenter()
        maven { setUrl("https://jitpack.io") }
        maven { setUrl("https://plugins.gradle.org/m2/") }
    }

    dependencies {
        classpath(BuildPlugins.kotlinGradlePlugin)
        classpath(BuildPlugins.androidGradlePlugin)
        classpath(BuildPlugins.intellijPlugin)
        // NOTE: Do not place your application dependencies here; they belong
        // in the individual module build.gradle files
    }
}

plugins {
    kotlin()
    detekt()
    dokka() apply false
    `maven-publish`
    bintray() apply false
}

allprojects {
    apply {
        plugin("io.gitlab.arturbosch.detekt")
    }
}

nonAndroidAppProjects()
    .forEachApplying {

        apply(plugin = "org.jetbrains.kotlin.jvm")

        detekt {
            toolVersion = BuildPlugins.Versions.detektVersion
            input = files("src/main/kotlin", "src/main/java")
            config.setFrom(detektConfig)
            baseline = detektBaseline
            autoCorrect = true
        }
    }

libraryProjects()
    .forEachApplying {

        apply(plugin = "maven-publish")
        apply(plugin = "org.jetbrains.dokka")
        apply(plugin = "com.jfrog.bintray")

        val dokka by tasks.getting(DokkaTask::class) {
            outputFormat = "html"
            outputDirectory = "$buildDir/javadoc"

            configuration {

                moduleName = name

                externalDocumentationLink {
                    noJdkLink = true
                    noStdlibLink = true
                    noAndroidSdkLink = true
                    url = URL("https://kotlin.github.io/kotlinx.coroutines/kotlinx-coroutines-core/")
                    url = URL("https://javadoc.io/doc/com.google.code.gson/gson/latest/com.google.gson/")
                }

                jdkVersion = JavaVersion.VERSION_1_8.majorVersionInt
            }

        }

        val sourcesJar by tasks.creating(Jar::class) {
            dependsOn(tasks.classes)
            archiveClassifier.set("sources")
            from(sourceSets.main.get().allSource)
        }

        val javadocJar by tasks.creating(Jar::class) {
            dependsOn(dokka)
            archiveClassifier.set("javadoc")
            from("$buildDir/javadoc")
        }

        val copyArtifacts by tasks.creating(Copy::class) {
            from("$buildDir/libs/")
            into("${rootProject.buildDir}/artifacts/${this@forEachApplying.name}")
            dependsOn("publishToMavenLocal")
        }

        tasks.create("rolloutLibrary") {
            dependsOn("bintrayUpload", copyArtifacts)
        }

        publishing {

            publications {
                create<MavenPublication>(name) {
                    from(components["java"])
                    artifact(sourcesJar)
                    artifact(javadocJar)

                    groupId = "com.github.Xlopec"
                    artifactId = name
                    version = versionName()
                }
            }

            repositories {
                mavenLocal()
            }
        }

        artifacts {
            archives(sourcesJar)
            archives(javadocJar)
        }

        the<BintrayExtension>().apply {

            user = "xlopec"
            key = bintrayApiKey()
            setPublications(name)

            with(pkg) {

                setLicenses("Apache-2.0")
                repo = "tea-bag"
                name = this@forEachApplying.name
                userOrg = "xlopec"
                vcsUrl = "https://github.com/Xlopec/Tea-bag.git"

                with(version) {

                    val format = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZZ", Locale.ENGLISH)

                    name = versionName()
                    released = format.format(Date())
                    desc = "debug"
                }
            }
        }
    }

pluginProject().forEachApplying {

    apply(plugin = "org.jetbrains.intellij")

    val copyArtifacts by tasks.creating(Copy::class) {
        from("$buildDir/libs/")
        into("${rootProject.buildDir}/artifacts/$name")
        dependsOn("buildPlugin")
    }

    tasks.create("rolloutPlugin") {
        dependsOn("publishPlugin", copyArtifacts)
    }

}

val detektAll by tasks.registering(Detekt::class) {
    description = "Runs analysis task over whole code"
    debug = false
    parallel = true
    ignoreFailures = false
    disableDefaultRuleSets = false
    buildUponDefaultConfig = true
    setSource(files(projectDir))
    config.setFrom(detektConfig)
    baseline.set(detektBaseline)

    include("**/*.kt", "**/*.kts")
    exclude("resources/", "build/", "**/test/java/**")

    reports {
        xml.enabled = false
        txt.enabled = false
        html.enabled = true
    }
}

val detektProjectBaseline by tasks.registering(DetektCreateBaselineTask::class) {
    buildUponDefaultConfig.set(true)
    ignoreFailures.set(true)
    parallel.set(true)
    setSource(files(rootDir))
    config.setFrom(detektConfig)
    baseline.set(detektBaseline)
    include("**/*.kt")
    include("**/*.kts")
    exclude("**/resources/**")
    exclude("**/build/**")
}

val detektFormat by tasks.creating(Detekt::class) {
    parallel = true
    autoCorrect = true
    buildUponDefaultConfig = true
    failFast = false
    ignoreFailures = false
    setSource(files(projectDir))

    include("**/*.kt")
    include("**/*.kts")
    exclude("**/resources/**")
    exclude("**/build/**")

    config.setFrom(files("$rootDir/detekt/detekt-config.yml"))
}

tasks.create("fullRollout") {
    setDependsOn((libraryProjects().map { p -> p.rolloutTask } + pluginProject().map { p -> p.rolloutTask }).toList())
}

allprojects {
    repositories {
        mavenLocal()
        google()
        jcenter()
    }

    tasks.withType<KotlinCompile>().all {
        kotlinOptions {
            // disables warning about usage of experimental Kotlin features
            @Suppress("SuspiciousCollectionReassignment")
            freeCompilerArgs += listOf(
                "-Xuse-experimental=kotlin.ExperimentalUnsignedTypes",
                "-Xuse-experimental=kotlin.Experimental",
                "-Xuse-experimental=kotlin.contracts.ExperimentalContracts",
                "-Xuse-experimental=kotlinx.coroutines.FlowPreview",
                "-Xuse-experimental=kotlinx.coroutines.ExperimentalCoroutinesApi",
                "-Xuse-experimental=io.ktor.util.KtorExperimentalAPI",
                "-XXLanguage:+NewInference",
                "-XXLanguage:+InlineClasses"
            )
        }
    }
}

val Project.rolloutTask: Task
    get() = tasks.findByName("rolloutLibrary")
        ?: tasks.findByName("rolloutPlugin")
        ?: error("Couldn't find rollout task for project $name")

fun nonAndroidAppProjects() =
    subprojects.asSequence().filterNot { project -> project.name == "app" }

fun libraryProjects() =
    nonAndroidAppProjects().filterNot { project -> project.name == "tea-time-travel-plugin" || project.name == "tea-test" }

fun pluginProject() =
    subprojects.asSequence().filter { project -> project.name == "tea-time-travel-plugin" }
