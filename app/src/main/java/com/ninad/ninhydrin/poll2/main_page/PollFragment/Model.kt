package com.ninad.ninhydrin.poll2.main_page.PollFragment

import android.util.Log
import com.google.firebase.database.*
import com.ninad.ninhydrin.poll2.main_page.Poll
import io.reactivex.Observable
import io.reactivex.ObservableEmitter
import io.reactivex.Single
import io.reactivex.SingleEmitter
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import java.util.concurrent.TimeUnit

class Model(private val modelToPresenter: MVP.ModelToPresenter) : MVP.PresenterToModel {

    private val database = FirebaseDatabase.getInstance()
    private var disposable: Disposable? = null

    //******************************** presenter -> model ****************************************//

    // 1 -> getting polls from database //

    override fun startReceivingPolls(isMyPolls: Boolean, RollNo: String, Branch: String,
                                     Year: String) {

        Log.w("Poll2_fragTrace", "in model")

        // set all the possible references the user can read from and write to
        val yearBranch = database.reference.child("polls").child(Year + Branch)
        val yearAll = database.reference.child("polls").child(Year + "All")
        val allBranch = database.reference.child("polls").child("All" + Branch)
        val allAll = database.reference.child("polls").child("AllAll")

        // get observables that return polls from above individual references
        // param isMyPoll determines whether to return all polls or my polls
        val allAllObs = getObs(allAll, isMyPolls, RollNo)
        val allBranchObs = getObs(allBranch, isMyPolls, RollNo)
        val yearAllObs = getObs(yearAll, isMyPolls, RollNo)
        val yearBranchObs = getObs(yearBranch, isMyPolls, RollNo)

        // merge all the above observables in one
        val mergedObservable = Observable.merge(allAllObs, allBranchObs, yearAllObs, yearBranchObs)

        // subscribe to this merged observable
        //** don't put a timeout on it, polls can come in anytime**//
        disposable = mergedObservable.observeOn(AndroidSchedulers.mainThread()).subscribe({ poll: Poll ->

            Log.w("POLL2_fragTrace", "${poll.Title} received in observer")
            modelToPresenter.newPoll(poll)

        }) { error ->
            Log.d("POLL2_fragTrace", "error in obs. Printing stack trace...")
            error.printStackTrace()
            modelToPresenter.getPollsFailure()
        }
    }

    private fun getObs(section: DatabaseReference, isMyPolls: Boolean, RollNo: String):
            Observable<Poll> {
        return Observable.create({ OuterEmitter: ObservableEmitter<Poll> ->

            var query = section.orderByChild("op").equalTo(RollNo)

            if (!isMyPolls) {
                query = section
            }

            query.addChildEventListener(object : ChildEventListener {

                override fun onCancelled(p0: DatabaseError) {
                    OuterEmitter.onError(p0.toException())
                }

                override fun onChildMoved(p0: DataSnapshot?, p1: String?) {
                }

                override fun onChildChanged(changedPoll: DataSnapshot?, p1: String?) {

                    val poll: Poll? = changedPoll?.getValue(Poll::class.java)

                    // poll stored in database does not contain the key in itself
                    poll?.Key = changedPoll?.key

                    // set references to the up/down voters list
                    val pollUpVotersRef: DatabaseReference? = changedPoll?.ref?.child("upvoters")
                    val pollDownVotersRef: DatabaseReference? = changedPoll?.ref?.child("downvoters")

                    // create a single obs that will return success only if found the
                    // user in the list
                    val upVoteCheckerSingle = getVoteCheckerSingle(pollUpVotersRef, RollNo)

                    upVoteCheckerSingle.subscribe({

                        poll?.isUpVotedByUser = true
                        if (poll != null)
                            modelToPresenter.childChanged(poll)

                    }) { error ->
                        // user has not upvoted or network error has occurred
                        // check both conditions
                        if (error.message == "User has not voted this poll.") {
                            val downVoteCheckerSingle = getVoteCheckerSingle(pollDownVotersRef,
                                    RollNo)

                            downVoteCheckerSingle.subscribe({

                                poll?.isDownVotedByUser = true
                                if (poll != null)
                                    modelToPresenter.childChanged(poll)
                            }) { downVoteError ->
                                if (downVoteError.message == "User has not voted this poll.") {
                                    if (poll != null)
                                        modelToPresenter.childChanged(poll)
                                }

                            }
                        } else {
                            Log.w("onChildChanged", "Network Error on UpVote Obs")
                        }
                    }

                }

                override fun onChildAdded(p0: DataSnapshot, p1: String?) {

                    /**
                     * When we get a new poll, check up/down voters list to see if user has
                     * up/down voted the poll
                     */
                    val poll: Poll? = p0.getValue(Poll::class.java)
                    Log.w("Poll2_fragTrace", "onChildAdded  " + poll?.Title)

                    if (poll == null)
                        OuterEmitter.onError(NullPointerException("Poll is null"))
                    else {

                        // add key to the poll
                        poll.Key = p0.key

                        // set references to the up/down voters list
                        val pollUpVotersRef: DatabaseReference? = p0.ref.child("upvoters")
                        val pollDownVotersRef: DatabaseReference? = p0.ref.child("downvoters")

                        // create a single obs that will return success only if found the
                        // user in the list
                        val upVoteCheckerSingle = getVoteCheckerSingle(pollUpVotersRef, RollNo)

                        upVoteCheckerSingle.subscribe({ _ ->
                            // user has upvoted this poll
                            // emit this poll now
                            Log.w("voteStatus", "${poll.Title} upvoted")
                            poll.isUpVotedByUser = true
                            OuterEmitter.onNext(poll)
                        }) { error ->

                            // user has not upvoted or network error has occurred
                            // check both conditions
                            if (error.message == "User has not voted this poll.") {

                                Log.w("voteStatus", " ${poll.Title} not upvoted")

                                val downVoteCheckerSingle = getVoteCheckerSingle(pollDownVotersRef,
                                        RollNo)

                                downVoteCheckerSingle.subscribe({ _ ->
                                    Log.w("voteStatus", "${poll.Title} downvoted")
                                    poll.isDownVotedByUser = true
                                    OuterEmitter.onNext(poll)

                                }) { downVoteError ->

                                    if (downVoteError.message == "User has not voted this poll.") {
                                        Log.w("voteStatus", "${poll.Title} not downvoted line 128")
                                        OuterEmitter.onNext(poll)
                                    } else {
                                        Log.w("voteStatus", "Network Error on DownVote Obs")
                                        OuterEmitter.onError(downVoteError)
                                    }
                                }
                            } else {
                                Log.w("voteStatus", "Network Error on UpVote Obs")
                                OuterEmitter.onError(error)
                            }
                        }

                    }
                }

                override fun onChildRemoved(p0: DataSnapshot?) {
                    if (p0 != null) {
                        Log.d("DeletePoll", "onChildRemoved")
                        modelToPresenter.pollRemoved(p0.key)
                    }
                }

            })
        })
    }

