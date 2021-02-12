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

package com.oliynick.max.tea.core.component

import core.component.*
import io.kotlintest.matchers.collections.shouldBeEmpty
import io.kotlintest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotlintest.shouldBe
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@RunWith(JUnit4::class)
class ComponentExtensionsTest {

    private val initialState = TodoState(Item("some1"), Item("some2"), Item("some3"))

    @Test
    fun `test single command extension`() {
        val first = DoAddItem(Item("some4"), initialState.items)

        val (state, commands) = initialState.command(DoAddItem(Item("some4"), initialState.items))

        initialState shouldBe state
        commands.shouldContainExactlyInAnyOrder(first)
    }

    @Test
    fun `test two commands extension`() {
        val first = DoAddItem(Item("some4"), initialState.items)
        val second = DoAddItem(Item("some5"), initialState.items)

        val (state, commands) = initialState.command(first, second)

        initialState shouldBe state
        commands.shouldContainExactlyInAnyOrder(first, second)
    }

    @Test
    fun `test three commands extension`() {
        val first = DoAddItem(Item("some4"), initialState.items)
        val second = DoAddItem(Item("some5"), initialState.items)
        val third = DoAddItem(Item("some6"), initialState.items)

        val (state, commands) = initialState.command(first, second, third)

        initialState shouldBe state
        commands.shouldContainExactlyInAnyOrder(first, second, third)
    }

    @Test
    fun `test multiple commands extension`() {
        val first = DoAddItem(Item("some4"), initialState.items)
        val second = DoAddItem(Item("some5"), initialState.items)
        val third = DoAddItem(Item("some6"), initialState.items)
        val fourth = DoAddItem(Item("some7"), initialState.items)

        val (state, commands) = initialState.command(first, second, third, fourth)

        initialState shouldBe state
        commands.shouldContainExactlyInAnyOrder(first, second, third, fourth)
    }

    @Test
    fun `test no commands extension`() {
        val (state, commands) = initialState.noCommand()

        initialState shouldBe state
        commands.shouldBeEmpty()
    }

    @Test
    fun `test side effect`() = runBlockingTest {

        val messages = DoAddItem(Item("some"), emptyList()).sideEffect<Command, Updated> { }

        messages.shouldBeEmpty()
    }

    @Test
    fun `test when effect returns no command the result is empty set`() = runBlockingTest {
        val messages = DoAddItem(Item("some"), emptyList()).effect<Command, Updated> { null }

        messages.shouldBeEmpty()
    }

    @Test
    fun `test when effect returns command the result is set containing the messages`() =
        runBlockingTest {
            val item = Item("some")
            val expectedMessage = Updated(listOf(item))
            val messages = DoAddItem(item, emptyList()).effect { expectedMessage }

            messages.shouldContainExactlyInAnyOrder(expectedMessage)
        }

}
