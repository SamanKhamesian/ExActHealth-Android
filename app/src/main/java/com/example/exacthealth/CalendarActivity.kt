package com.example.exacthealth

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.CalendarView
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class CalendarActivity : AppCompatActivity()
{
    private lateinit var foodSharedPreferencesManager: FoodSharedPreferencesManager
    private lateinit var foodListView: ListView

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_calendar)

        // Initialize FoodSharedPreferencesManager
        foodSharedPreferencesManager = FoodSharedPreferencesManager(this)
        foodListView = findViewById<ListView>(R.id.calendar_food_list)

        val calendarView = findViewById<CalendarView>(R.id.calendar_widget)
        val addFoodDetailsButton = findViewById<TextView>(R.id.add_food_details_icon)

        // Set initial date to current date
        val currentDate = Calendar.getInstance()
        calendarView.date = currentDate.timeInMillis

        val currentYear = currentDate.get(Calendar.YEAR)
        val currentMonth = currentDate.get(Calendar.MONTH)
        val currentDayOfMonth = currentDate.get(Calendar.DAY_OF_MONTH)

        val selectedDateFormat = formatDate(currentYear, currentMonth, currentDayOfMonth)
        val foodList = foodSharedPreferencesManager.loadFoodList(selectedDateFormat)
        updateListView(foodList)

        // Listen for date change events
        calendarView.setOnDateChangeListener { view, year, month, dayOfMonth ->
            // Handle date change
            val selectedDateFormat = formatDate(year, month, dayOfMonth)
            val foodList = foodSharedPreferencesManager.loadFoodList(selectedDateFormat)
            updateListView(foodList)
            showFoodListToast(selectedDateFormat, foodList)
        }

        addFoodDetailsButton.setOnClickListener {
            val intent = Intent(this, AddFoodActivity::class.java)
            startActivity(intent)
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