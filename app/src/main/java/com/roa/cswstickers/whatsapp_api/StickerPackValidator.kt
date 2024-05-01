/*
 * Copyright (c) WhatsApp Inc. and its affiliates.
 * All rights reserved.
 *
 * This source code is licensed under the BSD-style license found in the
 * LICENSE file in the root directory of this source tree.
 */
package com.roa.cswstickers.whatsapp_api

import android.content.Context
import android.graphics.BitmapFactory
import android.text.TextUtils
import android.util.Log
import android.util.Patterns
import android.webkit.URLUtil
import com.roa.cswstickers.utils.FileUtils
import com.roa.cswstickers.utils.ImageUtils
import java.io.IOException
import java.net.MalformedURLException
import java.net.URL
import java.util.Objects

object StickerPackValidator {
    private const val STICKER_FILE_SIZE_LIMIT_KB = 100
    private const val EMOJI_LIMIT = 3
    private const val IMAGE_HEIGHT = 512
    private const val IMAGE_WIDTH = 512
    private const val STICKER_SIZE_MIN = 3
    private const val STICKER_SIZE_MAX = 30
    private const val CHAR_COUNT_MAX = 128
    private const val ONE_KIBIBYTE = (8 * 1024).toLong()
    private const val TRAY_IMAGE_FILE_SIZE_MAX_KB = 50
    private const val TRAY_IMAGE_DIMENSION_MIN = 24
    private const val TRAY_IMAGE_DIMENSION_MAX = 512
    private const val PLAY_STORE_DOMAIN = "play.google.com"
    private const val APPLE_STORE_DOMAIN = "itunes.apple.com"

    /**
     * Checks whether a sticker pack contains valid data
     */
    @Throws(IllegalStateException::class)
    fun verifyStickerPackValidity(context: Context, stickerPack: StickerPack) {
        check(!TextUtils.isEmpty(stickerPack.identifier)) { "sticker pack identifier is empty" }
        check(!(stickerPack.identifier?.length!! > CHAR_COUNT_MAX)) { "sticker pack identifier cannot exceed " + CHAR_COUNT_MAX + " characters" }
        checkStringValidity(stickerPack.identifier)
        if (TextUtils.isEmpty(stickerPack.publisher)) {
            throw IllegalStateException("sticker pack publisher is empty, sticker pack identifier:" + stickerPack.identifier)
        }
        if (stickerPack.publisher?.length!! > CHAR_COUNT_MAX) {
            throw IllegalStateException("sticker pack publisher cannot exceed " + CHAR_COUNT_MAX + " characters, sticker pack identifier:" + stickerPack.identifier)
        }
        if (TextUtils.isEmpty(stickerPack.name)) {
            throw IllegalStateException("sticker pack name is empty, sticker pack identifier:" + stickerPack.identifier)
        }
        if (stickerPack.name?.length!! > CHAR_COUNT_MAX) {
            throw IllegalStateException("sticker pack name cannot exceed " + CHAR_COUNT_MAX + " characters, sticker pack identifier:" + stickerPack.identifier)
        }
        if (TextUtils.isEmpty(stickerPack.tray_image_file)) {
            throw IllegalStateException("sticker pack tray id is empty, sticker pack identifier:" + stickerPack.identifier)
        }
        if (!TextUtils.isEmpty(stickerPack.android_play_store_link) && !isValidWebsiteUrl(stickerPack.android_play_store_link.toString())) {
            throw IllegalStateException("Make sure to include http or https in url links, android play store link is not a valid url: " + stickerPack.android_play_store_link)
        }
        if (!TextUtils.isEmpty(stickerPack.android_play_store_link) && !isURLInCorrectDomain(
                stickerPack.android_play_store_link.toString(),
                PLAY_STORE_DOMAIN
            )
        ) {
            throw IllegalStateException("android play store link should use play store domain: " + PLAY_STORE_DOMAIN)
        }
        if (!TextUtils.isEmpty(stickerPack.ios_app_store_link) && !isValidWebsiteUrl(stickerPack.ios_app_store_link.toString())) {
            throw IllegalStateException("Make sure to include http or https in url links, ios app store link is not a valid url: " + stickerPack.ios_app_store_link)
        }
        if (!TextUtils.isEmpty(stickerPack.ios_app_store_link) && !isURLInCorrectDomain(
                stickerPack.ios_app_store_link.toString(),
                APPLE_STORE_DOMAIN
            )
        ) {
            throw IllegalStateException("iOS app store link should use app store domain: " + APPLE_STORE_DOMAIN)
        }
        if (!TextUtils.isEmpty(stickerPack.license_agreement_website) && !isValidWebsiteUrl(
                stickerPack.license_agreement_website.toString()
            )
        ) {
            throw IllegalStateException("Make sure to include http or https in url links, license agreement link is not a valid url: " + stickerPack.license_agreement_website)
        }
        if (!TextUtils.isEmpty(stickerPack.privacy_policy_website) && !isValidWebsiteUrl(stickerPack.privacy_policy_website.toString())) {
            throw IllegalStateException("Make sure to include http or https in url links, privacy policy link is not a valid url: " + stickerPack.privacy_policy_website)
        }
        if (!TextUtils.isEmpty(stickerPack.publisher_website) && !isValidWebsiteUrl(stickerPack.publisher_website.toString())) {
            throw IllegalStateException("Make sure to include http or https in url links, publisher website link is not a valid url: " + stickerPack.publisher_website)
        }
        if (!TextUtils.isEmpty(stickerPack.publisher_email) && !Patterns.EMAIL_ADDRESS.matcher(
                stickerPack.publisher_email.toString()
            ).matches()
        ) {
            throw IllegalStateException("publisher email does not seem valid, email is: " + stickerPack.publisher_email)
        }
        try {
            val iStream = context.contentResolver.openInputStream(
                ImageUtils.getStickerImageAsset(
                    stickerPack.identifier,
                    stickerPack.tray_image_file.toString()
                )
            )
            val bytes: ByteArray = FileUtils.getBytes(Objects.requireNonNull(iStream)!!)
            if (bytes.size > TRAY_IMAGE_FILE_SIZE_MAX_KB * ONE_KIBIBYTE) {
                throw IllegalStateException("tray image should be less than " + TRAY_IMAGE_FILE_SIZE_MAX_KB + " KB, tray image file: " + stickerPack.tray_image_file)
            }
            val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
            if (bitmap.height > TRAY_IMAGE_DIMENSION_MAX || bitmap.height < TRAY_IMAGE_DIMENSION_MIN) {
                throw IllegalStateException("tray image height should between " + TRAY_IMAGE_DIMENSION_MIN + " and " + TRAY_IMAGE_DIMENSION_MAX + " pixels, current tray image height is " + bitmap.height + ", tray image file: " + stickerPack.tray_image_file)
            }
            if (bitmap.width > TRAY_IMAGE_DIMENSION_MAX || bitmap.width < TRAY_IMAGE_DIMENSION_MIN) {
                throw IllegalStateException("tray image width should be between " + TRAY_IMAGE_DIMENSION_MIN + " and " + TRAY_IMAGE_DIMENSION_MAX + " pixels, current tray image width is " + bitmap.width + ", tray image file: " + stickerPack.tray_image_file)
            }
        } catch (e: IOException) {
            throw IllegalStateException("Cannot open tray image, " + stickerPack.tray_image_file, e)
        }
        val stickers: List<Sticker> = stickerPack.getStickers()!!
        if (stickers.size < STICKER_SIZE_MIN || stickers.size > STICKER_SIZE_MAX) {
            throw IllegalStateException("sticker pack sticker count should be between 3 to 30 inclusive, it currently has " + stickers.size + ", sticker pack identifier:" + stickerPack.identifier)
        }
        for (sticker in stickers) {
            validateSticker(context, stickerPack.identifier, sticker)
        }
    }

