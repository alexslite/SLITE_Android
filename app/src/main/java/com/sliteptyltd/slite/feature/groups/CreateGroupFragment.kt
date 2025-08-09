package com.sliteptyltd.slite.feature.groups

import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsCompat.CONSUMED
import androidx.core.view.updateLayoutParams
import androidx.fragment.app.Fragment
import com.sliteptyltd.slite.R
import com.sliteptyltd.slite.analytics.AnalyticsService
import com.sliteptyltd.slite.data.preference.InternalStorageManager
import com.sliteptyltd.slite.databinding.FragmentCreateGroupBinding
import com.sliteptyltd.slite.utils.extensions.navigateUp
import com.sliteptyltd.slite.utils.handlers.DialogHandler
import com.sliteptyltd.slite.views.composables.SliteTheme
import com.zhuinden.fragmentviewbindingdelegatekt.viewBinding
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel

class CreateGroupFragment : Fragment(R.layout.fragment_create_group) {

    private val binding by viewBinding(FragmentCreateGroupBinding::bind)
    private val viewModel by viewModel<CreateGroupViewModel>()
    private val internalStorageManager by inject<InternalStorageManager>()
    private val dialogHandler by inject<DialogHandler>()
    private val analyticsService by inject<AnalyticsService>()
    private var lightsList by mutableStateOf<List<AvailableLightItem>>(listOf())

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.root.setContent {
            SliteTheme {
                AvailableLightsScreen(
                    availableLights = lightsList,
                    onLightClick = ::onLightClick,
                    onCreateGroupClick = ::onCreateGroupClick,
                    onBackPressed = ::navigateUp
                )
            }
        }

        handleSystemBarsOverlaps()
        initObservers()
    }

    private fun onLightClick(lightId: Int) {
        viewModel.changeLightSelectionState(lightId)
    }

    private fun onCreateGroupClick() {
        val scenesContainingGroupedLights = viewModel.getScenesContainingGroupLights()
        val dialogTitle: String
        val dialogDescription: String?
        val confirmationButtonText: String
        if (scenesContainingGroupedLights.isEmpty()) {
            dialogTitle = getString(R.string.create_group_name_dialog_title)
            dialogDescription = null
            confirmationButtonText = getString(R.string.create_group_name_dialog_confirm_button_text)
        } else {
            dialogTitle = getString(R.string.create_group_delete_containing_scenes_dialog_title)
            dialogDescription = getString(R.string.create_group_delete_containing_scenes_dialog_description)
            confirmationButtonText = getString(R.string.create_group_delete_containing_scenes_dialog_confirm_button_text)
        }
        dialogHandler.showNameLightDialog(
            requireContext(),
            dialogTitle = dialogTitle,
            dialogDescriptionText = dialogDescription,
            confirmButtonText = confirmationButtonText,
            containingScenes = scenesContainingGroupedLights,
            onNameConfirmed = { groupName ->
                analyticsService.trackCreateGroup()
                internalStorageManager.newlyCreatedGroupId = viewModel.createNewGroup(groupName)
                navigateUp()
            }
        )
    }

    private fun initObservers() {
        viewModel.availableLights.observe(viewLifecycleOwner) { availableLights ->
            availableLights ?: return@observe
            lightsList = availableLights
        }
    }

    private fun handleSystemBarsOverlaps() {
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { _, windowInsets ->
            val insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars())

            binding.root.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                topMargin = insets.top
                bottomMargin = insets.bottom
            }
            CONSUMED
        }
    }
}