package com.ninad.ninhydrin.poll2.sign_up

import android.util.Log
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import io.reactivex.Single
import io.reactivex.SingleEmitter
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import java.util.concurrent.TimeUnit


class Model(private val modelToPresenter: MVP.ModelToPresenter) : MVP.PresenterToModel {

    private var outerDisposable: Disposable? = null
    private var innerDisposable: Disposable? = null

    private val database: FirebaseDatabase = FirebaseDatabase.getInstance()

    //-----------------------presenter -> model-----------------------------//

    override fun signUpUser(Roll_No: String, Branch: String, Year: String,
                            OldRoll_No: String?, OldBranch: String?, OldYear: String?) {

        // first of all, query all the roll numbers to see if
        // input roll no is unique or not
        // proceed to add user only if it is unique

        // an observable to check if input roll_no is unique
        // emits success only if it is unique
        val queryRollNoObservable: Single<Unit> = getQueryRollNoObservable(Roll_No, Branch, Year)

        // subscribe on this "outer" observable
        outerDisposable = queryRollNoObservable.observeOn(AndroidSchedulers.mainThread()).subscribe({ _ ->

            // roll_no is unique, proceed to add user
            signUpNewUser(Roll_No, Branch, Year, OldRoll_No, OldBranch, OldYear)

        }) { error ->

            Log.w("POLL2_error", "error from outer observable. Printing stack trace.....")
            error.printStackTrace()

            // if error is due to non-unique roll no
            // pass presenter method accordingly
            if (error.message?.startsWith("Roll No") ?: false)
                modelToPresenter.userAlreadyRegistered()
            else
                modelToPresenter.signUpFailed()
        }

    }

    override fun unSub() {
        Log.w("poll2_sign_up", "reached model")
        if (outerDisposable != null && !(outerDisposable as Disposable).isDisposed) {
            Log.w("POLL2_sign_up", "Disposing outer observable")
            outerDisposable?.dispose()
        }

        if (innerDisposable != null && !(innerDisposable as Disposable).isDisposed) {
            Log.w("POLL2_sign_up", "Disposing outer observable")
            innerDisposable?.dispose()
        }

    }

    //--------------------------------------------------------------------------------------------//

    // returns an observable to check uniqueness of roll_no
    // emits success only if unique
    private fun getQueryRollNoObservable(roll_No: String, branch: String, year: String): Single<Unit> {

        // query all the roll numbers from input year and branch
        return Single.create({ emitter: SingleEmitter<Unit> ->

            val query = database.reference.child("users").child(year + branch)

            // listen only once
            query.addListenerForSingleValueEvent(object : ValueEventListener {

                override fun onDataChange(p0: DataSnapshot) {
                    Log.w("POLL2_info", "user count : " + p0.childrenCount.toString())

                    // if no users have signed up
                    // it's the first user in that year and branch
                    if (p0.childrenCount.toInt() == 0)
                        emitter.onSuccess(Unit)
                    else {

                        Log.w("POLL2_info", "not the first user in this branch and year")

                        // iterate over users
                        // if found a match, emit error
                        for (snapshot in p0.children) {
                            Log.w("POLL2_users", snapshot.value.toString())
                            if (roll_No == snapshot.value.toString()) {
                                emitter.onError(Exception("Roll No already registered"))
                                return
                            }
                        }

                        // no match found, emit success!
                        emitter.onSuccess(Unit)

                    }
                }

                override fun onCancelled(p0: DatabaseError) {
                    emitter.onError(p0.toException())
                }

            })
        }).timeout(5, TimeUnit.SECONDS)
    }

    private fun signUpNewUser(Roll_No: String, Branch: String, Year: String,
                              OldRoll_No: String?, OldBranch: String?, OldYear: String?) {

        // an observable to add new user
        val signUpObservable: Single<Unit> = getObservable(Roll_No, Branch, Year)

        innerDisposable = signUpObservable.observeOn(AndroidSchedulers.mainThread()).subscribe({ _ ->

            Log.w("update","Created new info successfully")
            if (OldRoll_No != null && OldBranch != null && OldYear != null) {
                Log.w("update", "this is an update operation")

                //adding the updated info is successful
                // so we proceed to delete the previous info

                // get an obs to delete the previous info
                // success only if deleted successfully
                val deleteInfoObs: Single<Unit> = getDeleteInfoObs(OldRoll_No, OldYear, OldBranch)

                // subscribe to this observable
                deleteInfoObs.observeOn(AndroidSchedulers.mainThread()).subscribe({
                    // deleting previous user info is successful
                    Log.w("update", "deleted previous user info successfully!")
                    modelToPresenter.signUpSuccess(Roll_No, Branch, Year)
                }) {
                    // deleting previous user info failed
                    // but we already have created an entry with new data
                    // gotta delete that now
                    Log.w("update", "Deleting previous user info failed!")
                    val deleteNewlyCreatedInfo = getDeleteInfoObs(Roll_No, Year, Branch)
                    deleteNewlyCreatedInfo.observeOn(AndroidSchedulers.mainThread()).subscribe({
                        Log.w("update", "Deleted newly created info Successfully")
                    }) {

                    }
                    modelToPresenter.signUpFailed()
                }

            } else

                modelToPresenter.signUpSuccess(Roll_No, Branch, Year)

        }) { error ->
            Log.w("POLL2_error", "error from inner observable. Printing stack trace.....")
            error.printStackTrace()
            modelToPresenter.signUpFailed()
        }
    }

    private fun getDeleteInfoObs(oldRoll_No: String, oldYear: String, oldBranch: String): Single<Unit> {
        return Single.create({
            emitter: SingleEmitter<Unit> ->
            val reference = database.reference.child("users").child(oldYear + oldBranch)

            reference.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onCancelled(p0: DatabaseError) {
                    emitter.onError(p0.toException())
                }

                override fun onDataChange(p0: DataSnapshot?) {
                    if (p0 == null || !p0.hasChildren()) {
                        emitter.onError(NullPointerException("Old Roll No does not exits. Line 161"))
                    } else {
                        for (item in p0.children) {
                            if (item.value == oldRoll_No) {
                                Log.w("update", "old info found. Deleting...")
                                item.ref.removeValue({ error, _ ->
                                    if (error != null)
                                        emitter.onError(error.toException())
                                    else
                                        emitter.onSuccess(Unit)
                                })
                            }
                        }
                    }
                }

            })
        }).timeout(3, TimeUnit.SECONDS)
    }

    // returns an observable that adds the new user and emits values
    private fun getObservable(Roll_No: String, Branch: String, Year: String): Single<Unit> {

        return Single.create({ emitter: SingleEmitter<Unit> ->

            // getting a reference to push new user
            val reference = database.reference.child("users").child(Year + Branch).push()

            // add the new user
            reference.setValue(Roll_No, { databaseError, _ ->
                if (databaseError != null)
                    emitter.onError(databaseError.toException())
                else
                    emitter.onSuccess(Unit)
            })
        }).timeout(5, TimeUnit.SECONDS) // max limit 5 sec
        // emits a timeout exception after timeout expires
    }

    //----------------- return instance-------------------//
    fun getPresenterToModel(): MVP.PresenterToModel = this

}