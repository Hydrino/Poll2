package com.ninad.ninhydrin.poll2.main_page.PollFragment

import android.app.Fragment
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.Toast
import com.ninad.ninhydrin.poll2.R
import com.ninad.ninhydrin.poll2.main_page.Poll
import com.ninad.ninhydrin.poll2.main_page.PollFragment.PollInfoDialog.PollInfoDialog
import kotlinx.android.synthetic.main.polls_list_layout.*

/**
 * Setting this as a retained fragment
 *
 * On every activity recreation :
 * onAttach()   -> called
 *      onCreate()   -> not called
 *      .
 *      .
 *      .
 *      .
 *      onDestroy()  -> not called
 * onDetach()   -> called
 **/

// thus, onDestroy() will be called when we are exiting for real!

class PollFragment : Fragment(), MVP.PresenterToView, PollsRecyclerAdapter.onRecyclerItemClickListener {


    // information that we will get as arguments
    private var isMyPolls: Boolean = false
    private var RollNo: String = ""
    private var Branch: String = ""
    private var Year: String = ""
    private var ThumbsUp: ImageButton? = null
    private var ThumbsDown: ImageButton? = null

    private var presenter: Presenter? = null
    private var viewToPresenter: MVP.ViewToPresenter? = null

    private var recyclerViewAdapter: PollsRecyclerAdapter? = null
    private var polls: ArrayList<Poll>? = null

    // on create will be called only once when the fragment is initiated
    // and not on activity recreations
    // ideal place to initialise presenter
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // get arguments from main activity regarding the type of fragment
        isMyPolls = arguments.getBoolean(getString(R.string.is_my_polls))
        RollNo = arguments.getString(getString(R.string.roll_no))
        Branch = arguments.getString(getString(R.string.branch))
        Year = arguments.getString(getString(R.string.year))

        // set retain instance to true
        retainInstance = true

        Log.w("POLL2_frag", isMyPolls.toString())

        //initialise the presenter
        presenter = Presenter()
        viewToPresenter = presenter?.getViewToPresenter()

    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.polls_list_layout, container, false)
    }

    // initialise the recycler view and let presenter know
    // that we are ready to receive polls
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        polls_recycler_view.setHasFixedSize(true)
        polls_recycler_view.layoutManager = LinearLayoutManager(activity)

        // passing empty array list to recycler adapter
        // we will later add items from database
        polls = ArrayList()
        recyclerViewAdapter = PollsRecyclerAdapter(view.context, polls as ArrayList<Poll>, this)

        polls_recycler_view.adapter = recyclerViewAdapter

        // notify the presenter that we are ready to take in recycler view items(polls)
        Log.w("Poll2_fragTrace", "called presenter")
        viewToPresenter?.viewCreated(isMyPolls, RollNo, Branch, Year)

    }

    //----------------------------------life cycle callbacks--------------------------------------//

    // we attach the presenter here
    override fun onResume() {
        Log.d("Poll2_fragTrace", "on resume")
        presenter?.attach(this)
        super.onResume()
    }

    // detach the presenter here
    override fun onPause() {
        Log.d("Poll2_fragTrace", "on paused")
        presenter?.detach()
        super.onPause()
    }


    override fun onDestroy() {
        viewToPresenter?.unSub()
        Log.w("POLL2_frag", "destroyed")
        super.onDestroy()
    }

    // -------------------------------presenter -> view-------------------------------------------//

    // received a new poll from database
    override fun addNewPoll(poll: Poll) {
        Log.w("Poll2_fragTrace", "Added new poll successfully")
        polls?.add(0, poll)
        recyclerViewAdapter?.notifyItemInserted(0)
        recyclerViewAdapter?.notifyItemRangeChanged(0, polls?.size ?: 0)
        polls_recycler_view.scrollToPosition(0)
    }

    // receiving poll failed
    override fun showFailed(message: String) {
        // timeout exception will always occur
        // so show it only when we really couldn't fetch polls
        if (polls?.size == 0)
            Toast.makeText(activity, message, Toast.LENGTH_SHORT).show()
    }

    // a poll has been deleted by it's op
    override fun removePoll(key: String) {
        if (polls == null) return

        for (i in (polls as ArrayList<Poll>).indices) {
            if ((polls as ArrayList<Poll>)[i].Key == key) {
                (polls as ArrayList<Poll>).removeAt(i)
                recyclerViewAdapter?.notifyItemRemoved(i)
                recyclerViewAdapter?.notifyItemRangeChanged(i, (polls as ArrayList<Poll>).size - i)
                Log.d("DeletePoll", "Poll removed")
                break
            }
        }
    }

    // up voting or down voting failed
    override fun showVoteFailed(message: String) {
        Toast.makeText(activity, message, Toast.LENGTH_SHORT).show()
        ThumbsUp?.isEnabled = true
        ThumbsDown?.isEnabled = false
    }

    override fun showChildChanged(poll: Poll) {

        val key = poll.Key
        if (polls == null)
            return

        for (index in (polls as ArrayList).indices) {
            if ((polls as ArrayList<Poll>)[index].Key == key) {
                (polls as ArrayList<Poll>)[index] = poll
                Log.w("onChildChanged", "poll updated $index")

                recyclerViewAdapter?.notifyItemChanged(index)
            }
        }
    }

    override fun showDeletePollFailed(message: String) {
        Toast.makeText(activity, message, Toast.LENGTH_SHORT).show()
    }

    override fun showUndoFailed(message: String) {
        Toast.makeText(activity, message, Toast.LENGTH_SHORT).show()
    }

    //-----------------------------recycler view click listener-----------------------------------//

    override fun onItemClick(position: Int) {
        if (polls?.get(position) != null) {
            val infoDialog = PollInfoDialog(activity, (polls as ArrayList<Poll>)[position], isMyPolls, RollNo, viewToPresenter)
            infoDialog.show()
        }
    }

    override fun onThumbsUpClicked(position: Int, thumbs_up: ImageButton,
                                   thumbs_down: ImageButton) {

        ThumbsUp = thumbs_up
        ThumbsDown = thumbs_down

        thumbs_up.isEnabled = false
        thumbs_down.isEnabled = false

        viewToPresenter?.upVoted(RollNo, polls?.get(position)?.Key as String,
                polls?.get(position)?.Year as String, polls?.get(position)?.Branch as String)
    }

    override fun onThumbsDownClicked(position: Int, thumbs_up: ImageButton,
                                     thumbs_down: ImageButton) {
        ThumbsUp = thumbs_up
        ThumbsDown = thumbs_down

        thumbs_up.isEnabled = false
        thumbs_down.isEnabled = false

        viewToPresenter?.downVoted(RollNo, polls?.get(position)?.Key as String,
                polls?.get(position)?.Year as String, polls?.get(position)?.Branch as String)
    }

}