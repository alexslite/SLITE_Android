package com.sliteptyltd.slite.feature.onboarding.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.sliteptyltd.slite.R

class OnboardingAdapter(
    fragmentActivity: FragmentActivity
) : FragmentStateAdapter(fragmentActivity) {

    private val pages: List<OnboardingPageDetails> by lazy { initPagesList() }

    private fun initPagesList(): List<OnboardingPageDetails> =
        listOf(
            OnboardingPageDetails(
                R.string.onboarding_first_page_title,
                R.string.onboarding_first_page_description,
                R.drawable.bg_onboarding_first_page
            ),
            OnboardingPageDetails(
                R.string.onboarding_second_page_title,
                R.string.onboarding_second_page_description,
                R.drawable.bg_onboarding_second_page
            ),
            OnboardingPageDetails(
                R.string.onboarding_third_page_title,
                R.string.onboarding_third_page_description,
                R.drawable.bg_onboarding_third_page
            )
        )

    override fun getItemCount(): Int = pages.size

    override fun createFragment(position: Int): Fragment = OnboardingPageFragment.newInstance(pages[position])
}