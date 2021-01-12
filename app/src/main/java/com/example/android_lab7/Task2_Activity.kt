package com.example.android_lab7

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.task2_receiver.*

class Task2_Activity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.task2_receiver)

        val location = intent?.getStringExtra("location")

        if (location != null) {
            receiver.text = "Наша картинка расположена в $location"
        }
        Log.i("Task2", "Мы вернулись в активити и положили местоположение в поле")
    }
}