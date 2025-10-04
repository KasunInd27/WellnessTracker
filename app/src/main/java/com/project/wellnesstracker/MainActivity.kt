package com.project.wellnesstracker

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.project.wellnesstracker.fragments.HabitsFragment
import com.project.wellnesstracker.fragments.MoodJournalFragment
import com.project.wellnesstracker.fragments.SettingsFragment

class MainActivity : AppCompatActivity() {

    private lateinit var bottomNav: BottomNavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        bottomNav = findViewById(R.id.bottom_navigation)

        // Handle intent from widget or notification
        handleIntent()

        // Load default fragment only if not already loaded
        if (savedInstanceState == null) {
            val fragment = when (intent.getStringExtra("navigate_to")) {
                "settings" -> SettingsFragment()
                "mood" -> MoodJournalFragment()
                else -> HabitsFragment()
            }

            supportFragmentManager.beginTransaction()
                .replace(R.id.nav_host_fragment, fragment)
                .commit()

            // Update bottom navigation selection
            when (fragment) {
                is SettingsFragment -> bottomNav.selectedItemId = R.id.nav_settings
                is MoodJournalFragment -> bottomNav.selectedItemId = R.id.nav_mood
                else -> bottomNav.selectedItemId = R.id.nav_habits
            }
        }

        bottomNav.setOnItemSelectedListener { item ->
            val fragment = when (item.itemId) {
                R.id.nav_habits -> HabitsFragment()
                R.id.nav_mood -> MoodJournalFragment()
                R.id.nav_settings -> SettingsFragment()
                else -> return@setOnItemSelectedListener false
            }

            supportFragmentManager.beginTransaction()
                .replace(R.id.nav_host_fragment, fragment)
                .commit()

            true
        }
    }

    override fun onNewIntent(intent: android.content.Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleIntent()
    }

    private fun handleIntent() {
        val navigateTo = intent.getStringExtra("navigate_to")
        val source = intent.getStringExtra("source")

        // Log for debugging
        if (source != null) {
            android.util.Log.d("MainActivity", "Opened from: $source")
        }

        // Navigate to specific fragment if requested
        when (navigateTo) {
            "settings" -> {
                supportFragmentManager.beginTransaction()
                    .replace(R.id.nav_host_fragment, SettingsFragment())
                    .commit()
                bottomNav.selectedItemId = R.id.nav_settings
            }
            "mood" -> {
                supportFragmentManager.beginTransaction()
                    .replace(R.id.nav_host_fragment, MoodJournalFragment())
                    .commit()
                bottomNav.selectedItemId = R.id.nav_mood
            }
        }
    }
}