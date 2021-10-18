package com.example.opencv

import android.Manifest
import android.content.pm.PackageManager
import android.media.CamcorderProfile
import android.media.MediaRecorder
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.MotionEvent
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import com.example.opencv.databinding.ActivityCameraTestBinding
import org.opencv.android.CameraBridgeViewBase
import org.opencv.core.Core
import org.opencv.core.CvType
import org.opencv.core.Mat
import org.opencv.imgcodecs.Imgcodecs
import org.opencv.imgproc.Imgproc
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class CameraTestActivity : AppCompatActivity(), CameraBridgeViewBase.CvCameraViewListener2{
    private lateinit var binding: ActivityCameraTestBinding
    private lateinit var mat: Mat
    private var isFront = 1
    private var isCaptureImage = 0
    private var isCaptureVideo = 0
    private var isImage = true
    private var height = 0
    private var width = 0
    private lateinit var videRecorder: MediaRecorder
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCameraTestBinding.inflate(layoutInflater)
        setContentView(binding.root)
        videRecorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            MediaRecorder(this)
        } else {
            MediaRecorder()
        }
        binding.cameraView.visibility = CameraBridgeViewBase.VISIBLE
        binding.cameraView.setCvCameraViewListener(this)
        binding.cameraView.setCameraIndex(isFront)
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)==PackageManager.PERMISSION_DENIED ||
            ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)==PackageManager.PERMISSION_DENIED ||
            ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)==PackageManager.PERMISSION_DENIED ||
            ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)==PackageManager.PERMISSION_DENIED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA,Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.RECORD_AUDIO), 111)
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
        binding.captureImage.setOnClickListener {
            if(isCaptureImage == 0) {
                binding.captureImage.setImageResource(R.drawable.ic_complete)
                isCaptureImage = 1
            } else {
                isCaptureImage = 0
                binding.captureImage.setImageResource(R.drawable.ic_capture_image)
            }
        }
        binding.captureVideo.setOnClickListener {
            if(isCaptureVideo == 0) {
                binding.captureVideo.setImageResource(R.drawable.ic_recording)
                videRecorder.setAudioSource(MediaRecorder.AudioSource.MIC)
                videRecorder.setVideoSource(MediaRecorder.VideoSource.SURFACE)
                videRecorder.setProfile(CamcorderProfile.get(CamcorderProfile.QUALITY_HIGH))
                val simpleDateFormat = SimpleDateFormat("yyyy-MM-dd_HH-mm-ss", Locale.getDefault())
                val fileName = simpleDateFormat.format(Date())
                val file = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
                val dir = File(file.path + "/opencv/video")
                if(!dir.exists()) {
                    dir.mkdirs()
                }
                videRecorder.setOutputFile(dir.path + "/" + fileName + ".mp4")
                videRecorder.setVideoSize(height,width)
                videRecorder.prepare()
                binding.cameraView.setRecorder(videRecorder)
                videRecorder.start()
                isCaptureVideo =1
            } else {
                binding.captureVideo.setImageResource(R.drawable.ic_capture_video)
                binding.cameraView.setRecorder(null)
                videRecorder.stop()
                try {
                    Thread.sleep(1000)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                isCaptureVideo =0
            }
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
        mat = Mat(width, height, CvType.CV_8UC4)
    }

    override fun onCameraViewStopped() {
        mat.release()
    }

    override fun onCameraFrame(inputFrame: CameraBridgeViewBase.CvCameraViewFrame?): Mat {
        mat = inputFrame!!.rgba()
        if(isFront == 1) {
            Core.rotate(mat, mat, Core.ROTATE_180)
            Core.flip(mat, mat,0)
        }
        takeImageAndSave(mat)
        height = mat.height()
        width = mat.width()
        return mat
    }

    private fun takeImageAndSave(mat: Mat) {
        if(isCaptureImage == 1) {
            val saveMat = Mat()
            Core.flip(mat.t(), saveMat, 1)
            val simpleDateFormat = SimpleDateFormat("yyyy-MM-dd_HH-mm-ss", Locale.getDefault())
            val displayName = simpleDateFormat.format(Date())
            val file = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
            val dir = File(file.path + "/opencv/images")
            if(!dir.exists()) {
                dir.mkdirs()
            }
            val fileName = dir.path + "/" + displayName +".jpg"
            Imgproc.cvtColor(saveMat, saveMat, Imgproc.COLOR_RGBA2BGRA)
            Imgcodecs.imwrite(fileName, saveMat)
            isCaptureImage = 0
            binding.captureImage.setImageResource(R.drawable.ic_capture_image)
        }
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        var x = 0.toFloat()
        if(event?.action == MotionEvent.ACTION_DOWN) {
            x = event.x
            Log.d(this::class.simpleName,x.toString())
        } else if(event?.action == MotionEvent.ACTION_UP) {
            val delta = -(x - event.x)
            if(delta < 100) {
                isImage = true
                binding.captureImage.isVisible = isImage
                binding.captureVideo.isVisible = !isImage
            } else if(delta > 0) {
                isImage = false
                binding.captureImage.isVisible = isImage
                binding.captureVideo.isVisible = !isImage
            }
        }
        return super.onTouchEvent(event)
    }

    override fun onPause() {
        super.onPause()
        binding.cameraView.disableView()
    }


}