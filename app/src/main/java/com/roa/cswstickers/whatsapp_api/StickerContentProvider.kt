/*
 * Copyright (c) WhatsApp Inc. and its affiliates.
 * All rights reserved.
 *
 * This source code is licensed under the BSD-style license found in the
 * LICENSE file in the root directory of this source tree.
 */
package com.roa.cswstickers.whatsapp_api

import android.content.ContentProvider
import android.content.ContentResolver
import android.content.ContentValues
import android.content.Context
import android.content.UriMatcher
import android.database.Cursor
import android.database.MatrixCursor
import android.net.Uri
import android.os.ParcelFileDescriptor
import android.text.TextUtils
import android.util.Log
import com.google.gson.Gson
import com.roa.cswstickers.BuildConfig
import com.roa.cswstickers.utils.StickerPacksManager
import java.io.File
import java.io.FileNotFoundException
import java.util.Objects

class StickerContentProvider : ContentProvider() {
    private var stickerPackList: List<StickerPack>? = null
    override fun onCreate(): Boolean {


        val authority: String = BuildConfig.CONTENT_PROVIDER_AUTHORITY
        //check(authority.startsWith(Objects.requireNonNull(context)!!.packageName)) { "your authority (" + authority + ") for the content provider should start with your package name: " + context!!.packageName }
        check(Objects.requireNonNull(context)?.let { authority.startsWith(it.packageName) } == true) { "your authority (" + authority + ") for the content provider should start with your package name: " + context!!.packageName }



        //the call to get the metadata for the sticker packs.
        MATCHER.addURI(authority, METADATA, METADATA_CODE)

        //the call to get the metadata for single sticker pack. * represent the identifier
        MATCHER.addURI(authority, METADATA + "/*", METADATA_CODE_FOR_SINGLE_PACK)

        //gets the list of stickers for a sticker pack, * respresent the identifier.
        MATCHER.addURI(authority, STICKERS + "/*", STICKERS_CODE)


        for (stickerPack in getStickerPackList()!!) {
            val directoryPath = File(context!!.filesDir, "stickers_asset")
            val stickerPath = "$directoryPath/${stickerPack.identifier}"

            MATCHER.addURI(
                authority,
                STICKERS_ASSET + "/" + stickerPack.identifier + "/" + stickerPack.tray_image_file,
                STICKER_PACK_TRAY_ICON_CODE
            )
            for (sticker in stickerPack.getStickers()!!) {
                MATCHER.addURI(
                    authority,
                    STICKERS_ASSET + "/" + stickerPack.identifier + "/" + sticker.image_file,
                    STICKERS_ASSET_CODE
                )
            }
        }
        return true
    }

    override fun query(
        uri: Uri, projection: Array<String>?, selection: String?,
        selectionArgs: Array<String>?, sortOrder: String?
    ): Cursor {
        stickerPackList = StickerPacksManager.getStickerPacks(context!!)
        val code = MATCHER.match(uri)
        return when (code) {
            METADATA_CODE -> {
                getPackForAllStickerPacks(uri)
            }
            METADATA_CODE_FOR_SINGLE_PACK -> {
                getCursorForSingleStickerPack(uri)
            }
            STICKERS_CODE -> {
                getStickersForAStickerPack(uri)
            }
            else -> {
                throw IllegalArgumentException("Unknown URI: $uri")
            }
        }
    }

    override fun openFile(uri: Uri, mode: String): ParcelFileDescriptor? {


        val matchCode = MATCHER.match(uri)



        if (matchCode == STICKERS_ASSET_CODE || matchCode == STICKER_PACK_TRAY_ICON_CODE) {

            var parcelFileDescriptor: ParcelFileDescriptor? = null
            try {
                parcelFileDescriptor = ParcelFileDescriptor.open(
                    getImageAsset(uri),
                    ParcelFileDescriptor.MODE_READ_ONLY
                )
            } catch (e: FileNotFoundException) {
                e.printStackTrace()
            }
            return ParcelFileDescriptor(parcelFileDescriptor)
        }
        return null
    }

    override fun getType(uri: Uri): String {
        val matchCode = MATCHER.match(uri)
        return when (matchCode) {
            METADATA_CODE -> "vnd.android.cursor.dir/vnd." + BuildConfig.CONTENT_PROVIDER_AUTHORITY + "." + METADATA
            METADATA_CODE_FOR_SINGLE_PACK -> "vnd.android.cursor.item/vnd." + BuildConfig.CONTENT_PROVIDER_AUTHORITY + "." + METADATA
            STICKERS_CODE -> "vnd.android.cursor.dir/vnd." + BuildConfig.CONTENT_PROVIDER_AUTHORITY + "." + STICKERS
            STICKERS_ASSET_CODE -> "image/webp"
            STICKER_PACK_TRAY_ICON_CODE -> "image/png"
            else -> throw IllegalArgumentException("Unknown URI: $uri")
        }
    }

