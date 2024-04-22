package com.example.exacthealth.activities

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
import androidx.core.content.ContextCompat
import com.example.exacthealth.R
import com.example.exacthealth.classes.FoodDetails
import com.example.exacthealth.classes.FoodSharedPreferencesManager
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class AddFavoriteFoodActivity : AppCompatActivity()
{
    private lateinit var foodSharedPreferencesManager: FoodSharedPreferencesManager
    private lateinit var pickImagesLauncher: ActivityResultLauncher<Intent>
    private var selectedImagesUriList: ArrayList<Uri> = ArrayList()
    private var selectedImagesPathList: ArrayList<String> = ArrayList()

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_favorite_food)

        foodSharedPreferencesManager = FoodSharedPreferencesManager(this)

        val foodName = findViewById<EditText>(R.id.favorite_food_name_text_edit)

        val pickImagesButton = findViewById<Button>(R.id.favorite_food_pick_images_button)
        val selectedImagesButton = findViewById<Button>(R.id.favorite_food_selected_images_button)

        val foodProtein = findViewById<EditText>(R.id.favorite_food_protein_text_edit)
        val foodCarbs = findViewById<EditText>(R.id.favorite_food_carbs_text_edit)
        val foodFats = findViewById<EditText>(R.id.favorite_food_fats_text_edit)

        val backToFavoriteFoodsButton = findViewById<RelativeLayout>(R.id.back_to_favorite_food_layout)
        val saveEntryButton = findViewById<Button>(R.id.favorite_food_save_entry_button)

        pickImagesButton.setOnClickListener {
            openGalleryForMultipleImages()
        }

        backToFavoriteFoodsButton.setOnClickListener {
            val intent = Intent(this, FavoriteFoodActivity::class.java)
            startActivity(intent)
        }

        selectedImagesButton.setOnClickListener {
            val intent = Intent(this, SelectedImagesActivity::class.java)
            intent.putParcelableArrayListExtra("ImagesList", selectedImagesUriList)
            startActivity(intent)
        }

        saveEntryButton.setOnClickListener {

            if (foodName.text.isNullOrEmpty())
            {
                Toast.makeText(this, "Food name cannot be empty!", Toast.LENGTH_LONG).show()
            }
            else
            {
                val calendarInstance = Calendar.getInstance()

                val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
                val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                val currentDate = dateFormat.format(calendarInstance.time)
                val currentTime = timeFormat.format(calendarInstance.time)

                val foodDetails = FoodDetails(foodName.text.toString(),
                                              currentDate,
                                              currentTime,
                                              selectedImagesUriList,
                                              selectedImagesPathList,
                                              foodProtein.text.toString().toIntOrNull(),
                                              foodCarbs.text.toString().toIntOrNull(),
                                              foodFats.text.toString().toIntOrNull())

                println("SAMAN: " + foodDetails.name)

                foodSharedPreferencesManager.addFoodItem("none", foodDetails)

                // Optionally, you can also clear the form fields here if needed
                // Clear the form fields after saving the entry
                foodName.text.clear()
                foodProtein.text.clear()
                foodCarbs.text.clear()
                foodFats.text.clear()
                selectedImagesUriList.clear()
                selectedImagesButton.isEnabled = false
                selectedImagesButton.setTextColor(ContextCompat.getColor(this, R.color.light_red))
            }
        }

        pickImagesLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK)
            {
                selectedImagesUriList = ArrayList()
                selectedImagesPathList = ArrayList()

                val intent = result.data
                intent?.clipData?.let { clipData ->
                    for (i in 0 until clipData.itemCount)
                    {
                        val uri = clipData.getItemAt(i).uri
                        val path = getPathFromUri(uri)
                        path?.let { selectedImagesPathList.add(it) }
                        selectedImagesUriList.add(uri)
                    }
                } ?: run {
                    val uri = intent?.data
                    uri?.let {
                        val path = getPathFromUri(uri)
                        path?.let { selectedImagesPathList.add(it) }
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
    }

    private fun openGalleryForMultipleImages()
    {
        val galleryIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        galleryIntent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
        pickImagesLauncher.launch(galleryIntent)
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
}