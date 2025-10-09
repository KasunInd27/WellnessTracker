package com.project.wellnesstracker.models

data class HabitReminderSettings(
    var isEnabled: Boolean = false,
    var intervalMinutes: Int = 120, // Default 2 hours
    var startHour: Int = 8,
    var endHour: Int = 22
)