    @Synchronized
    private fun readContentFile(context: Context) {
        stickerPackList = StickerPacksManager.getStickerPacks(context)
    }

    fun getStickerPackList(): List<StickerPack>? {
        readContentFile(Objects.requireNonNull(context)!!)
        return stickerPackList
    }

    private fun getPackForAllStickerPacks(uri: Uri): Cursor {
        return getStickerPackInfo(uri, getStickerPackList()!!)
    }

    private fun getCursorForSingleStickerPack(uri: Uri): Cursor {
        val identifier = uri.lastPathSegment
        for (stickerPack in getStickerPackList()!!) {
            if (identifier == stickerPack.identifier) {
                return getStickerPackInfo(uri, listOf<StickerPack>(stickerPack))
            }
        }
        return getStickerPackInfo(uri, ArrayList<StickerPack>())
    }

    private fun getStickerPackInfo(uri: Uri, stickerPackList: List<StickerPack>): Cursor {
        val cursor = MatrixCursor(
            arrayOf(
                STICKER_PACK_IDENTIFIER_IN_QUERY,
                STICKER_PACK_NAME_IN_QUERY,
                STICKER_PACK_PUBLISHER_IN_QUERY,
                STICKER_PACK_ICON_IN_QUERY,
                ANDROID_APP_DOWNLOAD_LINK_IN_QUERY,
                IOS_APP_DOWNLOAD_LINK_IN_QUERY,
                PUBLISHER_EMAIL,
                PUBLISHER_WEBSITE,
                PRIVACY_POLICY_WEBSITE,
                LICENSE_AGREENMENT_WEBSITE
            )
        )
        for (stickerPack in stickerPackList) {
            val builder = cursor.newRow()
            builder.add(stickerPack.identifier)
            builder.add(stickerPack.name)
            builder.add(stickerPack.publisher)
            builder.add(stickerPack.tray_image_file)
            builder.add(stickerPack.android_play_store_link)
            builder.add(stickerPack.ios_app_store_link)
            builder.add(stickerPack.publisher_email)
            builder.add(stickerPack.publisher_website)
            builder.add(stickerPack.privacy_policy_website)
            builder.add(stickerPack.license_agreement_website)
        }
        cursor.setNotificationUri(Objects.requireNonNull(context)!!.contentResolver, uri)
        return cursor
    }

    private fun getStickersForAStickerPack(uri: Uri): Cursor {
        val identifier = uri.lastPathSegment
        val cursor = MatrixCursor(arrayOf(STICKER_FILE_NAME_IN_QUERY, STICKER_FILE_EMOJI_IN_QUERY))
        for (stickerPack in getStickerPackList()!!) {
            if (identifier == stickerPack.identifier) {
                for (sticker in stickerPack.getStickers()!!) {
                    cursor.addRow(
                        arrayOf<Any>(
                            sticker.image_file ?: "", // Si sticker.imageFileName es nulo, se utilizará una cadena vacía ""
                            TextUtils.join(",", sticker.emojis ?: emptyList<String>()) // Si sticker.emojis es nulo, se utilizará una lista vacía
                        )
                    )
                }
            }
        }
        cursor.setNotificationUri(Objects.requireNonNull(context)!!.contentResolver, uri)
        return cursor
    }

    @Throws(IllegalArgumentException::class)
    private fun getImageAsset(uri: Uri): File? {

        val pathSegments = uri.pathSegments

        require(pathSegments.size == 3) { "path segments should be 3, uri is: $uri" }

        val fileName = pathSegments[pathSegments.size - 1]

        val identifier = pathSegments[pathSegments.size - 2]

        require(!TextUtils.isEmpty(identifier)) { "identifier is empty, uri: $uri" }

        require(!TextUtils.isEmpty(fileName)) { "file name is empty, uri: $uri" }

        //making sure the file that is trying to be fetched is in the list of stickers.
        for (stickerPack in getStickerPackList()!!) {

            if (identifier == stickerPack.identifier) {
                if (fileName == stickerPack.tray_image_file) {
                    return fetchFile(fileName, identifier)
                } else {
                    for (sticker in stickerPack.getStickers()!!) {
                        if (fileName == sticker.image_file) {
                            return fetchFile(sticker.image_file ?: "", identifier)
                        }
                    }
                }
            }
        }
        return null
    }

