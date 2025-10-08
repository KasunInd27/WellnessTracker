package com.project.wellnesstracker.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.project.wellnesstracker.R
import com.project.wellnesstracker.adapters.MoodAdapter
import com.project.wellnesstracker.models.MoodEntry
import com.project.wellnesstracker.utils.DataManager

class MoodJournalFragment : Fragment() {

    private lateinit var dataManager: DataManager
    private lateinit var moodAdapter: MoodAdapter
    private val moods = mutableListOf<MoodEntry>()
    private var selectedEmoji: String? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_mood_journal, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        dataManager = DataManager(requireContext())
        moods.addAll(dataManager.loadMoodEntries())
        moods.sortByDescending { it.timestamp }

        val recyclerView = view.findViewById<RecyclerView>(R.id.mood_recycler)
        moodAdapter = MoodAdapter(moods)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = moodAdapter

        // Emoji selection
        val emojis = listOf("😊", "😐", "😢", "😡", "😴")
        val emojiViews = listOf(
            view.findViewById<TextView>(R.id.emoji_1),
            view.findViewById<TextView>(R.id.emoji_2),
            view.findViewById<TextView>(R.id.emoji_3),
            view.findViewById<TextView>(R.id.emoji_4),
            view.findViewById<TextView>(R.id.emoji_5)
        )

        emojiViews.forEachIndexed { index, emojiView ->
            emojiView.setOnClickListener {
                selectedEmoji = emojis[index]
                emojiViews.forEach { it.alpha = 0.5f }
                emojiView.alpha = 1.0f
            }
        }

        val noteInput = view.findViewById<EditText>(R.id.note_input)

        view.findViewById<Button>(R.id.save_mood_button).setOnClickListener {
            if (selectedEmoji != null) {
                val mood = MoodEntry(
                    emoji = selectedEmoji!!,
                    note = noteInput.text.toString()
                )
                moods.add(0, mood)
                moodAdapter.notifyItemInserted(0)
                recyclerView.scrollToPosition(0)
                dataManager.saveMoodEntries(moods)

                noteInput.text.clear()
                emojiViews.forEach { it.alpha = 1.0f }
                selectedEmoji = null

                Toast.makeText(requireContext(), "Mood saved!", Toast.LENGTH_SHORT).show()

                // Share functionality
                shareMoodSummary()
            } else {
                Toast.makeText(requireContext(), "Please select an emoji", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun shareMoodSummary() {
        val recentMoods = moods.take(5)
        val summary = buildString {
            append("My Recent Moods:\n\n")
            recentMoods.forEach {
                append("${it.emoji} ${it.getFormattedDate()}\n")
                if (it.note.isNotEmpty()) append("  ${it.note}\n")
            }
        }

        val shareIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, summary)
            type = "text/plain"
        }
    }
}