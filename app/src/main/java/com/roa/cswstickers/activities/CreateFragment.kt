package com.roa.cswstickers.activities

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.ContentValues
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.PopupMenu
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.roa.cswstickers.R
import com.roa.cswstickers.utils.FileUtils
import com.facebook.drawee.view.SimpleDraweeView
import com.sangcomz.fishbun.FishBun

import java.io.File

class CreateFragment : Fragment() {
    var imagesGridAdapter: ImagesGridAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }



    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {

        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == FishBun.FISHBUN_REQUEST_CODE) {

            val uries: ArrayList<Uri>?
            if (resultCode == AppCompatActivity.RESULT_OK) {
                uries = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    data?.getParcelableArrayListExtra<Uri>(FishBun.INTENT_PATH, Uri::class.java)!!
                } else {
                    data?.getParcelableArrayListExtra<Uri>(FishBun.INTENT_PATH)!!
                }
                //SampleCustomActivity.start(mainActivity)


            }
        }

    }

    inner class ImagesGridAdapter(context: Context, uries: ArrayList<Uri>) :
        RecyclerView.Adapter<ImageViewHolder>() {
        var uries = ArrayList<Uri>()
        var context: Context

        init {
            this.uries = uries
            this.context = context
        }

        override fun onCreateViewHolder(viewGroup: ViewGroup, i: Int): ImageViewHolder {
            val context = viewGroup.context
            val layoutInflater = LayoutInflater.from(context)
            val view: View = layoutInflater.inflate(R.layout.sticker_created_item, viewGroup, false)
            return ImageViewHolder(view)
        }

        override fun onBindViewHolder(imageAdapter: ImageViewHolder, index: Int) {
            imageAdapter.imageView.setImageURI(uries[index])
            imageAdapter.imageView.setPadding(8, 8, 8, 8)
            imageAdapter.imageView.setOnLongClickListener { v: View? ->
                val popupMenu =
                    PopupMenu(context, imageAdapter.imageView)
                popupMenu.inflate(R.menu.sticker_menu)
                popupMenu.setOnMenuItemClickListener { item: MenuItem ->
                    when (item.itemId) {
                        R.id.delete_sticker -> deleteSticker(index)
                        else -> {}
                    }
                    false
                }
                popupMenu.show()
                false
            }
        }

        fun deleteSticker(index: Int) {
            AlertDialog.Builder(context, R.style.DialogTheme)
                .setTitle("Deleting")
                .setMessage("Are you sure you want to delete this sticker?")
                .setPositiveButton("Yes") { dialog: DialogInterface?, which: Int ->
                    val uri = uries[index]
                    FileUtils.deleteFile(uri.path.toString(), context)
                    uries.removeAt(index)
                    notifyItemRemoved(index)
                    notifyDataSetChanged()
                    Toast.makeText(context, "Deleted", Toast.LENGTH_SHORT).show()
                    //verifyStickersCount()
                }
                .setNegativeButton("No", null)
                .show()
        }

        fun addToStickerPack(index: Int) {
            val intent = Intent(
                context,
                AddToStickerPackActivity::class.java
            )
            intent.setData(uries[index])
            startActivity(intent)
        }

        override fun getItemCount(): Int {
            return uries.size
        }
    }

    inner class ImageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var imageView: SimpleDraweeView

        init {
            imageView = itemView.findViewById<SimpleDraweeView>(R.id.sticker_created_image)
        }
    }

    companion object {
        fun addImageToGallery(filePath: String?, context: Context?) {
            val values = ContentValues()
            values.put(MediaStore.Images.Media.DATE_TAKEN, System.currentTimeMillis())
            values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
            values.put(MediaStore.MediaColumns.DATA, filePath)
            context!!.contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
        }
    }
}

