package com.sliteptyltd.slite.feature.onboarding

import android.content.res.ColorStateList
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.viewpager2.widget.ViewPager2
import com.sliteptyltd.slite.R
import com.sliteptyltd.slite.data.preference.InternalStorageManager
import com.sliteptyltd.slite.databinding.FragmentOnboardingBinding
import com.sliteptyltd.slite.feature.onboarding.adapter.OnboardingAdapter
import com.sliteptyltd.slite.utils.extensions.views.animateInvisible
import com.zhuinden.fragmentviewbindingdelegatekt.viewBinding
import org.koin.android.ext.android.inject

class OnboardingFragment : Fragment(R.layout.fragment_onboarding) {

    private val binding by viewBinding(FragmentOnboardingBinding::bind)
    private val internalStorageManager by inject<InternalStorageManager>()
    private val onboardingAdapter by lazy { initOnboardingAdapter() }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupAdapter()
        initListeners()
    }

    private fun setupAdapter() {
        binding.onboardingPager.adapter = onboardingAdapter
        binding.onboardingPager.offscreenPageLimit = onboardingAdapter.itemCount
        binding.onboardingPager.isUserInputEnabled = false
        binding.pageIndicator.attachTo(binding.onboardingPager)

        binding.onboardingPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {

            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)

                updateViewsVisibility(position)
                setPagePreviewViewsTint(position)
            }
        })
    }

    private fun updateViewsVisibility(currentPage: Int) {
        val isLastPage = currentPage == onboardingAdapter.itemCount - 1
        binding.pageIndicator.animateInvisible(isLastPage, VISIBILITY_ANIMATION_DURATION)
        binding.nextBtn.animateInvisible(isLastPage, VISIBILITY_ANIMATION_DURATION)
        binding.nextPagePreviewV.animateInvisible(isLastPage, VISIBILITY_ANIMATION_DURATION)
        binding.beginBtn.animateInvisible(!isLastPage, VISIBILITY_ANIMATION_DURATION)
    }

    private fun setPagePreviewViewsTint(currentPage: Int) {
        val previewViewsTint = if (currentPage == FIRST_PAGE_INDEX) {
            ColorStateList.valueOf(resources.getColor(R.color.onboarding_first_page_preview_tint, null))
        } else {
            ColorStateList.valueOf(resources.getColor(R.color.onboarding_second_page_preview_tint, null))
        }
        binding.nextBtn.backgroundTintList = previewViewsTint
        binding.nextPagePreviewV.backgroundTintList = previewViewsTint
    }

    private fun initListeners() {
        binding.beginBtn.setOnClickListener {
            internalStorageManager.isOnboardingComplete = true
            findNavController().navigate(OnboardingFragmentDirections.actionOnboardingFragmentToHomeGraph())
        }

        binding.nextBtn.setOnClickListener {
            binding.onboardingPager.currentItem += 1
        }
    }

    private fun initOnboardingAdapter(): OnboardingAdapter = OnboardingAdapter(requireActivity())

    companion object {
        private const val VISIBILITY_ANIMATION_DURATION = 250L
        private const val FIRST_PAGE_INDEX = 0
    }
}