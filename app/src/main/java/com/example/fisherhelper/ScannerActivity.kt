package com.example.fisherhelper

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import androidx.appcompat.app.AlertDialog
import android.app.Dialog
import android.app.DialogFragment
import android.content.ContentValues
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.media.Image
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.video.Recorder
import androidx.camera.video.Recording
import androidx.camera.video.VideoCapture
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.fisherhelper.databinding.ActivityScannerBinding
import com.google.mlkit.common.model.LocalModel
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.label.ImageLabeling
import com.google.mlkit.vision.label.custom.CustomImageLabelerOptions
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.activity_scanner.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors


typealias LumaListener = (luma: Double) -> Unit


class ScannerActivity : AppCompatActivity() {
    private lateinit var viewBinding: ActivityScannerBinding

    private var imageCapture: ImageCapture? = null

    private var videoCapture: VideoCapture<Recorder>? = null
    private var recording: Recording? = null

    private lateinit var cameraExecutor: ExecutorService

    override fun onCreate(savedInstanceState: Bundle?) {
        supportActionBar?.hide()
        super.onCreate(savedInstanceState)
        viewBinding = ActivityScannerBinding.inflate(layoutInflater)
        setContentView(viewBinding.root)

        // Request camera permissions
        if (allPermissionsGranted()) {
            startCamera()
        } else {
            ActivityCompat.requestPermissions(
                this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS)
        }

        // Set up the listeners for take photo and video capture buttons
        viewBinding.imageCaptureButton.setOnClickListener { takePhoto() }

        cameraExecutor = Executors.newSingleThreadExecutor()
    }

    private fun takePhoto() {
        // Get a stable reference of the modifiable image capture use case
        val imageCapture = imageCapture ?: return
        image_capture_button.setText("Scanning...")

        // Create time stamped name and MediaStore entry.
        val name = SimpleDateFormat(FILENAME_FORMAT, Locale.US)
            .format(System.currentTimeMillis())
        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, name)
            put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
            if(Build.VERSION.SDK_INT > Build.VERSION_CODES.P) {
                put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/CameraX-Image")
            }
        }

        // Create output options object which contains file + metadata
        val outputOptions = ImageCapture.OutputFileOptions
            .Builder(contentResolver,
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                contentValues)
            .build()

        // Set up image capture listener, which is triggered after photo has
        // been taken
        imageCapture.takePicture(
            ContextCompat.getMainExecutor(this),
            object : ImageCapture.OnImageCapturedCallback()    {
                override fun onCaptureSuccess(imageProxy: ImageProxy) = runBlocking{
                    val job = launch { scanner(imageProxy) }
                    job.join()
                }
            }
        )
    }


    private fun scanner(image: ImageProxy) {
        val mediaImage = image.image
        if(mediaImage != null) {
                val image = InputImage.fromMediaImage(mediaImage!!, image.imageInfo.rotationDegrees)
                Log.d("sdfghjklkjhgfdsdfghjkl", "1")
                val localModel = LocalModel.Builder()
                    .setAssetFilePath("model.tflite")
                    .build()
                Log.d("sdfghjklkjhgfdsdfghjkl", "2")
                val customImageLabelerOptions = CustomImageLabelerOptions.Builder(localModel)
                    .setConfidenceThreshold(0.5f)
                    .setMaxResultCount(5)
                    .build()
                Log.d("sdfghjklkjhgfdsdfghjkl", "3")
                val labeler = ImageLabeling.getClient(customImageLabelerOptions)
                Log.d("sdfghjklkjhgfdsdfghjkl", "4")
                var outputText = ""
                var maxConfidence = 0f
                Log.d("test", "test")
                labeler.process(image).addOnSuccessListener { labels ->
                    // Task completed successfully
                    for (label in labels) {
                        Log.d(label.text, labels.toString() + " " + labels.size)
                        if (label.confidence > maxConfidence) {
                            maxConfidence = label.confidence
                            outputText = label.text
                        }

                    }
                    image_capture_button.setText(maxConfidence.toString())
                    if (maxConfidence >= 0.7) {
                        val genusSpecies = outputText.split(" ")
                        val url = "https://www.fishbase.se/summary/${genusSpecies[0]}-${genusSpecies[1]}.html"
                        val i = Intent(Intent.ACTION_VIEW)
                        i.data = Uri.parse(url)
                        startActivity(i)
                    } else if (maxConfidence < 0.7 && maxConfidence > 0) {
                        val builder = AlertDialog.Builder(this@ScannerActivity)
                        builder.setTitle(R.string.dialog_titleNotSure)
                            .setMessage(R.string.dialog_notSure)
                            .setCancelable(false)
                            .setPositiveButton(R.string.find) { dialogInterface, it ->
                                dialogInterface.cancel()
                                val genusSpecies = outputText.split(" ")
                                val url = "https://www.fishbase.se/summary/${genusSpecies[0]}-${genusSpecies[1]}.html"
                                val i = Intent(Intent.ACTION_VIEW)
                                i.data = Uri.parse(url)
                                startActivity(i)
                            }
                            .setNegativeButton(R.string.tryAgain) { dialogInterface, it ->
                                dialogInterface.cancel()
                            }
                            .show()
                    } else {
                        Log.d(maxConfidence.toString(), maxConfidence.toString())
                        val builder = AlertDialog.Builder(this@ScannerActivity)
                        builder.setTitle(R.string.dialog_titleNotFound)
                            .setMessage(R.string.dialog_notFound)
                            .setCancelable(false)
                            .setPositiveButton(R.string.accept) { dialogInterface, it ->
                                dialogInterface.cancel()
                            }
                            .show()

                    }
                    image_capture_button.setText("Scan")
                }
                    .addOnFailureListener { e ->
                        //Error
                    }
            }
            image.close()
        }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)

        cameraProviderFuture.addListener({
            // Used to bind the lifecycle of cameras to the lifecycle owner
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()

            // Preview
            val preview = Preview.Builder()
                .build()
                .also {
                    it.setSurfaceProvider(viewBinding.viewFinder.surfaceProvider)
                }

            imageCapture = ImageCapture.Builder()
                .build()

            // Select back camera as a default
            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            try {
                // Unbind use cases before rebinding
                cameraProvider.unbindAll()

                // Bind use cases to camera
                cameraProvider.bindToLifecycle(
                    this, cameraSelector, preview, imageCapture)

            } catch(exc: Exception) {
                Log.e(TAG, "Use case binding failed", exc)
            }

        }, ContextCompat.getMainExecutor(this))
    }

    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(
            baseContext, it) == PackageManager.PERMISSION_GRANTED
    }

    @SuppressLint("MissingSuperCall")
    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String>, grantResults:
        IntArray) {
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (allPermissionsGranted()) {
                startCamera()
            } else {
                Toast.makeText(this,
                    "Permissions not granted by the user.",
                    Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
    }

    companion object {
        private const val TAG = "CameraXApp"
        private const val FILENAME_FORMAT = "yyyy-MM-dd-HH-mm-ss-SSS"
        private const val REQUEST_CODE_PERMISSIONS = 10
        private val REQUIRED_PERMISSIONS =
            mutableListOf (
                Manifest.permission.CAMERA,
            ).apply {
                if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
                    add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                }
            }.toTypedArray()
    }
}
