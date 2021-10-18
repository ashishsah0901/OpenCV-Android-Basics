package com.example.opencv

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.opencv.databinding.ActivityLineDetectionBinding
import org.opencv.android.CameraBridgeViewBase
import org.opencv.core.*
import org.opencv.imgproc.Imgproc
import kotlin.math.cos
import kotlin.math.sin

class LineDetectionActivity : AppCompatActivity(), CameraBridgeViewBase.CvCameraViewListener2 {
    private lateinit var binding: ActivityLineDetectionBinding
    private lateinit var mat1: Mat
    private var isFront = 1
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLineDetectionBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.cameraView.visibility = CameraBridgeViewBase.VISIBLE
        binding.cameraView.setCvCameraViewListener(this)
        binding.cameraView.setCameraIndex(isFront)
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)== PackageManager.PERMISSION_DENIED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), 111)
        }else{
            binding.cameraView.setCameraPermissionGranted()
            binding.cameraView.enableView()
        }
        binding.flipCamera.setOnClickListener {
            isFront = if(isFront == 1) 0 else 1
            binding.cameraView.disableView()
            binding.cameraView.setCameraIndex(isFront)
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
        mat1 = Mat(width, height, CvType.CV_8UC4)
    }

    override fun onCameraViewStopped() {
        mat1.release()
    }

    override fun onCameraFrame(inputFrame: CameraBridgeViewBase.CvCameraViewFrame?): Mat {
        mat1 = inputFrame!!.rgba()
        if(isFront == 1) {
            Core.rotate(mat1, mat1, Core.ROTATE_180)
            Core.flip(mat1, mat1,0)
        }
        val edges = Mat()
        Imgproc.Canny(mat1, edges, 80.0,200.0)
        val lines = Mat()
        val p1 = Point()
        val p2 = Point()
        var a: Double
        var b: Double
        var x0: Double
        var y0: Double
        Imgproc.HoughLines(edges, lines, 1.0, Math.PI/100,140)
        for(i in 0 until lines.rows()) {
            val vec = lines.get(i,0)
            val rho = vec[0]
            val theta = vec[1]
            a = cos(theta)
            b = sin(theta)
            x0 = a * rho
            y0 = b * rho
            p1.x = (x0 + 1000 * (-b))
            p1.y = (y0 + 1000 * a)
            p2.x = (x0 - 1000 * (-b))
            p2.y = (y0 - 1000 * a)
            Imgproc.line(mat1,p1, p2, Scalar(255.0,0.0,255.0,0.0),1,Imgproc.LINE_AA,0)
        }
        return mat1
    }

    override fun onPause() {
        super.onPause()
        binding.cameraView.disableView()
    }
}