package com.example.exacthealth

import android.content.Context
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import com.google.gson.reflect.TypeToken
import java.lang.reflect.Type
import java.text.SimpleDateFormat
import java.util.Locale

class FoodDetails
{
    var name: String = ""
    var date: String = ""
    var time: String = ""
    var images: ArrayList<Uri>? = ArrayList()
    var protein: Int? = null
    var carbs: Int? = null
    var fats: Int? = null

    // Constructor with required attributes
    // Primary constructor with required parameters
    constructor(name: String, date: String, time: String)
    {
        this.name = name
        this.date = convertDateFormat(date)
        this.time = time
    }

    // Secondary constructor with optional parameters
    constructor(name: String,
                date: String,
                time: String,
                images: ArrayList<Uri>? = ArrayList(),
                protein: Int? = null,
                carbs: Int? = null,
                fats: Int? = null) : this(name, date, time)
    {
        this.images = images
        this.protein = protein
        this.carbs = carbs
        this.fats = fats
    }

    private fun convertDateFormat(inputDate: String): String
    {
        val inputFormat = SimpleDateFormat("MMM dd, yyyy", Locale.US)
        val outputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)

        val newDate = inputFormat.parse(inputDate)
        return outputFormat.format(newDate!!)
    }
}

class FoodSharedPreferencesManager(private val context: Context)
{
    private val sharedPreferences = context.getSharedPreferences("food_data", Context.MODE_PRIVATE)

    private fun saveFoodList(date: String, foodList: List<FoodDetails>)
    {
        val editor = sharedPreferences.edit()
        val json = GsonProvider.gson.toJson(foodList)
        editor.putString(date, json)
        editor.apply()
    }

    fun loadFoodList(date: String): MutableList<FoodDetails>
    {
        val json = sharedPreferences.getString(date, "") ?: return mutableListOf()
        if (json.isEmpty())
        {
            return mutableListOf()
        }
        val type: Type = object : TypeToken<MutableList<FoodDetails>>()
        {}.type
        return GsonProvider.gson.fromJson(json, type) ?: mutableListOf()
    }

    fun addFoodItem(date: String, foodItem: FoodDetails)
    {
        val foodList = loadFoodList(date)
        foodList.add(foodItem)
        saveFoodList(date, foodList)
    }
}

class FoodListAdapter(context: Context, private val foodList: MutableList<FoodDetails>) : ArrayAdapter<FoodDetails>(
    context,
    0,
    foodList)
{
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View
    {
        val itemView = convertView ?: LayoutInflater.from(context).inflate(R.layout.list_saved_food, parent, false)

        val food = foodList[position]

        val nameTextView: TextView = itemView.findViewById(R.id.nameTextView)
        val proteinTextView: TextView = itemView.findViewById(R.id.proteinTextView)
        val carbsTextView: TextView = itemView.findViewById(R.id.carbsTextView)
        val fatTextView: TextView = itemView.findViewById(R.id.fatTextView)

        nameTextView.text = food.name
        proteinTextView.text = "Protein (g): ${food.protein ?: "N/A"}"
        carbsTextView.text = "Carbs (g): ${food.carbs ?: "N/A"}"
        fatTextView.text = "Fats (g): ${food.fats ?: "N/A"}"

        return itemView
    }
}