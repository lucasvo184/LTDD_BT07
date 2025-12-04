package com.example.a22110180_bt07.socket

import android.util.Log
import com.example.a22110180_bt07.Config
import io.socket.client.IO
import io.socket.client.Socket
import org.json.JSONObject
import java.net.URISyntaxException

object SocketManager {
    private const val TAG = "SocketManager"
    private val SOCKET_URL = Config.SOCKET_URL // Thay đổi theo API của sinh viên
    
    private var socket: Socket? = null
    private var isConnected: Boolean = false

    fun initialize(): Socket? {
        if (socket == null || !socket!!.connected()) {
            try {
                val opts = IO.Options().apply {
                    reconnection = true
                    reconnectionAttempts = 5
                    reconnectionDelay = 1000
                    reconnectionDelayMax = 5000
                    timeout = 20000
                }
                
                socket = IO.socket(SOCKET_URL, opts)
                
                socket!!.on(Socket.EVENT_CONNECT) {
                    isConnected = true
                    Log.d(TAG, "Socket connected")
                }
                
                socket!!.on(Socket.EVENT_DISCONNECT) {
                    isConnected = false
                    Log.d(TAG, "Socket disconnected")
                }
                
                socket!!.on(Socket.EVENT_CONNECT_ERROR) { args ->
                    isConnected = false
                    Log.e(TAG, "Socket connection error: ${args[0]}")
                }
                
                // Listen for reconnection (using reconnect event string)
                socket!!.on("reconnect") { args ->
                    isConnected = true
                    Log.d(TAG, "Socket reconnected: attempt ${args.getOrNull(0)}")
                }
                
            } catch (e: URISyntaxException) {
                Log.e(TAG, "Invalid socket URL: $SOCKET_URL", e)
            }
        }
        return socket
    }

    fun connect() {
        socket?.connect()
    }

    fun disconnect() {
        socket?.disconnect()
        socket = null
        isConnected = false
    }

    fun isConnected(): Boolean {
        return socket?.connected() == true && isConnected
    }

    fun on(event: String, callback: (Array<out Any>) -> Unit) {
        socket?.on(event) { args ->
            callback(args)
        }
    }

    fun off(event: String) {
        socket?.off(event)
    }

    fun emit(event: String, vararg args: Any) {
        if (isConnected()) {
            socket?.emit(event, *args)
        } else {
            Log.w(TAG, "Cannot emit: Socket not connected")
        }
    }

    fun joinRoom(roomId: String) {
        emit("join_room", roomId)
    }

    fun leaveRoom(roomId: String) {
        emit("leave_room", roomId)
    }

    // Profile update events
    fun onProfileUpdate(callback: (JSONObject) -> Unit) {
        on("profile_updated") { args ->
            if (args.isNotEmpty() && args[0] is JSONObject) {
                callback(args[0] as JSONObject)
            }
        }
    }

    fun onImageUpdate(callback: (JSONObject) -> Unit) {
        on("image_updated") { args ->
            if (args.isNotEmpty() && args[0] is JSONObject) {
                callback(args[0] as JSONObject)
            }
        }
    }

    fun subscribeToUser(userId: Int) {
        emit("subscribe_user", userId)
    }

    fun unsubscribeFromUser(userId: Int) {
        emit("unsubscribe_user", userId)
    }
}

