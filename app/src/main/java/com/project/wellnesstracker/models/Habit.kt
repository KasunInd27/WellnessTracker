package com.project.wellnesstracker.models

import java.util.UUID

data class Habit(
    val id: String = UUID.randomUUID().toString(),
    var name: String,
    var description: String = "",
    val createdDate: Long = System.currentTimeMillis(),
    var completedDates: MutableSet<String> = mutableSetOf() // Format: "yyyy-MM-dd"
) {
    fun isCompletedToday(): Boolean {
        val today = getCurrentDateString()
        return completedDates.contains(today)
    }

    fun toggleCompletion() {
        val today = getCurrentDateString()
        if (completedDates.contains(today)) {
            completedDates.remove(today)
        } else {
            completedDates.add(today)
        }
    }

    fun getTodayProgress(): Int {
        return if (isCompletedToday()) 100 else 0
    }

    private fun getCurrentDateString(): String {
        val sdf = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
        return sdf.format(java.util.Date())
    }
}
