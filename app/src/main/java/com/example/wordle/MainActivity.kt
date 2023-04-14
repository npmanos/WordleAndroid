package com.example.wordle

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import java.io.File
import kotlin.random.Random

const val ANSI_RESET = "\u001B[0m"   // Reset to default background color
const val ANSI_GREEN = "\u001B[42m"  // Green background color
const val ANSI_YELLOW = "\u001B[43m" // Yellow background color
const val ANSI_BLACK = "\u001B[40m"  // Black background color
class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Read a file (list of words) used in the game
        val wordList = File("wordle.txt").readLines()


        fun selectWord(): String = wordList[Random.nextInt(wordList.size)]

        /**
         * Function selectWord() utilizes the random class to "pick" a random word from the list of words(wordList)
         */


        fun legitGuess(guess: String): Boolean = when (guess) {
            in wordList -> true
            else -> false
        }

        /**
         * Function legitGuess() checks to see if the user's guess is a valid guess, by checking if the guess
         * is in the list of words(wordList)
         */


        fun countCharacterOccurrences(str: String): Map<Char, Int> {
            /**
             * Function countCharacterOccurrences takes in a String(str) and returns a map(charactersCounted),
             * where the keys are the characters in str, and the values are the number of times that character appears
             */
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


        fun gameState(guess: String, word: String): String {
            //TODO change to UI
            /**
             * Function gameState() takes in the user's guess and the target word, and returns a color coded version of the
             * guess based on the rules of Wordle.
             */
            val colorCoded = Array(5) { " " }
            val mapOfWord = countCharacterOccurrences(word).toMutableMap()
            for (i in 0..4) {
                if (guess[i] == word[i]) {
                    colorCoded[i] = "${ANSI_GREEN}${word[i]}${ANSI_RESET}"
                    mapOfWord[word[i]] = mapOfWord[word[i]]!! - 1
                }
            }
            for (i in 0..4) {
                if ((guess[i] in mapOfWord) && mapOfWord[guess[i]] != 0) {
                    colorCoded[i] = "${ANSI_YELLOW}${guess[i]}${ANSI_RESET}"
                    mapOfWord[guess[i]] = mapOfWord[guess[i]]!! - 1
                } else if ((guess[i] !in mapOfWord)) {
                    colorCoded[i] = "${ANSI_BLACK}${guess[i]}${ANSI_RESET}"
                }
            }


            var colorCodedWord = ""
            for (i in 0..4) {
                if (colorCoded[i] == " ") {
                    colorCoded[i] = "${ANSI_BLACK}${guess[i]}${ANSI_RESET}"
                }
                colorCodedWord += colorCoded[i]
            }

            return (colorCodedWord)
        }


        fun makeGuess(word: String, guess: String) {
            /**
             * Function makeGuess takes in the user's guess and target word,and has the user guess again if the user's guess
             * is invalid.
             */
            when (legitGuess(guess)) {
                false -> {
                    println("Invalid word, please guess again")
                    val guessAlt = readln()
                    makeGuess(word, guessAlt)
                }
                true -> println(gameState(guess, word))
            }

        }


        fun gameOver(userInput: String, word: String): Boolean {
            /**
             * Function gameOver checks to see if the user has won, if they have, they are congratulated and the function
             * returns True, if not it returns False.
             */
            return if (userInput == word) {
                println("Congratulations!  You are a winner!")
                true
            } else {
                false
            }
        }
    }
}