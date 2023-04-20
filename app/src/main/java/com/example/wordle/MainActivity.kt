/*
 * Assignment 3: Wordle Android
 * Authors: Jack Hatfield
 *          Nick Manos
 *
 * We took one of the provided solutions to Assignment 1 and encapsulated it in
 * its own class called GameManager. (This was done for better separation of
 * concerns by keeping game logic and UI management separate from each other. It
 * would also aid in adding a ViewModel in the future, though that was outside the
 * scope of this assignment and therefore not implemented.) We then removed removed
 * all code related to CLI interaction and replaced it with an API that would allow
 * the activity to appropriately display game state.
 *
 * We defined the methods named in the android:onClick property of the "button",
 * "button_enter", and "button_back" styles in themes.xml. The letterHandler() and
 * backspaceHandler() methods update the to reflect user input and alter the
 * currentGuess string in GameManager. The enterHandler method calls isValid() in
 * GameManager to see if currentGuess is in the wordlist, and if so, calls
 * submitGuess() in GameManager, which returns an Array of Pairs of Char and an
 * enum Result which indicates whether the letter was right, wrong, or misplaced.
 * The UI is then updated appropriately. If the gameOver property in GameManager is
 * true, endGame() is called to update the UI to reflect a win or lose state.
 *
 * All functionality should be implemented as described in the instructions and
 * there are no known bugs.
 */

package com.example.wordle

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.annotation.ColorRes
import androidx.appcompat.app.AppCompatActivity
import com.example.wordle.GameManager.Result
import java.io.BufferedReader
import java.io.InputStreamReader
import kotlin.random.Random

class MainActivity : AppCompatActivity() {
    private lateinit var gameManager: GameManager

    /**
     * [HashMap] containing the current state of keyboard key coloring.
     */
    private val kbResults = HashMap<Char, Result>()

    private val enterKey by lazy { findViewById<Button>(R.id.buttonEnter) }
    private val backspaceKey by lazy { findViewById<Button>(R.id.buttonBack) }

    @SuppressLint("DiscouragedApi")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Load wordlist from raw resources
        val wordList = BufferedReader(
            InputStreamReader(
                resources.openRawResource(
                    resources.getIdentifier("wordle", "raw", packageName)
                )
            )
        ).readLines()

        gameManager = GameManager(wordList)

        // Display the answer for debug purposes
        findViewById<TextView>(R.id.message).text =
            "The word is ${gameManager.selectedWord.lowercase()}"

        enterKey.isEnabled = gameManager.currentGuess.length == 5
        backspaceKey.isEnabled = gameManager.currentGuess.isNotEmpty()
    }

    /**
     * Letter key onClick handler as defined in `button` style in `themes.xml`
     *
     * @param[kbKey] reference to the [letter key][Button] that called this function
     * @see [android.R.attr.onClick]
     */
    fun letterHandler(kbKey: View) {
        kbKey as Button

        if (gameManager.currentGuess.length == 5) return

        gameManager.currentGuess += kbKey.text

        getCharBox(gameManager.guessCount + 1, gameManager.currentGuess.length).text = kbKey.text

        enterKey.isEnabled = gameManager.currentGuess.length == 5
        backspaceKey.isEnabled = gameManager.currentGuess.isNotEmpty()
    }

    /**
     * Enter key onClick handler as defined in `button_enter` style in `themes.xml`
     *
     * @param[enterKey] reference to the [enter key][Button]
     * @see [android.R.attr.onClick]
     */
    fun enterHandler(enterKey: View) {
        enterKey as Button

        if (gameManager.isGuessLegit()) {
            findViewById<TextView>(R.id.message).text =
                "The word is ${gameManager.selectedWord.lowercase()}"
            handleGuessResult(gameManager.submitGuess())

            enterKey.isEnabled = false
            backspaceKey.isEnabled = false
        } else {
            findViewById<TextView>(R.id.message).text = "Not a word"
        }
    }

    /**
     * Backspace key onClick handler as defined in `button_back` style in `themes.xml`
     *
     * @param[backspaceKey] reference to the [backspace key][Button]
     * @see [android.R.attr.onClick]
     */
    fun backspaceHandler(backspaceKey: View) {
        backspaceKey as Button

        gameManager.currentGuess--
        getCharBox(gameManager.guessCount + 1, gameManager.currentGuess.length + 1).text = ""

        enterKey.isEnabled = false
        backspaceKey.isEnabled = gameManager.currentGuess.isNotEmpty()
    }

    /**
     * Get the keyboard [Button] for the given letter.
     *
     * @param[letter] letter to get the key for (must be in ['A'][Char]..['Z'][Char])
     * @return [Button] referencing the keyboard key
     */
    private fun getLetterKey(letter: Char): Button {
        require(letter in 'A'..'Z')

        val keyId = resources.getIdentifier("button$letter", "id", packageName)
        return findViewById(keyId)
    }

    /**
     * Get all letter keys from the app keyboard.
     *
     * @return [List] of all [Button] references for keyboard letter keys
     */
    private fun getAllLetterKeys(): List<Button> {
        val keys = ArrayList<Button>()

        for (letter in Char(0x41)..Char(0x5A)) {
            keys.add(getLetterKey(letter))
        }

        return keys
    }

    /**
     * Get the [TextView] for the specified guess letter input box.
     *
     * @param[row] row coordinate (1 <= row <= 6)
     * @param[col] column coordinate (1 <= col <= 5)
     * @return [TextView] reference for the requested guess letter input box
     */
    private fun getCharBox(row: Int, col: Int): TextView {
        require(row in 1..6) { "row must satisfy: 1 <= row <= 6" }
        require(col in 1..5) { "col must satisfy: 1 <= col <= 5" }

        val charBoxId = resources.getIdentifier(
            "textView$row$col", "id", packageName
        )
        return findViewById(charBoxId)
    }

    /**
     * Update the UI to reflect result of a submitted guess
     *
     * @param[results] [Array] containing a [Char] to [Result] ``[Pair] for each letter in the guessed word
     */
    private fun handleGuessResult(results: Array<Pair<Char, Result>?>) {
        for ((idx, resultPair) in results.withIndex()) {
            if (resultPair == null) continue //This should never be null

            val (letter, result) = resultPair

            getCharBox(gameManager.guessCount, idx + 1).apply {
                backgroundTintList = getColorStateList(result.bgColor)
                setTextColor(resources.getColor(R.color.white, theme))
            }

            // Update the key color according to these rules: WRONG < MISPLACED < RIGHT
            if (result.priority > (kbResults[letter]?.priority ?: -1)) {
                kbResults[letter] = result
                getLetterKey(letter).apply {
                    backgroundTintList = getColorStateList(result.bgColor)
                    setTextColor(resources.getColor(R.color.white, theme))
                }
            }
        }

        if (gameManager.gameOver) endGame()
    }

    /**
     * Update UI to reflect end of game state.
     *
     * "You win" is shown if the last guess was correct, otherwise the correct answer is shown.
     */
    private fun endGame() {
        getAllLetterKeys().forEach {
            it.isEnabled = false
        }

        enterKey.isEnabled = false
        backspaceKey.isEnabled = false

        findViewById<TextView>(R.id.message).text =
            if (gameManager.currentGuess == gameManager.selectedWord) "You win!" else "The word was ${gameManager.selectedWord.lowercase()}"
    }
}

