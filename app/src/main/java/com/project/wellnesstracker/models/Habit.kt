package com.project.wellnesstracker.models

import java.util.UUID
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.Date

data class Habit(
    val id: String = UUID.randomUUID().toString(),
    var name: String = "",
    var description: String = "",
    var icon: String = "📝", // Default icon
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

    fun isWaterRelated(): Boolean {
        val waterKeywords = listOf("water", "hydrat", "drink", "beverage", "fluid", "💧")
        // Use safe calls (?.) and default to empty string ("") to prevent NullPointerException
        val safeName = name ?: ""
        val safeDescription = description ?: ""
        val safeIcon = icon ?: ""

        return waterKeywords.any {
            safeName.contains(it, ignoreCase = true) ||
                    safeDescription.contains(it, ignoreCase = true) ||
                    safeIcon.contains(it, ignoreCase = true)
        }
    }

    private fun getCurrentDateString(): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return sdf.format(Date())
    }

    companion object {
        fun getIconForHabit(habitName: String?): String {
            val name = habitName?.lowercase() ?: ""
            return when {
                name.contains("water") || name.contains("hydrat") || name.contains("drink") -> "💧"
                name.contains("exercise") || name.contains("workout") || name.contains("gym") -> "💪"
                name.contains("sleep") || name.contains("rest") || name.contains("bed") -> "😴"
                name.contains("read") || name.contains("book") -> "📚"
                name.contains("meditat") || name.contains("mindful") -> "🧘"
                name.contains("walk") || name.contains("run") || name.contains("jog") -> "🏃"
                name.contains("eat") || name.contains("meal") || name.contains("food") -> "🍎"
                name.contains("vitamin") || name.contains("supplement") -> "💊"
                name.contains("stretch") || name.contains("yoga") -> "🤸"
                name.contains("journal") || name.contains("write") -> "✍️"
                name.contains("learn") || name.contains("study") -> "🎓"
                name.contains("music") || name.contains("practice") -> "🎵"
                name.contains("clean") || name.contains("tidy") -> "🧹"
                name.contains("cook") || name.contains("prepare") -> "👨‍🍳"
                name.contains("call") || name.contains("contact") || name.contains("family") -> "📞"
                name.contains("pray") || name.contains("spiritual") -> "🙏"
                name.contains("garden") || name.contains("plant") -> "🌱"
                name.contains("bike") || name.contains("cycle") -> "🚴"
                name.contains("swim") -> "🏊"
                name.contains("art") || name.contains("draw") || name.contains("paint") -> "🎨"
                else -> "📝"
            }
        }
    }
}
