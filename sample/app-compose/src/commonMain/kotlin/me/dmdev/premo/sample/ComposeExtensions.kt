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

import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.*
import androidx.compose.ui.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import me.dmdev.premo.ExperimentalPremoApi
import me.dmdev.premo.PresentationModel
import me.dmdev.premo.navigation.BackStackChange
import me.dmdev.premo.navigation.StackNavigation

@Composable
fun <T> StateFlow<T>.bind(): T {
    return collectAsState().value
}

@Composable
fun <T> Flow<T>.bind(): T? {
    return collectAsState(null).value
}

@Composable
fun <T> Flow<T>.bind(initialValue: T): T {
    return collectAsState(initialValue).value
}

@OptIn(ExperimentalPremoApi::class)
@Composable
fun NavigationBox(
    navigation: StackNavigation,
    modifier: Modifier = Modifier,
    content: @Composable (PresentationModel?) -> Unit
) {
    NavigationBox(
        backstackChange = navigation.backStackChangesFlow.bind(BackStackChange.Nothing),
        modifier = modifier,
        content = content
    )
}

@OptIn(ExperimentalPremoApi::class)
@Composable
fun StackNavigation.bindNavigation(): BackStackChange {
    return backStackChangesFlow.collectAsState(
        currentTop?.let { BackStackChange.Set(it, listOf()) } ?: BackStackChange.Nothing
    ).value
}

@OptIn(ExperimentalPremoApi::class)
@Composable
fun NavigationBox(
    backstackChange: BackStackChange,
    modifier: Modifier = Modifier,
    content: @Composable (PresentationModel?) -> Unit
) {
    val stateHolder = rememberSaveableStateHolder()

    fun removePms(pms: List<PresentationModel>) {
        pms.forEach {
            stateHolder.removeState(it.tag)
        }
    }

    val pm = when (backstackChange) {
        is BackStackChange.Push -> {
            removePms(backstackChange.removedPms)
            backstackChange.enterPm
        }
        is BackStackChange.Pop -> {
            removePms(backstackChange.removedPms)
            backstackChange.enterPm
        }
        is BackStackChange.Set -> {
            removePms(backstackChange.removedPms)
            backstackChange.pm
        }
        is BackStackChange.Nothing -> null
    }

    Box(modifier) {
        stateHolder.SaveableStateProvider(pm?.tag ?: "") {
            content(pm)
        }
    }
}

@OptIn(ExperimentalAnimationApi::class, ExperimentalPremoApi::class)
@Composable
fun AnimatedNavigationBox(
    backStackChange: BackStackChange,
    modifier: Modifier = Modifier,
    enterTransition: ((initialPm: PresentationModel, targetPm: PresentationModel) -> EnterTransition) =
        { _, _ -> slideInHorizontally { height -> height } },
    exitTransition: ((initialPm: PresentationModel, targetPm: PresentationModel) -> ExitTransition) =
        { _, _ -> slideOutHorizontally { height -> -height } },
    popEnterTransition: ((initialPm: PresentationModel, targetPm: PresentationModel) -> EnterTransition) =
        { _, _ -> slideInHorizontally { height -> -height } },
    popExitTransition: ((initialPm: PresentationModel, targetPm: PresentationModel) -> ExitTransition) =
        { _, _ -> slideOutHorizontally { height -> height } },
    setTransition: ((pm: PresentationModel) -> EnterTransition) = { EnterTransition.None },
    defaultTransition: (() -> EnterTransition) = { EnterTransition.None },
    content: @Composable (PresentationModel?) -> Unit
) {
    AnimatedContent(
        targetState = backStackChange,
        contentAlignment = Alignment.Center,
        transitionSpec = {
            when (backStackChange) {
                is BackStackChange.Push -> {
                    enterTransition(backStackChange.exitPm, backStackChange.enterPm) with
                        exitTransition(backStackChange.exitPm, backStackChange.enterPm)
                }
                is BackStackChange.Pop -> {
                    popEnterTransition(backStackChange.exitPm, backStackChange.enterPm) with
                        popExitTransition(backStackChange.exitPm, backStackChange.enterPm)
                }
                is BackStackChange.Set -> {
                    setTransition(backStackChange.pm) with ExitTransition.None
                }
                else -> {
                    defaultTransition() with ExitTransition.None
                }
            }
        }
    ) { backstackChange: BackStackChange ->
        NavigationBox(
            backstackChange = backstackChange,
            modifier = modifier,
            content = content
        )
    }
}
