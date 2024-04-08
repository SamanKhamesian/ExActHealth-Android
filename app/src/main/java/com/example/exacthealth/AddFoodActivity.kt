package com.example.exacthealth

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Button
import android.widget.RelativeLayout
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class AddFoodActivity : AppCompatActivity()
{
    private lateinit var pickImagesLauncher: ActivityResultLauncher<Intent>
    private lateinit var selectedImagesUriList: ArrayList<Uri>

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_food)

        val foodDateLayout = findViewById<TextInputLayout>(R.id.food_date_layout)
        val foodDateInput = findViewById<TextInputEditText>(R.id.food_date_input)

        val foodTimeLayout = findViewById<TextInputLayout>(R.id.food_time_layout)
        val foodTimeInput = findViewById<TextInputEditText>(R.id.food_time_input)

        val pickImagesButton = findViewById<Button>(R.id.pick_images_button)
        val selectedImagesButton = findViewById<Button>(R.id.selected_images_button)

        val calendarButton = findViewById<RelativeLayout>(R.id.back_to_calendar_layout)
        val favoriteFoodButton = findViewById<ConstraintLayout>(R.id.add_from_favorites_layout)

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
                selectedImagesUriList = ArrayList()

                val intent = result.data
                intent?.clipData?.let { clipData ->
                    for (i in 0 until clipData.itemCount)
                    {
                        val uri = clipData.getItemAt(i).uri
                        selectedImagesUriList.add(uri)
                    }
                } ?: run {
                    val uri = intent?.data
                    uri?.let {
                        selectedImagesUriList.add(uri)
                    }
                }

                // Do something with the list of selected image URIs
                println("Selected ${selectedImagesUriList.size} images")
                // Enable the button
                selectedImagesButton.isEnabled = true
                selectedImagesButton.setTextColor(ContextCompat.getColor(this, R.color.red))
            }
        }

        selectedImagesButton.setOnClickListener {
            val intent = Intent(this, SelectedImagesActivity::class.java)
            intent.putParcelableArrayListExtra("ImagesList", selectedImagesUriList)
            startActivity(intent)
        }

        calendarButton.setOnClickListener {
            val intent = Intent(this, CalendarActivity::class.java)
            startActivity(intent)
        }

        favoriteFoodButton.setOnClickListener {
            val intent = Intent(this, FavoriteFoodActivity::class.java)
            startActivity(intent)
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
}