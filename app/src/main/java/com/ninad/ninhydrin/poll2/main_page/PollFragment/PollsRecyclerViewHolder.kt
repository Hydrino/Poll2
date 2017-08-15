package com.ninad.ninhydrin.poll2.main_page.PollFragment

import android.support.animation.DynamicAnimation
import android.support.animation.SpringAnimation
import android.support.animation.SpringForce
import android.support.v4.content.ContextCompat
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.View
import android.widget.ImageButton
import android.widget.ProgressBar
import android.widget.TextView
import com.ninad.ninhydrin.poll2.R
import com.ninad.ninhydrin.poll2.main_page.Poll
import kotlinx.android.synthetic.main.poll_row_layout.view.*
import java.util.*

class PollsRecyclerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

    private var title: TextView? = null
    private var date: TextView? = null
    private var yearBranch: TextView? = null
    private var up = 0
    private var down = 0
    private var progressBar: ProgressBar? = null


    init {
        title = itemView.row_layout_poll_title
        date = itemView.row_layout_date
        progressBar = itemView.row_layout_progress_bar
        yearBranch = itemView.row_layout_year_branch
    }

    fun bind(position: Int, poll: Poll, onRecyclerItemClickListener:
    PollsRecyclerAdapter.onRecyclerItemClickListener) {

        title?.text = poll.Title
        date?.text = poll.Date
        yearBranch?.text = String.format(Locale.ENGLISH, "%s %s", poll.Year, poll.Branch)

        up = if (poll.up == null || (poll.up as Int) < 0) 0 else poll.up as Int
        down = if (poll.down == null || (poll.down as Int) < 0) 0 else poll.down as Int

        if (!(up == 0 && down == 0)) {
            progressBar?.progress = up * 100 / (up + down)
        } else {
            progressBar?.progress = 50
        }


        // gotta do all this extra work because of some bug in recycler view that fills both buttons
        // in color
        if (poll.isUpVotedByUser && !poll.isDownVotedByUser) {

            Log.w("onChildChanged", "${poll.Title} is up voted")
            itemView.row_layout_thumbs_up.setColorFilter(ContextCompat.getColor(itemView.context,
                    R.color.thumbs_up))

            itemView.row_layout_thumbs_down.setColorFilter(ContextCompat.getColor(itemView.context,
                    android.R.color.darker_gray))

            itemView.row_layout_thumbs_down.isEnabled = false
            itemView.row_layout_thumbs_up.isEnabled = false

        } else if (poll.isDownVotedByUser && !poll.isUpVotedByUser) {

            Log.w("onChildChanged", "${poll.Title} is down voted")
            itemView.row_layout_thumbs_down.setColorFilter(ContextCompat.getColor(itemView.context,
                    R.color.thumbs_down))

            itemView.row_layout_thumbs_up.setColorFilter(ContextCompat.getColor(itemView.context,
                    android.R.color.darker_gray))

            itemView.row_layout_thumbs_down.isEnabled = false
            itemView.row_layout_thumbs_up.isEnabled = false
        } else {
            itemView.row_layout_thumbs_up.setColorFilter(ContextCompat.getColor(itemView.context,
                    android.R.color.darker_gray))

            itemView.row_layout_thumbs_down.setColorFilter(ContextCompat.getColor(itemView.context,
                    android.R.color.darker_gray))

            itemView.row_layout_thumbs_down.isEnabled = true
            itemView.row_layout_thumbs_up.isEnabled = true

        }

        itemView.setOnClickListener {
            onRecyclerItemClickListener.onItemClick(position)
        }

        itemView.row_layout_thumbs_up.setOnClickListener {

            animateThumb(itemView.row_layout_thumbs_up)

            onRecyclerItemClickListener.onThumbsUpClicked(position, itemView.row_layout_thumbs_up,
                    itemView.row_layout_thumbs_down)
        }

        itemView.row_layout_thumbs_down.setOnClickListener {

            animateThumb(itemView.row_layout_thumbs_down)

            onRecyclerItemClickListener.onThumbsDownClicked(position, itemView.row_layout_thumbs_up,
                    itemView.row_layout_thumbs_down)
        }
    }

    private fun animateThumb(row_layout_thumb: ImageButton?) {
        val springAnimX = SpringAnimation(row_layout_thumb, DynamicAnimation.SCALE_X,
                1f)
        val springAnimY = SpringAnimation(row_layout_thumb, DynamicAnimation.SCALE_Y,
                1f)

        springAnimX.setStartValue(1.5f)
        springAnimY.setStartValue(1.5f)

        springAnimX.spring.stiffness = SpringForce.STIFFNESS_LOW
        springAnimY.spring.stiffness = SpringForce.STIFFNESS_LOW

        springAnimX.start()
        springAnimY.start()
    }
}
