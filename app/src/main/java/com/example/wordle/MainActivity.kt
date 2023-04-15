package com.example.wordle

import android.annotation.SuppressLint
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.annotation.ColorRes
import java.io.BufferedReader
import java.io.InputStreamReader
import com.example.wordle.GameManager.Result as Result

class MainActivity : AppCompatActivity() {
    private lateinit var gameManager: GameManager
    private val kbResults = HashMap<Char, Result>()

    @SuppressLint("DiscouragedApi")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val wordList = BufferedReader(
            InputStreamReader(
            resources.openRawResource(
                resources.getIdentifier("wordle", "raw", packageName)
            )
        )
        ).readLines()

        gameManager = GameManager(wordList)

        val enterKey = findViewById<Button>(R.id.buttonEnter)
        val backspaceKey = findViewById<Button>(R.id.buttonBack)

        enterKey.isEnabled = gameManager.currentGuess.length == 5
        enterKey.setOnClickListener {
            if (gameManager.isGuessLegit()) {
                handleGuessResult(gameManager.submitGuess())

                enterKey.isEnabled = false
                backspaceKey.isEnabled = false
            }
        }

        backspaceKey.setOnClickListener {
            gameManager.currentGuess--
            getCharBox(gameManager.guessCount + 1, gameManager.currentGuess.length + 1)
                .text = ""

            enterKey.isEnabled = false
            backspaceKey.isEnabled = gameManager.currentGuess.isNotEmpty()
        }

        for (letter in Char(0x41)..Char(0x5A)) {
            getLetterKey(letter).setOnClickListener {
                if (gameManager.currentGuess.length == 5) return@setOnClickListener

                gameManager.currentGuess += letter

                getCharBox(gameManager.guessCount + 1, gameManager.currentGuess.length).text = letter.toString()

                enterKey.isEnabled = gameManager.currentGuess.length == 5
                backspaceKey.isEnabled = gameManager.currentGuess.isNotEmpty()
            }
        }
    }

    private fun getLetterKey(letter: Char): Button {
        val keyId = resources.getIdentifier("button$letter", "id", packageName)
        return findViewById(keyId)
    }

    private fun getAllLetterKeys(): List<Button> {
        val keys = ArrayList<Button>()

        for (letter in Char(0x41)..Char(0x5A)) {
            keys.add(getLetterKey(letter))
        }

        return keys
    }

    private fun getCharBox(row: Int, col: Int): TextView {
        require(row in 1..6) { "row must satisfy: 1 <= row <= 6" }
        require(col in 1..5) { "col must satisfy: 1 <= col <= 5" }

        val charBoxId = resources.getIdentifier(
            "textView$row$col",
            "id",
            packageName
        )
        return findViewById(charBoxId)
    }

    private fun handleGuessResult(results: Array<Pair<Char, Result>?>) {
        for ((idx, resultPair) in results.withIndex()) {
            if (resultPair == null) continue

            val (letter, result) = resultPair

            getCharBox(gameManager.guessCount, idx + 1).setBackgroundResource(result.bgColor)

            if (result.priority > (kbResults[letter]?.priority ?: -1)) {
                kbResults[letter] = result
                getLetterKey(letter).backgroundTintList = getColorStateList(result.bgColor)
            }
        }
    }

    private fun onGameEnd() {
        getAllLetterKeys().forEach {
            it.isEnabled = false
        }


    }
}

private class GameManager(private val wordList: List<String>) {
    enum class Result(val priority: Int, @ColorRes val bgColor: Int) {
        WRONG(0, R.color.gray),
        RIGHT(1, R.color.green),
        MISPLACED(2, R.color.yellow)
    }

    var currentGuess: String = ""
        set(value) {
            if (value.length <= 5) field = value
        }

    var guessCount = 0
        private set

    /**
     * Function selectWord() utilizes the random class to "pick" a random word from the list of words(wordList)
     */
    private val selectedWord = "SLATE"//wordList[Random.nextInt(wordList.size)].uppercase()

    /**
     * Function legitGuess() checks to see if the user's guess is a valid guess, by checking if the guess
     * is in the list of words(wordList)
     */
    fun isGuessLegit(): Boolean = currentGuess.lowercase() in wordList

    /**
     * Function countCharacterOccurrences takes in a String(str) and returns a map(charactersCounted),
     * where the keys are the characters in str, and the values are the number of times that character appears
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
     * Function gameState() takes in the user's guess and the target word, and returns a color coded version of the
     * guess based on the rules of Wordle.
     */
    fun submitGuess(): Array<Pair<Char, Result>?> {
        check(currentGuess.length == 5) { "Current guess must be 5 characters long before submitting." }

        val mapOfWord = countCharacterOccurrences()

        val result = arrayOfNulls<Pair<Char, Result>>(5)

        for (i in 0..4) {
            if (currentGuess[i] == selectedWord[i]) {
                result[i] = currentGuess[i] to Result.RIGHT
                mapOfWord[selectedWord[i]] = mapOfWord[selectedWord[i]]!! - 1
            }
        }
        for (i in 0..4) {
            if ((currentGuess[i] in mapOfWord) && mapOfWord[currentGuess[i]] != 0) {
                result[i] = currentGuess[i] to Result.MISPLACED
                mapOfWord[currentGuess[i]] = mapOfWord[currentGuess[i]]!! - 1
            } else if ((currentGuess[i] !in mapOfWord)) {
                result[i] = currentGuess[i] to Result.WRONG
            }
        }

        currentGuess = ""
        guessCount++

        return result
    }

    /**
     * Function gameOver checks to see if the user has won, if they have, they are congratulated and the function
     * returns True, if not it returns False.
     */
    val gameOver: Boolean
        get() = guessCount == 5 || currentGuess == selectedWord
}

operator fun String.dec() = if (isNotEmpty()) slice(0 until (length - 1)) else this
