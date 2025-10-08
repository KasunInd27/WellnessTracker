package com.project.wellnesstracker.fragments

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.res.Configuration
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.EditText
import android.widget.LinearLayout
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton

import com.project.wellnesstracker.R
import com.project.wellnesstracker.adapters.HabitAdapter
import com.project.wellnesstracker.models.Habit
import com.project.wellnesstracker.utils.DataManager
import com.project.wellnesstracker.utils.WidgetUpdateHelper

class HabitsFragment : Fragment() {

    private lateinit var dataManager: DataManager
    private lateinit var habitAdapter: HabitAdapter
    private val habits = mutableListOf<Habit>()
    private lateinit var recyclerView: RecyclerView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_habits, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recyclerView = view.findViewById(R.id.habits_recycler)

        // Animate RecyclerView items
        val layoutAnimationController = AnimationUtils.loadLayoutAnimation(
            requireContext(),
            R.anim.layout_animation_fall_down
        )
        recyclerView.layoutAnimation = layoutAnimationController

        // FAB animation
        val fabAnimation = AnimationUtils.loadAnimation(requireContext(), R.anim.scale_up)
        val addHabitButton = view.findViewById<ExtendedFloatingActionButton>(R.id.add_habit_button)

        addHabitButton.startAnimation(fabAnimation)


        dataManager = DataManager(requireContext())
        habits.addAll(dataManager.loadHabits())


        habitAdapter = HabitAdapter(
            habits,
            onHabitToggled = { saveHabits() },
            onHabitDeleted = { habit -> deleteHabit(habit) },
            onHabitClicked = { habit -> editHabit(habit) }
        )

        // Set layout manager based on screen size and orientation
        setupLayoutManager()

        recyclerView.adapter = habitAdapter

        view.findViewById<ExtendedFloatingActionButton
                >(R.id.add_habit_button).setOnClickListener {
            showAddHabitDialog()
        }
    }

    private fun setupLayoutManager() {
        val isTablet = resources.configuration.smallestScreenWidthDp >= 600
        val isLandscape = resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

        recyclerView.layoutManager = when {
            isTablet -> GridLayoutManager(requireContext(), 2) // 2 columns for tablets
            isLandscape -> GridLayoutManager(requireContext(), 2) // 2 columns for landscape
            else -> LinearLayoutManager(requireContext()) // 1 column for portrait phone
        }
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        // Update layout when orientation changes
        setupLayoutManager()
    }

    private fun showAddHabitDialog() {
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Add New Habit")

        val layout = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(50, 20, 50, 20)
        }

        val nameInput = EditText(requireContext()).apply {
            hint = "Habit name (e.g., Drink water)"
        }
        val descInput = EditText(requireContext()).apply {
            hint = "Description (optional)"
        }

        layout.addView(nameInput)
        layout.addView(descInput)
        builder.setView(layout)

        builder.setPositiveButton("Add") { _, _ ->
            val name = nameInput.text.toString()
            if (name.isNotBlank()) {
                val habit = Habit(name = name, description = descInput.text.toString())
                habits.add(habit)
                habitAdapter.notifyItemInserted(habits.size - 1)
                saveHabits()
            }
        }

        builder.setNegativeButton("Cancel", null)
        builder.show()
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun editHabit(habit: Habit) {
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Edit Habit")

        val layout = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(50, 20, 50, 20)
        }

        val nameInput = EditText(requireContext()).apply {
            setText(habit.name)
            hint = "Habit name"
        }
        val descInput = EditText(requireContext()).apply {
            setText(habit.description)
            hint = "Description"
        }

        layout.addView(nameInput)
        layout.addView(descInput)
        builder.setView(layout)

        builder.setPositiveButton("Save") { _, _ ->
            habit.name = nameInput.text.toString()
            habit.description = descInput.text.toString()
            habitAdapter.notifyDataSetChanged()
            saveHabits()
        }

        builder.setNegativeButton("Cancel", null)
        builder.show()
    }

    private fun deleteHabit(habit: Habit) {
        val position = habits.indexOf(habit)
        habits.remove(habit)
        habitAdapter.notifyItemRemoved(position)
        saveHabits()
    }

    private fun saveHabits() {
        dataManager.saveHabits(habits)
        // Update widget when habits change
        WidgetUpdateHelper.updateWidget(requireContext())
    }
}