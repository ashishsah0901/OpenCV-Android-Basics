package com.example.opencv

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.opencv.databinding.ActivityMainBinding
import org.opencv.android.BaseLoaderCallback
import org.opencv.android.LoaderCallbackInterface
import org.opencv.android.OpenCVLoader

class MainActivity : AppCompatActivity(){
    private lateinit var binding: ActivityMainBinding
    private val baseLoaderCallback = object : BaseLoaderCallback(this) {
        override fun onManagerConnected(status: Int) {
            when(status) {
                LoaderCallbackInterface.SUCCESS -> {
                }
                else -> {
                    super.onManagerConnected(status)
                }
            }
        }
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.openTestCamera.setOnClickListener {
            startActivity(Intent(this, CameraTestActivity::class.java))
        }
        binding.openGrayCamera.setOnClickListener {
            startActivity(Intent(this, ImageToGrayActivity::class.java))
        }
        binding.openColorDetect.setOnClickListener {
            startActivity(Intent(this, HSVColorActivity::class.java))
        }
        binding.faceDetection.setOnClickListener {
            startActivity(Intent(this, FaceDetectionActivity::class.java))
        }
        binding.lineDetector.setOnClickListener {
            startActivity(Intent(this, LineDetectionActivity::class.java))
        }
    }
    override fun onResume() {
        super.onResume()
        if(OpenCVLoader.initDebug()) {
            baseLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS)
        }else{
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_4_0,this, baseLoaderCallback)
        }
    }
}