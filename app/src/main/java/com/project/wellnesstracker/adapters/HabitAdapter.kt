// adapters/HabitAdapter.kt

package com.project.wellnesstracker.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageButton
import android.widget.ProgressBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.project.wellnesstracker.R
import com.project.wellnesstracker.models.Habit

class HabitAdapter(
    private val habits: MutableList<Habit>,
    private val onHabitToggled: (Habit) -> Unit,
    private val onHabitDeleted: (Habit) -> Unit,
    private val onHabitClicked: (Habit) -> Unit
) : RecyclerView.Adapter<HabitAdapter.HabitViewHolder>() {

    inner class HabitViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val checkbox: CheckBox = view.findViewById(R.id.habit_checkbox)
        val name: TextView = view.findViewById(R.id.habit_name)
        val description: TextView = view.findViewById(R.id.habit_description)
        val progress: ProgressBar = view.findViewById(R.id.habit_progress)
        val progressText: TextView = view.findViewById(R.id.progress_text)
        val deleteButton: ImageButton = view.findViewById(R.id.delete_button)
        val habitIcon: TextView = view.findViewById(R.id.habit_icon)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HabitViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_habit, parent, false)
        return HabitViewHolder(view)
    }

    override fun onBindViewHolder(holder: HabitViewHolder, position: Int) {
        val habit = habits[position]

        holder.name.text = habit.name
        holder.description.text = habit.description
        holder.checkbox.isChecked = habit.isCompletedToday()
        holder.progress.progress = habit.getTodayProgress()
        holder.progressText.text = "${habit.getTodayProgress()}%"

        // Display the habit's icon
        holder.habitIcon.text = habit.icon

        holder.checkbox.setOnClickListener {
            habit.toggleCompletion()
            onHabitToggled(habit)
            notifyItemChanged(position)
        }

        holder.deleteButton.setOnClickListener {
            onHabitDeleted(habit)
        }

        holder.itemView.setOnClickListener {
            onHabitClicked(habit)
        }
    }

    override fun getItemCount() = habits.size
}