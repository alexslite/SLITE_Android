package com.sliteptyltd.slite.views.indicatornavigation

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.animation.AccelerateInterpolator
import android.view.animation.AnticipateOvershootInterpolator
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.constraintlayout.widget.ConstraintSet.BOTTOM
import androidx.constraintlayout.widget.ConstraintSet.END
import androidx.constraintlayout.widget.ConstraintSet.START
import androidx.constraintlayout.widget.ConstraintSet.TOP
import androidx.transition.ChangeBounds
import androidx.transition.Transition
import androidx.transition.TransitionManager
import com.sliteptyltd.slite.R
import com.sliteptyltd.slite.databinding.ViewSliteBottomNavigationBarBinding
import com.sliteptyltd.slite.utils.Constants.BottomNavigation.CONSTRAINT_SET_CHANGE_INTERPOLATOR_TENSION
import com.sliteptyltd.slite.utils.extensions.views.OnAnimationStepAction
import com.sliteptyltd.slite.utils.extensions.views.animateInvisible
import com.sliteptyltd.slite.views.indicatornavigation.BottomNavigationTab.LIGHTS
import com.sliteptyltd.slite.views.indicatornavigation.BottomNavigationTab.SCENES

class IndicatorBottomNavigationView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : ConstraintLayout(context, attrs) {

    private val binding = ViewSliteBottomNavigationBarBinding.inflate(LayoutInflater.from(context), this)
    private val indicatorLightsConstraintSet by lazy { initTabIndicatorLightsConstraintSet() }
    private val indicatorScenesConstraintSet by lazy { initTabIndicatorScenesConstraintSet() }
    private var onTabSelectedListener: OnTabSelectedListener? = null
    var selectedTab: BottomNavigationTab = LIGHTS
        private set

    init {
        binding.lightsTabIconIB.setOnClickListener {
            onTabSelectedListener?.invoke(LIGHTS)
        }

        binding.scenesTabIconIB.setOnClickListener {
            onTabSelectedListener?.invoke(SCENES)
        }
    }

    fun selectTab(selectedTab: BottomNavigationTab) {
        if (this.selectedTab == selectedTab) return

        this.selectedTab = selectedTab
        when (selectedTab) {
            LIGHTS -> selectLightsTab()
            SCENES -> selectScenesTab()
        }
    }

    private fun selectLightsTab() {
        updateLightsTabState(true)
        updateScenesTabState(false)
        animateIndicatorToLightsConstraints()
    }

    private fun selectScenesTab() {
        updateScenesTabState(true)
        updateLightsTabState(false)
        animateIndicatorToScenesConstraints()
    }

    private fun updateLightsTabState(isSelected: Boolean) {
        binding.lightsTabTitleTV.animateTabTextInvisible(isSelected) {
            binding.lightsTabIconIB.setColorFilter(getTabColor(isSelected))
        }
    }

    private fun updateScenesTabState(isSelected: Boolean) {
        binding.scenesTabTitleTV.animateTabTextInvisible(isSelected) {
            binding.scenesTabIconIB.setColorFilter(getTabColor(isSelected))
        }
    }

    private fun getTabColor(isSelected: Boolean): Int {
        val colorRes = if (isSelected) {
            R.color.bottom_navigation_selected_item_tint
        } else {
            R.color.bottom_navigation_unselected_item_tint
        }

        return resources.getColor(colorRes, null)
    }

    fun setOnTabSelectedListener(onTabSelectedListener: OnTabSelectedListener) {
        this.onTabSelectedListener = onTabSelectedListener
    }

    private fun animateIndicatorToScenesConstraints() {
        TransitionManager.beginDelayedTransition(binding.root as ConstraintLayout, getChangeBoundsTransition())
        indicatorScenesConstraintSet.applyTo(binding.root as ConstraintLayout)
    }

    private fun animateIndicatorToLightsConstraints() {
        TransitionManager.beginDelayedTransition(binding.root as ConstraintLayout, getChangeBoundsTransition())
        indicatorLightsConstraintSet.applyTo(binding.root as ConstraintLayout)
    }

    private fun getChangeBoundsTransition(): Transition =
        ChangeBounds().apply {
            interpolator = AnticipateOvershootInterpolator(CONSTRAINT_SET_CHANGE_INTERPOLATOR_TENSION)
            duration = CONSTRAINT_SET_CHANGE_ANIMATION_DURATION
        }

    private fun initTabIndicatorLightsConstraintSet(): ConstraintSet =
        ConstraintSet().apply {
            connect(R.id.selectedTabIndicatorV, START, R.id.lightsTabTitleTV, START)
            connect(R.id.selectedTabIndicatorV, END, R.id.lightsTabTitleTV, END)
            connect(R.id.selectedTabIndicatorV, TOP, R.id.lightsTabTitleTV, TOP)
            connect(R.id.selectedTabIndicatorV, BOTTOM, R.id.lightsTabTitleTV, BOTTOM)
            constrainWidth(
                R.id.selectedTabIndicatorV,
                resources.getDimension(R.dimen.bottom_navigation_selected_tab_indicator_size).toInt()
            )
            constrainHeight(
                R.id.selectedTabIndicatorV,
                resources.getDimension(R.dimen.bottom_navigation_selected_tab_indicator_size).toInt()
            )
        }

    private fun initTabIndicatorScenesConstraintSet(): ConstraintSet =
        ConstraintSet().apply {
            connect(R.id.selectedTabIndicatorV, START, R.id.scenesTabTitleTV, START)
            connect(R.id.selectedTabIndicatorV, END, R.id.scenesTabTitleTV, END)
            connect(R.id.selectedTabIndicatorV, TOP, R.id.scenesTabTitleTV, TOP)
            connect(R.id.selectedTabIndicatorV, BOTTOM, R.id.scenesTabTitleTV, BOTTOM)
            constrainWidth(
                R.id.selectedTabIndicatorV,
                resources.getDimension(R.dimen.bottom_navigation_selected_tab_indicator_size).toInt()
            )
            constrainHeight(
                R.id.selectedTabIndicatorV,
                resources.getDimension(R.dimen.bottom_navigation_selected_tab_indicator_size).toInt()
            )
        }

    fun interface OnTabSelectedListener {

        operator fun invoke(selectedTab: BottomNavigationTab)
    }

    companion object {

        private const val VISIBILITY_ANIMATION_DURATION = 200L
        private const val CONSTRAINT_SET_CHANGE_ANIMATION_DURATION = 350L

        private fun View.animateTabTextInvisible(isVisible: Boolean, onAnimationStepAction: OnAnimationStepAction) {
            animateInvisible(isVisible, VISIBILITY_ANIMATION_DURATION, AccelerateInterpolator(), onAnimationStepAction)
        }
    }
}