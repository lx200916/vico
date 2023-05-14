/*
 * Copyright 2023 by Patryk Goworowski and Patrick Michalik.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.patrykandpatrick.vico.core.legend

import android.graphics.RectF
import com.patrykandpatrick.vico.core.chart.draw.ChartDrawContext
import com.patrykandpatrick.vico.core.component.dimension.Padding
import com.patrykandpatrick.vico.core.component.text.HorizontalPosition
import com.patrykandpatrick.vico.core.component.text.VerticalPosition
import com.patrykandpatrick.vico.core.context.MeasureContext
import com.patrykandpatrick.vico.core.dimensions.MutableDimensions
import com.patrykandpatrick.vico.core.dimensions.emptyDimensions
import com.patrykandpatrick.vico.core.extension.half
import com.patrykandpatrick.vico.core.legend.VerticalLegend.Item

/**
 * [HorizontalLegend] displays legend items in a line wrapping horizontal row.
 *
 * @param items a [Collection] of [Item]s to be displayed by this [HorizontalLegend].
 * @param iconSizeDp defines the size of all [Item.icon]s.
 * @param iconPaddingDp defines the padding between each [Item.icon] and its corresponding [Item.label].
 * @param lineSpacingDp define the vertical spacing between lines.
 * @param spacingDp defines the horizon spacing between each [Item] in line.
 * @param padding defines the padding of the content.
 */
public open class HorizontalLegend(
    public var items: Collection<Item>,
    public var iconSizeDp: Float,
    public var iconPaddingDp: Float,
    public var lineSpacingDp: Float = 0f,
    public var spacingDp: Float = 0f,
    override val padding: MutableDimensions = emptyDimensions(),
) : Legend, Padding {
    // Cache the height of each line
    private val heights = mutableListOf<Float>()

    // Cache the items in each line
    private val lines = mutableListOf<MutableList<Item>>(mutableListOf())
    override val bounds: RectF = RectF()
    override fun getHeight(context: MeasureContext, availableWidth: Float): Float = with(context) {
        if (items.isEmpty()) return@with 0f
        lines.clear()
        lines.add(mutableListOf())
        var height = (padding.verticalDp + lineSpacingDp).pixels + maxOf(
            items.first().getHeight(context, availableWidth),
            iconSizeDp.pixels,
        )
        var remainWidth = availableWidth
        var currentLine = 0
        heights.add(height)
        items.forEach {
            //Check if the rest of the row could fit the item or if the row is empty (if one item is super wide it will be alone in the row..with ellipsis.)
            if (remainWidth > it.getOriginalWidth(context, availableWidth) || remainWidth == availableWidth) {
                remainWidth -= it.getOriginalWidth(context, availableWidth)
                if (remainWidth > spacingDp.pixels || remainWidth == availableWidth) {
                    remainWidth -= spacingDp.pixels
                    lines[currentLine].add(it)
                    return@forEach
                }
            }
            //Line break
            currentLine++
            remainWidth = availableWidth
            lines.add(mutableListOf(it))
            val currentHeight = maxOf(it.getHeight(context, availableWidth), iconSizeDp.pixels)
            heights.add(currentHeight)
            height += currentHeight + lineSpacingDp.pixels
        }
        height
    }

    override fun draw(context: ChartDrawContext): Unit = with(context) {
        var currentTop = bounds.top + padding.topDp.pixels
        // isLtr? startX means the line starts at X from left : it starts at X from right
        val startX = if (isLtr) {
            chartBounds.left + padding.startDp.pixels
        } else {
            chartBounds.right - padding.startDp.pixels - iconSizeDp.pixels
        }
        val availableWidth = chartBounds.width()
        if (lines.isEmpty()) {
            var remainWidth = availableWidth
            var currentLine = 0
            items.forEach {
                if (remainWidth > it.getOriginalWidth(context, availableWidth)) {
                    remainWidth -= it.getOriginalWidth(context, availableWidth)
                    if (remainWidth > spacingDp.pixels) {
                        remainWidth -= spacingDp.pixels
                        lines[currentLine].add(it)
                        return@forEach
                    }
                }
                currentLine++
                remainWidth = availableWidth
                lines.add(mutableListOf(it))
            }
        }

        lines.forEachIndexed { index, item ->
            var currentStart = 0f
            val currentLineHeight = heights.getOrElse(index) { item.first().getHeight(context, availableWidth) }
            val centerY = currentTop + currentLineHeight.half

            item.forEach { it ->
                it.icon.draw(
                    context = context,
                    left = startX + currentStart,
                    top = centerY - iconSizeDp.half.pixels,
                    right = startX + iconSizeDp.pixels + currentStart,
                    bottom = centerY + iconSizeDp.half.pixels,
                )
                currentStart += if (isLtr) {
                    (iconSizeDp + iconPaddingDp).pixels
                } else {
                    -iconPaddingDp.pixels
                }
                it.label.drawText(
                    context = context,
                    text = it.labelText,
                    textX = startX + currentStart,
                    textY = centerY,
                    horizontalPosition = HorizontalPosition.End,
                    verticalPosition = VerticalPosition.Center,
                    maxTextWidth = (chartBounds.width() - (iconSizeDp + iconPaddingDp + padding.horizontalDp).pixels)
                        .toInt(),
                )
                currentStart += if (isLtr) {
                    it.getOriginalLabelWidth(context, availableWidth) + spacingDp.pixels
                } else {
                    -(it.getOriginalLabelWidth(context, availableWidth) + spacingDp.pixels + iconSizeDp.pixels)
                }
            }
            currentTop += currentLineHeight + lineSpacingDp.pixels
        }

    }

    protected open fun Item.getHeight(
        context: MeasureContext,
        availableWidth: Float,
    ): Float = with(context) {
        label.getHeight(
            context = context,
            text = labelText,
            width = (availableWidth - iconSizeDp.pixels - iconPaddingDp.pixels).toInt(),
        )
    }

    protected open fun Item.getOriginalLabelWidth(
        context: MeasureContext,
        availableWidth: Float,
    ): Float = with(context) {
        label.getWidth(
            context = context,
            text = labelText,
            width = (availableWidth - iconSizeDp.pixels - iconPaddingDp.pixels).toInt(),
        )
    }

    protected open fun Item.getOriginalWidth(
        context: MeasureContext,
        availableWidth: Float,
    ): Float = with(context) {
        this@getOriginalWidth.getOriginalLabelWidth(context, availableWidth) + (iconSizeDp + iconPaddingDp).pixels
    }

}
