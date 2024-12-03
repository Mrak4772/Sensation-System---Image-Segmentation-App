package com.rsvi.sensationsystem

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import org.tensorflow.lite.Interpreter
import org.tensorflow.lite.support.image.TensorImage
import android.content.res.AssetManager
import android.widget.Button
import org.tensorflow.lite.support.image.ops.ResizeOp
import org.tensorflow.lite.DataType
import org.tensorflow.lite.support.common.ops.NormalizeOp
import org.tensorflow.lite.support.image.ImageProcessor
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileInputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.channels.FileChannel
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class MainActivity : AppCompatActivity() {

    private lateinit var imageCapture: ImageCapture
    private lateinit var cameraExecutor: ExecutorService
    private lateinit var interpreter: Interpreter
    private var segmentedBitmap: Bitmap? = null // Nullable to avoid initialization issues

    companion object {
        private const val TAG = "SensationSystem"
        private const val MODEL_FILE = "segmentation_model.tflite"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Initialize components
        cameraExecutor = Executors.newSingleThreadExecutor()

        // Load TensorFlow Lite model
        try {
            val modelBuffer = loadModelFile(assets, MODEL_FILE)
            interpreter = Interpreter(modelBuffer)
        } catch (e: Exception) {
            Log.e(TAG, "Error loading TensorFlow Lite model: ${e.message}", e)
            Toast.makeText(this, "Model loading failed", Toast.LENGTH_SHORT).show()
        }

        // Request camera permissions
        requestPermissions()

        // Start camera
        startCamera()

        // Set click listener for "Capture and Analyze" button
        findViewById<Button>(R.id.captureButton).setOnClickListener {
            captureAndAnalyze()
        }

        // Set click listener for "View Result" button
        findViewById<Button>(R.id.viewResultButton).setOnClickListener {
            if (segmentedBitmap != null) {
                val intent = Intent(this, ResultActivity::class.java)

                // Convert the bitmap to a byte array to pass via Intent
                val byteArrayOutputStream = ByteArrayOutputStream()
                segmentedBitmap?.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream)
                val bitmapBytes = byteArrayOutputStream.toByteArray()

                intent.putExtra("segmentedImage", bitmapBytes)
                startActivity(intent)
            } else {
                Toast.makeText(this, "No result to show", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun loadModelFile(assetManager: AssetManager, modelPath: String): ByteBuffer {
        val fileDescriptor = assetManager.openFd(modelPath)
        val inputStream = FileInputStream(fileDescriptor.fileDescriptor)
        val fileChannel = inputStream.channel
        val startOffset = fileDescriptor.startOffset
        val declaredLength = fileDescriptor.declaredLength
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength)
    }

    private fun requestPermissions() {
        val permissions = arrayOf(Manifest.permission.CAMERA)
        if (!permissions.all {
                ContextCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED
            }) {
            ActivityCompat.requestPermissions(this, permissions, 101)
        }
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)

        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()
            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
            val preview = androidx.camera.core.Preview.Builder().build().also {
                it.setSurfaceProvider(findViewById<androidx.camera.view.PreviewView>(R.id.previewView).surfaceProvider)
            }

            imageCapture = ImageCapture.Builder().build()

            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageCapture)
            } catch (e: Exception) {
                Log.e(TAG, "Use case binding failed", e)
            }
        }, ContextCompat.getMainExecutor(this))
    }

    private fun captureAndAnalyze() {
        val photoFile = File(
            externalMediaDirs.firstOrNull(),
            "${System.currentTimeMillis()}.jpg"
        )

        val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()

        imageCapture.takePicture(outputOptions, ContextCompat.getMainExecutor(this),
            object : ImageCapture.OnImageSavedCallback {
                override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                    val bitmap = BitmapFactory.decodeFile(photoFile.absolutePath)

                    // Preprocess the image
                    val tensorImage = preprocessImage(bitmap)

                    // Perform segmentation
                    val outputBuffer = runSegmentation(tensorImage)

                    // Handle the segmentation results
                    handleSegmentationResults(outputBuffer)
                }

                override fun onError(exception: ImageCaptureException) {
                    Log.e(TAG, "Photo capture failed: ${exception.message}", exception)
                    Toast.makeText(this@MainActivity, "Error during detection: ${exception.message}", Toast.LENGTH_SHORT).show()
                }
            })
    }

    private fun preprocessImage(bitmap: Bitmap): TensorImage {
        val tensorImage = TensorImage(DataType.FLOAT32)
        tensorImage.load(bitmap)

        val imageProcessor = ImageProcessor.Builder()
            .add(ResizeOp(513, 513, ResizeOp.ResizeMethod.BILINEAR))
            .add(NormalizeOp(0.0f, 255.0f))
            .build()

        return imageProcessor.process(tensorImage)
    }

    private fun runSegmentation(tensorImage: TensorImage): ByteBuffer {
        val outputBuffer = ByteBuffer.allocateDirect(4 * 65 * 65 * 21).order(ByteOrder.nativeOrder())
        interpreter.run(tensorImage.buffer, outputBuffer)
        return outputBuffer
    }

    private fun handleSegmentationResults(outputBuffer: ByteBuffer) {
        val segmentedMask = decodeSegmentation(outputBuffer)
        segmentedBitmap = createBitmapFromMask(segmentedMask)

        Toast.makeText(this, "Segmentation completed successfully", Toast.LENGTH_SHORT).show()
    }

    private fun decodeSegmentation(outputBuffer: ByteBuffer): Array<IntArray> {
        val height = 65
        val width = 65
        val numClasses = 21
        val result = Array(height) { IntArray(width) }

        outputBuffer.rewind()
        val probabilities = FloatArray(numClasses)

        for (y in 0 until height) {
            for (x in 0 until width) {
                for (c in 0 until numClasses) {
                    probabilities[c] = outputBuffer.float
                }
                result[y][x] = probabilities.indices.maxByOrNull { probabilities[it] } ?: 0
            }
        }
        return result
    }

    private fun createBitmapFromMask(mask: Array<IntArray>): Bitmap {
        val height = mask.size
        val width = mask[0].size
        val colours = arrayOf(
            0xFFFF0000.toInt(), // Red
            0xFF00FF00.toInt(), // Green
            0xFF0000FF.toInt(), // Blue
            0xFFFFFF00.toInt(), // Yellow
            0xFF00FFFF.toInt(), // Cyan
            0xFFFF00FF.toInt(), // Magenta
            0xFF000000.toInt(), // Black
            0xFFFFFFFF.toInt()
        )

        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        for (y in mask.indices) {
            for (x in mask[y].indices) {
                val classIndex = mask[y][x]
                bitmap.setPixel(x, y, colours[classIndex % colours.size])
            }
        }
        return bitmap
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
        interpreter.close()
    }
}
