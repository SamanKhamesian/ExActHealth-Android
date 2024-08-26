package com.example.exacthealth.models

import android.app.Application
import android.util.Log
import androidx.health.connect.client.permission.HealthPermission
import androidx.health.connect.client.records.SleepSessionRecord
import androidx.health.connect.client.request.ReadRecordsRequest
import androidx.health.connect.client.time.TimeRangeFilter
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import java.time.Duration
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

class SleepSessionsViewModel(application: Application) : HealthDataViewModel(application)
{
    init
    {
        permissions = setOf(HealthPermission.getReadPermission(SleepSessionRecord::class))
    }

    private val _sleepSessionRecord = MutableLiveData<List<SleepSessionRecord>>()
    val sleepSessionRecord: LiveData<List<SleepSessionRecord>> = _sleepSessionRecord

    override fun readData()
    {
        val zoneId = ZoneId.systemDefault()
        val startTime = LocalDate.now(zoneId).minusDays(1).atStartOfDay(zoneId).toInstant()
        val endTime = LocalDate.now(zoneId).atStartOfDay(zoneId).toInstant()

        val readRequest = ReadRecordsRequest(recordType = SleepSessionRecord::class, timeRangeFilter = TimeRangeFilter.between(startTime, endTime))

        viewModelScope.launch {
            try
            {
                val response = healthConnectClient.readRecords(readRequest)

                if (response.records.isEmpty())
                {
                    _sleepSessionRecord.postValue(emptyList())
                }
                else
                {
                    _sleepSessionRecord.postValue(response.records)
                }
            }
            catch (e: Exception)
            {
                Log.d("ERROR IN READING HEART RATE RECORDS: ", " " + e.localizedMessage)
                _sleepSessionRecord.postValue(emptyList())
            }
        }
    }

    fun formatSleepSessionRecords(sleepSessionRecord: List<SleepSessionRecord>): List<Pair<String, Long>>
    {
        return sleepSessionRecord.map { record ->
            val startTimeWithOffset = ZonedDateTime.ofInstant(record.startTime, record.startZoneOffset)
            val formattedTime = startTimeWithOffset.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))
            val duration = Duration.between(record.startTime, record.endTime).toMinutes()
            formattedTime to duration
        }
    }
}