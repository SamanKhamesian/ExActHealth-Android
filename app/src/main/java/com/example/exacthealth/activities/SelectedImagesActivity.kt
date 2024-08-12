package com.example.exacthealth.activities

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ListView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.exacthealth.R
import com.example.exacthealth.classes.SelectedImageAdapter


class SelectedImagesActivity : AppCompatActivity()
{
    private var isEditMode = false
    private val selectedItems = mutableSetOf<Int>()
    private lateinit var adapter: SelectedImageAdapter
    private var selectedImagesArray: ArrayList<String> = ArrayList()
    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_selected_images)

        val listViewImages = findViewById<ListView>(R.id.selected_images_list_view)
        selectedImagesArray = intent.getStringArrayListExtra("imagesPathList")!!

        // Create and set adapter
        adapter =
            SelectedImageAdapter(this, R.layout.list_selected_images, selectedImagesArray, isEditMode, selectedItems)

        listViewImages.adapter = adapter

        val editButton = findViewById<TextView>(R.id.selected_images_edit_button)
        val deleteButton = findViewById<TextView>(R.id.selected_images_delete_button)
        val doneButton = findViewById<Button>(R.id.selected_images_done_button)

        val backToFoodDetailsButton = findViewById<RelativeLayout>(R.id.back_to_food_details_layout)

        backToFoodDetailsButton.setOnClickListener {
            finish()
        }

        doneButton.setOnClickListener {
            val resultIntent = Intent()
            resultIntent.putStringArrayListExtra("imagesPathList", selectedImagesArray)
            setResult(RESULT_OK, resultIntent)
            finish()
        }

        editButton.setOnClickListener {
            isEditMode = !isEditMode
            deleteButton.visibility = View.VISIBLE
            editButton.visibility = View.GONE
            adapter = SelectedImageAdapter(this,
                                           R.layout.list_selected_images,
                                           selectedImagesArray,
                                           isEditMode,
                                           selectedItems)
            listViewImages.adapter = adapter
        }

        deleteButton.setOnClickListener {
            val itemsToRemove = selectedItems.map { selectedImagesArray[it] }
            selectedImagesArray.removeAll(itemsToRemove)
            selectedItems.clear()
            isEditMode = false
            deleteButton.visibility = View.GONE
            editButton.visibility = View.VISIBLE
            adapter = SelectedImageAdapter(this,
                                           R.layout.list_selected_images,
                                           selectedImagesArray,
                                           isEditMode,
                                           selectedItems)
            listViewImages.adapter = adapter
        }
    }
}
