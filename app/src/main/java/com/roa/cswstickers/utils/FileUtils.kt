package com.roa.cswstickers.utils


import android.content.Context
import android.net.Uri
import android.provider.MediaStore
import android.util.Log
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.InputStream

object FileUtils {

    fun generateRandomIdentifier(): String {
        val possibilities = "abcdefghijklmnopqrstuvwxyz0123456789"
        val random = java.util.Random()
        val generatedIdentifier = StringBuilder()
        repeat(4) {
            generatedIdentifier.append(possibilities[random.nextInt(possibilities.length - 1)])
        }
        return generatedIdentifier.toString()
    }


    fun initializeDirectories(context: Context) {
        val directory = File(context.filesDir, "stickers_asset")
        if (!directory.exists()) {
            directory.mkdirs()
            val value = "{\"androidPlayStoreLink\": \"\",\"iosAppStoreLink\": \"\",\"sticker_packs\": [ ]}"

            try {
                val jsonFile = File(directory, "contents.json")
                FileOutputStream(jsonFile).use { outputStream ->
                    outputStream.write(value.toByteArray())
                }

            } catch (e: FileNotFoundException) {
                e.printStackTrace()
            }
        }else{
        }

    }

    @Throws(Exception::class)
    fun getBytes(inputStream: InputStream): ByteArray {
        val byteBuffer = ByteArrayOutputStream()
        val bufferSize = 1024
        val buffer = ByteArray(bufferSize)

        var len: Int
        while (inputStream.read(buffer).also { len = it } != -1) {
            byteBuffer.write(buffer, 0, len)
        }
        return byteBuffer.toByteArray()
    }

    fun deleteFolder(path: String) {
        val dir = File(path)
        if (dir.exists()) {
            val files = dir.listFiles()
            files?.forEach {
                if (it.isDirectory) {
                    deleteFolder(it.path)
                } else {
                    it.delete()
                }
            }
        }
        dir.delete()
    }

    fun deleteFile(path: String, context: Context) {
        val file = File(path)
        if (file.exists()) {
            file.delete()
        }
        context.contentResolver.delete(
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
            MediaStore.MediaColumns.DATA + "='" + path + "'", null
        )
    }

    fun getImageRealPathFromURI(context: Context, contentUri: Uri): String {
        context.contentResolver.query(contentUri, arrayOf(MediaStore.Images.Media.DATA), null, null, null)?.use { cursor ->
            cursor.moveToFirst()
            val columnIndex = cursor.getColumnIndex(MediaStore.Images.Media.DATA)
            return cursor.getString(columnIndex)
        }
        return contentUri.path ?: ""
    }

    fun getFolderSizeLabel(path: String): String {
        val size = getFolderSize(File(path)) / 1024 // Get size and convert bytes into Kb.
        return if (size >= 1024) {
            "${size / 1024} MB"
        } else {
            "$size KB"
        }
    }

    private fun getFolderSize(file: File): Long {
        var size: Long = 0
        if (file.isDirectory) {
            file.listFiles()?.forEach { child ->
                size += getFolderSize(child)
            }
        } else {
            size = file.length()
        }
        return size
    }
}
