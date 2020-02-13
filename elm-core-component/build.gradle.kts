import Libraries.coroutinesCore
import Libraries.kotlinReflect
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
    kotlin("jvm")
}

group = "com.github.Xlopec"
version = "0.0.2-alpha1"

repositories {
    mavenLocal()
    jcenter()
}


dependencies {

    api(coroutinesCore)

    implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar"))))
    implementation(kotlinStdLib)
    implementation(kotlinReflect)

    testImplementation(project(path = ":elm-core-test", configuration = "default"))
}
