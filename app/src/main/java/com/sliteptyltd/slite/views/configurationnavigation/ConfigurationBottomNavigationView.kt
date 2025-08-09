package com.sliteptyltd.slite.views.configurationnavigation

import android.content.Context
import android.content.res.ColorStateList
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.animation.AnticipateOvershootInterpolator
import android.widget.Button
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.constraintlayout.widget.ConstraintSet.TOP
import androidx.constraintlayout.widget.ConstraintSet.START
import androidx.constraintlayout.widget.ConstraintSet.END
import androidx.constraintlayout.widget.ConstraintSet.PARENT_ID
import androidx.transition.ChangeBounds
import androidx.transition.Transition
import androidx.transition.TransitionManager
import com.google.android.material.button.MaterialButton
import com.sliteptyltd.slite.R
import com.sliteptyltd.slite.databinding.ViewLightConfigurationNavigationBarBinding
import com.sliteptyltd.slite.data.model.light.LightConfigurationMode
import com.sliteptyltd.slite.data.model.light.LightConfigurationMode.WHITE
import com.sliteptyltd.slite.data.model.light.LightConfigurationMode.COLORS
import com.sliteptyltd.slite.data.model.light.LightConfigurationMode.EFFECTS
import com.sliteptyltd.slite.utils.Constants.BottomNavigation.CONSTRAINT_SET_CHANGE_INTERPOLATOR_TENSION

class ConfigurationBottomNavigationView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : ConstraintLayout(context, attrs) {

    private val binding = ViewLightConfigurationNavigationBarBinding.inflate(LayoutInflater.from(context), this)
    private var onConfigurationSelectedListener: OnConfigurationSelectedListener? = null
    private var selectedConfigurationView: TextView = binding.whiteConfigBtn

    init {
        binding.whiteConfigBtn.setOnClickListener {
            onConfigurationSelectedListener?.invoke(WHITE)
            changeConfigurationTabsStates(binding.whiteConfigBtn)
        }

        binding.colorConfigBtn.setOnClickListener {
            onConfigurationSelectedListener?.invoke(COLORS)
            changeConfigurationTabsStates(binding.colorConfigBtn)

        }

        binding.effectsConfigBtn.setOnClickListener {
            onConfigurationSelectedListener?.invoke(EFFECTS)
            changeConfigurationTabsStates(binding.effectsConfigBtn)
        }
    }

    fun selectConfigurationTab(lightConfigurationMode: LightConfigurationMode, shouldAnimate: Boolean = true) {
        val newlySelectedConfigurationTab = when (lightConfigurationMode) {
            WHITE -> binding.whiteConfigBtn
            COLORS -> binding.colorConfigBtn
            EFFECTS -> binding.effectsConfigBtn
        }
        changeConfigurationTabsStates(newlySelectedConfigurationTab, shouldAnimate)
    }

    private fun changeConfigurationTabsStates(newlySelectedConfigurationTab: Button, shouldAnimate: Boolean = true) {
        changeIndicatorPosition(newlySelectedConfigurationTab.id, shouldAnimate)

        val selectedTint = resources.getColor(R.color.bottom_navigation_selected_item_tint, null)
        newlySelectedConfigurationTab.setTextColor(selectedTint)
        (newlySelectedConfigurationTab as? MaterialButton)?.iconTint = ColorStateList.valueOf(selectedTint)
        newlySelectedConfigurationTab.isEnabled = false

        if (selectedConfigurationView.id == newlySelectedConfigurationTab.id) return

        val unselectedTint = resources.getColor(R.color.bottom_navigation_unselected_item_tint, null)
        selectedConfigurationView.setTextColor(unselectedTint)
        (selectedConfigurationView as? MaterialButton)?.iconTint = ColorStateList.valueOf(unselectedTint)
        selectedConfigurationView.isEnabled = true

        selectedConfigurationView = newlySelectedConfigurationTab
    }

    private fun changeIndicatorPosition(selectedButtonId: Int, shouldAnimate: Boolean = true) {
        val constraintSet = ConstraintSet().apply { clone(this) }
        constraintSet.clone(this)

        constraintSet.connect(selectedConfigurationView.id, TOP, PARENT_ID, TOP)

        constraintSet.connect(binding.selectedConfigurationIndicatorIV.id, START, selectedButtonId, START)
        constraintSet.connect(binding.selectedConfigurationIndicatorIV.id, END, selectedButtonId, END)

        if (shouldAnimate) {
            TransitionManager.beginDelayedTransition(binding.root as ConstraintLayout, getChangeBoundsTransition())
        }
        constraintSet.applyTo(binding.root as ConstraintLayout)
    }

    fun setOnConfigurationSelectedListener(onConfigurationSelectedListener: OnConfigurationSelectedListener) {
        this.onConfigurationSelectedListener = onConfigurationSelectedListener
    }

    private fun getChangeBoundsTransition(): Transition =
        ChangeBounds().apply {
            interpolator = AnticipateOvershootInterpolator(CONSTRAINT_SET_CHANGE_INTERPOLATOR_TENSION)
            duration = CONSTRAINT_SET_CHANGE_ANIMATION_DURATION
        }

    fun interface OnConfigurationSelectedListener {

        operator fun invoke(selectedConfiguration: LightConfigurationMode)
    }

    companion object {
        private const val CONSTRAINT_SET_CHANGE_ANIMATION_DURATION = 220L
    }
}