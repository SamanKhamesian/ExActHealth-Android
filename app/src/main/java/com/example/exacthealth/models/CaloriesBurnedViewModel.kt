package com.example.exacthealth.models


import android.app.Application
import android.util.Log
import androidx.health.connect.client.permission.HealthPermission
import androidx.health.connect.client.records.TotalCaloriesBurnedRecord
import androidx.health.connect.client.request.ReadRecordsRequest
import androidx.health.connect.client.time.TimeRangeFilter
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import kotlin.math.floor


data class CaloriesBurned(val startTime: String, val endTime: String, val duration: Long, val calories: Int)

class CaloriesBurnedViewModel(application: Application): HealthDataViewModel(application)
{
    init
    {
        permissions = setOf(HealthPermission.getReadPermission(TotalCaloriesBurnedRecord::class))
    }

    private val _caloriesBurned = MutableLiveData<List<TotalCaloriesBurnedRecord>>()
    val caloriesBurned: LiveData<List<TotalCaloriesBurnedRecord>> = _caloriesBurned

    private val yesterdayDateTime = LocalDateTime.now().minusDays(1).format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))
    private val defaultValue = listOf(CaloriesBurned(startTime = yesterdayDateTime, endTime = yesterdayDateTime, duration = 0, calories = 0))

    override fun readData()
    {
        val zoneId = ZoneId.systemDefault()
        val startTime = LocalDate.now(zoneId).minusDays(1).atStartOfDay(zoneId).toInstant()
        val endTime = LocalDate.now(zoneId).atStartOfDay(zoneId).toInstant()

        val readRequest = ReadRecordsRequest(recordType = TotalCaloriesBurnedRecord::class, timeRangeFilter = TimeRangeFilter.between(startTime, endTime))

        viewModelScope.launch {
            try
            {
                val response = healthConnectClient.readRecords(readRequest)

                if (response.records.isEmpty())
                {
                    _caloriesBurned.postValue(emptyList())
                }
                else
                {
                    _caloriesBurned.postValue(response.records)
                }

            }
            catch (e: Exception)
            {
                Log.d("ERROR IN READING CALORIES BURNED RECORDS: ", " " + e.localizedMessage)
                _caloriesBurned.postValue(emptyList())
            }
        }
    }

    fun formatCaloriesBurnedRecords(caloriesBurned: List<TotalCaloriesBurnedRecord>): List<CaloriesBurned>
    {
        if (caloriesBurned.isEmpty())
        {
            return defaultValue
        }
        else
        {
            return caloriesBurned.map {record ->
                val startTime = ZonedDateTime.ofInstant(record.startTime, record.startZoneOffset)
                val endTime = ZonedDateTime.ofInstant(record.endTime, record.endZoneOffset)

                val formattedStartTime = startTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))
                val formattedEndTime = endTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))

                val durationMinutes = java.time.Duration.between(record.startTime, record.endTime).toMinutes()

                val calories = floor(record.energy.inKilocalories).toInt()

                CaloriesBurned(startTime = formattedStartTime, endTime = formattedEndTime, duration = durationMinutes, calories = calories)
            }
        }
    }
}