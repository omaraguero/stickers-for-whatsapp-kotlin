package com.roa.cswstickers.activities

import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.roa.cswstickers.identities.StickerPacksContainer
import com.roa.cswstickers.utils.StickerPacksManager
import com.roa.cswstickers.R

class AddToStickerPackActivity : AppCompatActivity() {

    private lateinit var stickerUri: Uri

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_to_sticker_pack)
        stickerUri = intent.data!!
        StickerPacksManager.stickerPacksContainer = StickerPacksContainer("", "",
            StickerPacksManager.getStickerPacks(this).toMutableList()
        )
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        Toast.makeText(this, stickerUri.path, Toast.LENGTH_LONG).show()
    }
}
