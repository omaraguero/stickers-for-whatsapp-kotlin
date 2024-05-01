/*
 * Copyright (c) WhatsApp Inc. and its affiliates.
 * All rights reserved.
 *
 * This source code is licensed under the BSD-style license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.roa.cswstickers.whatsapp_api

import android.app.Dialog
import android.os.Bundle
import androidx.annotation.StringRes
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.DialogFragment

abstract class BaseActivity : AppCompatActivity() {
    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }

    class MessageDialogFragment : DialogFragment() {
        companion object {
            private const val ARG_TITLE_ID = "title_id"
            private const val ARG_MESSAGE = "message"

            fun newInstance(@StringRes titleId: Int, message: String): DialogFragment {
                val fragment = MessageDialogFragment()
                val arguments = Bundle().apply {
                    putInt(ARG_TITLE_ID, titleId)
                    putString(ARG_MESSAGE, message)
                }
                fragment.arguments = arguments
                return fragment
            }
        }

        override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
            val title = arguments?.getInt(ARG_TITLE_ID) ?: 0
            val message = arguments?.getString(ARG_MESSAGE)

            val dialogBuilder = AlertDialog.Builder(requireActivity())
                .setMessage(message)
                .setCancelable(true)
                .setPositiveButton(android.R.string.ok) { dialog, which -> dismiss() }

            if (title != 0) {
                dialogBuilder.setTitle(title)
            }
            return dialogBuilder.create()
        }
    }
}
