package com.example.exacthealth.activities

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.widget.Button
import android.widget.EditText
import android.widget.RelativeLayout
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.example.exacthealth.R
import com.example.exacthealth.classes.FoodDetails
import com.example.exacthealth.classes.FoodSharedPreferencesManager
import com.example.exacthealth.classes.SelectedDatePreferencesManager
import com.google.android.material.textfield.TextInputEditText
import java.io.File
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class AddFoodActivity : AppCompatActivity()
{
    private lateinit var foodSharedPreferencesManager: FoodSharedPreferencesManager
    private lateinit var selectedDatePreferencesManager: SelectedDatePreferencesManager
    private lateinit var pickImagesLauncher: ActivityResultLauncher<Intent>
    private lateinit var cameraLauncher: ActivityResultLauncher<Intent>
    private lateinit var selectedImagesActivityLauncher: ActivityResultLauncher<Intent>

    private var selectedImagesPathList: ArrayList<String> = ArrayList()
    private var currentPhotoPath: String? = null

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_food)

        foodSharedPreferencesManager = FoodSharedPreferencesManager(this)
        selectedDatePreferencesManager = SelectedDatePreferencesManager(this)

        val from = intent.getStringExtra("from").toString()

        val foodName = findViewById<EditText>(R.id.food_name_text_edit)

        val foodDateInput = findViewById<TextInputEditText>(R.id.food_details_date_input)
        val foodTimeInput = findViewById<TextInputEditText>(R.id.food_details_time_input)

        val pickImagesButton = findViewById<Button>(R.id.food_details_pick_images_button)
        val selectedImagesButton = findViewById<Button>(R.id.food_details_selected_images_button)

        val foodProtein = findViewById<EditText>(R.id.protein_text_edit)
        val foodCarbs = findViewById<EditText>(R.id.carbs_text_edit)
        val foodFats = findViewById<EditText>(R.id.fats_text_edit)

        val backToCalendarButton = findViewById<RelativeLayout>(R.id.back_to_calendar_layout)
        val favoriteFoodButton = findViewById<ConstraintLayout>(R.id.add_from_favorites_layout)

        val saveEntryButton = findViewById<Button>(R.id.food_details_save_entry_button)

        setDefaultInformation(from, foodName, foodProtein, foodCarbs, foodFats)
        setDefaultDateTime(from, foodDateInput, foodTimeInput)

        if (from == "edit")
        {
            favoriteFoodButton.visibility = ConstraintLayout.INVISIBLE
        }

        if (selectedImagesPathList.isNotEmpty())
        {
            selectedImagesButton.isEnabled = true
            selectedImagesButton.setTextColor(ContextCompat.getColor(this, R.color.red))
        }

        // Set up the ActivityResultLauncher
        selectedImagesActivityLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK)
            {
                val intent = result.data
                intent?.let {
                    val updatedSelectedImagesList = it.getStringArrayListExtra("imagesPathList")
                    updatedSelectedImagesList?.let { array ->
                        selectedImagesPathList = array
                    }
                }

                if (selectedImagesPathList.isNotEmpty())
                {
                    selectedImagesButton.isEnabled = true
                    selectedImagesButton.setTextColor(ContextCompat.getColor(this, R.color.red))
                }

                else
                {
                    selectedImagesButton.isEnabled = false
                    selectedImagesButton.setTextColor(ContextCompat.getColor(this, R.color.light_red))
                }
            }
        }

        pickImagesLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK)
            {
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

                if (selectedImagesPathList.isNotEmpty())
                {
                    selectedImagesButton.isEnabled = true
                    selectedImagesButton.setTextColor(ContextCompat.getColor(this, R.color.red))
                }

                else
                {
                    selectedImagesButton.isEnabled = false
                    selectedImagesButton.setTextColor(ContextCompat.getColor(this, R.color.light_red))
                }
            }
        }

        cameraLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK)
            {
                currentPhotoPath?.let {
                    selectedImagesPathList.add(it)
                    selectedImagesButton.isEnabled = true
                    selectedImagesButton.setTextColor(ContextCompat.getColor(this, R.color.red))
                }
            }
        }

        foodDateInput.setOnClickListener {
            setDate(foodDateInput)
        }

        foodTimeInput.setOnClickListener {
            setTime(foodTimeInput)
        }

        pickImagesButton.setOnClickListener {
            showImageSourceOptions()
        }

        selectedImagesButton.setOnClickListener {
            val intent = Intent(this, SelectedImagesActivity::class.java)
            intent.putExtra("imagesPathList", selectedImagesPathList)
            selectedImagesActivityLauncher.launch(intent)
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
                showFoodNameErrorToast()
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

                if (from == "edit")
                {
                    val oldFoodDate = intent.getStringExtra("foodDate")
                    val oldFoodList = foodSharedPreferencesManager.loadFoodList(oldFoodDate!!)
                    val oldFoodPosition = intent.getIntExtra("foodPosition", 0)

                    oldFoodList.removeAt(oldFoodPosition)
                    foodSharedPreferencesManager.saveFoodList(oldFoodDate, oldFoodList)

                    val newFoodDate = foodDetails.date
                    foodSharedPreferencesManager.addFoodItem(newFoodDate, foodDetails)

                    showEditFoodToast()

                    val intent = Intent(this, CalendarActivity::class.java)
                    startActivity(intent)
                }
                else
                {
                    foodSharedPreferencesManager.addFoodItem(foodDetails.date, foodDetails)

                    showAddFoodToast()

                    foodName.text.clear()
                    foodProtein.text.clear()
                    foodCarbs.text.clear()
                    foodFats.text.clear()
                    setDefaultDateTime(from, foodDateInput, foodTimeInput)
                    selectedImagesButton.isEnabled = false
                    selectedImagesButton.setTextColor(ContextCompat.getColor(this, R.color.light_red))
                }
            }
        }
    }

    private fun openGalleryForMultipleImages()
    {
        val galleryIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        galleryIntent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
        pickImagesLauncher.launch(galleryIntent)
    }

    private fun showImageSourceOptions()
    {
        val options = arrayOf("Select from Gallery", "Take a Picture")
        AlertDialog.Builder(this).setTitle("Choose an Option").setItems(options) { dialog, which ->
            when (which)
            {
                0 -> openGalleryForMultipleImages()
                1 -> openCamera()
            }
        }.show()
    }

    private fun openCamera()
    {
        val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        val photoFile: File = createImageFile()
        val photoURI: Uri = FileProvider.getUriForFile(this, "com.example.exacthealth.activities.provider", photoFile)
        currentPhotoPath = photoFile.absolutePath
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
        cameraLauncher.launch(cameraIntent)
    }

    private fun createImageFile(): File
    {
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val storageDir: File? = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile("JPEG_${timeStamp}_", ".jpg", storageDir).apply {
            currentPhotoPath = absolutePath
        }
    }

    private fun setDefaultDateTime(from: String?, foodDateInput: TextInputEditText, foodTimeInput: TextInputEditText)
    {
        val selectedDate = selectedDatePreferencesManager.getSelectedDate()
        val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
        val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
        val inputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        val currentTime = Calendar.getInstance()

        if (from == "edit")
        {
            val foodTimeString = intent.getStringExtra("foodTime")
            val foodDateString = intent.getStringExtra("foodDate")

            val foodTime = timeFormat.parse(foodTimeString!!)
            val foodDate = inputFormat.parse(foodDateString!!)

            foodTimeInput.setText(timeFormat.format(foodTime!!))
            foodDateInput.setText(dateFormat.format(foodDate!!))
        }
        else
        {
            foodTimeInput.setText(timeFormat.format(currentTime.time))

            val dateToDisplay = selectedDate?.let {
                inputFormat.parse(it)
            } ?: currentTime.time

            foodDateInput.setText(dateFormat.format(dateToDisplay))
        }
    }

    private fun setDefaultInformation(from: String?,
                                      foodName: EditText,
                                      foodProtein: EditText,
                                      foodCarbs: EditText,
                                      foodFats: EditText)
    {
        if (from == "favorite_food" || from == "edit")
        {
            foodName.setText(intent.getStringExtra("foodName"))
            setDefaultValue(foodProtein, intent.getIntExtra("foodProtein", -1))
            setDefaultValue(foodCarbs, intent.getIntExtra("foodCarbs", -1))
            setDefaultValue(foodFats, intent.getIntExtra("foodFats", -1))
            selectedImagesPathList = intent.getStringArrayListExtra("foodImagesPathList")!!
        }
    }

    private fun setDefaultValue(item: EditText, value: Int)
    {
        if (value != -1) item.setText(value.toString())
        else item.text = null
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
        val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
        val calendar = Calendar.getInstance()

        val inputText = input.text.toString()

        val date = dateFormat.parse(inputText)
        date?.let { calendar.time = it }

        val y = calendar.get(Calendar.YEAR)
        val m = calendar.get(Calendar.MONTH)
        val d = calendar.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(this, R.style.DialogTheme, { _, year, monthOfYear, dayOfMonth ->
            calendar.set(year, monthOfYear, dayOfMonth)
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

    private fun showAddFoodToast()
    {
        val message = "Item is added successfully"
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }

    private fun showFoodNameErrorToast()
    {
        val message = "Food name cannot be empty!"
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }

    private fun showEditFoodToast()
    {
        val message = "Item is edited successfully"
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }
}