package com.example.a22110180_bt07

/**
 * Configuration file for the application
 * Update these values according to your API and services
 */
object Config {
    // API Base URL
    const val API_BASE_URL = "http://app.iotstar.vn:8081/appfoods/"
    
    // Socket.IO URL
    const val SOCKET_URL = "http://app.iotstar.vn:8081"
    
    // Cloudinary Configuration (Optional)
    // Set these values if you want to use Cloudinary for image upload
    const val USE_CLOUDINARY = false
    const val CLOUDINARY_CLOUD_NAME = "your_cloud_name"
    const val CLOUDINARY_API_KEY = "your_api_key"
    const val CLOUDINARY_API_SECRET = "your_api_secret"
    
    /**
     * Initialize Cloudinary if enabled
     * Call this in Application class or MainActivity onCreate
     */
    fun initializeCloudinary(context: android.content.Context) {
        if (USE_CLOUDINARY) {
            com.example.a22110180_bt07.utils.ImageUploadHelper.initializeCloudinary(
                context,
                CLOUDINARY_CLOUD_NAME,
                CLOUDINARY_API_KEY,
                CLOUDINARY_API_SECRET
            )
        }
    }
}

