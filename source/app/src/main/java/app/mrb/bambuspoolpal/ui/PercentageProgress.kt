/**
 * Copyright (C) 2025 BambuSpoolPal
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package app.mrb.bambuspoolpal.ui

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import app.mrb.bambuspoolpal.R
import app.mrb.bambuspoolpal.shared.SharedViewModel

/**
 * Composable function to display a percentage-based progress bar with an image slider.
 *
 * @param initialPercentage The initial percentage of progress to display.
 * @param onPercentageChange Callback function to be invoked when the percentage changes.
 */
@Composable
fun PercentageProgressBar(
    sharedViewModel: SharedViewModel,
    initialPercentage: Int,
    onPercentageChange: (Int) -> Unit,
    onDragFinished: (Int) -> Unit
) {
    var currentPercentage by remember { mutableIntStateOf(initialPercentage.coerceIn(0, 100)) }
    val progress = currentPercentage / 100f
    val painter = painterResource(id = R.drawable.spool)
    val imageWidth = 40.dp

    val filamentDetail by sharedViewModel.filamentDetail.collectAsState()

    LaunchedEffect(filamentDetail?.actualWeight) {
        filamentDetail?.actualWeight?.let { weight ->
            currentPercentage = calculatePercentageFromWeight(weight, filamentDetail)
        } ?: run {
        }
    }

    val progress_as_state by animateFloatAsState(targetValue = progress, animationSpec = tween(1000))

    Column(
        verticalArrangement = Arrangement.spacedBy(4.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Layout(
            modifier = Modifier
                .padding(
                    PaddingValues(
                        vertical = imageWidth / 2,
                        horizontal = 4.dp + imageWidth / 2
                    )
                )
                .fillMaxWidth()
                .pointerInput(Unit) {
                    detectHorizontalDragGestures(
                        onHorizontalDrag = { change, _ ->
                            currentPercentage = calculateNewPercentage(change.position.x, size.width)
                            onPercentageChange(currentPercentage)
                        },
                        onDragEnd = {
                            onDragFinished(currentPercentage) // Invoke the callback when drag ends
                        }
                    )
                },
            content = {
                LinearProgressIndicator(
                    progress = { progress_as_state },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(50)),
                    color = MaterialTheme.colorScheme.primary,
                )
                Image(
                    painter = painter,
                    contentDescription = stringResource(R.string.slider_to_adjust_percentage),
                    modifier = Modifier
                        .size(imageWidth)
                        .pointerInput(Unit) {} 
                        .clickable(enabled = false) {}
                )
            }
        ) { measurables, constraints ->
            val progressIndicator = measurables[0].measure(constraints)
            val image = measurables[1].measure(constraints)

            layout(constraints.maxWidth, progressIndicator.height) {
                progressIndicator.placeRelative(0, 0)
                val imageY = (progressIndicator.height - image.height) / 2
                image.placeRelative((constraints.maxWidth * progress).toInt() - image.width / 2, imageY)
            }
        }
    }
}

/**
 * Calculates the new percentage based on the horizontal position and width.
 *
 * @param xPosition The horizontal position of the drag.
 * @param width The width of the layout.
 * @return The calculated percentage.
 */
private fun calculateNewPercentage(xPosition: Float, width: Int): Int {
    return ((xPosition / width) * 100).toInt().coerceIn(0, 100)
}
