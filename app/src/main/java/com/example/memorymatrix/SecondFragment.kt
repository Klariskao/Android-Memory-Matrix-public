package com.example.memorymatrix

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.activity.addCallback
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

// Top scores Fragment
class SecondFragment : Fragment() {

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_second, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Jetpack's Navigation Components argument from ThirdFragment
        val args: SecondFragmentArgs by navArgs()

        // Overriding the "back" button functionality to always return to FirstFragment
        requireActivity().onBackPressedDispatcher.addCallback(this) {
            findNavController().navigate(R.id.action_SecondFragment_to_FirstFragment)
        }

        // If any arguments are passed to SecondFragment
        if (args.score >= 0) {
            // Make latest score visible
            view.findViewById<TextView>(R.id.latestScore).text = args.score.toString()
            view.findViewById<CardView>(R.id.latestScoreCard).visibility = View.VISIBLE
        }

        val topScores = requireActivity().getSharedPreferences("top_scores", Context.MODE_PRIVATE)
                .getString("scores", "none")

        // If there are scores saved
        if (topScores != "none") {
            // Hide no scores available message
            view.findViewById<TextView>(R.id.message).visibility = View.GONE

            val gson = Gson()
            val listType = object : TypeToken<MutableList<UserScore>>() {}.type
            val obj = gson.fromJson<MutableList<UserScore>>(topScores, listType)

            // Load scoresRV
            view.findViewById<RecyclerView>(R.id.topScoresRecyclerView).apply {
                layoutManager = LinearLayoutManager(context)
                adapter = ScoresAdapter(obj)
            }
        }
    }
}