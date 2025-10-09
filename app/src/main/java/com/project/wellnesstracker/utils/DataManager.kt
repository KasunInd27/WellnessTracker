// utils/DataManager.kt

package com.project.wellnesstracker.utils

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.project.wellnesstracker.models.Habit
import com.project.wellnesstracker.models.HydrationSettings
import com.project.wellnesstracker.models.HabitReminderSettings
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

    // Habit Reminder Settings
    fun saveHabitReminderSettings(settings: HabitReminderSettings) {
        val json = gson.toJson(settings)
        prefs.edit().putString("habit_reminder_settings", json).apply()
    }

    fun loadHabitReminderSettings(): HabitReminderSettings {
        val json = prefs.getString("habit_reminder_settings", null)
        return if (json != null) {
            gson.fromJson(json, HabitReminderSettings::class.java)
        } else {
            HabitReminderSettings()
        }
    }

    // Today's completion percentage for widget
    fun getTodayCompletionPercentage(): Int {
        val habits = loadHabits()
        if (habits.isEmpty()) return 0

        val completedCount = habits.count { it.isCompletedToday() }
        return (completedCount * 100) / habits.size
    }

    // Get water-related habits
    fun getWaterRelatedHabits(): List<Habit> {
        return loadHabits().filter { it.isWaterRelated() }
    }

    // Get non-water habits
    fun getNonWaterHabits(): List<Habit> {
        return loadHabits().filter { !it.isWaterRelated() }
    }

    // Check if there are any incomplete water habits today
    fun hasIncompleteWaterHabits(): Boolean {
        return getWaterRelatedHabits().any { !it.isCompletedToday() }
    }

    // Check if there are any incomplete non-water habits today
    fun hasIncompleteNonWaterHabits(): Boolean {
        return getNonWaterHabits().any { !it.isCompletedToday() }
    }
}