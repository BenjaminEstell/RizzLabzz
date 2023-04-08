package edu.umich.mlyao.gymbrofe


import android.Manifest
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.database.Cursor
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.google.android.material.bottomsheet.BottomSheetDialog
import edu.umich.mlyao.gymbrofe.databinding.ActivityMainBinding
import edu.umich.mlyao.gymbrofe.databinding.MachineCardBinding
import kotlinx.coroutines.*
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
    private lateinit var binding: MachineCardBinding
    private lateinit var machine : Machine
    private lateinit var card: BottomSheetDialog

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
                startActivity(Intent(this, MachineActivity::class.java))
                Log.d("PhotoPicker", "activity started")
//                val scope = CoroutineScope(Dispatchers.Default)
//                scope.launch {
//                    val label = uri?.let { processImage(it) }
//                    println(label)
//                    if (label != null) {
//                        idMachine(label)
//                    }
//                }

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
        val card = BottomSheetDialog(this)
        binding = MachineCardBinding.inflate(layoutInflater)

        // Create time stamped name and MediaStore entry.
        val name = SimpleDateFormat(FILENAME_FORMAT, Locale.US)
            .format(System.currentTimeMillis())
        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, name)
            put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
            put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/GymBro-Image")
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

                override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                    val msg = "Photo capture succeeded: ${output.savedUri}"
                    Toast.makeText(baseContext, msg, Toast.LENGTH_SHORT).show()
                    Log.d(TAG, msg)


                    // Analyze photo
                    val scope = CoroutineScope(Dispatchers.Default)
                    scope.launch {
                        val label = output.savedUri?.let { processImage(it) }

                        if (label != null) {
                            machine = idMachine(label)
                        }

                        if (machine.name != null) {
                            Log.d("Machine Name", machine.name.toString())
                        }
                        if (machine.instructions != null) {
                            Log.d("Machine Name", machine.instructions.toString())
                        }
                        if (machine.gifUrl != null) {
                            Log.d("Machine Name", machine.gifUrl.toString())
                        }
                        val view = populateCard(machine)
                        runOnUiThread {
                            if (view != null) {
                                card.setContentView(view)
                            }
                            card.show()
                        }
                    }
                }
            }
        )
        // Toast popup
        Thread.sleep(5000)
        Toast.makeText(this, "Analyzing image...", Toast.LENGTH_LONG).show()
        Thread.sleep(5000)
    }

    fun returnToHomeScreen(view: View) {
        val intent = Intent(view.context, MainActivity::class.java)
        startActivity(intent)
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
            ).apply {}.toTypedArray()
    }

    private suspend fun analyze(imageUri: Uri): String? {
        // Get Image Path
        val filePath = getRealPathFromURI(imageUri)
        val file = filePath?.let { File(it) }

        // Base 64 Encode
        val encodedFile: String
        val fileInputStreamReader = FileInputStream(file)
        val bytes = file?.length()?.let { ByteArray(it.toInt()) }
        fileInputStreamReader.read(bytes)
        encodedFile = String(Base64.getEncoder().encode(bytes), StandardCharsets.US_ASCII)
        val apiKey = "q6YVWAZVYbczbL2e1K4n" // Your API Key
        val modelEndpoint = "gymbro/2" // Set model endpoint (Found in Dataset URL)

        // Construct the URL
        val jpgName = filePath?.split("/")?.last()
        val uploadURL =
            "https://detect.roboflow.com/$modelEndpoint?api_key=$apiKey&name=$jpgName&confidence=1"


        // Http Request
        val connection: HttpURLConnection?

        // Configure connection to URL
        val url = URL(uploadURL)
        connection = url.openConnection() as HttpURLConnection
        connection.requestMethod = "POST"
        connection.setRequestProperty("Content-Type",
            "application/x-www-form-urlencoded")
        connection.setRequestProperty("Content-Length",
            encodedFile.toByteArray().size.toString()
        )
        connection.setRequestProperty("Content-Language", "en-US")
        connection.useCaches = false
        connection.doOutput = true

        val stream: InputStream
        // Send request

        withContext(Dispatchers.IO) {
            val wr = DataOutputStream(
                connection.outputStream)
            wr.writeBytes(encodedFile)
            wr.close()
            // Get Response
            stream = connection.inputStream
        }


        val reader = BufferedReader(InputStreamReader(stream))
        var line: String?
        var label = "test"
        var parts = emptyList<String>()
        var singleline = "test"
        val delimiter1 = "{\"time\":"
        val delimiter2 = ",\"image\":{\"width\":"
        val delimiter3 = ",\"height\":"
        val delimiter4 = "},\"predictions\":[{\"x\":"
        val delimiter5 = ",\"y\":"
        val delimiter6 = ",\"width\":"
        val delimiter7 = ",\"confidence\":"
        val delimiter8 = ",\"class\":\""
        val delimiter9 = "\"}]}"
        val delimiter10 = "\"},{\"x\":"

        while (reader.readLine().also { line = it } != null) {
            println(line)
            singleline = line.toString()
            parts = singleline.split(delimiter1,delimiter2,delimiter3,delimiter4,delimiter5,delimiter6,delimiter7,delimiter8,delimiter9,delimiter10)
            println(parts)
        }
        reader.close()
        //println(parts.size/6)
        var largestBox = 0.0
        var x = 0.0
        var y = 0.0
        var width = 0.0
        var height = 0.0
        for (i in 1..parts.size/6){
            //println(parts[3+6+6*(i-1)])
            var box = parts[3+3+6*(i-1)].toDouble()*parts[3+4+6*(i-1)].toDouble()
            //println(box)

            if(box > largestBox){
                largestBox = box
                label = parts[3+6+6*(i-1)]
                x = parts[3+1+6*(i-1)].toDouble()
                y = parts[3+2+6*(i-1)].toDouble()
                width = parts[3+3+6*(i-1)].toDouble()
                height = parts[3+4+6*(i-1)].toDouble()
            }
        }
        val realx = x - width/2
        val realy = y - height/2
        println(realx)
        println(realy)
        println(width)
        println(height)

        val decodedString = Base64.getDecoder().decode(encodedFile)
        var bitmap = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.size)
        bitmap = rotateBitmap(bitmap, 90f)
        println(bitmap.width)
        println(bitmap.height)
        // Crop the subimage based on the given x, y, width, and height
        val subimage = Bitmap.createBitmap(bitmap, realx.toInt(), realy.toInt(), width.toInt(), height.toInt())
        //val subimage = Bitmap.createBitmap(bitmap, realx.toInt(), realy.toInt(), width.toInt(), height.toInt())
        val outputStream = ByteArrayOutputStream()
        subimage.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
        val byteArray = outputStream.toByteArray()
        val encodedFilecrop = String(Base64.getEncoder().encode(byteArray), StandardCharsets.US_ASCII)
        println(encodedFile) //original base64 string
        println(encodedFilecrop) //cropped base64 string


        if (label == "test"){
            label = "No machine found"
        }


        connection.disconnect()
