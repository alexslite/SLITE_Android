package com.sliteptyltd.slite.feature.lightconfiguration.photosensitivity

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.MarginLayoutParams
import androidx.activity.OnBackPressedCallback
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.ComposeView
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsCompat.CONSUMED
import androidx.core.view.updateLayoutParams
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.sliteptyltd.slite.R
import com.sliteptyltd.slite.data.model.light.LightConfigurationMode
import com.sliteptyltd.slite.data.preference.InternalStorageManager
import com.sliteptyltd.slite.utils.Constants.Connectivity.LIGHT_CONFIGURATION_UPDATE_THROTTLE
import com.sliteptyltd.slite.utils.bluetooth.BluetoothService
import com.sliteptyltd.slite.views.composables.SliteTheme
import com.sliteptyltd.slite.views.statelayout.ErrorStateLayout
import com.sliteptyltd.slite.views.statelayout.StateDetails
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel

class PhotoSensitivityWarningFragment : Fragment() {

    private val viewModel by viewModel<PhotoSensitivityViewModel>()
    private val internalStorageManager by inject<InternalStorageManager>()
    private val args by navArgs<PhotoSensitivityWarningFragmentArgs>()
    private val bluetoothService by inject<BluetoothService>()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View =
        ComposeView(requireContext()).apply {
            setContent { PhotoSensitivityWarning() }
        }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        handleSystemBarsOverlaps(view)

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() = navigateToLightConfigurationFragment()
        })
    }

    @Composable
    private fun PhotoSensitivityWarning() {
        SliteTheme {
            ErrorStateLayout(
                StateDetails.Error(
                    drawable = R.drawable.ic_photo_sensitivity,
                    subtitle = R.string.photo_sensitivity_warning_description,
                    buttonText = R.string.photo_sensitivity_acknowledgement_button_text
                ),
                ::onPhotoSensitivityWarningAcknowledged
            )
        }
    }

    private fun onPhotoSensitivityWarningAcknowledged() {
        viewLifecycleOwner.lifecycleScope.launch {
            args.lightAddress?.let {
                bluetoothService.setSliteConfigurationMode(it, LightConfigurationMode.EFFECTS)
            }
            delay(LIGHT_CONFIGURATION_UPDATE_THROTTLE)
            internalStorageManager.hasUserAcknowledgedPhotoSensitivityWarning = true
            viewModel.setLightEffectsMode(args.selectedLightId, args.lightConfiguration)
            navigateToLightConfigurationFragment()
        }
    }

    private fun navigateToLightConfigurationFragment() {
        findNavController().navigate(
            PhotoSensitivityWarningFragmentDirections.actionPhotoSensitivityWarningFragmentToLightConfigurationDialogFragment(
                args.selectedLightId
            )
        )
    }

    private fun handleSystemBarsOverlaps(rootView: View) {
        ViewCompat.setOnApplyWindowInsetsListener(rootView) { _, windowInsets ->
            val insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars())

            rootView.updateLayoutParams<MarginLayoutParams> {
                topMargin = insets.top
                bottomMargin = insets.bottom
            }

            CONSUMED
        }
    }
}