package com.shubhamkumarwinner.camera.feature

import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import com.shubhamkumarwinner.camera.databinding.CameraFragmentBinding
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

class CameraFragment : Fragment() {
//    private val viewModel: CameraViewModel by viewModels()
    lateinit var currentPhotoPath: String

    private lateinit var binding: CameraFragmentBinding
    private val takePicture = registerForActivityResult(
        ActivityResultContracts
        .TakePicturePreview()) {
        binding.imageView.setImageBitmap(it)
        saveToFile(it)
    }



    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        binding = CameraFragmentBinding.inflate(layoutInflater, container, false)
        binding.openCamera.setOnClickListener {
            takePicture.launch(null)
        }
        return binding.root
    }

    private fun saveToFile(bitmap: Bitmap): File? {
        var file: File? = null
        return try {
            val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
            file = File("/storage/emulated/0/Pictures/${"JPEG_${timeStamp}_.jpg"}")
            file.createNewFile()

            Log.d("test", "name: $file")

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

}

