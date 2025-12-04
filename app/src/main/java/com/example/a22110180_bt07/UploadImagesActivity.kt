package com.example.a22110180_bt07

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.example.a22110180_bt07.api.ApiService
import com.example.a22110180_bt07.api.RetrofitClient
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File

class UploadImagesActivity : AppCompatActivity() {
    private lateinit var imgPreview: ImageView
    private lateinit var btnBack: ImageView
    private lateinit var btnChooseFile: Button
    private lateinit var btnUploadImages: Button
    private lateinit var progressBar: ProgressBar

    private var selectedImageUri: Uri? = null
    private var selectedImagePath: String? = null
    private var userId: Int = 0
    private lateinit var apiService: ApiService

    companion object {
        private const val REQUEST_CODE_PICK_IMAGE = 1000
        private const val REQUEST_CODE_PERMISSION = 1001
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_upload_images)

        userId = intent.getIntExtra("userId", 0)
        apiService = RetrofitClient.apiService

        initViews()
        setupClickListeners()
    }

    private fun initViews() {
        imgPreview = findViewById(R.id.imgPreview)
        btnBack = findViewById(R.id.btnBack)
        btnChooseFile = findViewById(R.id.btnChooseFile)
        btnUploadImages = findViewById(R.id.btnUploadImages)
        progressBar = findViewById(R.id.progressBar)
    }

    private fun setupClickListeners() {
        btnBack.setOnClickListener {
            finish()
        }

        btnChooseFile.setOnClickListener {
            if (checkPermissions()) {
                openImagePicker()
            } else {
                requestPermissions()
            }
        }

        btnUploadImages.setOnClickListener {
            if (selectedImagePath != null) {
                uploadImage()
            } else {
                Toast.makeText(this, "Vui lòng chọn ảnh trước", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun checkPermissions(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_MEDIA_IMAGES
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED
        }
    }

    private fun requestPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.READ_MEDIA_IMAGES),
                REQUEST_CODE_PERMISSION
            )
        } else {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                ),
                REQUEST_CODE_PERMISSION
            )
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE_PERMISSION) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openImagePicker()
            } else {
                Toast.makeText(this, "Cần quyền truy cập ảnh để tiếp tục", Toast.LENGTH_SHORT)
                    .show()
            }
        }
    }

    private fun openImagePicker() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent, REQUEST_CODE_PICK_IMAGE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_PICK_IMAGE && resultCode == Activity.RESULT_OK) {
            data?.data?.let { uri ->
                selectedImageUri = uri
                selectedImagePath = RealPathUtil.getRealPath(this, uri)
                
                // Display selected image
                Glide.with(this)
                    .load(uri)
                    .transform(CircleCrop())
                    .into(imgPreview)
            }
        }
    }

    private fun uploadImage() {
        if (selectedImagePath == null) {
            Toast.makeText(this, "Vui lòng chọn ảnh", Toast.LENGTH_SHORT).show()
            return
        }

        val file = File(selectedImagePath!!)
        if (!file.exists()) {
            Toast.makeText(this, "File không tồn tại", Toast.LENGTH_SHORT).show()
            return
        }

        progressBar.visibility = ProgressBar.VISIBLE
        btnUploadImages.isEnabled = false

        // Create request body for user ID
        val idRequestBody = userId.toString().toRequestBody("text/plain".toMediaTypeOrNull())

        // Create multipart body for image
        val requestFile = file.asRequestBody("image/*".toMediaTypeOrNull())
        val imagePart = MultipartBody.Part.createFormData("image", file.name, requestFile)

        // Call API
        val call = apiService.uploadImage(idRequestBody, imagePart)
        call.enqueue(object : Callback<com.example.a22110180_bt07.api.UploadResponse> {
            override fun onResponse(
                call: Call<com.example.a22110180_bt07.api.UploadResponse>,
                response: Response<com.example.a22110180_bt07.api.UploadResponse>
            ) {
                progressBar.visibility = ProgressBar.GONE
                btnUploadImages.isEnabled = true

                if (response.isSuccessful) {
                    val uploadResponse = response.body()
                    if (uploadResponse?.success == true) {
                        Toast.makeText(
                            this@UploadImagesActivity,
                            "Upload thành công!",
                            Toast.LENGTH_SHORT
                        ).show()

                        // Return result to ProfileActivity
                        val resultIntent = Intent()
                        resultIntent.putExtra("imageUrl", uploadResponse.imageUrl)
                        setResult(Activity.RESULT_OK, resultIntent)
                        finish()
                    } else {
                        Toast.makeText(
                            this@UploadImagesActivity,
                            uploadResponse?.message ?: "Upload thất bại",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                } else {
                    Toast.makeText(
                        this@UploadImagesActivity,
                        "Lỗi: ${response.code()}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            override fun onFailure(
                call: Call<com.example.a22110180_bt07.api.UploadResponse>,
                t: Throwable
            ) {
                progressBar.visibility = ProgressBar.GONE
                btnUploadImages.isEnabled = true
                Toast.makeText(
                    this@UploadImagesActivity,
                    "Lỗi kết nối: ${t.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        })
    }
}

