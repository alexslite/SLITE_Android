package com.sliteptyltd.slite.feature.effectsservice.provider

import com.sliteptyltd.slite.feature.effectsservice.Effect
import com.sliteptyltd.slite.feature.effectsservice.Effect.DISCO
import com.sliteptyltd.slite.feature.effectsservice.Effect.FAULTY_GLOBE
import com.sliteptyltd.slite.feature.effectsservice.Effect.FIRE
import com.sliteptyltd.slite.feature.effectsservice.Effect.FIREWORKS
import com.sliteptyltd.slite.feature.effectsservice.Effect.LIGHTNING
import com.sliteptyltd.slite.feature.effectsservice.Effect.PAPARAZZI
import com.sliteptyltd.slite.feature.effectsservice.Effect.POLICE
import com.sliteptyltd.slite.feature.effectsservice.Effect.PULSING
import com.sliteptyltd.slite.feature.effectsservice.Effect.STROBE
import com.sliteptyltd.slite.feature.effectsservice.Effect.TV
import com.sliteptyltd.slite.feature.effectsservice.provider.effect.DiscoEffectHandler
import com.sliteptyltd.slite.feature.effectsservice.provider.effect.FaultyGlobeEffectHandler
import com.sliteptyltd.slite.feature.effectsservice.provider.effect.FireEffectHandler
import com.sliteptyltd.slite.feature.effectsservice.provider.effect.FireworksEffectHandler
import com.sliteptyltd.slite.feature.effectsservice.provider.effect.LightEffectHandler
import com.sliteptyltd.slite.feature.effectsservice.provider.effect.LightningEffectHandler
import com.sliteptyltd.slite.feature.effectsservice.provider.effect.PaparazziEffectHandler
import com.sliteptyltd.slite.feature.effectsservice.provider.effect.PoliceEffectHandler
import com.sliteptyltd.slite.feature.effectsservice.provider.effect.PulsingEffectHandler
import com.sliteptyltd.slite.feature.effectsservice.provider.effect.StrobeEffectHandler
import com.sliteptyltd.slite.feature.effectsservice.provider.effect.TvEffectHandler

typealias OnColorChangedCallback = (color: Int) -> Unit

class EffectsProvider {

    fun getEffect(effect: Effect): LightEffectHandler =
        when (effect) {
            FAULTY_GLOBE -> FaultyGlobeEffectHandler()
            PAPARAZZI -> PaparazziEffectHandler()
            TV -> TvEffectHandler()
            LIGHTNING -> LightningEffectHandler()
            POLICE -> PoliceEffectHandler()
            FIREWORKS -> FireworksEffectHandler()
            DISCO -> DiscoEffectHandler()
            PULSING -> PulsingEffectHandler()
            STROBE -> StrobeEffectHandler()
            FIRE -> FireEffectHandler()
        }
}