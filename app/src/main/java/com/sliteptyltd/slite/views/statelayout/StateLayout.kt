package com.sliteptyltd.slite.views.statelayout

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.core.view.children
import com.sliteptyltd.slite.databinding.ViewStateLayoutBinding
import androidx.compose.runtime.setValue
import androidx.core.view.isInvisible
import com.sliteptyltd.slite.utils.ComposableContent

class StateLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : FrameLayout(context, attrs) {

    private val binding = ViewStateLayoutBinding.inflate(LayoutInflater.from(context), this)
    private var stateDetails: StateDetails by mutableStateOf(StateDetails.Loading)
    private var onActionButtonClickListener: OnActionButtonClickListener? = null
    private var loadingStateContent by mutableStateOf<ComposableContent?>(null)
    private var successStateContent by mutableStateOf<ComposableContent?>(null)

    init {
        binding.stateInformationLayout.setContent {
            StateDetailsLayout(
                stateDetails = stateDetails,
                onActionButtonClickListener,
                { loadingStateContent?.invoke() },
                { successStateContent?.invoke() }
            )
        }
    }

    fun setState(stateDetails: StateDetails) {
        this.stateDetails = stateDetails
        setContentVisibility(stateDetails is StateDetails.Success)
    }

    fun setOnActionButtonClick(onActionButtonClickListener: OnActionButtonClickListener) {
        this.onActionButtonClickListener = onActionButtonClickListener
    }

    fun setLoadingStateComposable(loadingStateComposable: ComposableContent) {
        this.loadingStateContent = loadingStateComposable
    }

    fun setSuccessStateComposable(successStateComposable: ComposableContent) {
        this.successStateContent = successStateComposable
    }

    private fun setContentVisibility(isContentVisible: Boolean) {
        children.forEach {
            if (it.id != binding.stateInformationLayout.id && it.isInvisible != !isContentVisible) {
                it.isInvisible = !isContentVisible
            }
        }
    }

    fun interface OnActionButtonClickListener {

        operator fun invoke()
    }
}