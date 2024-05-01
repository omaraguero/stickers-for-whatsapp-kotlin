package com.roa.cswstickers.activities

import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.AdapterView.OnItemClickListener
import android.widget.BaseAdapter
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.GridView
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.airbnb.lottie.LottieAnimationView
import com.facebook.drawee.view.SimpleDraweeView
import com.roa.cswstickers.R
import com.roa.cswstickers.utils.StickerPacksManager
import com.roa.cswstickers.whatsapp_api.Sticker
import com.roa.cswstickers.whatsapp_api.StickerContentProvider
import com.roa.cswstickers.whatsapp_api.StickerPack
import com.roa.cswstickers.whatsapp_api.StickerPackDetailsActivity
import com.sangcomz.fishbun.FishBun
import com.sangcomz.fishbun.adapter.image.impl.GlideAdapter
import java.io.File
import java.util.Objects


class NewStickerPackActivity : AppCompatActivity() {
    var imageAdapter: ImageAdapter? = null
    var nameEdit: EditText? = null

    var authorEdit: EditText? = null
    var empty: LottieAnimationView? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_sticker_pack)
        // UI references.
        val tool = findViewById<Toolbar>(R.id.toolbar1)
        tool.title = "Sticker"
        setSupportActionBar(tool)
        supportActionBar!!.setDisplayShowTitleEnabled(false)
        supportActionBar!!.setDisplayShowHomeEnabled(true)
        nameEdit = findViewById<EditText>(R.id.sticker_pack_name_edit)
        empty = findViewById(R.id.animation_view)
        authorEdit = findViewById<EditText>(R.id.sticker_pack_author_edit)

        val btnCreate = findViewById<FrameLayout>(R.id.btn_create_pack)
        btnCreate.setOnClickListener { v: View? ->
            if (empty != null) {
                empty!!.visibility = View.GONE
            }
            FishBun.with(this@NewStickerPackActivity)
                .setImageAdapter(GlideAdapter())
                .setMaxCount(30)
                .exceptGif(true)
                .setActionBarColor(
                    Color.parseColor("#fead00"),
                    Color.parseColor("#fead00"),
                    false
                )
                .setMinCount(3).setActionBarTitleColor(Color.parseColor("#ffffff"))
                .startAlbum()

        }



        val gridview = findViewById<GridView>(R.id.sticker_pack_grid_images_preview)
        imageAdapter = ImageAdapter(this)
        gridview.adapter = imageAdapter
        gridview.onItemClickListener =
            OnItemClickListener { parent: AdapterView<*>?, v: View?, position: Int, id: Long ->
                Toast.makeText(this@NewStickerPackActivity, "Image removed", Toast.LENGTH_SHORT)
                    .show()
                imageAdapter!!.uries!!.removeAt(position)
                imageAdapter!!.notifyDataSetChanged()
            }
        if (supportActionBar != null) {
            supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_new_sticker_pack, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.save_sticker_pack) {
            if (validateValues()) {
                Toast.makeText(this, "You have to fill all empty spaces", Toast.LENGTH_SHORT).show()
            } else {
                saveStickerPack(
                    imageAdapter!!.uries,
                    nameEdit!!.text.toString(),
                    authorEdit!!.text.toString()
                )
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun validateValues(): Boolean {
        return nameEdit!!.text.toString()
            .trim { it <= ' ' }.length == 0 || authorEdit!!.text.toString()
            .trim { it <= ' ' }.length == 0 || imageAdapter!!.uries!!.size == 0
    }




    private fun saveStickerPack(uries: List<Uri>?, name: String, author: String) {

        var dialog = com.roa.cswstickers.activities.AlertDialog.progressDialog(this)
        dialog.show()

        Thread {
            try {
                val intent = Intent(
                    this@NewStickerPackActivity,
                    StickerPackDetailsActivity::class.java
                )
                intent.putExtra(StickerPackDetailsActivity.EXTRA_SHOW_UP_BUTTON, true)
                val identifier = nameEdit?.text.toString() + "CSWstickers"


                val stickerPack = StickerPack(
                    identifier,
                    name,
                    author,
                    Objects.requireNonNull<Array<Any>>(
                        uries!!.toTypedArray()
                    )[0].toString(),
                    "",
                    "",
                    "",
                    "",
                    "1"
                )

                val stickerList: List<Sticker> =
                    StickerPacksManager.saveStickerPackFilesLocally(stickerPack.identifier.toString(),
                        uries, this@NewStickerPackActivity)
                stickerPack.setStickers(stickerList)

                val directoryPath = File(this.filesDir, "stickers_asset")
                val stickerPath = "$directoryPath/$identifier"

                val trayIconFile: String = "TrayIconOf_$identifier" + ".png"


                StickerPacksManager.createStickerPackTrayIconFile(uries[0], Uri.parse("$stickerPath/$trayIconFile"), this@NewStickerPackActivity)


                stickerPack.tray_image_file = trayIconFile

                StickerPacksManager.stickerPacksContainer!!.addStickerPack(stickerPack)

                StickerPacksManager.saveStickerPacksToJson(StickerPacksManager.stickerPacksContainer!!, this)
                insertStickerPackInContentProvider(stickerPack)

                intent.putExtra(StickerPackDetailsActivity.EXTRA_STICKER_PACK_DATA, stickerPack)
                startActivity(intent)
                finish()
            } catch (e: Exception) {
                e.printStackTrace()
            }
            dialog.dismiss()
        }.start()
    }



    private fun insertStickerPackInContentProvider(stickerPack: StickerPack) {
        val contentValues = ContentValues()
        contentValues.put("stickerPack", com.google.gson.Gson().toJson(stickerPack))
        contentResolver.insert(StickerContentProvider.AUTHORITY_URI, contentValues)
    }

    @SuppressLint("SetTextI18n")
    @Deprecated("Deprecated in Java")
    public override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == FishBun.FISHBUN_REQUEST_CODE) {
            val uries: ArrayList<Uri>?
            if (resultCode == RESULT_OK) {
                uries = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    data?.getParcelableArrayListExtra<Uri>(FishBun.INTENT_PATH, Uri::class.java)!!
                } else {
                    data?.getParcelableArrayListExtra<Uri>(FishBun.INTENT_PATH)!!
                }

                if (uries.size > 0) {
                    imageAdapter!!.uries = uries
                    imageAdapter!!.notifyDataSetChanged()
                    (findViewById<View>(R.id.stickers_selected_textview) as TextView).text =
                        uries.size.toString() + " stickers selected"
                }
            }
        }
    }


    inner class ImageAdapter(private val mContext: Context) : BaseAdapter() {

        override fun getCount(): Int {
            return uries!!.size
        }

        override fun getItem(position: Int): Any? {
            return null
        }

        override fun getItemId(position: Int): Long {
            return 0
        }

        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            val imageView: SimpleDraweeView
            if (convertView == null) {
                imageView = SimpleDraweeView(mContext)
                imageView.layoutParams = ViewGroup.LayoutParams(150, 150)
                imageView.scaleType = ImageView.ScaleType.FIT_CENTER
                imageView.adjustViewBounds = true
                imageView.setPadding(8, 8, 8, 8)
            } else {
                imageView = convertView as SimpleDraweeView
            }
            imageView.setImageURI(uries!![position])
            return imageView
        }

        var uries: ArrayList<Uri>? = ArrayList()
    }
}
