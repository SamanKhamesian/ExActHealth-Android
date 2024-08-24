package com.example.exacthealth.classes

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.provider.Settings
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale


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

fun isInternetAvailable(context: Context): Boolean
{
    val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    val network = connectivityManager.activeNetwork ?: return false
    val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
    return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
}

fun showNoInternetDialog(context: Context, retryAction: () -> Unit)
{
    AlertDialog.Builder(context)
        .setTitle("No Internet Connection")
        .setMessage("Internet connection is required to proceed. Would you like to turn on your network and try again?")
        .setPositiveButton("Retry") { _, _ ->
            // Call the retry action which will re-run the testRequest
            retryAction()
        }
        .setNeutralButton("Settings") { _, _ ->
            // Open network settings
            context.startActivity(Intent(Settings.ACTION_WIRELESS_SETTINGS))
        }
        .show()
}


fun getDefaultValue(value: Int): String?
{
    return if (value != -1) value.toString()
    else null
}

fun convertDisplayDateToDashFormat(inputDate: String): String
{
    val inputFormat = SimpleDateFormat("MMM dd, yyyy", Locale.US)
    val outputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
    val newDate = inputFormat.parse(inputDate)
    return outputFormat.format(newDate!!)
}

fun createDashFormatDate(year: Int, month: Int, dayOfMonth: Int): String
{
    val calendar = Calendar.getInstance()
    calendar.set(year, month, dayOfMonth)
    val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
    return dateFormat.format(calendar.time)
}

fun showSaveFoodToast(context: Context)
{
    val message = "Item is saved successfully"
    Toast.makeText(context, message, Toast.LENGTH_LONG).show()
}

fun showFoodNameErrorToast(context: Context)
{
    val message = "Food name cannot be empty!"
    Toast.makeText(context, message, Toast.LENGTH_LONG).show()
}

fun showEditFoodToast(context: Context)
{
    val message = "Item is edited successfully"
    Toast.makeText(context, message, Toast.LENGTH_LONG).show()
}

fun showAddFavoriteFoodToast(context: Context)
{
    val message = "Item is added to the favorite list successfully"
    Toast.makeText(context, message, Toast.LENGTH_LONG).show()
}

fun showEmptyFoodListToast(context: Context, date: String)
{
    val message = "No food entries for $date"
    Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
}

fun showPermissionDeniedToast(context: Context)
{
    val message = "Permission denied"
    Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
}
