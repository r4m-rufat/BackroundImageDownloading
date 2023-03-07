package com.codeworld.imagedownloadinginbackground

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.job.JobParameters
import android.app.job.JobService
import android.content.ContentValues.TAG
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Build
import android.os.Environment
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.Default
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.*
import java.net.HttpURLConnection
import java.net.URL

private const val ID = "NOTIFICATION_ID"

@Suppress("NAME_SHADOWING")
class BackgroundJob : JobService() {

    private var imagePath: String? = null

    override fun onStartJob(params: JobParameters?): Boolean {
        CoroutineScope(Default).launch {

            loadImage(params?.extras?.getString(IMAGE_URL, ""))
            
            Log.d(TAG, "onStartJob: ${params?.extras?.getString(IMAGE_URL, "")}")

        }
        return true
    }

    override fun onStopJob(params: JobParameters?): Boolean {
        return true
    }

    private suspend fun loadImage(url: String?): Bitmap? {

        withContext(IO) {

            val url = URL(url)
            val connection: HttpURLConnection

            Log.d(TAG, "loadImage: image started")

            try {
                connection = url.openConnection() as HttpURLConnection
                connection.connect()
                val inputStream: InputStream = connection.inputStream
                val bufferedInputStream = BufferedInputStream(inputStream)

                val filename = "${System.currentTimeMillis()}.jpg"
                var fileOutputStream: OutputStream? = null

                var imagePath = applicationContext.getExternalFilesDir(Environment.DIRECTORY_PICTURES)?.path

                imagePath = "$imagePath/$filename"
                Log.d(TAG, "loadImage: $imagePath")

                val file = File(imagePath)
                fileOutputStream = FileOutputStream(file)

                createNotificationChannelAndNotify()

                val bitmap = BitmapFactory.decodeStream(bufferedInputStream)

                bitmap?.compress(Bitmap.CompressFormat.JPEG, 100, fileOutputStream)
                fileOutputStream.flush()

            } catch (ignore: IOException) {
                Log.d(TAG, "loadImage: image downloading exception ${ignore.message}")
            }

        }

        return null

    }

    private fun createNotificationChannelAndNotify() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(ID, "Notification Name", importance)

            val notificationManager = getSystemService(
                NotificationManager::class.java
            )
            notificationManager.createNotificationChannel(channel)

            val builder: NotificationCompat.Builder = NotificationCompat.Builder(this, ID)
            builder.setContentTitle("Download")
            builder.setContentText(getString(R.string.notification_content, imagePath))
            builder.setSmallIcon(R.drawable.ic_launcher_background)
            builder.priority = NotificationCompat.PRIORITY_HIGH
            builder.setAutoCancel(true)
            builder.build()

            if (ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                NotificationManagerCompat.from(this).notify(100, builder.build())
                return
            }

        }
    }

}