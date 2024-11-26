package com.example.exacthealth.classes

import android.content.Context
import com.example.exacthealth.models.SleepStage
import com.google.gson.reflect.TypeToken
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class HealthSharedPreferencesManager(private val context: Context)
{
    private val sharedPreferences = context.getSharedPreferences("smartwatch_data", Context.MODE_PRIVATE)
    private val serverRequestHandler: ServerRequestHandler = ServerRequestHandler(context)

    // Default value with current date and time
    private val yesterdayDateTime = LocalDateTime.now().minusDays(1).format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))
    private val defaultValue = listOf(yesterdayDateTime to 0)
    private val defaultSleepStage =
        listOf(SleepStage(startTime = yesterdayDateTime, endTime = yesterdayDateTime, duration = 0L, stageCode = 0, stageName = "Unknown Stage"))

    fun saveHeartRate(heartRateData: List<Pair<String, Int>>)
    {
        saveDataToSharedPreferences("heartRateData", heartRateData)

        val jsonHeartRateData = sharedPreferences.getString("heartRateData", "") ?: ""
        val date = heartRateData[0].first.split(" ")[0]

        serverRequestHandler.sendHeartRateData(date = "2024-11-20", jsonHeartRateData = jsonHeartRateData, context = context)
    }

    fun saveStepCounts(stepCountsData: List<Pair<String, Int>>)
    {
        saveDataToSharedPreferences("stepCountsData", stepCountsData)
        val jsonStepCountsData = sharedPreferences.getString("stepCountsData", "") ?: ""
        val date = stepCountsData[0].first.split(" ")[0]

        serverRequestHandler.sendStepCountData(date = "2024-11-20", jsonStepCountsData = jsonStepCountsData, context = context)
    }

    fun saveSleepStage(sleepStageData: List<SleepStage>)
    {
        saveDataToSharedPreferences("sleepStageData", sleepStageData)
        val jsonSleepStageData = sharedPreferences.getString("sleepStageData", "") ?: ""
        val date = sleepStageData[0].startTime.split(" ")[0]

        serverRequestHandler.sendSleepStageData(date = "2024-11-20", jsonSleepStageData = jsonSleepStageData, context = context)
    }

    fun loadHeartRate(date: String): List<Pair<String, Int>>
    {
        val heartRateDataJson = serverRequestHandler.getHeartRateFromDate(date = "2024-11-20", context = context)
        return if (heartRateDataJson.isEmpty()) defaultValue
        else
        {
            val heartRateData: List<Pair<String, Int>> = parseHealthJsonData<List<Pair<String, Int>>>(heartRateDataJson)
            heartRateData
        }
    }

    fun loadStepCounts(date: String): List<Pair<String, Int>>
    {
        val stepCountsDataJson = serverRequestHandler.getStepCountsFromDate(date = "2024-11-20", context = context)
        return if (stepCountsDataJson.isEmpty()) defaultValue
        else
        {
            val stepCountsData: List<Pair<String, Int>> = parseHealthJsonData<List<Pair<String, Int>>>(stepCountsDataJson)
            stepCountsData
        }
    }

    fun loadSleepStage(date: String): List<SleepStage>
    {
        val sleepStageDataJson = serverRequestHandler.getSleepStageFromDate(date = "2024-11-20", context = context)
        return if (sleepStageDataJson.isEmpty()) defaultSleepStage
        else
        {
            val sleepStageData: List<SleepStage> = parseHealthJsonData<List<SleepStage>>(sleepStageDataJson)
            sleepStageData
        }
    }

    private fun <T> saveDataToSharedPreferences(key: String, data: T)
    {
        val editor = sharedPreferences.edit()
        val json = GsonProvider.gson.toJson(data)
        editor.putString(key, json)
        editor.apply()
    }

    private inline fun <reified T> parseHealthJsonData(json: String): T
    {
        val type = object: TypeToken<T>()
        {}.type
        return GsonProvider.gson.fromJson<T>(json, type)
    }
}