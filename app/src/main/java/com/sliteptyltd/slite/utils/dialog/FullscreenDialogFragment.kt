package com.sliteptyltd.slite.utils.dialog

import android.app.Dialog
import android.os.Bundle
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.Window.FEATURE_NO_TITLE
import androidx.annotation.LayoutRes
import androidx.core.view.WindowCompat
import androidx.fragment.app.DialogFragment
import com.sliteptyltd.slite.R

open class FullscreenDialogFragment(
    @LayoutRes dialogLayout: Int
) : DialogFragment(dialogLayout) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.FullScreenDialog)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog =
        super.onCreateDialog(savedInstanceState).apply {
            requestWindowFeature(FEATURE_NO_TITLE)
            window?.let { dialogWindow ->
                dialogWindow.setLayout(MATCH_PARENT, MATCH_PARENT)
                dialogWindow.attributes.windowAnimations = R.style.FullScreenDialog_Animation
                WindowCompat.setDecorFitsSystemWindows(dialogWindow, false)
            }
        }
}