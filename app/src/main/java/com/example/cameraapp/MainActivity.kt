package com.example.cameraapp

import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.SeekBar
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import java.io.File
import java.io.IOException


private var MainActivity.originalBitmap: Bitmap
private val MainActivity.currentPhotoUri: Any

class MainActivity : AppCompatActivity() {

    private lateinit var imageView: ImageView
    private lateinit var btnCamera: Button
    private lateinit var btnGallery: Button
    private lateinit var btnFlip: Button
    private lateinit var btnSave: Button
    private lateinit var seekBarBrightness: SeekBar
    private lateinit var seekBarContrast: SeekBar
    private lateinit var binding: ActivityMainBinding

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (permissions[android.Manifest.permission.CAMERA] == true ||
            permissions[android.Manifest.permission.READ_EXTERNAL_STORAGE] == true ||
            permissions[android.Manifest.permission.WRITE_EXTERNAL_STORAGE] == true
        ) {
            Toast.makeText(this, "Permissões garantidas!", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(
                this,
                "Permissões são necessárias para o funcionamento do aplicativo.",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    private val takePictureLauncher = registerForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            currentPhotoUri?.let { uri ->
                val bitmap = BitmapFactory.decodeStream(contentResolver.openInputStream(uri))
                originalBitmap = bitmap
                imageView.setImageBitmap(originalBitmap)
                resetSlidersAndFlip()
            }
        }
    }
    private val pickImageLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            val bitmap = BitmapFactory.decodeStream(contentResolver.openInputStream(it))
            originalBitmap = bitmap
            imageView.setImageBitmap(originalBitmap)
            resetSlidersAndFlip()
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        imageView = binding.imageView
        btnCamera = binding.btnCamera
        btnGallery = binding.btnGallery
        btnFlip = binding.btnFlip
        btnSave = binding.btnSave
        seekBarBrightness = binding.seekBarBrightness
        seekBarContrast = binding.seekBarContrast

        checkPermissions()

        btnCamera.setOnClickListener { startCameraIntent() }
        btnGallery.setOnClickListener { startGalleryIntent() }
        btnFlip.setOnClickListener { flipImage() }
        btnSave.setOnClickListener { saveEditedImage() }

        setupSeekBarListeners()
    }
    private fun checkPermissions() {
        val permissions = arrayOf(
            android.Manifest.permission.CAMERA,
            android.Manifest.permission.READ_EXTERNAL_STORAGE,
            android.Manifest.permission.WRITE_EXTERNAL_STORAGE
        )

        val permissionsToRequest = permissions.filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }

        if (permissionsToRequest.isNotEmpty()) {
            requestPermissionLauncher.launch(permissionsToRequest.toTypedArray())
        }
    }
    private fun startCameraIntent() {
        val photoFile: File? = try {
            createImageFile()
        } catch (ex: IOException) {
            Log.e(TAG, "Erro ao criar arquivo de imagem", ex)
            null
        }

        photoFile?.also {
            val photoUri = FileProvider.getUriForFile(
                this,
                "com.example.photogram.fileprovider",
                it
            )
            currentPhotoUri = photoUri
            takePictureLauncher.launch(photoUri)
        }
    }
}