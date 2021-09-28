package com.example.memorymatrix

import android.media.SoundPool
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.memorymatrix.databinding.FragmentFirstBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class FirstFragment : Fragment(R.layout.fragment_first) {

    // Set up binding for FirstFragment
    private var _binding: FragmentFirstBinding? = null
    private val binding get() = _binding!!

    // Prepare button views
    private lateinit var playButton: Button
    private lateinit var leaderboardButton: Button

    // SoundPool for playing PLAY sound
    private lateinit var soundPool: SoundPool
    private var soundIdStart: Int = 0

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        _binding = FragmentFirstBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        // Release SoundPool on destroy
        soundPool.release()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Set soundPool and PLAY sound
        soundPool = SoundPool.Builder().setMaxStreams(1).build()
        soundIdStart = soundPool.load(context, R.raw.start_sound, 1)

        // Get button views
        playButton = binding.playButton
        leaderboardButton = binding.leaderboardButton

        // On click of TOP SCORES show top scores
        leaderboardButton.setOnClickListener {
            CoroutineScope(Dispatchers.Main).launch {
                moveButtonsDown()
                // Wait for moving to finish
                delay(50)
                // Move buttons off screen
                moveButtonsUp()
                // Wait for moving off the screen to finish
                delay(400)
                // Navigate to the top scores fragment (SecondFragment)
                findNavController().navigate(R.id.action_FirstFragment_to_SecondFragment)
            }
        }

        // On click of PLAY start game in new Fragment
        playButton.setOnClickListener {
            CoroutineScope(Dispatchers.Main).launch {
                // Play the start sound
                playStartSound(soundIdStart)
                moveButtonsDown()
                // Wait for moving to finish
                delay(50)
                // Move buttons off screen
                moveButtonsUp()
                // Wait for moving off the screen to finish
                delay(400)
                // Navigate to the game fragment (ThirdFragment)
                findNavController().navigate(R.id.action_FirstFragment_to_ThirdFragment)
            }
        }
    }

    private fun playStartSound(resourceFile: Int) {
        soundPool.play(resourceFile, 1.0F, 1.0F, 0, 0, 1.0F)
    }

    private fun moveButtonsDown() {
        playButton.animate().yBy(100F).duration = 50
        leaderboardButton.animate().yBy(100F).duration = 50
    }

    private fun moveButtonsUp() {
        playButton.animate().y(-200F).duration = 400
        leaderboardButton.animate().y(-200F).duration = 400
    }
}