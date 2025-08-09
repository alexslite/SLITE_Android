package com.sliteptyltd.slite.utils

import android.app.Activity
import android.app.Application
import android.os.Bundle

open class ActivityLifecycleStateChangeListener : Application.ActivityLifecycleCallbacks {

    override fun onActivityCreated(activity: Activity, bundle: Bundle?) = Unit

    override fun onActivityStarted(activity: Activity) = Unit

    override fun onActivityResumed(activity: Activity) = Unit

    override fun onActivityPaused(activity: Activity) = Unit

    override fun onActivityStopped(activity: Activity) = Unit

    override fun onActivitySaveInstanceState(activity: Activity, bundle: Bundle) = Unit

    override fun onActivityDestroyed(activity: Activity) = Unit
}