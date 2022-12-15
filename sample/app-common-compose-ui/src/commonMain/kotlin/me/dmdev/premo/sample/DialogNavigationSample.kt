/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2020-2022 Dmitriy Gorbunov (dmitriy.goto@gmail.com)
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

import androidx.compose.foundation.layout.*
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import me.dmdev.premo.navigation.back
import me.dmdev.premo.sample.dilaog_navigation.DialogNavigationPm

@Composable
fun DialogNavigationScreen(
    pm: DialogNavigationPm,
    windowSizes: WindowSizes
) {

    PmBox(
        title = "Dialog Navigation",
        backHandler = { pm.back() },
        windowSizes = windowSizes
    ) {

        val message = pm.messagesFlow.bind("")

        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.weight(0.5f))
            Text(message)
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = { pm.showSimpleDialogClick() }) {
                Text("Show simple dialog")
            }
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = { pm.showSimpleDialogForResultClick() }) {
                Text("Show dialog for result")
            }
            Spacer(Modifier.weight(0.5f))
        }

        pm.dialogNavigation.dialog.bind()?.let { dialogPm ->
            DialogScreen(dialogPm, pm.dialogNavigation::onDismissRequest)
        }
    }
}
