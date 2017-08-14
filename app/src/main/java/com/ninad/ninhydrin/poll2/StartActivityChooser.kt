package com.ninad.ninhydrin.poll2

import android.content.Context
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import com.ninad.ninhydrin.poll2.main_page.MainActivity
import com.ninad.ninhydrin.poll2.sign_up.SignUpActivity

class StartActivityChooser : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (isUserSignedIn())
            startActivity(Intent(applicationContext, MainActivity::class.java))
        else
            startActivity(Intent(applicationContext, SignUpActivity::class.java))

        finish()
    }

    private fun isUserSignedIn(): Boolean {
        val GodPref = getSharedPreferences(getString(R.string.god), Context.MODE_PRIVATE)
        val isUserSignedIn = GodPref.getBoolean(getString(R.string.is_user_signed_in), false)
        return isUserSignedIn
    }

}
