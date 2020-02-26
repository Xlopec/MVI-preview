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

tasks.withType<Test> {
    useJUnitPlatform()
}

dependencies {

    implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar"))))
    implementation(project(configuration = "default", path = ":tea-core"))

    implementation(Libraries.kotlinStdLib)
    api("org.jetbrains.kotlinx:atomicfu:0.14.1")

    api(Libraries.coroutinesCore)
    api("junit:junit:4.12")
    api(Libraries.coroutinesTest)
    // todo remove dependency on strikt
    api("io.strikt:strikt-core:0.22.2")
    api("io.kotlintest:kotlintest-runner-junit5:3.4.2")

}