package com.example.exacthealth.fragments

import android.content.SharedPreferences
import android.graphics.Color
import android.graphics.DashPathEffect
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.exacthealth.R
import com.example.exacthealth.classes.GsonProvider
import com.example.exacthealth.classes.HealthSharedPreferencesManager
import com.example.exacthealth.models.SleepStage
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.formatter.ValueFormatter
import com.google.gson.reflect.TypeToken
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class HealthFragment: Fragment()
{
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View?
    {
        return inflater.inflate(R.layout.fragment_health, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?)
    {
        super.onViewCreated(view, savedInstanceState)
        val healthSharedPreferencesManager = HealthSharedPreferencesManager(view.context)

        // Calculate yesterday's date
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        val yesterdayDate = LocalDate.now().minusDays(1).format(formatter)

        // Prepare Health Data
        val heartRateData: List<Pair<String, Int>> = healthSharedPreferencesManager.loadHeartRate(date = yesterdayDate)
        val stepCountsData: List<Pair<String, Int>> = healthSharedPreferencesManager.loadStepCounts(date = yesterdayDate)
        val sleepStages: List<SleepStage> = healthSharedPreferencesManager.loadSleepStage(date = yesterdayDate)

        // Display Yesterday Date
        val healthDateView: TextView = view.findViewById(R.id.health_date_view)
        healthDateView.text = yesterdayDate.plus(" (yesterday)")

        // Display Heart Rate Chart
        val heartRateChart: LineChart = view.findViewById(R.id.health_heartrate_lineChart)
        prepareHeartRateChart(heartRateChart, heartRateData)

        // Display Step Count
        val stepCountsView = view.findViewById<TextView>(R.id.health_step_counts_view)
        stepCountsView.text = stepCountsData.firstOrNull()?.second?.toString()?.plus(" steps") ?: "No steps data"

        // Display Sleep Stage Chart
        val sleepChart: LineChart = view.findViewById(R.id.health_sleep_lineChart)
        prepareSleepStageChart(sleepChart, sleepStages)
    }

    private fun prepareHeartRateChart(chart: LineChart, heartRateData: List<Pair<String, Int>>)
    {
        if (heartRateData.isEmpty()) return

        val redColor = ContextCompat.getColor(requireContext(), R.color.green)

        // Prepare data entries
        val entries = heartRateData.mapIndexed {index, pair ->
            Entry(index.toFloat(), pair.second.toFloat())
        }

        // Create dataset
        val dataSet = LineDataSet(entries, "Heart Rate")
        dataSet.color = redColor
        dataSet.setCircleColor(redColor)
        dataSet.circleRadius = 4f
        dataSet.lineWidth = 2f
        dataSet.setDrawValues(false)

        // Configure chart data
        val lineData = LineData(dataSet)
        chart.data = lineData

        // Chart Styling
        chart.apply {
            setDrawGridBackground(false)
            description.isEnabled = false
            legend.isEnabled = false
            axisRight.isEnabled = false
        }

        // X-Axis Styling
        chart.xAxis.apply {
            position = XAxis.XAxisPosition.BOTTOM
            setDrawGridLines(false)
            textColor = Color.GRAY
            labelRotationAngle = -45f
            valueFormatter = IndexAxisValueFormatter(heartRateData.map {it.first.split(" ")[1]})
        }

        // Y-Axis Styling
        chart.axisLeft.apply {
            setDrawGridLines(false)
            textColor = Color.GRAY
        }

        // Refresh chart
        chart.invalidate()
    }

    private fun prepareSleepStageChart(chart: LineChart, sleepStages: List<SleepStage>)
    {
        if (sleepStages.isEmpty()) return

        // Map unique stages to specific Y-axis positions
        val stageMap = mapOf("Deep Sleep" to 0f, "Light Sleep" to 1f, "REM Sleep" to 2f, "Awake" to 3f)

        // Filter out short-duration stages (e.g., <3 minutes)
        val filteredStages = sleepStages.filter {it.duration >= 3}

        // Prepare entries for horizontal lines (time as X, stage as Y)
        val entries = mutableListOf<Entry>()

        filteredStages.forEach {stage ->
            val startHour = stage.startTime.split(" ")[1].split(":")[0].toFloat()
            val startMinute = stage.startTime.split(" ")[1].split(":")[1].toFloat()
            val startTimeInHours = startHour + startMinute / 60

            val endHour = stage.endTime.split(" ")[1].split(":")[0].toFloat()
            val endMinute = stage.endTime.split(" ")[1].split(":")[1].toFloat()
            val endTimeInHours = endHour + endMinute / 60

            val yValue = stageMap[stage.stageName] ?: 0f

            entries.add(Entry(startTimeInHours, yValue))
            entries.add(Entry(endTimeInHours, yValue))
        }

        // Create LineDataSet
        val dataSet = LineDataSet(entries, "Sleep Stages").apply {
            color = ContextCompat.getColor(requireContext(), R.color.green)
            setDrawCircles(false)
            setDrawValues(false)
            lineWidth = 2f
            setDrawFilled(false)
        }

        // Configure dashed Y-axis lines
        chart.axisLeft.apply {
            setDrawGridLines(true)
            setGridDashedLine(DashPathEffect(floatArrayOf(10f, 10f), 0f))
            textColor = Color.GRAY
            granularity = 1f
            valueFormatter = IndexAxisValueFormatter(stageMap.keys.toList())
        }

        // Configure X-axis
        chart.xAxis.apply {
            position = XAxis.XAxisPosition.BOTTOM
            setDrawGridLines(false)
            textColor = Color.GRAY
            valueFormatter = TimeValueFormatter()
            labelRotationAngle = -45f
        }

        // Configure chart appearance
        chart.apply {
            description.isEnabled = false
            legend.isEnabled = false
            axisRight.isEnabled = false
            setDrawBorders(false)
            setDrawGridBackground(false)
        }

        // Set data to chart
        chart.data = LineData(dataSet)
        chart.invalidate()
    }

    private inline fun <reified T> parseHealthJsonData(json: String): T?
    {
        if (json.isEmpty()) return null
        val type = object: TypeToken<T>()
        {}.type
        return GsonProvider.gson.fromJson<T>(json, type)
    }

    class TimeValueFormatter: ValueFormatter()
    {
        override fun getFormattedValue(value: Float): String
        {
            val hour = value.toInt()
            val minute = ((value - hour) * 60).toInt()
            return String.format("%02d:%02d", hour, minute)
        }
    }
}
