package com.example.android_lab7


import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.*
import androidx.appcompat.app.AppCompatActivity
import android.util.Log
import kotlinx.android.synthetic.main.activity_main.*

class Task1_Activity : AppCompatActivity() {

    private val imageURL = "https://b1.m24.ru/c/773088.483xp.jpg"
    private var connected = false

    inner class MyHandlerMsg : Handler() {
        override fun handleMessage(msg: Message) {
            when (msg.what) {
                1 -> message.text = msg.obj as String
                else -> super.handleMessage(msg)
            }
        }
    }

    private val connection = object : ServiceConnection {
        var messenger: Messenger? = null

        override fun onServiceConnected(className: ComponentName?, service: IBinder?) {
            Log.i("Task3", "Сервис подключен")
            messenger = Messenger(service)
            connected = true
        }

        override fun onServiceDisconnected(arg0: ComponentName?) {
            Log.i("Task3", "Сервис отключен")
            messenger = null
            connected = false
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        Log.i("Task3", "UI поток - " + Thread.currentThread().name)

        started.setOnClickListener {
            // Для Task1 - started service
            //startService(Intent(this, Task1_Service::class.java)
            startService(Intent(this, Task3_Messenger::class.java)
                .putExtra("imageURL", imageURL))
        }

        bind.setOnClickListener {
            bindService(Intent(this, Task3_Messenger::class.java), connection, Context.BIND_AUTO_CREATE)
        }

        unbind.setOnClickListener {
            if (connected) {
                val messenger = Messenger(MyHandlerMsg())
                val message = Message.obtain(null, 1)
                message.replyTo = messenger
                message.obj = imageURL
                try {
                    connection.messenger?.send(message)
                } catch (e: Exception) { }
                unbindService(connection)
                connected = false
            }
        }
    }
}
