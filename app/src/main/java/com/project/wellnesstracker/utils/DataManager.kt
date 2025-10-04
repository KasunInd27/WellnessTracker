package com.project.wellnesstracker.utils

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.project.wellnesstracker.models.Habit
import com.project.wellnesstracker.models.HydrationSettings
import com.project.wellnesstracker.models.MoodEntry

class DataManager(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("WellnessTrackerPrefs", Context.MODE_PRIVATE)
    private val gson = Gson()

    // Habits Management
    fun saveHabits(habits: List<Habit>) {
        val json = gson.toJson(habits)
        prefs.edit().putString("habits", json).apply()
    }

    fun loadHabits(): MutableList<Habit> {
        val json = prefs.getString("habits", null) ?: return mutableListOf()
        val type = object : TypeToken<MutableList<Habit>>() {}.type
        return gson.fromJson(json, type) ?: mutableListOf()
    }

    // Mood Entries Management
    fun saveMoodEntries(moods: List<MoodEntry>) {
        val json = gson.toJson(moods)
        prefs.edit().putString("moods", json).apply()
    }

    fun loadMoodEntries(): MutableList<MoodEntry> {
        val json = prefs.getString("moods", null) ?: return mutableListOf()
        val type = object : TypeToken<MutableList<MoodEntry>>() {}.type
        return gson.fromJson(json, type) ?: mutableListOf()
    }

    // Hydration Settings
    fun saveHydrationSettings(settings: HydrationSettings) {
        val json = gson.toJson(settings)
        prefs.edit().putString("hydration_settings", json).apply()
    }

    fun loadHydrationSettings(): HydrationSettings {
        val json = prefs.getString("hydration_settings", null)
        return if (json != null) {
            gson.fromJson(json, HydrationSettings::class.java)
        } else {
            HydrationSettings()
        }
    }

    // Today's completion percentage for widget
    fun getTodayCompletionPercentage(): Int {
        val habits = loadHabits()
        if (habits.isEmpty()) return 0

        val completedCount = habits.count { it.isCompletedToday() }
        return (completedCount * 100) / habits.size
    }
}
