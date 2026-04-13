package com.serifcicek.expobackgroundservice

import android.content.Intent
import com.facebook.react.HeadlessJsTaskService
import com.facebook.react.bridge.Arguments
import com.facebook.react.jstasks.HeadlessJsTaskConfig

class ExpoBackgroundServiceHeadlessTaskService : HeadlessJsTaskService() {
    // Intent yanına ? koyduk (Nullable yaptık)
    override fun getTaskConfig(intent: Intent?): HeadlessJsTaskConfig? {
        val extras = intent?.extras // intent? kullanarak güvenli hale getirdik
        return if (extras != null) {
            HeadlessJsTaskConfig(
                "MyBackgroundStepTask",
                Arguments.fromBundle(extras),
                5000,
                true
            )
        } else {
            null
        }
    }
}