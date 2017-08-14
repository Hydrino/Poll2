package com.ninad.ninhydrin.poll2.main_page

import android.app.Fragment
import android.os.Bundle


class RetainFrag : Fragment() {

    private var currentPagePosition: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        retainInstance = true
    }

    fun setCurrentPagePosition(pos: Int) {
        currentPagePosition = pos
    }

    fun getCurrentPagePosition(): Int {
        return currentPagePosition
    }
}