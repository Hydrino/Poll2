package com.ninad.ninhydrin.poll2.sign_up

import android.util.Log


class Presenter : MVP.ViewToPresenter, MVP.ModelToPresenter {

    private var presenterToView: MVP.PresenterToView? = null
    private var presenterToModel: MVP.PresenterToModel? = null

    init {
        val model = Model(this)
        presenterToModel = model.getPresenterToModel()
    }


    //--------------------view -> presenter----------------------------//
    override fun ButtonClicked(Roll_No: String, Branch: String, Year: String, OldRoll_No: String?,
                               OldBranch: String?, OldYear: String?) {

        // if roll no is not valid, show error in view
        if (Roll_No.length != 9)
            presenterToView?.showFailed("Invalid Roll No")
        // if old data and new data are same, convey to user
        else if (Roll_No == OldRoll_No && Year == OldYear && Branch == OldBranch) {
            Log.w("update", "same info entered again")
            presenterToView?.showFailed("Same data entered as before.")
        } else {
            // else pass to model
            Log.w("update", "sending to model")
            presenterToModel?.signUpUser(Roll_No, Branch, Year, OldRoll_No, OldBranch, OldYear)

        }
    }

    //--------------------model -> presenter------------------------//
    override fun signUpFailed(message: String) {
        presenterToView?.showFailed(message)
    }

    override fun signUpSuccess(Roll_No: String, Branch: String, Year: String) {
        presenterToModel?.unSub()
        presenterToView?.showSuccess(Roll_No, Branch, Year)

    }

    override fun userAlreadyRegistered(message: String) {
        presenterToView?.showFailed(message)
    }

    // -------------------lifecycle methods-------------------------//

    fun attach(p: MVP.PresenterToView) {
        presenterToView = p
    }

    fun detach() {
        presenterToView = null
    }

    // -------------------return instance--------//
    fun getViewToPresenter(): MVP.ViewToPresenter = this


}