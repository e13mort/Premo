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

package me.dmdev.premo.sample.component

import js.core.jso
import me.dmdev.premo.sample.MainPm
import me.dmdev.premo.sample.collectAsState
import me.dmdev.premo.sample.component
import mui.material.Container
import mui.material.CssBaseline
import mui.material.PaletteMode
import mui.material.Stack
import mui.material.StackDirection
import mui.material.styles.ThemeProvider
import mui.material.styles.createTheme
import mui.system.responsive
import mui.system.sx
import react.FC
import react.Props
import web.cssom.AlignItems
import web.cssom.JustifyContent
import web.cssom.px

external interface MainComponentProps : Props {
    var pm: MainPm
}

val MainComponent = FC<MainComponentProps> { props ->

    val masterPm = props.pm.navigation.master
    val detailPm = props.pm.navigation.detailFlow.collectAsState()

    ThemeProvider {
        this.theme = createTheme(
            jso {
                palette = jso { mode = PaletteMode.light }
            }
        )
        CssBaseline()

        Stack {
            direction = responsive(StackDirection.row)
            spacing = responsive(2)
            sx {
                justifyContent = JustifyContent.center
                alignItems = AlignItems.center
                height = 500.px
            }

            Container {
                this.component(masterPm)
            }

            Container {
                this.component(detailPm)
            }
        }
    }
}
