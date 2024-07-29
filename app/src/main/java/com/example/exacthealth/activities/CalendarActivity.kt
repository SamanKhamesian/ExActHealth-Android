package com.example.exacthealth.activities

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import android.widget.CalendarView
import android.widget.ImageView
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.exacthealth.R
import com.example.exacthealth.classes.FoodDetails
import com.example.exacthealth.classes.FoodListAdapter
import com.example.exacthealth.classes.FoodSharedPreferencesManager
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class CalendarActivity : AppCompatActivity()
{
    private lateinit var foodSharedPreferencesManager: FoodSharedPreferencesManager
    private lateinit var foodListView: ListView
    private val PERMISSION_CODE = 1001
    var foodList: MutableList<FoodDetails> = ArrayList()

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_calendar)

        // Initialize FoodSharedPreferencesManager
        foodSharedPreferencesManager = FoodSharedPreferencesManager(this)
        foodListView = findViewById<ListView>(R.id.calendar_food_list)

        val calendarView = findViewById<CalendarView>(R.id.calendar_widget)
        val addFoodDetailsButton = findViewById<ImageView>(R.id.add_food_details_icon)

        // Set initial date to current date
        val currentDate = Calendar.getInstance()
        calendarView.date = currentDate.timeInMillis

        val currentYear = currentDate.get(Calendar.YEAR)
        val currentMonth = currentDate.get(Calendar.MONTH)
        val currentDayOfMonth = currentDate.get(Calendar.DAY_OF_MONTH)

        var selectedDateFormat = formatDate(currentYear, currentMonth, currentDayOfMonth)
        foodList = foodSharedPreferencesManager.loadFoodList(selectedDateFormat)

        // Check if permission is granted before loading images
        if (checkPermission())
        {
            // Permission is already granted, load the images
            updateListView(foodList)
        }
        else
        {
            // Request permission from the user
            requestPermission()
        }

        // Listen for date change events
        calendarView.setOnDateChangeListener { view, year, month, dayOfMonth ->
            // Handle date change
            selectedDateFormat = formatDate(year, month, dayOfMonth)
            foodList = foodSharedPreferencesManager.loadFoodList(selectedDateFormat)
            // Check if permission is granted before loading images
            if (checkPermission())
            {
                // Permission is already granted, load the images
                updateListView(foodList)
            }
            else
            {
                // Request permission from the user
                requestPermission()
            }
            showFoodListToast(selectedDateFormat, foodList)
        }

        addFoodDetailsButton.setOnClickListener {
            val intent = Intent(this, AddFoodActivity::class.java)
            intent.putExtra("selectedDate", selectedDateFormat)
            startActivity(intent)
        }
    }

    private fun checkPermission(): Boolean
    {
        // Check if permission is granted
        return ContextCompat.checkSelfPermission(this,
                                                 Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestPermission()
    {
        // Request permission from the user
        ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), PERMISSION_CODE)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray)
    {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_CODE)
        {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)
            {
                // Permission granted, load the images
                updateListView(foodList)
            }
            else
            {
                // Permission denied, handle accordingly
                Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun formatDate(year: Int, month: Int, dayOfMonth: Int): String
    {
        val calendar = Calendar.getInstance()
        calendar.set(year, month, dayOfMonth)
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        return dateFormat.format(calendar.time)
    }

    private fun updateListView(foodList: MutableList<FoodDetails>)
    {
        if (foodList.isNotEmpty())
        {
            foodListView.visibility = View.VISIBLE

            val adapter = FoodListAdapter(this, foodList)
            foodListView.adapter = adapter
        }
        else
        {
            foodListView.visibility = View.INVISIBLE
        }
    }

    private fun showFoodListToast(date: String, foodList: MutableList<FoodDetails>?)
    {
        if (foodList.isNullOrEmpty())
        {
            val message = "No food entries for $date"
            Toast.makeText(this, message, Toast.LENGTH_LONG).show()
        }
    }
}