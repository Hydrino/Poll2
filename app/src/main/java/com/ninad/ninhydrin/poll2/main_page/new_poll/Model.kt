package com.ninad.ninhydrin.poll2.main_page.new_poll

import android.util.Log
import com.google.firebase.database.FirebaseDatabase
import com.ninad.ninhydrin.poll2.main_page.Poll
import io.reactivex.Single
import io.reactivex.SingleEmitter
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import java.util.concurrent.TimeUnit


class Model(private val modelToPresenter: MVP.ModelToPresenter) : MVP.PresenterToModel {

    private val database = FirebaseDatabase.getInstance()
    private var reference = database.reference.child("polls")
    private var disposable: Disposable? = null

    //------------------------------presenter -> model--------------------------------------------//

    override fun AddNewPollRequest(Title: String, Desc: String, RollNo: String, Date: String,
                                   Branch: String, Year: String) {

        Log.w("POLL2_new_poll", "in model")

        // create an observable that adds a new poll
        // success only if added successfully
        val addNewPollObs = getAddNewPollObs(Title, Desc, RollNo, Date, Branch, Year)

        // subscribe to this observable
        disposable = addNewPollObs.observeOn(AndroidSchedulers.mainThread()).subscribe({
            // successfully added new poll, pass to presenter
            Log.w("POLL2_new_poll", "success!")
            modelToPresenter.AddNewPollSuccess()

        }) { error ->
            Log.w("POLL2_new_poll", "Error in obs, printing stack trace...")
            error.printStackTrace()
            modelToPresenter.AddNewPollFailed("Network Error. Try again.")
        }
    }

    override fun unSub() {
        if (disposable != null && !(disposable as Disposable).isDisposed) {
            disposable?.dispose()
        }
    }

    //--------------------------------------------------------------------------------------------//

    private fun getAddNewPollObs(title: String, desc: String, rollNo: String, date: String,
                                 branch: String, year: String): Single<Unit> {

        return Single.create { emitter: SingleEmitter<Unit> ->

            reference = reference.child(year + branch).push()

            // create a new poll class from the info
            val poll = Poll()
            poll.Title = title
            poll.Desc = desc
            poll.OP = rollNo
            poll.Date = date
            poll.up = 0
            poll.down = 0
            poll.Branch = branch
            poll.Year = year

            reference.setValue(poll, { error, _ ->
                if (error != null)
                    emitter.onError(error.toException())
                else
                    emitter.onSuccess(Unit)
            })
        }.timeout(5, TimeUnit.SECONDS)
    }

    fun getPresenterToModel(): MVP.PresenterToModel = this
}