    private fun fetchFile(fileName: String, identifier: String): File {

        val directoryPath = File(context!!.filesDir, "stickers_asset" + "/" + identifier + "/" + fileName)


        //val stickerPath = "$directoryPath/$identifier}"

        return directoryPath
        //return File(Constants.STICKERS_DIRECTORY_PATH + identifier + "/" + fileName)
    }

    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<String>?): Int {
        throw UnsupportedOperationException("Not supported")
    }

    override fun insert(uri: Uri, values: ContentValues?): Uri {

        val authority: String = BuildConfig.CONTENT_PROVIDER_AUTHORITY
        check(authority.startsWith(Objects.requireNonNull(context)!!.packageName)) { "your authority (" + authority + ") for the content provider should start with your package name: " + context!!.packageName }
        val stickerPack: StickerPack = Gson().fromJson(
            values!!["stickerPack"] as String,
            StickerPack::class.java
        )


        val directoryPath = File(context!!.filesDir, "stickers_asset")
        val stickerPath = "$directoryPath/${stickerPack.identifier}"

        val stickertrayicon = stickerPack.tray_image_file




        /*
        MATCHER.addURI(
            authority,
            stickerPath + "/" + stickerPack.tray_image_file,
            STICKER_PACK_TRAY_ICON_CODE
        )

         */

        MATCHER.addURI(
            authority,
            STICKERS_ASSET + "/" + stickerPack.identifier + "/" + stickerPack.tray_image_file,
            STICKER_PACK_TRAY_ICON_CODE
        )

        for (sticker in stickerPack.getStickers()!!) {
            MATCHER.addURI(
                authority,
                STICKERS_ASSET + "/" + stickerPack.identifier + "/" + sticker.image_file,
                STICKERS_ASSET_CODE
            )
        }





        return uri
    }

    override fun update(
        uri: Uri, values: ContentValues?, selection: String?,
        selectionArgs: Array<String>?
    ): Int {
        throw UnsupportedOperationException("Not supported")
    }


    companion object {
        /**
         * Do not change the strings listed below, as these are used by WhatsApp. And changing these will break the interface between sticker app and WhatsApp.
         */
        const val STICKER_PACK_IDENTIFIER_IN_QUERY = "sticker_pack_identifier"
        const val STICKER_PACK_NAME_IN_QUERY = "sticker_pack_name"
        const val STICKER_PACK_PUBLISHER_IN_QUERY = "sticker_pack_publisher"
        const val STICKER_PACK_ICON_IN_QUERY = "sticker_pack_icon"
        const val ANDROID_APP_DOWNLOAD_LINK_IN_QUERY = "android_play_store_link"
        const val IOS_APP_DOWNLOAD_LINK_IN_QUERY = "ios_app_download_link"
        const val PUBLISHER_EMAIL = "sticker_pack_publisher_email"
        const val PUBLISHER_WEBSITE = "sticker_pack_publisher_website"
        const val PRIVACY_POLICY_WEBSITE = "sticker_pack_privacy_policy_website"
        const val LICENSE_AGREENMENT_WEBSITE = "sticker_pack_license_agreement_website"
        const val STICKER_FILE_NAME_IN_QUERY = "sticker_file_name"
        const val STICKER_FILE_EMOJI_IN_QUERY = "sticker_emoji"
        //const val CONTENT_FILE_NAME = "contents.json"
        const val METADATA = "metadata"
        var AUTHORITY_URI = Uri.Builder().scheme(ContentResolver.SCHEME_CONTENT)
            .authority(BuildConfig.CONTENT_PROVIDER_AUTHORITY).appendPath(
                METADATA
            ).build()

        /**
         * Do not change the values in the UriMatcher because otherwise, WhatsApp will not be able to fetch the stickers from the ContentProvider.
         */
        private val MATCHER = UriMatcher(UriMatcher.NO_MATCH)

        private const val METADATA_CODE = 1
        private const val METADATA_CODE_FOR_SINGLE_PACK = 2
        const val STICKERS = "stickers"
        private const val STICKERS_CODE = 3

        //
        //val directoryPath = File(this.filesDir, "stickersPack")
        //const val STICKERS_ASSET = "stickers_asset"
        const val STICKERS_ASSET = "stickers_asset"
        private const val STICKERS_ASSET_CODE = 4
        private const val STICKER_PACK_TRAY_ICON_CODE = 5
    }
}
