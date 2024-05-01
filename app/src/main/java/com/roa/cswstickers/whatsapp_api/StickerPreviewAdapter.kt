package com.roa.cswstickers.whatsapp_api

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.roa.cswstickers.R
import com.roa.cswstickers.utils.ImageUtils
import java.io.File

class StickerPreviewAdapter(
    private val context: Context,
    private val layoutInflater: LayoutInflater,
    private val errorResource: Int,
    private val cellSize: Int,
    private val cellPadding: Int,
    private val stickerPack: StickerPack
) : RecyclerView.Adapter<StickerPreviewViewHolder>() {

    private var cellLimit = 0

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StickerPreviewViewHolder {
        val itemView = layoutInflater.inflate(R.layout.sticker_image, parent, false)
        val vh = StickerPreviewViewHolder(itemView)

        val layoutParams = vh.stickerPreviewView.layoutParams
        layoutParams.height = cellSize
        layoutParams.width = cellSize
        vh.stickerPreviewView.layoutParams = layoutParams
        vh.stickerPreviewView.setPadding(cellPadding, cellPadding, cellPadding, cellPadding)

        return vh
    }

    override fun onBindViewHolder(holder: StickerPreviewViewHolder, position: Int) {
        holder.stickerPreviewView.setImageResource(errorResource)
        val stickerFileName = stickerPack.stickers?.get(position)?.image_file

        val directoryPath = File(context.filesDir, "stickers_asset")
        val stickerPath = "$directoryPath/${stickerPack.identifier}"

        holder.stickerPreviewView.setImageURI(ImageUtils.getStickerImageAsset(stickerPack.identifier.toString(),
            stickerFileName.toString(), stickerPath
        ))
    }

    override fun getItemCount(): Int {
        var numberOfPreviewImagesInPack = stickerPack.stickers!!.size
        if (cellLimit > 0) {
            numberOfPreviewImagesInPack = minOf(numberOfPreviewImagesInPack, cellLimit)
        }
        return numberOfPreviewImagesInPack
    }
}
