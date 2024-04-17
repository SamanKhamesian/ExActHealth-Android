package com.example.exacthealth

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.BaseAdapter
import android.widget.CalendarView
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
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

class FoodListAdapter(context: Context, private val foodList: MutableList<FoodDetails>) : ArrayAdapter<FoodDetails>(
    context,
    0,
    foodList)
{
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View
    {
        val itemView = convertView ?: LayoutInflater.from(context).inflate(R.layout.list_saved_food, parent, false)

        val food = foodList[position]

        val nameTextView: TextView = itemView.findViewById(R.id.saved_food_name_text_view)
        val proteinTextView: TextView = itemView.findViewById(R.id.saved_food_protein_text_view)
        val carbsTextView: TextView = itemView.findViewById(R.id.saved_food_carbs_text_view)
        val fatTextView: TextView = itemView.findViewById(R.id.saved_fat_text_view)

        nameTextView.text = food.name
        proteinTextView.text = "Protein (g): ${food.protein ?: "N/A"}"
        carbsTextView.text = "Carbs (g): ${food.carbs ?: "N/A"}"
        fatTextView.text = "Fats (g): ${food.fats ?: "N/A"}"

        // Inside your activity or fragment
        val imagesLayout: LinearLayout = itemView.findViewById(R.id.food_images_layout)
        val listView: ListView = itemView.findViewById(R.id.food_images_list_view)
        imagesLayout.visibility = View.INVISIBLE

        if (!food.images.isNullOrEmpty())
        {
            val adapter = FoodImageAdapter(context, R.layout.list_saved_food_images, food.images!!)
            listView.adapter = adapter
            imagesLayout.visibility = View.VISIBLE
        }
        else
        {
            imagesLayout.visibility = View.INVISIBLE
        }

        return itemView
    }
}

class FoodImageAdapter(private val context: Context, private val resource: Int, private val images: ArrayList<Uri>) : ArrayAdapter<Uri>(
    context,
    resource,
    images)
{
    override fun getCount(): Int
    {
        return images.size
    }

    override fun getItem(position: Int): Uri
    {
        return images[position]
    }

    override fun getItemId(position: Int): Long
    {
        return position.toLong()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View
    {
        val view: View =
            convertView ?: LayoutInflater.from(context).inflate(resource, parent, false)
        val imageView: ImageView = view.findViewById(R.id.saved_food_image_view)
        val imageUri = images[position]
        imageView.setImageURI(imageUri)
        return view
    }
}