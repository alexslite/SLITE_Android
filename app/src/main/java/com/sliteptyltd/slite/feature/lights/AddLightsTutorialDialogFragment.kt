package com.sliteptyltd.slite.feature.lights

import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import androidx.core.view.updateLayoutParams
import androidx.fragment.app.FragmentManager
import com.sliteptyltd.slite.R
import com.sliteptyltd.slite.databinding.DialogAddLightsTutorialBinding
import com.sliteptyltd.slite.utils.dialog.FullscreenDialogFragment
import com.zhuinden.fragmentviewbindingdelegatekt.viewBinding
import kotlin.math.roundToInt

class AddLightsTutorialDialogFragment : FullscreenDialogFragment(R.layout.dialog_add_lights_tutorial) {

    private val binding by viewBinding(DialogAddLightsTutorialBinding::bind)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adjustAddButtonOverlap()
        initListeners()
    }

    override fun onDestroyView() {
        (parentFragment as? OnTutorialCompletedCallback)?.completeAddLightsTutorial(false)
        super.onDestroyView()
    }

    private fun initListeners() {
        binding.root.setOnClickListener {
            completeAddLightsTutorial()
        }

        binding.instructionAddLightBtn.setOnClickListener {
            completeAddLightsTutorial(true)
        }
    }

    private fun completeAddLightsTutorial(shouldProceedToAddLightsFlow: Boolean = false) {
        (parentFragment as? OnTutorialCompletedCallback)?.completeAddLightsTutorial(shouldProceedToAddLightsFlow)
        dismiss()
    }

    private fun adjustAddButtonOverlap() {
        val addButtonX = arguments.rightMargin
        val addButtonY = arguments.topMargin
        val offset = resources.getDimension(R.dimen.lights_add_tutorial_button_offset).roundToInt()

        binding.instructionAddLightBtn.updateLayoutParams<ViewGroup.MarginLayoutParams> {
            rightMargin = addButtonX - offset
            topMargin = addButtonY - offset
        }
    }

    interface OnTutorialCompletedCallback {

        fun completeAddLightsTutorial(shouldProceedToAddLightsFlow: Boolean)
    }

    companion object {
        private const val TAG_ADD_LIGHTS_TUTORIAL_DIALOG = "TAG_ADD_LIGHTS_TUTORIAL_DIALOG"
        private const val KEY_ADD_BUTTON_RIGHT_MARGIN = "KEY_ADD_BUTTON_RIGHT_MARGIN"
        private const val KEY_ADD_BUTTON_TOP_MARGIN = "KEY_ADD_BUTTON_top_MARGIN"

        private val Bundle?.rightMargin: Int get() = this?.getInt(KEY_ADD_BUTTON_RIGHT_MARGIN) ?: 0
        private val Bundle?.topMargin: Int get() = this?.getInt(KEY_ADD_BUTTON_TOP_MARGIN) ?: 0

        fun show(fragmentManager: FragmentManager, rightMargin: Int, topMargin: Int) {
            AddLightsTutorialDialogFragment().apply {
                arguments = Bundle().apply {
                    putInt(KEY_ADD_BUTTON_RIGHT_MARGIN, rightMargin)
                    putInt(KEY_ADD_BUTTON_TOP_MARGIN, topMargin)
                }
                show(fragmentManager, TAG_ADD_LIGHTS_TUTORIAL_DIALOG)
            }
        }
    }
}