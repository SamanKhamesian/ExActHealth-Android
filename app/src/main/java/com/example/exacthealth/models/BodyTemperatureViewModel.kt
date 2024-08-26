package com.example.exacthealth.models

import android.app.Application
import android.util.Log
import androidx.health.connect.client.permission.HealthPermission
import androidx.health.connect.client.records.BodyTemperatureRecord
import androidx.health.connect.client.request.ReadRecordsRequest
import androidx.health.connect.client.time.TimeRangeFilter
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.ZoneId

class BodyTemperatureViewModel(application: Application) : HealthDataViewModel(application)
{
    init
    {
        permissions = setOf(HealthPermission.getReadPermission(BodyTemperatureRecord::class))
    }

    private val _bodyTemp = MutableLiveData<List<BodyTemperatureRecord>>()
    val bodyTemp: LiveData<List<BodyTemperatureRecord>> = _bodyTemp

    override fun readData()
    {
        val zoneId = ZoneId.systemDefault()
        val startTime = LocalDate.now(zoneId).minusDays(1).atStartOfDay(zoneId).toInstant()
        val endTime = LocalDate.now(zoneId).atStartOfDay(zoneId).toInstant()

        val readRequest = ReadRecordsRequest(recordType = BodyTemperatureRecord::class, timeRangeFilter = TimeRangeFilter.between(startTime, endTime))

        viewModelScope.launch {
            try
            {
                val response = healthConnectClient.readRecords(readRequest)

                if (response.records.isEmpty())
                {
                    _bodyTemp.postValue(emptyList())
                }
                else
                {
                    _bodyTemp.postValue(response.records)
                }
            }
            catch (e: Exception)
            {
                Log.d("ERROR IN READING HEART RATE RECORDS: ", " " + e.localizedMessage)
                _bodyTemp.postValue(emptyList())
            }
        }
    }
}