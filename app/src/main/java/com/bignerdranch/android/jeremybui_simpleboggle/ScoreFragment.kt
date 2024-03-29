package com.bignerdranch.android.jeremybui_simpleboggle

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment

class ScoreFragment : Fragment() {
    private lateinit var textViewScore: TextView
    private var currentScore: Int = 0

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_score, container, false)
        textViewScore = view.findViewById(R.id.textViewScore)

        val restartGameButton = view.findViewById<Button>(R.id.restartGameButton)
        restartGameButton.setOnClickListener {
            (activity as? MainActivity)?.onRestartGame()
        }

        updateScoreDisplay()
        return view
    }

    fun updateScore(newScore: Int) {
        currentScore = newScore
        updateScoreDisplay()
    }

    private fun updateScoreDisplay() {
        textViewScore.text = getString(R.string.score_format, currentScore)
    }
}
