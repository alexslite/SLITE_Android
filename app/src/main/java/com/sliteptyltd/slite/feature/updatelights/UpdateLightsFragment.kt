package com.sliteptyltd.slite.feature.updatelights

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updateLayoutParams
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import com.sliteptyltd.slite.views.composables.SliteTheme
import org.koin.androidx.viewmodel.ext.android.viewModel

class UpdateLightsFragment : Fragment() {

    private val viewModel by viewModel<UpdateLightsViewModel>()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return ComposeView(requireContext()).apply {
            setContent {
                SliteTheme {
                    UpdateLightsScreen(viewModel) {
                        findNavController().popBackStack()
                    }
                }
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        handleSystemBarsOverlaps(view)
        viewModel.doOnInit()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        viewModel.doOnDestroy()
    }

    private fun handleSystemBarsOverlaps(rootView: View) {
        ViewCompat.setOnApplyWindowInsetsListener(rootView) { _, windowInsets ->
            val insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars())

            rootView.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                topMargin = insets.top
                bottomMargin = insets.bottom
            }

            WindowInsetsCompat.CONSUMED
        }
    }
}