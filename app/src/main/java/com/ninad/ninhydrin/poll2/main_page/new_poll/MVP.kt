package com.ninad.ninhydrin.poll2.main_page.new_poll


class MVP {

    interface ViewToPresenter {
        fun DoneClicked(Title: String, Desc: String, RollNo: String, Date: String,
                        Branch: String, Year: String)
    }

    interface PresenterToModel {
        fun AddNewPollRequest(Title: String, Desc: String, RollNo: String, Date: String,
                              Branch: String, Year: String)

        fun unSub()
    }

    interface ModelToPresenter {
        fun AddNewPollFailed(message: String)
        fun AddNewPollSuccess()
    }

    interface PresenterToView {
        fun showSuccess()
        fun showFailed(message: String)
    }

}