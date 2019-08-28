package com.geomy.fast.ui.main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.support.v4.app.Fragment
import android.support.v4.view.ViewPager
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.DialogInterface
import android.content.Intent
import android.content.res.Resources
import android.support.design.widget.Snackbar
import android.support.v7.app.AlertDialog
import android.widget.*
import com.geomy.fast.MainActivity
import com.geomy.fast.R
import com.geomy.fast.ResultActivity
import kotlinx.android.synthetic.main.obligations_layout.view.*
import android.graphics.drawable.GradientDrawable
import android.graphics.Color
import android.util.Log

/**
 * A placeholder fragment containing a simple view.
 */
class PlaceholderFragment : Fragment() {

    private lateinit var pageViewModel: PageViewModel

    private var TAG = "FAST"

    private var locality = 1
    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        Log.d(TAG, "onCreate")
        pageViewModel = ViewModelProviders.of(this).get(PageViewModel::class.java).apply {

            locality = arguments?.getInt(ARG_SECTION_NUMBER) ?: 1
            setIndex(locality)

            Log.d(TAG, "Location Index: " + locality)
        }



    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_main, container, false)

        val obligationEdtTxt: EditText = root.findViewById(R.id.obligationEdTxt)


        var homeLoanVal = 0
        var perLoanVal = 0
        var othersVal = 0

        obligationEdtTxt.setOnClickListener {view ->

            //Inflate the dialog with custom view


            context?.let { context ->

                val mDialogView = inflater.inflate(R.layout.obligations_layout, null)
                val homeLoan: EditText = mDialogView.findViewById(R.id.homeLoanEt)
                val perLoan: EditText = mDialogView.findViewById(R.id.perLoanEt)
                val others:EditText = mDialogView.findViewById(R.id.othersEt)

                homeLoan.setText(homeLoanVal.toString())
                perLoan.setText(perLoanVal.toString())
                others.setText(othersVal.toString())

                //AlertDialogBuilder
                val mBuilder = AlertDialog.Builder(context)
                    .setView(mDialogView)
                    .setTitle("Existing Obligations")
                //show dialog
                val  mAlertDialog = mBuilder.show()

                //login button click of custom layout
                mDialogView.dialogLoginBtn.setOnClickListener {
                    //dismiss dialog
                    mAlertDialog.dismiss()
                    //get text from EditTexts of custom layout

                    /*val homeLoan = if (homeLoan.text.toString() != "") homeLoan.text.toString().toInt() else 0
                    val perLoan = if (perLoan.text.toString() != "") perLoan.text.toString().toInt() else 0
                    val others = if (others.text.toString() != "") others.text.toString().toInt() else 0*/
                    homeLoanVal = if (homeLoan.text.toString() != "") homeLoan.text.toString().toInt() else 0
                    perLoanVal = if (perLoan.text.toString() != "") perLoan.text.toString().toInt() else 0
                    othersVal = if (others.text.toString() != "") others.text.toString().toInt() else 0

                    val totalObligations = homeLoanVal + perLoanVal + othersVal

                    //set the input text in TextView
                    obligationEdtTxt.setText(totalObligations.toString())
                }
                //cancel button click of custom layout
                mDialogView.dialogCancelBtn.setOnClickListener {
                    //dismiss dialog
                    mAlertDialog.dismiss()
                    obligationEdtTxt.setText("0")

                    //Reset saved values of existing obligations
                    homeLoanVal = 0
                    perLoanVal = 0
                    othersVal = 0
                }
            }

        }




        //Collect form data for result activity

        val myPage: ViewPager  = activity!!.findViewById(R.id.view_pager)


        //Launch result activity

        val calculateBtn: Button = root.findViewById(R.id.calculateBtn)

        calculateBtn.setOnClickListener { view ->

            val isFormComplete = validateForm(root)

            var isLoanAmtValid = true

            if (isFormComplete) {

                isLoanAmtValid = validateLoanAmount(root)
            }

            if(isFormComplete and isLoanAmtValid) {
                activity?.let{
                    val intent = Intent (it, ResultActivity::class.java)
                    //Load form values into Intent, to access in Result activity

                    val vehicleValEt: EditText = root.findViewById(R.id.vValueEt)
                    val loanAmntEt: EditText = root.findViewById(R.id.lAmountInp)
                    val interestRateEt: EditText = root.findViewById(R.id.intrEdTxt)
                    val loanTenureEt: EditText = root.findViewById(R.id.tenureEdTxt)
                    val obligationsEt: EditText = root.findViewById(R.id.obligationEdTxt)
                    val netIncomeEt: EditText = root.findViewById(R.id.netIncomeEt)

                    intent.putExtra("VEHICLE_VALUE", vehicleValEt.text.toString())
                    intent.putExtra("LOAN_AMNT", loanAmntEt.text.toString())
                    intent.putExtra("INTEREST_RATE", interestRateEt.text.toString())
                    intent.putExtra("LOAN_TENURE", loanTenureEt.text.toString())
                    intent.putExtra("OBLIGATIONS", obligationsEt.text.toString())
                    intent.putExtra("NET_INCOME", netIncomeEt.text.toString())
                    intent.putExtra("LOCATION", myPage.currentItem)

                    it.startActivity(intent)
                }

            }

        }


        return root

    }

    private fun validateLoanAmount(root: View): Boolean {

        var isLoanAmntValid = true
        val vehicleValEt: EditText = root.findViewById(R.id.vValueEt)
        val loanAmntEt: EditText = root.findViewById(R.id.lAmountInp)

        val vehicleVal = vehicleValEt.text.toString().toInt()
        val loanAmt = loanAmntEt.text.toString().toInt()

        val fieldsToHighlight = mutableListOf<EditText>()
        val fieldsToSetDefault = mutableListOf<EditText>()

        //Loan amount cannot be greater than vehicle value

        if (loanAmt > vehicleVal) {
            isLoanAmntValid = false
        }

        if(!isLoanAmntValid) {

            fieldsToHighlight.add(loanAmntEt)
            displayInvalidInputs(fieldsToHighlight
                , fieldsToSetDefault
                , getString(R.string.err_loan_amount)
                , root)
        } else {
            fieldsToSetDefault.add(loanAmntEt)
        }

        return isLoanAmntValid

    }

    fun validateForm(root: View): Boolean {

        val vehicleValEt: EditText = root.findViewById(R.id.vValueEt)
        val loanAmntEt: EditText = root.findViewById(R.id.lAmountInp)
        val interestRateEt: EditText = root.findViewById(R.id.intrEdTxt)
        val loanTenureEt: EditText = root.findViewById(R.id.tenureEdTxt)
        val obligationsEt: EditText = root.findViewById(R.id.obligationEdTxt)
        val netIncomeEt: EditText = root.findViewById(R.id.netIncomeEt)

        val fieldsToHighlight = mutableListOf<EditText>()
        val fieldsToSetDefault = mutableListOf<EditText>()

        if (vehicleValEt.text.toString() == "") {
            fieldsToHighlight.add(vehicleValEt)
        } else {
            fieldsToSetDefault.add(vehicleValEt)
        }

        if (loanAmntEt.text.toString() == "") {
            fieldsToHighlight.add(loanAmntEt)
        } else {
            fieldsToSetDefault.add(loanAmntEt)
        }

        if(interestRateEt.text.toString() == "") {
            fieldsToHighlight.add(interestRateEt)
        } else{
            fieldsToSetDefault.add(interestRateEt)
        }

        if(loanTenureEt.text.toString() == "") {
            fieldsToHighlight.add(loanTenureEt)
        } else{
            fieldsToSetDefault.add(loanTenureEt)
        }

        if(netIncomeEt.text.toString() == "") {
            fieldsToHighlight.add(netIncomeEt)
        } else {
            fieldsToSetDefault.add(netIncomeEt)
        }

        if(obligationsEt.text.toString() == "") {
            obligationsEt.setText("0")
        }

        displayInvalidInputs(fieldsToHighlight
            , fieldsToSetDefault
            , getString(R.string.err_mandatory_fields)
            , root)

        if (fieldsToHighlight.size > 0 ) {
            return false
        } else{
            return true
        }



    }

    private fun displayInvalidInputs(fieldsToHighlight: MutableList<EditText>
                             , fieldsToSetDefault: MutableList<EditText>
                             , errMsg: String
                             ,root: View) {

        //Highlight the mandatory fields for user input
        val valErrTv: TextView = root.findViewById(R.id.valErrorTV)
        if (fieldsToHighlight.size > 0 ) {
            // Initialize a new GradientDrawable instance
            val gd = GradientDrawable()
            // Set the gradient drawable background to transparent
            gd.setColor(Color.parseColor("#00ffffff"))

            // Set a border for the gradient drawable
            gd.setStroke(4, resources.getColor(R.color.colorRed))

            for (item in fieldsToHighlight) {
                item.setBackground(gd)
            }

            valErrTv.setText(errMsg)
            valErrTv.visibility = View.VISIBLE
        } else {
            valErrTv.setText("")
            valErrTv.visibility = View.GONE
        }

        // Set the Form edit text background to default

        if (fieldsToSetDefault.size > 0) {

            for (item in fieldsToSetDefault) {
                item.setBackgroundResource(android.R.drawable.edit_text)
            }
        }
    }

    companion object {
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        private const val ARG_SECTION_NUMBER = "section_number"

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        @JvmStatic
        fun newInstance(sectionNumber: Int): PlaceholderFragment {

            Log.d("FAST", "sectionNumber: " + sectionNumber)
            return PlaceholderFragment().apply {
                arguments = Bundle().apply {
                    putInt(ARG_SECTION_NUMBER, sectionNumber)
                }
            }
        }
    }
}