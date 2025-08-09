package com.sliteptyltd.slite.utils.handlers

import android.app.Dialog
import android.content.Context
import android.graphics.drawable.ColorDrawable
import android.view.inputmethod.EditorInfo
import androidx.core.view.isVisible
import androidx.core.widget.doOnTextChanged
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomsheet.BottomSheetBehavior.STATE_EXPANDED
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.sliteptyltd.slite.BuildConfig.VERSION_NAME
import com.sliteptyltd.slite.R
import com.sliteptyltd.slite.databinding.DialogAppUpdateNoticeBinding
import com.sliteptyltd.slite.databinding.DialogDisconnectedInfoReasoningBinding
import com.sliteptyltd.slite.databinding.DialogInputColorHexBinding
import com.sliteptyltd.slite.databinding.DialogLightActionConfirmationBinding
import com.sliteptyltd.slite.databinding.DialogNameSelectedSliteBinding
import com.sliteptyltd.slite.databinding.DialogPermissionsRationaleBinding
import com.sliteptyltd.slite.databinding.DialogSceneCreationRequirementsBinding
import com.sliteptyltd.slite.databinding.DialogSupportBinding
import com.sliteptyltd.slite.feature.lights.containingscenes.ContainingScene
import com.sliteptyltd.slite.feature.lights.containingscenes.ContainingScenesAdapter
import com.sliteptyltd.slite.utils.Constants.ColorUtils.COLOR_HEX_PREFIX
import com.sliteptyltd.slite.utils.Constants.ColorUtils.COLOR_HEX_REGEX
import com.sliteptyltd.slite.utils.Constants.Lights.LIGHT_NAME_MAX_LENGTH
import com.sliteptyltd.slite.utils.Constants.Lights.LIGHT_NAME_MIN_LENGTH
import com.sliteptyltd.slite.utils.Constants.Support.PRIVACY_POLICY_URL
import com.sliteptyltd.slite.utils.Constants.Support.TERMS_OF_USE_URL
import com.sliteptyltd.slite.utils.extensions.layoutInflater
import com.sliteptyltd.slite.utils.extensions.openPlayStorePage
import com.sliteptyltd.slite.utils.extensions.views.setOpenEmailSpan
import com.sliteptyltd.slite.utils.extensions.views.setOutlineColorForInputStatus
import com.sliteptyltd.slite.utils.extensions.views.setSupportUrlSpan
import com.sliteptyltd.slite.utils.extensions.views.setUpdateFirmwareSpan
import java.lang.Integer.min

class DialogHandler {

