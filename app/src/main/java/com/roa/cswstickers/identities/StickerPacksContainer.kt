package com.roa.cswstickers.identities

import com.roa.cswstickers.whatsapp_api.StickerPack

class StickerPacksContainer(
    var android_play_store_link: String = "",
    var ios_app_store_link: String = "",
    var sticker_packs: MutableList<StickerPack>
) {
    constructor() : this("", "", mutableListOf())

    fun addStickerPack(stickerPack: StickerPack) {
        sticker_packs.add(stickerPack)
    }

    fun removeStickerPack(index: Int): StickerPack? {
        if (index in sticker_packs.indices) {
            return sticker_packs.removeAt(index)
        }
        return null
    }

    fun getStickerPack(index: Int): StickerPack? {
        return sticker_packs.getOrNull(index)
    }
}
