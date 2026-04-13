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
    private var customBody: String = "Calculate your steps..."
    private val handler = Handler(Looper.getMainLooper())
    private val periodicTask = object : Runnable {
        override fun run() {
            val prefs = getSharedPreferences("StepPrefs", Context.MODE_PRIVATE)
            val currentSteps = prefs.getInt("real_steps", 0)
            
            // 1. JS TARAFINDAKİ FONKSİYONUNU TETİKLE!
            ExpoBackgroundServiceModule.instance?.sendEvent("onTimerTick", mapOf(
                "steps" to currentSteps,
                "message" to "Arka plandan selamlar, ben 60 saniyede bir çalışıyorum!"
            ))

            val serviceIntent = Intent(applicationContext, ExpoBackgroundServiceHeadlessTaskService::class.java)
            val bundle = Bundle()
            bundle.putInt("steps", currentSteps) // Adımı gönderiyoruz
            serviceIntent.putExtras(bundle)
            applicationContext.startService(serviceIntent) // JS'i dürttük!
            // Örnek: Eğer adım sayısı 10.000'i geçerse özel bir bildirim fırlat
            if (currentSteps >= 10000) {
                sendCustomNotification("Hedefe Ulaştın!", "10.000 adımı geçtin, tebrikler Şerif Abi!")
            }
            
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

        val prefs = getSharedPreferences("StepPrefs", Context.MODE_PRIVATE)
        val currentSteps = prefs.getInt("real_steps", 0)

        startForeground(NOTIFICATION_ID, getNotification(currentSteps))
        handler.post(periodicTask)
        return START_STICKY 
    }

    override fun onDestroy() {
        super.onDestroy()
        sensorManager?.unregisterListener(this)
        handler.removeCallbacks(periodicTask)
        println("Native: Servis tamamen durduruldu.")
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event?.sensor?.type == Sensor.TYPE_STEP_COUNTER) {
            val sensorValue = event.values[0].toInt()
            val prefs = getSharedPreferences("StepPrefs", Context.MODE_PRIVATE)
            var offset = prefs.getInt("offset_steps", -1)

            if (offset == -1 || sensorValue < offset) {
                offset = sensorValue
                prefs.edit().putInt("offset_steps", offset).apply()
            }

            val realSteps = sensorValue - offset
            prefs.edit().putInt("real_steps", realSteps).apply()

            notificationManager.notify(NOTIFICATION_ID, getNotification(realSteps))

            // 2. HER ADIMDA JS'İ TETİKLE (Canlı güncelleme için)
            ExpoBackgroundServiceModule.instance?.sendEvent("onStepUpdate", mapOf(
                "steps" to realSteps
            ))
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
    override fun onBind(intent: Intent?): IBinder? = null

    // YENİ: İSTEDİĞİN ZAMAN ÖZEL BİLDİRİM FIRLATMA FONKSİYONU
    private fun sendCustomNotification(title: String, message: String) {
        val builder = NotificationCompat.Builder(this, "step_channel")
            .setContentTitle(title)
            .setContentText(message)
            .setSmallIcon(android.R.drawable.ic_dialog_info) // Farklı bir ikon
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true) // Tıklanınca silinsin

        // 2 numaralı ID ile gönderiyoruz ki ana adım sayacını ezmesin
        notificationManager.notify(2, builder.build())
    }

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