    fun showNameLightDialog(
        context: Context,
        defaultSliteName: String = "",
        dialogTitle: String,
        confirmButtonText: String,
        dialogDescriptionText: String? = null,
        containingScenes: List<ContainingScene> = listOf(),
        onNameConfirmed: (sliteName: String) -> Unit,
        onDismiss: (() -> Unit)? = null
    ) {
        val dialog = BottomSheetDialog(context)
        val dialogBinding = DialogNameSelectedSliteBinding.inflate(context.layoutInflater, null, false)

        dialogBinding.dialogTitleTV.text = dialogTitle
        dialogBinding.confirmBtn.text = confirmButtonText
        dialogBinding.dialogDescriptionTV.isVisible = !dialogDescriptionText.isNullOrEmpty()
        dialogBinding.dialogDescriptionTV.text = dialogDescriptionText

        dialogBinding.confirmBtn.setOnClickListener {
            onNameConfirmed(dialogBinding.sliteNameTIET.text.toString().trim())
            dialog.dismiss()
        }

        dialogBinding.sliteNameTIET.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                val inputSliteName = dialogBinding.sliteNameTIET.text?.toString()?.trim()
                if (inputSliteName == null || inputSliteName.length < LIGHT_NAME_MIN_LENGTH) {
                    dialogBinding.sliteNameTIL.setOutlineColorForInputStatus(false)
                    return@setOnEditorActionListener true
                }
                onNameConfirmed(inputSliteName)
                dialog.dismiss()
            }
            false
        }

        dialogBinding.sliteNameTIET.apply {
            doOnTextChanged { text, _, _, _ ->
                val isSliteNameValid = text?.trim() != null && text.trim().length >= LIGHT_NAME_MIN_LENGTH
                dialogBinding.confirmBtn.isEnabled = isSliteNameValid

                if (isSliteNameValid) {
                    dialogBinding.sliteNameTIL.setOutlineColorForInputStatus(true)
                }
            }
            val inputText = if (defaultSliteName.length > LIGHT_NAME_MAX_LENGTH) {
                defaultSliteName.substring(0, LIGHT_NAME_MAX_LENGTH)
            } else {
                defaultSliteName
            }
            setText(inputText)
            requestFocus()
            setSelection(min(defaultSliteName.length, LIGHT_NAME_MAX_LENGTH))
        }

        if (containingScenes.isNotEmpty()) {
            dialogBinding.containingScenesList.isVisible = true
            dialogBinding.containingScenesList.layoutManager = LinearLayoutManager(context)
            dialogBinding.containingScenesList.adapter = ContainingScenesAdapter().apply {
                submitList(containingScenes)
            }
        }

        dialog.setOnDismissListener { onDismiss?.invoke() }
        dialog.behavior.state = STATE_EXPANDED
        dialog.setContentView(dialogBinding.root)
        dialog.show()
    }

    fun showLightActionsConfirmationDialog(
        context: Context,
        dialogTitle: String,
        dialogDescriptionText: String,
        confirmButtonText: String,
        containingScenes: List<ContainingScene> = listOf(),
        onConfirmAction: () -> Unit
    ) {
        val dialog = BottomSheetDialog(context)
        val dialogBinding = DialogLightActionConfirmationBinding.inflate(context.layoutInflater, null, false)

        dialogBinding.confirmBtn.setOnClickListener {
            onConfirmAction()
            dialog.dismiss()
        }
        dialogBinding.cancelBtn.setOnClickListener { dialog.dismiss() }

        dialogBinding.removeLightTitleTV.text = dialogTitle
        dialogBinding.removeLightSubtitleTV.text = dialogDescriptionText
        dialogBinding.confirmBtn.text = confirmButtonText

        if (containingScenes.isNotEmpty()) {
            dialogBinding.containingScenesList.isVisible = true
            dialogBinding.containingScenesList.layoutManager = LinearLayoutManager(context)
            dialogBinding.containingScenesList.adapter = ContainingScenesAdapter().apply {
                submitList(containingScenes)
            }
        }

        dialog.behavior.state = STATE_EXPANDED
        dialog.setContentView(dialogBinding.root)
        dialog.show()
    }

    fun showDisconnectedLightInfoDialog(context: Context, onReconnectButtonPressed: () -> Unit) {
        val dialog = BottomSheetDialog(context)
        val dialogBinding = DialogDisconnectedInfoReasoningBinding.inflate(context.layoutInflater, null, false)

        dialogBinding.reconnectBtn.setOnClickListener {
            onReconnectButtonPressed()
            dialog.dismiss()
        }

        dialog.behavior.state = STATE_EXPANDED
        dialog.setContentView(dialogBinding.root)
        dialog.show()
    }

    fun showColorHexInputDialog(
        context: Context,
        currentHexValue: String,
        onConfirmHexValue: (inputColorHex: String) -> Unit
    ) {
        val dialog = BottomSheetDialog(context)
        val dialogBinding = DialogInputColorHexBinding.inflate(context.layoutInflater, null, false)

        dialogBinding.colorHexTIET.apply {
            setOnEditorActionListener { _, actionId, _ ->
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    val inputColorHex = dialogBinding.colorHexTIET.text?.toString()?.trim()
                    if (inputColorHex.isNullOrEmpty() ||
                        inputColorHex.isNullOrBlank() ||
                        text?.trim()?.matches(COLOR_HEX_REGEX.toRegex()) != true
                    ) {
                        dialogBinding.colorHexTIL.setOutlineColorForInputStatus(false)
                        return@setOnEditorActionListener true
                    }
                    onConfirmHexValue(COLOR_HEX_PREFIX + inputColorHex)
                    dialog.dismiss()
                }
                false
            }

            doOnTextChanged { text, _, _, _ ->
                val isColorHexValid = !text?.trim().isNullOrEmpty() && (text?.trim()?.matches(COLOR_HEX_REGEX.toRegex()) == true)
                dialogBinding.doneBtn.isEnabled = isColorHexValid

                if (isColorHexValid) {
                    dialogBinding.colorHexTIL.setOutlineColorForInputStatus(true)
                }
            }

            setText(currentHexValue)
            requestFocus()
            setSelection(currentHexValue.length)
        }

        dialogBinding.doneBtn.setOnClickListener {
            onConfirmHexValue(COLOR_HEX_PREFIX + dialogBinding.colorHexTIET.text.toString())
            dialog.dismiss()
        }

        dialog.behavior.state = STATE_EXPANDED
        dialog.setContentView(dialogBinding.root)
        dialog.show()
    }

    fun showPermissionsRationaleDialog(
        context: Context,
        title: String,
        rationale: String,
        onOpenSettingsClick: (() -> Unit)? = null,
        onDismiss: (() -> Unit)? = null
    ) {
        val dialog = Dialog(context)
        val dialogBinding = DialogPermissionsRationaleBinding.inflate(context.layoutInflater, null, false)

        dialogBinding.titleTV.text = title
        dialogBinding.rationaleTV.text = rationale
        dialogBinding.openSettingsBtn.isVisible = onOpenSettingsClick != null
        dialogBinding.openSettingsBtn.setOnClickListener {
            dialog.dismiss()
            onOpenSettingsClick?.invoke()
        }
        dialogBinding.dismissBtn.setOnClickListener {
            dialog.dismiss()
        }

        dialog.setOnDismissListener { onDismiss?.invoke() }
        dialog.window?.setBackgroundDrawable(ColorDrawable(context.resources.getColor(R.color.transparent, null)))
        dialog.setContentView(dialogBinding.root)
        dialog.show()
    }

    fun showSceneCreationRequirementsDialog(context: Context) {
        val dialog = Dialog(context)
        val dialogBinding = DialogSceneCreationRequirementsBinding.inflate(context.layoutInflater, null, false)

        dialogBinding.dismissBtn.setOnClickListener {
            dialog.dismiss()
        }

        dialog.window?.setBackgroundDrawable(ColorDrawable(context.resources.getColor(R.color.transparent, null)))
        dialog.setContentView(dialogBinding.root)
        dialog.show()
    }

    fun showSupportDialog(context: Context, areUpdatesAvailable: Boolean, onEmailClientNotFound: () -> Unit, onOpenUpdateScreen: () -> Unit) {
        val dialog = BottomSheetDialog(context)
        val dialogBinding = DialogSupportBinding.inflate(context.layoutInflater, null, false)

        dialogBinding.privacyPolicyTV.setSupportUrlSpan(context.getString(R.string.support_dialog_privacy_policy), PRIVACY_POLICY_URL)
        dialogBinding.termsOfUseTV.setSupportUrlSpan(context.getString(R.string.support_dialog_terms_of_use), TERMS_OF_USE_URL)
        dialogBinding.contactTV.setOpenEmailSpan(context.getString(R.string.support_dialog_contact)) {
            dialog.dismiss()
            onEmailClientNotFound()
        }

        if (areUpdatesAvailable) {
            dialogBinding.updateAvailableTV.isVisible = true
            dialogBinding.updateAvailableTV.setUpdateFirmwareSpan(context.getString(R.string.support_dialog_update)) {
                dialog.dismiss()
                onOpenUpdateScreen.invoke()
            }
        }

        dialogBinding.appVersionTV.text = context.getString(R.string.support_dialog_app_version_text_format, VERSION_NAME)

        dialog.setContentView(dialogBinding.root)
        dialog.show()
    }

    fun showAppUpdateNoticeDialog(context: Context, isMandatory: Boolean, onDismiss: (() -> Unit)) {
        val dialog = Dialog(context)
        val dialogBinding = DialogAppUpdateNoticeBinding.inflate(context.layoutInflater, null, false)

        val dialogTitle: Int
        val dialogDescription: Int

        if (isMandatory) {
            dialogTitle = R.string.app_update_mandatory_install_title
            dialogDescription = R.string.app_update_mandatory_install_description
        } else {
            dialogTitle = R.string.app_update_recommended_install_title
            dialogDescription = R.string.app_update_recommended_install_description
            dialogBinding.dismissBtn.setOnClickListener {
                dialog.dismiss()
            }
        }

        dialogBinding.appUpdateDialogTitleTV.text = context.getString(dialogTitle)
        dialogBinding.appUpdateDialogDescriptionTV.text = context.getString(dialogDescription)
        dialogBinding.dismissBtn.isVisible = !isMandatory
        dialogBinding.installBtn.setOnClickListener {
            if (!isMandatory) onDismiss()
            context.openPlayStorePage()
        }

        dialog.window?.setBackgroundDrawable(ColorDrawable(context.resources.getColor(R.color.transparent, null)))
        dialog.setContentView(dialogBinding.root)
        dialog.setCancelable(!isMandatory)
        dialog.setCanceledOnTouchOutside(!isMandatory)
        dialog.setOnDismissListener {
            if (!isMandatory) onDismiss()
        }
        dialog.show()
    }
}