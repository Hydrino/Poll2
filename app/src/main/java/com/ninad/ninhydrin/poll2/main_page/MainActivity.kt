package com.ninad.ninhydrin.poll2.main_page

import android.app.FragmentManager
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.app.AppCompatActivity
import android.view.Gravity
import android.view.MenuItem
import android.view.View
import android.widget.ArrayAdapter
import com.ninad.ninhydrin.poll2.R
import com.ninad.ninhydrin.poll2.main_page.PollFragment.PollFragment
import com.ninad.ninhydrin.poll2.main_page.new_poll.AddNewPollActivity
import com.ninad.ninhydrin.poll2.sign_up.SignUpActivity
import kotlinx.android.synthetic.main.main_layout.*
import kotlinx.android.synthetic.main.nav_bar_layout.*

class MainActivity : AppCompatActivity() {

    // listener for nav drawer events
    lateinit private var actionBarToggle: ActionBarDrawerToggle

    //list of nav bar list items
    private val navBarListItems = listOf("All Polls", "My Polls", "Update Info")

    // the current page position
    private var currentPagePosition = 0

    // the fragments to be shown
    private var allPollsFrag: PollFragment? = null
    private var myPollsFrag: PollFragment? = null

    // Retain Frag for storing current page position
    private var retainFrag: RetainFrag? = null

    lateinit private var fragment_Manager: FragmentManager

    private val RETAIN_FRAG_TAG = "RetainFragTag"
    private val ALL_POLLS_FRAG_TAG = "AllPollsFragTag"
    private val MY_POLLS_FRAG_TAG = "MyPollsFragTag"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_layout)

        // initialise all the variables
        initVariables()

        // initialise navigation drawer
        initNavDrawer()

        //initialise the current page position
        initCurrentPagePosition()

        // set action bar title according to current page position
        supportActionBar?.title = navBarListItems[currentPagePosition]

        //set the page by current page position
        setCurrentPageByPagePosition(currentPagePosition)

        // set onclick listener for the FAB
        // start AddNewPollActivity
        add_new_poll_fab.setOnClickListener({
            if (nav_drawer.isDrawerOpen(Gravity.START))
                nav_drawer.closeDrawer(Gravity.START)
            else
                startActivity(Intent(this,
                        AddNewPollActivity::class.java))
        })

    }

    private fun initVariables() {
        fragment_Manager = fragmentManager
    }

    private fun initNavDrawer() {

        // important line for drawer layout to work
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        // initialise the listener
        actionBarToggle = object : ActionBarDrawerToggle(this, nav_drawer,
                R.string.open_nav, R.string.close_nav) {
            override fun onDrawerSlide(drawerView: View?, slideOffset: Float) {
                add_new_poll_fab.scaleX = 1 - 0.3f * slideOffset
                add_new_poll_fab.scaleY = 1 - 0.3f * slideOffset
                add_new_poll_fab.rotation = 135 * slideOffset
                super.onDrawerSlide(drawerView, slideOffset)
            }
        }

        // add listener to drawer layout
        nav_drawer.addDrawerListener(actionBarToggle)


        // initialise the navigation drawer list
        initNavDrawerListItems()

    }

    private fun initNavDrawerListItems() {
        val adapter = ArrayAdapter(this, R.layout.nav_bar_list_item, navBarListItems)
        nav_bar_items_list.adapter = adapter

        //setting on item click listener for nav bar list items
        nav_bar_items_list.setOnItemClickListener({ _, _, pos, _ ->

            //close the drawer every time first
            nav_drawer.closeDrawer(Gravity.START)

            // if update info is clicked we start a new activity and don't have to care about
            // the small other things
            if (pos == 2) {
                setCurrentPageByPagePosition(2)
                return@setOnItemClickListener
            }

            // if same item is clicked , just return
            if (pos == currentPagePosition) {
                return@setOnItemClickListener
            }

            // change the page according to the new position
            setCurrentPageByPagePosition(pos)

            //update current page and change in retain frag
            currentPagePosition = pos
            retainFrag?.setCurrentPagePosition(pos)

            supportActionBar?.title = navBarListItems[currentPagePosition]

        })
    }

    private fun initCurrentPagePosition() {
        retainFrag = fragment_Manager.findFragmentByTag(RETAIN_FRAG_TAG) as RetainFrag?

        if (retainFrag == null) {
            retainFrag = RetainFrag()
            fragment_Manager.beginTransaction().add(retainFrag, RETAIN_FRAG_TAG).commit()

            currentPagePosition = 0
            retainFrag?.setCurrentPagePosition(currentPagePosition)
        } else {
            currentPagePosition = (retainFrag as RetainFrag).getCurrentPagePosition()
        }
    }


    private fun setCurrentPageByPagePosition(pagePosition: Int) {

        /**
         * fragments are set to be retained
         * thus we check if they already exist or not
         * if they don't then create new and add them ,
         *  else do nothing
         **/
        val GOD = getSharedPreferences(getString(R.string.god), Context.MODE_PRIVATE)
        val RollNo = GOD.getString(getString(R.string.roll_no), "")
        val Branch = GOD.getString(getString(R.string.branch), "")
        val Year = GOD.getString(getString(R.string.year), "")

        val bundle = Bundle()
        bundle.putString(getString(R.string.roll_no), RollNo)
        bundle.putString(getString(R.string.branch), Branch)
        bundle.putString(getString(R.string.year), Year)

        when (pagePosition) {

            0 -> {
                // check if the fragment already exist or not
                allPollsFrag = fragment_Manager.findFragmentByTag(ALL_POLLS_FRAG_TAG) as PollFragment?

                // fragment does not exist, create new and add arguments
                if (allPollsFrag == null) {
                    allPollsFrag = PollFragment()

                    bundle.putBoolean(getString(R.string.is_my_polls), false)
                    allPollsFrag?.arguments = bundle

                    fragment_Manager.beginTransaction().replace(R.id.main_container,
                            allPollsFrag, ALL_POLLS_FRAG_TAG).commit()
                }
            }

            1 -> {
                myPollsFrag = fragment_Manager.findFragmentByTag(MY_POLLS_FRAG_TAG) as PollFragment?

                if (myPollsFrag == null) {
                    myPollsFrag = PollFragment()

                    bundle.putBoolean(getString(R.string.is_my_polls), true)
                    myPollsFrag?.arguments = bundle

                    fragment_Manager.beginTransaction().replace(R.id.main_container,
                            myPollsFrag, MY_POLLS_FRAG_TAG).commit()
                }
            }

            2 -> {
                val intent = Intent(this, SignUpActivity::class.java)
                intent.putExtra(getString(R.string.old_roll_no), RollNo)
                intent.putExtra(getString(R.string.old_branch), Branch)
                intent.putExtra(getString(R.string.old_year), Year)

                startActivity(intent)
            }
        }

    }

    //-------------------------------nav bar required methods-------------------------------------//

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return actionBarToggle.onOptionsItemSelected(item) || super.onOptionsItemSelected(item)
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        actionBarToggle.onConfigurationChanged(newConfig)
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        // Sync the toggle state after onRestoreInstanceState has occurred.
        actionBarToggle.syncState()
    }

    //--------------------------------------------------------------------------------------------//

}