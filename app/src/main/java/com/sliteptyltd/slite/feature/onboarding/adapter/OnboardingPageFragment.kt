package com.sliteptyltd.slite.feature.onboarding.adapter

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.sliteptyltd.slite.R
import com.sliteptyltd.slite.databinding.FragmentOnboardingPageBinding
import com.zhuinden.fragmentviewbindingdelegatekt.viewBinding

class OnboardingPageFragment : Fragment(R.layout.fragment_onboarding_page) {

    private val binding by viewBinding(FragmentOnboardingPageBinding::bind)
    private val pageDetails by lazy { initPageDetails() }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setPageDetails()
    }

    private fun setPageDetails() {
        pageDetails?.let { pageDetails ->
            Glide.with(binding.onboardingBackgroundIV)
                .load(pageDetails.background)
                .centerCrop()
                .into(binding.onboardingBackgroundIV)
            binding.titleTV.setText(pageDetails.title)
            binding.descriptionTV.setText(pageDetails.description)
        }
    }

    private fun initPageDetails(): OnboardingPageDetails? = arguments.onboardingPageDetails

    companion object {
        private const val ONBOARDING_PAGE_DETAILS_KEY = "ONBOARDING_PAGE_DETAILS_KEY"

        private val Bundle?.onboardingPageDetails: OnboardingPageDetails? get() = this?.getParcelable(ONBOARDING_PAGE_DETAILS_KEY)

        fun newInstance(pageDetails: OnboardingPageDetails): OnboardingPageFragment =
            OnboardingPageFragment().apply {
                arguments = Bundle().apply { putParcelable(ONBOARDING_PAGE_DETAILS_KEY, pageDetails) }
            }
    }
}