package com.shubhamkumarwinner.camera.feature

import android.app.Activity
import android.app.Activity.RESULT_OK
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.provider.MediaStore.Images.Media.getBitmap
import android.text.method.ScrollingMovementMethod
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import com.shubhamkumarwinner.camera.databinding.CameraFragmentBinding
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

const val REQUEST_VIDEO_CAPTURE = 1
class CameraFragment : Fragment() {
//    private val viewModel: CameraViewModel by viewModels()
    lateinit var currentPhotoPath: String

    private lateinit var binding: CameraFragmentBinding
    private val takePicture = registerForActivityResult(
        ActivityResultContracts
        .TakePicturePreview()) {
//        binding.imageView.setImageBitmap(it)
        saveImageToFile(it)
    }



    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        binding = CameraFragmentBinding.inflate(layoutInflater, container, false)
        binding.openCamera.setOnClickListener {
//            takePicture.launch(null)
//            takeVideo.launch(null)
            dispatchTakeVideoIntent()

        }
        return binding.root
    }

    private fun dispatchTakeVideoIntent() {
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
    }

    /*private fun saveVideoToFile(uri: Uri): File? {
        var file: File? = null
        var bitmap = getBitmap(requireActivity().contentResolver, uri)
        return try {
            val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
            file = File("/storage/emulated/0/Pictures/${"VID_${timeStamp}_.mp4"}")
            file.createNewFile()

            //Convert bitmap to byte array
            val bos = ByteArrayOutputStream()
//            bitmap.compress(Bitmap.CompressFormat.WEBP, 100, bos) // YOU can also save it in JPEG
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

}