    private fun getVoteCheckerSingle(pollVotersRef: DatabaseReference?, rollNo: String):
            Single<Unit> {

        var count: Long = 0

        return Single.create({ emitter: SingleEmitter<Unit> ->
            pollVotersRef?.addListenerForSingleValueEvent(object : ValueEventListener {

                override fun onCancelled(p0: DatabaseError) {
                    emitter.onError(Exception("Network Error."))
                }

                override fun onDataChange(p0: DataSnapshot?) {

                    if (p0 == null) {
                        emitter.onError(Exception("Network Error"))
                    } else if (!p0.hasChildren()) {
                        emitter.onError(Exception("User has not voted this poll."))
                    } else {
                        for (item in p0.children) {
                            if (item.value == rollNo) {
                                emitter.onSuccess(Unit)
                                break
                            } else
                                count++
                        }

                        if (count == p0.childrenCount) {
                            emitter.onError(Exception("User has not voted this poll."))
                        }

                    }
                }
            })
        })
    }


    // 2 -> voting on a poll //

    // first we add a log on up voted/down voted poll
    // if it is successful, we update the up/down vote count
    override fun upVotePoll(RollNo: String, key: String, Year: String, Branch: String) {
        votePoll(true, RollNo, key, Year, Branch)
    }

    override fun downVotePoll(RollNo: String, key: String, Year: String, Branch: String) {
        votePoll(false, RollNo, key, Year, Branch)
    }

    // add a log on the poll
    private fun votePoll(isUpVoted: Boolean, RollNo: String, key: String, Year: String,
                         Branch: String) {
        var reference = database.reference.child("polls").child(Year + Branch).child(key)

        reference = if (isUpVoted) reference.child("upvoters").push() else reference.
                child("downvoters").push()

        // obs to add log on the poll
        val votePollObs = getAddLogObs(reference, RollNo)

        // subscribe to this observable
        votePollObs.observeOn(AndroidSchedulers.mainThread()).subscribe({ _ ->

            Log.w("votePoll", "Outer obs success.")
            // Adding log successful,proceed to update poll up/down vote count
            updateVoteCount(reference, isUpVoted, false, RollNo)

        }) {
            error ->
            Log.w("VotePoll", "Error in outer obs. Printing stack trace...")
            error.printStackTrace()
            modelToPresenter.votedFailed()
        }

    }

    // an observable that adds a log to the up/down voted poll
    // success if added successfully
    private fun getAddLogObs(reference: DatabaseReference, rollNo: String): Single<Unit> {

        return Single.create { emitter: SingleEmitter<Unit> ->

            reference.setValue(rollNo, { error, _ ->
                if (error != null)
                    emitter.onError(error.toException())
                else {
                    emitter.onSuccess(Unit)
                }
            })
        }.timeout(3, TimeUnit.SECONDS)
    }


