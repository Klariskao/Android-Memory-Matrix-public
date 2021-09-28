package com.example.memorymatrix

import android.animation.AnimatorInflater
import android.animation.AnimatorSet
import android.content.Context
import android.content.res.Resources
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.cardview.widget.CardView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView

class TilesAdapter(columnNumber: Int, rowNumber: Int, private var tilesToBeCorrect: List<Int>, private val recyclerViewClickListener: RecyclerViewClickListener)
    : RecyclerView.Adapter<TilesAdapter.AndroidVersionRecyclerViewHolder>() {

    lateinit var mParent: ViewGroup
    var tilesNumber = columnNumber * rowNumber

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AndroidVersionRecyclerViewHolder {
        mParent = parent
        return AndroidVersionRecyclerViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.tile, parent, false))
    }

    // Get number of items
    override fun getItemCount(): Int = tilesNumber

    // Loop through items
    override fun onBindViewHolder(holder: AndroidVersionRecyclerViewHolder, position: Int) {
        holder.bind(mParent, position, tilesToBeCorrect, recyclerViewClickListener)
    }

    class AndroidVersionRecyclerViewHolder(private val view: View): RecyclerView.ViewHolder(view){
        private val tile: ConstraintLayout = view.findViewById(R.id.tileLayout)

        fun bind(parent: ViewGroup, position: Int, tilesToBeCorrect: List<Int>, recyclerViewClickListener: RecyclerViewClickListener){
            // Correct tile
            if (position in tilesToBeCorrect) {
                // Set back and front views and hide the unused one
                val front = view.findViewById<CardView>(R.id.tileFront)
                val back = view.findViewById<CardView>(R.id.tileBack)
                view.findViewById<CardView>(R.id.tileWrongFront).visibility = View.GONE
                // Assign back and front
                findViews(front, back)
                // On correct tile click
                tile.setOnClickListener {
                    flipCard()
                    recyclerViewClickListener.onTileClickAction(position)
                }
            }
            // Wrong tile
            else{
                // Set back and front views and hide the unused one
                val front = view.findViewById<CardView>(R.id.tileWrongFront)
                val back = view.findViewById<CardView>(R.id.tileBack)
                view.findViewById<CardView>(R.id.tileFront).visibility = View.GONE
                // Assign back and front
                findViews(front, back)
                // On wrong tile click
                tile.setOnClickListener {
                    flipCard()
                    recyclerViewClickListener.onTileClickAction(position)
                }
            }

            changeCameraDistance(parent.resources)
            loadAnimations(parent.context)
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

        private fun flipCard() {
            mIsFrontVisible = if (!mIsFrontVisible) {
                mSetRightOut!!.setTarget(mCardFrontLayout)
                mSetLeftIn!!.setTarget(mCardBackLayout)
                mSetRightOut!!.start()
                mSetLeftIn!!.start()
                true
            } else {
                mSetRightOut!!.setTarget(mCardBackLayout)
                mSetLeftIn!!.setTarget(mCardFrontLayout)
                mSetRightOut!!.start()
                mSetLeftIn!!.start()
                false
            }
        }
    }
}

