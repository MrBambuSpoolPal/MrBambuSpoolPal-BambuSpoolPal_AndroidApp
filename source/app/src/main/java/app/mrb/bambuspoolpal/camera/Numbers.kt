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

package app.mrb.bambuspoolpal.camera

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.coroutines.delay
import java.util.concurrent.Executors

private const val TAG = "NumberRecognition"
private const val CAMERA_PERMISSION_REQUEST_CODE = 123

class NumberRecognitionActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Check if all required permissions are granted
        if (allPermissionsGranted()) {
            // If permissions are granted, start the camera with the content
            startCameraWithContent()
        } else {
            // If permissions are not granted, request them
            ActivityCompat.requestPermissions(
                this, REQUIRED_PERMISSIONS, CAMERA_PERMISSION_REQUEST_CODE
            )
        }
    }

    private fun startCameraWithContent() {
        setContent {
            // Set the Jetpack Compose content for the activity
            NumberRecognitionActivityContent(onNumberValidated = { number ->
                // Callback function to handle the validated number
                val resultIntent = Intent()
                resultIntent.putExtra("VALIDATED_NUMBER", number)
                setResult(RESULT_OK, resultIntent)
                finish() // Finish the activity and return the result
            })
        }
    }

    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(baseContext, it) == PackageManager.PERMISSION_GRANTED
    }

    companion object {
        private val REQUIRED_PERMISSIONS = arrayOf(Manifest.permission.CAMERA)
    }
}

@Composable
fun NumberRecognitionActivityContent(onNumberValidated: (String?) -> Unit) {
    MaterialTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            // State to hold the recognized number
            var recognizedNumber by remember { mutableStateOf("") }
            // State to hold the validated number
            var validatedNumber by remember { mutableStateOf("") }

            Column(modifier = Modifier.fillMaxSize()) {
                // Box to stack the camera preview and the mire overlay
                Box(modifier = Modifier.weight(1f)) {
                    // Composable function to display the camera preview
                    CameraPreview(onNumberRecognized = { number ->
                        recognizedNumber = number
                    })
                    // Composable function to draw the mire overlay
                    MireOverlay()
                    // Text to display "Scanning..." at the bottom
                    Text(
                        text = "Scanning...",
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(16.dp),
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
                // Column to hold the recognized number, validate button, and validated number
                Column(
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Text to display the recognized number
                    Text(
                        text = "Recognized Number: $recognizedNumber",
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    // Button to trigger validation (in this case, just stores the recognized number)
                    Button(
                        onClick = { validatedNumber = recognizedNumber },
                        enabled = recognizedNumber.isNotEmpty()
                    ) {
                        Text("Validate")
                    }

                    // Display the validated number if it's not empty
                    if (validatedNumber.isNotEmpty()) {
                        Text(
                            text = "Validated : $validatedNumber",
                            color = Color.Green,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                        // LaunchedEffect to send the validated number back after a short delay
                        LaunchedEffect(validatedNumber) {
                            delay(1000) // Delay for 1 second
                            onNumberValidated(validatedNumber) // Call the callback to return the result
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MireOverlay(color: Color = Color.Red, strokeWidth: Float = 5f, rectWidthDp: Dp = 200.dp, rectHeightDp: Dp = 100.dp) {
    // Canvas composable to draw the rectangular mire overlay
    Canvas(modifier = Modifier.fillMaxSize()) {
        val canvasWidth = size.width
        val canvasHeight = size.height

        // Convert Dp values to pixels
        val rectWidth = rectWidthDp.toPx()
        val rectHeight = rectHeightDp.toPx()

        // Calculate the top-left coordinates to center the rectangle
        val topLeftX = (canvasWidth - rectWidth) / 2
        val topLeftY = (canvasHeight - rectHeight) / 2

        // Draw the rectangle
        drawRect(
            color = color,
            topLeft = Offset(topLeftX, topLeftY),
            size = Size(rectWidth, rectHeight),
            style = Stroke(width = strokeWidth) // Use Stroke to draw the outline of the rectangle
        )
    }
}

@Composable
fun CameraPreview(onNumberRecognized: (String) -> Unit) {
    val context = LocalContext.current
    val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current
    // Create a future for the ProcessCameraProvider
    val cameraProviderFuture = remember { ProcessCameraProvider.getInstance(context) }

    AndroidView(
        factory = { ctx: android.content.Context ->
            // Create a PreviewView to display the camera feed
            val previewView = androidx.camera.view.PreviewView(ctx)

            cameraProviderFuture.addListener({
                // Get the ProcessCameraProvider
                val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()
                // Build the Preview use case
                val preview = Preview.Builder()
                    .build()
                    .also {
                        it.surfaceProvider = previewView.surfaceProvider
                    }

                // Build the ImageAnalysis use case
                val imageAnalysis = ImageAnalysis.Builder()
                    .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                    .build()
                    .also {
                        it.setAnalyzer(
                            Executors.newSingleThreadExecutor(),
                            NumberRecognitionAnalyzer(onNumberRecognized)
                        )
                    }

                // Select the back camera as the default
                val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

                try {
                    // Unbind all use cases before binding new ones
                    cameraProvider.unbindAll()
                    // Bind the camera use cases to the lifecycle
                    cameraProvider.bindToLifecycle(
                        lifecycleOwner,
                        cameraSelector,
                        preview,
                        imageAnalysis
                    )
                } catch (exc: Exception) {
                    Log.e(TAG, "Binding of camera failed", exc)
                }
            }, ContextCompat.getMainExecutor(ctx))
            previewView
        },
        modifier = Modifier.fillMaxSize()
    )
}

private class NumberRecognitionAnalyzer(
    private val onNumberRecognized: (String) -> Unit
) : ImageAnalysis.Analyzer {

    // Get an instance of the TextRecognizer
    private val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

    override fun analyze(imageProxy: ImageProxy) {
        // Get the media image from the ImageProxy

        @androidx.camera.core.ExperimentalGetImage
        val mediaImage = imageProxy.image
        if (mediaImage == null) {
            imageProxy.close()
            return
        }

        // Convert the media image to an InputImage for ML Kit
        val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)

        // Process the image with the TextRecognizer
        recognizer.process(image)
            .addOnSuccessListener { visionText ->
                // Extract recognized numbers from the VisionText
                val numbers = visionText.textBlocks.flatMap { block ->
                    block.lines.mapNotNull { line ->
                        val text = line.text
                        // Filter lines that contain only numbers
                        if (text.matches(Regex("[0-9]+"))) {
                            text
                        } else {
                            null
                        }
                    }
                }.joinToString(separator = "\n")

                // If numbers are recognized, call the onNumberRecognized callback
                if (numbers.isNotEmpty()) {
                    onNumberRecognized(numbers)
                }
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Text recognition failed", e)
            }
            .addOnCompleteListener {
                imageProxy.close() // Always close the ImageProxy after processing
            }
    }
}