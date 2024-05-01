/*
 * Copyright (c) WhatsApp Inc. and its affiliates.
 * All rights reserved.
 *
 * This source code is licensed under the BSD-style license found in the
 * LICENSE file in the root directory of this source tree.
 */
package com.roa.cswstickers.whatsapp_api

import android.os.Parcel
import android.os.Parcelable

class StickerPack : Parcelable {
    val identifier: String?
    val name: String?
    val publisher: String?
    var tray_image_file: String?
    val publisher_email: String?
    val publisher_website: String?
    val privacy_policy_website: String?
    val license_agreement_website: String?
    val image_data_version: String?
    val avoid_cache: Boolean
    //val animated_sticker_pack: Boolean
    var ios_app_store_link: String? = null
    var stickers: List<Sticker>? = null
    //var total_size: Long = 0
        private set
    var android_play_store_link: String? = null

    //var is_whitelisted = false

    constructor(
        identifier: String?,
        name: String?,
        publisher: String?,
        trayImageFile: String?,
        publisherEmail: String?,
        publisherWebsite: String?,
        privacyPolicyWebsite: String?,
        licenseAgreementWebsite: String?,
        imageDataVersion: String?,
        avoidCache: Boolean = false,
        //animatedStickerPack: Boolean = false
    ) {
        this.identifier = identifier
        this.name = name
        this.publisher = publisher
        this.tray_image_file = trayImageFile
        this.publisher_email = publisherEmail
        this.publisher_website = publisherWebsite
        this.privacy_policy_website = privacyPolicyWebsite
        this.license_agreement_website = licenseAgreementWebsite
        this.image_data_version = imageDataVersion
        this.avoid_cache = avoidCache
        //this.animated_sticker_pack = animatedStickerPack
    }

    private constructor(`in`: Parcel) {
        identifier = `in`.readString()
        name = `in`.readString()
        publisher = `in`.readString()
        tray_image_file = `in`.readString()
        publisher_email = `in`.readString()
        publisher_website = `in`.readString()
        privacy_policy_website = `in`.readString()
        license_agreement_website = `in`.readString()
        ios_app_store_link = `in`.readString()
        stickers = `in`.createTypedArrayList<Sticker>(Sticker.CREATOR)
        //total_size = `in`.readLong()
        android_play_store_link = `in`.readString()
        //is_whitelisted = `in`.readByte().toInt() != 0
        image_data_version = `in`.readString()
        avoid_cache = `in`.readByte().toInt() != 0
        //animated_sticker_pack = `in`.readByte().toInt() != 0
    }

    @JvmName("Stickers")
    fun setStickers(stickers: List<Sticker>) {
        this.stickers = stickers
        //total_size = 0
        //for (sticker in stickers) {
        //    total_size += sticker.size
        //}
    }

    @JvmName("AndroidPlayStoreLink")
    fun setAndroidPlayStoreLink(androidPlayStoreLink: String?) {
        this.android_play_store_link = androidPlayStoreLink
    }

    @JvmName("iosAppStoreLink")
    fun setIosAppStoreLink(iosAppStoreLink: String?) {
        this.ios_app_store_link = iosAppStoreLink
    }

    @JvmName("Stickers")
    fun getStickers(): List<Sticker>? {
        return stickers
    }

    override fun describeContents(): Int {
        return 0
    }

    fun getIsWhiteListed(): Boolean {
        return true
    }



    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeString(identifier)
        dest.writeString(name)
        dest.writeString(publisher)
        dest.writeString(tray_image_file)
        dest.writeString(publisher_email)
        dest.writeString(publisher_website)
        dest.writeString(privacy_policy_website)
        dest.writeString(license_agreement_website)
        dest.writeString(ios_app_store_link)
        dest.writeTypedList(stickers)
        //dest.writeLong(total_size)
        dest.writeString(android_play_store_link)
        //dest.writeByte((if (is_whitelisted) 1 else 0).toByte())
        dest.writeString(image_data_version)
        dest.writeByte((if (avoid_cache) 1 else 0).toByte())
        //dest.writeByte((if (animated_sticker_pack) 1 else 0).toByte())
    }

    companion object {
        @JvmField
        val CREATOR: Parcelable.Creator<StickerPack?> = object : Parcelable.Creator<StickerPack?> {
            override fun createFromParcel(`in`: Parcel): StickerPack {
                return StickerPack(`in`)
            }

            override fun newArray(size: Int): Array<StickerPack?> {
                return arrayOfNulls(size)
            }
        }
    }
}

