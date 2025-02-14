package com.example.exacthealth.activities

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.StrictMode
import android.util.Log
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
import com.example.exacthealth.classes.isInternetAvailable
import kotlinx.coroutines.launch
import java.net.HttpURLConnection
import java.net.URL

class LoadingActivity: AppCompatActivity()
{
    private lateinit var healthSharedPreferencesManager: HealthSharedPreferencesManager

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_loading)

        testConnection()
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

    private fun testConnection()
    {
        val sharedPreferences = getSharedPreferences("user_session", MODE_PRIVATE)
        val url = URL(LoginActivity.TEST_CONNECTION_URL)
        val (responseCode, responseText) = testRequest(url, this)

        Log.d("Response Code: ", "$responseCode")
        Log.d("Response Text: ", responseText)

        when (responseCode)
        {
            200  ->
            {
                val csrfToken = responseText.substringAfter("name=\"csrfmiddlewaretoken\" value=\"").substringBefore("\">")
                val editor = sharedPreferences.edit()
                editor.putString("CSRF_TOKEN", csrfToken)
                editor.apply()
            }
        }
    }

    private fun testRequest(url: URL, context: Context): Pair<Int, String>
    {
        val sharedPreferences = getSharedPreferences("user_session", MODE_PRIVATE)
        val policy = StrictMode.ThreadPolicy.Builder().permitAll().build()
        StrictMode.setThreadPolicy(policy)

        if (!isInternetAvailable(context))
        {
            return Pair(503, "Service Unavailable: Unable to connect to the server. Please check your internet connection.")
        }

        return try
        {
            // Use the main request to check for connectivity and server availability
            with(url.openConnection() as HttpURLConnection) {
                requestMethod = "GET"

                // Retrieve cookies from the response header
                val cookieHeader = getHeaderField("Set-Cookie") ?: throw Exception("Failed to retrieve cookies")

                val editor = sharedPreferences.edit()
                editor.putString("SAVED_COOKIE", cookieHeader)
                editor.apply()

                val responseText = buildString {
                    append("Successful Connection\n")
                    inputStream.bufferedReader().forEachLine { append(it).append("\n") }
                }

                // If the request is successful, return the server's response
                Pair(responseCode, responseText)
            }
        }
        catch (e: java.net.UnknownHostException)
        {
            // This exception indicates that the URL could not be resolved, likely due to no internet
            Pair(404, "Unknown Host: Unable to resolve the server's hostname. Please check your internet connection.")
        }
        catch (e: java.net.SocketTimeoutException)
        {
            // This exception indicates that the server did not respond within the expected time frame
            Pair(408, "Request Timeout: The server took too long to respond. Please try again later.")
        }
        catch (e: java.net.ConnectException)
        {
            // This exception indicates that there was a problem connecting to the server, possibly due to no internet
            Pair(503, "Service Unavailable: Unable to connect to the server. Please check your internet connection.")
        }
        catch (e: Exception)
        {
            // Handle any other exceptions that might occur
            e.printStackTrace()
            Pair(500, "Connection Failed: Please try again later.")
        }
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
