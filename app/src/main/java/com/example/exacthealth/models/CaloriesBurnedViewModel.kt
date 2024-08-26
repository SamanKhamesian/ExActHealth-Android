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
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

class CaloriesBurnedViewModel(application: Application) : HealthDataViewModel(application)
{
    init
    {
        permissions = setOf(HealthPermission.getReadPermission(TotalCaloriesBurnedRecord::class))
    }

    private val _caloriesBurnedRecord = MutableLiveData<List<TotalCaloriesBurnedRecord>>()
    val caloriesBurnedRecord: LiveData<List<TotalCaloriesBurnedRecord>> = _caloriesBurnedRecord

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
                    _caloriesBurnedRecord.postValue(emptyList())
                }
                else
                {
                    _caloriesBurnedRecord.postValue(response.records)
                }
            }
            catch (e: Exception)
            {
                Log.d("ERROR IN READING HEART RATE RECORDS: ", " " + e.localizedMessage)
                _caloriesBurnedRecord.postValue(emptyList())
            }
        }
    }

    fun formatCaloriesBurnedRecords(caloriesBurnedRecord: List<TotalCaloriesBurnedRecord>): List<Pair<String, Double>>
    {
        return caloriesBurnedRecord.map { record ->
            val startTimeWithOffset = ZonedDateTime.ofInstant(record.startTime, record.startZoneOffset)
            val formattedTime = startTimeWithOffset.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))
            val caloriesBurned = record.energy.inCalories
            formattedTime to caloriesBurned
        }
    }
}