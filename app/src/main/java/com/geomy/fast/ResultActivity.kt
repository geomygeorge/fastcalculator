package com.geomy.fast

import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v7.app.AppCompatActivity;
import android.support.v7.preference.PreferenceManager
import android.util.Log
import android.view.View
import android.widget.TextView
import android.widget.Toast
import android.widget.ToggleButton

import kotlinx.android.synthetic.main.activity_result.*
import java.math.RoundingMode
import java.text.DecimalFormat
import kotlin.math.round
import android.content.res.ColorStateList



class ResultActivity : AppCompatActivity() {

    private var ERROR_MSG = ""
    private var SALARY_SLAB_1 = 0
    private var SALARY_SLAB_2 = 0
    private var MIN_TH_RURAL = 0
    private var MIN_TH_SEMIURBAN = 0
    private var MIN_TH_URBAN = 0

    private val URBAN_LOC_INDEX = 0
    private val SEMIURBAN_LOC_INDEX = 1
    private val RURAL_LOC_INDEX = 2

    private val TAB_TITLES = arrayOf(
        R.string.tab_text_1,
        R.string.tab_text_2,
        R.string.tab_text_3
    )

    var vehicle_value = "0"
    var loan_amnt = "0"
    var interest_rate = "0"
    var loan_tenure = "0"
    var obligations = "0"
    var net_income = "0"
    var location = 0

