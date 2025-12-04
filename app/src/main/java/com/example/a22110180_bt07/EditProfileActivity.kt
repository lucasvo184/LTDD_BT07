package com.example.a22110180_bt07

import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.a22110180_bt07.api.ApiService
import com.example.a22110180_bt07.api.RetrofitClient
import com.google.android.material.textfield.TextInputEditText
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class EditProfileActivity : AppCompatActivity() {
    private lateinit var btnBack: ImageView
    private lateinit var btnSave: Button
    private lateinit var etId: TextInputEditText
    private lateinit var etUsername: TextInputEditText
    private lateinit var etFullName: TextInputEditText
    private lateinit var etEmail: TextInputEditText
    private lateinit var spinnerGender: Spinner
    private lateinit var progressBar: ProgressBar

    private lateinit var apiService: ApiService
    private var userId: Int = 0
    private var username: String = ""
    private var fullName: String = ""
    private var email: String = ""
    private var gender: String = ""

    private val genderOptions = arrayOf("Male", "Female", "Other")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_profile)

        apiService = RetrofitClient.apiService

        // Get data from intent
        userId = intent.getIntExtra("userId", 0)
        username = intent.getStringExtra("username") ?: ""
        fullName = intent.getStringExtra("fullName") ?: ""
        email = intent.getStringExtra("email") ?: ""
        gender = intent.getStringExtra("gender") ?: "Male"

        initViews()
        setupClickListeners()
        loadData()
    }

    private fun initViews() {
        btnBack = findViewById(R.id.btnBack)
        btnSave = findViewById(R.id.btnSave)
        etId = findViewById(R.id.etId)
        etUsername = findViewById(R.id.etUsername)
        etFullName = findViewById(R.id.etFullName)
        etEmail = findViewById(R.id.etEmail)
        spinnerGender = findViewById(R.id.spinnerGender)
        progressBar = findViewById(R.id.progressBar)
    }

    private fun setupClickListeners() {
        btnBack.setOnClickListener {
            finish()
        }

        btnSave.setOnClickListener {
            if (validateInput()) {
                updateProfile()
            }
        }
    }

    private fun loadData() {
        etId.setText(userId.toString())
        etUsername.setText(username)
        etFullName.setText(fullName)
        etEmail.setText(email)

        // Setup gender spinner
        val adapter = android.widget.ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            genderOptions
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerGender.adapter = adapter

        // Set selected gender
        val genderIndex = genderOptions.indexOf(gender)
        if (genderIndex >= 0) {
            spinnerGender.setSelection(genderIndex)
        }
    }

    private fun validateInput(): Boolean {
        val fullNameText = etFullName.text.toString().trim()
        val emailText = etEmail.text.toString().trim()

        if (fullNameText.isEmpty()) {
            etFullName.error = "Vui lòng nhập họ tên"
            return false
        }

        if (emailText.isEmpty()) {
            etEmail.error = "Vui lòng nhập email"
            return false
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(emailText).matches()) {
            etEmail.error = "Email không hợp lệ"
            return false
        }

        return true
    }

    private fun updateProfile() {
        val fullNameText = etFullName.text.toString().trim()
        val emailText = etEmail.text.toString().trim()
        val selectedGender = spinnerGender.selectedItem.toString()

        progressBar.visibility = ProgressBar.VISIBLE
        btnSave.isEnabled = false

        val call = apiService.updateProfile(
            userId = userId,
            fullName = fullNameText,
            email = emailText,
            gender = selectedGender
        )

        call.enqueue(object : Callback<com.example.a22110180_bt07.api.ProfileUpdateResponse> {
            override fun onResponse(
                call: Call<com.example.a22110180_bt07.api.ProfileUpdateResponse>,
                response: Response<com.example.a22110180_bt07.api.ProfileUpdateResponse>
            ) {
                progressBar.visibility = ProgressBar.GONE
                btnSave.isEnabled = true

                if (response.isSuccessful) {
                    val updateResponse = response.body()
                    if (updateResponse?.success == true) {
                        Toast.makeText(
                            this@EditProfileActivity,
                            "Cập nhật thành công!",
                            Toast.LENGTH_SHORT
                        ).show()

                        // Return updated data
                        val resultIntent = android.content.Intent()
                        resultIntent.putExtra("fullName", fullNameText)
                        resultIntent.putExtra("email", emailText)
                        resultIntent.putExtra("gender", selectedGender)
                        setResult(RESULT_OK, resultIntent)
                        finish()
                    } else {
                        Toast.makeText(
                            this@EditProfileActivity,
                            updateResponse?.message ?: "Cập nhật thất bại",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                } else {
                    Toast.makeText(
                        this@EditProfileActivity,
                        "Lỗi: ${response.code()}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            override fun onFailure(
                call: Call<com.example.a22110180_bt07.api.ProfileUpdateResponse>,
                t: Throwable
            ) {
                progressBar.visibility = ProgressBar.GONE
                btnSave.isEnabled = true
                Toast.makeText(
                    this@EditProfileActivity,
                    "Lỗi kết nối: ${t.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        })
    }
}

