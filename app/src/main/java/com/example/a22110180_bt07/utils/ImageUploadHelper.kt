package com.example.a22110180_bt07.utils

import android.content.Context
import android.net.Uri
import android.util.Log
import com.cloudinary.android.MediaManager
import com.cloudinary.android.callback.ErrorInfo
import com.cloudinary.android.callback.UploadCallback
import com.example.a22110180_bt07.RealPathUtil
import java.io.File

object ImageUploadHelper {
    private const val TAG = "ImageUploadHelper"
    private var useCloudinary = false

    fun initializeCloudinary(context: Context, cloudName: String, apiKey: String, apiSecret: String) {
        try {
            val config = HashMap<String, String>()
            config["cloud_name"] = cloudName
            config["api_key"] = apiKey
            config["api_secret"] = apiSecret
            
            MediaManager.init(context, config)
            useCloudinary = true
            Log.d(TAG, "Cloudinary initialized")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to initialize Cloudinary", e)
            useCloudinary = false
        }
    }

    fun uploadImage(
        context: Context,
        imageUri: Uri,
        userId: Int,
        onSuccess: (String) -> Unit,
        onError: (String) -> Unit
    ) {
        if (useCloudinary) {
            try {
                uploadToCloudinary(context, imageUri, userId, onSuccess, onError)
            } catch (e: Exception) {
                Log.e(TAG, "Cloudinary upload failed", e)
                onError("Cloudinary upload failed: ${e.message}")
            }
        } else {
            // Use server upload (handled by UploadImagesActivity)
            onError("Cloudinary not initialized, use server upload instead")
        }
    }

    private fun uploadToCloudinary(
        context: Context,
        imageUri: Uri,
        userId: Int,
        onSuccess: (String) -> Unit,
        onError: (String) -> Unit
    ) {
        val imagePath = RealPathUtil.getRealPath(context, imageUri)
        if (imagePath == null) {
            onError("Không thể lấy đường dẫn ảnh")
            return
        }

        val file = File(imagePath)
        if (!file.exists()) {
            onError("File không tồn tại")
            return
        }

        val requestId = MediaManager.get().upload(imageUri)
            .option("folder", "profiles")
            .option("public_id", "user_${userId}_${System.currentTimeMillis()}")
            .option("transformation", "w_500,h_500,c_fill,g_face")
            .callback(object : UploadCallback {
                override fun onStart(requestId: String) {
                    Log.d(TAG, "Upload started: $requestId")
                }

                override fun onProgress(requestId: String, bytes: Long, totalBytes: Long) {
                    val progress = (bytes * 100 / totalBytes).toInt()
                    Log.d(TAG, "Upload progress: $progress%")
                }

                override fun onSuccess(requestId: String, resultData: Map<Any?, Any?>) {
                    val secureUrl = resultData["secure_url"] as? String
                    if (secureUrl != null) {
                        Log.d(TAG, "Upload success: $secureUrl")
                        onSuccess(secureUrl)
                    } else {
                        onError("Không nhận được URL từ Cloudinary")
                    }
                }

                override fun onError(requestId: String, error: ErrorInfo) {
                    Log.e(TAG, "Upload error: ${error.description}")
                    onError(error.description ?: "Lỗi upload")
                }

                override fun onReschedule(requestId: String, error: ErrorInfo) {
                    Log.w(TAG, "Upload rescheduled: ${error.description}")
                }
            })
            .dispatch()
    }

    fun isCloudinaryAvailable(): Boolean {
        return useCloudinary
    }
}

