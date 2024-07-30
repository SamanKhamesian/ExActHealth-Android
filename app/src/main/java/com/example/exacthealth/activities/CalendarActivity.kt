package com.example.exacthealth.activities

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import android.widget.CalendarView
import android.widget.ImageView
import android.widget.ListView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.exacthealth.R
import com.example.exacthealth.classes.FoodDetails
import com.example.exacthealth.classes.FoodListAdapter
import com.example.exacthealth.classes.FoodSharedPreferencesManager
import com.example.exacthealth.classes.SelectedDatePreferencesManager
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

        var selectedDateFormat = formatDate(currentYear, currentMonth, currentDayOfMonth)
        selectedDatePreferencesManager.setSelectedDate(selectedDateFormat)

        calendarView.date = currentDate.timeInMillis
        foodList = foodSharedPreferencesManager.loadFoodList(selectedDateFormat)

        if (checkPermission()) updateListView(foodList)
        else requestPermission()

        calendarView.setOnDateChangeListener { _, year, month, dayOfMonth ->
            selectedDateFormat = formatDate(year, month, dayOfMonth)
            selectedDatePreferencesManager.setSelectedDate(selectedDateFormat)
            foodList = foodSharedPreferencesManager.loadFoodList(selectedDateFormat)

            if (checkPermission()) updateListView(foodList)
            else requestPermission()

            if (foodList.isEmpty()) showFoodListToast(selectedDateFormat)
        }

        addNewFood.setOnClickListener {
            val intent = Intent(this, AddFoodActivity::class.java)
            intent.putExtra("from", "calendar")
            startActivity(intent)
        }
    }

    private fun checkPermission(): Boolean
    {
        return ContextCompat.checkSelfPermission(this,
                                                 Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestPermission()
    {
        ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), PERMISSION_CODE)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray)
    {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_CODE)
        {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) updateListView(
                foodList)
            else showPermissionDeniedToast()
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

    private fun showFoodListToast(date: String)
    {
        val message = "No food entries for $date"
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    private fun showPermissionDeniedToast()
    {
        val message = "Permission denied"
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}