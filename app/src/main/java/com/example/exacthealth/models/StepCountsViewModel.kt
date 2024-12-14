package com.example.exacthealth.models

import android.app.Application
import android.util.Log
import androidx.health.connect.client.permission.HealthPermission
import androidx.health.connect.client.records.StepsRecord
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

class StepCountsViewModel(application: Application): HealthDataViewModel(application)
{
    init
    {
        permissions = setOf(HealthPermission.getReadPermission(StepsRecord::class))
    }

    private val _stepCounts = MutableLiveData<List<StepsRecord>>()
    val stepCounts: LiveData<List<StepsRecord>> = _stepCounts

    private val yesterdayDateTime = LocalDateTime.now().minusDays(1).format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))
    private val defaultValue = listOf(yesterdayDateTime to 0)

    override fun readData()
    {
        val zoneId = ZoneId.systemDefault()
        val startTime = LocalDate.now(zoneId).minusDays(1).atStartOfDay(zoneId).toInstant()
        val endTime = LocalDate.now(zoneId).atStartOfDay(zoneId).toInstant()

        val readRequest = ReadRecordsRequest(recordType = StepsRecord::class, timeRangeFilter = TimeRangeFilter.between(startTime, endTime))

        viewModelScope.launch {
            try
            {
                val response = healthConnectClient.readRecords(readRequest)

                if (response.records.isEmpty())
                {
                    _stepCounts.postValue(emptyList())
                }
                else
                {
                    _stepCounts.postValue(response.records)
                }
            }
            catch (e: Exception)
            {
                Log.d("ERROR IN READING STEP COUNTS RECORDS: ", " " + e.localizedMessage)
                _stepCounts.postValue(emptyList())
            }
        }
    }

    fun formatStepCountsRecords(stepCounts: List<StepsRecord>): List<Pair<String, Int>>
    {
        if (stepCounts.isEmpty())
        {
            return defaultValue
        }
        else
        {
            return stepCounts.map {record ->
                val startTimeWithOffset = ZonedDateTime.ofInstant(record.startTime, record.startZoneOffset)
                val formattedTime = startTimeWithOffset.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))
                val steps = record.count.toInt()
                formattedTime to steps
            }
        }
    }
}