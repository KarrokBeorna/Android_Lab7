# Цели

Получить практические навыки разработки сервисов (started и bound) и Broadcast Receivers.

## Задача 1 - Started сервис для скачивания изображения

В лабораторной сказано, что нужно использовать код из 6 работы, а там только AsyncTask, который устарел и корутины. Что поделать, буду использовать корутины.

__Листинг 1.1 - Task1_Activity.kt__

    class Task1_Activity : AppCompatActivity() {
        private val imageURL = "https://b1.m24.ru/c/773088.483xp.jpg"

        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            setContentView(R.layout.activity_main)
            Log.i("Task3", "UI поток - " + Thread.currentThread().name)

            started.setOnClickListener {
                startService(Intent(this, Task1_Service::class.java)
                    .putExtra("imageURL", imageURL))
            }
        }
    }

__Листинг 1.2 - Task1_Service.kt__

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

__Листинг 1.3 - Сервис в Манифесте__

    <service
        android:name=".Task1_Service"
        android:enabled="true"
        android:exported="true">
    </service>

Суть задания: скачать изображение, однако показывать его нигде не надо, всего лишь показать путь к нему.

1. В активити также объявляем `URL` картинки (в этот раз я взял покороче URL, чтобы он меня не раздражал своим огромным числом циферок на конце, но это по-прежнему лиса).
2. В `onCreate()` мы также отображаем всё на экран (в нашем случае всего лишь кнопка с надписью "Start Service"); смотрим, в каком потоке мы находимся (для сравнения, что скачивание происходит не в ui-потоке); вешаем слушателя на кнопку и в нём мы будем запускать сервис с нашим imageURL, который положим в данные Intent'a при помощи `putExtra()` по такому же ключу, как и название переменной.
3. Так как в данной задаче мы используем `started service`, то `onBind() = null`.
4. В методе `onStartCommand()` мы будем скачивать нашу картинку с помощью корутины, здесь, как и в 6 работе (3 задача), мы будем использовать `диспетчер IO`. В нём же посмотрим на поток, в котором будем происходить скачивание (в Logcat'e заметим, что это не ui-поток). После чего мы загрузим и сохраним нашу картинку с помощью функции `downAndLoc()`. Сохраняем картинку с помощью `Bitmap.Compress()`. Определим путь с помощью метода `absolutePath`, который будет вызван на нашем файле (`filesDir` выдает как раз путь до локального хранилища приложения, после чего мы прибавляем к нему наше рандомное имя скачанного файла).
5. Для задачи 2 нам необходимо отправить широковещательное сообщение, в котором будет опять-таки содержаться Intent, однако в этот раз не с URL, а с местоположением файла. Сделаем это с помощью метода `sendBroadcast()`.
6. Останавливаем службу с помощью метода `stopSelf()`
7. `int START_NOT_STICKY`, как выразился Андрей Николаевич "Умер и умер, запустим новый сервис, если потребуется".
8. В методе `onDestroy()` мы отменяем нашу корутину.
9. В манифесте мы просто прописываем наш сервис.

## Задача 2 - Broadcast Receiver

Здесь мы используем предыдущее наше приложение, в котором мы уже отправили наше широковещательное сообщение, теперь нам осталось лишь принять его и положить в `TextView`.

__Листинг 2.1 - Task2_Activity.kt__

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

__Листинг 2.2 - Task2_Receiver.kt__

    class Task2_Receiver : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {

            val location = intent?.getStringExtra("location")

            Log.i("Task2", "Мы забрали местоположение файла")

            context?.startActivity(Intent(context, Task2_Activity::class.java)
                .putExtra("location", location))
        }
    }

__Листинг 2.3 - Ресивер в Манифесте__

    <receiver
        android:name=".Task2_Receiver"
        android:exported="true"
        android:enabled="true" >
        <intent-filter>
            <action android:name="com.example.android_lab7.SUCCESS" />
        </intent-filter>
    </receiver>

1. Стартовать приложение будет также с нашего `Task1_Activity`, при нажатии на кнопку будет отправляться широковещательное сообщение, а `BroadcastReceiver` будет принимать его.
2. Обработка содержимого `Intent'a` производится с помощью `getStringExtra(key)`.
3. После этого при помощи `startActivity()` будем запускать нашу новую активити, на которой расположено лишь одно текстовое поле.
4. При запуске активити мы также обрабатываем `intent`, и если содержимое не пустое, то наполняем им текстовое поле.

## Задача 3 - Bound Service для скачивания изображения

Суть задания: если started service, то как и в 1 задании, сообщение принимает ресивер, если bound service - принимаем результат сами же.

Теперь мы добавляем к нашему Task1_Activity `Handler` для мессенджера и интерфейс `ServiceConnection` для соединения с сервисом. В самом сервисе также добавится `Handler` для обработки полученного сообщения

__Листинг 3.1 - Task1_Activity.kt (дополненная версия)__

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

