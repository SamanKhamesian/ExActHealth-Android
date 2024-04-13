package com.example.exacthealth

import android.content.Context
import com.google.gson.reflect.TypeToken
import java.lang.reflect.Type

class FoodSharedPreferencesManager(private val context: Context)
{
    private val sharedPreferences = context.getSharedPreferences("food_data", Context.MODE_PRIVATE)

    private fun saveFoodList(date: String, foodList: List<Food>)
    {
        val editor = sharedPreferences.edit()
        val json = GsonProvider.gson.toJson(foodList)
        editor.putString(date, json)
        editor.apply()
    }

    fun loadFoodList(date: String): MutableList<Food>
    {
        val json = sharedPreferences.getString(date, "") ?: return mutableListOf()
        if (json.isEmpty())
        {
            return mutableListOf()
        }
        val type: Type = object : TypeToken<MutableList<Food>>()
        {}.type
        return GsonProvider.gson.fromJson(json, type) ?: mutableListOf()
    }

    fun addFoodItem(date: String, foodItem: Food)
    {
        val foodList = loadFoodList(date)
        foodList.add(foodItem)
        saveFoodList(date, foodList)
    }
}