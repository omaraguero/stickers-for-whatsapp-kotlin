package com.roa.cswstickers.whatsapp_api

import android.text.TextUtils
import android.util.JsonReader
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader

class ContentFileParser {
    companion object {
        @Throws(IOException::class, IllegalStateException::class)
        fun parseStickerPacks(contentsInputStream: InputStream): List<StickerPack> {
            contentsInputStream.use { inputStream ->
                JsonReader(InputStreamReader(inputStream)).use { reader ->
                    return readStickerPacks(reader)
                }
            }
        }

        @Throws(IOException::class, IllegalStateException::class)
        private fun readStickerPacks(reader: JsonReader): List<StickerPack> {
            val stickerPackList = ArrayList<StickerPack>()
            var androidPlayStoreLink: String? = null
            var iosAppStoreLink: String? = null

            reader.beginObject()
            while (reader.hasNext()) {
                when (val key = reader.nextName()) {
                    "android_play_store_link" -> androidPlayStoreLink = reader.nextString()
                    "ios_app_store_link" -> iosAppStoreLink = reader.nextString()
                    "sticker_packs" -> {
                        reader.beginArray()
                        while (reader.hasNext()) {
                            val stickerPack = readStickerPack(reader)
                            stickerPackList.add(stickerPack)
                        }
                        reader.endArray()
                    }
                    else -> throw IllegalStateException("unknown field in json: $key")
                }
            }
            reader.endObject()

            if (stickerPackList.isEmpty()) {
                throw IllegalStateException("sticker pack list cannot be empty")
            }

            for (stickerPack in stickerPackList) {
                stickerPack.setAndroidPlayStoreLink(androidPlayStoreLink.toString())
                stickerPack.setIosAppStoreLink(iosAppStoreLink.toString())
            }
            return stickerPackList
        }

        @Throws(IOException::class, IllegalStateException::class)
        private fun readStickerPack(reader: JsonReader): StickerPack {
            reader.beginObject()
            var identifier: String? = null
            var name: String? = null
            var publisher: String? = null
            var trayImageFile: String? = null
            var publisherEmail: String? = null
            var publisherWebsite: String? = null
            var privacyPolicyWebsite: String? = null
            var licenseAgreementWebsite: String? = null
            var imageDataVersion = ""
            var avoidCache = false
            var animatedStickerPack = false
            var stickerList: List<Sticker>? = null


            while (reader.hasNext()) {
                when (val key = reader.nextName()) {
                    "identifier" -> identifier = reader.nextString()
                    "name" -> name = reader.nextString()
                    "publisher" -> publisher = reader.nextString()
                    "tray_image_file" -> trayImageFile = reader.nextString()
                    "publisher_email" -> publisherEmail = reader.nextString()
                    "publisher_website" -> publisherWebsite = reader.nextString()
                    "privacy_policy_website" -> privacyPolicyWebsite = reader.nextString()
                    "license_agreement_website" -> licenseAgreementWebsite = reader.nextString()
                    "stickers" -> stickerList = readStickers(reader)
                    "image_data_version" -> imageDataVersion = reader.nextString()
                    "avoid_cache" -> avoidCache = reader.nextBoolean()
                    "animated_sticker_pack" -> animatedStickerPack = reader.nextBoolean()
                    else -> reader.skipValue()
                }
            }

            if (TextUtils.isEmpty(identifier)) {
                throw IllegalStateException("identifier cannot be empty")
            }
            if (TextUtils.isEmpty(name)) {
                throw IllegalStateException("name cannot be empty")
            }
            if (TextUtils.isEmpty(publisher)) {
                throw IllegalStateException("publisher cannot be empty")
            }
            if (TextUtils.isEmpty(trayImageFile)) {
                throw IllegalStateException("tray_image_file cannot be empty")
            }
            if (stickerList == null || stickerList.isEmpty()) {
                throw IllegalStateException("sticker list is empty")
            }
            if (identifier?.contains("..") == true || identifier?.contains("/") == true) {
                throw IllegalStateException("identifier should not contain .. or / to prevent directory traversal")
            }
            if (TextUtils.isEmpty(imageDataVersion)) {
                throw IllegalStateException("image_data_version should not be empty")
            }

            reader.endObject()
            return StickerPack(
                identifier.toString(), name.toString(),
                publisher.toString(), trayImageFile.toString(), publisherEmail.toString(),
                publisherWebsite.toString(),
                privacyPolicyWebsite.toString(), licenseAgreementWebsite.toString()
                ,imageDataVersion.toString()
            //,
                //imageDataVersion, avoidCache, animatedStickerPack
            ) .apply { setStickers(stickerList) }
        }

        @Throws(IOException::class, IllegalStateException::class)
        private fun readStickers(reader: JsonReader): List<Sticker> {
            reader.beginArray()
            val stickerList = ArrayList<Sticker>()

            while (reader.hasNext()) {
                reader.beginObject()
                var imageFile: String? = null
                val emojis = ArrayList<String>()

                while (reader.hasNext()) {
                    when (val key = reader.nextName()) {
                        "image_file" -> imageFile = reader.nextString()
                        "emojis" -> {
                            reader.beginArray()
                            while (reader.hasNext()) {
                                val emoji = reader.nextString()
                                if (!TextUtils.isEmpty(emoji)) {
                                    emojis.add(emoji)
                                }
                            }
                            reader.endArray()
                        }
                        else -> throw IllegalStateException("unknown field in json: $key")
                    }
                }

                reader.endObject()
                if (TextUtils.isEmpty(imageFile)) {
                    throw IllegalStateException("sticker image_file cannot be empty")
                }
                if (imageFile != null) {
                    if (!imageFile.endsWith(".webp")) {
                        throw IllegalStateException("image file for stickers should be webp files, image file is: $imageFile")
                    }
                }
                if (imageFile!!.contains("..") || imageFile.contains("/")) {
                    throw IllegalStateException("the file name should not contain .. or / to prevent directory traversal, image file is: $imageFile")
                }
                stickerList.add(Sticker(imageFile, emojis))
            }
            reader.endArray()
            return stickerList
        }
    }
}
