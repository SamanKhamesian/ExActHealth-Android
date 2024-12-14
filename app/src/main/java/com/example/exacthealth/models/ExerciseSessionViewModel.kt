package com.example.exacthealth.models

import android.app.Application
import android.util.Log
import androidx.health.connect.client.permission.HealthPermission
import androidx.health.connect.client.records.ActiveCaloriesBurnedRecord
import androidx.health.connect.client.records.ExerciseSessionRecord
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
import kotlin.math.ceil

// Data class to represent formatted exercise session details
data class ExerciseSessionDetails(val startTime: String, val endTime: String, val duration: Long, val exerciseType: String)

class ExerciseSessionViewModel(application: Application): HealthDataViewModel(application)
{
    init
    {
        // Add read permission for exercise sessions
        permissions =
            setOf(HealthPermission.getReadPermission(ExerciseSessionRecord::class), HealthPermission.getReadPermission(ActiveCaloriesBurnedRecord::class))
    }

    // LiveData to hold exercise session records
    private val _exerciseSessions = MutableLiveData<List<ExerciseSessionRecord>>()
    val exerciseSessions: LiveData<List<ExerciseSessionRecord>> = _exerciseSessions

    private val yesterdayDateTime: String = LocalDateTime.now().minusDays(1).format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))
    private val defaultExerciseSession =
        listOf(ExerciseSessionDetails(exerciseType = "None", startTime = yesterdayDateTime, endTime = yesterdayDateTime, duration = 0))

    override fun readData()
    {
        val zoneId = ZoneId.systemDefault()

        val startTime = LocalDate.now(zoneId).minusDays(1).atStartOfDay(zoneId).toInstant()
        val endTime = LocalDate.now(zoneId).atStartOfDay(zoneId).toInstant()

        val readRequest = ReadRecordsRequest(recordType = ExerciseSessionRecord::class, timeRangeFilter = TimeRangeFilter.between(startTime, endTime))

        viewModelScope.launch {
            try
            {
                val response = healthConnectClient.readRecords(readRequest)

                if (response.records.isEmpty())
                {
                    _exerciseSessions.postValue(emptyList())
                }
                else
                {
                    _exerciseSessions.postValue(response.records)
                }
            }
            catch (e: Exception)
            {
                Log.d("ERROR IN READING EXERCISE SESSION RECORDS: ", " " + e.localizedMessage)
                _exerciseSessions.postValue(emptyList())
            }
        }
    }

    // Helper function to format exercise session records
    fun formatExerciseSessionRecords(sessions: List<ExerciseSessionRecord>): List<ExerciseSessionDetails>
    {
        if (sessions.isEmpty())
        {
            return defaultExerciseSession
        }
        else
        {
            return sessions.map {record ->
                val startTimeWithOffset = ZonedDateTime.ofInstant(record.startTime, record.startZoneOffset)
                val endTimeWithOffset = ZonedDateTime.ofInstant(record.endTime, record.endZoneOffset)

                val formattedStartTime = startTimeWithOffset.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))
                val formattedEndTime = endTimeWithOffset.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))

                ExerciseSessionDetails(exerciseType = getExerciseTypeName(record.exerciseType),
                                       startTime = formattedStartTime,
                                       endTime = formattedEndTime,
                                       duration = calculateDuration(record))
            }
        }
    }

    // Helper function to get human-readable exercise type
    private fun getExerciseTypeName(exerciseType: Int): String = when (exerciseType)
    {
        ExerciseSessionRecord.EXERCISE_TYPE_WALKING       -> "Walking"
        ExerciseSessionRecord.EXERCISE_TYPE_RUNNING       -> "Running"
        ExerciseSessionRecord.EXERCISE_TYPE_BIKING        -> "Biking"
        ExerciseSessionRecord.EXERCISE_TYPE_SWIMMING_POOL -> "Swimming"
        ExerciseSessionRecord.EXERCISE_TYPE_WEIGHTLIFTING -> "Weightlifting"
        ExerciseSessionRecord.EXERCISE_TYPE_YOGA          -> "Yoga"
        else                                              -> "Other Exercise"
    }

    // Calculate duration in minutes
    private fun calculateDuration(record: ExerciseSessionRecord): Long
    {
        val durationMinutes = java.time.Duration.between(record.startTime, record.endTime).toMinutes()
        return durationMinutes
    }
}