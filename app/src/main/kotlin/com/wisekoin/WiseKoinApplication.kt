package com.wisekoin

import android.app.Application
import io.sentry.android.core.SentryAndroid

class WiseKoinApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        SentryAndroid.init(this) { options ->
            options.setTracePropagationTargets(listOf(BuildConfig.API_BASE_URL))
        }
    }
}
