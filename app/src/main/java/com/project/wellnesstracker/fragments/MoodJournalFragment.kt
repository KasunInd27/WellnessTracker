package com.project.wellnesstracker.fragments

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
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

class MoodJournalFragment : Fragment() {

    private lateinit var dataManager: DataManager
    private lateinit var moodAdapter: MoodAdapter
    private val moods = mutableListOf<MoodEntry>()
    private var selectedEmoji: String? = null
    private lateinit var moodChart: LineChart

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_mood_journal, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        dataManager = DataManager(requireContext())
        moods.addAll(dataManager.loadMoodEntries())
        moods.sortByDescending { it.timestamp }

        // Initialize chart
        moodChart = view.findViewById(R.id.mood_chart)
        setupMoodChart()

        val recyclerView = view.findViewById<RecyclerView>(R.id.mood_recycler)
        moodAdapter = MoodAdapter(moods)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = moodAdapter

        // Emoji selection
        val emojis = listOf("😊", "😐", "😢", "😡", "😴")
        val emojiViews = listOf(
            view.findViewById<TextView>(R.id.emoji_1),
            view.findViewById<TextView>(R.id.emoji_2),
            view.findViewById<TextView>(R.id.emoji_3),
            view.findViewById<TextView>(R.id.emoji_4),
            view.findViewById<TextView>(R.id.emoji_5)
        )

        emojiViews.forEachIndexed { index, emojiView ->
            emojiView.setOnClickListener {
                selectedEmoji = emojis[index]
                emojiViews.forEach { it.alpha = 0.5f }
                emojiView.alpha = 1.0f
            }
        }

        val noteInput = view.findViewById<EditText>(R.id.note_input)
        // Calendar view toggle
        view.findViewById<Button>(R.id.calendar_view_button).setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.nav_host_fragment, MoodCalendarFragment())
                .commit()
        }

        view.findViewById<Button>(R.id.save_mood_button).setOnClickListener {
            if (selectedEmoji != null) {
                val mood = MoodEntry(
                    emoji = selectedEmoji!!,
                    note = noteInput.text.toString()
                )
                moods.add(0, mood)
                moodAdapter.notifyItemInserted(0)
                recyclerView.scrollToPosition(0)
                dataManager.saveMoodEntries(moods)

                // Update chart
                setupMoodChart()

                noteInput.text.clear()
                emojiViews.forEach { it.alpha = 1.0f }
                selectedEmoji = null

                Toast.makeText(requireContext(), "Mood saved!", Toast.LENGTH_SHORT).show()

                // Share functionality
                shareMoodSummary()
            } else {
                Toast.makeText(requireContext(), "Please select an emoji", Toast.LENGTH_SHORT).show()
            }
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
            val dayMoods = moods.filter { it.getDateOnly() == date }
            if (dayMoods.isNotEmpty()) {
                val avgValue = dayMoods.mapNotNull { emojiToValue[it.emoji] }.average().toFloat()
                entries.add(Entry(index.toFloat(), avgValue))
            } else {
                // Add 0 for days with no mood entries
                entries.add(Entry(index.toFloat(), 0f))
            }
        }

        if (entries.isEmpty()) {
            // Show empty chart
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

    private fun shareMoodSummary() {
        val recentMoods = moods.take(5)
        val summary = buildString {
            append("My Recent Moods:\n\n")
            recentMoods.forEach {
                append("${it.emoji} ${it.getFormattedDate()}\n")
                if (it.note.isNotEmpty()) append("  ${it.note}\n")
            }
        }

        val shareIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, summary)
            type = "text/plain"
        }

        // Optional: Uncomment to auto-share
        // startActivity(Intent.createChooser(shareIntent, "Share Mood Summary"))
    }
}