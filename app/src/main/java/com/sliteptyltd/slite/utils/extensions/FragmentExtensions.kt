package com.sliteptyltd.slite.utils.extensions

import android.content.Intent
import android.content.Intent.CATEGORY_DEFAULT
import android.content.Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS
import android.content.Intent.FLAG_ACTIVITY_NEW_TASK
import android.content.Intent.FLAG_ACTIVITY_NO_HISTORY
import android.net.Uri
import android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS
import android.provider.Settings.ACTION_BLUETOOTH_SETTINGS
import android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.sliteptyltd.slite.utils.Constants.SETTINGS_URI_SCHEME

fun Fragment.navigateUp() {
    if (findNavController().previousBackStackEntry == null) {
        requireActivity().onBackPressedDispatcher.onBackPressed()
    } else {
        findNavController().navigateUp()
    }
}

fun Fragment.openAppSettings() {
    val intent = Intent(ACTION_APPLICATION_DETAILS_SETTINGS).apply {
        data = Uri.fromParts(SETTINGS_URI_SCHEME, requireContext().packageName, null)
        addCategory(CATEGORY_DEFAULT)
        flags = FLAG_ACTIVITY_NEW_TASK or FLAG_ACTIVITY_NO_HISTORY or FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS
    }

    startActivity(intent)
}

fun Fragment.openBluetoothSettings() {
    startActivity(Intent(ACTION_BLUETOOTH_SETTINGS))
}

fun Fragment.openLocationSettings() {
    startActivity(Intent(ACTION_LOCATION_SOURCE_SETTINGS))
}