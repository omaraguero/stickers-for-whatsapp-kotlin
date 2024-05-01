package com.roa.cswstickers.whatsapp_api

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewTreeObserver
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.facebook.drawee.view.SimpleDraweeView
import com.roa.cswstickers.R
import com.roa.cswstickers.utils.FileUtils
import com.roa.cswstickers.utils.ImageUtils
import com.sangcomz.fishbun.FishBun
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

class StickerPackDetailsActivity : AddStickerPackActivity() {

    companion object {
        const val EXTRA_STICKER_PACK_ID = "sticker_pack_id"
        const val EXTRA_STICKER_PACK_AUTHORITY = "sticker_pack_authority"
        const val EXTRA_STICKER_PACK_NAME = "sticker_pack_name"
        const val EXTRA_STICKER_PACK_WEBSITE = "sticker_pack_website"
        const val EXTRA_STICKER_PACK_EMAIL = "sticker_pack_email"
        const val EXTRA_STICKER_PACK_PRIVACY_POLICY = "sticker_pack_privacy_policy"
        const val EXTRA_STICKER_PACK_TRAY_ICON = "sticker_pack_tray_icon"
        const val EXTRA_SHOW_UP_BUTTON = "show_up_button"
        const val EXTRA_STICKER_PACK_DATA = "sticker_pack"
    }

