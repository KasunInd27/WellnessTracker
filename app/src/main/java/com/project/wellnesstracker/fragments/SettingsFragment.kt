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
import androidx.work.WorkManager
import com.project.wellnesstracker.R
import com.project.wellnesstracker.models.HydrationSettings
import com.project.wellnesstracker.utils.DataManager
import com.project.wellnesstracker.workers.HydrationReminderWorker
import java.util.concurrent.TimeUnit

class SettingsFragment : Fragment() {

    private lateinit var dataManager: DataManager
    private lateinit var settings: HydrationSettings

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

        saveButton.setOnClickListener {
            val interval = intervalInput.text.toString().toIntOrNull() ?: 60

            // Validate minimum interval
            if (interval < 1) {
                Toast.makeText(requireContext(), "Minimum interval is 1 minute", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            settings.isEnabled = hydrationSwitch.isChecked
            settings.intervalMinutes = interval

            dataManager.saveHydrationSettings(settings)

            if (settings.isEnabled) {
                scheduleHydrationReminders(interval)
                Toast.makeText(requireContext(), "Reminders enabled!", Toast.LENGTH_SHORT).show()
            } else {
                cancelHydrationReminders()
                Toast.makeText(requireContext(), "Reminders disabled", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun scheduleHydrationReminders(intervalMinutes: Int) {
        if (intervalMinutes < 1) {
            Toast.makeText(requireContext(), "Minimum interval is 1 minute", Toast.LENGTH_SHORT).show()
            return
        }

        // Cancel any existing work first
        cancelHydrationReminders()

        val workRequest = androidx.work.OneTimeWorkRequestBuilder<HydrationReminderWorker>()
            .setInitialDelay(intervalMinutes.toLong(), TimeUnit.MINUTES)
            .addTag("HydrationReminder")
            .build()

        WorkManager.getInstance(requireContext()).enqueue(workRequest)

        // Set up the next reminder when this one completes
        WorkManager.getInstance(requireContext())
            .getWorkInfoByIdLiveData(workRequest.id)
            .observe(viewLifecycleOwner) { workInfo ->
                if (workInfo?.state?.isFinished == true && settings.isEnabled) {
                    // Reschedule the next reminder
                    scheduleHydrationReminders(intervalMinutes)
                }
            }
    }

    private fun cancelHydrationReminders() {
        WorkManager.getInstance(requireContext()).cancelAllWorkByTag("HydrationReminder")
    }
}