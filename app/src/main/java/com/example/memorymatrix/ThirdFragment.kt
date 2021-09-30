package com.example.memorymatrix

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.AnimatedVectorDrawable
import android.media.SoundPool
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.TextView
import androidx.core.view.doOnLayout
import androidx.core.view.get
import androidx.core.view.marginStart
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.memorymatrix.R.*
import com.example.memorymatrix.databinding.FragmentThirdBinding
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.util.*
import kotlin.math.ceil

class ThirdFragment : Fragment(layout.fragment_third), RecyclerViewClickListener {

    // Set up binding for ThirdFragment
    private var _binding: FragmentThirdBinding? = null
    private val binding get() = _binding!!

    // Get list of levels
    private val levelsList = LevelsData().levelsList

    // Set up game variables
    private var trialNumber = 1
    var userScore = 0
    var tilesUncovered = mutableListOf<Int>()

    private var level = 1
    private var tilesToFind = 0
    private var columnNumber = 0
    private var rowNumber = 0

    private var tilesToBeCorrect = mutableListOf<Int>()

    // Set up sound variables
    private lateinit var soundPool: SoundPool
    private var soundIdAllCorrect: Int = 0
    private var soundIdFlip: Int = 0
    private var soundIdRemovingTiles: Int = 0
    private var soundIdWrong: Int = 0

    // Get views for the Fragment
    private lateinit var animationRecyclerView: RecyclerView
    private lateinit var mainRecyclerView: RecyclerView
    private lateinit var tilesNumberView: TextView
    private lateinit var trialNumberView: TextView
    private lateinit var scoreNumberView: TextView
    private lateinit var frameLayout: FrameLayout

    // Set up adapters
    private var mainAdapter = TilesAdapter(0, 0, listOf(), this@ThirdFragment)
    private lateinit var animationAdapter: WhiteTilesAdapter

    // Initialize offset variables
    var tileOffset = 0
    var isFirstAllCorrect = true

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentThirdBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // Release SoundPool on destroy
        soundPool.release()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Check if previous level is saved and assign game variables
        level = getLevel().toInt()
        tilesToFind = levelsList[level - 1].tiles
        columnNumber = levelsList[level - 1].columns
        rowNumber = levelsList[level - 1].rows

        // Get tiles to be correct
        tilesToBeCorrect = getRandomArray((columnNumber * rowNumber) - 1, tilesToFind)

        // Assign views for the Fragment
        animationRecyclerView = binding.animationRecyclerView
        mainRecyclerView = binding.mainRecyclerView
        tilesNumberView = binding.tilesNumberView
        trialNumberView = binding.trialNumberView
        scoreNumberView = binding.scoreNumberView
        frameLayout = binding.frameLayout

        // Create soundPool
        soundPool = SoundPool.Builder().setMaxStreams(1).build()
        // Create sounds
        soundIdAllCorrect = soundPool.load(context, raw.all_correct_sound, 1)
        soundIdFlip = soundPool.load(context, raw.flipping_sound, 1)
        soundIdRemovingTiles = soundPool.load(context, raw.removing_tiles_sound, 1)
        soundIdWrong = soundPool.load(context, raw.wrong_sound, 1)

        // Assign adapters
        mainAdapter = TilesAdapter(columnNumber, rowNumber, tilesToBeCorrect, this@ThirdFragment)
        animationAdapter = WhiteTilesAdapter(columnNumber, rowNumber)

        tilesNumberView.text = tilesToFind.toString()

        // Start game
        CoroutineScope(Main).launch {
            // Set the grid view adapter for animationRecyclerView
            animationRecyclerView.apply {
                layoutManager = GridLayoutWrapper(context, columnNumber)
                adapter = animationAdapter
            }.post {
                // Show tile revealing animation
                loadingTilesAnimation()
            }
        }

