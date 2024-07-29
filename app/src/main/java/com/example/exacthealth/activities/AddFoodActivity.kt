package com.example.exacthealth.activities

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Button
import android.widget.EditText
import android.widget.RelativeLayout
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import com.example.exacthealth.R
import com.example.exacthealth.classes.FoodDetails
import com.example.exacthealth.classes.FoodSharedPreferencesManager
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class AddFoodActivity : AppCompatActivity()
{
    private lateinit var foodSharedPreferencesManager: FoodSharedPreferencesManager
    private lateinit var pickImagesLauncher: ActivityResultLauncher<Intent>
    private var selectedImagesPathList: ArrayList<String> = ArrayList()

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_food)

        foodSharedPreferencesManager = FoodSharedPreferencesManager(this)

        val foodName = findViewById<EditText>(R.id.food_name_text_edit)

        val foodDateLayout = findViewById<TextInputLayout>(R.id.food_details_date_layout)
        val foodDateInput = findViewById<TextInputEditText>(R.id.food_details_date_input)

        val foodTimeLayout = findViewById<TextInputLayout>(R.id.food_details_time_layout)
        val foodTimeInput = findViewById<TextInputEditText>(R.id.food_details_time_input)

        val pickImagesButton = findViewById<Button>(R.id.food_details_pick_images_button)
        val selectedImagesButton = findViewById<Button>(R.id.food_details_selected_images_button)

        val foodProtein = findViewById<EditText>(R.id.protein_text_edit)
        val foodCarbs = findViewById<EditText>(R.id.carbs_text_edit)
        val foodFats = findViewById<EditText>(R.id.fats_text_edit)

        val backToCalendarButton = findViewById<RelativeLayout>(R.id.back_to_calendar_layout)
        val favoriteFoodButton = findViewById<ConstraintLayout>(R.id.add_from_favorites_layout)

        val saveEntryButton = findViewById<Button>(R.id.food_details_save_entry_button)

        setDefaultDateTime(foodDateInput, foodTimeInput)

        foodDateInput.setOnClickListener {
            setDate(foodDateInput)
        }

        foodTimeInput.setOnClickListener {
            setTime(foodTimeInput)
        }

        pickImagesButton.setOnClickListener {
            openGalleryForMultipleImages()
        }

        // Set up the ActivityResultLauncher
        pickImagesLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK)
            {
                selectedImagesPathList = ArrayList()

                val intent = result.data
                intent?.clipData?.let { clipData ->
                    for (i in 0 until clipData.itemCount)
                    {
                        val uri = clipData.getItemAt(i).uri
                        val path = getPathFromUri(uri)
                        path?.let { selectedImagesPathList.add(it) }
                    }
                } ?: run {
                    val uri = intent?.data
                    uri?.let {
                        val path = getPathFromUri(uri)
                        path?.let { selectedImagesPathList.add(it) }
                    }
                }

                // Do something with the list of selected image URIs
                println("Selected : ${selectedImagesPathList}")
                // Enable the button
                selectedImagesButton.isEnabled = true
                selectedImagesButton.setTextColor(ContextCompat.getColor(this, R.color.red))
            }
        }

        selectedImagesButton.setOnClickListener {
            val intent = Intent(this, SelectedImagesActivity::class.java)
            intent.putExtra("imagesPathList", selectedImagesPathList)
            startActivity(intent)
        }

        backToCalendarButton.setOnClickListener {
            val intent = Intent(this, CalendarActivity::class.java)
            startActivity(intent)
        }

        favoriteFoodButton.setOnClickListener {
            val intent = Intent(this, FavoriteFoodActivity::class.java)
            startActivity(intent)
        }

        saveEntryButton.setOnClickListener {

            if (foodName.text.isNullOrEmpty())
            {
                Toast.makeText(this, "Food name cannot be empty!", Toast.LENGTH_LONG).show()
            }
            else
            {
                val foodDetails = FoodDetails(foodName.text.toString(),
                                              convertDateFormat(foodDateInput.text.toString()),
                                              foodTimeInput.text.toString(),
                                              selectedImagesPathList,
                                              foodProtein.text.toString().toIntOrNull(),
                                              foodCarbs.text.toString().toIntOrNull(),
                                              foodFats.text.toString().toIntOrNull())

                foodSharedPreferencesManager.addFoodItem(foodDetails.date, foodDetails)

                showDeletedFoodToast(this)

                foodName.text.clear()
                foodProtein.text.clear()
                foodCarbs.text.clear()
                foodFats.text.clear()
                setDefaultDateTime(foodDateInput, foodTimeInput)
                selectedImagesButton.isEnabled = false
                selectedImagesButton.setTextColor(ContextCompat.getColor(this, R.color.light_red))
            }
        }
    }

    private fun openGalleryForMultipleImages()
    {
        val galleryIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        galleryIntent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
        pickImagesLauncher.launch(galleryIntent)
    }

    private fun setDefaultDateTime(foodDateInput: TextInputEditText, foodTimeInput: TextInputEditText)
    {
        val currentTime = Calendar.getInstance()
        val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
        val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())

        foodTimeInput.setText(timeFormat.format(currentTime.time))
        foodDateInput.setText(dateFormat.format(currentTime.time))
    }

    private fun convertDateFormat(inputDate: String): String
    {
        val inputFormat = SimpleDateFormat("MMM dd, yyyy", Locale.US)
        val outputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        val newDate = inputFormat.parse(inputDate)
        return outputFormat.format(newDate!!)
    }

    // Function to get the path from URI
    private fun getPathFromUri(uri: Uri): String?
    {
        val projection = arrayOf(MediaStore.Images.Media.DATA)
        val cursor = contentResolver.query(uri, projection, null, null, null)
        cursor?.use {
            val columnIndex = it.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
            it.moveToFirst()
            return it.getString(columnIndex)
        }
        return null
    }

    private fun setDate(input: TextInputEditText)
    {
        val calendar = Calendar.getInstance()
        val y = calendar.get(Calendar.YEAR)
        val m = calendar.get(Calendar.MONTH)
        val d = calendar.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(this, R.style.DialogTheme, { _, year, monthOfYear, dayOfMonth ->
            calendar.set(year, monthOfYear, dayOfMonth)
            val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
            val selectedDate = dateFormat.format(calendar.time)
            input.setText(selectedDate)
        }, y, m, d)
        datePickerDialog.show()
    }

    private fun setTime(input: TextInputEditText)
    {
        val calendar = Calendar.getInstance()
        val hourOfDay = calendar.get(Calendar.HOUR_OF_DAY)
        val minute = calendar.get(Calendar.MINUTE)

        val timePickerDialog = TimePickerDialog(this, R.style.DialogTheme, { _, selectedHourOfDay, selectedMinute ->
            val timeFormat = String.format("%02d:%02d", selectedHourOfDay, selectedMinute)
            input.setText(timeFormat)
        }, hourOfDay, minute, true)
        timePickerDialog.show()
    }

    private fun showDeletedFoodToast(context: Context)
    {
        val message = "Item is added successfully"
        Toast.makeText(context, message, Toast.LENGTH_LONG).show()
    }
}