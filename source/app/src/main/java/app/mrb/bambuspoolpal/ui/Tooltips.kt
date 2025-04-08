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

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import kotlinx.coroutines.delay

@Composable
fun TooltipButton(
    modifier: Modifier = Modifier,
    text: String,
    tooltipMessage: String = "",
    onClick: () -> Unit,
    running: Boolean = false,
    showTooltip: MutableState<Boolean> = remember { mutableStateOf(false) },
    icon: ImageVector,
    invert: Boolean = false,
    // New state to track the toggle state
    isToggled: Boolean = false
) {

    // Animatable value for button scale to create a touch feedback animation
    val scale = remember { Animatable(1f) }

    OutlinedButton(
        onClick = onClick,
        enabled = !running, // Disable the button when running is true
        modifier = modifier
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = {
                        // Animate the button scale down slightly on press
                        scale.animateTo(0.9f, animationSpec = tween(100))
                        delay(100)
                        // Animate the button scale back to normal
                        scale.animateTo(1f, animationSpec = tween(100))
                    }
                )
            }
            .height(IntrinsicSize.Min), // Set the height of the button to the minimum intrinsic height of its content
        shape = RoundedCornerShape(12.dp) // Apply rounded corners to the button
    ) {
        if (running) {
            // Show a circular progress indicator when the button is in the running state
            CircularProgressIndicator(
                modifier = Modifier.size(24.dp),
                color = MaterialTheme.colorScheme.onPrimary // Use the onPrimary color from the theme
            )
        } else {
            // Display the button content based on the invert flag
            if (invert) {
                // If invert is true, show text first, then icon
                Text(text)
                Spacer(modifier = Modifier.width(4.dp))
                Icon(
                    icon,
                    contentDescription = text, // Use the text as content description for accessibility
                    modifier = Modifier.size(24.dp) // Fixed size for the icon to avoid layout shifts
                )

            } else {
                // If invert is false, show icon first, then text
                Icon(
                    icon,
                    contentDescription = text, // Use the text as content description for accessibility
                    modifier = Modifier.size(24.dp) // Fixed size for the icon to avoid layout shifts
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(if (isToggled) "$text (auto)" else text)
            }
        }
    }

    // Show the tooltip popup if showTooltip state is true
    if (showTooltip.value) {
        Popup(
            onDismissRequest = { showTooltip.value = false } // Dismiss the tooltip when clicked outside
        ) {
            Surface(
                modifier = Modifier.padding(8.dp),
                shape = MaterialTheme.shapes.medium, // Use the medium shape from the theme
                tonalElevation = 8.dp // Apply a tonal elevation for a subtle shadow
            ) {
                Text(
                    text = tooltipMessage,
                    modifier = Modifier.padding(16.dp) // Add padding inside the tooltip
                )
            }
        }
    }
}
