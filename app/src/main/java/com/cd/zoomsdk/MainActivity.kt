package com.cd.zoomsdk

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import us.zoom.sdk.*

class MainActivity : AppCompatActivity() {

    private val authListener = object : ZoomSDKAuthenticationListener {
        override fun onZoomSDKLoginResult(result: Long) {
            if (result.toInt() == ZoomAuthenticationError.ZOOM_AUTH_ERROR_SUCCESS) {
                startMeeting(this@MainActivity)
            } else {
                val builder = AlertDialog.Builder(this@MainActivity)
                builder.setTitle("Message")
                builder.setMessage("Login Failed!!")
                builder.setCancelable(true)
                val dialog = builder.create()
                dialog.show()
            }
        }
        override fun onZoomIdentityExpired() = Unit
        override fun onZoomSDKLogoutResult(p0: Long) = Unit
        override fun onZoomAuthIdentityExpired() = Unit
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        initializeSdk(this)
        initViews()
    }

    private fun initializeSdk(context: Context) {
        val sdk = ZoomSDK.getInstance()

        // Using hard-coded values for this key/secret only for test purpose !
        val params = ZoomSDKInitParams().apply {
            appKey = "glHNOrtN1uMWogQ7CI8gT5QBFSP3BmAxryGJ" // SDK key
            appSecret = "cB8gAGZHVunb9yKOiBctQW9mhKouMPKWtyNq" // SDK secret
            domain = "zoom.us"
            enableLog = true
        }

        val listener = object : ZoomSDKInitializeListener {
            override fun onZoomSDKInitializeResult(errorCode: Int, internalErrorCode: Int) = Unit
            override fun onZoomAuthIdentityExpired() = Unit
        }

        sdk.initialize(context, listener, params)
    }

    private fun initViews() {
        val join_button = findViewById<Button>(R.id.join_button)
        join_button.setOnClickListener {
            createJoinMeetingDialog()
        }

        val login_button = findViewById<Button>(R.id.login_button)
        login_button.setOnClickListener {
            if (ZoomSDK.getInstance().isLoggedIn) {
                startMeeting(this)
            } else {
                createLoginDialog()
            }
        }
    }


    private fun createJoinMeetingDialog() {
        AlertDialog.Builder(this)
            .setView(R.layout.activity_join)
            .setPositiveButton("Join") { dialog, _ ->
                dialog as AlertDialog

                val numberInput = dialog.findViewById<EditText>(R.id.meeting_number)
                val passwordInput = dialog.findViewById<EditText>(R.id.meeting_password)
                val nameInput = dialog.findViewById<EditText>(R.id.user_name)
                val meetingNumber = numberInput?.text?.toString()
                val password = passwordInput?.text?.toString()
                val name = nameInput?.text?.toString()

                if (meetingNumber!!.isNotEmpty() && password!!.isNotEmpty() && name!!.isNotEmpty() ) {
                    joinMeeting(this@MainActivity, meetingNumber, password, name)
                }
                dialog.dismiss()
            }.show()
    }

    private fun joinMeeting(context: Context, meetingNumber: String, pw: String, name: String) {

        val meetingService = ZoomSDK.getInstance().meetingService
        val options = JoinMeetingOptions()
        val params = JoinMeetingParams().apply {
            displayName = name
            meetingNo = meetingNumber
            password = pw
        }
        meetingService.joinMeetingWithParams(context, params, options)
    }


    private fun createLoginDialog() {
        AlertDialog.Builder(this)
            .setView(R.layout.activity_login)
            .setPositiveButton("Log in") { dialog, _ ->
                dialog as AlertDialog

                val emailInput = dialog.findViewById<EditText>(R.id.login_email)
                val passwordInput = dialog.findViewById<EditText>(R.id.login_password)
                val email = emailInput?.text?.toString()
                val password = passwordInput?.text?.toString()

                if (email!!.isNotEmpty() && password!!.isNotEmpty() ) {
                    login(email, password)
                }
                dialog.dismiss()
            }.show()
    }

    private fun login(username: String, password: String) {
        val result = ZoomSDK.getInstance().loginWithZoom(username, password)
        if (result == ZoomApiError.ZOOM_API_ERROR_SUCCESS) {
            //  listen for authentication result before starting a meeting
          ZoomSDK.getInstance().addAuthenticationListener(authListener)
        }
    }

    private fun startMeeting(context: Context) {
        val zoomSdk = ZoomSDK.getInstance()
        if (zoomSdk.isLoggedIn) {
            val meetingService = zoomSdk.meetingService
            val options = StartMeetingOptions()
            meetingService.startInstantMeeting(context, options)
        }
    }


}