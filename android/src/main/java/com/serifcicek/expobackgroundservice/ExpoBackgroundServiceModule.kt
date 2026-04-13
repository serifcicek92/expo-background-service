package com.serifcicek.expobackgroundservice

import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.os.Build
import expo.modules.kotlin.modules.Module
import expo.modules.kotlin.modules.ModuleDefinition

class ExpoBackgroundServiceModule : Module() {
  
  // Servis içinden bu modüle ulaşıp JS'e veri gönderebilmek için
  companion object {
    var instance: ExpoBackgroundServiceModule? = null
  }

  override fun definition() = ModuleDefinition {
    Name("ExpoBackgroundService")

    // İŞTE TELSİZ KANALLARIMIZ: Adım değiştiğinde ve Zamanlayıcı çalıştığında JS'e haber vereceğiz
    Events("onStepUpdate", "onTimerTick")

    // Modül ayağa kalktığında instance'ı doldur
    OnCreate {
      instance = this@ExpoBackgroundServiceModule
    }
    
    OnDestroy {
      instance = null
    }

    Function("hello") {
      return@Function "Sistem Hazır - Şerif Çiçek"
    }

    Function("getStepCount") {
      val context = appContext.reactContext ?: return@Function 0
      val prefs = context.getSharedPreferences("StepPrefs", Context.MODE_PRIVATE)
      return@Function prefs.getInt("real_steps", 0)
    }

    Function("isServiceRunning") {
      val context = appContext.reactContext ?: return@Function false
      val manager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
      for (service in manager.getRunningServices(Integer.MAX_VALUE)) {
        if (StepCounterService::class.java.name == service.service.className) {
          return@Function true
        }
      }
      return@Function false
    }

    Function("stopService") {
      val context = appContext.reactContext ?: return@Function "Hata"
      val intent = Intent(context, StepCounterService::class.java)
      context.stopService(intent)
      return@Function "Servis Kapatıldı"
    }

    Function("startService") { title: String, body: String, initialSteps: Int ->
      val context = appContext.reactContext ?: return@Function "Hata"
      val intent = Intent(context, StepCounterService::class.java)
      
      intent.putExtra("notificationTitle", title)
      intent.putExtra("notificationBody", body)
      intent.putExtra("initialSteps", initialSteps) // JS'ten gelen taban adım
      
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
          context.startForegroundService(intent)
      } else {
          context.startService(intent)
      }
      return@Function "Servis Özel Metinle Başlatıldı!"
    }
  }
}