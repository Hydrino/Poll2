package com.ninad.ninhydrin.poll2.main_page.PollFragment.PollInfoDialog

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.support.animation.DynamicAnimation
import android.support.animation.SpringAnimation
import android.support.animation.SpringForce
import android.support.constraint.ConstraintLayout
import android.support.v4.content.ContextCompat
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageButton
import com.ninad.ninhydrin.poll2.R
import com.ninad.ninhydrin.poll2.main_page.Poll
import com.ninad.ninhydrin.poll2.main_page.PollFragment.MVP
import kotlinx.android.synthetic.main.poll_info_dialog.view.*


class PollInfoDialog(private val dialogContext: Context, private val poll: Poll,
                     private val isMyPolls: Boolean,
                     private val RollNo: String,
                     private val viewToPresenter: MVP.ViewToPresenter?) : Dialog(dialogContext) {

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)

        // set custom white rectangle background for the dialog
        window.decorView.background = ContextCompat.getDrawable(dialogContext, R.drawable.poll_info_dialog_background)

        // inflate dialog view
        val view = LayoutInflater.from(dialogContext).inflate(R.layout.poll_info_dialog,
                null, false)

        // set params for the view
        val params = ConstraintLayout.LayoutParams(500,
                ConstraintLayout.LayoutParams.WRAP_CONTENT)

        // set dialog properties
        setDialogInformation(view)

        setContentView(view, params)
    }

    private fun setDialogInformation(view: View) {
        view.poll_info_title.text = poll.Title
        view.poll_info_date.text = poll.Date
        view.poll_info_desc.text = poll.Desc
        view.upVotes.text = poll.up.toString()
        view.downVotes.text = poll.down.toString()
        view.poll_info_delete.visibility = if (isMyPolls) View.VISIBLE else View.GONE
        view.poll_info_undo.setColorFilter(ContextCompat.getColor(dialogContext,
                android.R.color.holo_red_dark))
        view.poll_info_undo.isEnabled = true

        if (!(poll.up == 0 && poll.down == 0)) {
            view.poll_info_progress_bar.progress = poll.up as Int * 100 / (poll.up as Int + poll.down as Int)
        }

        if (poll.isUpVotedByUser && !poll.isDownVotedByUser) {
            view.poll_info_thumbs_up.isEnabled = false
            view.poll_info_thumbs_down.isEnabled = false

            view.poll_info_thumbs_up.setColorFilter(ContextCompat.getColor(dialogContext,
                    R.color.thumbs_up))

            view.poll_info_thumbs_down.setColorFilter(ContextCompat.getColor(dialogContext,
                    android.R.color.darker_gray))

        } else if (poll.isDownVotedByUser && !poll.isUpVotedByUser) {
            view.poll_info_thumbs_up.isEnabled = false
            view.poll_info_thumbs_down.isEnabled = false

            view.poll_info_thumbs_up.setColorFilter(ContextCompat.getColor(dialogContext,
                    android.R.color.darker_gray))

            view.poll_info_thumbs_down.setColorFilter(ContextCompat.getColor(dialogContext,
                    R.color.thumbs_down))
        } else {
            view.poll_info_undo.isEnabled = false
        }

        view.poll_info_thumbs_up.setOnClickListener {
            viewToPresenter?.upVoted(RollNo, poll.Key ?: "", poll.Year ?: "",
                    poll.Branch ?: "")
            dismiss()
        }

        view.poll_info_thumbs_down.setOnClickListener {

            viewToPresenter?.downVoted(RollNo, poll.Key ?: "", poll.Year ?: "",
                    poll.Branch ?: "")
            dismiss()
        }

        view.poll_info_delete.setOnClickListener {
            dismiss()
            viewToPresenter?.deletePollClicked(poll.Year, poll.Branch, poll.Key)
        }

        view.poll_info_undo.setOnClickListener {

            viewToPresenter?.undoClicked(poll.Year, poll.Branch, poll.Key, RollNo,
                    poll.isUpVotedByUser)
            dismiss()
        }
    }

}