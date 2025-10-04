package com.project.wellnesstracker.models

data class HydrationSettings(
    var isEnabled: Boolean = false,
    var intervalMinutes: Int = 60,
    var startHour: Int = 8,
    var endHour: Int = 22
)
