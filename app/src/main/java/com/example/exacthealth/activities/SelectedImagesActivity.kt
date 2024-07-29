package com.example.exacthealth.activities

import android.os.Bundle
import android.widget.ListView
import android.widget.RelativeLayout
import androidx.appcompat.app.AppCompatActivity
import com.example.exacthealth.R
import com.example.exacthealth.classes.SelectedImageAdapter


class SelectedImagesActivity : AppCompatActivity()
{
    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_selected_images)

        val listViewImages = findViewById<ListView>(R.id.selected_images_list_view)
        val selectedImagesArray = intent.getStringArrayListExtra("imagesPathList")

        // Create and set adapter
        val adapter = SelectedImageAdapter(this, R.layout.list_selected_images, selectedImagesArray.orEmpty())
        listViewImages.adapter = adapter

        val backToFoodDetailsButton = findViewById<RelativeLayout>(R.id.back_to_food_details_layout)

        backToFoodDetailsButton.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
    }
}