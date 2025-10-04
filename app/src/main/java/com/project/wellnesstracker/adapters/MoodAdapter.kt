package com.project.wellnesstracker.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.project.wellnesstracker.R
import com.project.wellnesstracker.models.MoodEntry

class MoodAdapter(
    private val moods: MutableList<MoodEntry>
) : RecyclerView.Adapter<MoodAdapter.MoodViewHolder>() {

    inner class MoodViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val emoji: TextView = view.findViewById(R.id.mood_emoji)
        val date: TextView = view.findViewById(R.id.mood_date)
        val note: TextView = view.findViewById(R.id.mood_note)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MoodViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_mood, parent, false)
        return MoodViewHolder(view)
    }

    override fun onBindViewHolder(holder: MoodViewHolder, position: Int) {
        val mood = moods[position]
        holder.emoji.text = mood.emoji
        holder.date.text = mood.getFormattedDate()
        holder.note.text = mood.note.ifEmpty { "No note" }
    }

    override fun getItemCount() = moods.size
}
