package com.example.opencv

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.opencv.databinding.ActivityHsvColorBinding
import org.opencv.android.CameraBridgeViewBase
import org.opencv.core.CvType
import org.opencv.core.Mat
import org.opencv.imgproc.Imgproc

class HSVColorActivity : AppCompatActivity(), CameraBridgeViewBase.CvCameraViewListener2 {
    private lateinit var binding: ActivityHsvColorBinding
    private lateinit var mat: Mat
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHsvColorBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.cameraView.visibility = CameraBridgeViewBase.VISIBLE
        binding.cameraView.setCvCameraViewListener(this)
        binding.cameraView.setCameraIndex(0)
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)== PackageManager.PERMISSION_DENIED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), 111)
        }else{
            binding.cameraView.setCameraPermissionGranted()
            binding.cameraView.enableView()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        if(requestCode == 111) {
            if (grantResults.isNotEmpty()
                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                binding.cameraView.setCameraPermissionGranted()
                binding.cameraView.enableView()
            } else {
                Toast.makeText(this, "Camera permission required.", Toast.LENGTH_LONG).show()
                this.finish()
            }
        } else{
            super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        }
    }

    override fun onResume() {
        super.onResume()
        binding.cameraView.enableView()
    }

    override fun onCameraViewStarted(width: Int, height: Int) {
        mat = Mat(width, height, CvType.CV_16UC4)
    }

    override fun onCameraViewStopped() {
        mat.release()
    }

    override fun onCameraFrame(inputFrame: CameraBridgeViewBase.CvCameraViewFrame?): Mat {
        Imgproc.cvtColor(inputFrame!!.rgba(), mat, Imgproc.COLOR_RGB2HSV_FULL)
        return mat
    }

    override fun onPause() {
        super.onPause()
        binding.cameraView.disableView()
    }
}