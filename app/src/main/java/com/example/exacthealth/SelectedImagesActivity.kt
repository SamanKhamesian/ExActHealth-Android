package com.example.exacthealth

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.ListView
import android.widget.RelativeLayout
import androidx.appcompat.app.AppCompatActivity


class SelectedImagesActivity : AppCompatActivity()
{
    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_selected_images)
        val listViewImages = findViewById<ListView>(R.id.selected_images_list_view)
        val selectedImagesArray = intent.getParcelableArrayListExtra<Uri>("ImagesList")

        // Create and set adapter
        val adapter = ImageAdapter(this, R.layout.list_selected_images, selectedImagesArray.orEmpty())
        listViewImages.adapter = adapter

        val backToFoodDetailsButton = findViewById<RelativeLayout>(R.id.back_to_food_details_layout)

        backToFoodDetailsButton.setOnClickListener {
            onBackPressed()
        }
    }
}

class ImageAdapter(context: Context, private val resource: Int, private val images: List<Uri>) : ArrayAdapter<Uri>(
    context,
    resource,
    images)
{
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View
    {
        var itemView = convertView
        val viewHolder: ViewHolder

        if (itemView == null)
        {
            itemView = LayoutInflater.from(context).inflate(resource, parent, false)
            viewHolder = ViewHolder(itemView)
            itemView.tag = viewHolder
        }
        else
        {
            viewHolder = itemView.tag as ViewHolder
        }

        val startIndex = position * 3
        val endIndex = minOf(startIndex + 3, images.size) // Ensure endIndex doesn't exceed the size of the images list

        // Hide all image views initially
        viewHolder.imageView1.visibility = View.INVISIBLE
        viewHolder.imageView2.visibility = View.INVISIBLE
        viewHolder.imageView3.visibility = View.INVISIBLE

        // Display images
        for ((index, i) in (startIndex until endIndex).withIndex())
        {
            when (index)
            {
                0 ->
                {
                    viewHolder.imageView1.setImageURI(images[i])
                    viewHolder.imageView1.visibility = View.VISIBLE
                }

                1 ->
                {
                    viewHolder.imageView2.setImageURI(images[i])
                    viewHolder.imageView2.visibility = View.VISIBLE
                }

                2 ->
                {
                    viewHolder.imageView3.setImageURI(images[i])
                    viewHolder.imageView3.visibility = View.VISIBLE
                }
            }
        }

        return itemView!!
    }

    private class ViewHolder(view: View)
    {
        val imageView1: ImageView = view.findViewById(R.id.favorite_food_image_view1)
        val imageView2: ImageView = view.findViewById(R.id.favorite_food_image_view2)
        val imageView3: ImageView = view.findViewById(R.id.favorite_food_image_view3)
    }
}

