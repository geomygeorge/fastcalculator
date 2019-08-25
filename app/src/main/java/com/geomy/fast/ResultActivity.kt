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

    val FOIR_SLAB_1 = 55
    val FOIR_SLAB_2 = 60
    val FOIR_SLAB_3 = 65
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
        val totalOligations = emi + obligations.toInt()
        try {

            foir = calculateFOIR(totalOligations, net_income.toDouble())

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

        val isTakeHomeElgbl = checkTakeHomeEligibility(net_income.toInt(), totalOligations, location)

        val ltv = calculateLTV(vehicle_value.toDouble(), loan_amnt.toDouble())

        setResults(foir, ltv, emi
            , net_income.toDouble(),
            location, totalOligations)

        setLoanEligibilityResults(isFOIRElgbl, isTakeHomeElgbl, isDeviationApplied)
        setFOIRSlab(net_income.toInt())

    }

    private fun checkFOIREligibility(net_income: Int, foir: Double) : Boolean {

        val annual_income =  net_income * 12
        var foir_eligibility = false

        //RULE #1
        //Apply FOIR Business Rule
        // Upto 5L income --> 55% FOIR
        // Between 5L to 12L income --> 60% FOIR
        // Above 12L income --> 65% FOIR

        if (annual_income <= SALARY_SLAB_1) {

            if (foir <= FOIR_SLAB_1) {

                foir_eligibility = true
            }


        } else if ((annual_income > SALARY_SLAB_1) and (annual_income <= SALARY_SLAB_2)) {

            if (foir <= FOIR_SLAB_2) {

                foir_eligibility = true
            }
        } else {

            if (foir <= FOIR_SLAB_3) {

                foir_eligibility = true
            }
        }

        return foir_eligibility
    }

    private fun checkFOIREligibilityWithDeviation(net_income: Int, foir: Double) : Boolean {

        val annual_income =  net_income * 12
        var foir_eligibility = false

        //RULE - Increase FOIR by 10%
        //Apply FOIR Business Rule
        // Upto 5L income --> 65% FOIR
        // Between 5L to 12L income --> 70% FOIR
        // Above 12L income --> 75% FOIR

        if (annual_income <= SALARY_SLAB_1) {

            if (foir <= (FOIR_SLAB_1 + FOIR_DEVIATION)) {

                foir_eligibility = true
            }


        } else if ((annual_income > SALARY_SLAB_1) and (annual_income <= SALARY_SLAB_2)) {

            if (foir <= (FOIR_SLAB_2 + FOIR_DEVIATION)) {

                foir_eligibility = true
            }
        } else {

            if (foir <= (FOIR_SLAB_3 + FOIR_DEVIATION)) {

                foir_eligibility = true
            }
        }

        return foir_eligibility
    }

    /**
     *
     */
    private fun checkTakeHomeEligibility(income: Int, totalOligations: Double,
                                         location: Int) : Boolean {


        var min_takehome_eligibility = false

        //RULE #2
        //Apply Minimum Take Home rule
        //Rural - 10K ((Location Index = 2)
        //Semi Urban - 15K (Location Index = 1)
        //Urban - 20K (Location Index = 0)

        val actualIncome = income - totalOligations
        if (location == URBAN_LOC_INDEX) {

            if(actualIncome >= MIN_TH_URBAN) {

                min_takehome_eligibility = true
            }

        } else if (location == SEMIURBAN_LOC_INDEX){

            if(actualIncome >= MIN_TH_SEMIURBAN) {

                min_takehome_eligibility = true
            }

        } else{
            if(actualIncome >= MIN_TH_RURAL) {

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

    private fun setResults(foir: Double
                           , ltv: Double
                           , emi: Double
                           , net_income:Double
                           , location: Int
                           , totalOligations: Double) {

        val foirTv: TextView = findViewById(R.id.foirTv)
        val takehomeTV: TextView = findViewById(R.id.takehomeTv)
        val ltvTV: TextView = findViewById(R.id.ltvTv)
        val emiTV: TextView = findViewById(R.id.emiTv)
        val locationTV: TextView = findViewById(R.id.locationTv)

        //Setting Results

        //FOIR
        foirTv.text = foir.toString() + "%"

        //Take Home
        val netTakeHome = roundOffDecimal(net_income - totalOligations)
        takehomeTV.text = netTakeHome.toString()

        //LTV
        ltvTV.text = ltv.toString() + "%"

        //EMI
        emiTV.text = emi.toString()

        //Location
        locationTV.text = resources.getText(TAB_TITLES[location])


    }

    private fun setLoanEligibilityResults (isFOIRElgbl: Boolean
                                           , isTakeHomeElgbl: Boolean
                                           , isDeviationApplied: Boolean) {

        val foirTv: TextView = findViewById(R.id.foirTv)
        val takehomeTV: TextView = findViewById(R.id.takehomeTv)
        val eligibilityTV: TextView = findViewById(R.id.resultTv)
        val deviationBut: ToggleButton = findViewById(R.id.deviationBut)
        val foirLablel: TextView = findViewById(R.id.foirTvLable)
        val takeHomeLabel: TextView = findViewById(R.id.takehomeLable)

        //Store default Textview color
        val ltvTV: TextView = findViewById(R.id.ltvTv)
        val defaultTvColor = ltvTV.getCurrentTextColor() //save original colors

        //Eligibility

        //Evaluate the combination of Rule 1 & 2

        resetFOIRAlert()
        resetTakeHomeAlert()
        if (isFOIRElgbl and isTakeHomeElgbl) {

            eligibilityTV.setText(R.string.result_eligible_yes)
            eligibilityTV.setTextColor(resources.getColor(R.color.colorPrimary))

            //Reset the Text color
            foirLablel.setTextColor(defaultTvColor)
            foirTv.setTextColor(defaultTvColor)
            takeHomeLabel.setTextColor(defaultTvColor)
            takehomeTV.setTextColor(defaultTvColor)

            if(isDeviationApplied) {

                val calculatedFOIR = foirTv.text.toString().toInt()
                setFOIRDeviationInfo(calculatedFOIR)
            }

        } else {

            //FOIR condition check failed
            if(!isFOIRElgbl) {

                //Set FOIR color as RED to indicate the in-eligibility
                foirLablel.setTextColor(resources.getColor(R.color.colorRed))
                foirTv.setTextColor(resources.getColor(R.color.colorRed))

                //Display the reason for FOIR in-eligibility
                setFOIRAlert(isDeviationApplied)

            }

            //Min Take home check failed

            if(!isTakeHomeElgbl) {

                //Set Take Home color as RED to indicate the in-eligibility
                takeHomeLabel.setTextColor(resources.getColor(R.color.colorRed))
                takehomeTV.setTextColor(resources.getColor(R.color.colorRed))

                setTakeHomeAlert()
            }

            //Set in-eligibility and RED color
            eligibilityTV.setText(R.string.result_eligible_no)
            eligibilityTV.setTextColor(resources.getColor(R.color.colorRed))

            //Enable Deviation button
            deviationBut.visibility = View.VISIBLE

        }
    }

    private fun resetTakeHomeAlert() {

        var minTakeHomeLabel: TextView = findViewById(R.id.takehomeAlertLab)
        var minTakeHomeAlert: TextView = findViewById(R.id.takehomeAlertTv)

        minTakeHomeLabel.visibility = View.GONE

        minTakeHomeAlert.text =  ""
        minTakeHomeAlert.visibility = View.GONE
    }

    private fun resetFOIRAlert() {

        var foirAlertLabel: TextView = findViewById(R.id.foirAlertLabel)
        var foirAlertTV: TextView = findViewById(R.id.foirAlertTv)

        foirAlertLabel.text = ""
        foirAlertLabel.visibility = View.GONE

        foirAlertTV.text = ""
        foirAlertTV.visibility = View.GONE


    }

    private fun setTakeHomeAlert() {

        var minTakeHomeLab: TextView = findViewById(R.id.takehomeAlertLab)
        var minTakeHomeAlert: TextView = findViewById(R.id.takehomeAlertTv)

        val takeHomeNorm = getTakeHomeNorm()

        val takeHomeAlert = "Minimum " + takeHomeNorm.toString()
        minTakeHomeLab.visibility = View.VISIBLE

        minTakeHomeAlert.visibility = View.VISIBLE
        minTakeHomeAlert.setText(takeHomeAlert)

    }

    private fun getTakeHomeNorm(): Any {

        var takeHomeNorm: Int = 0

        if (location == URBAN_LOC_INDEX) {

            takeHomeNorm = MIN_TH_URBAN

        } else if (location == SEMIURBAN_LOC_INDEX){

            takeHomeNorm = MIN_TH_SEMIURBAN

        } else{

            takeHomeNorm = MIN_TH_RURAL
        }

        return takeHomeNorm

    }

    private fun setFOIRAlert(isDeviationApplied: Boolean) {

        var foirAlertLabel: TextView = findViewById(R.id.foirAlertLabel)
        var foirAlertTV: TextView = findViewById(R.id.foirAlertTv)

        //Make the field visible
        foirAlertLabel.visibility =View.VISIBLE

        //Set foir norm label
        foirAlertLabel.setText(R.string.foir_norm)

        val foirNorm = getFoirNorm(isDeviationApplied)

        val foirNormMsg = "Only upto " + foirNorm

        foirAlertTV.setText(foirNormMsg)
        foirAlertTV.visibility = View.VISIBLE

    }

    private fun setFOIRDeviationInfo(foir: Int) {

        var foirAlertLabel: TextView = findViewById(R.id.foirAlertLabel)
        var foirAlertTV: TextView = findViewById(R.id.foirAlertTv)

        //Make the field visible
        foirAlertLabel.visibility =View.VISIBLE
        //Set the foir deviation label
        foirAlertLabel.setText(R.string.foir_deviation)

        val foirDiff = calculateFOIRDeviation(foir)

        foirAlertTV.setText(foirDiff.toString() + "%")

        foirAlertTV.visibility = View.VISIBLE

    }

    private fun calculateFOIRDeviation(foir:Int) : Int {

        val annual_income =  net_income.toInt() * 12
        var foirDiff = 0

        if (annual_income <= SALARY_SLAB_1) {

            foirDiff = (FOIR_SLAB_1 + FOIR_DEVIATION) - foir

        } else if ((annual_income > SALARY_SLAB_1) and (annual_income <= SALARY_SLAB_2)) {

            foirDiff = (FOIR_SLAB_2 + FOIR_DEVIATION) - foir

        } else {

            foirDiff = (FOIR_SLAB_3 + FOIR_DEVIATION) - foir

        }

        return foirDiff
    }

    private fun getFoirNorm(isDeviationApplied: Boolean) : Int {

        val annual_income =  net_income.toInt() * 12
        var foirPercentage = 0

        if (annual_income <= SALARY_SLAB_1) {

            if (isDeviationApplied) {

                foirPercentage = FOIR_SLAB_1 + FOIR_DEVIATION
            } else {
                foirPercentage = FOIR_SLAB_1
            }


        } else if ((annual_income > SALARY_SLAB_1) and (annual_income <= SALARY_SLAB_2)) {

            if (isDeviationApplied) {
                foirPercentage = FOIR_SLAB_2 + FOIR_DEVIATION
            } else {
                foirPercentage = FOIR_SLAB_2
            }

        } else {

            if (isDeviationApplied) {

                foirPercentage = (FOIR_SLAB_3 + FOIR_DEVIATION)
            } else {
                foirPercentage = FOIR_SLAB_3
            }

        }

        return foirPercentage
    }

    private fun setFOIRSlab(netIncome: Int) {

        val foirSlabTv: TextView = findViewById(R.id.foirSlabTv)

        foirSlabTv.text = getFOIRSalaryCategory(netIncome)
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

    private fun getFOIRSalaryCategory(netIncome: Int): String {

        val annualIncome = netIncome * 12
        var salaryCategory = ""

        if (annualIncome <= SALARY_SLAB_1) {

            salaryCategory = "Less than 5 Lacs"
        } else if((annualIncome > SALARY_SLAB_1) and (annualIncome <= SALARY_SLAB_2)) {

            salaryCategory = "5 to 12 Lacs"
        } else {

            salaryCategory = "Greater than 12 Lacs"
        }

        return salaryCategory
    }

    private fun getFOIRdifference() {

    }
}
