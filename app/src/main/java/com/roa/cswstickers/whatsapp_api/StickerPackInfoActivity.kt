/*
 * Copyright (c) WhatsApp Inc. and its affiliates.
 * All rights reserved.
 *
 * This source code is licensed under the BSD-style license found in the
 * LICENSE file in the root directory of this source tree.
 */
package com.roa.cswstickers.whatsapp_api

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Rect
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.widget.TextView
import androidx.annotation.DrawableRes
import androidx.annotation.IdRes
import com.roa.cswstickers.R
import java.io.FileNotFoundException
import java.io.InputStream

class StickerPackInfoActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sticker_pack_info)
        val trayIconUriString: String =
            getIntent().getStringExtra(StickerPackDetailsActivity.EXTRA_STICKER_PACK_TRAY_ICON).toString()
        val website: String =
            getIntent().getStringExtra(StickerPackDetailsActivity.EXTRA_STICKER_PACK_WEBSITE).toString()
        val email: String =
            getIntent().getStringExtra(StickerPackDetailsActivity.EXTRA_STICKER_PACK_EMAIL).toString()
        val privacyPolicy: String =
            getIntent().getStringExtra(StickerPackDetailsActivity.EXTRA_STICKER_PACK_PRIVACY_POLICY)
                .toString()
        val trayIcon: TextView = findViewById(R.id.tray_icon)
        try {
            val inputStream: InputStream =
                getContentResolver().openInputStream(Uri.parse(trayIconUriString))!!
            val trayDrawable = BitmapDrawable(getResources(), inputStream)
            val emailDrawable = getDrawableForAllAPIs(R.drawable.sticker_3rdparty_email)
            trayDrawable.bounds =
                emailDrawable?.let { Rect(0, 0, it.intrinsicWidth, emailDrawable.intrinsicHeight) }!!
            trayIcon.setCompoundDrawablesRelative(trayDrawable, null, null, null)
        } catch (e: FileNotFoundException) {
            Log.e(
                TAG,
                "could not find the uri for the tray image:$trayIconUriString"
            )
        }
        setupTextView(website, R.id.view_webpage)
        val sendEmail: TextView = findViewById(R.id.send_email)
        if (TextUtils.isEmpty(email)) {
            sendEmail.visibility = View.GONE
        } else {
            sendEmail.setOnClickListener { v: View? ->
                launchEmailClient(
                    email
                )
            }
        }
        setupTextView(privacyPolicy, R.id.privacy_policy)
    }

    private fun setupTextView(website: String, @IdRes textViewResId: Int) {
        val viewWebpage: TextView = findViewById(textViewResId)
        if (TextUtils.isEmpty(website)) {
            viewWebpage.visibility = View.GONE
        } else {
            viewWebpage.setOnClickListener { v: View? ->
                launchWebpage(
                    website
                )
            }
        }
    }

    private fun launchEmailClient(email: String) {
        val emailIntent = Intent(
            Intent.ACTION_SENDTO, Uri.fromParts(
                "mailto", email, null
            )
        )
        emailIntent.putExtra(Intent.EXTRA_EMAIL, arrayOf(email))
        startActivity(
            Intent.createChooser(
                emailIntent,
                getResources().getString(R.string.info_send_email_to_prompt)
            )
        )
    }

    private fun launchWebpage(website: String) {
        val uri = Uri.parse(website)
        val intent = Intent(Intent.ACTION_VIEW, uri)
        startActivity(intent)
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    private fun getDrawableForAllAPIs(@DrawableRes id: Int) =
        getDrawable(id)


    companion object {
        private const val TAG = "StickerPackInfoActivity"
    }
}
