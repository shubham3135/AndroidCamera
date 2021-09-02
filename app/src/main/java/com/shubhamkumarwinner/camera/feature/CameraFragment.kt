package com.shubhamkumarwinner.camera.feature

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.shubhamkumarwinner.camera.R
import com.shubhamkumarwinner.camera.databinding.CameraFragmentBinding
import java.io.File
import java.nio.ByteBuffer
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

const val REQUEST_VIDEO_CAPTURE = 1
typealias LumaListener = (luma: Double) -> Unit
class CameraFragment : Fragment() {
    private var imageCapture: ImageCapture? = null
    private var cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

    private var outputDirectory: File? = null
    private lateinit var cameraExecutor: ExecutorService

    private lateinit var binding: CameraFragmentBinding
    /*private val takePicture = registerForActivityResult(
        ActivityResultContracts
        .TakePicturePreview()) {
//        binding.imageView.setImageBitmap(it)
//        saveImageToFile(it)
    }*/

    private val multiplePermission = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ){ result ->
        if (result[Manifest.permission.CAMERA] == true
            && result[Manifest.permission.WRITE_EXTERNAL_STORAGE] == true
            && result[Manifest.permission.READ_EXTERNAL_STORAGE] == true){
            startCamera()
        }
        else if(!shouldShowRequestPermissionRationale(Manifest.permission.CAMERA)
            || !shouldShowRequestPermissionRationale(Manifest.permission.WRITE_EXTERNAL_STORAGE)
            || !shouldShowRequestPermissionRationale(Manifest.permission.READ_EXTERNAL_STORAGE)){
            val alertDialog: AlertDialog.Builder = AlertDialog.Builder(requireContext())
            alertDialog.setTitle("You have permanently denied required permission")
            alertDialog.setMessage("Please allow it in your settings")
            alertDialog.setPositiveButton(
                "Settings"
            ) { _, _ ->
                startActivity(
                    Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                        .setData(Uri.fromParts("package",
                            requireActivity().packageName, null)))
            }
            alertDialog.setNegativeButton(
                "Not now"
            ) { _, _ ->

            }
            val alert: AlertDialog = alertDialog.create()
            alert.setCanceledOnTouchOutside(false)
            alert.show()
        }
        else{
            Toast.makeText(requireContext(), "Permission is denied", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        activity?.window?.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN)
        binding = CameraFragmentBinding.inflate(layoutInflater, container, false)

        multiplePermission.launch(
            arrayOf(
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.CAMERA
            )
        )

        binding.openCamera.setOnClickListener {
            takePhoto()
        }
        binding.flipCamera.setOnClickListener {
            if (cameraSelector == CameraSelector.DEFAULT_BACK_CAMERA) {
                cameraExecutor.shutdown()
                cameraSelector = CameraSelector.DEFAULT_FRONT_CAMERA
                startCamera()
            }else{
                cameraExecutor.shutdown()
                cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
                startCamera()
            }
        }
        outputDirectory = File("/storage/emulated/0/${getString(R.string.app_name)}Photo").apply {
            mkdir()
        }

        cameraExecutor = Executors.newSingleThreadExecutor()
        return binding.root
    }

    private fun takePhoto() {
        // Get a stable reference of the modifiable image capture use case
        val imageCapture = imageCapture ?: return

        // Create time-stamped output file to hold the image
        val photoFile = File(
            outputDirectory,
            "IMG_"+SimpleDateFormat(FILENAME_FORMAT, Locale.US
            ).format(System.currentTimeMillis()) + ".jpg")

        // Create output options object which contains file + metadata
        val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()

        // Set up image capture listener, which is triggered after photo has
        // been taken
        imageCapture.takePicture(
            outputOptions, ContextCompat.getMainExecutor(requireContext()), object : ImageCapture.OnImageSavedCallback {
                override fun onError(exc: ImageCaptureException) {
                    Log.e(TAG, "Photo capture failed: ${exc.message}", exc)
                }

                override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                    val savedUri = Uri.fromFile(photoFile)
                    val msg = "Photo capture succeeded: $savedUri"
                    Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()
                    Log.d(TAG, msg)
                }
            })
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext())

        cameraProviderFuture.addListener(Runnable {
            // Used to bind the lifecycle of cameras to the lifecycle owner
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()

            // Preview
            val preview = Preview.Builder()
                .build()
                .also {
                    it.setSurfaceProvider(binding.previewView.surfaceProvider)
                }

            imageCapture = ImageCapture.Builder()
                .build()

            val imageAnalyzer = ImageAnalysis.Builder()
                .build()
                .also {
                    it.setAnalyzer(cameraExecutor, LuminosityAnalyzer { luma ->
//                        Log.d(TAG, "Average luminosity: $luma")
                    })
                }

            try {
                // Unbind use cases before rebinding
                cameraProvider.unbindAll()

                // Bind use cases to camera
                cameraProvider.bindToLifecycle(
                    this, cameraSelector, preview, imageCapture, imageAnalyzer)

            } catch(exc: Exception) {
                Log.e(TAG, "Use case binding failed", exc)
            }

        }, ContextCompat.getMainExecutor(requireContext()))
    }

    /*private fun dispatchTakeVideoIntent() {
        Intent(MediaStore.ACTION_VIDEO_CAPTURE).also { takeVideoIntent ->
            takeVideoIntent.resolveActivity(requireActivity().packageManager)?.also {
                startActivityForResult(takeVideoIntent, REQUEST_VIDEO_CAPTURE)
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == REQUEST_VIDEO_CAPTURE && resultCode == RESULT_OK) {
            val videoUri: Uri = data?.data!!
//            saveVideoToFile(videoUri)
            binding.videoView.setVideoURI(videoUri)
            binding.videoView.start()
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    private fun saveImageToFile(bitmap: Bitmap): File? {
        var file: File? = null
        return try {
            val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
            file = File("/storage/emulated/0/Pictures/${"JPEG_${timeStamp}_.jpg"}")
            file.createNewFile()

            //Convert bitmap to byte array
            val bos = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bos) // YOU can also save it in JPEG
            val bitmapData = bos.toByteArray()

            //write the bytes in file
            val fos = FileOutputStream(file)
            fos.write(bitmapData)
            fos.flush()
            fos.close()
            file
        } catch (e: Exception) {
            e.printStackTrace()
            file // it will return null
        }
    }*/

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
    }

    private class LuminosityAnalyzer(private val listener: LumaListener) : ImageAnalysis.Analyzer {

        private fun ByteBuffer.toByteArray(): ByteArray {
            rewind()    // Rewind the buffer to zero
            val data = ByteArray(remaining())
            get(data)   // Copy the buffer into a byte array
            return data // Return the byte array
        }

        override fun analyze(image: ImageProxy) {

            val buffer = image.planes[0].buffer
            val data = buffer.toByteArray()
            val pixels = data.map { it.toInt() and 0xFF }
            val luma = pixels.average()

            listener(luma)

            image.close()
        }
    }

    companion object {
        private const val TAG = "CameraXBasic"
        private const val FILENAME_FORMAT = "yyyyMMdd_HHmm_ssSSS"
    }

}

