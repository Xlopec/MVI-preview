@file:Suppress("TestFunctionName")

package com.oliynick.max.tea.core.component

import com.oliynick.max.tea.core.Env
import core.component.BasicComponentTest
import kotlinx.coroutines.CoroutineScope
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@RunWith(JUnit4::class)
class ComponentTest : BasicComponentTest(::ComponentFactory) {

    private companion object {

        fun ComponentFactory(
            @Suppress("UNUSED_PARAMETER") scope: CoroutineScope,
            env: Env<Char, String, Char>,
        ): Component<Char, String, Char> = Component(env)
    }

}
