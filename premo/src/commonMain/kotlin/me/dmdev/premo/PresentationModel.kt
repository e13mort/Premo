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

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import me.dmdev.premo.PmLifecycle.State.CREATED
import me.dmdev.premo.PmLifecycle.State.DESTROYED
import me.dmdev.premo.PmLifecycle.State.IN_FOREGROUND
import kotlin.reflect.KType
import kotlin.reflect.typeOf

abstract class PresentationModel(params: PmParams) {

    private val pmFactory: PmFactory = params.factory
    private val pmStateSaverFactory = params.stateSaverFactory

    val description: PmDescription = params.description
    val parent: PresentationModel? = params.parent
    val tag: String = if (parent != null) {
        "${parent.tag}/${description.key}"
    } else {
        description.key
    }
    val lifecycle: PmLifecycle = PmLifecycle()
    val scope: CoroutineScope = MainScope()
    var inForegroundScope: CoroutineScope? = null
        private set

    val messageHandler: PmMessageHandler = PmMessageHandler(parent?.messageHandler)
    val stateHandler: PmStateHandler = PmStateHandler(pmStateSaverFactory.createPmStateSaver(tag))

    private val allChildren = mutableListOf<PresentationModel>()
    private val attachedChildren = mutableListOf<PresentationModel>()

    init {
        subscribeToLifecycle()
    }

    fun attachChild(pm: PresentationModel) {
        pm.lifecycle.moveTo(lifecycle.state)
        attachedChildren.add(pm)
    }

    fun detachChild(pm: PresentationModel) {
        pm.lifecycle.moveTo(DESTROYED)
        attachedChildren.remove(pm)
    }

    @Suppress("FunctionName", "UNCHECKED_CAST")
    fun <PM : PresentationModel> Child(description: PmDescription): PM {
        val config = PmParams(
            parent = this,
            description = description,
            factory = pmFactory,
            stateSaverFactory = pmStateSaverFactory
        )

        val childPm = pmFactory.createPm(config) as PM
        allChildren.add(childPm)
        return childPm
    }

    fun saveState() {
        stateHandler.saveState()
        allChildren.forEach { it.saveState() }
    }

    private fun removeChild(childPm: PresentationModel) {
        allChildren.remove(childPm)
    }

    private fun subscribeToLifecycle() {
        lifecycle.addObserver { state ->
            attachedChildren.forEach { pm ->
                pm.lifecycle.moveTo(lifecycle.state)
            }

            when (state) {
                IN_FOREGROUND -> {
                    inForegroundScope = MainScope()
                }
                CREATED -> {
                    inForegroundScope?.cancel()
                    inForegroundScope = null
                }
                DESTROYED -> {
                    scope.cancel()
                    parent?.removeChild(this)
                    pmStateSaverFactory.deletePmStateSaver(tag)
                }
            }
        }
    }
}

fun PresentationModel.attachToParent() {
    parent?.attachChild(this)
}

fun PresentationModel.detachFromParent() {
    parent?.detachChild(this)
}

@Suppress("FunctionName")
fun <PM : PresentationModel> PresentationModel.AttachedChild(description: PmDescription): PM {
    return Child<PM>(description).also {
        attachChild(it)
    }
}

@Suppress("FunctionName")
inline fun <reified T> PresentationModel.SaveableFlow(
    key: String,
    initialValue: T
): MutableStateFlow<T> {
    return stateHandler.SaveableFlow(
        key = key,
        initialValue = initialValue
    )
}

@Suppress("FunctionName")
inline fun <T, reified S> PresentationModel.SaveableFlow(
    key: String,
    noinline initialValueProvider: () -> T,
    noinline saveTypeMapper: (T) -> S,
    noinline restoreTypeMapper: (S) -> T
): MutableStateFlow<T> {
    return stateHandler.SaveableFlow(
        key = key,
        initialValueProvider = initialValueProvider,
        saveType = typeOf<S>(),
        saveTypeMapper = saveTypeMapper,
        restoreTypeMapper = restoreTypeMapper
    )
}

@Suppress("FunctionName")
fun <T, S> PresentationModel.SaveableFlow(
    key: String,
    initialValueProvider: () -> T,
    saveType: KType,
    saveTypeMapper: (T) -> S,
    restoreTypeMapper: (S) -> T
): MutableStateFlow<T> {
    return stateHandler.SaveableFlow(
        key = key,
        initialValueProvider = initialValueProvider,
        saveType = saveType,
        saveTypeMapper = saveTypeMapper,
        restoreTypeMapper = restoreTypeMapper
    )
}
