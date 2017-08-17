package com.ninad.ninhydrin.poll2.sign_up

class MVP {

    // we reuse this activity for updating info too
    // we pass the old info for deletion
    interface ViewToPresenter {
        fun ButtonClicked(Roll_No: String, Branch: String, Year: String,
                          OldRoll_No: String?, OldBranch: String?, OldYear: String?)
    }

    interface PresenterToModel {
        fun signUpUser(Roll_No: String, Branch: String, Year: String,
                       OldRoll_No: String?, OldBranch: String?, OldYear: String?)
        fun unSub()
    }

    interface ModelToPresenter {
        fun signUpFailed(message: String = "Network error. Please try again.")
        fun userAlreadyRegistered(message: String = "Roll No already registered.")
        fun signUpSuccess(Roll_No: String, Branch: String, Year: String)
    }

    interface PresenterToView {
        fun showFailed(message: String)
        fun showSuccess(Roll_No: String, Branch: String, Year: String)
    }

}