    private lateinit var recyclerView: RecyclerView
    private lateinit var layoutManager: GridLayoutManager
    private var stickerPreviewAdapter: StickerPreviewAdapter? = null
    private var numColumns: Int = 0
    private lateinit var addButton: View
    private lateinit var alreadyAddedText: View
    private lateinit var stickerPack: StickerPack
    private lateinit var divider: View
    private var whiteListCheckCoroutine: WhiteListCheckCoroutine? = null



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.sticker_pack_details)
        val showUpButton = intent.getBooleanExtra(EXTRA_SHOW_UP_BUTTON, false)

        //stickerPack = intent.getParcelableExtra(EXTRA_STICKER_PACK_DATA)!!

        stickerPack = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getParcelableExtra(EXTRA_STICKER_PACK_DATA, StickerPack::class.java)!!
        } else {
            intent.getParcelableExtra(EXTRA_STICKER_PACK_DATA)!!
        }
        val packNameTextView: TextView = findViewById(R.id.pack_name)
        val packPublisherTextView: TextView = findViewById(R.id.author)
        val packTrayIcon: ImageView = findViewById(R.id.tray_image)
        val packSizeTextView: TextView = findViewById(R.id.pack_size)
        //val expandedStickerView: SimpleDraweeView = findViewById(R.id.sticker_details_expanded_sticker)

        addButton = findViewById(R.id.add_to_whatsapp_button)
        alreadyAddedText = findViewById(R.id.already_added_text)
        layoutManager = GridLayoutManager(this, 1)
        recyclerView = findViewById(R.id.sticker_list)
        recyclerView.layoutManager = layoutManager
        recyclerView.viewTreeObserver.addOnGlobalLayoutListener(pageLayoutListener)
        recyclerView.addOnScrollListener(dividerScrollListener)
        divider = findViewById(R.id.divider)
        if (stickerPreviewAdapter == null) {
            stickerPreviewAdapter = StickerPreviewAdapter(
                this,
                layoutInflater,
                R.drawable.sticker_error,
                resources.getDimensionPixelSize(R.dimen.sticker_pack_details_image_size),
                resources.getDimensionPixelSize(R.dimen.sticker_pack_details_image_padding),
                stickerPack
            )
            recyclerView.adapter = stickerPreviewAdapter
        }
        packNameTextView.text = stickerPack.name
        packPublisherTextView.text = stickerPack.publisher


        val directoryPath = File(this.filesDir, "stickers_asset")
        val stickerPath = "$directoryPath/${stickerPack.identifier}"



        packTrayIcon.setImageURI(
            ImageUtils.getStickerImageAsset(
                stickerPack.identifier.toString(),
                stickerPack.tray_image_file.toString(),
                stickerPath
            )
        )

        packSizeTextView.text = FileUtils.getFolderSizeLabel(stickerPath)



        addButton.setOnClickListener {
            addStickerPackToWhatsApp(
                stickerPack.identifier.toString(),
                stickerPack.name.toString()
            )
        }
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(showUpButton)
            setTitle(if (showUpButton) R.string.title_activity_sticker_pack_details_multiple_pack else R.string.title_activity_sticker_pack_details_single_pack)
        }
    }




    private fun launchInfoActivity(
        publisherWebsite: String?,
        publisherEmail: String?,
        privacyPolicyWebsite: String?,
        trayIconUriString: String
    ) {
        val intent = Intent(this, StickerPackInfoActivity::class.java).apply {
            putExtra(EXTRA_STICKER_PACK_ID, stickerPack.identifier)
            putExtra(EXTRA_STICKER_PACK_WEBSITE, publisherWebsite)
            putExtra(EXTRA_STICKER_PACK_EMAIL, publisherEmail)
            putExtra(EXTRA_STICKER_PACK_PRIVACY_POLICY, privacyPolicyWebsite)
            putExtra(EXTRA_STICKER_PACK_TRAY_ICON, trayIconUriString)
        }
        startActivity(intent)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.toolbar, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return if (item.itemId == R.id.action_info) {
            val publisherWebsite = stickerPack.publisher_website
            val publisherEmail = stickerPack.publisher_email
            val privacyPolicyWebsite = stickerPack.privacy_policy_website


            val trayIconUri =
                ImageUtils.getStickerImageAsset(stickerPack.identifier.toString(),
                    stickerPack.tray_image_file.toString(), )
            launchInfoActivity(
                publisherWebsite,
                publisherEmail,
                privacyPolicyWebsite,
                trayIconUri.toString()
            )
            true
        } else {
            super.onOptionsItemSelected(item)
        }
    }

    private val pageLayoutListener = ViewTreeObserver.OnGlobalLayoutListener {
        setNumColumns(recyclerView.width / resources.getDimensionPixelSize(R.dimen.sticker_pack_details_image_size))
    }

    private fun setNumColumns(numColumns: Int) {
        if (this.numColumns != numColumns) {
            layoutManager.spanCount = numColumns
            this.numColumns = numColumns
            stickerPreviewAdapter?.notifyDataSetChanged()
        }
    }

    private val dividerScrollListener = object : RecyclerView.OnScrollListener() {
        override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
            super.onScrollStateChanged(recyclerView, newState)
            updateDivider(recyclerView)
        }

        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
            super.onScrolled(recyclerView, dx, dy)
            updateDivider(recyclerView)
        }

        private fun updateDivider(recyclerView: RecyclerView) {
            val showDivider = recyclerView.computeVerticalScrollOffset() > 0
            divider.visibility = if (showDivider) View.VISIBLE else View.INVISIBLE
        }
    }

    override fun onResume() {
        super.onResume()
        whiteListCheckCoroutine = WhiteListCheckCoroutine(this)
        whiteListCheckCoroutine!!.execute(stickerPack)
    }

    override fun onPause() {
        super.onPause()
        whiteListCheckCoroutine?.cancel()
    }

    private fun updateAddUI(isWhitelisted: Boolean) {
        if (isWhitelisted) {
            addButton.visibility = View.GONE
            alreadyAddedText.visibility = View.VISIBLE
        } else {
            addButton.visibility = View.VISIBLE
            alreadyAddedText.visibility = View.GONE
        }
    }

    internal class WhiteListCheckCoroutine(
        private val stickerPackDetailsActivity: StickerPackDetailsActivity
    ) {

        private var job: Job? = null

        fun execute(stickerPack: StickerPack?) {
            job = CoroutineScope(Dispatchers.IO).launch {
                val isWhitelisted = stickerPack?.let {
                    WhitelistCheck.isWhitelisted(
                        stickerPackDetailsActivity,
                        it.identifier.toString()
                    )
                } ?: false
                withContext(Dispatchers.Main) {
                    stickerPackDetailsActivity.updateAddUI(isWhitelisted)
                }
            }
        }

        fun cancel() {
            job?.cancel()
        }
    }
}


