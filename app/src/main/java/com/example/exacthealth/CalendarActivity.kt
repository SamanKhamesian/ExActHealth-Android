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
import android.widget.CalendarView
import android.widget.ImageView
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
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
        val addFoodDetailsButton = findViewById<TextView>(R.id.add_food_details_icon)

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
        val imagesLayout: RecyclerView = itemView.findViewById(R.id.food_images_list_view)
        imagesLayout.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        imagesLayout.visibility = View.INVISIBLE

        if (!food.paths.isNullOrEmpty())
        {
            // Sample list of image URLs
            val imageUrls = food.paths
            val adapter = imageUrls?.let { FoodImageAdapter(it) }
            imagesLayout.adapter = adapter
            imagesLayout.visibility = View.VISIBLE
        }
        else
        {
            imagesLayout.visibility = View.INVISIBLE
        }

        return itemView
    }
}

class FoodImageAdapter(private val images: ArrayList<String>) : RecyclerView.Adapter<FoodImageAdapter.ImageViewHolder>()
{

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageViewHolder
    {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.list_saved_food_images, parent, false)
        return ImageViewHolder(view)
    }

    override fun onBindViewHolder(holder: ImageViewHolder, position: Int)
    {
        val imagePath = images[position]
        val fileUri = Uri.parse("file://$imagePath")
        holder.imageView.setImageURI(fileUri)
    }

    override fun getItemCount(): Int
    {
        return images.size
    }

    class ImageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)
    {
        val imageView: ImageView = itemView.findViewById(R.id.saved_food_image_view)
    }
}
