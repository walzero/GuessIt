/*
 * Copyright 2018, The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.walterrezende.guessit.screens.game

import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.getSystemService
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.NavHostFragment.findNavController
import com.walterrezende.guessit.R
import com.walterrezende.guessit.databinding.GameFragmentBinding
import timber.log.Timber

/**
 * Fragment where the game is played
 */
class GameFragment : Fragment() {

    private lateinit var viewModel: GameViewModel

    private lateinit var binding: GameFragmentBinding

    private val gameFinished by lazy { Observer<Boolean> { onGameFinish(it) } }
    private val buzzChanged by lazy { Observer<BuzzType> { onBuzz(it) } }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        // Inflate view and obtain an instance of the binding class
        binding = DataBindingUtil.inflate(
            inflater,
            R.layout.game_fragment,
            container,
            false
        )

        Timber.i("Called ViewModelProvider")
        viewModel = ViewModelProvider(this).get(GameViewModel::class.java)
        binding.lifecycleOwner = this
        binding.gameViewModel = viewModel

        return binding.root
    }

    override fun onStart() {
        super.onStart()
        addObservers()
    }

    override fun onStop() {
        super.onStop()
        removeObservers()
    }

    private fun addObservers() {
        viewModel.eventGameFinished.observe(this, gameFinished)
        viewModel.buzz.observe(this, buzzChanged)
    }

    private fun removeObservers() {
        viewModel.eventGameFinished.removeObserver(gameFinished)
        viewModel.buzz.removeObserver(buzzChanged)
    }

    private fun onGameFinish(hasFinished: Boolean) {
        if (hasFinished) {
            gameFinished()
            viewModel.onGameFinishComplete()
        }
    }

    /**
     * Called when the game is finished
     */
    private fun gameFinished() {
        val action = GameFragmentDirections.actionGameToScore(viewModel.score.value ?: 0)
        findNavController(this).navigate(action)
    }

    private fun onBuzz(buzzType: BuzzType?) {
        buzzType?.takeUnless { it == BuzzType.NO_BUZZ }?.let {
            buzz(it.pattern)
            viewModel.onBuzzComplete()
        }
    }

    private fun buzz(pattern: LongArray) {
        val buzzer = requireActivity().getSystemService<Vibrator>()

        buzzer?.let {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                it.vibrate(VibrationEffect.createWaveform(pattern, 0))
            } else {
                //deprecated in API 26
                it.vibrate(pattern, 0)
            }
        }
    }
}
