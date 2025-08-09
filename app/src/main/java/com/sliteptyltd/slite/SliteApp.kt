package com.sliteptyltd.slite

import android.app.Application
import com.sliteptyltd.slite.BuildConfig.DEBUG
import com.sliteptyltd.persistence.di.databaseModule
import com.sliteptyltd.slite.BuildConfig.APPLICATION_ID
import com.sliteptyltd.slite.BuildConfig.VERSION_NAME
import com.sliteptyltd.slite.di.handlers
import com.sliteptyltd.slite.di.internalStorageManager
import com.sliteptyltd.slite.di.providers
import com.sliteptyltd.slite.di.repositories
import com.sliteptyltd.slite.di.services
import com.sliteptyltd.slite.di.useCases
import com.sliteptyltd.slite.di.viewModels
import com.sliteptyltd.slite.utils.Constants.Sentry.DSN
import com.sliteptyltd.slite.utils.Constants.Sentry.TRACES_SAMPLE_RATE
import com.sliteptyltd.slite.utils.LogMe
import io.sentry.android.core.SentryAndroid
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class SliteApp : Application() {

    override fun onCreate() {
        super.onCreate()
        initKoin()
        initSentry()
        LogMe.setLoggingEnabled(DEBUG)
    }

    private fun initKoin() {
        startKoin {
            androidContext(this@SliteApp)
            modules(
                databaseModule,
                repositories,
                useCases,
                viewModels,
                internalStorageManager,
                handlers,
                services,
                providers
            )
        }
    }

    private fun initSentry() {
        if (!DEBUG) {
            SentryAndroid.init(this) {
                it.dsn = DSN
                it.isDebug = false
                it.tracesSampleRate = TRACES_SAMPLE_RATE
                it.release = "${APPLICATION_ID}@${VERSION_NAME}"
            }
        }
    }
}