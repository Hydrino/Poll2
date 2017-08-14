package com.ninad.ninhydrin.poll2.main_page.PollFragment

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import com.ninad.ninhydrin.poll2.R
import com.ninad.ninhydrin.poll2.main_page.Poll


class PollsRecyclerAdapter(private val context: Context, private val polls: ArrayList<Poll>,
                           private val onItemClickListener: onRecyclerItemClickListener) :
        RecyclerView.Adapter<PollsRecyclerViewHolder>() {

    interface onRecyclerItemClickListener {
        fun onItemClick(position: Int)
        fun onThumbsUpClicked(position: Int,thumbs_up:ImageButton,thumbs_down:ImageButton)
        fun onThumbsDownClicked(position: Int,thumbs_up: ImageButton,thumbs_down:ImageButton)
    }


    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): PollsRecyclerViewHolder {
        val view: View = LayoutInflater.from(context).inflate(R.layout.poll_row_layout, parent,
                false)
        return PollsRecyclerViewHolder(view)
    }

    override fun onBindViewHolder(holder: PollsRecyclerViewHolder, position: Int) {
        holder.bind(position, polls[position], onItemClickListener)
    }


    override fun getItemCount(): Int {
        return polls.size
    }

    override fun getItemViewType(position: Int): Int {
        return position
    }
}