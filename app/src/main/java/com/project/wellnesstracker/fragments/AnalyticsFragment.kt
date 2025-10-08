package com.project.wellnesstracker.fragments

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CalendarView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.project.wellnesstracker.R
import com.project.wellnesstracker.adapters.MoodAdapter
import com.project.wellnesstracker.models.MoodEntry
import com.project.wellnesstracker.utils.DataManager
import java.text.SimpleDateFormat
import java.util.*

class AnalyticsFragment : Fragment() {

    private lateinit var dataManager: DataManager
    private val allMoods = mutableListOf<MoodEntry>()
    private val filteredMoods = mutableListOf<MoodEntry>()
    private lateinit var moodAdapter: MoodAdapter
    private lateinit var moodChart: LineChart
    private var selectedDate: String = ""

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_analytics, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        dataManager = DataManager(requireContext())
        allMoods.addAll(dataManager.loadMoodEntries())

        // Initialize chart
        moodChart = view.findViewById(R.id.mood_chart)
        setupMoodChart()

        // Initialize calendar
        val calendarView = view.findViewById<CalendarView>(R.id.calendar_view)
        val selectedDateLabel = view.findViewById<TextView>(R.id.selected_date_label)
        val recyclerView = view.findViewById<RecyclerView>(R.id.selected_date_moods)

        moodAdapter = MoodAdapter(filteredMoods)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = moodAdapter

        // Set today's date as default
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        selectedDate = dateFormat.format(Date())
        updateSelectedDateLabel(selectedDateLabel, selectedDate)
        filterMoodsByDate(selectedDate)

        calendarView.setOnDateChangeListener { _, year, month, dayOfMonth ->
            val calendar = Calendar.getInstance()
            calendar.set(year, month, dayOfMonth)
            selectedDate = dateFormat.format(calendar.time)
            updateSelectedDateLabel(selectedDateLabel, selectedDate)
            filterMoodsByDate(selectedDate)
        }
    }

    private fun setupMoodChart() {
        // Get last 7 days of mood data
        val calendar = Calendar.getInstance()
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val displayFormat = SimpleDateFormat("MMM dd", Locale.getDefault())

        val last7Days = mutableListOf<String>()
        val dateLabels = mutableListOf<String>()

        for (i in 6 downTo 0) {
            calendar.add(Calendar.DAY_OF_YEAR, if (i == 6) -6 else 1)
            val dateStr = dateFormat.format(calendar.time)
            val displayStr = displayFormat.format(calendar.time)
            last7Days.add(dateStr)
            dateLabels.add(displayStr)
        }

        // Map emojis to numeric values (for chart)
        val emojiToValue = mapOf(
            "😊" to 5f,
            "😐" to 3f,
            "😢" to 2f,
            "😡" to 1f,
            "😴" to 2.5f
        )

        // Calculate average mood per day
        val entries = mutableListOf<Entry>()
        last7Days.forEachIndexed { index, date ->
            val dayMoods = allMoods.filter { it.getDateOnly() == date }
            if (dayMoods.isNotEmpty()) {
                val avgValue = dayMoods.mapNotNull { emojiToValue[it.emoji] }.average().toFloat()
                entries.add(Entry(index.toFloat(), avgValue))
            } else {
                entries.add(Entry(index.toFloat(), 0f))
            }
        }

        if (entries.isEmpty()) {
            moodChart.clear()
            moodChart.setNoDataText("No mood data yet. Start logging your moods!")
            moodChart.invalidate()
            return
        }

        val dataSet = LineDataSet(entries, "Mood Score").apply {
            color = Color.parseColor("#2196F3")
            setCircleColor(Color.parseColor("#2196F3"))
            lineWidth = 2f
            circleRadius = 4f
            setDrawValues(false)
            mode = LineDataSet.Mode.CUBIC_BEZIER
            setDrawFilled(true)
            fillColor = Color.parseColor("#2196F3")
            fillAlpha = 50
        }

        val lineData = LineData(dataSet)
        moodChart.data = lineData

        // Customize chart
        moodChart.description.isEnabled = false
        moodChart.legend.isEnabled = false
        moodChart.setTouchEnabled(true)
        moodChart.setScaleEnabled(false)
        moodChart.setPinchZoom(false)

        // X-axis
        moodChart.xAxis.apply {
            position = XAxis.XAxisPosition.BOTTOM
            valueFormatter = IndexAxisValueFormatter(dateLabels)
            granularity = 1f
            setDrawGridLines(false)
            textSize = 10f
        }

        // Y-axis
        moodChart.axisLeft.apply {
            axisMinimum = 0f
            axisMaximum = 6f
            granularity = 1f
            setDrawGridLines(true)
            textSize = 10f
        }

        moodChart.axisRight.isEnabled = false

        moodChart.animateX(1000)
        moodChart.invalidate()
    }

    private fun updateSelectedDateLabel(label: TextView, date: String) {
        val displayFormat = SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault())
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        try {
            val parsedDate = dateFormat.parse(date)
            if (parsedDate != null) {
                label.text = "Moods for ${displayFormat.format(parsedDate)}:"
            }
        } catch (e: Exception) {
            label.text = "Moods for selected date:"
        }
    }

    private fun filterMoodsByDate(date: String) {
        filteredMoods.clear()
        filteredMoods.addAll(allMoods.filter { it.getDateOnly() == date })
        moodAdapter.notifyDataSetChanged()
    }

    override fun onResume() {
        super.onResume()
        // Refresh data when fragment becomes visible
        allMoods.clear()
        allMoods.addAll(dataManager.loadMoodEntries())
        setupMoodChart()
        filterMoodsByDate(selectedDate)
    }
}