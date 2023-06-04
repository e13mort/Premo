/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2020-2023 Dmitriy Gorbunov (dmitriy.goto@gmail.com)
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package me.dmdev.premo.navigation

import me.dmdev.premo.PmLifecycle
import me.dmdev.premo.PmLifecycle.State.CREATED
import me.dmdev.premo.PmLifecycle.State.DESTROYED
import me.dmdev.premo.PmLifecycle.State.IN_FOREGROUND
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

class SetNavigatorTest {

    private lateinit var lifecycle: PmLifecycle
    private lateinit var navigator: SetNavigator
    private lateinit var pm1: TestPm
    private lateinit var pm2: TestPm
    private lateinit var pm3: TestPm

    @BeforeTest
    fun setUp() {
        lifecycle = PmLifecycle()
        lifecycle.moveTo(IN_FOREGROUND)
        pm1 = TestPm()
        pm2 = TestPm()
        pm3 = TestPm()
        navigator = SetNavigatorImpl(
            lifecycle = lifecycle,
            values = listOf(pm1, pm2, pm3),
            onChangeCurrent = { index, navigator -> navigator.changeCurrent(index) }
        )
    }

    @Test
    fun testInitialState() {
        assertEquals(navigator.values, listOf(pm1, pm2, pm3))
        assertEquals(pm1, navigator.current)
        assertEquals(pm1.lifecycle.state, IN_FOREGROUND)
        assertEquals(pm2.lifecycle.state, CREATED)
        assertEquals(pm3.lifecycle.state, CREATED)
    }

    @Test
    fun testSetCurrent() {
        navigator.changeCurrent(navigator.values.indexOf(pm2))

        assertEquals(pm2, navigator.current)
        assertEquals(pm1.lifecycle.state, CREATED)
        assertEquals(pm2.lifecycle.state, IN_FOREGROUND)
        assertEquals(pm3.lifecycle.state, CREATED)
    }

    @Test
    fun testMoveParentLifecycleToCreated() {
        lifecycle.moveTo(CREATED)

        assertEquals(pm1.lifecycle.state, CREATED)
        assertEquals(pm2.lifecycle.state, CREATED)
        assertEquals(pm3.lifecycle.state, CREATED)
    }

    @Test
    fun testMoveParentLifecycleToCreatedAndThenInForeground() {
        lifecycle.moveTo(CREATED)
        lifecycle.moveTo(IN_FOREGROUND)

        assertEquals(pm1.lifecycle.state, IN_FOREGROUND)
        assertEquals(pm2.lifecycle.state, CREATED)
        assertEquals(pm3.lifecycle.state, CREATED)
    }

    @Test
    fun testEmptySetNavigator() {
        navigator = SetNavigatorImpl(
            lifecycle = lifecycle,
            values = listOf(),
            onChangeCurrent = { index, navigator -> navigator.changeCurrent(index) }
        )

        assertEquals(listOf(), navigator.values)
        assertEquals(null, navigator.current)
    }

    @Test
    fun testSetValuesToEmptyNavigator() {
        navigator = SetNavigatorImpl(
            lifecycle = lifecycle,
            values = listOf(),
            onChangeCurrent = { index, navigator -> navigator.changeCurrent(index) }
        )

        val pm1 = TestPm()
        val pm2 = TestPm()
        val pm3 = TestPm()

        navigator.changeValues(listOf(pm1, pm2, pm3))

        assertEquals(listOf(pm1, pm2, pm3), navigator.values)
        assertEquals(navigator.current, pm1)
        assertEquals(IN_FOREGROUND, pm1.lifecycle.state)
        assertEquals(CREATED, pm2.lifecycle.state)
        assertEquals(CREATED, pm3.lifecycle.state)
    }

    @Test
    fun testReplaceValues() {
        val pm4 = TestPm()
        val pm5 = TestPm()
        val pm6 = TestPm()

        navigator.changeValues(listOf(pm4, pm5, pm6))

        assertEquals(listOf(pm4, pm5, pm6), navigator.values)
        assertEquals(navigator.current, pm4)
        assertEquals(DESTROYED, pm1.lifecycle.state)
        assertEquals(DESTROYED, pm2.lifecycle.state)
        assertEquals(DESTROYED, pm3.lifecycle.state)
        assertEquals(IN_FOREGROUND, pm4.lifecycle.state)
        assertEquals(CREATED, pm5.lifecycle.state)
        assertEquals(CREATED, pm6.lifecycle.state)
    }

    @Test
    fun testPartlyReplaceValues() {
        val pm4 = TestPm()
        val pm5 = TestPm()

        navigator.changeValues(listOf(pm2, pm4, pm5))

        assertEquals(listOf(pm2, pm4, pm5), navigator.values)
        assertEquals(navigator.current, pm2)
        assertEquals(DESTROYED, pm1.lifecycle.state)
        assertEquals(IN_FOREGROUND, pm2.lifecycle.state)
        assertEquals(DESTROYED, pm3.lifecycle.state)
        assertEquals(CREATED, pm4.lifecycle.state)
        assertEquals(CREATED, pm5.lifecycle.state)
    }
}
