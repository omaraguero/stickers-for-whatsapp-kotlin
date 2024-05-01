package com.roa.cswstickers.whatsapp_api

import android.app.Activity
import android.app.Dialog
import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import com.roa.cswstickers.BuildConfig
import com.roa.cswstickers.R

abstract class AddStickerPackActivity : BaseActivity() {
    companion object {
        private const val TAG = "AddStickerPackActivity"
    }

    fun addStickerPackToWhatsApp(identifier: String, stickerPackName: String) {
        try {

            val packageManager = packageManager

            if (!WhitelistCheck.isWhatsAppConsumerAppInstalled(packageManager) && !WhitelistCheck.isWhatsAppSmbAppInstalled(packageManager)) {
                Toast.makeText(this, R.string.add_pack_fail_prompt_update_whatsapp, Toast.LENGTH_LONG).show()
                return
            }

            val stickerPackWhitelistedInWhatsAppConsumer = WhitelistCheck.isStickerPackWhitelistedInWhatsAppConsumer(this, identifier)
            val stickerPackWhitelistedInWhatsAppSmb = WhitelistCheck.isStickerPackWhitelistedInWhatsAppSmb(this, identifier)

            if (!stickerPackWhitelistedInWhatsAppConsumer && !stickerPackWhitelistedInWhatsAppSmb) {

                launchIntentToAddPackToChooser(identifier, stickerPackName)

            } else if (!stickerPackWhitelistedInWhatsAppConsumer) {

                launchIntentToAddPackToSpecificPackage(identifier, stickerPackName, WhitelistCheck.CONSUMER_WHATSAPP_PACKAGE_NAME)

            } else if (!stickerPackWhitelistedInWhatsAppSmb) {
                launchIntentToAddPackToSpecificPackage(identifier, stickerPackName, WhitelistCheck.SMB_WHATSAPP_PACKAGE_NAME)

            } else {
                Toast.makeText(this, R.string.add_pack_fail_prompt_update_whatsapp, Toast.LENGTH_LONG).show()
            }
        } catch (e: Exception) {
            Toast.makeText(this, R.string.add_pack_fail_prompt_update_whatsapp, Toast.LENGTH_LONG).show()
        }
    }

    private fun launchIntentToAddPackToSpecificPackage(identifier: String, stickerPackName: String, whatsappPackageName: String) {
        val intent = createIntentToAddStickerPack(identifier, stickerPackName)
        intent.`package` = whatsappPackageName

        try {
            addPackLauncher.launch(intent)
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(this, R.string.add_pack_fail_prompt_update_whatsapp, Toast.LENGTH_LONG).show()
        }
    }

//ES DONDE FALLA AL HACER FETCH
    private val addPackLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->

        if (result.resultCode == Activity.RESULT_CANCELED) {
            val data = result.data
            if (data != null) {
                val validationError = data.getStringExtra("validation_error")
                if (validationError != null) {
                    if (BuildConfig.DEBUG) {
                        MessageDialogFragment.newInstance(R.string.title_validation_error, validationError).show(supportFragmentManager, "validation error")
                    }
                }
            } else {
                StickerPackNotAddedMessageFragment().show(supportFragmentManager, "sticker_pack_not_added")
            }
        }
    }


    private fun launchIntentToAddPackToChooser(identifier: String, stickerPackName: String) {
        val intent = createIntentToAddStickerPack(identifier, stickerPackName)
        try {
            chooserLauncher.launch(Intent.createChooser(intent, getString(R.string.add_to_whatsapp)))
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(this, R.string.add_pack_fail_prompt_update_whatsapp, Toast.LENGTH_LONG).show()
        }
    }

    private val chooserLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_CANCELED) {
            val data = result.data
            if (data != null) {
                val validationError = data.getStringExtra("validation_error")
                if (validationError != null) {
                    if (BuildConfig.DEBUG) {
                        MessageDialogFragment.newInstance(R.string.title_validation_error, validationError).show(supportFragmentManager, "validation error")
                    }
                    Log.e(TAG, "Validation failed:$validationError")
                }
            } else {
                StickerPackNotAddedMessageFragment().show(supportFragmentManager, "sticker_pack_not_added")
            }
        }
    }


    private fun createIntentToAddStickerPack(identifier: String, stickerPackName: String): Intent {
        val intent = Intent()
        intent.action = "com.whatsapp.intent.action.ENABLE_STICKER_PACK"
        intent.putExtra(StickerPackDetailsActivity.EXTRA_STICKER_PACK_ID, identifier)
        intent.putExtra(StickerPackDetailsActivity.EXTRA_STICKER_PACK_AUTHORITY, BuildConfig.CONTENT_PROVIDER_AUTHORITY)
        intent.putExtra(StickerPackDetailsActivity.EXTRA_STICKER_PACK_NAME, stickerPackName)
        return intent
    }


    class StickerPackNotAddedMessageFragment : DialogFragment() {
        override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
            val dialogBuilder = AlertDialog.Builder(requireActivity())
                .setMessage(R.string.add_pack_fail_prompt_update_whatsapp)
                .setCancelable(true)
                .setPositiveButton(android.R.string.ok) { dialog, which -> dismiss() }
                .setNeutralButton(R.string.add_pack_fail_prompt_update_play_link) { dialog, which -> launchWhatsAppPlayStorePage() }

            return dialogBuilder.create()
        }

        private fun launchWhatsAppPlayStorePage() {
            activity?.let { activity ->
                val packageManager = activity.packageManager
                val whatsAppInstalled = WhitelistCheck.isPackageInstalled(WhitelistCheck.CONSUMER_WHATSAPP_PACKAGE_NAME, packageManager)
                val smbAppInstalled = WhitelistCheck.isPackageInstalled(WhitelistCheck.SMB_WHATSAPP_PACKAGE_NAME, packageManager)
                val playPackageLinkPrefix = "http://play.google.com/store/apps/details?id="
                when {
                    whatsAppInstalled && smbAppInstalled -> launchPlayStoreWithUri("https://play.google.com/store/apps/developer?id=WhatsApp+LLC")
                    whatsAppInstalled -> launchPlayStoreWithUri(playPackageLinkPrefix + WhitelistCheck.CONSUMER_WHATSAPP_PACKAGE_NAME)
                    smbAppInstalled -> launchPlayStoreWithUri(playPackageLinkPrefix + WhitelistCheck.SMB_WHATSAPP_PACKAGE_NAME)
                }
            }
        }

        private fun launchPlayStoreWithUri(uriString: String) {
            val intent = Intent(Intent.ACTION_VIEW)
            intent.data = Uri.parse(uriString)
            intent.`package` = "com.android.vending"
            try {
                startActivity(intent)
            } catch (e: ActivityNotFoundException) {
                Toast.makeText(activity, R.string.cannot_find_play_store, Toast.LENGTH_LONG).show()
            }
        }
    }
}


