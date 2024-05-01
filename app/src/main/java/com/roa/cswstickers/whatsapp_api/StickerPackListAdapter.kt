/*
 * Copyright (c) WhatsApp Inc. and its affiliates.
 * All rights reserved.
 *
 * This source code is licensed under the BSD-style license found in the
 * LICENSE file in the root directory of this source tree.
 */
package com.roa.cswstickers.whatsapp_api

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.widget.PopupMenu
import androidx.recyclerview.widget.RecyclerView
import com.facebook.drawee.view.SimpleDraweeView
import com.roa.cswstickers.R
import com.roa.cswstickers.activities.MyStickersFragment
import com.roa.cswstickers.utils.FileUtils
import com.roa.cswstickers.utils.ImageUtils
import com.roa.cswstickers.utils.StickerPacksManager
import java.io.File

class StickerPackListAdapter(
    stickerPacks: List<StickerPack>,
    onAddButtonClickedListener: OnAddButtonClickedListener,
    parent: MyStickersFragment
) :
    RecyclerView.Adapter<StickerPackListItemViewHolder>() {
    private var stickerPacks: MutableList<StickerPack>
    private val onAddButtonClickedListener: OnAddButtonClickedListener
    private var maxNumberOfStickersInARow = 0
    private val parent: MyStickersFragment

    init {
        this.stickerPacks = stickerPacks as MutableList<StickerPack>
        this.onAddButtonClickedListener = onAddButtonClickedListener
        this.parent = parent
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, i: Int): StickerPackListItemViewHolder {
        val context = viewGroup.context
        val layoutInflater = LayoutInflater.from(context)
        val stickerPackRow: View = layoutInflater.inflate(R.layout.sticker_packs_list_item, viewGroup, false)
        return StickerPackListItemViewHolder(stickerPackRow)
    }

    override fun onBindViewHolder(viewHolder: StickerPackListItemViewHolder, index: Int) {
        val pack: StickerPack = stickerPacks[index]
        val context: Context = viewHolder.publisherView.getContext()
        viewHolder.publisherView.setText(pack.publisher)


        val directoryPath = File(context.filesDir, "stickers_asset")
        val stickerPath = "$directoryPath/${pack.identifier}"

        //viewHolder.filesizeView.setText(FileUtils.getFolderSizeLabel(Constants.STICKERS_DIRECTORY_PATH + pack.identifier))
        viewHolder.filesizeView.setText(FileUtils.getFolderSizeLabel(stickerPath))

        viewHolder.titleView.setText(pack.name)


        viewHolder.container.setOnClickListener { view ->
            val intent: Intent = Intent(
                view.getContext(),
                StickerPackDetailsActivity::class.java
            )
            intent.putExtra(StickerPackDetailsActivity.EXTRA_SHOW_UP_BUTTON, true)
            intent.putExtra(StickerPackDetailsActivity.EXTRA_STICKER_PACK_DATA, pack)
            view.getContext().startActivity(intent)
        }
        viewHolder.imageRowView.removeAllViews()
        //if this sticker pack contains less stickers than the max, then take the smaller size.
        val actualNumberOfStickersToShow =
            Math.min(maxNumberOfStickersInARow, pack.getStickers()!!.size)
        for (i in 0 until actualNumberOfStickersToShow) {
            val rowImage = LayoutInflater.from(context).inflate(
                R.layout.sticker_pack_list_item_image,
                viewHolder.imageRowView,
                false
            ) as SimpleDraweeView

            rowImage.setImageURI(
                ImageUtils.getStickerImageAsset(
                    pack.identifier.toString(),
                    pack.getStickers()!!.get(i).image_file.toString(),
                    stickerPath
                )
            )
            val lp = rowImage.layoutParams as LinearLayout.LayoutParams
            val marginBetweenImages: Int =
                (viewHolder.imageRowView.getMeasuredWidth() - maxNumberOfStickersInARow * viewHolder.imageRowView.getContext()
                    .getResources()
                    .getDimensionPixelSize(R.dimen.sticker_pack_list_item_preview_image_size)) / (maxNumberOfStickersInARow - 1) - lp.leftMargin - lp.rightMargin
            if (i != actualNumberOfStickersToShow - 1 && marginBetweenImages > 0) { //do not set the margin for the last image
                lp.setMargins(
                    lp.leftMargin,
                    lp.topMargin,
                    lp.rightMargin + marginBetweenImages,
                    lp.bottomMargin
                )
                rowImage.layoutParams = lp
            }
            viewHolder.imageRowView.addView(rowImage)
        }
        setAddButtonAppearance(viewHolder.addButton, pack)
        viewHolder.container.setOnLongClickListener { view ->
            val popupMenu =
                PopupMenu(context, viewHolder.addButton)
            popupMenu.inflate(R.menu.sticker_option_menu)
            popupMenu.setOnMenuItemClickListener { item: MenuItem ->
                when (item.itemId) {
                    R.id.sticker_delete -> AlertDialog.Builder(
                        context,
                        R.style.DialogTheme
                    )
                        .setTitle("Deleting")
                        .setMessage("Are you sure you want to delete this sticker pack?")
                        .setPositiveButton(
                            "Yes"
                        ) { dialog: DialogInterface?, which: Int ->
                            removeStickerPack(index, context)
                            Toast.makeText(context, "Deleted", Toast.LENGTH_SHORT).show()
                            parent.verifyStickersCount()
                        }
                        .setNegativeButton("No", null)
                        .show()

                    else -> {}
                }
                false
            }
            popupMenu.show()
            false
        }
    }

    private fun removeStickerPack(index: Int, context: Context) {
        stickerPacks.removeAt(index)
        notifyItemRemoved(index)
        notifyItemRangeChanged(index, stickerPacks.size)
        StickerPacksManager.deleteStickerPack(index, context)
    }

    private fun setAddButtonAppearance(addButton: ImageView, pack: StickerPack) {
        if (pack.getIsWhiteListed()) {
            addButton.setImageResource(R.drawable.sticker_3rdparty_added)
            addButton.isClickable = false
            addButton.setOnClickListener(null)
            setBackground(addButton)
        } else {
            addButton.setImageResource(R.drawable.sticker_3rdparty_add)
            addButton.setOnClickListener { v: View? ->
                onAddButtonClickedListener.onAddButtonClicked(
                    pack
                )
            }
            val outValue = TypedValue()
            addButton.context.theme.resolveAttribute(
                android.R.attr.selectableItemBackground,
                outValue,
                true
            )
            addButton.setBackgroundResource(outValue.resourceId)
        }
    }

    private fun setBackground(view: View) {
        view.background = null
    }

    override fun getItemCount(): Int {
        return stickerPacks.size
    }

    fun setMaxNumberOfStickersInARow(maxNumberOfStickersInARow: Int) {
        if (this.maxNumberOfStickersInARow != maxNumberOfStickersInARow) {
            this.maxNumberOfStickersInARow = maxNumberOfStickersInARow
            notifyDataSetChanged()
        }
    }

    fun setStickerPackList(stickerPackList: List<StickerPack>) {
        stickerPacks = stickerPackList.toMutableList()
        notifyDataSetChanged()
    }

    fun interface OnAddButtonClickedListener {
        fun onAddButtonClicked(stickerPack: StickerPack?)
    }
}
