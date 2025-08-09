package com.sliteptyltd.slite.feature.effectsservice

import android.app.Activity
import android.app.Application
import com.sliteptyltd.slite.utils.ActivityLifecycleStateChangeListener

class EffectsHandler(
    private val application: Application
) {

    init {
        application.registerActivityLifecycleCallbacks(object : ActivityLifecycleStateChangeListener() {
            override fun onActivityDestroyed(activity: Activity) = stopAllEffects()
        })
    }

    fun updateEffectStatus(lightAddress: String, effect: Effect?, isPoweredOn: Boolean) {
        if (isPoweredOn && effect != null) {
            playEffect(lightAddress, effect)
        } else {
            stopEffect(lightAddress)
        }
    }

    fun updateEffectStatus(lightsAddressesList: List<String>, groupId: Int, effect: Effect?, isPoweredOn: Boolean) {
        if (isPoweredOn && effect != null) {
            playEffect(lightsAddressesList, effect, groupId)
        } else {
            stopEffect(lightsAddressesList, groupId)
        }
    }

    private fun playEffect(lightAddress: String, effect: Effect) {
        application.startService(
            EffectsPlayerService.createEffectCommandIntent(application, listOf(lightAddress), effect)
        )
    }

    private fun playEffect(groupedLightsAddresses: List<String>, effect: Effect, groupId: Int) {
        application.startService(
            EffectsPlayerService.createEffectCommandIntent(application, groupedLightsAddresses, effect, groupId = groupId)
        )
    }

    fun stopEffect(lightAddress: String) {
        application.startService(
            EffectsPlayerService.createEffectCommandIntent(application, listOf(lightAddress))
        )
    }

    private fun stopEffect(groupedLightsAddresses: List<String>, groupId: Int) {
        application.startService(
            EffectsPlayerService.createEffectCommandIntent(application, groupedLightsAddresses, groupId = groupId)
        )
    }

    fun stopAllEffects() {
        application.stopService(
            EffectsPlayerService.createEffectCommandIntent(application)
        )
    }
}