package com.ninad.ninhydrin.poll2.sign_up


class Presenter : MVP.ViewToPresenter, MVP.ModelToPresenter {

    private var presenterToView: MVP.PresenterToView? = null
    private var presenterToModel: MVP.PresenterToModel? = null

    init {
        val model = Model(this)
        presenterToModel = model.getPresenterToModel()
    }


    //--------------------view -> presenter----------------------------//
    override fun ButtonClicked(Roll_No: String, Branch: String, Year: String) {

        // if roll no is not valid, show error in view
        if (Roll_No.length != 9)
            presenterToView?.showFailed("Invalid Roll No")
        else
        // else pass to model
            presenterToModel?.signUpUser(Roll_No, Branch, Year)

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