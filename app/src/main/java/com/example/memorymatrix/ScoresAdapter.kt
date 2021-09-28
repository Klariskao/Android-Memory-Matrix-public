package com.example.memorymatrix

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.time.LocalDate

class ScoresAdapter(private val scoresList: List<UserScore>) : RecyclerView.Adapter<ScoresAdapter.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.score_row, parent, false)
        return ViewHolder(view, parent.context)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(position, scoresList)
    }

    override fun getItemCount(): Int {
        return scoresList.size
    }

    class ViewHolder(private val view: View, val context: Context) : RecyclerView.ViewHolder(view) {
        // Display score position, score and date
        fun bind(position: Int, scoresList: List<UserScore>) {
            view.findViewById<TextView>(R.id.positionView).text = context.resources.getString(R.string.x_dot, position + 1)
            view.findViewById<TextView>(R.id.scoreView).text = scoresList[position].score.toString()
            view.findViewById<TextView>(R.id.dateView).text =
                    getFormattedDate(scoresList[position].day, scoresList[position].month, scoresList[position].year)
        }

        private fun getFormattedDate(day: Int, month: Int, year: Int): String {
            val timeNow = LocalDate.now()
            // If the day and year are the same
            return if (timeNow.dayOfMonth == day && timeNow.monthValue == month && timeNow.year == year) {
                "Today"
            }
            // If the day is one more
            else if (timeNow.dayOfMonth - day == 1 && timeNow.monthValue == month && timeNow.year == year) {
                "Yesterday"
            }
            // Otherwise return formatted date
            else {
                "$day/$month/$year"
            }
        }
    }
}
