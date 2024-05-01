package com.roa.cswstickers.utils

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.util.Log
import com.google.gson.Gson
import com.roa.cswstickers.identities.StickerPacksContainer
import com.roa.cswstickers.whatsapp_api.ContentFileParser
import com.roa.cswstickers.whatsapp_api.Sticker
import com.roa.cswstickers.whatsapp_api.StickerPack
import java.io.*
import java.util.ArrayList

object StickerPacksManager {
    var stickerPacksContainer: StickerPacksContainer? = null

    fun saveStickerPackFilesLocally(identifier: String, stickersUries: List<Uri>, context: Context): List<Sticker> {

        val directoryPath = File(context.filesDir, "stickers_asset")
        val stickerPath = "$directoryPath/$identifier"


        val stickerList: MutableList<Sticker> = ArrayList()
        val directory = File(stickerPath)
        if (!directory.exists()) {
            directory.mkdir()
        }
        for (uri in stickersUries) {
            val firstFiveChar = identifier.substring(0, minOf(identifier.length, 5))
            val stickerName: String = firstFiveChar + FileUtils.generateRandomIdentifier()

            val sticker = Sticker("$stickerName.webp", null)
            //val sticker = Sticker(FileUtils.generateRandomIdentifier() + ".webp", null)
            stickerList.add(sticker)
            saveStickerFilesLocally(sticker, uri, stickerPath, context)
        }
        return stickerList
    }

    private fun saveStickerFilesLocally(sticker: Sticker, stickerUri: Uri, stickerPath: String, context: Context) {
        if (Build.VERSION.SDK_INT < 30) {
            createStickerImageFile(stickerUri, Uri.parse("$stickerPath/${sticker.image_file}"), context, Bitmap.CompressFormat.WEBP)
        } else {
            createStickerImageFile(stickerUri, Uri.parse("$stickerPath/${sticker.image_file}"), context, Bitmap.CompressFormat.WEBP_LOSSY)
        }

        //createStickerImageFile(stickerUri, Uri.parse("$stickerPath/${sticker.image_file}"), context, Bitmap.CompressFormat.WEBP_LOSSLESS)
    }
    fun getStickerPacks(context: Context): List<StickerPack> {
        var stickerPackList: List<StickerPack> = ArrayList()

        //if (RequestPermissionsHelper.verifyPermissions(context)) {
            val contentFile = File(context.filesDir, "stickers_asset/contents.json")
            try {
                FileInputStream(contentFile).use { contentsInputStream ->
                    stickerPackList = ContentFileParser.parseStickerPacks(contentsInputStream)
                }
            } catch (e: IOException) {
                Log.i("Content provider: ", "contents.json" + " file has some issues: " + e.message)
            } catch (e: IllegalStateException) {
                Log.i("Content provider: ", "contents.json" + " file has some issues: " + e.message)
            }
        //}
        val stringg = stickerPackList.toString()

        return stickerPackList
    }

    //*//// REVISAR LO QUE SE ESCRIBE EN JSON
    fun saveStickerPacksToJson(container: StickerPacksContainer, context: Context) {
        val json = Gson().toJson(container)
        try {
            //val file = File(Constants.STICKERS_DIRECTORY_PATH + "/contents.json")
            val file = File(context.filesDir, "stickers_asset/contents.json")
            val output: Writer = BufferedWriter(FileWriter(file))
            output.write(json)
            output.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    fun createStickerImageFile(sourceUri: Uri, destinyUri: Uri, context: Context, format: Bitmap.CompressFormat) {
        val destinationFilename = destinyUri.path
        try {
            val file = destinationFilename?.let { File(it) }
            val bitmapdata = ImageUtils.compressImageToBytes(sourceUri, 70, 512, 512, context, format)
            val fos = FileOutputStream(file)
            fos.write(bitmapdata)
            fos.flush()
            fos.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    fun createStickerPackTrayIconFile(sourceUri: Uri, destinyUri: Uri, context: Context) {
        val destinationFilename = destinyUri.path
        try {
            val file = File(destinationFilename.toString())
            val bitmapdata = ImageUtils.compressImageToBytes(sourceUri, 80, 96, 96, context, Bitmap.CompressFormat.PNG)
            val fos = FileOutputStream(file)
            fos.write(bitmapdata)
            fos.flush()
            fos.close()
        } catch (e: IOException) {

            e.printStackTrace()
        }
    }

    fun deleteStickerPack(index: Int, context: Context) {

        val pack = stickerPacksContainer?.removeStickerPack(index)


        val directoryPath = File(context.filesDir, "stickers_asset")

        pack?.let {
            val stickerPath = "$directoryPath/${it.identifier}"

            //FileUtils.initializeDirectories(stickerPath)
            FileUtils.deleteFolder(stickerPath)
            //FileUtils.deleteFolder(directoryPath.toString() + it.identifier)

            saveStickerPacksToJson(stickerPacksContainer!!, context)
        }
    }
}
