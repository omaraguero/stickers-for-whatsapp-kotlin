package com.roa.cswstickers.whatsapp_api

import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.roa.cswstickers.R

/*
* Copyright (c) WhatsApp Inc. and its affiliates.
* All rights reserved.
*
* This source code is licensed under the BSD-style license found in the
* LICENSE file in the root directory of this source tree.
*/

class StickerPackListItemViewHolder(val container: View) : RecyclerView.ViewHolder(
    container
) {
    val titleView: TextView
    val publisherView: TextView
    val filesizeView: TextView
    val addButton: ImageView
    //val animatedStickerPackIndicator: ImageView
    val imageRowView: LinearLayout

    init {
        titleView = itemView.findViewById(R.id.sticker_pack_title)
        publisherView = itemView.findViewById(R.id.sticker_pack_publisher)
        filesizeView = itemView.findViewById(R.id.sticker_pack_filesize)
        addButton = itemView.findViewById(R.id.add_button_on_list)
        imageRowView = itemView.findViewById(R.id.sticker_packs_list_item_image_list)
        //animatedStickerPackIndicator = itemView.findViewById(R.id.sticker_pack_animation_indicator)
    }
}