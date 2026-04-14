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
    
    private var customTitle: String = "StepMap Takip"
    private var customBody: String = "Adım"

    private val handler = Handler(Looper.getMainLooper())
    private val periodicTask = object : Runnable {
        override fun run() {
            val prefs = getSharedPreferences("StepPrefs", Context.MODE_PRIVATE)
            val currentDisplaySteps = prefs.getInt("exact_notification_steps", 0) 
            
            ExpoBackgroundServiceModule.instance?.sendEvent("onTimerTick", mapOf(
                "steps" to currentDisplaySteps
            ))

            val serviceIntent = Intent(applicationContext, ExpoBackgroundServiceHeadlessTaskService::class.java)
            val bundle = Bundle()
            bundle.putInt("steps", currentDisplaySteps)
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
        val prefs = getSharedPreferences("StepPrefs", Context.MODE_PRIVATE)

        // NULL INTENT KORUMASI: Sistem servisi dirilttiğinde intent null gelir!
        if (intent != null && intent.hasExtra("notificationTitle")) {
            customTitle = intent.getStringExtra("notificationTitle") ?: "StepMap Takip"
            customBody = intent.getStringExtra("notificationBody") ?: "Adım"
            val initialSteps = intent.getIntExtra("initialSteps", 0)
            
            prefs.edit()
                .putString("saved_title", customTitle)
                .putString("saved_body", customBody)
                .putInt("daily_base_steps", initialSteps)
                .apply()
        } else {
            // Servis sistem tarafından geri diriltildi, eski başlıkları hafızadan çek!
            customTitle = prefs.getString("saved_title", "StepMap Takip") ?: "StepMap"
            customBody = prefs.getString("saved_body", "Adım") ?: "Adım"
        }

        // Çökmeyi engellemek için bildirimi anında bas!
        val lastKnownSteps = prefs.getInt("exact_notification_steps", 0)
        startForeground(NOTIFICATION_ID, getNotification(lastKnownSteps))
        
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

            val currentDate = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(java.util.Date())
            val savedDate = prefs.getString("saved_date", "")

            var offset = prefs.getInt("offset_steps", -1)
            var dailyBase = prefs.getInt("daily_base_steps", 0)

            // GECE YARISI SIFIRLAMASI
            if (offset == -1 || savedDate != currentDate || sensorValue < offset) {
                offset = sensorValue
                
                if (savedDate != currentDate && savedDate != "") {
                    dailyBase = 0
                    // Gün değiştiyse, eski net adımı da sıfırla!
                    prefs.edit().putInt("exact_notification_steps", 0).apply() 
                }
                
                prefs.edit()
                    .putInt("offset_steps", offset)
                    .putInt("daily_base_steps", dailyBase)
                    .putString("saved_date", currentDate)
                    .apply()
            }

            val displaySteps = (sensorValue - offset) + dailyBase
            
            prefs.edit().putInt("exact_notification_steps", displaySteps).apply()
            notificationManager.notify(NOTIFICATION_ID, getNotification(displaySteps))

            ExpoBackgroundServiceModule.instance?.sendEvent("onStepUpdate", mapOf(
                "steps" to displaySteps 
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
            .setOngoing(true) // Kullanıcı bildirimi yana kaydırıp silemesin
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