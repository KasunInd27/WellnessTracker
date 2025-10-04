package com.project.wellnesstracker.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.widget.SwitchCompat
import androidx.fragment.app.Fragment
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.project.wellnesstracker.R
import com.project.wellnesstracker.models.HydrationSettings
import com.project.wellnesstracker.utils.DataManager
import com.project.wellnesstracker.workers.HydrationReminderWorker
import java.util.concurrent.TimeUnit

class SettingsFragment : Fragment() {

    private lateinit var dataManager: DataManager
    private lateinit var settings: HydrationSettings

    companion object {
        private const val MIN_INTERVAL_MINUTES = 15 // WorkManager minimum
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_settings, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        dataManager = DataManager(requireContext())
        settings = dataManager.loadHydrationSettings()

        val hydrationSwitch = view.findViewById<SwitchCompat>(R.id.hydration_switch)
        val intervalInput = view.findViewById<EditText>(R.id.interval_input)
        val saveButton = view.findViewById<Button>(R.id.save_settings_button)

        hydrationSwitch.isChecked = settings.isEnabled
        intervalInput.setText(settings.intervalMinutes.toString())

        // Add hint about minimum interval
        intervalInput.hint = "Minimum: $MIN_INTERVAL_MINUTES minutes"

        saveButton.setOnClickListener {
            val inputInterval = intervalInput.text.toString().toIntOrNull()

            // Validate input
            if (inputInterval == null || inputInterval <= 0) {
                Toast.makeText(
                    requireContext(),
                    "Please enter a valid interval",
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }

            // Enforce minimum and warn user if adjusted
            val actualInterval = if (inputInterval < MIN_INTERVAL_MINUTES) {
                Toast.makeText(
                    requireContext(),
                    "Minimum interval is $MIN_INTERVAL_MINUTES minutes. Using $MIN_INTERVAL_MINUTES.",
                    Toast.LENGTH_LONG
                ).show()
                MIN_INTERVAL_MINUTES
            } else {
                inputInterval
            }

            settings.isEnabled = hydrationSwitch.isChecked
            settings.intervalMinutes = actualInterval

            dataManager.saveHydrationSettings(settings)

            if (settings.isEnabled) {
                scheduleHydrationReminders(actualInterval)
                Toast.makeText(
                    requireContext(),
                    "Reminders enabled! Next reminder in $actualInterval minutes",
                    Toast.LENGTH_LONG
                ).show()
            } else {
                cancelHydrationReminders()
                Toast.makeText(
                    requireContext(),
                    "Reminders disabled",
                    Toast.LENGTH_SHORT
                ).show()
            }

            // Update the display to show actual interval
            intervalInput.setText(actualInterval.toString())
        }
    }

    private fun scheduleHydrationReminders(intervalMinutes: Int) {
        val workRequest = PeriodicWorkRequestBuilder<HydrationReminderWorker>(
            intervalMinutes.toLong(),
            TimeUnit.MINUTES
        )
            .addTag("hydration_reminder") // For easier debugging
            .build()

        WorkManager.getInstance(requireContext()).enqueueUniquePeriodicWork(
            "HydrationReminder",
            ExistingPeriodicWorkPolicy.UPDATE,
            workRequest
        )
    }

    private fun cancelHydrationReminders() {
        WorkManager.getInstance(requireContext()).cancelUniqueWork("HydrationReminder")
    }
}