    val FOIR_DEVIATION = 10
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_result)
        setSupportActionBar(toolbar)

        val actionBar = this.supportActionBar

        actionBar?.setDisplayHomeAsUpEnabled(true)
        var bundle :Bundle =intent.extras

        //Get form values from Main activity

        vehicle_value = bundle.getString("VEHICLE_VALUE")
        loan_amnt = bundle.getString("LOAN_AMNT")
        interest_rate = bundle.getString("INTEREST_RATE")
        loan_tenure = bundle.getString("LOAN_TENURE")
        obligations = bundle.getString("OBLIGATIONS")
        net_income = bundle.getString("NET_INCOME")
        location = bundle.getInt("LOCATION")


        //Get preferences

        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this /* Activity context */)
        SALARY_SLAB_1 = sharedPreferences.getString("salary_slab_1", "5").toInt() * 100000 // 5 Lacs
        SALARY_SLAB_2 = sharedPreferences.getString("salary_slab_2", "12").toInt() * 100000  // 12 Lacs
        MIN_TH_RURAL = sharedPreferences.getString("rural_takehome", "10000").toInt()  // 10K
        MIN_TH_SEMIURBAN = sharedPreferences.getString("semi_urban_takehome", "15000").toInt()  // 15K
        MIN_TH_URBAN = sharedPreferences.getString("urban_takehome", "20000").toInt()  // 20K

        //Eligibility check without deviation
        calculateEligibility(false)

        val deviationBut: ToggleButton = findViewById(R.id.deviationBut)

        deviationBut.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                // The toggle is enabled
                calculateEligibility(false)
            } else {
                calculateEligibility(true)
            }
        }

    }

    private fun calculateEligibility(isDeviationApplied: Boolean) {


        var emi = 0.0
        try {

            emi = calculateEMI(loan_amnt.toDouble(), interest_rate.toDouble(), loan_tenure.toDouble())
        } catch(e: Exception) {

            ERROR_MSG = "Error in calculating EMI"
        }


        var foir = 0.0
        try {

            val total_obligations = emi + obligations.toInt()
            foir = calculateFOIR(total_obligations, net_income.toDouble())

        } catch (e: Exception) {

            ERROR_MSG = "Error in calculating FOIR"

        }

        var isFOIRElgbl = false

        if(isDeviationApplied) {

            // Apply 10% deviation
            isFOIRElgbl = checkFOIREligibilityWithDeviation(net_income.toInt(), foir)
        } else {
            isFOIRElgbl = checkFOIREligibility(net_income.toInt(), foir)
        }

        val isTakeHomeElgbl = checkTakeHomeEligibility(net_income.toInt(), location)

        val ltv = calculateLTV(vehicle_value.toDouble(), loan_amnt.toDouble())

        setResults(foir, ltv, isFOIRElgbl, isTakeHomeElgbl, emi, net_income.toDouble(), location)

    }

    private fun checkFOIREligibility(net_income: Int, foir: Double) : Boolean {

        val annual_income =  net_income * 12 // This is an assumption. Need to check business, whether annual salary need to consider here.
        var foir_eligibility = false

        //RULE #1
        //Apply FOIR Business Rule
        // Upto 5L income --> 55% FOIR
        // Between 5L to 12L income --> 60% FOIR
        // Above 12L income --> 65% FOIR

        if (annual_income <= SALARY_SLAB_1) {

            if (foir <= 55) {

                foir_eligibility = true
            }


        } else if ((annual_income > SALARY_SLAB_1) and (annual_income <= SALARY_SLAB_2)) {

            if (foir <= 60) {

                foir_eligibility = true
            }
        } else {

            if (foir <= 65) {

                foir_eligibility = true
            }
        }

        return foir_eligibility
    }

    private fun checkFOIREligibilityWithDeviation(net_income: Int, foir: Double) : Boolean {

        val annual_income =  net_income * 12 // This is an assumption. Need to check business, whether annual salary need to consider here.
        var foir_eligibility = false

        //RULE - Increase FOIR by 10%
        //Apply FOIR Business Rule
        // Upto 5L income --> 65% FOIR
        // Between 5L to 12L income --> 70% FOIR
        // Above 12L income --> 75% FOIR

        if (annual_income <= SALARY_SLAB_1) {

            if (foir <= 65) {

                foir_eligibility = true
            }


        } else if ((annual_income > SALARY_SLAB_1) and (annual_income <= SALARY_SLAB_2)) {

            if (foir <= 70) {

                foir_eligibility = true
            }
        } else {

            if (foir <= 75) {

                foir_eligibility = true
            }
        }

        return foir_eligibility
    }

    /**
     *
     */
    private fun checkTakeHomeEligibility(net_income: Int, location: Int) : Boolean {


        var min_takehome_eligibility = false

        //RULE #2
        //Apply Minimum Take Home rule
        //Rural - 10K ((Location Index = 2)
        //Semi Urban - 15K (Location Index = 1)
        //Urban - 20K (Location Index = 0)

        if (location == URBAN_LOC_INDEX) {

            if(net_income >= MIN_TH_URBAN) {

                min_takehome_eligibility = true
            }

        } else if (location == SEMIURBAN_LOC_INDEX){

            if(net_income >= MIN_TH_SEMIURBAN) {

                min_takehome_eligibility = true
            }

        } else{
            if(net_income >= MIN_TH_RURAL) {

                min_takehome_eligibility = true
            }
        }


        return min_takehome_eligibility

    }

    private fun calculateLTV(vehicle_value: Double, loan_amount: Double):Double {

        var ltv: Double = (loan_amount / vehicle_value) * 100

        ltv = roundOffDecimal(ltv)

        return ltv
    }

    private fun roundOffDecimal(number: Double): Double {
        val df = DecimalFormat("#.##")
        df.roundingMode = RoundingMode.FLOOR
        return df.format(number).toDouble()
    }

    private fun setResults(foir: Double, ltv: Double, isFOIRElgbl: Boolean, isTakeHomeElgbl: Boolean
                           , emi: Double, net_income:Double, location: Int) {

        val foirTv: TextView = findViewById(R.id.foirTv)
        val takehomeTV: TextView = findViewById(R.id.takehomeTv)
        val ltvTV: TextView = findViewById(R.id.ltvTv)
        val emiTV: TextView = findViewById(R.id.emiTv)
        val locationTV: TextView = findViewById(R.id.locationTv)
        val eligibilityTV: TextView = findViewById(R.id.resultTv)
        val deviationBut: ToggleButton = findViewById(R.id.deviationBut)
        val foirLablel: TextView = findViewById(R.id.foirTvLable)
        val takeHomeLabel: TextView = findViewById(R.id.takehomeLable)

        //Store default Textview color
        val defaultTvColor = ltvTV.getCurrentTextColor() //save original colors
        //Setting Reults

        //FOIR
        foirTv.text = foir.toString() + "%"
        //Take Home
        takehomeTV.text = net_income.toString()

        //LTV
        ltvTV.text = ltv.toString() + "%"

        //EMI
        emiTV.text = emi.toString()

        //Location
        locationTV.text = resources.getText(TAB_TITLES[location])

        //Eligibility

        //Evaluate the combination of Rule 1 & 2


        if (isFOIRElgbl and isTakeHomeElgbl) {

            eligibilityTV.setText(R.string.result_eligible_yes)
            eligibilityTV.setTextColor(resources.getColor(R.color.colorPrimary))


            //Reset the Text color
            foirLablel.setTextColor(defaultTvColor)
            foirTv.setTextColor(defaultTvColor)
            takeHomeLabel.setTextColor(defaultTvColor)
            takehomeTV.setTextColor(defaultTvColor)

        } else {

            //FOIR condition check failed
            if(!isFOIRElgbl) {

                //Set FOIR color as RED to indicate the in-eligibility
                foirLablel.setTextColor(resources.getColor(R.color.colorRed))
                foirTv.setTextColor(resources.getColor(R.color.colorRed))

            }

            //Min Take home check failed

            if(!isTakeHomeElgbl) {

                //Set Take Home color as RED to indicate the in-eligibility
                takeHomeLabel.setTextColor(resources.getColor(R.color.colorRed))
                takehomeTV.setTextColor(resources.getColor(R.color.colorRed))
            }

            //Set in-eligibility and RED color
            eligibilityTV.setText(R.string.result_eligible_no)
            eligibilityTV.setTextColor(resources.getColor(R.color.colorRed))

            //Enable Deviation button
            deviationBut.visibility = View.VISIBLE

        }


    }

    private fun calculateEMI(principal :Double, rate: Double, time: Double): Double {

        val rate=rate/(12*100) /*one month interest*/

        var emi = (principal*rate*Math.pow(1+rate,time))/(Math.pow(1+rate,time)-1)

        emi = roundOffDecimal(emi)

        return emi
    }

    private fun calculateFOIR(total_obligations: Double, netIncome: Double): Double {

        var foir = (total_obligations/netIncome) * 100

        foir = roundOffDecimal(foir)
        return foir
    }
}
