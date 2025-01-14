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

package me.dmdev.premo

import me.dmdev.premo.PmLifecycle.State.CREATED
import me.dmdev.premo.PmLifecycle.State.DESTROYED
import me.dmdev.premo.PmLifecycle.State.IN_FOREGROUND

class PmLifecycle {

    private val observers: MutableList<Observer> = mutableListOf()

    var state: State = CREATED
        private set

    fun addObserver(observer: Observer) {
        observers.add(observer)
        observer.onLifecycleChange(state)
    }

    fun removeObserver(observer: Observer) {
        observers.remove(observer)
    }

    fun moveTo(targetState: State) {
        if (targetState == state) return

        when (targetState) {
            CREATED -> {
                when (state) {
                    IN_FOREGROUND -> {
                        notifyChange(CREATED)
                    }
                    else -> { /*do nothing */
                    }
                }
            }
            IN_FOREGROUND -> {
                when (state) {
                    CREATED -> {
                        notifyChange(IN_FOREGROUND)
                    }
                    else -> { /*do nothing */
                    }
                }
            }
            DESTROYED -> {
                when (state) {
                    CREATED -> {
                        notifyChange(DESTROYED)
                    }
                    IN_FOREGROUND -> {
                        notifyChange(CREATED)
                        notifyChange(DESTROYED)
                    }
                    DESTROYED -> { /*do nothing */
                    }
                }
            }
        }
    }

    enum class State {
        CREATED,
        IN_FOREGROUND,
        DESTROYED
    }

    fun interface Observer {
        fun onLifecycleChange(state: State)
    }

    private fun notifyChange(state: State) {
        this.state = state

        observers.forEach { observer ->
            observer.onLifecycleChange(state)
        }

        if (state == DESTROYED) {
            observers.clear()
        }
    }
}
