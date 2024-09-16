package com.example.exacthealth.activities

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.widget.CalendarView
import android.widget.ImageView
import android.widget.ListView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.exacthealth.R
import com.example.exacthealth.classes.FoodDetails
import com.example.exacthealth.classes.FoodListAdapter
import com.example.exacthealth.classes.FoodSharedPreferencesManager
import com.example.exacthealth.classes.SelectedDatePreferencesManager
import com.example.exacthealth.classes.createDashFormatDate
import com.example.exacthealth.classes.showEmptyFoodListToast
import com.example.exacthealth.classes.showPermissionDeniedToast
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class CalendarActivity : AppCompatActivity()
{
    private lateinit var foodSharedPreferencesManager: FoodSharedPreferencesManager
    private lateinit var selectedDatePreferencesManager: SelectedDatePreferencesManager
    private lateinit var foodListView: ListView
    private lateinit var foodList: MutableList<FoodDetails>

    private val PERMISSION_CODE = 1001

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_calendar)

        foodSharedPreferencesManager = FoodSharedPreferencesManager(this)
        selectedDatePreferencesManager = SelectedDatePreferencesManager(this)
        foodListView = findViewById(R.id.calendar_food_list)
        foodList = ArrayList()

        val calendarView = findViewById<CalendarView>(R.id.calendar_widget)
        val addNewFood = findViewById<ImageView>(R.id.add_food_details_icon)

        val currentDate = Calendar.getInstance()

        val currentYear = currentDate.get(Calendar.YEAR)
        val currentMonth = currentDate.get(Calendar.MONTH)
        val currentDayOfMonth = currentDate.get(Calendar.DAY_OF_MONTH)

        var selectedDateFormat = createDashFormatDate(currentYear, currentMonth, currentDayOfMonth)
        selectedDatePreferencesManager.setSelectedDate(selectedDateFormat)

        calendarView.date = currentDate.timeInMillis
        foodList = foodSharedPreferencesManager.loadFoodList(selectedDateFormat)

        // Android 12 (API level 31) or lower
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU)
        {
            if (checkPermissions()) updateListView(foodList)
            else requestPermissions()
        }
        // Android 13+ (API level 33 or higher)
        else
        {
            if (checkPermissionForCamera()) updateListView(foodList)
            else requestPermissionForCamera()
        }

        calendarView.setOnDateChangeListener { _, year, month, dayOfMonth ->
            selectedDateFormat = createDashFormatDate(year, month, dayOfMonth)
            selectedDatePreferencesManager.setSelectedDate(selectedDateFormat)
            foodList = foodSharedPreferencesManager.loadFoodList(selectedDateFormat)

            updateListView(foodList)

            if (foodList.isEmpty()) showEmptyFoodListToast(this, selectedDateFormat)
        }

        addNewFood.setOnClickListener {
            val intent = Intent(this, AddFoodActivity::class.java)
            intent.putExtra("from", "calendar")
            startActivity(intent)
        }
    }

    private fun checkPermissions(): Boolean
    {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
        {
            // For Android 13+ (API level 33+), check READ_MEDIA_IMAGES permission
            val mediaPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES) == PackageManager.PERMISSION_GRANTED
            val cameraPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED

            mediaPermission && cameraPermission

        }
        else
        {
            // For Android 10 to 12 (API levels 29 to 32), check READ_EXTERNAL_STORAGE permission
            val storagePermission = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
            val cameraPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED

            storagePermission && cameraPermission
        }
    }


    private fun checkPermissionForCamera(): Boolean
    {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestPermissions()
    {
        val permissionsNeeded = mutableListOf<String>()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
        {
            // Android 13+ (API level 33): Request READ_MEDIA_IMAGES
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES) != PackageManager.PERMISSION_GRANTED)
            {
                permissionsNeeded.add(Manifest.permission.READ_MEDIA_IMAGES)
            }

        }
        else
        {
            // Android 10 to 12 (API level 29 to 32): Request READ_EXTERNAL_STORAGE
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)
            {
                permissionsNeeded.add(Manifest.permission.READ_EXTERNAL_STORAGE)
            }
        }

        // Camera permission (same for all versions)
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED)
        {
            permissionsNeeded.add(Manifest.permission.CAMERA)
        }

        if (permissionsNeeded.isNotEmpty())
        {
            ActivityCompat.requestPermissions(this, permissionsNeeded.toTypedArray(), PERMISSION_CODE)
        }
    }

    private fun requestPermissionForCamera()
    {
        ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), PERMISSION_CODE)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray)
    {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == PERMISSION_CODE)
        {
            if (grantResults.isNotEmpty() && grantResults.all { it == PackageManager.PERMISSION_GRANTED })
            {
                updateListView(foodList)
            }
            else
            {
                // Handle permission denial
                val deniedPermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
                {
                    Manifest.permission.READ_MEDIA_IMAGES
                }
                else
                {
                    Manifest.permission.READ_EXTERNAL_STORAGE
                }

                if (!ActivityCompat.shouldShowRequestPermissionRationale(this, deniedPermission))
                {
                    showPermissionDeniedDialog()
                }
                else
                {
                    showPermissionDeniedToast(this)
                }
            }
        }
    }

    private fun updateListView(foodList: MutableList<FoodDetails>)
    {
        if (foodList.isNotEmpty())
        {
            val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())

            foodList.sortBy { food ->
                timeFormat.parse(food.time)
            }

            foodListView.visibility = View.VISIBLE

            val adapter = FoodListAdapter(this, foodList)
            foodListView.adapter = adapter
        }
        else
        {
            foodListView.visibility = View.INVISIBLE
        }
    }

    private fun showPermissionDeniedDialog()
    {
        AlertDialog.Builder(this).setTitle("Permission Required")
            .setMessage("Permission to read external storage is required for this app. Please enable it in the app settings.")
            .setPositiveButton("Go to Settings") { _, _ ->
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                    data = Uri.fromParts("package", packageName, null)
                }
                startActivity(intent)
            }.setNegativeButton("Cancel", null).show()
    }
}