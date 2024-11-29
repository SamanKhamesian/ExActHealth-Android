package com.example.exacthealth.activities

import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import com.example.exacthealth.R
import com.example.exacthealth.classes.HealthSharedPreferencesManager
import com.example.exacthealth.models.BodyTemperatureViewModel
import com.example.exacthealth.models.CaloriesBurnedViewModel
import com.example.exacthealth.models.DistanceViewModel
import com.example.exacthealth.models.HeartRateViewModel
import com.example.exacthealth.models.SleepSessionViewModel
import com.example.exacthealth.models.SleepStage
import com.example.exacthealth.models.StepCountsViewModel
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class LoadingActivity: AppCompatActivity()
{
    private lateinit var healthSharedPreferencesManager: HealthSharedPreferencesManager

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_loading)

        healthSharedPreferencesManager = HealthSharedPreferencesManager(this)

        // Check if Health Connect is installed
        if (!isHealthConnectAvailable())
        {
            redirectToPlayStore()
            return
        }

        // Check permissions and proceed
        checkPermissionsAndObserveData()
    }

    private fun isHealthConnectAvailable(): Boolean
    {
        val packageManager = packageManager
        return try
        {
            packageManager.getPackageInfo("com.google.android.apps.healthdata", 0)
            true
        }
        catch (e: PackageManager.NameNotFoundException)
        {
            false
        }
    }

    private fun redirectToPlayStore()
    {
        Toast.makeText(this, "Please install Health Connect first.", Toast.LENGTH_LONG).show()
        try
        {
            // Open the Google Play Store app
            val intent = Intent(Intent.ACTION_VIEW).apply {
                data = Uri.parse("market://details?id=com.google.android.apps.healthdata")
                // Ensure it's opened in the Play Store app
                setPackage("com.android.vending")
            }
            startActivity(intent)
        }
        catch (e: ActivityNotFoundException)
        {
            // Fallback to browser if the Play Store app is unavailable
            val intent = Intent(Intent.ACTION_VIEW).apply {
                data = Uri.parse("https://play.google.com/store/apps/details?id=com.google.android.apps.healthdata")
            }
            startActivity(intent)
        }

        // Exit the app after redirection
        finishAffinity()
    }

    private fun checkPermissionsAndObserveData()
    {
        val heartRateViewModel = HeartRateViewModel(application)
        val stepCountsViewModel = StepCountsViewModel(application)
        val distanceViewModel = DistanceViewModel(application)
        val bodyTemperatureViewModel = BodyTemperatureViewModel(application)
        val caloriesBurnedViewModel = CaloriesBurnedViewModel(application)
        val sleepSessionViewModel = SleepSessionViewModel(application)

        val healthDataViewModels =
            arrayOf(heartRateViewModel, stepCountsViewModel, distanceViewModel, bodyTemperatureViewModel, caloriesBurnedViewModel, sleepSessionViewModel)

        val allPermissions = healthDataViewModels.flatMap {it.permissions}.distinct().map {it.toString()}.toTypedArray()

        val requestPermissionsLauncher = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {permissionsResult ->
            if (permissionsResult.values.all {it})
            {
                // Permissions granted, observe data
                observeDataAndProceed(heartRateViewModel,
                                      stepCountsViewModel,
                                      distanceViewModel,
                                      bodyTemperatureViewModel,
                                      caloriesBurnedViewModel,
                                      sleepSessionViewModel)
            }
            else
            {
                Toast.makeText(this, "Permissions not granted", Toast.LENGTH_SHORT).show()
            }
        }

        lifecycleScope.launch {
            val allPermissionsGranted = healthDataViewModels.all {it.hasAllPermissions()}

            if (!allPermissionsGranted)
            {
                // Request all permissions
                requestPermissionsLauncher.launch(allPermissions)
            }
            else
            {
                // Permissions already granted
                observeDataAndProceed(heartRateViewModel,
                                      stepCountsViewModel,
                                      distanceViewModel,
                                      bodyTemperatureViewModel,
                                      caloriesBurnedViewModel,
                                      sleepSessionViewModel)
            }
        }
    }

    private fun observeDataAndProceed(heartRateViewModel: HeartRateViewModel,
                                      stepCountsViewModel: StepCountsViewModel,
                                      distanceViewModel: DistanceViewModel,
                                      bodyTemperatureViewModel: BodyTemperatureViewModel,
                                      caloriesBurnedViewModel: CaloriesBurnedViewModel,
                                      sleepSessionViewModel: SleepSessionViewModel)
    {
        // Default value with current date and time
        val yesterdayDateTime = LocalDateTime.now().minusDays(1).format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))
        val defaultValue = listOf(yesterdayDateTime to 0)
        val defaultSleepStage =
            listOf(SleepStage(startTime = yesterdayDateTime, endTime = yesterdayDateTime, duration = 0L, stageCode = 0, stageName = "Unknown Stage"))

        val readinessMap = mutableMapOf("heartRate" to false,
                                        "stepCounts" to false,
                                        "sleepSession" to false,
                                        "distance" to true,
                                        "bodyTemperature" to true,
                                        "caloriesBurned" to true)

        val checkAllReady = {
            if (readinessMap.values.all {it})
            {
                Handler(Looper.getMainLooper()).postDelayed({
                                                                val intent = Intent(this@LoadingActivity, CalendarActivity::class.java)
                                                                startActivity(intent)
                                                                finish()
                                                            }, 2500)
            }
        }

        // Call readData explicitly to fetch data
        heartRateViewModel.readData()
        stepCountsViewModel.readData()
        distanceViewModel.readData()
        bodyTemperatureViewModel.readData()
        caloriesBurnedViewModel.readData()
        sleepSessionViewModel.readData()

        // Observe each ViewModel
        observeModelData(heartRateViewModel.heartRates, heartRateViewModel::formatHeartRateRecords) {data ->

            val heartRateData = data.ifEmpty {defaultValue}
            healthSharedPreferencesManager.saveHeartRate(heartRateData)
            readinessMap["heartRate"] = true
            checkAllReady()
        }

        observeModelData(stepCountsViewModel.stepCounts, stepCountsViewModel::formatStepCountsRecords) {data ->

            val stepCountsData = data.ifEmpty {defaultValue}
            healthSharedPreferencesManager.saveStepCounts(stepCountsData)
            readinessMap["stepCounts"] = true
            checkAllReady()
        }

        observeModelData(sleepSessionViewModel.sleepSessionRecord, sleepSessionViewModel::formatSleepSessionRecords) {data ->
            val sleepStageData = data.ifEmpty {defaultSleepStage}
            healthSharedPreferencesManager.saveSleepStage(sleepStageData)
            readinessMap["sleepSession"] = true
            checkAllReady()
        }

        distanceViewModel.distanceRecord.observe(this, Observer {distanceRecord ->
            val distanceRecordPairs = distanceViewModel.formatDistanceRecords(distanceRecord)
            val temp = distanceRecordPairs.ifEmpty {listOf("Null" to 0)}
        })

        caloriesBurnedViewModel.caloriesBurnedRecord.observe(this, Observer {caloriesBurnedRecord ->
            val caloriesBurnedRecordPairs = caloriesBurnedViewModel.formatCaloriesBurnedRecords(caloriesBurnedRecord)
            val temp = caloriesBurnedRecordPairs.ifEmpty {listOf("Null" to 0)}
        })

        bodyTemperatureViewModel.bodyTemp.observe(this, Observer {bodyTemp ->
            val bodyTempPairs = bodyTemp.count()
        })
    }

    private fun <T, R> observeModelData(liveData: LiveData<T>, formatFunction: (T) -> R, onDataReady: (R) -> Unit)
    {
        liveData.observe(this, Observer {data ->
            val formattedData = formatFunction(data)
            onDataReady(formattedData)
        })
    }
}