    @Throws(IllegalStateException::class)
    private fun validateSticker(context: Context, identifier: String, sticker: Sticker) {
        if (sticker.emojis?.size!! > EMOJI_LIMIT) {
            throw IllegalStateException("emoji count exceed limit, sticker pack identifier:" + identifier + ", filename:" + sticker.image_file)
        }
        if (TextUtils.isEmpty(sticker.image_file)) {
            throw IllegalStateException("no file path for sticker, sticker pack identifier:$identifier")
        }
        validateStickerFile(context, identifier, sticker.image_file.toString())
    }

    @Throws(IllegalStateException::class)
    private fun validateStickerFile(context: Context, identifier: String, fileName: String) {
        try {
            val iStream = context.contentResolver.openInputStream(
                ImageUtils.getStickerImageAsset(
                    identifier,
                    fileName
                )
            )
            val bytes: ByteArray = FileUtils.getBytes(Objects.requireNonNull(iStream)!!)
            if (bytes.size > STICKER_FILE_SIZE_LIMIT_KB * ONE_KIBIBYTE) {
                throw IllegalStateException("sticker should be less than " + STICKER_FILE_SIZE_LIMIT_KB + "KB, sticker pack identifier:" + identifier + ", filename:" + fileName)
            }

            // Load bytes into a Bitmap
            val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)

            // Validate the dimensions of the bitmap
            if (bitmap.height != IMAGE_HEIGHT || bitmap.width != IMAGE_WIDTH) {
                throw IllegalStateException("sticker dimensions should be $IMAGE_WIDTH x $IMAGE_HEIGHT, sticker pack identifier:$identifier, filename:$fileName")
            }

            // Optionally, check for animated images here

        } catch (e: IOException) {
            throw IllegalStateException(
                "cannot open sticker file: sticker pack identifier:$identifier, filename:$fileName",
                e
            )
        }
    }
    }

    private fun checkStringValidity(string: String) {
        val pattern = "[\\w-.,'\\s]+" // [a-zA-Z0-9_-.' ]
        if (!string.matches(pattern.toRegex())) {
            throw IllegalStateException("$string contains invalid characters, allowed characters are a to z, A to Z, _ , ' - . and space character")
        }
        if (string.contains("..")) {
            throw IllegalStateException("$string cannot contain ..")
        }
    }

    @Throws(IllegalStateException::class)
    private fun isValidWebsiteUrl(websiteUrl: String): Boolean {
        try {
            URL(websiteUrl)
        } catch (e: MalformedURLException) {
            Log.e("StickerPackValidator", "url: $websiteUrl is malformed")
            throw IllegalStateException("url: $websiteUrl is malformed", e)
        }
        return URLUtil.isHttpUrl(websiteUrl) || URLUtil.isHttpsUrl(websiteUrl)
    }

    @Throws(IllegalStateException::class)
    private fun isURLInCorrectDomain(urlString: String, domain: String): Boolean {
        try {
            val url = URL(urlString)
            if (domain == url.host) {
                return true
            }
        } catch (e: MalformedURLException) {
            Log.e("StickerPackValidator", "url: $urlString is malformed")
            throw IllegalStateException("url: $urlString is malformed")
        }
        return false
    }