    // updates the up/down vote count
    private fun updateVoteCount(reference: DatabaseReference, upVoted: Boolean, isUndo: Boolean,
                                RollNo: String) {

        var newRef = if (upVoted) reference.parent.parent.child("up") else reference.
                parent.parent.child("down")

        // creating a transaction to update up/down vote count
        val voteUpdateSingle = Single.create({ emitter: SingleEmitter<Unit> ->
            newRef.runTransaction(object : Transaction.Handler {

                override fun doTransaction(currentData: MutableData?): Transaction.Result {

                    if (currentData?.value == null || (currentData.value as Long) < 0f) {
                        currentData?.value = 0
                    }

                    currentData?.value = if (isUndo) ((currentData?.value) as Long - 1) else
                        ((currentData?.value) as Long + 1)
                    return Transaction.success(currentData)
                }

                override fun onComplete(p0: DatabaseError?, p1: Boolean, p2: DataSnapshot?) {
                    if (p0 != null) {
                        emitter.onError(p0.toException())
                    } else {
                        emitter.onSuccess(Unit)
                    }
                }

            })
        }).timeout(3, TimeUnit.SECONDS)

        voteUpdateSingle.observeOn(AndroidSchedulers.mainThread()).subscribe({ _ ->
            // do nothing on success
            // on child changed of polls will take care of it
            Log.w("votePoll", "Inner obs success.")
        }) { error ->
            Log.w("votePoll", "Error in inner obs.")
            error.printStackTrace()
            if (isUndo) {
                // gotta create the deleted poll again
                newRef = newRef.parent
                newRef = if (upVoted) newRef.child("upvoters").push() else
                    newRef.child("downvoters").push()

                val addLogObs = getAddLogObs(newRef, RollNo)
                addLogObs.observeOn(AndroidSchedulers.mainThread()).subscribe({
                    modelToPresenter.undoFailed()
                }) {

                }


                modelToPresenter.undoFailed()
            } else {
                // gotta delete the log
                newRef = newRef.parent
                newRef = if (upVoted) newRef.child("upvoters") else newRef.child("downvoters")
                val removeLogObs = getRemoveLogObs(newRef, RollNo)

                removeLogObs.observeOn(AndroidSchedulers.mainThread()).subscribe({
                    modelToPresenter.votedFailed()
                }) {
                }
            }
        }
    }

    // 3 -> Un subscribe from the observable
    override fun unSub() {
        if (disposable != null && disposable?.isDisposed == false)
            disposable?.dispose()
    }

    // 4 -> delete a poll
    override fun deletePoll(Year: String, Branch: String, Key: String) {

        // an observable to delete a poll
        val deletePollSingle = getDeletePollSingle(Year, Branch, Key)

        deletePollSingle.observeOn(AndroidSchedulers.mainThread()).subscribe({
            // do nothing, on child removed will take care of this
        }) {
            modelToPresenter.deletePollFailed()
        }
    }

    private fun getDeletePollSingle(year: String, branch: String, key: String): Single<Unit> {
        return Single.create { emitter: SingleEmitter<Unit> ->
            val reference = database.reference.child("polls").child(year + branch).child(key)
            reference.setValue(null, { error, _ ->
                if (error != null)
                    emitter.onError(error.toException())
                else
                    emitter.onSuccess(Unit)
            })
        }.timeout(4, TimeUnit.SECONDS)
    }

    // 5 - > Undo a vote
    override fun undoVote(Year: String, Branch: String, Key: String, RollNo: String,
                          isUpVoted: Boolean) {
        // first we remove the log on the poll
        // if it is successful, we proceed to decrease the vote count

        // get an observable to remove log on the poll
        // success if removed successfully
        var voteListRef = database.reference.child("polls").child(Year + Branch).child(Key)
        voteListRef = if (isUpVoted) voteListRef.child("upvoters")
        else
            voteListRef.child("downvoters")

        val removeLogObs = getRemoveLogObs(voteListRef, RollNo)

        removeLogObs.subscribe({
            // log removed successfully
            // proceed to decrease vote count
            updateVoteCount(voteListRef.push(), isUpVoted, true,RollNo)
        }) { error ->
            Log.w("Undo", "Error in removing log.")
            error.printStackTrace()
            modelToPresenter.undoFailed()
        }
    }

    private fun getRemoveLogObs(voteListRef: DatabaseReference, rollNo: String): Single<Unit> {
        return Single.create { emitter: SingleEmitter<Unit> ->

            voteListRef.addListenerForSingleValueEvent(object : ValueEventListener {

                override fun onCancelled(p0: DatabaseError) {
                    emitter.onError(p0.toException())
                }

                override fun onDataChange(p0: DataSnapshot?) {
                    if (p0 == null || !p0.hasChildren()) {
                        emitter.onError(NullPointerException())
                    } else {
                        for (log in p0.children) {
                            if (log.value == rollNo) {
                                log.ref.setValue(null)
                                emitter.onSuccess(Unit)
                                break
                            }
                        }
                    }
                }
            })

        }.timeout(4, TimeUnit.SECONDS)
    }

    //********************************************************************************************//

    fun getPresenterToModel(): MVP.PresenterToModel = this

}