package com.bignerdranch.android.jeremybui_simpleboggle

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.GridLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import kotlin.math.abs

class GameFragment : Fragment() {
    private var listener: GameInteractionListener? = null
    private lateinit var selectedLettersTextView: TextView
    private val selectedLetters = StringBuilder()
    private val selectedButtons = mutableListOf<Button>()
    private lateinit var dictionary: Set<String>
    private var currentScore = 0
    private val lastSelectedPositions = mutableListOf<Int>()
    private val gridSize = 4

    interface GameInteractionListener {
        fun onScoreUpdated(score: Int)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is GameInteractionListener) {
            listener = context
        } else {
            throw RuntimeException("$context must implement GameInteractionListener")
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        dictionary = loadDictionary()
        val view = inflater.inflate(R.layout.fragment_game, container, false)
        val gridLayout: GridLayout = view.findViewById(R.id.gridLayoutGameBoard)
        selectedLettersTextView = view.findViewById(R.id.selectedLettersTextView)
        val submitButton: Button = view.findViewById(R.id.submitBtn)
        val clearButton: Button = view.findViewById(R.id.clearBtn)

        initializeGameBoard(gridLayout)
        submitButton.setOnClickListener { submitWord() }
        clearButton.setOnClickListener { clearSelection() }
        return view
    }

    private fun initializeGameBoard(gridLayout: GridLayout) {
        val rowCount = gridSize
        val columnCount = gridSize
        gridLayout.rowCount = rowCount
        gridLayout.columnCount = columnCount
        val letters = ('A'..'Z').toList()
        for (i in 0 until rowCount * columnCount) {
            val button = Button(context).apply {
                text = letters.random().toString()
                layoutParams = GridLayout.LayoutParams(
                    GridLayout.spec(i / columnCount, GridLayout.FILL, 1f),
                    GridLayout.spec(i % columnCount, GridLayout.FILL, 1f)
                ).apply {
                    width = 0
                    height = 0
                    setMargins(8, 8, 8, 8)
                }
                setOnClickListener {
                    if (isValidSelection(i)) {
                        handleLetterClick(this, i)
                    }
                }
            }
            gridLayout.addView(button)
        }
    }

    private fun handleLetterClick(button: Button, position: Int) {
        button.isEnabled = false
        selectedLetters.append(button.text)
        selectedLettersTextView.text = selectedLetters.toString()
        selectedButtons.add(button)
        lastSelectedPositions.add(position)
    }

    private fun isValidSelection(position: Int): Boolean {
        // Ensure selection is adjacent to the last if not the first letter
        if (lastSelectedPositions.isNotEmpty()) {
            val lastPosition = lastSelectedPositions.last()
            val lastRow = lastPosition / gridSize
            val lastCol = lastPosition % gridSize
            val currentRow = position / gridSize
            val currentCol = position % gridSize
            if (abs(lastRow - currentRow) > 1 || abs(lastCol - currentCol) > 1) {
                return false
            }
        }
        return true
    }

    private fun submitWord() {
        val word = selectedLetters.toString().uppercase()
        if (word.isNotEmpty() && isValidWord(word)) {
            currentScore += calculateScore(word)
            listener?.onScoreUpdated(currentScore)
        } else {
            currentScore -= 10
            listener?.onScoreUpdated(currentScore)
        }
        clearSelection()
    }

    private fun isValidWord(word: String): Boolean {
        // Implement additional checks here if necessary
        return word.length >= 4 &&
                dictionary.contains(word) &&
                word.count { it in "AEIOUaeiou" } >= 2
    }

    private fun calculateScore(word: String): Int {
        val consonantCount = word.count { it !in "AEIOUaeiou" }
        val vowelCount = word.count { it in "AEIOUaeiou" }
        val baseScore = consonantCount + (vowelCount * 5)

        val containsSpecialConsonant = word.any { it in "SZPXQszpxq" }

        var multiplier = 1
        if (containsSpecialConsonant) {
            multiplier = 2
        }

        return baseScore * multiplier
    }


    // clear button
    private fun clearSelection() {
        selectedLetters.clear()
        selectedLettersTextView.text = ""
        selectedButtons.forEach { button ->
            button.isEnabled = true
        }
        selectedButtons.clear()
    }

    // load dictionary from assets folder
    private fun loadDictionary(): Set<String> {
        val words = mutableSetOf<String>()
        context?.assets?.open("dictionary.txt")?.bufferedReader().use { br ->
            br?.forEachLine { line ->
                words.add(line.trim().uppercase())
            }
        }
        return words
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
    }
}
