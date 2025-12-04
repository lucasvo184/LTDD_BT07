package com.example.a22110180_bt07

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.example.a22110180_bt07.socket.SocketManager
import org.json.JSONObject

class ProfileActivity : AppCompatActivity() {
    private lateinit var imgProfile: ImageView
    private lateinit var btnBack: ImageView
    private lateinit var btnEdit: ImageView
    private lateinit var btnLogout: Button
    private lateinit var tvId: TextView
    private lateinit var tvUsername: TextView
    private lateinit var tvFullName: TextView
    private lateinit var tvEmail: TextView
    private lateinit var tvGender: TextView

    // Sample data - replace with actual user data
    private var userId: Int = 3
    private var username: String = "trung1"
    private var fullName: String = "Nguyễn Hữu Trung"
    private var email: String = "trung2@gmail.com"
    private var gender: String = "Male"
    private var imageUrl: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        initViews()
        setupClickListeners()
        loadProfileData()
        setupSocket()
    }

    private fun setupSocket() {
        // Initialize and connect socket
        SocketManager.initialize()
        SocketManager.connect()
        
        // Subscribe to user updates
        SocketManager.subscribeToUser(userId)
        
        // Listen for profile updates
        SocketManager.onProfileUpdate { data ->
            runOnUiThread {
                try {
                    if (data.has("fullname")) {
                        fullName = data.getString("fullname")
                    }
                    if (data.has("email")) {
                        email = data.getString("email")
                    }
                    if (data.has("gender")) {
                        gender = data.getString("gender")
                    }
                    loadProfileData()
                    Log.d("ProfileActivity", "Profile updated via socket")
                } catch (e: Exception) {
                    Log.e("ProfileActivity", "Error parsing profile update", e)
                }
            }
        }
        
        // Listen for image updates
        SocketManager.onImageUpdate { data ->
            runOnUiThread {
                try {
                    if (data.has("imageUrl")) {
                        imageUrl = data.getString("imageUrl")
                        Glide.with(this)
                            .load(imageUrl)
                            .placeholder(R.drawable.ic_launcher_foreground)
                            .transform(CircleCrop())
                            .into(imgProfile)
                        Log.d("ProfileActivity", "Image updated via socket")
                    }
                } catch (e: Exception) {
                    Log.e("ProfileActivity", "Error parsing image update", e)
                }
            }
        }
    }

    private fun initViews() {
        imgProfile = findViewById(R.id.imgProfile)
        btnBack = findViewById(R.id.btnBack)
        btnEdit = findViewById(R.id.btnEdit)
        btnLogout = findViewById(R.id.btnLogout)
        tvId = findViewById(R.id.tvId)
        tvUsername = findViewById(R.id.tvUsername)
        tvFullName = findViewById(R.id.tvFullName)
        tvEmail = findViewById(R.id.tvEmail)
        tvGender = findViewById(R.id.tvGender)
    }

    private fun setupClickListeners() {
        btnBack.setOnClickListener {
            finish()
        }

        btnEdit.setOnClickListener {
            val intent = Intent(this, EditProfileActivity::class.java)
            intent.putExtra("userId", userId)
            intent.putExtra("username", username)
            intent.putExtra("fullName", fullName)
            intent.putExtra("email", email)
            intent.putExtra("gender", gender)
            startActivityForResult(intent, REQUEST_CODE_EDIT_PROFILE)
        }

        imgProfile.setOnClickListener {
            val intent = Intent(this, UploadImagesActivity::class.java)
            intent.putExtra("userId", userId)
            startActivityForResult(intent, REQUEST_CODE_UPLOAD_IMAGE)
        }

        btnLogout.setOnClickListener {
            // Handle logout
            finish()
        }
    }

    private fun loadProfileData() {
        tvId.text = userId.toString()
        tvUsername.text = username
        tvFullName.text = fullName
        tvEmail.text = email
        tvGender.text = gender

        // Load profile image if available
        imageUrl?.let {
            Glide.with(this)
                .load(it)
                .placeholder(R.drawable.ic_launcher_foreground)
                .transform(CircleCrop())
                .into(imgProfile)
        } ?: run {
            // Set default circular placeholder
            Glide.with(this)
                .load(R.drawable.ic_launcher_foreground)
                .transform(CircleCrop())
                .into(imgProfile)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_UPLOAD_IMAGE && resultCode == RESULT_OK) {
            // Reload profile image if upload was successful
            val uploadedImageUrl = data?.getStringExtra("imageUrl")
            uploadedImageUrl?.let {
                imageUrl = it
                Glide.with(this)
                    .load(it)
                    .placeholder(R.drawable.ic_launcher_foreground)
                    .transform(CircleCrop())
                    .into(imgProfile)
            }
        } else if (requestCode == REQUEST_CODE_EDIT_PROFILE && resultCode == RESULT_OK) {
            // Reload profile data if update was successful
            data?.let {
                fullName = it.getStringExtra("fullName") ?: fullName
                email = it.getStringExtra("email") ?: email
                gender = it.getStringExtra("gender") ?: gender
                loadProfileData()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        // Unsubscribe and disconnect socket
        SocketManager.unsubscribeFromUser(userId)
        SocketManager.disconnect()
    }

    companion object {
        const val REQUEST_CODE_UPLOAD_IMAGE = 1001
        const val REQUEST_CODE_EDIT_PROFILE = 1002
    }
}

