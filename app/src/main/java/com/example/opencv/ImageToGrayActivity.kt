package com.example.opencv

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Bundle
import android.provider.MediaStore
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.opencv.databinding.ActivityImageToGrayBinding
import org.opencv.android.Utils
import org.opencv.core.Mat
import org.opencv.imgproc.Imgproc

class ImageToGrayActivity : AppCompatActivity() {
    private lateinit var binding: ActivityImageToGrayBinding
    private var bitmap: Bitmap? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityImageToGrayBinding.inflate(layoutInflater)
        setContentView(binding.root)
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)== PackageManager.PERMISSION_DENIED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), 111)
        }
        binding.chooseImage.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            startActivityForResult(intent, 111)
        }
        binding.convert.setOnClickListener {
            if(bitmap == null) {
                return@setOnClickListener
            }
            val mat1 = Mat()
            val mat2 = Mat()
            val grayBitmap = Bitmap.createBitmap(bitmap!!.width, bitmap!!.height, Bitmap.Config.RGB_565)
            Utils.bitmapToMat(bitmap, mat1)
            Imgproc.cvtColor(mat1, mat2, Imgproc.COLOR_RGB2GRAY)
            Utils.matToBitmap(mat2, grayBitmap)
            binding.grayImage.setImageBitmap(grayBitmap)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode == 111 && resultCode == RESULT_OK && data != null) {
            val uri = data.data
            bitmap = MediaStore.Images.Media.getBitmap(contentResolver, uri)
            binding.grayImage.setImageBitmap(bitmap)
        }
    }
}