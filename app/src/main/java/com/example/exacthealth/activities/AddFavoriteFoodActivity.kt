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


class AddFavoriteFoodActivity : AppCompatActivity()
{
    private lateinit var foodSharedPreferencesManager: FoodSharedPreferencesManager
    private lateinit var pickImagesLauncher: ActivityResultLauncher<Intent>
    private var selectedImagesPathList: ArrayList<String> = ArrayList()

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_favorite_food)

        foodSharedPreferencesManager = FoodSharedPreferencesManager(this)

        val from = intent.getStringExtra("from").toString()

        val foodName = findViewById<EditText>(R.id.favorite_food_name_text_edit)

        val pickImagesButton = findViewById<Button>(R.id.favorite_food_pick_images_button)
        val selectedImagesButton = findViewById<Button>(R.id.favorite_food_selected_images_button)

        val foodProtein = findViewById<EditText>(R.id.favorite_food_protein_text_edit)
        val foodCarbs = findViewById<EditText>(R.id.favorite_food_carbs_text_edit)
        val foodFats = findViewById<EditText>(R.id.favorite_food_fats_text_edit)

        val backToFavoriteFoodsButton = findViewById<RelativeLayout>(R.id.back_to_favorite_food_layout)
        val saveEntryButton = findViewById<Button>(R.id.favorite_food_save_entry_button)

        setDefaultInformation(from, foodName, foodProtein, foodCarbs, foodFats)

        if (selectedImagesPathList.isNotEmpty())
        {
            selectedImagesButton.isEnabled = true
            selectedImagesButton.setTextColor(ContextCompat.getColor(this, R.color.red))
        }

        pickImagesButton.setOnClickListener {
            openGalleryForMultipleImages()
        }

        backToFavoriteFoodsButton.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        selectedImagesButton.setOnClickListener {
            val intent = Intent(this, SelectedImagesActivity::class.java)
            intent.putExtra("imagesPathList", selectedImagesPathList)
            startActivity(intent)
        }

        saveEntryButton.setOnClickListener {

            if (foodName.text.isNullOrEmpty())
            {
                showFoodNameErrorToast()
            }
            else
            {
                val currentDate = "none"
                val currentTime = "none"

                val foodDetails = FoodDetails(foodName.text.toString(),
                                              currentDate,
                                              currentTime,
                                              selectedImagesPathList,
                                              foodProtein.text.toString().toIntOrNull(),
                                              foodCarbs.text.toString().toIntOrNull(),
                                              foodFats.text.toString().toIntOrNull())

                if (from == "edit")
                {
                    val foodList = foodSharedPreferencesManager.loadFoodList("none")
                    val foodPosition = intent.getIntExtra("foodPosition", 0)
                    foodList.removeAt(foodPosition)
                    foodList.add(foodPosition, foodDetails)
                    foodSharedPreferencesManager.saveFoodList(currentDate, foodList)
                }

                else
                {
                    foodSharedPreferencesManager.addFoodItem(currentDate, foodDetails)
                }

                showSaveFavoriteFoodToast()
                val intent = Intent(this, FavoriteFoodActivity::class.java)
                startActivity(intent)
            }
        }

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

                if (selectedImagesPathList.isNotEmpty())
                {
                    selectedImagesButton.isEnabled = true
                    selectedImagesButton.setTextColor(ContextCompat.getColor(this, R.color.red))
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

    private fun setDefaultInformation(from: String?,
                                      foodName: EditText,
                                      foodProtein: EditText,
                                      foodCarbs: EditText,
                                      foodFats: EditText)
    {
        if (from == "edit")
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

    private fun showSaveFavoriteFoodToast()
    {
        val message = "Item is saved successfully"
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }

    private fun showFoodNameErrorToast()
    {
        val message = "Food name cannot be empty!"
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }
}