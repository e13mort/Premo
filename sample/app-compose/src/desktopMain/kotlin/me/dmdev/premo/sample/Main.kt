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

package me.dmdev.premo.sample

import androidx.compose.desktop.ui.tooling.preview.*
import androidx.compose.runtime.*
import androidx.compose.ui.window.*
import me.dmdev.premo.JvmPmDelegate
import me.dmdev.premo.sample.serialization.Serializers
import me.dmdev.premo.saver.JsonFileStateSaver

@Preview
fun main() {
    val pmDelegate = JvmPmDelegate<MainPm>(
        pmDescription = MainPm.Description,
        pmFactory = MainPmFactory(),
        pmStateSaver = JsonFileStateSaver(Serializers.json)
    )

    application {
        val windowState = rememberWindowState()
        val windowSizes = windowSizes(windowState.size.width, windowState.size.height)
        pmDelegate.attachWindowLifecycle(windowState)

        fun ApplicationScope.onCloseWindow() {
            pmDelegate.onSaveState()
            exitApplication()
        }

        Window(
            onCloseRequest = ::onCloseWindow,
            title = "Premo",
            state = windowState
        ) {
            App(pmDelegate.presentationModel, windowSizes)
        }
    }
}

@Composable
fun JvmPmDelegate<*>.attachWindowLifecycle(windowState: WindowState) {
    LaunchedEffect(this, windowState) {
        snapshotFlow(windowState::isMinimized).collect { isMinimized ->
            if (isMinimized) {
                onBackground()
            } else {
                onForeground()
            }
        }
    }

    DisposableEffect(this) {
        onCreate()
        onDispose(::onDestroy)
    }
}
