package com.example.exacthealth

import android.content.Intent
import android.os.Bundle
import android.widget.CalendarView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class CalendarActivity : AppCompatActivity()
{
    private lateinit var foodSharedPreferencesManager: FoodSharedPreferencesManager

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_calendar)

        // Initialize FoodSharedPreferencesManager
        foodSharedPreferencesManager = FoodSharedPreferencesManager(this)

        val calendarView = findViewById<CalendarView>(R.id.calendar_widget)
        val addFoodDetailsButton = findViewById<TextView>(R.id.add_food_details_icon)

        // Set initial date to current date
        val currentDate = Calendar.getInstance()
        val currentYear = currentDate.get(Calendar.YEAR)
        val currentMonth = currentDate.get(Calendar.MONTH)
        val currentDayOfMonth = currentDate.get(Calendar.DAY_OF_MONTH)
        calendarView.date = currentDate.timeInMillis

        // Listen for date change events
        calendarView.setOnDateChangeListener { view, year, month, dayOfMonth ->
            // Handle date change
            val selectedDate = Calendar.getInstance()
            selectedDate.set(year, month, dayOfMonth)

            val selectedDateFormat = formatDate(year, month, dayOfMonth)
            val foodList = foodSharedPreferencesManager.loadFoodList(selectedDateFormat)
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

    private fun showFoodListToast(date: String, foodList: MutableList<Food>?)
    {
        val message = if (!foodList.isNullOrEmpty())
        {
            val foodIntakeInfo = StringBuilder()

            // Extract food intake information
            for ((index, food) in foodList.withIndex())
            {
                foodIntakeInfo.append("Food ${index + 1}:\n")
                foodIntakeInfo.append("Name: ${food.name}\n")
                foodIntakeInfo.append("Protein: ${food.protein}\n")
                foodIntakeInfo.append("Carbs: ${food.carb}\n")
                foodIntakeInfo.append("Fat: ${food.fat}\n")
                foodIntakeInfo.append("\n")
            }

            // Create the message
            val messageHeader = "Food list for $date:\n"
            val fullMessage = messageHeader + foodIntakeInfo.toString()
            fullMessage
        }
        else
        {
            "No food entries for $date"
        }

        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }
}