package com.ninad.ninhydrin.poll2.sign_up

import android.app.FragmentManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.widget.Toast
import com.ninad.ninhydrin.poll2.R
import com.ninad.ninhydrin.poll2.main_page.MainActivity
import kotlinx.android.synthetic.main.sign_up_layout.*


class SignUpActivity : AppCompatActivity(), MVP.PresenterToView {

    private val RETAIN_FRAGMENT_TAG = "RetainFragTag"
    private var presenter: Presenter? = null
    private var viewToPresenter: MVP.ViewToPresenter? = null
    lateinit private var fragment_manager: FragmentManager

    // all the variables containing old info(in case of updating info)
    // we will extract these values from intent of MainActivity
    private var OldRoll_No: String? = null
    private var OldBranch: String? = null
    private var OldYear: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.sign_up_layout)

        // initialise all the variables
        initVariables()

        // initialise the presenter
        initPresenter()

        // pass the values to presenter when button clicked
        sign_up_done_button.setOnClickListener({
            sign_up_done_button.isEnabled = false
            sign_up_roll_no_edit_text.isEnabled = false
            sign_up_year_spinner.isEnabled = false
            sign_up_branch_spinner.isEnabled = false

            sign_up_done_button.text = getString(R.string.registering)
            viewToPresenter?.ButtonClicked(sign_up_roll_no_edit_text.text.toString(),
                    sign_up_branch_spinner.selectedItem.toString(),
                    sign_up_year_spinner.selectedItem.toString(),
                    OldRoll_No, OldBranch, OldYear)

        })

    }

    private fun initVariables() {
        fragment_manager = fragmentManager

        // getting old values (in case of updating info)
        val intent = intent
        OldBranch = intent.getStringExtra(getString(R.string.old_branch))
        OldYear = intent.getStringExtra(getString(R.string.old_year))
        OldRoll_No = intent.getStringExtra(getString(R.string.old_roll_no))

        if(OldBranch!=null && OldYear!=null && OldRoll_No!=null)
            sign_up_text_message.text = getString(R.string.update_text_message)

        Log.w("update", "$OldYear $OldBranch $OldRoll_No")
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

    // ----------------- presenter -> view -------------------//

    override fun showFailed(message: String) {
        Toast.makeText(applicationContext, message, Toast.LENGTH_SHORT).show()
        sign_up_done_button.isEnabled = true
        sign_up_roll_no_edit_text.isEnabled = true
        sign_up_year_spinner.isEnabled = true
        sign_up_branch_spinner.isEnabled = true
        sign_up_done_button.text = getString(R.string.sign_up)
    }


    override fun showSuccess(Roll_No: String, Branch: String, Year: String) {
        Toast.makeText(applicationContext, "Registered Successfully", Toast.LENGTH_SHORT).show()

        // store user info in God preference
        // also change sign in status
        storeInfoLocally(Roll_No, Branch, Year)

        // start main page
        startActivity(Intent(applicationContext, MainActivity::class.java))
        finish()
    }

    private fun storeInfoLocally(roll_No: String, branch: String, year: String) {
        val GodPref = getSharedPreferences(getString(R.string.god), Context.MODE_PRIVATE)
        val editor = GodPref.edit()

        editor.putString(getString(R.string.roll_no), roll_No)
        editor.putString(getString(R.string.branch), branch)
        editor.putString(getString(R.string.year), year)
        editor.putBoolean(getString(R.string.is_user_signed_in), true)

        Log.w("update", "$roll_No $branch $year put in God")
        editor.apply()
    }

    //-----------------------------------------------------//

    override fun onDestroy() {
        presenter?.detach()
        super.onDestroy()
    }

}