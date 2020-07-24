package com.example.whatsappclone.Activity

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.core.widget.addTextChangedListener
import com.example.whatsappclone.R
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.android.synthetic.main.activity_login.*

class LoginActivity : AppCompatActivity() {
    private  lateinit var PhoneNumber:String
    private lateinit var CountryCode:String
    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

       phoneNumberEt.addTextChangedListener {
            nextBtn.isEnabled =  !(it.isNullOrEmpty() || it.length < 10)
        }

        nextBtn.setOnClickListener {
            checkNumber()
        }
    }

    private fun checkNumber() {
        CountryCode = ccp.selectedCountryCodeWithPlus
        PhoneNumber = CountryCode + phoneNumberEt.text.toString()

        notifyUser()
    }

    private fun notifyUser() {
        MaterialAlertDialogBuilder(this).apply {
            setMessage("We will be verifying the phone number $PhoneNumber\n" +
                    "Is this OK or you would like to edit the number?")

            setPositiveButton("OK"){ _,_ ->
                showOtpActivity()
            }

            setNegativeButton("Edit"){ dialog, which ->
               dialog.dismiss()
            }

            setCancelable(false)
            create()
            show()


        }
    }

    private fun showOtpActivity() {
        startActivity(Intent(this,OtpActivity::class.java).putExtra(PHONE_NUMBER,PhoneNumber))
        finish()
    }
}