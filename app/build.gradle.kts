/*
 * Copyright (C) 2021. Maksym Oliinyk.
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

import Libraries.Versions.accompanies
import Libraries.Versions.compose
import Libraries.appcompat
import Libraries.coroutinesAndroid
import Libraries.gson
import Libraries.immutableCollections
import Libraries.kotlinStdLib
import Libraries.stitch
import TestLibraries.espressoCore
import TestLibraries.espressoRunner

plugins {
    id("com.android.application")
    id("kotlin-android")
}

android {
    compileSdkVersion(30)

    defaultConfig {
        applicationId = "com.oliinyk.max.news.reader"
        minSdkVersion(21)
        targetSdkVersion(30)
        versionCode = 1
        versionName = "1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    buildFeatures {
        compose = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    kotlinOptions {
        jvmTarget = "1.8"
        useIR = true
        kotlinOptions.languageVersion = "1.5"
    }

    composeOptions {
        kotlinCompilerExtensionVersion = compose
    }

    flavorDimensions += "remote"
    productFlavors {

        create("remote") {
            dimension = "remote"
            applicationIdSuffix = ".remote"
            versionNameSuffix = "(remote debuggable)"
        }

        create("default") {
            dimension = "remote"
        }
    }

    sourceSets {

        maybeCreate("remote")
            .java.srcDirs("remote/java", "main/java")

        maybeCreate("default")
            .java.srcDirs("default/java", "main/java")
    }
}

dependencies {
    implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar"))))

    implementation(project(":tea-core"))
    implementation(project(":tea-time-travel"))
    implementation(project(":tea-time-travel-adapter-gson"))

    implementation(kotlinStdLib)
    implementation(immutableCollections)
    implementation(coroutinesAndroid)

    implementation("androidx.compose.ui:ui:$compose")
    implementation("androidx.compose.foundation:foundation:$compose")
    implementation("androidx.compose.foundation:foundation-layout:$compose")
    implementation("androidx.compose.material:material:$compose")
    implementation("androidx.compose.material:material-icons-core:$compose")
    implementation("androidx.compose.material:material-icons-extended:$compose")
    implementation("androidx.compose.ui:ui-tooling:$compose")
    implementation("androidx.compose.runtime:runtime:$compose")
    implementation("androidx.compose.animation:animation:$compose")
    implementation("androidx.compose.compiler:compiler:$compose")
    implementation("androidx.activity:activity-compose:1.3.0-alpha02")

    implementation("dev.chrisbanes.accompanist:accompanist-insets:$accompanies")
    implementation("dev.chrisbanes.accompanist:accompanist-coil:$accompanies")

    implementation("androidx.appcompat:appcompat:$appcompat")

    implementation("org.mongodb:stitch-android-sdk:$stitch")

    implementation("com.google.code.gson:gson:$gson")
    // todo remove
    implementation("com.squareup.okhttp3:okhttp:4.8.1")
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.8.1")

    testImplementation(project(":tea-test"))
    testImplementation(coroutinesAndroid)

    androidTestImplementation("androidx.test:runner:$espressoRunner")
    androidTestImplementation("androidx.test.espresso:espresso-core:$espressoCore")

}
