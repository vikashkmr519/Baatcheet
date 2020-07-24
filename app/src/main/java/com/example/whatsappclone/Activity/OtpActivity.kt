package com.example.whatsappclone.Activity

import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.CountDownTimer
import android.provider.ContactsContract
import android.text.SpannableString
import android.text.Spanned
import android.text.TextPaint
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.core.view.isVisible
import com.example.whatsappclone.R
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.FirebaseException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthProvider
import kotlinx.android.synthetic.main.activity_otp.*
import java.util.*
import java.util.concurrent.TimeUnit


const val PHONE_NUMBER ="phoneNumber"
class OtpActivity : AppCompatActivity(), View.OnClickListener {


    lateinit var callbacks:PhoneAuthProvider.OnVerificationStateChangedCallbacks
    var phoneNumber:String? = null
    var mverificationId: String? = null
    var mResendToken : PhoneAuthProvider.ForceResendingToken?= null
    private lateinit var progressDialog :ProgressDialog
    private var mCounterDown:CountDownTimer?= null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_otp)

        initViews()
        startVerify()

    }

    private fun startVerify() {

        startPhoneNumberVerification(phoneNumber!!)

        showTimer(60000)
        progressDialog = createProgressDialog("Sending a verification code",false)
        progressDialog.show()
    }

    private fun showTimer(miliSecInFuture:Long) {
        resendBtn.isEnabled = false
       mCounterDown = object : CountDownTimer(miliSecInFuture,1000){
            override fun onFinish() {
                resendBtn.isEnabled = true
                counterTv.isVisible = false
            }

            override fun onTick(millisUntilFinished: Long) {
                counterTv.isVisible = true
                counterTv.text = getString(R.string.second_remaining,millisUntilFinished/1000)
            }

        }.start()
    }


    // This method will send a code to a given phone number as an SMS
    private fun startPhoneNumberVerification(phoneNumber: String) {
        PhoneAuthProvider.getInstance().verifyPhoneNumber(
            phoneNumber,      // Phone number to verify
            60,               // Timeout duration
            TimeUnit.SECONDS, // Unit of timeout
            this,            // Activity (for callback binding)
            callbacks
        ) // OnVerificationStateChangedCallbacks
    }

    override fun onDestroy() {
        super.onDestroy()
        if(mCounterDown!=null){
            mCounterDown!!.cancel()
        }
    }

    private fun initViews() {
        phoneNumber = intent.getStringExtra(PHONE_NUMBER)
        verifyTv.text = "Verify $phoneNumber"
        setSpannableString()

        //init click listener
        verificationBtn.setOnClickListener(this)
        resendBtn.setOnClickListener(this)


        //init fire base verify phone number callback
        callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

            override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                // This callback will be invoked in two situations:
                // 1 - Instant verification. In some cases the phone number can be instantly
                //     verified without needing to send or enter a verification code.
                // 2 - Auto-retrieval. On some devices Google Play services can automatically
                //     detect the incoming verification SMS and perform verification without
                //     user action.

                if(::progressDialog.isInitialized){
                    progressDialog.dismiss()
                }
                val smsMessage = credential.smsCode
                if(!smsMessage.isNullOrBlank())
                    sentcodeEt.setText(smsMessage)

                signInWithPhoneAuthCredential(credential)
            }

            override fun onVerificationFailed(e: FirebaseException) {
                // This callback is invoked in an invalid request for verification is made,
                // for instance if the the phone number format is not valid.

                if (::progressDialog.isInitialized) {
                    progressDialog.dismiss()
                }
                if (e is FirebaseAuthInvalidCredentialsException) {
                    // Invalid request
                    // ...
                    Log.e("Exception:", "FirebaseAuthInvalidCredentialsException", e)
                    Log.e("=========:", "FirebaseAuthInvalidCredentialsException " + e.message)

                } else if (e is FirebaseTooManyRequestsException) {
                    // The SMS quota for the project has been exceeded
                    // ...

                    Log.e("Exception:", "FirebaseTooManyRequestsException", e)
                }

                // Show a message and update the UI
                // ...
                notifyUserAndRetry("Your Phone Number might be wrong or connection error.Retry again!")
            }

            override fun onCodeSent(
                verificationId: String,
                token: PhoneAuthProvider.ForceResendingToken
            ) {
                // The SMS verification code has been sent to the provided phone number, we
                // now need to ask the user to enter the code and then construct a credential
                // by combining the code with a verification ID.

                // Save verification ID and resending token so we can use them later
                progressDialog.dismiss()
                counterTv.isVisible = false

                // Save verification ID and resending token so we can use them later
                Log.e("onCodeSent==", "onCodeSent:$verificationId")
                mverificationId = verificationId
                mResendToken = token

                // ...
            }
        }
    }

    private fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential) {
    val mAuth = FirebaseAuth.getInstance()
        mAuth.signInWithCredential(credential)
            .addOnCompleteListener(this) {
                if(it.isSuccessful){
                    startActivity(
                        Intent(this,SignUpActivity::class.java)
                    )
                    finish()
                }else{
                    notifyUserAndRetry("Your Phone Number Verification failed. try Again !!")
                }
            }
    }

    private fun notifyUserAndRetry(message: String) {
        MaterialAlertDialogBuilder(this).apply {
            setMessage(message)
            setPositiveButton("Ok"){ _, _ ->
                showLoginActivity()

            }

            setNegativeButton("Cancel"){ dialog, _ ->
                dialog.dismiss()

            }
            setCancelable(false)
            create()
            show()
        }
    }

    private fun setSpannableString() {
        val span = SpannableString(getString(R.string.waiting_text, phoneNumber))
        val clickableSpan = object: ClickableSpan(){
            override fun onClick(widget: View) {
                showLoginActivity()
            }

            override fun updateDrawState(ds: TextPaint) {
                super.updateDrawState(ds)
                ds.isUnderlineText=  false
                ds.color =ds.linkColor
            }

        }
        span.setSpan(clickableSpan,span.length-13,span.length,Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        waitingTv.movementMethod = LinkMovementMethod.getInstance()
        waitingTv.text = span
    }

    private fun showLoginActivity() {
        startActivity(Intent(this,LoginActivity::class.java)
            .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK))

    }

    override fun onBackPressed() {

    }

    override fun onClick(v: View?) {
         when(v){
             verificationBtn -> {
                 var code = sentcodeEt.text.toString()
                 if (code.isNotEmpty() && !mverificationId.isNullOrBlank()) {

                     progressDialog = createProgressDialog("Please wait...", false)
                     progressDialog.show()


                     val credential = PhoneAuthProvider.getCredential(mverificationId!!, code.toString())
                     signInWithPhoneAuthCredential(credential)

                 }
             }

                 resendBtn -> {

                     if(mResendToken != null){
                         showTimer(60000)

                         progressDialog = createProgressDialog("Sending a verification code",false)
                         progressDialog.show()


                        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                           phoneNumber!!,
                            60,
                            TimeUnit.SECONDS,
                            this,
                            callbacks,
                            mResendToken
                        )

                     }else{
                         Toast.makeText(this, "Sorry, You can't request new code now, Please wait ...", Toast.LENGTH_SHORT).show()
                     }
             }
         }
    }
    fun Context.createProgressDialog(message:String,isCancelable:Boolean):ProgressDialog{
        return ProgressDialog(this).apply {
            setCancelable(false)
            setMessage(message)
            setCanceledOnTouchOutside(false)
        }
    }
}

