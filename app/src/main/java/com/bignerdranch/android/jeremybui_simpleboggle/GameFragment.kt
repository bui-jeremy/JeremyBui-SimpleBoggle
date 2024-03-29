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
import com.google.android.material.snackbar.Snackbar
import kotlin.math.abs

class GameFragment : Fragment() {
    private var listener: GameInteractionListener? = null
    private lateinit var selectedLettersTextView: TextView
    private val selectedLetters = StringBuilder()
    private var currentScore = 0
    private val gridSize = 4
    private var lastSelectedIndex: Int? = null
    private lateinit var dictionary: Set<String>
    private lateinit var gridLayout: GridLayout
    private var initialLetters: List<String>? = null
    private val allButtons = mutableListOf<Button>()
    private val submittedWords = mutableSetOf<String>()


    interface GameInteractionListener {
        fun onScoreUpdated(score: Int)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is GameInteractionListener) {
            listener = context
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_game, container, false)
        gridLayout = view.findViewById(R.id.gridLayoutGameBoard)
        selectedLettersTextView = view.findViewById(R.id.selectedLettersTextView)

        view.findViewById<Button>(R.id.submitBtn).setOnClickListener { submitWord() }
        view.findViewById<Button>(R.id.clearBtn).setOnClickListener { clearSelection() }

        dictionary = loadDictionary()

        if (initialLetters == null) {
            generateInitialLetters()
        }
        initializeGameBoard()

        return view
    }

    private fun generateInitialLetters() {
        val vowels = listOf("A", "E", "I", "O", "U")
        val consonants = ('A'..'Z').filter { !vowels.contains(it.toString()) }
        val lettersList = mutableListOf<String>()

        // game was unplayable, better chance for vowel
        val vowelProbability = 0.10

        // ensure at least 2 vowels are included initially
        lettersList.add(vowels.random())
        lettersList.add(vowels.random())

        // chance for each slot to be either a vowel or a consonant
        for (i in 2 until gridSize * gridSize) {
            if (Math.random() < vowelProbability) {
                lettersList.add(vowels.random())
            } else {
                lettersList.add(consonants.random().toString())
            }
        }
        initialLetters = lettersList.shuffled()
    }
    private fun initializeGameBoard() {
        gridLayout.removeAllViews()
        allButtons.clear()

        initialLetters?.forEachIndexed { index, letter ->
            val button = Button(context).apply {
                text = letter
                layoutParams = GridLayout.LayoutParams(
                    GridLayout.spec(index / gridSize, GridLayout.FILL, 1f),
                    GridLayout.spec(index % gridSize, GridLayout.FILL, 1f)
                ).apply {
                    width = 0
                    height = 0
                    setMargins(8, 8, 8, 8)
                }
                setOnClickListener { handleLetterClick(this, index) }
            }
            button.setBackgroundColor(resources.getColor(android.R.color.holo_blue_light))
            gridLayout.addView(button)
            allButtons.add(button)
        }
    }

    private fun handleLetterClick(button: Button, position: Int) {
        if (isValidSelection(position)) {
            selectedLetters.append(button.text)
            selectedLettersTextView.text = selectedLetters.toString()
            button.isEnabled = false
            button.setBackgroundColor(resources.getColor(android.R.color.darker_gray))
            lastSelectedIndex = position
        }
    }
    private fun isValidSelection(position: Int): Boolean {
        lastSelectedIndex?.let {
            // calculate the row and column of the last and current selections
            val lastRow = it / gridSize
            val lastCol = it % gridSize
            val currentRow = position / gridSize
            val currentCol = position % gridSize

            // check if the selected button is adjacent to the last
            if (abs(lastRow - currentRow) > 1 || abs(lastCol - currentCol) > 1) {
                Snackbar.make(requireView(), "You may only select connected letters", Snackbar.LENGTH_SHORT).show()
                return false
            }
        }
        return true
    }


    private fun submitWord() {
        val word = selectedLetters.toString().uppercase()
        if (submittedWords.contains(word)) {
            Snackbar.make(requireView(), "You've already used $word", Snackbar.LENGTH_SHORT).show()
            clearSelection()
            return
        }

        if (word.length >= 4 && isValidWord(word)) {
            val scoreToAdd = calculateScore(word)
            submittedWords.add(word)
            currentScore += scoreToAdd
            listener?.onScoreUpdated(currentScore)
            Snackbar.make(requireView(), "That’s correct, +$scoreToAdd", Snackbar.LENGTH_SHORT).show()
        } else {
            currentScore = maxOf(0, currentScore - 10)
            listener?.onScoreUpdated(currentScore)
            Snackbar.make(requireView(), "That’s incorrect, -10", Snackbar.LENGTH_SHORT).show()
        }
        clearSelection()
    }

    private fun isValidWord(word: String): Boolean {
        return word.length >= 4 && dictionary.contains(word) && word.count { it in "AEIOUaeiou" } >= 2
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
        allButtons.forEach { button ->
            button.isEnabled = true
            button.setBackgroundColor(resources.getColor(android.R.color.holo_blue_light))
        }
        lastSelectedIndex = null
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

    fun resetGame() {
        currentScore = 0
        clearSelection()
        generateInitialLetters()
        initializeGameBoard()
        submittedWords.clear()
    }
    override fun onDetach() {
        super.onDetach()
        listener = null
    }
}
