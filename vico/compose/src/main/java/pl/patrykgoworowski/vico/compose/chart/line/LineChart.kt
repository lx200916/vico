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

package pl.patrykgoworowski.vico.compose.chart.line

import android.graphics.Paint
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.Dp
import pl.patrykgoworowski.vico.compose.style.currentChartStyle
import pl.patrykgoworowski.vico.core.Dimens
import pl.patrykgoworowski.vico.core.component.Component
import pl.patrykgoworowski.vico.core.component.shape.shader.DynamicShader
import pl.patrykgoworowski.vico.core.chart.line.LineChart

@Composable
public fun lineChart(
    point: Component? = null,
    pointSize: Dp = currentChartStyle.lineChart.pointSize,
    spacing: Dp = currentChartStyle.lineChart.spacing,
    lineWidth: Dp = currentChartStyle.lineChart.lineWidth,
    lineColor: Color = currentChartStyle.lineChart.lineColor,
    lineBackgroundShader: DynamicShader? = currentChartStyle.lineChart.lineBackgroundShader,
    lineStrokeCap: StrokeCap = StrokeCap.Round,
    cubicStrength: Float = Dimens.CUBIC_STRENGTH,
    minX: Float? = null,
    maxX: Float? = null,
    minY: Float? = null,
    maxY: Float? = null,
): LineChart = remember { LineChart() }.apply {
    this.point = point
    this.pointSizeDp = pointSize.value
    this.spacingDp = spacing.value
    this.lineWidth = lineWidth.value
    this.lineColor = lineColor.toArgb()
    this.lineBackgroundShader = lineBackgroundShader
    this.lineStrokeCap = lineStrokeCap.paintCap
    this.cubicStrength = cubicStrength
    this.minX = minX
    this.maxX = maxX
    this.minY = minY
    this.maxY = maxY
}

private val StrokeCap.paintCap: Paint.Cap
    get() = when (this) {
        StrokeCap.Butt -> Paint.Cap.BUTT
        StrokeCap.Round -> Paint.Cap.ROUND
        StrokeCap.Square -> Paint.Cap.SQUARE
        else -> throw IllegalArgumentException("Not `StrokeCap.Butt`, `StrokeCap.Round`, or `StrokeCap.Square`.")
    }