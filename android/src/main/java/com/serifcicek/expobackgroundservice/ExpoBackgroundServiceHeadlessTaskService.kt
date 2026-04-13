package com.serifcicek.expobackgroundservice

import android.content.Intent
import com.facebook.react.HeadlessJsTaskService
import com.facebook.react.bridge.Arguments
import com.facebook.react.jstasks.HeadlessJsTaskConfig

class ExpoBackgroundServiceHeadlessTaskService : HeadlessJsTaskService() {
    override fun getTaskConfig(intent: Intent): HeadlessJsTaskConfig? {
        val extras = intent.extras
        return if (extras != null) {
            HeadlessJsTaskConfig(
                "MyBackgroundStepTask", // JS tarafındaki isimle aynı olmalı (App.tsx)
                Arguments.fromBundle(extras),
                5000, // 5 saniye zaman aşımı
                true  // Uygulama kapalıyken çalışmasına izin ver
            )
        } else {
            null
        }
    }
}