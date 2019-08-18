package com.geomy.fast.ui.main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.support.v4.app.Fragment
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
import android.support.v4.content.res.ResourcesCompat

/**
 * A placeholder fragment containing a simple view.
 */
class PlaceholderFragment : Fragment() {

    private lateinit var pageViewModel: PageViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        pageViewModel = ViewModelProviders.of(this).get(PageViewModel::class.java).apply {
            setIndex(arguments?.getInt(ARG_SECTION_NUMBER) ?: 1)
        }


    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_main, container, false)
        //val textView: TextView = root.findViewById(R.id.section_label)
        pageViewModel.text.observe(this, Observer<String> {
            //textView.text = it
        })

        val obligationEdtTxt: EditText = root.findViewById(R.id.obligationEdTxt)

        obligationEdtTxt.setOnClickListener {view ->

            //Inflate the dialog with custom view

            context?.let { context ->

                val mDialogView = inflater.inflate(R.layout.obligations_layout, null)
                val homeLoan: EditText = mDialogView.findViewById(R.id.homeLoanEt)
                val perLoan: EditText = mDialogView.findViewById(R.id.perLoanEt)
                val others:EditText = mDialogView.findViewById(R.id.othersEt)

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

                    val homeLoan = if (homeLoan.text.toString() != "") homeLoan.text.toString().toInt() else 0
                    val perLoan = if (perLoan.text.toString() != "") perLoan.text.toString().toInt() else 0
                    val others = if (others.text.toString() != "") others.text.toString().toInt() else 0

                    val totalObligations = homeLoan + perLoan + others

                    //set the input text in TextView
                    obligationEdtTxt.setText(totalObligations.toString())
                }
                //cancel button click of custom layout
                mDialogView.dialogCancelBtn.setOnClickListener {
                    //dismiss dialog
                    mAlertDialog.dismiss()
                    obligationEdtTxt.setText("0")
                }
            }

        }

        fun validateForm(): Boolean {

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
                //vehicleValEt.setBackgroundResource(android.R.drawable.editbox_background)
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


            //Highlight the mandatory fields for user input
            val valErrTv: TextView = root.findViewById(R.id.valErrorTV)
            if (fieldsToHighlight.size > 0 ) {
                // Initialize a new GradientDrawable instance
                val gd = GradientDrawable()
                // Set the gradient drawable background to transparent
                gd.setColor(Color.parseColor("#00ffffff"))

                // Set a border for the gradient drawable
                gd.setStroke(4, Color.RED)

                for (item in fieldsToHighlight) {


                    item.setBackground(gd)

                }


                valErrTv.visibility = View.VISIBLE
            } else {
                valErrTv.visibility = View.INVISIBLE
            }

            // Set the Form edit text background to default

            if (fieldsToSetDefault.size > 0) {

                for (item in fieldsToSetDefault) {
                    item.setBackgroundResource(android.R.drawable.editbox_background)
                }
            }

            if (fieldsToHighlight.size > 0 ) {
                return false
            } else{
                return true
            }



        }
        //Collect form data for result activity


        //Launch result activity

        val calculateBtn: Button = root.findViewById(R.id.calculateBtn)


        calculateBtn.setOnClickListener { view ->

            if(validateForm()) {
                activity?.let{
                    val intent = Intent (it, ResultActivity::class.java)
                    it.startActivity(intent)
                }

            }

        }


        return root

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
            return PlaceholderFragment().apply {
                arguments = Bundle().apply {
                    putInt(ARG_SECTION_NUMBER, sectionNumber)
                }
            }
        }
    }
}