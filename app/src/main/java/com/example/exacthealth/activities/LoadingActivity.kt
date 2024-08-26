package com.example.exacthealth.activities

import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import com.example.exacthealth.R
import com.example.exacthealth.models.BodyTemperatureViewModel
import com.example.exacthealth.models.CaloriesBurnedViewModel
import com.example.exacthealth.models.DistanceViewModel
import com.example.exacthealth.models.HealthDataViewModel
import com.example.exacthealth.models.HeartRateViewModel
import com.example.exacthealth.models.SleepSessionsViewModel
import com.example.exacthealth.models.StepCountsViewModel
import kotlinx.coroutines.launch

class LoadingActivity : AppCompatActivity()
{
    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_loading)

        val heartRateViewModel = HeartRateViewModel(application)
        val stepCountsViewModel = StepCountsViewModel(application)
        val distanceViewModel = DistanceViewModel(application)
        val bodyTemperatureViewModel = BodyTemperatureViewModel(application)
        val caloriesBurnedViewModel = CaloriesBurnedViewModel(application)
        val sleepSessionsViewModel = SleepSessionsViewModel(application)

        if (isHealthConnectAvailable())
        {
            checkAllPermissionsAndRun(heartRateViewModel,
                                      stepCountsViewModel,
                                      distanceViewModel,
                                      bodyTemperatureViewModel,
                                      caloriesBurnedViewModel,
                                      sleepSessionsViewModel)
        }
        else
        {
            Toast.makeText(this, "Health Connect is not available", Toast.LENGTH_SHORT).show()
        }

        heartRateViewModel.heartRates.observe(this, Observer { heartRates ->
            val heartRatePairs = heartRateViewModel.formatHeartRateRecords(heartRates)
            val temp = heartRatePairs.ifEmpty { listOf("Null" to 0) }
        })

        stepCountsViewModel.stepCounts.observe(this, Observer { stepCounts ->
            val stepCountsPairs = stepCountsViewModel.formatStepCountsRecords(stepCounts)
            val temp = stepCountsPairs.ifEmpty { listOf("Null" to 0) }
        })

        distanceViewModel.distanceRecord.observe(this, Observer { distanceRecord ->
            val distanceRecordPairs = distanceViewModel.formatDistanceRecords(distanceRecord)
            val temp = distanceRecordPairs.ifEmpty { listOf("Null" to 0) }
        })

        caloriesBurnedViewModel.caloriesBurnedRecord.observe(this, Observer { caloriesBurnedRecord ->
            val caloriesBurnedRecordPairs = caloriesBurnedViewModel.formatCaloriesBurnedRecords(caloriesBurnedRecord)
            val temp = caloriesBurnedRecordPairs.ifEmpty { listOf("Null" to 0) }
        })

        sleepSessionsViewModel.sleepSessionRecord.observe(this, Observer { sleepSessionRecord ->
            val sleepSessionRecordPairs = sleepSessionsViewModel.formatSleepSessionRecords(sleepSessionRecord)
            val temp = sleepSessionRecordPairs.ifEmpty { listOf("Null" to 0) }
        })

        bodyTemperatureViewModel.bodyTemp.observe(this, Observer { bodyTemp ->
            val bodyTempPairs = bodyTemp.count()
        })
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

//    private fun checkPermissionsAndRun(healthDataViewModel: HealthDataViewModel)
//    {
//        val permissions = healthDataViewModel.permissions
//
//        val requestPermissionsLauncher = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissionsResult ->
//            if (permissionsResult.values.all { it })
//            {
//                lifecycleScope.launch {
//                    healthDataViewModel.readData()
//                }
//            }
//            else
//            {
//                Toast.makeText(this, "Permissions not granted", Toast.LENGTH_SHORT).show()
//            }
//        }
//
//        lifecycleScope.launch {
//            val permissionsGranted = healthDataViewModel.hasAllPermissions()
//
//            if (!permissionsGranted)
//            {
//                requestPermissionsLauncher.launch(permissions.map { it.toString() }.toTypedArray())
//            }
//            else
//            {
//                healthDataViewModel.readData()
//            }
//        }
//    }

    private fun checkAllPermissionsAndRun(vararg healthDataViewModels: HealthDataViewModel)
    {
        // Combine permissions from all ViewModels
        val allPermissions = healthDataViewModels.flatMap { it.permissions }.distinct() // To ensure there are no duplicates
            .map { it.toString() }.toTypedArray()

        val requestPermissionsLauncher = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissionsResult ->
            if (permissionsResult.values.all { it })
            {
                // If all permissions are granted, read data from all ViewModels
                lifecycleScope.launch {
                    healthDataViewModels.forEach { it.readData() }
                }
            }
            else
            {
                Toast.makeText(this, "Permissions not granted", Toast.LENGTH_SHORT).show()
            }
        }

        lifecycleScope.launch {
            val allPermissionsGranted = healthDataViewModels.all { it.hasAllPermissions() }

            if (!allPermissionsGranted)
            {
                // Request all permissions at once
                requestPermissionsLauncher.launch(allPermissions)
            }
            else
            {
                // Permissions are already granted, read data from all ViewModels
                healthDataViewModels.forEach { it.readData() }
            }
        }
    }
}