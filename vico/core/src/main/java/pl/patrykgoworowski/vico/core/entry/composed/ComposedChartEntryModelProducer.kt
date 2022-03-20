/*
 * Copyright (c) 2021. Patryk Goworowski
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package pl.patrykgoworowski.vico.core.entry.composed

import java.util.SortedMap
import java.util.TreeMap
import java.util.concurrent.Executor
import java.util.concurrent.Executors
import pl.patrykgoworowski.vico.core.DEF_THREAD_POOL_SIZE
import pl.patrykgoworowski.vico.core.chart.composed.ComposedChartEntryModel
import pl.patrykgoworowski.vico.core.entry.ChartEntry
import pl.patrykgoworowski.vico.core.entry.ChartEntryModel
import pl.patrykgoworowski.vico.core.entry.ChartModelProducer

/**
 * A subclass of [ChartModelProducer] that generates [ComposedChartEntryModel].
 *
 * @property chartModelProducers the list of [ChartModelProducer]s that are composed by this
 * [ComposedChartEntryModelProducer].
 * @param backgroundExecutor [Executor] used to generate instances of the [ComposedChartEntryModel] off the main thread.
 *
 * @see ComposedChartEntryModel
 * @see ChartModelProducer
 */
public class ComposedChartEntryModelProducer<Model : ChartEntryModel>(
    public val chartModelProducers: List<ChartModelProducer<Model>>,
    backgroundExecutor: Executor = Executors.newFixedThreadPool(DEF_THREAD_POOL_SIZE),
) : ChartModelProducer<ComposedChartEntryModel<Model>> {

    private val compositeModelReceivers: HashMap<Any, CompositeModelReceiver<Model>> = HashMap()

    private val executor: Executor = backgroundExecutor

    private var cachedModel: ComposedChartEntryModel<Model>? = null

    public constructor(
        vararg chartModelProducers: ChartModelProducer<Model>,
        backgroundExecutor: Executor = Executors.newFixedThreadPool(DEF_THREAD_POOL_SIZE),
    ) : this(chartModelProducers.toList(), backgroundExecutor)

    override fun getModel(): ComposedChartEntryModel<Model> =
        cachedModel ?: getModel(chartModelProducers.map { it.getModel() })
            .also { cachedModel = it }

    override fun progressModel(key: Any, progress: Float) {
        chartModelProducers.forEach { producer ->
            producer.progressModel(key, progress)
        }
    }

    override fun registerForUpdates(
        key: Any,
        updateListener: () -> ComposedChartEntryModel<Model>?,
        onModel: (ComposedChartEntryModel<Model>) -> Unit,
    ) {
        val receiver = CompositeModelReceiver(onModel, executor)
        compositeModelReceivers[key] = receiver
        chartModelProducers.forEachIndexed { index, producer ->
            producer.registerForUpdates(
                key = key,
                updateListener = { updateListener()?.composedEntryCollections?.get(index) },
                onModel = receiver.getModelReceiver(index),
            )
        }
    }

    private class CompositeModelReceiver<Model : ChartEntryModel>(
        private val onModel: (ComposedChartEntryModel<Model>) -> Unit,
        private val executor: Executor,
    ) {

        private val modelReceivers: SortedMap<Int, Model?> = TreeMap()

        internal fun getModelReceiver(index: Int): (Model) -> Unit {
            val modelReceiver: (Model) -> Unit = { model ->
                onModelUpdate(index, model)
            }
            modelReceivers[index] = null
            return modelReceiver
        }

        private fun onModelUpdate(index: Int, model: Model) {
            modelReceivers[index] = model
            val models = modelReceivers.values.mapNotNull { it }
            if (modelReceivers.values.size == models.size) {
                executor.execute {
                    onModel(getModel(models))
                }
            }
        }
    }

    override fun unregisterFromUpdates(key: Any) {
        compositeModelReceivers.remove(key)
        chartModelProducers.forEach { producer ->
            producer.unregisterFromUpdates(key)
        }
    }

    private companion object {
        private fun <Model : ChartEntryModel> getModel(
            models: List<Model>,
        ): ComposedChartEntryModel<Model> = object : ComposedChartEntryModel<Model> {
            override val composedEntryCollections: List<Model> = models
            override val entries: List<List<ChartEntry>> = models.map { it.entries }.flatten()
            override val minX: Float = models.minOf { it.minX }
            override val maxX: Float = models.maxOf { it.maxX }
            override val minY: Float = models.minOf { it.minY }
            override val maxY: Float = models.maxOf { it.maxY }
            override val stackedMaxY: Float = models.maxOf { it.stackedMaxY }
            override val stepX: Float = models.minOf { it.stepX }
        }
    }
}
