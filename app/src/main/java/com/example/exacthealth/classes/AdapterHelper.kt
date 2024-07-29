package com.example.exacthealth.classes

import android.content.Context
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.PopupMenu
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.exacthealth.R

class SelectedImageAdapter(context: Context,
                           private val resource: Int,
                           private val images: List<String>) : ArrayAdapter<String>(context, resource, images)
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
                    val imagePath = images[i]
                    val fileUri = Uri.parse("file://$imagePath")
                    viewHolder.imageView1.setImageURI(fileUri)
                    viewHolder.imageView1.visibility = View.VISIBLE
                }

                1 ->
                {
                    val imagePath = images[i]
                    val fileUri = Uri.parse("file://$imagePath")
                    viewHolder.imageView2.setImageURI(fileUri)
                    viewHolder.imageView2.visibility = View.VISIBLE
                }

                2 ->
                {
                    val imagePath = images[i]
                    val fileUri = Uri.parse("file://$imagePath")
                    viewHolder.imageView3.setImageURI(fileUri)
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

open class FoodListAdapter(context: Context,
                           val foodList: MutableList<FoodDetails>) : ArrayAdapter<FoodDetails>(context, 0, foodList)
{
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View
    {
        val foodSharedPreferencesManager = FoodSharedPreferencesManager(context)
        val itemView = convertView ?: LayoutInflater.from(context).inflate(R.layout.list_saved_food, parent, false)

        val food = foodList[position]

        val nameTextView: TextView = itemView.findViewById(R.id.saved_food_name_text_view)
        val proteinTextView: TextView = itemView.findViewById(R.id.saved_food_protein_text_view)
        val carbsTextView: TextView = itemView.findViewById(R.id.saved_food_carbs_text_view)
        val fatTextView: TextView = itemView.findViewById(R.id.saved_fat_text_view)

        nameTextView.text = food.name
        "Protein (g): ${food.protein ?: "N/A"}".also { proteinTextView.text = it }
        "Carbs (g): ${food.carbs ?: "N/A"}".also { carbsTextView.text = it }
        "Fats (g): ${food.fats ?: "N/A"}".also { fatTextView.text = it }

        val optionsMenu: ImageView = itemView.findViewById(R.id.saved_food_options_menu)

        // Inside your activity or fragment
        val imagesLayout: RecyclerView = itemView.findViewById(R.id.food_images_list_view)
        imagesLayout.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        imagesLayout.visibility = View.INVISIBLE

        if (food.paths.isNotEmpty())
        {
            // Sample list of image URLs
            val imagesPathList = food.paths
            val adapter = FoodImageAdapter(imagesPathList)
            imagesLayout.adapter = adapter
            imagesLayout.visibility = View.VISIBLE
        }
        else
        {
            imagesLayout.visibility = View.INVISIBLE
        }

        optionsMenu.setOnClickListener {
            val popupMenu = PopupMenu(context, optionsMenu)
            popupMenu.menuInflater.inflate(R.menu.menu_card_view, popupMenu.menu)
            popupMenu.show()

            popupMenu.setOnMenuItemClickListener { menuItem ->
                when (menuItem.itemId) {
                    R.id.menu_edit -> {
                        // Handle edit action
                        true
                    }
                    R.id.menu_delete -> {
                        foodList.removeAt(position)
                        foodSharedPreferencesManager.saveFoodList(food.date, foodList)
                        notifyDataSetChanged()
                        showDeletedFoodToast(context)
                        true
                    }
                    R.id.menu_close -> {
                        popupMenu.dismiss()
                        true
                    }
                    else -> false
                }
            }
        }

        return itemView
    }
}

class FavoriteFoodListAdapter(context: Context,
                              foodList: MutableList<FoodDetails>,
                              private val itemClickListener: (FoodDetails) -> Unit) : FoodListAdapter(context, foodList)
{
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View
    {
        // Use the parent class's getView method to set up the view
        val itemView = super.getView(position, convertView, parent)

        // Customize the click listener for this adapter
        itemView.setOnClickListener {
            val item = foodList[position]
            itemClickListener(item)
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

private fun showDeletedFoodToast(context: Context)
{
    val message = "Item is deleted successfully"
    Toast.makeText(context, message, Toast.LENGTH_LONG).show()
}