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

    // DÜZELTME 1: "onStepDetected" telsiz kanalını buraya ekledik!
    Events("onStepUpdate", "onTimerTick", "onStepDetected")

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
      
      val savedDate = prefs.getString("saved_date", "")
      val currentDate = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(java.util.Date())

      if (savedDate != currentDate && savedDate != "") {
          return@Function 0 
      }

      return@Function prefs.getInt("exact_notification_steps", 0)
    }

    // DÜZELTME 2: Fonksiyonlar arasındaki hatalı virgüller (,) temizlendi!
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

    // Function("getStepCounterValue") {
    //     val context = appContext.reactContext ?: return@Function 0
    //     val prefs = context.getSharedPreferences("StepPrefs", Context.MODE_PRIVATE)
    //     return@Function prefs.getInt("raw_sensor", 0)
    // }
    Function("getStepCounterValue") {
        val context = appContext.reactContext ?: return@Function 0
        val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        val stepCounter = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)

        if (stepCounter == null) return@Function -1

        // 1. Bir "Dur Tabelası" (Latch) oluşturuyoruz. 1 adet veri bekliyoruz.
        val latch = java.util.concurrent.CountDownLatch(1)
        var currentValue = 0

        val tempListener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent?) {
                if (event?.sensor?.type == Sensor.TYPE_STEP_COUNTER) {
                    currentValue = event.values[0].toInt()
                    // 2. Veriyi aldık! Dur tabelasını kaldır, yol açılsın.
                    latch.countDown()
                }
            }
            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
        }

        sensorManager.registerListener(tempListener, stepCounter, SensorManager.SENSOR_DELAY_FASTEST)

        // 3. KRİTİK NOKTA: Burada bekliyoruz. 
        // Maksimum 1 saniye bekle, gelmezse devam et (Uygulama donmasın diye)
        latch.await(1000, java.util.concurrent.TimeUnit.MILLISECONDS)

        // 4. Değeri aldık veya süre doldu, artık dinleyiciyi kapatabiliriz.
        sensorManager.unregisterListener(tempListener)

        return@Function currentValue
    }

    Function("startStepDetection") {
        val context = appContext.reactContext ?: return@Function false
        val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as android.hardware.SensorManager
        val detectorSensor = sensorManager.getDefaultSensor(android.hardware.Sensor.TYPE_STEP_DETECTOR)

        val listener = object : android.hardware.SensorEventListener {
            override fun onSensorChanged(event: android.hardware.SensorEvent?) {
                sendEvent("onStepDetected", mapOf("detected" to true))
            }
            override fun onAccuracyChanged(sensor: android.hardware.Sensor?, accuracy: Int) {}
        }

        sensorManager.registerListener(listener, detectorSensor, android.hardware.SensorManager.SENSOR_DELAY_UI)
        return@Function true
    }

    // DÜZELTME 3: index.ts'te olan ama burada unutulan getSensorStatus eklendi!
    Function("getSensorStatus") {
        val context = appContext.reactContext ?: return@Function mapOf("error" to "Context bulunamadı")
        val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as android.hardware.SensorManager
        
        val detector = sensorManager.getDefaultSensor(android.hardware.Sensor.TYPE_STEP_DETECTOR)
        val counter = sensorManager.getDefaultSensor(android.hardware.Sensor.TYPE_STEP_COUNTER)

        return@Function mapOf(
            "hasDetector" to (detector != null),
            "detectorName" to (detector?.name ?: "Mevcut Değil"),
            "hasCounter" to (counter != null),
            "counterName" to (counter?.name ?: "Mevcut Değil")
        )
    }

    Function("stopService") {
      val context = appContext.reactContext ?: return@Function "Hata"
      val intent = Intent(context, StepCounterService::class.java)
      context.stopService(intent)
      return@Function "Servis Kapatıldı"
    }

    // DÜZELTME 4: Yanlışlıkla silinen Function("startService") başlığı geri getirildi!
    Function("startService") { title: String, body: String, initialSteps: Int ->
      val context = appContext.reactContext ?: return@Function "Hata"
      val intent = Intent(context, StepCounterService::class.java)
      
      intent.putExtra("notificationTitle", title)
      intent.putExtra("notificationBody", body)
      intent.putExtra("initialSteps", initialSteps) 
      
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
          context.startForegroundService(intent)
      } else {
          context.startService(intent)
      }
      return@Function "Servis Özel Metinle Başlatıldı!"
    }
  }
}