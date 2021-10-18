package com.example.opencv

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.opencv.databinding.ActivityFaceDetectionBinding
import org.opencv.android.CameraBridgeViewBase
import org.opencv.core.*
import org.opencv.imgproc.Imgproc
import org.opencv.imgproc.Imgproc.rectangle
import org.opencv.objdetect.CascadeClassifier
import org.opencv.objdetect.Objdetect
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

class FaceDetectionActivity : AppCompatActivity(), CameraBridgeViewBase.CvCameraViewListener2 {
    private lateinit var binding: ActivityFaceDetectionBinding
    private lateinit var mat1: Mat
    private var cascadeClassifier: CascadeClassifier? = null
    private var cascadeClassifierEye: CascadeClassifier? = null
    private var isFront = 1
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFaceDetectionBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.cameraView.visibility = CameraBridgeViewBase.VISIBLE
        binding.cameraView.setCvCameraViewListener(this)
        binding.cameraView.setCameraIndex(isFront)
        try {
            val inputStream = resources.openRawResource(R.raw.haarcascade_frontalface_alt)
            val cascadeDir = getDir("cascade", Context.MODE_PRIVATE)
            val mCascadeFile = File(cascadeDir, "face_detector.xml")
            val os = FileOutputStream(mCascadeFile)
            val buffer = ByteArray(4096)
            var bytesRead: Int
            while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                os.write(buffer, 0, bytesRead)
            }
            inputStream.close()
            os.close()
            cascadeClassifier = CascadeClassifier(mCascadeFile.absolutePath)
            val iser = resources.openRawResource(R.raw.haarcascade_lefteye_2splits)
            val cascadeDirER = getDir("cascadeER", MODE_PRIVATE)
            val cascadeFileER = File(cascadeDirER, "haarcascade_eye_right.xml")
            val oser = FileOutputStream(cascadeFileER)
            val bufferER = ByteArray(4096)
            var bytesReadER: Int
            while (iser.read(bufferER).also { bytesReadER = it } != -1) {
                oser.write(bufferER, 0, bytesReadER)
            }
            iser.close()
            oser.close()
            cascadeClassifierEye = CascadeClassifier(cascadeFileER.absolutePath)
        } catch (e: IOException) {
            e.printStackTrace()
        }
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
        mat1 = cascadeRec(mat1)
        if(isFront == 1) {
            Core.rotate(mat1, mat1, Core.ROTATE_180)
            Core.flip(mat1, mat1,0)
        }
        return mat1
    }

    private fun cascadeRec(mat: Mat): Mat {
        Core.rotate(mat, mat, Core.ROTATE_90_COUNTERCLOCKWISE)
        val rgb = Mat()
        Imgproc.cvtColor(mat, rgb, Imgproc.COLOR_RGBA2RGB)
        val faces = MatOfRect()
        cascadeClassifier?.detectMultiScale(rgb, faces, 2.0, 1, Objdetect.CASCADE_FIND_BIGGEST_OBJECT or Objdetect.CASCADE_SCALE_IMAGE, Size(30.0, 30.0), Size(900.0, 900.0))
        for(face in faces.toArray()) {
            rectangle(mat, face.tl(),face.br(), Scalar(255.0,0.0,0.0),3)
            val cropped = Mat(rgb, face)
            val eyes = MatOfRect()
            cascadeClassifierEye?.detectMultiScale(cropped,eyes,2.0,2,Objdetect.CASCADE_FIND_BIGGEST_OBJECT or Objdetect.CASCADE_SCALE_IMAGE, Size(20.0, 10.0), Size(900.0, 900.0))
            for(eye in eyes.toArray()) {
                val x1 = eye.tl().x + face.tl().x
                val y1 = eye.tl().y + face.tl().y
                val w = eye.br().x - eye.tl().x
                val h = eye.br().y - eye.tl().y
                val x2 = w + x1
                val y2 = h + y1
                rectangle(mat, Point(x1,y1),Point(x2,y2), Scalar(255.0,0.0,255.0),3)
            }
        }
        Core.rotate(mat, mat, Core.ROTATE_90_CLOCKWISE)
        return mat
    }

    override fun onPause() {
        super.onPause()
        binding.cameraView.disableView()
    }
}