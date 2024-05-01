package com.roa.cswstickers.activities

import android.content.Intent
import android.os.Bundle
import android.widget.FrameLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.roa.cswstickers.R
import com.roa.cswstickers.utils.FileUtils
import com.roa.cswstickers.utils.RequestPermissionsHelper


class RequestPermissionActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_request_permission)
        FileUtils.initializeDirectories(this)
        startActivity(Intent(this, MainActivity::class.java))
        finish()

        if (RequestPermissionsHelper.verifyPermissions(this)) {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        } else {
            RequestPermissionsHelper.requestPermissions(this)
        }

        findViewById<FrameLayout>(R.id.grant_permissions_button_framelayout).setOnClickListener {
            RequestPermissionsHelper.requestPermissions(this)
        }


    }


    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        FileUtils.initializeDirectories(this)
        if (RequestPermissionsHelper.verifyPermissions(this)) {
            startActivity(Intent(this, MainActivity::class.java))
        } else {
            Toast.makeText(this, "We need access to write and read files in your phone", Toast.LENGTH_SHORT).show()
        }
    }


}
