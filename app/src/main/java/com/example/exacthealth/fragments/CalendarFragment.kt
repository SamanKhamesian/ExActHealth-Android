package com.example.exacthealth.fragments

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CalendarView
import android.widget.ImageView
import android.widget.ListView
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.exacthealth.R
import com.example.exacthealth.activities.AddFoodActivity
import com.example.exacthealth.classes.FoodDetails
import com.example.exacthealth.classes.FoodListAdapter
import com.example.exacthealth.classes.FoodSharedPreferencesManager
import com.example.exacthealth.classes.SelectedDatePreferencesManager
import com.example.exacthealth.classes.createDashFormatDate
import com.example.exacthealth.classes.showEmptyFoodListToast
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class CalendarFragment : Fragment()
{
    private lateinit var foodSharedPreferencesManager: FoodSharedPreferencesManager
    private lateinit var selectedDatePreferencesManager: SelectedDatePreferencesManager
    private lateinit var foodListView: ListView
    private lateinit var foodList: MutableList<FoodDetails>

    private val PERMISSION_CODE = 1001

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View?
    {
        return inflater.inflate(R.layout.fragment_calendar, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?)
    {
        super.onViewCreated(view, savedInstanceState)

        foodSharedPreferencesManager = FoodSharedPreferencesManager(requireContext())
        selectedDatePreferencesManager = SelectedDatePreferencesManager(requireContext())
        foodListView = view.findViewById(R.id.calendar_food_list)
        foodList = ArrayList()

        val calendarView = view.findViewById<CalendarView>(R.id.calendar_widget)
        val addNewFood = view.findViewById<ImageView>(R.id.add_food_details_icon)

        val currentDate = Calendar.getInstance()
        val currentYear = currentDate.get(Calendar.YEAR)
        val currentMonth = currentDate.get(Calendar.MONTH)
        val currentDayOfMonth = currentDate.get(Calendar.DAY_OF_MONTH)

        var selectedDateFormat = createDashFormatDate(currentYear, currentMonth, currentDayOfMonth)
        selectedDatePreferencesManager.setSelectedDate(selectedDateFormat)

        calendarView.date = currentDate.timeInMillis
        foodList = foodSharedPreferencesManager.loadFoodList(selectedDateFormat)

        // Android 12 (API level 31) or lower
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU)
        {
            if (checkPermissions()) updateListView(foodList)
            else requestPermissions()
        }
        else
        {
            // Android 13+ (API level 33 or higher)
            if (checkPermissionForCamera()) updateListView(foodList)
            else requestPermissionForCamera()
        }

        // Handle date changes in CalendarView
        calendarView.setOnDateChangeListener { _, year, month, dayOfMonth ->
            selectedDateFormat = createDashFormatDate(year, month, dayOfMonth)
            selectedDatePreferencesManager.setSelectedDate(selectedDateFormat)
            foodList = foodSharedPreferencesManager.loadFoodList(selectedDateFormat)

            updateListView(foodList)

            if (foodList.isEmpty())
            {
                showEmptyFoodListToast(requireContext(), selectedDateFormat)
            }
        }

        // Add food button click listener
        addNewFood.setOnClickListener {
            val intent = Intent(requireActivity(), AddFoodActivity::class.java)
            intent.putExtra("from", "calendar")
            startActivity(intent)
        }
    }

    // Permission handling logic
    private fun checkPermissions(): Boolean
    {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
        {
            val mediaPermission =
                ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.READ_MEDIA_IMAGES) == PackageManager.PERMISSION_GRANTED
            val cameraPermission = ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
            mediaPermission && cameraPermission
        }
        else
        {
            val storagePermission =
                ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
            val cameraPermission = ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
            storagePermission && cameraPermission
        }
    }

    private fun checkPermissionForCamera(): Boolean
    {
        return ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestPermissions()
    {
        val permissionsNeeded = mutableListOf<String>()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
        {
            if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.READ_MEDIA_IMAGES) != PackageManager.PERMISSION_GRANTED)
            {
                permissionsNeeded.add(Manifest.permission.READ_MEDIA_IMAGES)
            }
        }
        else
        {
            if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)
            {
                permissionsNeeded.add(Manifest.permission.READ_EXTERNAL_STORAGE)
            }
        }
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED)
        {
            permissionsNeeded.add(Manifest.permission.CAMERA)
        }
        if (permissionsNeeded.isNotEmpty())
        {
            ActivityCompat.requestPermissions(requireActivity(), permissionsNeeded.toTypedArray(), PERMISSION_CODE)
        }
    }

    private fun requestPermissionForCamera()
    {
        ActivityCompat.requestPermissions(requireActivity(), arrayOf(Manifest.permission.CAMERA), PERMISSION_CODE)
    }

    // Handle permissions result in the fragment
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray)
    {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_CODE)
        {
            if (grantResults.isNotEmpty() && grantResults.all { it == PackageManager.PERMISSION_GRANTED })
            {
                updateListView(foodList)
            }
            else
            {
                showPermissionDeniedDialog()
            }
        }
    }

    private fun updateListView(foodList: MutableList<FoodDetails>)
    {
        if (foodList.isNotEmpty())
        {
            val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
            foodList.sortBy { food -> timeFormat.parse(food.time) }
            foodListView.visibility = View.VISIBLE
            val adapter = FoodListAdapter(requireContext(), foodList)
            foodListView.adapter = adapter
        }
        else
        {
            foodListView.visibility = View.INVISIBLE
        }
    }

    private fun showPermissionDeniedDialog()
    {
        AlertDialog.Builder(requireContext()).setTitle("Permission Required")
            .setMessage("Permission to read external storage is required for this app. Please enable it in the app settings.")
            .setPositiveButton("Go to Settings") { _, _ ->
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                    data = Uri.fromParts("package", requireContext().packageName, null)
                }
                startActivity(intent)
            }.setNegativeButton("Cancel", null).show()
    }
}
