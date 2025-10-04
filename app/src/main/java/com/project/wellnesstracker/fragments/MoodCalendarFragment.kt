package com.project.wellnesstracker.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CalendarView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.project.wellnesstracker.R
import com.project.wellnesstracker.adapters.MoodAdapter
import com.project.wellnesstracker.models.MoodEntry
import com.project.wellnesstracker.utils.DataManager
import java.text.SimpleDateFormat
import java.util.*

class MoodCalendarFragment : Fragment() {

    private lateinit var dataManager: DataManager
    private val allMoods = mutableListOf<MoodEntry>()
    private val filteredMoods = mutableListOf<MoodEntry>()
    private lateinit var moodAdapter: MoodAdapter
    private var selectedDate: String = ""

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_mood_calendar, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        dataManager = DataManager(requireContext())
        allMoods.addAll(dataManager.loadMoodEntries())

        val calendarView = view.findViewById<CalendarView>(R.id.calendar_view)
        val recyclerView = view.findViewById<RecyclerView>(R.id.selected_date_moods)
        val switchViewButton = view.findViewById<Button>(R.id.switch_view_button)

        moodAdapter = MoodAdapter(filteredMoods)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = moodAdapter

        // Set today's date as default
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        selectedDate = dateFormat.format(Date())
        filterMoodsByDate(selectedDate)

        calendarView.setOnDateChangeListener { _, year, month, dayOfMonth ->
            val calendar = Calendar.getInstance()
            calendar.set(year, month, dayOfMonth)
            selectedDate = dateFormat.format(calendar.time)
            filterMoodsByDate(selectedDate)
        }

        switchViewButton.setOnClickListener {
            // Switch back to list view
            parentFragmentManager.beginTransaction()
                .replace(R.id.nav_host_fragment, MoodJournalFragment())
                .commit()
        }
    }

    private fun filterMoodsByDate(date: String) {
        filteredMoods.clear()
        filteredMoods.addAll(allMoods.filter { it.getDateOnly() == date })
        moodAdapter.notifyDataSetChanged()
    }
}