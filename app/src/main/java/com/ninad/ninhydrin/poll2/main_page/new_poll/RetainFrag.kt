package com.ninad.ninhydrin.poll2.main_page.new_poll

import android.app.Fragment
import android.os.Bundle

class RetainFrag : Fragment() {
    private var presenter: Presenter? = null

    fun setPresenter(p: Presenter) {
        presenter = p
    }

    fun getPresenter(): Presenter? {
        return presenter
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        retainInstance = true
    }
}