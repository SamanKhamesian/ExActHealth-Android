package com.example.exacthealth.classes

import android.content.Context
import android.content.SharedPreferences

class SelectedDatePreferencesManager(context: Context)
{
    private val NAME = "selected_date_from_calendar"
    private val KEY = "selected_date"
    private val sharedPreferences: SharedPreferences = context.getSharedPreferences(NAME, Context.MODE_PRIVATE)

    fun setSelectedDate(value: String)
    {
        val editor = sharedPreferences.edit()
        editor.putString(KEY, value)
        editor.apply()
    }

    fun getSelectedDate(): String?
    {
        return sharedPreferences.getString(KEY, null)
    }
}
