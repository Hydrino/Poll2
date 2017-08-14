package com.ninad.ninhydrin.poll2.main_page.new_poll

import android.util.Log


class Presenter : MVP.ViewToPresenter, MVP.ModelToPresenter {

    private var presenterToView: MVP.PresenterToView? = null
    private var presenterToModel: MVP.PresenterToModel? = null

    init {
        val model = Model(this)
        presenterToModel = model.getPresenterToModel()
    }

    //-------------------------------view -> presenter--------------------------------------------//
    override fun DoneClicked(Title: String, Desc: String, RollNo: String, Date: String,
                             Branch: String, Year: String) {

        Log.w("POLL2_new_poll", "in presenter")
        if (Title == "") {
            presenterToView?.showFailed("Please enter title")
            return
        }

        if (RollNo == "") {
            presenterToView?.showFailed("Fatal error. Uninstall app now!")
            return
        }

        presenterToModel?.AddNewPollRequest(Title, Desc, RollNo, Date, Branch, Year)

    }


    //--------------------------------model -> presenter------------------------------------------//
    override fun AddNewPollFailed(message: String) {
        presenterToView?.showFailed(message)
    }

    override fun AddNewPollSuccess() {
        presenterToModel?.unSub()
        presenterToView?.showSuccess()
    }

    //----------------------------------life cycle methods----------------------------------------//

    fun attach(p: MVP.PresenterToView) {
        presenterToView = p
    }

    fun detach() {
        presenterToView = null
    }

    fun getViewToPresenter(): MVP.ViewToPresenter = this

}