package com.roa.cswstickers.whatsapp_api

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.facebook.drawee.view.SimpleDraweeView
import com.roa.cswstickers.R

class StickerPreviewViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    val stickerPreviewView: SimpleDraweeView = itemView.findViewById(R.id.sticker_preview)
}
