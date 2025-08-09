package com.sliteptyltd.slite.utils

import android.app.Activity
import android.widget.TextView
import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.core.content.res.ResourcesCompat
import com.tapadoo.alerter.Alerter
import com.sliteptyltd.slite.R

class AnnouncementHandler {

    fun showSuccessAnnouncement(activity: Activity, message: String?) {
        if (message.isNullOrEmpty()) return

        showAnnouncement(activity, message, AnnouncementType.Success)
    }

    fun showWarningAnnouncement(activity: Activity, message: String?) {
        if (message.isNullOrEmpty()) return

        showAnnouncement(activity, message, AnnouncementType.Warning)
    }

    private fun showAnnouncement(activity: Activity, message: String, announcement: AnnouncementType) {
        Alerter.create(activity, R.layout.layout_announcement)
            .setBackgroundColorRes(R.color.transparent)
            .setDismissable(true)
            .also {
                it.getLayoutContainer()?.apply {
                    findViewById<TextView>(R.id.announcementTextTv).apply {
                        text = message
                        setTextColor(ResourcesCompat.getColor(resources, announcement.textColor, null))
                    }
                    this.background = ResourcesCompat.getDrawable(resources, announcement.background, null)
                }
            }.show()
    }

    companion object {
        sealed class AnnouncementType(@DrawableRes val background: Int, @ColorRes val textColor: Int) {
            object Success : AnnouncementType(R.drawable.bg_announcement_success, R.color.announcement_success_text_color)
            object Warning : AnnouncementType(R.drawable.bg_announcement_warning, R.color.announcement_warning_text_color)
        }
    }
}