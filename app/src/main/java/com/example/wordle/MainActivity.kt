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
    var currentGuess = ""

    @SuppressLint("DiscouragedApi")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        for (letter in Char(0x41)..Char(0x5A)) {
            val keyId = resources.getIdentifier("button$letter", "id", packageName)
            val kbKey= findViewById<Button>(keyId)

            kbKey.setOnClickListener {
                if (currentGuess.length < 5) currentGuess += letter
            }
        }

        val wordList = BufferedReader(
            InputStreamReader(
            resources.openRawResource(
                resources.getIdentifier("wordle", "raw", packageName)
            )
        )
        ).readLines()

        val gameManager = GameManager(wordList)
    }
}

private class GameManager(val wordList: List<String>) {
    enum class Guess { RIGHT, WRONG, MISPLACED } // Use this to return guess to UI with a map of Char -> Guess.whatever

    /**
     * Function selectWord() utilizes the random class to "pick" a random word from the list of words(wordList)
     */
    private val selectedWord = wordList[Random.nextInt(wordList.size)].lowercase()

    /**
     * Function legitGuess() checks to see if the user's guess is a valid guess, by checking if the guess
     * is in the list of words(wordList)
     */
    fun legitGuess(guess: String): Boolean = guess.lowercase() in wordList

    /**
     * Function countCharacterOccurrences takes in a String(str) and returns a map(charactersCounted),
     * where the keys are the characters in str, and the values are the number of times that character appears
     */
    fun countCharacterOccurrences(str: String): Map<Char, Int> {
        val charactersCounted = mutableMapOf<Char, Int>()
        for (char in str) {
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
    fun gameState(guess: String, word: String): Pair<String, ArrayList<Guess>> {
        //TODO change to UI

        val mapOfWord = countCharacterOccurrences(word).toMutableMap()

        val colorCodedUI = ArrayList<Guess>(5)

        for (i in 0..4) {
            if (guess[i] == word[i]) {
                colorCodedUI[i] = Guess.RIGHT
                mapOfWord[word[i]] = mapOfWord[word[i]]!! - 1
            }
        }
        for (i in 0..4) {
            if ((guess[i] in mapOfWord) && mapOfWord[guess[i]] != 0) {
                colorCodedUI[i] = Guess.MISPLACED
                mapOfWord[guess[i]] = mapOfWord[guess[i]]!! - 1
            } else if ((guess[i] !in mapOfWord)) {
                colorCodedUI[i] = Guess.WRONG
            }
        }

        return word to colorCodedUI
    }

    /**
     * Function makeGuess takes in the user's guess and target word,and has the user guess again if the user's guess
     * is invalid.
     */
    fun makeGuess(word: String, guess: String) {
        //TODO change to prompt for input from app keyboard and prints in app
        when (legitGuess(guess)) {
            false -> {
                println("Invalid word, please guess again")
                val guessAlt = readln()
                makeGuess(word, guessAlt)
            }
            true -> println(gameState(guess, word))
        }

    }

    /**
     * Function gameOver checks to see if the user has won, if they have, they are congratulated and the function
     * returns True, if not it returns False.
     */
    fun gameOver(userInput: String, word: String): Boolean {
        return userInput == word
    }
}