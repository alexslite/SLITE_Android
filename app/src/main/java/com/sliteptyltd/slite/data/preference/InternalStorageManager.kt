package com.sliteptyltd.slite.data.preference

import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.SharedPreferences

class InternalStorageManager(context: Context) : InternalStorageFieldDelegate.StorageManager {

    private val appPreferences: SharedPreferences = context.getSharedPreferences(KEY_APP_PREFERENCES, MODE_PRIVATE)

    var isOnboardingComplete by InternalStorageFieldDelegate.Boolean(appPreferences, KEY_IS_ONBOARDING_COMPLETE, false)
    var hasUserSeenAddLightsTutorial by InternalStorageFieldDelegate.Boolean(appPreferences, KEY_HAS_USED_SEEN_ADD_LIGHTS_TUTORIAL, false)
    var hasUserAddedFirstDevice by InternalStorageFieldDelegate.Boolean(appPreferences, KEY_HAS_USER_ADDED_FIRST_DEVICE, false)
    var hasUserAcknowledgedPhotoSensitivityWarning by InternalStorageFieldDelegate.Boolean(
        appPreferences,
        KEY_HAS_USER_ACKNOWLEDGED_PHOTO_SENSITIVITY_WARNING,
        false
    )
    var newlyCreatedGroupId by InternalStorageFieldDelegate.Int(appPreferences, KEY_NEWLY_CREATED_GROUP_ID, NO_NEWLY_CREATED_GROUP_ID)
    var latestRecommendedAppVersionShown by InternalStorageFieldDelegate.String(
        appPreferences,
        KEY_LATEST_RECOMMENDED_APP_VERSION_SHOWN,
        null
    )

    companion object {
        private const val KEY_APP_PREFERENCES = "com.sliteptyltd.slite.KEY_SLITE_APP_PREFERENCES"

        private const val KEY_IS_ONBOARDING_COMPLETE = "KEY_IS_ONBOARDING_COMPLETE"
        private const val KEY_HAS_USED_SEEN_ADD_LIGHTS_TUTORIAL = "KEY_HAS_USED_SEEN_ADD_LIGHTS_TUTORIAL"
        private const val KEY_HAS_USER_ADDED_FIRST_DEVICE = "KEY_HAS_USER_ADDED_FIRST_DEVICE"
        private const val KEY_HAS_USER_ACKNOWLEDGED_PHOTO_SENSITIVITY_WARNING = "KEY_HAS_USER_ACKNOWLEDGED_PHOTO_SENSITIVITY_WARNING"
        private const val KEY_NEWLY_CREATED_GROUP_ID = "KEY_NEWLY_CREATED_GROUP_ID"
        private const val KEY_LATEST_RECOMMENDED_APP_VERSION_SHOWN = "KEY_LATEST_RECOMMENDED_APP_VERSION_SHOWN"

        const val NO_NEWLY_CREATED_GROUP_ID = -1
    }
}