package com.example.exacthealth.activities

import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import com.example.exacthealth.R
import com.example.exacthealth.classes.HealthDataViewModel
import com.example.exacthealth.classes.HeartRateViewModel
import com.example.exacthealth.classes.StepCountsViewModel
import kotlinx.coroutines.launch

class LoadingActivity : AppCompatActivity()
{
    private lateinit var finalHeartRateRecordList: List<Pair<String, Int>>
    private lateinit var finalStepCountsRecordList: List<Pair<String, Int>>

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_loading)

        val heartRateViewModel = HeartRateViewModel(application)
        val stepCountsViewModel = StepCountsViewModel(application)

        if (isHealthConnectAvailable())
        {
            checkPermissionsAndRun(heartRateViewModel)
            checkPermissionsAndRun(stepCountsViewModel)
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

    private fun checkPermissionsAndRun(healthDataViewModel: HealthDataViewModel)
    {
        val permissions = healthDataViewModel.permissions

        val requestPermissionsLauncher = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissionsResult ->
            if (permissionsResult.values.all { it })
            {
                lifecycleScope.launch {
                    healthDataViewModel.readData()
                }
            }
            else
            {
                Toast.makeText(this, "Permissions not granted", Toast.LENGTH_SHORT).show()
            }
        }

        lifecycleScope.launch {
            val permissionsGranted = healthDataViewModel.hasAllPermissions()

            if (!permissionsGranted)
            {
                requestPermissionsLauncher.launch(permissions.map { it.toString() }.toTypedArray())
            }
            else
            {
                healthDataViewModel.readData()
            }
        }
    }
}