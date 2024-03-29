package com.bignerdranch.android.jeremybui_simpleboggle

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle

class MainActivity : AppCompatActivity(), GameFragment.GameInteractionListener {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction().apply {
                replace(R.id.gameFragmentContainer, GameFragment())
                replace(R.id.scoreFragmentContainer, ScoreFragment())
                commit()
            }
        }
    }

    override fun onScoreUpdated(score: Int) {
        val scoreFragment = supportFragmentManager.findFragmentById(R.id.scoreFragmentContainer) as ScoreFragment
        scoreFragment.updateScore(score)
    }
}