__Листинг 3.2 - Task3_Messenger.kt__

    class Task3_Messenger : Service() {

        private var mIcon11: Bitmap? = null
        private var job: Job? = null

        inner class  MyHandler : Handler() {
            override fun handleMessage(msg: Message) {
                when (msg.what) {
                    1 -> {
                        val replyTo = msg.replyTo
                        val imageURL = msg.obj as String
                        job = CoroutineScope(Dispatchers.IO).launch {
                            Log.i("Task3", "Bound Service - " + Thread.currentThread().name)

                            val location = downAndLoc(imageURL)

                            Log.i("Task3", "Получили местоположение: $location")
                            val message = Message.obtain(null, 1)
                            message.obj = location
                            replyTo.send(message)
                        }
                    }
                    else -> super.handleMessage(msg)
                }
            }
        }

        override fun onBind(intent: Intent?): IBinder? = Messenger(MyHandler()).binder

        override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
            val imageURL = intent?.getStringExtra("imageURL")

            if (imageURL != null) {
                job = CoroutineScope(Dispatchers.IO).launch {
                    Log.i("Task3", "Started Service - " + Thread.currentThread().name)

                    val location = downAndLoc(imageURL)

                    sendBroadcast(Intent("com.example.android_lab7.SUCCESS")
                        .putExtra("location", location))
                    stopSelf()

                    Log.i("Task3", "Картинка сохранена в $location. Сервис остановлен")
                }
            } else {
                stopSelf()
                Log.i("Task3", "URL пустой, сервис остановлен")
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
            Log.i("Task3", "Корутина отменена")
            super.onDestroy()
        }
    }

1. Как и в первой задаче, добавляем сервис в Манифест.
2. Добавляем переменную в активити для слежки за соединением с сервисом - `connected`.
3. Добавляем `inner class MyHadler`, где мы будем обрабатывать полученное от сервиса сообщение - вставлять в текстовое поле путь до скачанной картинки.
4. Добавляем интерфейс `ServiceConnection`, который будет устанавливать связь с сервисом при нажатии клавиши `Bind`, а при нажатии на `Unbind` в Handler будет передано сообщение, которое обработается и поместится в текстовое поле, при этом связь с сервисом будет разорвана при помощи метода `unbindService(connection)`. Стоит заметить, что вызов метода `onServiceDisconnected()` __НЕ__ будет происходить при вызове метода `unbindService()`. В руководстве написано, что данный метод предназначен лишь для неожиданных обрывов с сервисом, когда служба аварийно закончила свою работу или была остановлена.
5. В методе `onCreate()` теперь мы добавлем слушателей для 3 кнопок. Первая - работает как в первой задаче - запускает started service. Вторая - присоединяется к сервису через `bindService()`, внутрь мы передаем Intent, ServiceConnection и флаг для запуска сервиса. Третья - создает класс Мессенджера, внутрь мы передаем наш Handler; также создаем наше сообщение с URL картинки. Метод `obtain()` служит для наполнения целью и флагом в what. `replyTo` - наш адрес мессенджера. `obj` - наш URL. `send()` - отправление сообщения.
6. В классе сервиса у нас также добавился Handler, который будет принимать наше сообщение с URL картинки.
7. Метод onBind() у нас теперь не null, что говорит о том, что теперь мы не просто started, но еще и bound. Здесь мы получаем объект IBinder с помощью метода binder(), вызванного на мессенджере с переданным ему Handler'ом.
8. Дальше всё осталось также, как и в первой задаче.

Вот так выглядит экран с нашими кнопками и текстовыми полями:

![](https://github.com/KarrokBeorna/Android_Lab7/blob/master/images/4.png)

- Первая картинка - Экран после нажатия на `Start Service`
- Вторая картинка - Как выглядит экран до отвязки от сервиса
- Третья картинка - После отвязки от сервиса

# Выводы

Выполнял работу с 15 дня до 11 утра без перерыва - 20 часов получается.

Собственно, мне понравилось то, что задания связаны друг с другом - это уже создает образ, будто делаешь проект, как в каком-нибудь 2 или 3 семестре.

Задание 1 весьма простое, потому что ты практически полностью копипастишь код из 6 работы. Проблемы вызвало лишь сохранение картинки, я не мог понять, сохраняется картинка или нет... Я почему-то считал, что когда мы декодируем поток, то автоматически сохраняем, но это не так.

Задание 2 очень простое, потому что мы в первой задаче уже сделали всё необходимое. Здесь нужно было лишь "взять-отправить".

Задание 3 смогло подогреть пятую точку. Во-первых, onServiceDisconnected(), я около часа пытался понять, почему он не вызывается, хотя явно видно, что мы отсоединяемся от сервиса, так как кнопка unbind больше ничего не изменяла, а bind соединял нас заново. Во-вторых, долго разбирался что-кому-куда передается, чтобы брать это и использовать. Например, я не сразу понял, как мне в активити ловить сообщение, ведь в сервисе есть Handler, который всё делает; я думал, что в активити мы лишь соединяемся и откуда-то получаем результат, но всё оказалось тривиально: "отправил-подождал-получил-обработал".

Что ж, курс программирования на Андроид подошел к концу. Было весьма интересно слушать Андрея Николаевича (добрый молодой человек), также рад был попробовать сдавать лабораторные новому преподавателю - Артёму Олеговичу (также очень спокойный и добрый).
