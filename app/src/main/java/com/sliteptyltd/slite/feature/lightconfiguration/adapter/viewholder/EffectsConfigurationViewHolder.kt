package com.sliteptyltd.slite.feature.lightconfiguration.adapter.viewholder

import android.text.SpannableString
import android.text.style.ImageSpan
import android.view.View.NO_ID
import android.view.ViewGroup
import android.widget.RadioButton
import androidx.annotation.IdRes
import androidx.core.view.children
import com.sliteptyltd.slite.R
import com.sliteptyltd.slite.databinding.ItemEffectsBinding
import com.sliteptyltd.slite.feature.effectsservice.Effect
import com.sliteptyltd.slite.feature.effectsservice.Effect.FAULTY_GLOBE
import com.sliteptyltd.slite.feature.effectsservice.Effect.FIRE
import com.sliteptyltd.slite.feature.effectsservice.Effect.PAPARAZZI
import com.sliteptyltd.slite.feature.effectsservice.Effect.LIGHTNING
import com.sliteptyltd.slite.feature.effectsservice.Effect.FIREWORKS
import com.sliteptyltd.slite.feature.effectsservice.Effect.DISCO
import com.sliteptyltd.slite.feature.effectsservice.Effect.PULSING
import com.sliteptyltd.slite.feature.effectsservice.Effect.POLICE
import com.sliteptyltd.slite.feature.effectsservice.Effect.TV
import com.sliteptyltd.slite.feature.effectsservice.Effect.STROBE
import com.sliteptyltd.slite.feature.lightconfiguration.adapter.LightConfigurationItem
import com.sliteptyltd.slite.feature.lightconfiguration.adapter.OnConfigurationValueUpdated
import com.sliteptyltd.slite.feature.lightconfiguration.adapter.OnTrackConfigurationChangedEvent
import com.sliteptyltd.slite.utils.extensions.views.layoutInflater

