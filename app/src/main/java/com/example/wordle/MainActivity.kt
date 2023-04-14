package com.example.wordle

import android.annotation.SuppressLint
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import java.io.BufferedReader
import java.io.InputStreamReader
import kotlin.random.Random

const val ANSI_RESET = "\u001B[0m"   // Reset to default background color
const val ANSI_GREEN = "\u001B[42m"  // Green background color
const val ANSI_YELLOW = "\u001B[43m" // Yellow background color
const val ANSI_BLACK = "\u001B[40m"  // Black background color
class MainActivity : AppCompatActivity() {
    private lateinit var gameManager: GameManager

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

        for (letter in Char(0x41)..Char(0x5A)) {
            val keyId = resources.getIdentifier("button$letter", "id", packageName)
            val kbKey= findViewById<Button>(keyId)

            kbKey.setOnClickListener {
                gameManager.currentGuess += letter
            }
        }
    }
}

private class GameManager(private val wordList: List<String>) {
    enum class Guess { RIGHT, WRONG, MISPLACED } // Use this to return guess to UI with a map of Char -> Guess.whatever

    var currentGuess: String = ""
        set(value) {
            if (value.length <= 5) field = value
        }

    var guessCount = 0
        private set

    /**
     * Function selectWord() utilizes the random class to "pick" a random word from the list of words(wordList)
     */
    private val selectedWord = wordList[Random.nextInt(wordList.size)].lowercase()

    /**
     * Function legitGuess() checks to see if the user's guess is a valid guess, by checking if the guess
     * is in the list of words(wordList)
     */
    fun isGuessLegit(): Boolean = currentGuess.lowercase() in wordList

    /**
     * Function countCharacterOccurrences takes in a String(str) and returns a map(charactersCounted),
     * where the keys are the characters in str, and the values are the number of times that character appears
     */
    private fun countCharacterOccurrences(selectedWord: String): MutableMap<Char, Int> {
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
    fun submitGuess(): Pair<String, ArrayList<Guess>> {
        check(isGuessLegit()) { "Current guess must be 5 characters long before submitting." }

        val mapOfWord = countCharacterOccurrences(selectedWord)

        val colorCodedUI = ArrayList<Guess>(5)

        for (i in 0..4) {
            if (currentGuess[i] == selectedWord[i]) {
                colorCodedUI[i] = Guess.RIGHT
                mapOfWord[selectedWord[i]] = mapOfWord[selectedWord[i]]!! - 1
            }
        }
        for (i in 0..4) {
            if ((currentGuess[i] in mapOfWord) && mapOfWord[currentGuess[i]] != 0) {
                colorCodedUI[i] = Guess.MISPLACED
                mapOfWord[currentGuess[i]] = mapOfWord[currentGuess[i]]!! - 1
            } else if ((currentGuess[i] !in mapOfWord)) {
                colorCodedUI[i] = Guess.WRONG
            }
        }

        return selectedWord to colorCodedUI
    }

//    /**
//     * Function makeGuess takes in the user's guess and target word,and has the user guess again if the user's guess
//     * is invalid.
//     */
//    fun makeGuess(word: String, guess: String) {
//        //TODO change to prompt for input from app keyboard and prints in app
//        when (isGuessLegit(guess)) {
//            false -> {
//                println("Invalid word, please guess again")
//                val guessAlt = readln()
//                makeGuess(word, guessAlt)
//            }
//            true -> println(submitGuess(guess, word))
//        }
//
//    }

    /**
     * Function gameOver checks to see if the user has won, if they have, they are congratulated and the function
     * returns True, if not it returns False.
     */
    val gameOver: Boolean
        get() = guessCount == 5 || currentGuess == selectedWord
}