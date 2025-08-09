package com.sliteptyltd.slite.feature.addcomponent

import android.app.Dialog
import android.os.Bundle
import android.view.KeyEvent
import android.view.KeyEvent.ACTION_DOWN
import android.view.KeyEvent.KEYCODE_BACK
import android.view.View
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsCompat.CONSUMED
import androidx.core.view.updatePadding
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.lifecycleScope
import com.sliteptyltd.slite.R
import com.sliteptyltd.slite.databinding.DialogAddSliteComponentDropdownBinding
import com.sliteptyltd.slite.utils.dialog.FullscreenDialogFragment
import com.sliteptyltd.slite.views.composables.SliteTheme
import com.zhuinden.fragmentviewbindingdelegatekt.viewBinding
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class AddComponentDropdownDialogFragment : FullscreenDialogFragment(R.layout.dialog_add_slite_component_dropdown) {

    private val binding by viewBinding(DialogAddSliteComponentDropdownBinding::bind)
    private var isDropdownOpen: Boolean by mutableStateOf(false)

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog =
        super.onCreateDialog(savedInstanceState).apply {
            setOnKeyListener { _, _, keyEvent -> onKeyPressed(keyEvent) }
        }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        handleSystemBarsOverlaps()
        initListeners()
        setupDropdownContent()
    }

    private fun setupDropdownContent() {
        binding.dropdownContainer.setContent {
            SliteTheme {
                AddComponentDropdown(
                    ::dismissDelayed,
                    ::onAddLightClick,
                    ::onNewGroupClick,
                    ::onSaveSceneClick,
                    arguments.canCreateGroup,
                    arguments.canCreateScene,
                    isDropdownOpen
                )
            }
        }

        binding.dropdownContainer.post {
            isDropdownOpen = true
        }
    }

    private fun initListeners() {
        binding.root.setOnClickListener {
            isDropdownOpen = false
            dismissDelayed()
        }
    }

    private fun handleSystemBarsOverlaps() {
        ViewCompat.setOnApplyWindowInsetsListener(binding.dropdownContainer) { _, windowInsets ->
            val insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars())

            binding.dropdownContainer.updatePadding(top = insets.top)
            CONSUMED
        }
    }

    private fun dismissDelayed(onDismiss: (() -> Unit)? = null) {
        isDropdownOpen = false
        viewLifecycleOwner.lifecycleScope.launch {
            delay(DROPDOWN_ANIMATION_DISMISS_DELAY)

            dismiss()
            onDismiss?.invoke()
        }
    }

    private fun onKeyPressed(keyEvent: KeyEvent?): Boolean =
        if (keyEvent?.keyCode == KEYCODE_BACK && keyEvent.action != ACTION_DOWN) {
            dismissDelayed()
            true
        } else {
            false
        }

    private fun onAddLightClick() {
        dismissDelayed {
            (parentFragment as? AddComponentDropdownActions)?.onAddLightClick()
        }
    }

    private fun onNewGroupClick() {
        (parentFragment as? AddComponentDropdownActions)?.onNewGroupClick()
        dismiss()
    }

    private fun onSaveSceneClick() {
        (parentFragment as? AddComponentDropdownActions)?.onSaveSceneClick()
        dismiss()
    }

    interface AddComponentDropdownActions {

        fun onAddLightClick()

        fun onNewGroupClick()

        fun onSaveSceneClick()
    }

    companion object {
        private const val DROPDOWN_ANIMATION_DISMISS_DELAY = 200L
        private const val TAG_ADD_COMPONENT_DIALOG_DROPDOWN = "TAG_ADD_LIGHTS_TUTORIAL_DIALOG"
        private const val KEY_CAN_CREATE_GROUP = "KEY_CAN_CREATE_GROUP"
        private const val KEY_CAN_CREATE_SCENE = "KEY_CAN_CREATE_SCENE"

        private val Bundle?.canCreateGroup: Boolean get() = this?.getBoolean(KEY_CAN_CREATE_GROUP) ?: false
        private val Bundle?.canCreateScene: Boolean get() = this?.getBoolean(KEY_CAN_CREATE_SCENE) ?: false

        fun show(fragmentManager: FragmentManager, canCreateGroup: Boolean, canCreateScene: Boolean) {
            AddComponentDropdownDialogFragment().apply {
                arguments = Bundle().apply {
                    putBoolean(KEY_CAN_CREATE_GROUP, canCreateGroup)
                    putBoolean(KEY_CAN_CREATE_SCENE, canCreateScene)
                }
                show(fragmentManager, TAG_ADD_COMPONENT_DIALOG_DROPDOWN)
            }
        }
    }
}