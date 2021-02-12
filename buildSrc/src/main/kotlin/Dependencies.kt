import BuildPlugins.Versions.buildToolsVersion
import BuildPlugins.Versions.intellijVersion
import Libraries.Versions.coroutines
import Libraries.Versions.ktor

const val kotlinVersion = "1.4.30"

object BuildPlugins {

    object Versions {
        const val buildToolsVersion = "7.0.0-alpha06"
        const val detektVersion = "1.15.0"
        const val dokkaVersion = "1.4.20"
        const val bintrayVersion = "1.8.4"
        const val intellijVersion = "0.6.5"
    }

    const val androidGradlePlugin = "com.android.tools.build:gradle:$buildToolsVersion"
    const val kotlinGradlePlugin = "org.jetbrains.kotlin:kotlin-gradle-plugin:$kotlinVersion"
    const val intellijPlugin = "org.jetbrains.intellij.plugins:gradle-intellij-plugin:$intellijVersion"
}

object Libraries {
    object Versions {
        const val coroutines = "1.4.2"
        const val ktor = "1.5.1"
        const val compose = "1.0.0-alpha11"
        const val accompanies = "0.5.0"
    }

    const val coroutinesCore = "org.jetbrains.kotlinx:kotlinx-coroutines-core:$coroutines"
    const val coroutinesAndroid = "org.jetbrains.kotlinx:kotlinx-coroutines-android:$coroutines"
    const val coroutinesSwing = "org.jetbrains.kotlinx:kotlinx-coroutines-swing:$coroutines"
    const val coroutinesTest = "org.jetbrains.kotlinx:kotlinx-coroutines-test:$coroutines"
    const val kotlinStdLib = "org.jetbrains.kotlin:kotlin-stdlib-jdk8:$kotlinVersion"

    const val ktorServerCore = "io.ktor:ktor-server-core:$ktor"
    const val ktorServerNetty = "io.ktor:ktor-server-netty:$ktor"
    const val ktorServerWebsockets = "io.ktor:ktor-websockets:$ktor"

    const val ktorClientWebsockets = "io.ktor:ktor-client-websockets:$ktor"
    const val ktorClientOkHttp = "io.ktor:ktor-client-okhttp:$ktor"

    const val immutableCollections = "org.jetbrains.kotlinx:kotlinx-collections-immutable:0.3.3"
}

object TestLibraries {
    private object Versions {
        const val ktor = "1.5.1"
    }

    const val ktorMockJvm = "io.ktor:ktor-client-mock-jvm:${Versions.ktor}"
    const val ktorServerTests = "io.ktor:ktor-server-tests:${Versions.ktor}"

}

