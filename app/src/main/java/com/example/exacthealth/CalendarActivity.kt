package com.example.exacthealth

import android.content.Intent
import android.os.Bundle
import android.widget.CalendarView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import java.util.Calendar

class CalendarActivity : AppCompatActivity()
{
    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_calendar)

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
        }

        addFoodDetailsButton.setOnClickListener {
            val intent = Intent(this, AddFoodActivity::class.java)
            startActivity(intent)
        }
    }
}