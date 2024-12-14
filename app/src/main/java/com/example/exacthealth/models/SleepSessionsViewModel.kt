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
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

data class SleepStage(val startTime: String, val endTime: String, val duration: Long, val stageCode: Int, val stageName: String)

class SleepSessionViewModel(application: Application): HealthDataViewModel(application)
{
    init
    {
        permissions = setOf(HealthPermission.getReadPermission(SleepSessionRecord::class))
    }

    private val _sleepSessionRecord = MutableLiveData<List<SleepSessionRecord>>()
    val sleepSessionRecord: LiveData<List<SleepSessionRecord>> = _sleepSessionRecord

    private val yesterdayDateTime = LocalDateTime.now().minusDays(1).format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))
    private val defaultSleepStage =
        listOf(SleepStage(startTime = yesterdayDateTime, endTime = yesterdayDateTime, duration = 0L, stageCode = 0, stageName = "Unknown Stage"))

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
                Log.d("ERROR IN READING SLEEP SESSION RECORDS: ", " " + e.localizedMessage)
                _sleepSessionRecord.postValue(emptyList())
            }
        }
    }

    fun formatSleepSessionRecords(sleepSession: List<SleepSessionRecord>): List<SleepStage>
    {
        if (sleepSession.isEmpty())
        {
            return defaultSleepStage
        }
        else
        {
            return sleepSession.flatMap {session ->
                session.stages.map {stage ->
                    val startTime = ZonedDateTime.ofInstant(stage.startTime, session.endZoneOffset)
                    val endTime = ZonedDateTime.ofInstant(stage.endTime, session.endZoneOffset)

                    val formattedStartTime = startTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))
                    val formattedEndTime = endTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))

                    val durationMinutes = java.time.Duration.between(stage.startTime, stage.endTime).toMinutes()

                    val stageCode = stage.stage
                    val stageName = mapStageToName(stage.stage)

                    SleepStage(startTime = formattedStartTime,
                               endTime = formattedEndTime,
                               duration = durationMinutes,
                               stageCode = stageCode,
                               stageName = stageName)
                }
            }
        }
    }

    private fun mapStageToName(stage: Int): String
    {
        return when (stage)
        {
            0    -> "Unknown Stage"
            1    -> "Awake"
            2    -> "Sleeping"
            3    -> "Out of Bed"
            4    -> "Light Sleep"
            5    -> "Deep Sleep"
            6    -> "REM Sleep"
            7    -> "Awake in Bed"
            else -> "Invalid Stage"
        }
    }
}