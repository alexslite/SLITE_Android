package com.sliteptyltd.slite.utils.extensions

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.Intent.ACTION_SENDTO
import android.content.Intent.ACTION_VIEW
import android.content.pm.PackageManager.PERMISSION_GRANTED
import android.net.Uri
import android.os.Build.VERSION.SDK_INT
import android.os.Build.VERSION_CODES.S
import android.view.LayoutInflater
import com.sliteptyltd.slite.R
import com.sliteptyltd.slite.utils.Constants
import com.sliteptyltd.slite.utils.Constants.Connectivity.MIN_SDK_31_BLUETOOTH_PERMISSIONS
import com.sliteptyltd.slite.utils.Constants.Support.CONTACT_ADDRESS_URI_STRING

val Context.layoutInflater: LayoutInflater get() = LayoutInflater.from(this)

val Context.hasBluetoothCapabilitiesPermissions: Boolean
    get() = if (SDK_INT >= S) {
        MIN_SDK_31_BLUETOOTH_PERMISSIONS.all { permission -> checkSelfPermission(permission) == PERMISSION_GRANTED }
    } else {
        true
    }

fun Context.openPlayStorePage() {
    startActivity(
        Intent(
            ACTION_VIEW,
            Uri.parse(Constants.Support.PLAY_STORE_APP_URL)
        )
    )
}

fun Context.openChooserForSupportEmail(onEmailClientNotFound: () -> Unit) {
    try {
        startActivity(
            Intent.createChooser(
                Intent(ACTION_SENDTO, Uri.parse(CONTACT_ADDRESS_URI_STRING)),
                getString(R.string.support_email_app_chooser_title)
            )
        )
    } catch (e: ActivityNotFoundException) {
        onEmailClientNotFound()
    }
}