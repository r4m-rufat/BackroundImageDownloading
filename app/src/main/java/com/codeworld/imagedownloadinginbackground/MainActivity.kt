package com.codeworld.imagedownloadinginbackground

import android.app.job.JobInfo
import android.app.job.JobScheduler
import android.content.ComponentName
import android.content.pm.PackageManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.PersistableBundle
import android.widget.EditText
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat

private const val DOWNLOAD_IMAGE_JOB = 100
const val IMAGE_URL = "image_url"

class MainActivity : AppCompatActivity() {

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        requestPermission()

    }

    private fun scheduleJob() {

        val bundle = PersistableBundle()
        bundle.putString(IMAGE_URL, findViewById<EditText>(R.id.editImageUrl).text.toString())

        val info  = JobInfo.Builder(DOWNLOAD_IMAGE_JOB, ComponentName(applicationContext, BackgroundJob::class.java))
            .setPeriodic(15 * 60 * 1000)
            .setRequiredNetworkType(JobInfo.NETWORK_TYPE_UNMETERED)
            .setPersisted(true)
            .setExtras(bundle)
            .build();
        val jobScheduler = getSystemService(JOB_SCHEDULER_SERVICE) as JobScheduler
        jobScheduler.schedule(info)

    }


    private fun requestPermission() {

        val requestPermission = registerForActivityResult(ActivityResultContracts.RequestPermission()) {isGranted ->

            if (isGranted)
                scheduleJob()
            else
                Toast.makeText(
                    this,
                    "Job cannot be started without granted permission!",
                    Toast.LENGTH_SHORT
                ).show()

        }

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED){
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
                requestPermission.launch(android.Manifest.permission.POST_NOTIFICATIONS)

        }

    }

}