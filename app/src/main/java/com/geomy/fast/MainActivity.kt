package com.geomy.fast

import android.os.Bundle
import android.support.design.widget.FloatingActionButton
import android.support.design.widget.TabLayout
import android.support.v4.view.ViewPager
import android.support.v7.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import com.geomy.fast.ui.main.SectionsPagerAdapter
import android.content.Intent
import android.widget.EditText


class MainActivity : AppCompatActivity() {

    val isAppUnderTest = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val sectionsPagerAdapter = SectionsPagerAdapter(this, supportFragmentManager)
        val viewPager: ViewPager = findViewById(R.id.view_pager)
        viewPager.adapter = sectionsPagerAdapter
        val tabs: TabLayout = findViewById(R.id.tabs)
        tabs.setupWithViewPager(viewPager)
        //val fab: FloatingActionButton = findViewById(R.id.fab)
        setSupportActionBar(findViewById(R.id.my_toolbar))

        /*fab.setOnClickListener { view ->

            if (isAppUnderTest) {

                fillFormValues()
            } else {
                resetForm()
            }

        }*/
    }

    private fun resetForm() {

        val vehicleValEt: EditText = findViewById(R.id.vValueEt)
        val loanAmntEt: EditText = findViewById(R.id.lAmountInp)
        val interestRateEt: EditText = findViewById(R.id.intrEdTxt)
        val loanTenureEt: EditText = findViewById(R.id.tenureEdTxt)
        val obligationsEt: EditText = findViewById(R.id.obligationEdTxt)
        val netIncomeEt: EditText = findViewById(R.id.netIncomeEt)

        vehicleValEt.setText("")
        loanAmntEt.setText("")
        interestRateEt.setText("")
        loanTenureEt.setText("")
        obligationsEt.setText("")
        netIncomeEt.setText("")
    }

    //For testing only

    private fun fillFormValues() {

        val vehicleValEt: EditText = findViewById(R.id.vValueEt)
        val loanAmntEt: EditText = findViewById(R.id.lAmountInp)
        val interestRateEt: EditText = findViewById(R.id.intrEdTxt)
        val loanTenureEt: EditText = findViewById(R.id.tenureEdTxt)
        val obligationsEt: EditText = findViewById(R.id.obligationEdTxt)
        val netIncomeEt: EditText = findViewById(R.id.netIncomeEt)

        vehicleValEt.setText("500000")
        loanAmntEt.setText("250000")
        interestRateEt.setText("9.15")
        loanTenureEt.setText("24")
        obligationsEt.setText("15000")
        netIncomeEt.setText("50000")
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        super.onCreateOptionsMenu(menu)
        val menuInflater = menuInflater
        menuInflater.inflate(R.menu.settings_menu, menu)
        return true
    }



    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId
        if (id == R.id.action_settings) {
            val intent = Intent(this@MainActivity, SettingsActivity::class.java)
            startActivity(intent)
            return true
        }
        return super.onOptionsItemSelected(item)
    }

}


