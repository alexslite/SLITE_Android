package com.sliteptyltd.slite.feature.effectsservice

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.NotificationManager.IMPORTANCE_DEFAULT
import android.app.PendingIntent
import android.app.PendingIntent.FLAG_IMMUTABLE
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import com.sliteptyltd.slite.R
import com.sliteptyltd.slite.SliteActivity
import com.sliteptyltd.slite.feature.effectsservice.data.EffectJobDetails
import com.sliteptyltd.slite.feature.effectsservice.data.EffectLights
import com.sliteptyltd.slite.feature.effectsservice.provider.EffectsProvider
import com.sliteptyltd.slite.utils.color.toArgbHexString
import com.sliteptyltd.slite.utils.logMe
import kotlinx.coroutines.*
import kotlinx.coroutines.Dispatchers.Main
import org.koin.android.ext.android.inject

class EffectsPlayerService : Service() {

    private val effectsCreatorsProvider by inject<EffectsProvider>()
    private val notificationsManager: NotificationManager get() = getNotificationManager()
    private val effectsJobsMap = mutableMapOf<Int, EffectJobDetails>()
    private val effectsJobsScope = CoroutineScope(SupervisorJob() + Main)
    private val effectsPlayer by inject<EffectsPlayer>()

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        handleEffectCommand(intent)

