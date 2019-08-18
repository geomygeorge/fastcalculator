package com.geomy.fast

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import com.geomy.fast.ui.main.SettingsFragment
import kotlinx.android.synthetic.main.activity_settings.*


class SettingsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
        setSupportActionBar(toolbar_settings)

        val actionBar = this.supportActionBar

        actionBar?.setDisplayHomeAsUpEnabled(true)

        supportFragmentManager.beginTransaction()
            //.replace(android.R.id.content, SettingsFragment())
            .add(R.id.fragment_settings, SettingsFragment())
            .commit()
    }

    /*override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.getItemId()
        if (id == android.R.id.home) {
            NavUtils.navigateUpFromSameTask(this)
        }
        return super.onOptionsItemSelected(item)
    }*/
}