        CoroutineScope(Main).launch {
            // Set the grid view adapter for mainRecyclerView
            mainRecyclerView.apply {
                layoutManager = GridLayoutWrapper(context, columnNumber)
                adapter = mainAdapter
            }
        }
    }

    private fun getRandomArray(totalNumberOfTiles: Int, tilesToFind: Int): MutableList<Int> {
        // Create an array of random numbers within the range of totalNumberOfTiles
        // Size of the array is equal to tilesToFind
        return (0..totalNumberOfTiles).shuffled().take(tilesToFind) as MutableList<Int>
    }

    private fun getLevel(): String {
        // Get level from SharedPreferences
        return requireActivity().getSharedPreferences("level", Context.MODE_PRIVATE)
            .getString("level", "1").toString()
    }

    private fun playSound(resourceFile: Int) {
        soundPool.play(resourceFile, 1F, 1F, 1, 0, 1F)
    }

    private fun loadingTilesAnimation() {
        // Make RecyclerView elements unclickable
        frameLayout.setOnClickListener { }

        // Wait for layout to be ready
        animationRecyclerView.doOnLayout {
            // Make RecyclerView visible
            animationRecyclerView.visibility = View.VISIBLE
            // Launch a coroutine
            CoroutineScope(Main).launch {
                // Flip first half of tiles
                for (i in 0 until columnNumber) {
                    for (j in 0.. i) {
                        animationRecyclerView.getChildAt(j * columnNumber + (i - j)).callOnClick()
                    }
                    // Wait for 125 milliseconds for the flip to be 1/4 complete
                    delay(125)
                }

                // Flip second half of tiles
                // Special case when rows are two more than columns
                if (rowNumber == 8) {
                    for (i in 1 until rowNumber) {
                        if (i == 1) {
                            for (j in rowNumber - 1 downTo i + 1) {
                                animationRecyclerView.getChildAt(j * columnNumber + (i - j)).callOnClick()
                            }
                            // Wait for 125 milliseconds for the flip to be 1/4 complete
                            delay(125)
                        }
                        // Consequent loops run as usual
                        else {
                            for (j in rowNumber downTo i + 1) {
                                animationRecyclerView.getChildAt(j * columnNumber + (i - j)).callOnClick()
                            }
                            // Wait for 125 milliseconds for the flip to be 1/4 complete
                            delay(125)
                        }
                    }
                }
                // If columns and rows difference is less than 2
                else {
                    for (i in 1 until rowNumber) {
                        for (j in rowNumber downTo i + 1) {
                            animationRecyclerView.getChildAt(j * columnNumber + (i - j)).callOnClick()
                        }
                        // Wait for 125 milliseconds for the flip to be 1/4 complete
                        delay(125)
                    }
                }
                // Wait for all tiles to finish flipping
                delay(375)

                // Hide animation Recycler View to reveal main RV
                animationRecyclerView.visibility = View.GONE
                // Show tiles to look for
                revealTilesToGuess()
            }
        }
    }

    private fun revealTilesToGuess() {
        // Make mainRecyclerView visible
        mainRecyclerView.visibility = View.VISIBLE
        mainRecyclerView.doOnLayout {
            CoroutineScope(Main).launch {
                delay(500)
                // Reveal correct tiles to be found
                for(ele in tilesToBeCorrect){
                    // Flip cards which are correct to reveal
                    mainRecyclerView.getChildAt(ele).callOnClick()
                    // Subtract score added by reveal and update score view
                    userScore -= 100
                    scoreNumberView.text = userScore.toString()
                    // Remove uncovered tiles added by reveal
                    tilesUncovered.remove(ele)
                }
                // Keep tiles uncovered for 2 seconds and flip back
                delay(2000)
                for(ele in tilesToBeCorrect){
                    // Flip cards which are correct to hide
                    mainRecyclerView.getChildAt(ele).callOnClick()
                    // Subtract score added by reveal and update score view
                    userScore -= 100
                    scoreNumberView.text = userScore.toString()
                    // Enable tile clicking
                    mainRecyclerView[ele].isClickable = true
                    // Remove uncovered tiles added by reveal
                    tilesUncovered.remove(ele)
                }
                // Wait for tiles to flip back
                delay(500)
                // Make recycler view elements clickable
                frameLayout.visibility = View.GONE
            }
        }
    }

    private fun revealCorrectTiles(){
        // Get number of tiles guessed wrong
        val tilesGuessedWrong = tilesToBeCorrect.minus(tilesUncovered)
        // Get last flipped tile
        val lastUncovered = tilesUncovered[tilesUncovered.size - 1]

        CoroutineScope(Main).launch {
            // If one guessed tile was wrong
            if (tilesGuessedWrong.size == 1) {
                // Level stays the same, flip unguessed tile
                mainRecyclerView.getChildAt(tilesGuessedWrong[0]).callOnClick()
                // Subtract score added by reveal and update score view
                userScore -= 100
                scoreNumberView.text = userScore.toString()
            }
            // If more than one guessed tile was wrong
            else {
                // Level decreases by 1
                if(level > 1){
                    level -= 1
                }
                for (ele in tilesGuessedWrong){
                    // Flip unguessed tiles
                    mainRecyclerView.getChildAt(ele).callOnClick()
                    // Subtract score added by reveal and update score view
                    userScore -= 100
                    scoreNumberView.text = userScore.toString()
                }

            }
            // Wait for tiles to flip
            delay(750)
            removingTilesAnimation(lastUncovered)
        }
    }

    private fun allCorrectAnimation(position: Int) {
        playSound(raw.all_correct_sound)
        // Get correct tile, brown tile and circle view
        val correctTile = binding.backgroundCorrectTile
        val brownTile = binding.brownTile
        val circle = binding.correctTileLayout
        // Find which row and column tile is at
        val row = ceil(((position + 1).toFloat() / columnNumber))
        val column = (position + 1) - (row - 1) * columnNumber

        // If the allCorrectAnimation is not shown for the first time adjust offset of brownTile
        if (!isFirstAllCorrect) {
            tileOffset = binding.linearLayout.marginStart + brownTile.marginStart
        }
        isFirstAllCorrect = false

        val width = mainRecyclerView.getChildAt(position).measuredWidth
        // Adjust position of brown tile to cover original tile
        brownTile.x = width * (column - 1) + tileOffset
        brownTile.y = width * (row - 1) + tileOffset
        // Make brown tile visible
        brownTile.visibility = View.VISIBLE

        // Adjust position of background tile to cover original tile
        correctTile.x = width * (column - 1)
        correctTile.y = width * (row - 1)
        // Make correct tile and circle visible
        correctTile.visibility = View.VISIBLE
        circle.visibility = View.VISIBLE

        // Get ImageView of circle to square animation
        val icon = binding.circleToSquareAnimation
        // Set its drawable to circle_to_square_animation
        icon.setImageResource(drawable.circle_to_square_animation)
        // Get the drawable
        val drawable = icon.drawable
        // Start a coroutine
        CoroutineScope(Main).launch {
            // Start enlarging circle animation
            circle.animate().scaleYBy(30F).scaleXBy(30F).duration = 500L
            // Check drawable is animated
            if(drawable is AnimatedVectorDrawable){
                // Wait for circle to enlarge
                delay(500)
                // Hide circle after enlarging finished
                circle.visibility = View.GONE
                // Reset circle size
                circle.animate().scaleYBy(-30F).scaleXBy(-30F)
                // Make ImageView of circle to square animation visible and run it
                icon.visibility = View.VISIBLE
                drawable.start()
                // Wait for animation to finish, set color of brownTile to green
                delay(500)
                brownTile.setCardBackgroundColor(Color.parseColor("#51A729"))
                // Make the square from circle invisible because its size is not precisely 50x50dp
                icon.visibility = View.GONE
            }
        }

        // Get ImageView of enlarging/deflating tick animation
        val tick = binding.tickImageView
        // Set its drawable to animated_tick
        tick.setImageResource(R.drawable.animated_tick)
        // Get the drawable
        val tickDrawable = tick.drawable
        // Start a coroutine
        CoroutineScope(Main).launch {
            // Check drawable is animated
            if(tickDrawable is AnimatedVectorDrawable){
                // Make ImageView of enlarging/deflating tick animation visible and run it
                tick.visibility = View.VISIBLE
                tickDrawable.start()
                // Wait for tick to fully enlarge, then slightly deflate and stop the animation
                delay(625)
                tickDrawable.stop()
                // After animated tick disappears make tickView visible
                binding.tickView.visibility = View.VISIBLE
            }
            // Remove tiles
            removingTilesAnimation(tilesUncovered[tilesUncovered.size - 1])
        }
    }

    override fun onTileClickAction(position: Int) {
        // Add uncovered tile to tilesUncovered
        tilesUncovered.add(position)

        // If last tile uncovered
        if (tilesUncovered.size == tilesToFind) {
            // Make recycler view elements unclickable
            binding.frameLayout.visibility = View.VISIBLE

            // If all uncovered tiles are correct
            if (tilesUncovered.containsAll(tilesToBeCorrect)) {
                playSound(soundIdFlip)
                // Add score for correct tile and update score view
                userScore += 100
                scoreNumberView.text = userScore.toString()
                // All correct last click animation
                allCorrectAnimation(position)
            }
            // If not all uncovered tiles are correct
            else {
                // If last uncovered tile is correct
                if (position in tilesToBeCorrect) {
                    playSound(soundIdFlip)
                    // Add score for correct tile and update score view
                    userScore += 100
                    scoreNumberView.text = userScore.toString()
                }
                // If message is showing, set to ""
                if (binding.infoView.text != "") {
                    binding.infoView.text = ""
                }
                revealCorrectTiles()
            }
        }
        // If clicked tile is correct
        else if (position in tilesToBeCorrect) {
            playSound(soundIdFlip)
            // Add score for correct tile and update score view
            userScore += 100
            scoreNumberView.text = userScore.toString()
            // Disable tile clicking
            binding.mainRecyclerView[position].isClickable = false
            // If message is already showing and at least one try left, update it
            if (binding.infoView.text.isNotBlank() && tilesUncovered.size < tilesToFind) {
                // If only 1 tile left
                if (tilesUncovered.size - tilesToFind == 1) {
                    binding.infoView.text = getString(string.one_tile_left)
                }
                // If more than one tile left
                else {
                    binding.infoView.text = getString(string.x_tiles_left, tilesToFind - tilesUncovered.size)
                }
            }
            // Else hide message
            else {
                binding.infoView.text = ""
            }

        }
        // If clicked tile is wrong
        else {
            playSound(raw.wrong_sound)
            // Disable tile clicking
            binding.mainRecyclerView[position].isClickable = false
            // Show message
            // If only 1 tile left
            if (tilesUncovered.size - tilesToFind == 1) {
                binding.infoView.text = getString(string.one_tile_left)
            }
            // If more than one tile left
            else {
                binding.infoView.text = getString(string.x_tiles_left, tilesToFind - tilesUncovered.size)
            }
        }
    }

    private fun bonusPointsAnimation(additionalValue: Int) {
        CoroutineScope(Main).launch {
            // Show bonus strings
            binding.bonusTextView.visibility = View.VISIBLE
            binding.bonusNumberView.text = getString(string.plus_x, additionalValue)
            // Score animator
            val animator = ValueAnimator.ofInt(userScore, additionalValue + userScore)
            animator.duration = 1000
            animator.addUpdateListener { animation -> binding.scoreNumberView.text = animation.animatedValue.toString() }
            animator.start()
            // Update score with bonus
            userScore += additionalValue
            // Wait until animation finished
            delay(1000)
            // Hide bonus strings
            binding.bonusTextView.visibility = View.GONE
            binding.bonusNumberView.text = ""
        }
    }

    private fun removingTilesAnimation(startingTile: Int) {
        // Make recycler view elements unclickable
        frameLayout.visibility = View.VISIBLE

        // Make the background of animationRecyclerView transparent so that mainRecyclerView shows through
        animationRecyclerView.setBackgroundColor(Color.parseColor("#00FFFFFF"))
        // Cover mainRecyclerView with animationRecyclerView
        animationRecyclerView.visibility = View.VISIBLE

        // Find which row and column tile is at
        val row = ceil(((startingTile + 1).toFloat() / columnNumber)).toInt()
        val column = (startingTile + 1) - (row - 1) * columnNumber

        // If all tiles are correct play sound of soundIdRemovingTiles
        if (tilesUncovered.size == tilesToBeCorrect.size) {
            playSound(soundIdRemovingTiles)
        }

        // Run two coroutines
        CoroutineScope(Main).launch {
            // Move in a row to the left starting from the startingTile
            for (i in row downTo 1) {
                //Log.d("TAG", "Row loop 1: $i")
                clickByColumn(i, column)
                // Wait before moving to the next column on the left
                delay(125)
            }

            // Wait for tiles to be removed
            delay(columnNumber * 500L)

            // Set mainRecyclerView back to GONE
            mainRecyclerView.visibility = View.GONE

            // If all guessed tiles were correct
            if(tilesToBeCorrect.size == tilesUncovered.size) {
                // Wait for tiles to be removed
                delay(500)
                // Add bonus points
                bonusPointsAnimation(tilesToFind * 100)
                // Wait until animation finished
                delay(1000)

                // Level increases by 1
                if(level < 22){
                    level += 1
                }

                // Make brownTile invisible and set its color back to brown
                val brownTile = binding.brownTile
                brownTile.visibility = View.GONE
                brownTile.setCardBackgroundColor(Color.parseColor("#714A43"))
                // Get views to original setup
                binding.tickImageView.visibility = View.GONE
                binding.tickView.visibility = View.GONE
                mainRecyclerView.visibility = View.GONE
            }
            // Launch the next trial
            moveToNextTrial()
        }

        CoroutineScope(Main).launch {
            // Wait for starting tile to flip 1/4
            delay(125)
            // Move in a row to the right starting from the tile to the right of the startingTile
            for (i in row + 1 until rowNumber + 1) {
                //Log.d("TAG", "Row loop 2: $i")
                clickByColumn(i, column)
                // Wait before moving to the next column on the right
                delay(125)
            }
        }
    }

    private fun clickByColumn(row: Int, column: Int) {
        CoroutineScope(Main).launch {
            // Move in a column up starting from the startingTile
            for (i in column downTo 1) {
                animationRecyclerView.getChildAt((row - 1) * columnNumber + i - 1).callOnClick()
                // Wait before moving to the next row up
                delay(125)
            }
        }

        CoroutineScope(Main).launch {
            // Wait for starting tile to flip 1/4
            delay(125)
            // Move in a column down starting from the tile below the startingTile
            for (i in column + 1 until columnNumber + 1) {
                animationRecyclerView.getChildAt((row - 1) * columnNumber + i - 1).callOnClick()
                // Wait before moving to the next row down
                delay(125)
            }
        }
    }

    private fun moveToNextTrial() {
        // Increase trialNumber by 1
        trialNumber += 1

        // If the trial is not the final one update trialNumberView and launch the next trial
        if(trialNumber < 13) {
            // Update trialNumber view
            binding.trialNumberView.text = trialNumber.toString()
            // Update game variables
            tilesToFind = levelsList[level - 1].tiles
            columnNumber = levelsList[level - 1].columns
            rowNumber = levelsList[level - 1].rows

            tilesNumberView.text = tilesToFind.toString()
            launchNextTrial()
        }
        else {
            endGame()
        }
    }

    private fun launchNextTrial() {
        // Clear tilesUncovered
        tilesUncovered.clear()

        val newTilesToBeCorrect = getRandomArray((columnNumber * rowNumber) - 1, tilesToFind)
        tilesToBeCorrect.clear()
        tilesToBeCorrect.addAll(newTilesToBeCorrect)
        // Update mainRecyclerView
        (mainRecyclerView.layoutManager as GridLayoutManager).spanCount = columnNumber
        mainRecyclerView.adapter = TilesAdapter(columnNumber, rowNumber, tilesToBeCorrect, this@ThirdFragment)
        // Update animationRecyclerView (update span and tile number)
        (animationRecyclerView.layoutManager as GridLayoutManager).spanCount = columnNumber
        animationRecyclerView.adapter = WhiteTilesAdapter(columnNumber, rowNumber)

        // Reset visibilities of RecyclerViews
        mainRecyclerView.visibility = View.GONE
        animationRecyclerView.visibility = View.VISIBLE

        CoroutineScope(Main).launch {
            delay(500)
            loadingTilesAnimation()
        }
    }

    private fun endGame() {
        saveScore()
        saveLevel()

        // Jetpack's Navigation Components sending an argument (userScore) to SecondFragment
        val action = ThirdFragmentDirections.actionThirdFragmentToSecondFragment(userScore)
        // Navigate to SecondFragment
        view?.findNavController()?.navigate(action)
    }

    private fun saveScore() {
        // Get scores from SharedPreferences
        val topScores = requireActivity().getSharedPreferences("top_scores", Context.MODE_PRIVATE)
                .getString("scores", "none")

        var updatedTopScores: MutableList<UserScore> = mutableListOf()

        // If topScores is not empty
        if (topScores != "none") {
            // Convert string to JSON
            val gson = Gson()
            val listType = object : TypeToken<MutableList<UserScore>>() {}.type
            val obj = gson.fromJson<MutableList<UserScore>>(topScores, listType)
            // If all 5 top scores are full
            if (obj.size == 5) {
                // Check if current score is higher than lowest top score
                if (obj[obj.size - 1].score <= userScore) {
                    // If it is remove the lowest score from topScores obj
                    obj.removeLast()
                    val date = LocalDate.now()
                    // Add userScore and sort the topScores obj by score
                    obj.add(UserScore(date.dayOfMonth, date.monthValue, date.year, userScore))
                    updatedTopScores = obj.sortedByDescending { it.score } as MutableList<UserScore>
                }
            }
            // If not all 5 top scores are full
            else {
                val date = LocalDate.now()
                // Add score to topScores obj and sort by score
                obj.add(UserScore(date.dayOfMonth, date.monthValue, date.year, userScore))
                updatedTopScores = obj.sortedByDescending { it.score } as MutableList<UserScore>
            }

        }
        // If topScores is empty
        else {
            val date = LocalDate.now()
            // Create UserScore and add it to an empty list of scores
            val newScore = UserScore(date.dayOfMonth, date.monthValue, date.year, userScore)
            updatedTopScores.add(newScore)
        }
        // If new score was added to the topScores
        if (updatedTopScores.isNotEmpty()) {
            val gson = Gson()
            // Save new scores list to SharedPreferences
            requireActivity().getSharedPreferences("top_scores", Context.MODE_PRIVATE).edit()
                    .apply {
                        putString("scores", gson.toJson(updatedTopScores))
                    }.apply()
        }
    }

    private fun saveLevel() {
        // Save level for use in next game
        requireActivity().getSharedPreferences("level", Context.MODE_PRIVATE).edit()
                .apply {
                    putString("level", level.toString())
                }.apply()
    }
}



