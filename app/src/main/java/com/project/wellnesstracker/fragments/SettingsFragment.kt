// fragments/SettingsFragment.kt

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
import com.google.android.material.chip.Chip
import com.project.wellnesstracker.R
import com.project.wellnesstracker.models.HabitReminderSettings
import com.project.wellnesstracker.models.HydrationSettings
import com.project.wellnesstracker.utils.DataManager
import com.project.wellnesstracker.workers.HabitReminderWorker
import com.project.wellnesstracker.workers.HydrationReminderWorker
import java.util.concurrent.TimeUnit

class SettingsFragment : Fragment() {

    private lateinit var dataManager: DataManager
    private lateinit var hydrationSettings: HydrationSettings
    private lateinit var habitReminderSettings: HabitReminderSettings

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
        hydrationSettings = dataManager.loadHydrationSettings()
        habitReminderSettings = dataManager.loadHabitReminderSettings()

        // Hydration reminder controls
        val hydrationSwitch = view.findViewById<SwitchCompat>(R.id.hydration_switch)
        val hydrationIntervalInput = view.findViewById<EditText>(R.id.hydration_interval_input)

        // Habit reminder controls
        val habitReminderSwitch = view.findViewById<SwitchCompat>(R.id.habit_reminder_switch)
        val habitIntervalInput = view.findViewById<EditText>(R.id.habit_interval_input)

        val saveButton = view.findViewById<Button>(R.id.save_settings_button)

        // Set initial values
        hydrationSwitch.isChecked = hydrationSettings.isEnabled
        hydrationIntervalInput.setText(hydrationSettings.intervalMinutes.toString())

        habitReminderSwitch.isChecked = habitReminderSettings.isEnabled
        habitIntervalInput.setText(habitReminderSettings.intervalMinutes.toString())

        // Setup hydration chips
        setupHydrationChips(view, hydrationIntervalInput)

        // Setup habit reminder chips
        setupHabitChips(view, habitIntervalInput)

        saveButton.setOnClickListener {
            val hydrationInterval = hydrationIntervalInput.text.toString().toIntOrNull() ?: 60
            val habitInterval = habitIntervalInput.text.toString().toIntOrNull() ?: 120

            // Validate minimum intervals
            if (hydrationInterval < 1 || habitInterval < 1) {
                Toast.makeText(requireContext(), "Minimum interval is 1 minute", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Save hydration settings
            hydrationSettings.isEnabled = hydrationSwitch.isChecked
            hydrationSettings.intervalMinutes = hydrationInterval
            dataManager.saveHydrationSettings(hydrationSettings)

            // Save habit reminder settings
            habitReminderSettings.isEnabled = habitReminderSwitch.isChecked
            habitReminderSettings.intervalMinutes = habitInterval
            dataManager.saveHabitReminderSettings(habitReminderSettings)

            // Schedule or cancel hydration reminders
            if (hydrationSettings.isEnabled && dataManager.hasIncompleteWaterHabits()) {
                scheduleHydrationReminders(hydrationInterval)
            } else {
                cancelHydrationReminders()
            }

            // Schedule or cancel habit reminders
            if (habitReminderSettings.isEnabled && dataManager.hasIncompleteNonWaterHabits()) {
                scheduleHabitReminders(habitInterval)
            } else {
                cancelHabitReminders()
            }

            Toast.makeText(requireContext(), "Settings saved!", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupHydrationChips(view: View, intervalInput: EditText) {
        view.findViewById<Chip>(R.id.hydration_chip_30).setOnClickListener {
            intervalInput.setText("30")
        }
        view.findViewById<Chip>(R.id.hydration_chip_60).setOnClickListener {
            intervalInput.setText("60")
        }
        view.findViewById<Chip>(R.id.hydration_chip_90).setOnClickListener {
            intervalInput.setText("90")
        }
        view.findViewById<Chip>(R.id.hydration_chip_120).setOnClickListener {
            intervalInput.setText("120")
        }
    }

    private fun setupHabitChips(view: View, intervalInput: EditText) {
        view.findViewById<Chip>(R.id.habit_chip_60).setOnClickListener {
            intervalInput.setText("60")
        }
        view.findViewById<Chip>(R.id.habit_chip_120).setOnClickListener {
            intervalInput.setText("120")
        }
        view.findViewById<Chip>(R.id.habit_chip_180).setOnClickListener {
            intervalInput.setText("180")
        }
        view.findViewById<Chip>(R.id.habit_chip_240).setOnClickListener {
            intervalInput.setText("240")
        }
    }

    private fun scheduleHydrationReminders(intervalMinutes: Int) {
        if (intervalMinutes < 1) {
            Toast.makeText(requireContext(), "Minimum interval is 1 minute", Toast.LENGTH_SHORT).show()
            return
        }

        cancelHydrationReminders()

        val workRequest = androidx.work.OneTimeWorkRequestBuilder<HydrationReminderWorker>()
            .setInitialDelay(intervalMinutes.toLong(), TimeUnit.MINUTES)
            .addTag("HydrationReminder")
            .build()

        WorkManager.getInstance(requireContext()).enqueue(workRequest)

        WorkManager.getInstance(requireContext())
            .getWorkInfoByIdLiveData(workRequest.id)
            .observe(viewLifecycleOwner) { workInfo ->
                if (workInfo?.state?.isFinished == true && hydrationSettings.isEnabled) {
                    scheduleHydrationReminders(intervalMinutes)
                }
            }
    }

    private fun cancelHydrationReminders() {
        WorkManager.getInstance(requireContext()).cancelAllWorkByTag("HydrationReminder")
    }

    private fun scheduleHabitReminders(intervalMinutes: Int) {
        if (intervalMinutes < 1) {
            Toast.makeText(requireContext(), "Minimum interval is 1 minute", Toast.LENGTH_SHORT).show()
            return
        }

        cancelHabitReminders()

        val workRequest = androidx.work.OneTimeWorkRequestBuilder<HabitReminderWorker>()
            .setInitialDelay(intervalMinutes.toLong(), TimeUnit.MINUTES)
            .addTag("HabitReminder")
            .build()

        WorkManager.getInstance(requireContext()).enqueue(workRequest)

        WorkManager.getInstance(requireContext())
            .getWorkInfoByIdLiveData(workRequest.id)
            .observe(viewLifecycleOwner) { workInfo ->
                if (workInfo?.state?.isFinished == true && habitReminderSettings.isEnabled) {
                    scheduleHabitReminders(intervalMinutes)
                }
            }
    }

    private fun cancelHabitReminders() {
        WorkManager.getInstance(requireContext()).cancelAllWorkByTag("HabitReminder")
    }
}