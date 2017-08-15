package com.ninad.ninhydrin.poll2.main_page.new_poll

import android.app.FragmentManager
import android.content.Context
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.Toast
import com.ninad.ninhydrin.poll2.R
import kotlinx.android.synthetic.main.new_poll_layout.*
import java.text.SimpleDateFormat
import java.util.*

class AddNewPollActivity : AppCompatActivity(), MVP.PresenterToView {

    // presenter
    private var presenter: Presenter? = null
    private var viewToPresenter: MVP.ViewToPresenter? = null

    // user roll no for poll info
    lateinit private var RollNo: String

    // Retain fragment
    private val RETAIN_FRAGMENT_TAG = "RetainFragTag"
    lateinit private var fragment_manager: FragmentManager


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.new_poll_layout)

        //initialise all the variables
        initVariables()

        // initialise the presenter
        initPresenter()

        // user should be able to add polls only for their branch or all branches.
        // Same with year.
        // So get user's year and branch and set spinner items accordingly
        // also get user's roll no for poll info
        getUserDetailsAndUpdateSpinners()

        // add click listener on button
        // call presenter on click
        val dateFormat = SimpleDateFormat("dd MMM yy", Locale.ENGLISH)
        val cal = Calendar.getInstance()

        new_poll_done_button.setOnClickListener {

            new_poll_done_button.isEnabled = false
            new_poll_title.isEnabled = false
            new_poll_desc.isEnabled = false
            Log.w("POLL2_new_poll", "button clicked")

            // get current date
            val date = dateFormat.format(cal.time)

            // pass values to presenter
            viewToPresenter?.DoneClicked(new_poll_title.text.toString(), new_poll_desc.text.toString(),
                    RollNo, date, new_poll_branch_spinner.selectedItem.toString(),
                    new_poll_year_spinner.selectedItem.toString())

        }

    }

    private fun initVariables() {
        fragment_manager = fragmentManager
    }

    private fun initPresenter() {
        var retainFrag: RetainFrag? = fragment_manager.findFragmentByTag(RETAIN_FRAGMENT_TAG) as RetainFrag?

        if (retainFrag == null) {
            retainFrag = RetainFrag()
            fragment_manager.beginTransaction().add(retainFrag, RETAIN_FRAGMENT_TAG).commit()

            presenter = Presenter()
            retainFrag.setPresenter(presenter as Presenter)
        } else
            presenter = retainFrag.getPresenter()

        presenter?.attach(this)
        viewToPresenter = presenter?.getViewToPresenter()
    }

    private fun getUserDetailsAndUpdateSpinners() {

        // getting values from GOD pref
        val GOD = getSharedPreferences(getString(R.string.god), Context.MODE_PRIVATE)
        val branch = GOD.getString(getString(R.string.branch), "")
        val year = GOD.getString(getString(R.string.year), "")
        RollNo = GOD.getString(getString(R.string.roll_no), "")

        // create arrays for spinner items
        val branches = listOf(branch, "All")
        val years = listOf(year, "All")

        // create adapters
        val branchAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, branches)
        val yearAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, years)

        // set adapters
        new_poll_branch_spinner.adapter = branchAdapter
        new_poll_year_spinner.adapter = yearAdapter

    }


    //---------------------------------presenter -> view------------------------------------------//
    override fun showSuccess() {
        Toast.makeText(applicationContext, "Your poll will be reviewed and added shortly.",
                Toast.LENGTH_SHORT).show()
        finish()
    }

    override fun showFailed(message: String) {
        Toast.makeText(applicationContext, message, Toast.LENGTH_LONG).show()
        new_poll_done_button.isEnabled = true
        new_poll_title.isEnabled = true
        new_poll_desc.isEnabled = true
    }

    override fun onDestroy() {
        presenter?.detach()
        super.onDestroy()
    }
}