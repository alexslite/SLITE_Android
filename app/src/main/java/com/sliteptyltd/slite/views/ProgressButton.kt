package com.sliteptyltd.slite.views

import android.content.Context
import android.content.res.ColorStateList
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.isVisible
import com.sliteptyltd.slite.databinding.ViewProgressButtonBinding

class ProgressButton @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0,
    defStyleRes: Int = 0
) : ConstraintLayout(context, attrs, defStyle, defStyleRes) {

    private var isButtonEnabled: Boolean = true
    private val binding = ViewProgressButtonBinding.inflate(LayoutInflater.from(context), this)
    private var buttonText: String? = null
    val isLoading: Boolean get() = binding.loadingIndicator.isVisible

    fun setButtonClickListener(onClickListener: () -> Unit) {
        binding.button.setOnClickListener {
            onClickListener()
        }
    }

    fun setButtonText(buttonText: String?) {
        this.buttonText = buttonText
        binding.button.text = buttonText
    }

    fun setIsLoading(isLoading: Boolean) {
        binding.button.isEnabled = !isLoading && isButtonEnabled
        binding.loadingIndicator.isVisible = isLoading
        binding.button.alpha = if (isLoading) DISABLED_BUTTON_ALPHA else ENABLED_BUTTON_ALPHA
    }

    fun setIsEnabled(isEnabled: Boolean) {
        isButtonEnabled = isEnabled
        binding.loadingIndicator.isVisible = false
        binding.button.isEnabled = isEnabled
        binding.button.alpha = if (isEnabled) ENABLED_BUTTON_ALPHA else DISABLED_BUTTON_ALPHA
    }

    fun setTextColor(color: Int) {
        binding.button.setTextColor(color)
    }

    fun setButtonBackgroundColorTint(color: Int) {
        binding.button.backgroundTintList = ColorStateList.valueOf(color)
    }

    companion object {
        const val DISABLED_BUTTON_ALPHA = 0.6f
        const val ENABLED_BUTTON_ALPHA = 1f
    }
}