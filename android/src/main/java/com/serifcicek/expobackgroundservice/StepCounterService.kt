package com.serifcicek.expobackgroundservice

import android.app.*
import android.content.*
import android.hardware.*
import android.os.*
import androidx.core.app.NotificationCompat

class StepCounterService : Service(), SensorEventListener {
    private var sensorManager: SensorManager? = null
    private var stepSensor: Sensor? = null
    private lateinit var notificationManager: NotificationManager
    private val NOTIFICATION_ID = 1
    private var customTitle: String = "Step Counter"
    private var customBody: String = "Adım"

    private val handler = Handler(Looper.getMainLooper())
    private val periodicTask = object : Runnable {
        override fun run() {
            val prefs = getSharedPreferences("StepPrefs", Context.MODE_PRIVATE)
            // DELTA MOTORU İÇİN HAM VERİYİ OKU
            val currentRawSteps = prefs.getInt("raw_sensor", 0) 
            
            ExpoBackgroundServiceModule.instance?.sendEvent("onTimerTick", mapOf(
                "steps" to currentRawSteps
            ))

            val serviceIntent = Intent(applicationContext, ExpoBackgroundServiceHeadlessTaskService::class.java)
            val bundle = Bundle()
            bundle.putInt("steps", currentRawSteps) // Hayalet moda HAM veri gidiyor
            serviceIntent.putExtras(bundle)
            applicationContext.startService(serviceIntent)
            
            handler.postDelayed(this, 60000)
        }
    }

    override fun onCreate() {
        super.onCreate()
        notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        createNotificationChannel()

        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        stepSensor = sensorManager?.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)
        
        stepSensor?.let {
            sensorManager?.registerListener(this, it, SensorManager.SENSOR_DELAY_UI)
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        intent?.getStringExtra("notificationTitle")?.let { customTitle = it }
        intent?.getStringExtra("notificationBody")?.let { customBody = it }
        
        // JS'TEN GELEN TABAN ADIMI KAYDET
        val initialSteps = intent?.getIntExtra("initialSteps", 0) ?: 0
        val prefs = getSharedPreferences("StepPrefs", Context.MODE_PRIVATE)
        prefs.edit().putInt("daily_base_steps", initialSteps).apply()

        // Başlangıçta bildirimi bas
        startForeground(NOTIFICATION_ID, getNotification(initialSteps))
        handler.post(periodicTask)
        return START_STICKY 
    }

    override fun onDestroy() {
        super.onDestroy()
        sensorManager?.unregisterListener(this)
        handler.removeCallbacks(periodicTask)
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event?.sensor?.type == Sensor.TYPE_STEP_COUNTER) {
            val sensorValue = event.values[0].toInt()
            val prefs = getSharedPreferences("StepPrefs", Context.MODE_PRIVATE)
            
            // DELTA MOTORU İÇİN HAM VERİYİ KAYDET
            prefs.edit().putInt("raw_sensor", sensorValue).apply()

            val currentDate = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(java.util.Date())
            val savedDate = prefs.getString("saved_date", "")

            var offset = prefs.getInt("offset_steps", -1)
            var dailyBase = prefs.getInt("daily_base_steps", 0)

            // GECE YARISI VEYA İLK BAŞLANGIÇ KONTROLÜ
            if (offset == -1 || savedDate != currentDate || sensorValue < offset) {
                offset = sensorValue
                
                // Gün değişmişse taban sayıyı (JS'ten gelen) sıfırla ki sabaha 0 kalsın
                if (savedDate != currentDate && savedDate != "") {
                    dailyBase = 0
                    prefs.edit().putInt("daily_base_steps", 0).apply()
                }
                
                prefs.edit()
                    .putInt("offset_steps", offset)
                    .putString("saved_date", currentDate)
                    .apply()
            }

            // BİLDİRİMDE GÖRÜNEN SAYI = (Şu anki - Ofset) + JS'ten gelen taban adım
            val displaySteps = (sensorValue - offset) + dailyBase
            notificationManager.notify(NOTIFICATION_ID, getNotification(displaySteps))

            // JS'E "HAM" VERİYİ GÖNDER! (Görsel sayıyı değil)
            ExpoBackgroundServiceModule.instance?.sendEvent("onStepUpdate", mapOf(
                "steps" to sensorValue 
            ))
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
    override fun onBind(intent: Intent?): IBinder? = null

    private fun getNotification(steps: Int): Notification {
        val finalBody = "$steps $customBody"
        return NotificationCompat.Builder(this, "step_channel")
            .setContentTitle(customTitle)
            .setContentText(finalBody)
            .setSmallIcon(android.R.drawable.ic_menu_mylocation)
            .setOnlyAlertOnce(true)
            .build()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val serviceChannel = NotificationChannel("step_channel", "Adım Sayar", NotificationManager.IMPORTANCE_LOW)
            notificationManager.createNotificationChannel(serviceChannel)
        }
    }
}