class EffectsConfigurationViewHolder(
    private val binding: ItemEffectsBinding,
    private val onConfigurationValueUpdated: OnConfigurationValueUpdated,
    private val onTrackConfigurationChangedEvent: OnTrackConfigurationChangedEvent
) : LightConfigurationViewHolder(binding) {

    private var effectConfiguration: LightConfigurationItem.EffectsConfigurationItem? = null

    init {
        initEffectsRadioButtons()

        binding.firstEffectsSectionRG.setOnCheckedChangeListener { _, checkedRadioButtonId ->
            val checkedEffect = getEffectForCheckedRadioButton(checkedRadioButtonId)
            if (checkedRadioButtonId == NO_ID || checkedEffect == effectConfiguration?.effect) return@setOnCheckedChangeListener
            binding.secondEffectsSectionRG.clearCheck()
            binding.firstEffectsSectionRG.check(checkedRadioButtonId)
            effectConfiguration?.let { effectConfiguration ->
                effectConfiguration.effect = checkedEffect
                onConfigurationValueUpdated(effectConfiguration)
                trackEffectChanged(checkedEffect)
            }
        }

        binding.secondEffectsSectionRG.setOnCheckedChangeListener { _, checkedRadioButtonId ->
            val checkedEffect = getEffectForCheckedRadioButton(checkedRadioButtonId)
            if (checkedRadioButtonId == NO_ID || checkedEffect == effectConfiguration?.effect) return@setOnCheckedChangeListener
            binding.firstEffectsSectionRG.clearCheck()
            binding.secondEffectsSectionRG.check(checkedRadioButtonId)
            effectConfiguration?.let { effectConfiguration ->
                effectConfiguration.effect = checkedEffect
                onConfigurationValueUpdated(effectConfiguration)
                trackEffectChanged(checkedEffect)
            }
        }
    }

    override fun bind(lightConfiguration: LightConfigurationItem) {
        val effectConfiguration = (lightConfiguration as? LightConfigurationItem.EffectsConfigurationItem)
            ?: throw IllegalArgumentException("Item of type ${lightConfiguration::class.java} could not be cast to LightConfigurationItem.EffectsConfigurationItem")
        this.effectConfiguration = effectConfiguration

        binding.firstEffectsSectionRG.check(effectConfiguration.effect.getCheckedRadioButtonForEffect())
        binding.secondEffectsSectionRG.check(effectConfiguration.effect.getCheckedRadioButtonForEffect())
    }

    private fun initEffectsRadioButtons() {
        binding.firstEffectsSectionRG.children.forEach { childView ->
            if (childView is RadioButton) {
                childView.setEffectImageSpannableString()
            }
        }

        binding.secondEffectsSectionRG.children.forEach { childView ->
            if (childView is RadioButton) {
                childView.setEffectImageSpannableString()
            }
        }
    }

    private fun trackEffectChanged(effect: Effect?) {
        val effectConfiguration =
            effectConfiguration?.copy(effect = effect) ?: return
        onTrackConfigurationChangedEvent(effectConfiguration)
    }

    fun clearEffectSelection() {
        binding.firstEffectsSectionRG.clearCheck()
        binding.secondEffectsSectionRG.clearCheck()
    }

    private fun getEffectForCheckedRadioButton(@IdRes checkedRadioButtonId: Int): Effect? =
        when (checkedRadioButtonId) {
            R.id.faultyGlobeRB -> FAULTY_GLOBE
            R.id.fireRB -> FIRE
            R.id.paparazziRB -> PAPARAZZI
            R.id.lightningRB -> LIGHTNING
            R.id.fireworksRB -> FIREWORKS
            R.id.discoRB -> DISCO
            R.id.pulsingRB -> PULSING
            R.id.policeRB -> POLICE
            R.id.tvRB -> TV
            R.id.strobeRB -> STROBE
            else -> null
        }

    private fun Effect?.getCheckedRadioButtonForEffect(): Int =
        when (this) {
            FAULTY_GLOBE -> R.id.faultyGlobeRB
            FIRE -> R.id.fireRB
            PAPARAZZI -> R.id.paparazziRB
            LIGHTNING -> R.id.lightningRB
            FIREWORKS -> R.id.fireworksRB
            DISCO -> R.id.discoRB
            PULSING -> R.id.pulsingRB
            POLICE -> R.id.policeRB
            TV -> R.id.tvRB
            STROBE -> R.id.strobeRB
            else -> NO_ID
        }

    companion object {

        private const val EFFECT_SPANNABLE_IMAGE_STRING_FORMAT = " \n\n%s"

        private fun RadioButton.setEffectImageSpannableString() {
            text = SpannableString(String.format(EFFECT_SPANNABLE_IMAGE_STRING_FORMAT, text)).apply {
                setSpan(ImageSpan(context, getImageForEffect()), 0, 1, 0)
            }
        }

        private fun RadioButton.getImageForEffect(): Int =
            when (id) {
                R.id.fireRB -> R.drawable.ic_effect_fire
                R.id.policeRB -> R.drawable.if_effect_police
                R.id.faultyGlobeRB -> R.drawable.ic_effect_faulty_globe
                R.id.paparazziRB -> R.drawable.ic_effect_paparazzi
                R.id.lightningRB -> R.drawable.ic_effect_lightning
                R.id.fireworksRB -> R.drawable.ic_effect_fireworks
                R.id.discoRB -> R.drawable.ic_effect_disco
                R.id.pulsingRB -> R.drawable.ic_effect_pulsing
                R.id.tvRB -> R.drawable.ic_effect_tv
                else -> R.drawable.ic_effect_strobe
            }

        fun newInstance(
            parent: ViewGroup,
            onConfigurationValueUpdated: OnConfigurationValueUpdated,
            onTrackConfigurationChangedEvent: OnTrackConfigurationChangedEvent
        ): EffectsConfigurationViewHolder = EffectsConfigurationViewHolder(
            ItemEffectsBinding.inflate(parent.layoutInflater, parent, false),
            onConfigurationValueUpdated,
            onTrackConfigurationChangedEvent
        )
    }
}