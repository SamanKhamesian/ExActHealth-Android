package com.example.exacthealth.activities

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.widget.Button
import android.widget.EditText
import android.widget.RelativeLayout
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.example.exacthealth.R
import com.example.exacthealth.classes.FoodDetails
import com.example.exacthealth.classes.FoodSharedPreferencesManager
import com.example.exacthealth.classes.getDefaultValue
import com.example.exacthealth.classes.showFoodNameErrorToast
import com.example.exacthealth.classes.showSaveFavoriteFoodToast
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


class AddFavoriteFoodActivity : AppCompatActivity()
{
    private lateinit var foodSharedPreferencesManager: FoodSharedPreferencesManager
    private lateinit var pickImagesLauncher: ActivityResultLauncher<Intent>
    private lateinit var cameraLauncher: ActivityResultLauncher<Intent>
    private lateinit var selectedImagesActivityLauncher: ActivityResultLauncher<Intent>

    private var selectedImagesPathList: ArrayList<String> = ArrayList()
    private var currentPhotoPath: String? = null

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

        // Set up the ActivityResultLauncher
        selectedImagesActivityLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
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

        pickImagesButton.setOnClickListener {
            showImageSourceOptions()
        }

        backToFavoriteFoodsButton.setOnClickListener {
            val resultIntent = Intent()
            setResult(RESULT_OK, resultIntent)
            finish()
        }

        selectedImagesButton.setOnClickListener {
            val intent = Intent(this, SelectedImagesActivity::class.java)
            intent.putExtra("imagesPathList", selectedImagesPathList)
            selectedImagesActivityLauncher.launch(intent)
        }

        saveEntryButton.setOnClickListener {

            if (foodName.text.isNullOrEmpty())
            {
                showFoodNameErrorToast(this)
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

                showSaveFavoriteFoodToast(this)

                val resultIntent = Intent()
                setResult(RESULT_OK, resultIntent)
                finish()
            }
        }
    }

    private fun showImageSourceOptions()
    {
        val options = arrayOf("Select from Gallery", "Take a Picture")
        AlertDialog.Builder(this).setTitle("Choose an Option").setItems(options) { _, which ->
            when (which)
            {
                0 -> openGalleryForMultipleImages()
                1 -> openCamera()
            }
        }.show()
    }

    private fun openGalleryForMultipleImages()
    {
        val galleryIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        galleryIntent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
        pickImagesLauncher.launch(galleryIntent)
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
            foodProtein.setText(getDefaultValue(intent.getIntExtra("foodProtein", -1)))
            foodCarbs.setText(getDefaultValue(intent.getIntExtra("foodCarbs", -1)))
            foodFats.setText(getDefaultValue(intent.getIntExtra("foodFats", -1)))
            selectedImagesPathList = intent.getStringArrayListExtra("foodImagesPathList")!!
        }
    }
}