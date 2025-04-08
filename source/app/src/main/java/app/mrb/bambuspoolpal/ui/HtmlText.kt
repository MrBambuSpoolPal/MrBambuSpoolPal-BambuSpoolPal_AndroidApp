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

import android.text.style.URLSpan
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextDecoration
import androidx.core.text.HtmlCompat

@Composable
fun HtmlText(htmlString: String) {
    val uriHandler = LocalUriHandler.current

    val spanned = HtmlCompat.fromHtml(htmlString, HtmlCompat.FROM_HTML_MODE_LEGACY)

    var layoutResult: TextLayoutResult? = null

    val annotatedString = buildAnnotatedString {
        append(spanned.toString())
        spanned.getSpans(0, spanned.length, URLSpan::class.java).forEach { span ->
            val url = span.url
            val start = spanned.getSpanStart(span)
            val end = spanned.getSpanEnd(span)

            addStyle(
                style = SpanStyle(
                    color = Color.Blue,
                    textDecoration = TextDecoration.Underline
                ),
                start = start,
                end = end
            )

            addStringAnnotation(
                tag = "URL",
                annotation = url,
                start = start,
                end = end
            )
        }
    }

    Text(
        text = annotatedString,
        modifier = Modifier.pointerInput(Unit) {
            detectTapGestures { offset: Offset ->
                layoutResult?.let { layout ->
                    val position = layout.getOffsetForPosition(offset)
                    annotatedString
                        .getStringAnnotations(tag = "URL", start = position, end = position)
                        .firstOrNull()
                        ?.let { annotation ->
                            uriHandler.openUri(annotation.item)
                        }
                }
            }
        },
        onTextLayout = { layoutResult = it }
    )
}


