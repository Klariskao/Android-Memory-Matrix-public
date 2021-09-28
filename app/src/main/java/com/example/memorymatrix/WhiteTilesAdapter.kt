package com.example.memorymatrix

import android.animation.AnimatorInflater
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Context
import android.content.res.Resources
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.cardview.widget.CardView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.ceil

class WhiteTilesAdapter(private val columnNumber: Int, rowNumber: Int) : RecyclerView.Adapter<WhiteTilesAdapter.AndroidVersionRecyclerViewHolder>() {

    private lateinit var mParent: ViewGroup
    var tilesNumber = columnNumber * rowNumber

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AndroidVersionRecyclerViewHolder {
        mParent = parent
        return AndroidVersionRecyclerViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.white_tile, parent, false))
    }

    // Get number of items
    override fun getItemCount(): Int = tilesNumber

    // Loop through items
    override fun onBindViewHolder(holder: AndroidVersionRecyclerViewHolder, position: Int) {
        holder.bind(mParent, columnNumber)
    }

    class AndroidVersionRecyclerViewHolder(private val view: View): RecyclerView.ViewHolder(view){
        private val tile: ConstraintLayout = view.findViewById(R.id.whiteTileLayout)

        fun bind(parent: ViewGroup, columnNumber: Int){
            // Set back and front views
            val front = view.findViewById<CardView>(R.id.whiteTileFront)
            val back = view.findViewById<CardView>(R.id.whiteTileBack)
            // Assign back and front
            findViews(front, back)
            changeCameraDistance(parent.resources)
            loadAnimations(parent.context)
            // On tile click
            tile.setOnClickListener {
                // Flip card based on
                flipCard(columnNumber)
            }
        }

        private var mSetRightOut: AnimatorSet? = null
        private var mSetLeftIn: AnimatorSet? = null
        private var mIsFrontVisible = true
        private lateinit var mCardFrontLayout: View
        private lateinit var mCardBackLayout: View

        private fun changeCameraDistance(resources: Resources) {
            val distance = 8000
            val scale: Float = resources.displayMetrics.density * distance
            mCardFrontLayout.cameraDistance = scale
            mCardBackLayout.cameraDistance = scale
        }

        private fun loadAnimations(context: Context) {
            mSetRightOut = AnimatorInflater.loadAnimator(context, R.animator.front_card_flip) as AnimatorSet
            mSetLeftIn = AnimatorInflater.loadAnimator(context, R.animator.back_card_flip) as AnimatorSet
        }

        private fun findViews(front: CardView, back: CardView) {
            mCardFrontLayout = front
            mCardBackLayout = back
        }

        private fun flipCard(columnNumber: Int) {
            val front = view.findViewById<CardView>(R.id.whiteTileFront)

            // Second turn when tiles are disappearing
            mIsFrontVisible = if (!mIsFrontVisible) {
                // Don't block thread
                CoroutineScope(Main).launch {
                    // Animation of color change from transparent to white on front of the card
                    val animator = ObjectAnimator.ofArgb(front, "backgroundColor",
                            Color.TRANSPARENT, Color.WHITE)
                    animator.duration = 500
                    animator.start()
                    // Wait for color to change before turning tile
                    delay(500)

                    // Set tile background color to dark brown to cover main RV before flipping tile
                    tile.setBackgroundColor(Color.parseColor("#362722"))

                    mSetRightOut!!.setTarget(mCardFrontLayout)
                    mSetLeftIn!!.setTarget(mCardBackLayout)
                    mSetRightOut!!.start()
                    mSetLeftIn!!.start()
                }
                true
            }
            // First turn when animation RV is loading
            // Flip from dark brown to light brown (tiles appearing)
            else {
                mSetRightOut!!.setTarget(mCardBackLayout)
                mSetLeftIn!!.setTarget(mCardFrontLayout)
                mSetRightOut!!.start()
                mSetLeftIn!!.start()

                // After all tiles are flipped and animation RV is GONE
                // Set front of the tile to transparent to prepare for second flip
                CoroutineScope(Main).launch {
                    delay((500 * columnNumber + 500 * ceil(columnNumber/2.0)).toLong())
                    front.setCardBackgroundColor(Color.TRANSPARENT)
                }
                false
            }
        }
    }
}