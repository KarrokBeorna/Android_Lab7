package com.example.android_lab7

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

class Task2_Receiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {

        val location = intent?.getStringExtra("location")

        Log.i("Task2", "Мы забрали местоположение файла")

        context?.startActivity(Intent(context, Task2_Activity::class.java)
            .putExtra("location", location))
    }
}