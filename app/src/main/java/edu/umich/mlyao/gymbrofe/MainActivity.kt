package edu.umich.mlyao.gymbrofe

import android.Manifest
import android.content.ContentValues
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import edu.umich.mlyao.gymbrofe.databinding.ActivityMainBinding
import java.io.*
import java.net.HttpURLConnection
import java.net.URL
import java.nio.charset.StandardCharsets
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors


class MainActivity : AppCompatActivity() {
    private lateinit var viewBinding: ActivityMainBinding

    private var imageCapture: ImageCapture? = null

    private lateinit var cameraExecutor: ExecutorService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(viewBinding.root)

        // Request camera permissions
        if (allPermissionsGranted()) {
            startCamera()
        } else {
            ActivityCompat.requestPermissions(
                this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS)
        }

        // Set up the listeners for take photo and pick image buttons
        viewBinding.imageCaptureButton.setOnClickListener { takePhoto() }

        // Registers a photo picker activity launcher in single-select mode.
        val pickMedia = registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
            // Callback is invoked after the user selects a media item or closes the
            // photo picker.
            if (uri != null) {
                Log.d("PhotoPicker", "Selected URI: $uri")

                // Analyze photo
                // analyze(uri)
            } else {
                Log.d("PhotoPicker", "No media selected")
            }
        }
        viewBinding.imageGalleryButton.setOnClickListener {
            // Launch the photo picker and allow the user to choose only images.
            pickMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        }

        cameraExecutor = Executors.newSingleThreadExecutor()
    }

    private fun takePhoto() {
        // Get a stable reference of the modifiable image capture use case
        val imageCapture = imageCapture ?: return

        // Create time stamped name and MediaStore entry.
        val name = SimpleDateFormat(FILENAME_FORMAT, Locale.US)
            .format(System.currentTimeMillis())
        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, name)
            put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
            if(Build.VERSION.SDK_INT > Build.VERSION_CODES.P) {
                put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/GymBro-Image")
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
            outputOptions,
            ContextCompat.getMainExecutor(this),
            object : ImageCapture.OnImageSavedCallback {
                override fun onError(exc: ImageCaptureException) {
                    Log.e(TAG, "Photo capture failed: ${exc.message}", exc)
                }

                override fun
                        onImageSaved(output: ImageCapture.OutputFileResults){
                    val msg = "Photo capture succeeded: ${output.savedUri}"
                    Toast.makeText(baseContext, msg, Toast.LENGTH_SHORT).show()
                    Log.d(TAG, msg)

                    // Analyze photo
                    // output.savedUri?.let { analyze(it) }
                }
            }
        )
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

            imageCapture = ImageCapture.Builder().build()

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

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String>, grantResults:
        IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
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

    companion object {
        private const val TAG = "GymBro"
        private const val FILENAME_FORMAT = "yyyy-MM-dd-HH-mm-ss-SSS"
        private const val REQUEST_CODE_PERMISSIONS = 10
        private val REQUIRED_PERMISSIONS =
            mutableListOf (
                Manifest.permission.CAMERA
            ).apply {
                if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
                    add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                }
            }.toTypedArray()
    }

    private fun analyze(imageUri: Uri) {
        // Get Image Path
        val filePath = System.getProperty("user.dir") + System.getProperty("file.separator") + "app/src/main/java/edu/umich/anasharm/gymbrotest/chestpress2.JPG"
        //val filePath = "/Users/erican/Desktop/Rizz/app/src/main/java/edu/umich/anasharm/gymbrotest/chestpress2.JPG"
        val file = File(filePath)

        // Base 64 Encode
        val encodedFile: String
        val fileInputStreamReader = FileInputStream(file)
        val bytes = ByteArray(file.length().toInt())
        fileInputStreamReader.read(bytes)
        encodedFile = String(Base64.getEncoder().encode(bytes), StandardCharsets.US_ASCII)
        val API_KEY = "q6YVWAZVYbczbL2e1K4n" // Your API Key
        val MODEL_ENDPOINT = "gymbro/2" // Set model endpoint (Found in Dataset URL)

        // Construct the URL
        val uploadURL ="https://detect.roboflow.com/" + MODEL_ENDPOINT + "?api_key=" + API_KEY + "&name=chestpress2.JPG";

        var label: String?

        // Http Request
        var connection: HttpURLConnection? = null

        // Configure connection to URL
        val url = URL(uploadURL)
        connection = url.openConnection() as HttpURLConnection
        connection.requestMethod = "POST"
        connection.setRequestProperty("Content-Type",
            "application/x-www-form-urlencoded")
        connection.setRequestProperty("Content-Length",
            Integer.toString(encodedFile.toByteArray().size))
        connection.setRequestProperty("Content-Language", "en-US")
        connection.useCaches = false
        connection.doOutput = true

        //Send request
        val wr = DataOutputStream(
            connection.outputStream)
        wr.writeBytes(encodedFile)
        wr.close()

        // Get Response
        val stream = connection.inputStream
        val reader = BufferedReader(InputStreamReader(stream))
        var line: String?
        var firstline = "test"
        var count = 0
        while (reader.readLine().also { line = it } != null) {
            if(count == 0){
                firstline = (line).toString()
                count = 1
            }
        }
        reader.close()

        var delimiter1 = "{\"time\":"
        var delimiter2 = ",\"image\":{\"width\":"
        var delimiter3 = ",\"height\":"
        var delimiter4 = "},\"predictions\":[{\"x\":"
        var delimiter5 = ",\"y\":"
        var delimiter6 = ",\"width\":"
        var delimiter7 = ",\"confidence\":"
        var delimiter8 = ",\"class\":\""
        var delimiter9 = "\"}]}"
        println(firstline)
        val parts = firstline.split(delimiter1,delimiter2,delimiter3,delimiter4,delimiter5,delimiter6,delimiter7,delimiter8,delimiter9)
        println(parts)
        label = parts[9]

        connection?.disconnect()

        //return label when used
        println(label)
    }
}