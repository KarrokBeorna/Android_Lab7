package com.example.android_lab7

import android.app.Service
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.IBinder
import android.util.Log
import kotlinx.coroutines.*
import java.io.File
import java.io.InputStream
import java.net.URL
import kotlin.random.Random

class Task1_Service : Service() {

    private var mIcon11: Bitmap? = null
    private var job: Job? = null

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val imageURL = intent?.getStringExtra("imageURL")

        if (imageURL != null) {
            job = CoroutineScope(Dispatchers.IO).launch {
                Log.i("Task1", "Service поток - " + Thread.currentThread().name)

                val location = downAndLoc(imageURL)

                sendBroadcast(Intent("com.example.android_lab7.SUCCESS")
                    .putExtra("location", location))
                stopSelf()

                Log.i("Task1", "Картинка сохранена в $location. Сервис остановлен")
            }
        } else {
            stopSelf()
            Log.i("Task1", "URL пустой, сервис остановлен")
        }

        return START_NOT_STICKY
    }

    private fun downAndLoc(imageURL: String): String {
        try {
            val stream: InputStream = URL(imageURL).openStream()
            mIcon11 = BitmapFactory.decodeStream(stream)
        } catch (e: Exception) { }
        val file = "mIcon${Random.nextInt(0,10000)}.jpg"

        openFileOutput(file, MODE_PRIVATE).use {
            mIcon11?.compress(Bitmap.CompressFormat.JPEG, 75, it)
        }

        return File(filesDir, file).absolutePath
    }

    override fun onDestroy() {
        job?.cancel()
        Log.i("Task1", "Корутина отменена")
        super.onDestroy()
    }
}