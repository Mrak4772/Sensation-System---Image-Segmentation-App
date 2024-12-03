package com.rsvi.sensationsystem

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import java.io.File
import java.io.FileOutputStream

class ResultActivity : AppCompatActivity() {

    private lateinit var resultImageView: ImageView
    private lateinit var saveButton: Button
    private lateinit var shareButton: Button
    private var segmentedBitmap: Bitmap? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_result)

        resultImageView = findViewById(R.id.resultImageView)
        saveButton = findViewById(R.id.saveImageButton)
        shareButton = findViewById(R.id.shareImageButton)

        // Retrieve the segmented image from the intent
        val byteArray = intent.getByteArrayExtra("segmentedImage")
        if (byteArray != null) {
            segmentedBitmap = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.size)
            resultImageView.setImageBitmap(segmentedBitmap)
        } else {
            Toast.makeText(this, "No image to display", Toast.LENGTH_SHORT).show()
        }

        saveButton.setOnClickListener {
            segmentedBitmap?.let { bitmap ->
                saveImage(bitmap)
            } ?: Toast.makeText(this, "No image to save", Toast.LENGTH_SHORT).show()
        }

        shareButton.setOnClickListener {
            Toast.makeText(this, "Sharing feature not yet implemented!", Toast.LENGTH_SHORT).show()
        }
    }

    private fun saveImage(bitmap: Bitmap) {
        val filename = "segmented_image_${System.currentTimeMillis()}.png"
        val file = File(getExternalFilesDir(null), filename)

        try {
            FileOutputStream(file).use { outputStream ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
                Toast.makeText(this, "Image saved: ${file.absolutePath}", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            Toast.makeText(this, "Failed to save image: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
}
