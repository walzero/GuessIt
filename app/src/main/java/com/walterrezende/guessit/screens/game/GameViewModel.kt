package com.walterrezende.guessit.screens.game

import android.os.CountDownTimer
import android.text.format.DateUtils
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import timber.log.Timber

class GameViewModel : ViewModel() {

    private val timer: CountDownTimer by lazy {
        object : CountDownTimer(COUNTDOWN_TIME, ONE_SECOND) {

            override fun onTick(millisUntilFinished: Long) {
                _time.value = millisUntilFinished/ONE_SECOND

                if(millisUntilFinished < (COUNTDOWN_TIME/3)) {
                    setBuzz(BuzzType.COUNTDOWN_PANIC)
                }
            }

            override fun onFinish() {
                setBuzz(BuzzType.GAME_OVER)
                _eventGameFinished.value = true
            }
        }
    }

    // The current word
    private val _word = MutableLiveData<String>()
    val word: LiveData<String>
        get() = _word

    // The current score
    private val _score = MutableLiveData<Int>()
    val score: LiveData<Int>
        get() = _score

    private val _time = MutableLiveData<Long>()
    val time = Transformations.map(_time) { time ->
        DateUtils.formatElapsedTime(time)
    }

    private val _buzz = MutableLiveData<BuzzType>()
    val buzz: LiveData<BuzzType>
        get() = _buzz

    // The list of words - the front of the list is the next word to guess
    private lateinit var wordList: MutableList<String>

    private val _eventGameFinished = MutableLiveData<Boolean>()
    val eventGameFinished: LiveData<Boolean>
        get() = _eventGameFinished

    init {
        _eventGameFinished.value = false
        Timber.i("GameViewModel created")
        resetList()
        nextWord()
        _score.value = 0
        timer.start()
    }

    override fun onCleared() {
        super.onCleared()
        timer.cancel()
        Timber.i("GameViewModel destroyed")
    }

    /**
     * Moves to the next word in the list
     */
    private fun nextWord() {
        //Select and remove a word from the list
        if (wordList.isEmpty()) {
            resetList()
        }

        _word.value = wordList.removeAt(0)
    }

    /** Methods for buttons presses **/

    fun onSkip() {
        _score.value = score.value?.dec()
        nextWord()
    }

    fun onCorrect() {
        _score.value = score.value?.inc()
        nextWord()
        setBuzz(BuzzType.CORRECT)
    }

    fun onGameFinishComplete() {
        _eventGameFinished.value = false
    }

    fun onBuzzComplete() {
        setBuzz(BuzzType.NO_BUZZ)
    }

    fun setBuzz(buzzType: BuzzType) {
        _buzz.value = buzzType
    }

    /**
     * Resets the list of words and randomizes the order
     */
    private fun resetList() {
        wordList = mutableListOf(
            "queen",
            "hospital",
            "basketball",
            "cat",
            "change",
            "snail",
            "soup",
            "calendar",
            "sad",
            "desk",
            "guitar",
            "home",
            "railway",
            "zebra",
            "jelly",
            "car",
            "crow",
            "trade",
            "bag",
            "roll",
            "bubble"
        )
        wordList.shuffle()
    }

    companion object {
        // These represent different important times
        // This is when the game is over
        private const val DONE = 0L

        // This is the number of milliseconds in a second
        private const val ONE_SECOND = 1000L

        // This is the total time of the game
        private const val COUNTDOWN_TIME = 10000L
    }
}

enum class BuzzType(val pattern: LongArray) {
    CORRECT(CORRECT_BUZZ_PATTERN),
    GAME_OVER(GAME_OVER_BUZZ_PATTERN),
    COUNTDOWN_PANIC(PANIC_BUZZ_PATTERN),
    NO_BUZZ(NO_BUZZ_PATTERN)
}

private val CORRECT_BUZZ_PATTERN = longArrayOf(100, 100, 100, 100, 100, 100)
private val PANIC_BUZZ_PATTERN = longArrayOf(0, 200)
private val GAME_OVER_BUZZ_PATTERN = longArrayOf(0, 2000)
private val NO_BUZZ_PATTERN = longArrayOf(0)