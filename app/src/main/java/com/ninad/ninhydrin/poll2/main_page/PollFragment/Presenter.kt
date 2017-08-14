package com.ninad.ninhydrin.poll2.main_page.PollFragment

import android.util.Log
import com.ninad.ninhydrin.poll2.main_page.Poll


class Presenter : MVP.ViewToPresenter, MVP.ModelToPresenter {

    private var presenterToView: MVP.PresenterToView? = null
    private var presenterToModel: MVP.PresenterToModel? = null

    init {
        val model = Model(this)
        presenterToModel = model.getPresenterToModel()
    }

    //-------------------------------view -> presenter--------------------------------------------//
    override fun viewCreated(isMyPolls: Boolean, RollNo: String, Branch: String, Year: String) {
        Log.w("Poll2_fragTrace", "in presenter")
        if (RollNo == "" || Branch == "" || Year == "") {
            presenterToView?.showFailed("Fatal error. Uninstall app.")
            return
        }

        presenterToModel?.startReceivingPolls(isMyPolls, RollNo, Branch, Year)
    }

    override fun unSub() {
        presenterToModel?.unSub()
    }

    override fun upVoted(RollNo: String, key: String, Year: String, Branch: String) {
        if (RollNo == "" || key == "" || Year == "" || Branch == "")
            presenterToView?.showVoteFailed("Fatal error. Uninstall app.")
        else {
            presenterToModel?.upVotePoll(RollNo, key, Year, Branch)
        }
    }

    override fun downVoted(RollNo: String, key: String, Year: String, Branch: String) {
        if (RollNo == "" || key == "" || Year == "" || Branch == "")
            presenterToView?.showVoteFailed("Fatal error. Uninstall app.")
        else {
            presenterToModel?.downVotePoll(RollNo, key, Year, Branch)
        }
    }

    override fun deletePollClicked(Year: String?, Branch: String?, Key: String?) {
        if (Year != null && Branch != null && Key != null) {
            presenterToModel?.deletePoll(Year, Branch, Key)
        }
    }

    override fun undoClicked(Year: String?, Branch: String?, Key: String?, RollNo: String?,
                             isUpVoted: Boolean) {
        if (Year != null && Year != "" &&
                Branch != null && Branch != "" &&
                Key != null && Key != "" &&
                RollNo != null && RollNo != "") {
            presenterToModel?.undoVote(Year, Branch, Key, RollNo, isUpVoted)
        } else {
            presenterToView?.showUndoFailed("Fatal error. Uninstall app. ")
        }
    }

    //------------------------------model -> presenter--------------------------------------------//
    override fun newPoll(poll: Poll) {
        Log.w("Poll2_fragTrace", "successfully returned in presenter")
        presenterToView?.addNewPoll(poll)
    }

    override fun pollRemoved(key: String) {
        presenterToView?.removePoll(key)
    }

    override fun getPollsFailure(message: String) {
        Log.w("Poll2_fragTrace", "failure returned in presenter")
        presenterToView?.showFailed(message)
    }


    override fun votedFailed(message: String) {
        presenterToView?.showVoteFailed(message)
    }

    override fun childChanged(poll: Poll) {
        presenterToView?.showChildChanged(poll)
    }

    override fun deletePollFailed(message: String) {
        presenterToView?.showDeletePollFailed(message)
    }

    override fun undoFailed(message: String) {
        presenterToView?.showUndoFailed(message)
    }


    // ------------------------------life cycle methods-------------------------------------------//
    fun attach(p: MVP.PresenterToView) {
        presenterToView = p
    }

    fun detach() {
        presenterToView = null
    }

    fun getViewToPresenter(): MVP.ViewToPresenter = this

}