        return START_STICKY
    }

    override fun onDestroy() {
        stopAllEffects()
        super.onDestroy()
    }

    private fun handleEffectCommand(effectIntent: Intent?) {
        if (effectIntent == null) return

        val effect = effectIntent.effect
        val lightAddresses = effectIntent.lightAddress.lightsAddresses
        val groupId = effectIntent.groupId

        when {
            lightAddresses.isEmpty() -> stopAllEffects()
            effect != null -> {
                if (groupId == NO_GROUP_ID) {
                    startEffect(lightAddresses.first().hashCode(), lightAddresses, effect)
                } else {
                    startEffect(groupId, lightAddresses, effect)
                }
            }
            else -> {
                val address = lightAddresses.first()
                if (groupId == NO_GROUP_ID) {
                    stopEffect(address.hashCode(), address, false)
                } else {
                    stopEffect(groupId, address, lightAddresses.size > INDIVIDUAL_LIGHT_ADDRESSES_COUNT_THRESHOLD)
                }
            }
        }
    }

    private fun stopEffect(effectJobId: Int, lightAddress: String, shutdownGroupEffect: Boolean) {
        if ((effectsJobsMap[effectJobId]?.lightsAddresses?.size == INDIVIDUAL_LIGHT_ADDRESSES_COUNT_THRESHOLD && lightAddress.hashCode() == effectJobId) || shutdownGroupEffect) {
            effectsJobsMap[effectJobId]?.lightEffectHandler?.stop()
            effectsJobsMap.remove(effectJobId)
        } else {
            if (lightAddress == NO_LIGHT_ADDRESS) {
                effectsJobsMap[effectJobId]?.lightEffectHandler?.stop()
                effectsJobsMap.remove(effectJobId)
            } else {
                val groupEffectJobInfo = effectsJobsMap.entries.firstOrNull { entry -> entry.value.lightsAddresses.contains(lightAddress) }
                effectsJobsMap.values.forEach { effectJobDetails ->
                    if (lightAddress in effectJobDetails.lightsAddresses) {
                        effectJobDetails.lightsAddresses.remove(lightAddress)
                        return@forEach
                    }
                }
                if (effectsJobsMap[effectJobId]?.lightsAddresses != null && effectsJobsMap[effectJobId]?.lightsAddresses?.isEmpty() == true) {
                    effectsJobsMap[effectJobId]?.lightEffectHandler?.stop()
                    effectsJobsMap.remove(effectJobId)
                } else if (groupEffectJobInfo?.value?.lightsAddresses?.isEmpty() == true) {
                    groupEffectJobInfo.value.lightEffectHandler.stop()
                    groupEffectJobInfo.key.let { effectsJobsMap.remove(it) }

                }
            }
        }

        if (effectsJobsMap.isEmpty()) {
            stopSelf()
        }
    }

    private fun startEffect(effectJobId: Int, lightAddresses: List<String>, effect: Effect) {
        if (effectsJobsMap.isEmpty()) {
            showEffectsNotification()
        }
        if (effect == effectsJobsMap[effectJobId]?.lightEffectHandler?.effect) return
        effectsJobsMap[effectJobId]?.lightEffectHandler?.stop()
        val lightEffectHandler = effectsCreatorsProvider.getEffect(effect)
        effectsJobsMap[effectJobId] = EffectJobDetails(lightAddresses.toMutableList(), lightEffectHandler)
        effectsJobsScope.launch {
            var job: Job? = null
            lightEffectHandler.start { color ->
                if (job?.isActive == false || job == null) {
                    job = effectsJobsScope.launch {
                        delay(25L)
                        effectsJobsMap[effectJobId]?.lightsAddresses?.forEach { lightAddress ->
                            effectsPlayer.setColor(lightAddress, color)
                        }
                        logMe("$LIGHT_COLOR_HEX_DEBUGGING_TAG: $effectJobId Addresses: $lightAddresses") { color.toArgbHexString() }
                    }.apply { invokeOnCompletion { job = null } }
                }
            }
        }
    }

    private fun stopAllEffects() {
        effectsJobsMap.values.forEach { effectHandler -> effectHandler.lightEffectHandler.stop() }
        effectsJobsMap.clear()

        stopSelf()
    }

    private fun showEffectsNotification() {
        startForeground(EFFECTS_NOTIFICATION_ID, buildEffectsNotification())
    }

    private fun buildEffectsNotification(): Notification =
        Notification.Builder(this, createNotificationChannel())
            .setOngoing(true)
            .setContentTitle(getString(R.string.effects_notification_title))
            .setContentText(getString(R.string.effects_notification_content_text))
            .setSmallIcon(R.drawable.ic_slite_logo)
            .setContentIntent(createEffectsPendingIntent())
            .build()

    private fun createNotificationChannel(): String {
        val channel = NotificationChannel(
            EFFECTS_NOTIFICATION_CHANNEL_ID,
            getString(R.string.effects_notification_channel_name),
            IMPORTANCE_DEFAULT
        )
        notificationsManager.createNotificationChannel(channel)
        return channel.id
    }

    private fun createEffectsPendingIntent(): PendingIntent =
        PendingIntent.getActivity(this, EFFECTS_PENDING_INTENT_REQUEST_CODE, Intent(this, SliteActivity::class.java), FLAG_IMMUTABLE)

    private fun getNotificationManager(): NotificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager

    override fun onBind(intent: Intent?): IBinder? = null

    companion object {
        //TODO: Remove debugging TAG after BT impl
        private const val LIGHT_COLOR_HEX_DEBUGGING_TAG = "SliteEffectColor"

        private const val EFFECTS_NOTIFICATION_CHANNEL_ID = "EFFECTS_NOTIFICATION_CHANNEL_ID"
        private const val EFFECTS_NOTIFICATION_ID = 1001
        private const val EFFECTS_PENDING_INTENT_REQUEST_CODE = 1000
        private const val LIGHT_ID_KEY = "LIGHT_ID_KEY"
        private const val GROUP_ID_KEY = "GROUP_ID_KEY"
        private const val EFFECT_KEY = "EFFECT_KEY"
        private const val NO_LIGHT_ADDRESS = ""
        private const val INDIVIDUAL_LIGHT_ADDRESSES_COUNT_THRESHOLD = 1
        const val NO_GROUP_ID = -1

        private val Intent.lightAddress: EffectLights get() = this.getParcelableExtra(LIGHT_ID_KEY) ?: EffectLights(emptyList())
        private val Intent.groupId: Int get() = this.getIntExtra(GROUP_ID_KEY, NO_GROUP_ID)
        private val Intent.effect: Effect? get() = Effect.fromString(this.getStringExtra(EFFECT_KEY))

        fun createEffectCommandIntent(
            context: Context,
            lightAddresses: List<String> = listOf(),
            effect: Effect? = null,
            groupId: Int = NO_GROUP_ID
        ): Intent =
            Intent(context, EffectsPlayerService::class.java).apply {
                putExtra(LIGHT_ID_KEY, EffectLights(lightAddresses))
                putExtra(GROUP_ID_KEY, groupId)
                putExtra(EFFECT_KEY, effect?.name)
            }
    }
}