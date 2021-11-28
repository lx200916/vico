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

package pl.patrykgoworowski.vico.app.ui.component

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import pl.patrykgoworowski.vico.compose.axis.horizontal.bottomAxis
import pl.patrykgoworowski.vico.compose.axis.vertical.startAxis
import pl.patrykgoworowski.vico.compose.dataset.DataSet
import pl.patrykgoworowski.vico.compose.dataset.column.columnDataSet
import pl.patrykgoworowski.vico.compose.dataset.line.lineDataSet
import pl.patrykgoworowski.vico.core.dataset.composed.plus
import pl.patrykgoworowski.vico.core.dataset.entry.collection.EntryModel
import pl.patrykgoworowski.vico.core.dataset.entry.collection.composed.ComposedEntryCollection

@Composable
internal fun ComposedChart(
    modifier: Modifier = Modifier,
    model: ComposedEntryCollection<EntryModel>,
) {
    DataSet(
        modifier = modifier,
        dataSet = columnDataSet() + lineDataSet(),
        entryCollection = model,
        startAxis = startAxis(),
        bottomAxis = bottomAxis(),
        marker = markerComponent(),
    )
}