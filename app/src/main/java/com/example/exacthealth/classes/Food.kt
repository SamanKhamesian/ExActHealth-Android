package com.example.exacthealth.classes

import java.io.File
import android.content.Context
import com.google.gson.reflect.TypeToken
import java.lang.reflect.Type
import java.text.SimpleDateFormat
import java.util.Locale

class FoodDetails
{
    var name: String = ""
    var date: String = ""
    var time: String = ""
    var paths: ArrayList<String> = ArrayList()
    var protein: Int? = null
    var carbs: Int? = null
    var fats: Int? = null

    // Constructor with required attributes
    // Primary constructor with required parameters
    constructor(name: String, date: String, time: String)
    {
        this.name = name
        this.date = date
        this.time = time
    }

    // Secondary constructor with optional parameters
    constructor(name: String,
                date: String,
                time: String,
                paths: ArrayList<String> = ArrayList(),
                protein: Int? = null,
                carbs: Int? = null,
                fats: Int? = null) : this(name, date, time)
    {
        this.paths = paths
        this.protein = protein
        this.carbs = carbs
        this.fats = fats
    }
}

class FoodSharedPreferencesManager(private val context: Context)
{
    private val sharedPreferences = context.getSharedPreferences("food_data", Context.MODE_PRIVATE)
    private val serverRequestHandler: ServerRequestHandler = ServerRequestHandler(context)

    fun saveFoodList(date: String, foodList: MutableList<FoodDetails>)
    {
        if (date != "None")
        {
            val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())

            foodList.sortBy { food: FoodDetails ->
                timeFormat.parse(food.time)
            }
        }

        val editor = sharedPreferences.edit()
        val json = GsonProvider.gson.toJson(foodList)
        editor.putString(date, json)
        editor.apply()

        val allPaths = foodList.flatMap { it.paths }.toCollection(ArrayList())

        val updatedList = sharedPreferences.getString(date, "") ?: ""
        serverRequestHandler.sendUpdatedList(date = date, jsonFoodList = updatedList, context)
        serverRequestHandler.sendFoodImages(date = date, imagePaths = allPaths, context)
    }

    fun loadFoodList(date: String): MutableList<FoodDetails>
    {
        val json = serverRequestHandler.getFoodListFromDate(date = date, context)

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