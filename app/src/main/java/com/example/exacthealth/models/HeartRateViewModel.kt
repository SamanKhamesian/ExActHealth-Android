package com.example.exacthealth.models

import android.app.Application
import android.util.Log
import androidx.health.connect.client.permission.HealthPermission
import androidx.health.connect.client.records.HeartRateRecord
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

class HeartRateViewModel(application: Application): HealthDataViewModel(application)
{
    init
    {
        permissions = setOf(HealthPermission.getReadPermission(HeartRateRecord::class))
    }

    private val _heartRates = MutableLiveData<List<HeartRateRecord>>()
    val heartRates: LiveData<List<HeartRateRecord>> = _heartRates

    private val yesterdayDateTime = LocalDateTime.now().minusDays(1).format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))
    private val defaultValue = listOf(yesterdayDateTime to 0)

    override fun readData()
    {
        val zoneId = ZoneId.systemDefault()
        val startTime = LocalDate.now(zoneId).minusDays(1).atStartOfDay(zoneId).toInstant()
        val endTime = LocalDate.now(zoneId).atStartOfDay(zoneId).toInstant()

        val readRequest = ReadRecordsRequest(recordType = HeartRateRecord::class, timeRangeFilter = TimeRangeFilter.between(startTime, endTime))

        viewModelScope.launch {
            try
            {
                val response = healthConnectClient.readRecords(readRequest)

                if (response.records.isEmpty())
                {
                    _heartRates.postValue(emptyList())
                }
                else
                {
                    _heartRates.postValue(response.records)
                }
            }
            catch (e: Exception)
            {
                Log.d("ERROR IN READING HEART RATE RECORDS: ", " " + e.localizedMessage)
                _heartRates.postValue(emptyList())
            }
        }
    }

    fun formatHeartRateRecords(heartRates: List<HeartRateRecord>): List<Pair<String, Int>>
    {
        if (heartRates.isEmpty())
        {
            return defaultValue
        }
        else
        {
            return heartRates.mapNotNull {record ->
                val sample = record.samples.lastOrNull() ?: return@mapNotNull null
                val beatsPerMinute = sample.beatsPerMinute
                val startTimeWithOffset = ZonedDateTime.ofInstant(record.startTime, record.startZoneOffset)
                val formattedTime = startTimeWithOffset.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))
                formattedTime to beatsPerMinute.toInt()
            }
        }
    }
}