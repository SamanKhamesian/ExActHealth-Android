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
import com.example.exacthealth.models.CaloriesBurnedViewModel
import com.example.exacthealth.models.ExerciseSessionViewModel
import com.example.exacthealth.models.HeartRateViewModel
import com.example.exacthealth.models.SleepSessionViewModel
import com.example.exacthealth.models.StepCountsViewModel
import kotlinx.coroutines.launch

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
        val sleepSessionViewModel = SleepSessionViewModel(application)
        val exerciseSessionViewModel = ExerciseSessionViewModel(application)
        val caloriesBurnedViewModel = CaloriesBurnedViewModel(application)

        val healthDataViewModels = arrayOf(heartRateViewModel, stepCountsViewModel, sleepSessionViewModel, exerciseSessionViewModel, caloriesBurnedViewModel)

        val allPermissions = healthDataViewModels.flatMap {it.permissions}.distinct().map {it.toString()}.toTypedArray()

        val requestPermissionsLauncher = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {permissionsResult ->
            if (permissionsResult.values.all {it})
            {
                // Permissions granted, observe data
                observeDataAndProceed(heartRateViewModel, stepCountsViewModel, sleepSessionViewModel, exerciseSessionViewModel, caloriesBurnedViewModel)
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
                observeDataAndProceed(heartRateViewModel, stepCountsViewModel, sleepSessionViewModel, exerciseSessionViewModel, caloriesBurnedViewModel)
            }
        }
    }

    private fun observeDataAndProceed(heartRateViewModel: HeartRateViewModel,
                                      stepCountsViewModel: StepCountsViewModel,
                                      sleepSessionViewModel: SleepSessionViewModel,
                                      exerciseSessionViewModel: ExerciseSessionViewModel,
                                      caloriesBurnedViewModel: CaloriesBurnedViewModel)
    {
        val readinessMap =
            mutableMapOf("heartRate" to false, "stepCounts" to false, "sleepSession" to false, "exerciseSession" to false, "caloriesBurned" to false)

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
        sleepSessionViewModel.readData()
        exerciseSessionViewModel.readData()
        caloriesBurnedViewModel.readData()

        // Observe each ViewModel
        observeModelData(heartRateViewModel.heartRates, heartRateViewModel::formatHeartRateRecords) {data ->
            healthSharedPreferencesManager.saveHeartRate(data)
            readinessMap["heartRate"] = true
            checkAllReady()
        }

        observeModelData(stepCountsViewModel.stepCounts, stepCountsViewModel::formatStepCountsRecords) {data ->
            healthSharedPreferencesManager.saveStepCounts(data)
            readinessMap["stepCounts"] = true
            checkAllReady()
        }

        observeModelData(sleepSessionViewModel.sleepSessionRecord, sleepSessionViewModel::formatSleepSessionRecords) {data ->
            healthSharedPreferencesManager.saveSleepStage(data)
            readinessMap["sleepSession"] = true
            checkAllReady()
        }

        observeModelData(exerciseSessionViewModel.exerciseSessions, exerciseSessionViewModel::formatExerciseSessionRecords) {data ->
            healthSharedPreferencesManager.saveExerciseSession(data)
            readinessMap["exerciseSession"] = true
            checkAllReady()
        }

        observeModelData(caloriesBurnedViewModel.caloriesBurned, caloriesBurnedViewModel::formatCaloriesBurnedRecords) {data ->
            healthSharedPreferencesManager.saveCaloriesBurned(data)
            readinessMap["caloriesBurned"] = true
            checkAllReady()
        }
    }

    private fun <T, R> observeModelData(liveData: LiveData<T>, formatFunction: (T) -> R, onDataReady: (R) -> Unit)
    {
        liveData.observe(this, Observer {data ->
            val formattedData = formatFunction(data)
            onDataReady(formattedData)
        })
    }
}