/**
 * Manages game state separate from UI.
 *
 * Handles selecting a word, holding the current guess, tracking the number of guess, checking a
 * guess against the answer, and determining if the game is over.
 *
 * @param[wordList] [List] of [Strings][String] containing valid words
 */
private class GameManager(private val wordList: List<String>) {

    /**
     * Enum representing the result of a letter in a guess.
     *
     * @property priority A higher priority result should replace a lower priority result on the
     *                    keyboard
     * @property bgColor [ColorRes] for the color that should be used in the UI to represent
     *                   the result
     */
    enum class Result(val priority: Int, @ColorRes val bgColor: Int) {
        WRONG(0, R.color.gray), RIGHT(2, R.color.green), MISPLACED(1, R.color.yellow)
    }

    /**
     * String containing the current guess input.
     *
     * [Length][String.length] must be `<= 5`.
     */
    var currentGuess: String = ""
        set(value) {
            if (value.length <= 5) field = value
        }

    /**
     * The number of submitted guesses.
     */
    var guessCount = 0
        private set

    /**
     * The selected word.
     *
     * Utilizes the [Random] class to "pick" a random word from [wordList]
     */
    val selectedWord =
        wordList[Random.nextInt(wordList.size)].uppercase().also { Log.d(".example.wordle", it) }

    /**
     * Checks to see if the user's guess is a valid guess, by checking if the guess
     * is in [wordList]
     */
    fun isGuessLegit(): Boolean = currentGuess.lowercase() in wordList

    /**
     * Returns a map where the keys are the characters in [selectedWord] and the values are the
     * number of times that character appears in [selectedWord].
     *
     * @return [MutableMap] with letters mapped to their count
     */
    private fun countCharacterOccurrences(): MutableMap<Char, Int> {
        val charactersCounted = mutableMapOf<Char, Int>()
        for (char in selectedWord) {
            when {
                charactersCounted.containsKey(char) -> charactersCounted[char] =
                    charactersCounted[char]!! + 1

                else -> charactersCounted[char] = 1
            }
        }

        return charactersCounted
    }

    /**
     * Checks [currentGuess] against [selectedWord] and returns an array pairing each letter to
     * the appropriate [Result].
     *
     * Increments [guessCount], updates [gameOver], and clears [currentGuess] if the game isn't over.
     *
     * @return [Array] containing a [Char] to [Result] ``[Pair] for each letter in the guessed word
     */
    fun submitGuess(): Array<Pair<Char, Result>?> {
        check(currentGuess.length == 5) { "Current guess must be 5 characters long before submitting." }
        check(isGuessLegit())

        val mapOfWord = countCharacterOccurrences()

        val result = arrayOfNulls<Pair<Char, Result>>(5)

        for (i in 0..4) {
            if (currentGuess[i] == selectedWord[i]) {
                result[i] = currentGuess[i] to Result.RIGHT
                mapOfWord[selectedWord[i]] = mapOfWord[selectedWord[i]]!! - 1
            } else if ((currentGuess[i] in mapOfWord) && mapOfWord[currentGuess[i]] != 0) {
                result[i] = currentGuess[i] to Result.MISPLACED
                mapOfWord[currentGuess[i]] = mapOfWord[currentGuess[i]]!! - 1
            } else {
                result[i] = currentGuess[i] to Result.WRONG
            }
        }

        guessCount++
        gameOver = guessCount == 6 || currentGuess == selectedWord
        if (!gameOver) currentGuess = ""

        return result
    }

    /**
     * Whether the game has reached its end state (win or lose).
     */
    var gameOver: Boolean = false
        private set
}

/**
 * Returns this string with the last [Char] removed.
 *
 * If the string is empty, it is returned as is.
 */
operator fun String.dec() = if (isNotEmpty()) slice(0 until (length - 1)) else this
