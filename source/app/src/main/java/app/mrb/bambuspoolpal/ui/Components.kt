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

import android.graphics.Rect
import android.util.Log
import android.view.ViewTreeObserver
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.mrb.bambuspoolpal.R
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.Locale

/**
 * A composable that arranges its content horizontally with spaced items.
 *
 * @param content The composable content to be arranged horizontally.
 */
@Composable
fun MainRow(content: @Composable RowScope.() -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(6.dp), // Adds spacing between items
        verticalAlignment = Alignment.CenterVertically, // Vertically center items
        content = content // Renders the content passed to this composable
    )
}

/**
 * Composable function to display a section with a title and content.
 *
 * @param title The title of the section.
 * @param content The composable content inside the section.
 */
@Composable
fun Section(title: String, content: @Composable ColumnScope.() -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(4.dp)
    ) {
        Text(title, style = MaterialTheme.typography.titleMedium)
        content()
    }
}

/**
 * Composable function to display a styled box with a border and padding.
 *
 * @param content The composable content inside the box.
 */
@Composable
fun StyledBox(content: @Composable ColumnScope.() -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(0.dp),
        shape = RoundedCornerShape(8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        ) {
            content()
        }
    }
}

/**
 * Composable function to display a title bar at the top of the screen.
 *
 * @param title The title to display in the title bar.
 */
@Composable
fun TitleBar(title: String) {
    Surface(
        color = MaterialTheme.colorScheme.primary,
        contentColor = MaterialTheme.colorScheme.onPrimary
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
                style = TextStyle(fontSize = 18.sp, color = Color.White)
            )
        }
    }
}

/**
 * Composable function to display a custom top app bar with settings and help buttons.
 *
 * @param title The title to display in the app bar.
 * @param onSettingsClick Callback function to be invoked when the settings button is clicked.
 * @param onHelpClick Callback function to be invoked when the help button is clicked.
 */
@Composable
fun CustomTopAppBar(
    title: String,
    onSettingsClick: () -> Unit,
    onHelpClick: () -> Unit
) {
    Surface(
        color = MaterialTheme.colorScheme.primary,
        contentColor = MaterialTheme.colorScheme.onPrimary
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
                style = TextStyle(fontSize = 20.sp, color = Color.White)
            )

            IconButton(onClick = onHelpClick) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.HelpOutline,
                    contentDescription = "Help",
                    tint = Color.White
                )
            }
            IconButton(onClick = onSettingsClick) {
                Icon(
                    imageVector = Icons.Filled.Settings,
                    contentDescription = stringResource(R.string.settings),
                    tint = Color.White
                )
            }
        }
    }
}

/**
 * Composable function to detect keyboard visibility changes.
 *
 * @param onKeyboardVisibilityChanged Callback function to be invoked when keyboard visibility changes.
 */
@Composable
fun KeyboardVisibilityDetector(
    onKeyboardVisibilityChanged: (Boolean) -> Unit
) {
    val view = LocalView.current
    var keyboardOpen by remember { mutableStateOf(false) }

    DisposableEffect(view) {
        val viewTreeObserver = view.viewTreeObserver
        val listener = ViewTreeObserver.OnGlobalLayoutListener {
            val rect = Rect()
            view.getWindowVisibleDisplayFrame(rect)
            val screenHeight = view.rootView.height
            val keypadHeight = screenHeight - rect.bottom
            val isOpen = keypadHeight > screenHeight * 0.15

            if (keyboardOpen != isOpen) {
                keyboardOpen = isOpen
                onKeyboardVisibilityChanged(isOpen)
            }
        }

        viewTreeObserver.addOnGlobalLayoutListener(listener)

        onDispose {
            viewTreeObserver.removeOnGlobalLayoutListener(listener)
        }
    }
}

/**
 * Composable function to display a compact row with horizontally arranged content.
 *
 * @param content The composable content to be arranged in a row.
 */
@Composable
fun CompactRow(content: @Composable RowScope.() -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically,
        content = content
    )
}

/**
 * Composable function to display localized date and time.
 *
 * @param localDateTime The LocalDateTime object to be formatted and displayed.
 */
@Composable
fun DisplayLocalizedDateTime(localDateTime: LocalDateTime?) {
    val formattedDateTime = if (localDateTime != null) {
        try {
            val locale = Locale.getDefault()
            val formatter = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM)
                .withLocale(locale)
                .withZone(ZoneId.systemDefault())

            localDateTime.format(formatter)
        } catch (e: Exception) {
            Log.e("DisplayLocalizedDateTime", "Error formatting date", e)
            stringResource(R.string.invalid_datetime)
        }
    } else {
        stringResource(R.string.date_not_available)
    }

    Text(text = stringResource(R.string.production, formattedDateTime), style = MaterialTheme.typography.bodyMedium)
}