//        Toast.makeText(baseContext, label, Toast.LENGTH_SHORT).show()
        println(label)
        return label
    }


    private suspend fun idMachine(label: String): Machine {
        return MachineActivity.getMachine(label)
    }
    private suspend fun processImage(output_uri: Uri): String? {
        return output_uri?.let { analyze(it) }
    }

    private suspend fun populateCard(machine: Machine): View? {
        runOnUiThread {
            binding.machineName.text = machine.name
            binding.machineInstructions.text = machine.instructions
//            ImageView imageView = (ImageView) findViewById(R.id.machine_usage_image);
            Glide.with(this).load(machine.gifUrl).into(binding.machineUsageImage)
//            binding.machineUsageImage.setImageResource(machine.gifUrl)
            binding.cardView.visibility = View.VISIBLE
        }
        val view = binding.root
        return view
    }

    private fun getRealPathFromURI(uri: Uri): String? {
        val cursor: Cursor? = contentResolver.query(uri, null, null, null, null)
        cursor?.moveToFirst()
        val idx: Int? = cursor?.getColumnIndex(MediaStore.Images.ImageColumns.DATA)
        val realPath = idx?.let { cursor.getString(it) }
        cursor?.close()
        return realPath
    }

    fun rotateBitmap(bitmap: Bitmap, degrees: Float): Bitmap {
        val matrix = Matrix()
        matrix.postRotate(degrees)
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
    }
}