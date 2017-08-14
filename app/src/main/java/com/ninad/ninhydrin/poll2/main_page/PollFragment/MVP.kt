package com.ninad.ninhydrin.poll2.main_page.PollFragment

import com.ninad.ninhydrin.poll2.main_page.Poll


class MVP {

    interface ViewToPresenter {
        fun viewCreated(isMyPolls: Boolean, RollNo: String, Branch: String, Year: String)
        fun upVoted(RollNo: String, key: String, Year: String, Branch: String)
        fun downVoted(RollNo: String, key: String, Year: String, Branch: String)
        fun unSub()
        fun deletePollClicked(Year: String?, Branch: String?, Key: String?)
        fun undoClicked(Year: String?, Branch: String?, Key: String?, RollNo: String?,
                        isUpVoted: Boolean)
    }

    interface PresenterToModel {
        fun startReceivingPolls(isMyPolls: Boolean, RollNo: String, Branch: String, Year: String)
        fun upVotePoll(RollNo: String, key: String, Year: String, Branch: String)
        fun downVotePoll(RollNo: String, key: String, Year: String, Branch: String)
        fun unSub()
        fun deletePoll(Year: String, Branch: String, Key: String)
        fun undoVote(Year: String, Branch: String, Key: String, RollNo: String, isUpVoted: Boolean)
    }

    interface ModelToPresenter {
        fun newPoll(poll: Poll)
        fun getPollsFailure(message: String = "Polls unavailable. Try again later.")
        fun unSub()
        fun pollRemoved(key: String)
        fun votedFailed(message: String = "Operation Failed. Check Network")
        fun childChanged(poll: Poll)
        fun deletePollFailed(message: String = "Couldn't delete poll. Try again later. ")
        fun undoFailed(message: String = "Operation Failed. Check Network")
    }

    interface PresenterToView {
        fun addNewPoll(poll: Poll)
        fun removePoll(key: String)
        fun showFailed(message: String)
        fun showVoteFailed(message: String)
        fun showChildChanged(poll: Poll)
        fun showDeletePollFailed(message: String)
        fun showUndoFailed(message: String)
    }
}