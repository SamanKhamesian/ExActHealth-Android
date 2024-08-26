package com.example.exacthealth.models

import android.app.Application
import android.util.Log
import androidx.health.connect.client.permission.HealthPermission
import androidx.health.connect.client.records.DistanceRecord
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

class DistanceViewModel(application: Application) : HealthDataViewModel(application)
{
    init
    {
        permissions = setOf(HealthPermission.getReadPermission(DistanceRecord::class))
    }

    private val _distanceRecord = MutableLiveData<List<DistanceRecord>>()
    val distanceRecord: LiveData<List<DistanceRecord>> = _distanceRecord

    override fun readData()
    {
        val zoneId = ZoneId.systemDefault()
        val startTime = LocalDate.now(zoneId).minusDays(4).atStartOfDay(zoneId).toInstant()
        val endTime = LocalDate.now(zoneId).minusDays(3).atStartOfDay(zoneId).toInstant()

        val readRequest = ReadRecordsRequest(recordType = DistanceRecord::class, timeRangeFilter = TimeRangeFilter.between(startTime, endTime))

        viewModelScope.launch {
            try
            {
                val response = healthConnectClient.readRecords(readRequest)

                println("KIR")

                if (response.records.isEmpty())
                {
                    _distanceRecord.postValue(emptyList())
                }
                else
                {
                    _distanceRecord.postValue(response.records)
                }
            }
            catch (e: Exception)
            {
                Log.d("ERROR IN READING STEP COUNTS RECORDS: ", " " + e.localizedMessage)
                _distanceRecord.postValue(emptyList())
            }
        }
    }

    fun formatDistanceRecords(distanceRecord: List<DistanceRecord>): List<Pair<String, Double>>
    {
        return distanceRecord.map { record ->
            val startTimeWithOffset = ZonedDateTime.ofInstant(record.startTime, record.startZoneOffset)
            val formattedTime = startTimeWithOffset.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))
            val distance = record.distance.inMiles
            formattedTime to distance
        }
    }
}