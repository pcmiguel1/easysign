package com.pcmiguel.easysign.libraries

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.LayoutInflater
import com.pcmiguel.easysign.R

class LoadingDialog(
    val context: Context
) {

    private lateinit var dialog: AlertDialog

    fun startLoading(onCancelListener: DialogInterface.OnCancelListener?= null) {

        val mDialogView = LayoutInflater.from(context).inflate(R.layout.popup_loading, null)
        val mBuilder = android.app.AlertDialog.Builder(context)
            .setView(mDialogView)
            .setCancelable(false)
        dialog = mBuilder.create()
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        onCancelListener?.apply {
            dialog.setOnCancelListener {
                onCancelListener.onCancel(dialog)
            }
        }
        dialog.show()
        dialog.window?.setLayout(250, 250)

    }

    fun isDismiss() {
        dialog.dismiss()
    }

    fun isShowing() : Boolean {
        return dialog.isShowing
    }

}