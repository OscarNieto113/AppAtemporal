package com.example.appatemporal.framework.view


import android.content.Context
import android.content.Intent
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.*
import androidx.activity.viewModels
import androidx.lifecycle.Observer
import com.example.appatemporal.data.localdatabase.entities.Usuario
import com.example.appatemporal.databinding.ActivityOtpactivityBinding
import com.example.appatemporal.domain.Repository
import com.example.appatemporal.framework.viewModel.OTPViewModel
import com.google.firebase.FirebaseException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.*
import java.util.concurrent.TimeUnit

/**
 * Class that inherits from AppCompatActivity
 */
class OTPActivity : AppCompatActivity() {

    private lateinit var auth : FirebaseAuth
    private lateinit var binding: ActivityOtpactivityBinding
    private lateinit var OTP: String
    private lateinit var resendToken: PhoneAuthProvider.ForceResendingToken
    private lateinit var phoneNumber: String
    private val otpViewModel: OTPViewModel by viewModels()

    /**
     * Overrides function onCreate and starts the activity
     *
     * @param savedInstanceState: Bundle? -> Saved instance of the activity
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        auth = FirebaseAuth.getInstance()

        binding = ActivityOtpactivityBinding.inflate(layoutInflater)
        setContentView(binding.root)

        OTP = intent.getStringExtra("OTP").toString()
        resendToken = intent.getParcelableExtra("resendToken")!!
        phoneNumber = intent.getStringExtra("phoneNumber")!!

        binding.otpProgressBar.visibility = View.INVISIBLE
        binding.otpEditText1.requestFocus()
        addTextChangeListener()
        resendOTPvVisibility()

        binding.resendTextView.setOnClickListener {
            resenderVerificationCode()
            resendOTPvVisibility()
        }

        binding.verifyOTPBtn.setOnClickListener {
            val typedOTP = binding.otpEditText1.text.toString() + binding.otpEditText2.text.toString() + binding.otpEditText3.text.toString() +
                    binding.otpEditText4.text.toString() + binding.otpEditText5.text.toString() + binding.otpEditText6.text.toString()
            if (typedOTP.isNotEmpty()){
                if (typedOTP.length == 6){
                    val credential : PhoneAuthCredential = PhoneAuthProvider.getCredential(
                        OTP, typedOTP
                    )
                    binding.otpProgressBar.visibility = View.VISIBLE
                    signInWithPhoneAuthCredential(credential)
                }else{
                    Toast.makeText( this, "Código incorrecto", Toast.LENGTH_SHORT).show()
                }
            }else{
                Toast.makeText( this, "Ingresa el código envíado a tu celular", Toast.LENGTH_SHORT).show()
            }
        }

    }

    /**
     * Sets the availability of the resend token function
     */
    private fun resendOTPvVisibility(){
        binding.otpEditText1.setText("")
        binding.otpEditText2.setText("")
        binding.otpEditText3.setText("")
        binding.otpEditText4.setText("")
        binding.otpEditText5.setText("")
        binding.otpEditText6.setText("")
        binding.resendTextView.isEnabled = false
        binding.resendTextView.setTextColor(Color.parseColor("#606060"))

        Handler(Looper.myLooper()!!).postDelayed(Runnable{
            binding.resendTextView.setTextColor(Color.parseColor("#E4EBF1"))
            binding.resendTextView.isEnabled = true
        },60000)
    }

    /**
     * Resends the token to the phone number
     */
    private fun resenderVerificationCode(){
        val options = PhoneAuthOptions.newBuilder(auth)
            .setPhoneNumber(phoneNumber)       // Phone number to verify
            .setTimeout(60L, TimeUnit.SECONDS) // Timeout and unit
            .setActivity(this)                 // Activity (for callback binding)
            .setCallbacks(callbacks)          // OnVerificationStateChangedCallbacks
            .build()
        PhoneAuthProvider.verifyPhoneNumber(options)
    }

    private val callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

        /**
         * Depending on the device used, it will verify the OTP
         * credential by receiving the message
         *
         * @param credential: PhoneAuthCredential -> Credential sent by Firebase
         */
        override fun onVerificationCompleted(credential: PhoneAuthCredential) {
            //signInWithPhoneAuthCredential(credential)
        }

        /**
         * Logs to console if verification failed due to
         * invalid credentials or excessive amount of requests
         *
         * @param e: FirebaseException -> Exception returned by Firebase when verification fails
         */
        override fun onVerificationFailed(e: FirebaseException) {
            if (e is FirebaseAuthInvalidCredentialsException) {
                // Invalid request\
                Log.d("TAG", "onVerificationFailed: ${e.toString()}")
            } else if (e is FirebaseTooManyRequestsException) {
                // The SMS quota for the project has been exceeded
                Log.d("TAG", "onVerificationFailed: ${e.toString()}")
            }
        }

        /**
         * Sends the SMS code to phone number
         *
         * @param verificationId: String -> The 6 digits of the SMS sent by Firebase Authentication
         * @param token: PhoneAuthProvider.ForceResentingToken -> Token used by Firebase to send the OTP
         */
        override fun onCodeSent(
            verificationId: String,
            token: PhoneAuthProvider.ForceResendingToken
        ) {
            OTP = verificationId
            resendToken = token
        }
    }

    /**
     * Verifies the SMS entered by the user and authenticates the user
     * if its correct. If it's an existing user it will send him to
     * the main page according to role, on the other hand, it will
     * send the user to a register form
     *
     * @param credential: PhoneAuthCredential -> credential sent by Firebase
     */
    private fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential) {
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    binding.otpProgressBar.visibility = View.VISIBLE
                    Toast.makeText(this,"Autenticación exitosa", Toast.LENGTH_SHORT).show()

                    val uid: String = auth.currentUser?.uid.toString()

                    val repository = Repository(this)
                    otpViewModel.verifyUser(uid, repository)

                    otpViewModel.userExists.observe(this, Observer {
                        val existence = it as Boolean
                        if (existence) {
                            val userUidSharedPref = getSharedPreferences("user", Context.MODE_PRIVATE)
                            var userSharedPrefEdit = userUidSharedPref.edit()
                            userSharedPrefEdit.putString("userUid", uid)
                            userSharedPrefEdit.apply()
                            otpViewModel.getUser(uid, repository)
                            otpViewModel.userData.observe(this, Observer {
                                val localDbUser = it
                                otpViewModel.addUserLocalDB(localDbUser, repository)
                                userSharedPrefEdit.putString("rol", it.rol)
                                userSharedPrefEdit.apply()
                                if(it.rol == "Organizador"){
                                    val intent = Intent(this, ActivityMainHomepageOrganizador::class.java)
                                    startActivity(intent)
                                }else{
                                    val intent = Intent(this, ActivityMainHomepageEspectador::class.java)
                                    startActivity(intent)
                                }
                            })
                        } else {
                            val intent = Intent(this, RegisterActivity::class.java)
                            intent.putExtra("userUid", uid)
                            startActivity(intent)
                        }
                    })
                } else {
                    // Sign in failed, display a message and update the UI
                    Log.d("TAG","SignInWithPhoneAuthCredential: ${task.exception.toString()}")
                    if (task.exception is FirebaseAuthInvalidCredentialsException) {
                        // The verification code entered was invalid
                        Toast.makeText(this, "SMS Incorrecto", Toast.LENGTH_SHORT ).show()
                    }
                    // Update UI
                }
            }
    }

    /**
     * Set text change listeners to edit texts for SMS verification code
     */
    private fun addTextChangeListener(){
        binding.otpEditText1.addTextChangedListener(EditTextWatcher(binding.otpEditText1))
        binding.otpEditText2.addTextChangedListener(EditTextWatcher(binding.otpEditText2))
        binding.otpEditText3.addTextChangedListener(EditTextWatcher(binding.otpEditText3))
        binding.otpEditText4.addTextChangedListener(EditTextWatcher(binding.otpEditText4))
        binding.otpEditText5.addTextChangedListener(EditTextWatcher(binding.otpEditText5))
        binding.otpEditText6.addTextChangedListener(EditTextWatcher(binding.otpEditText6))
    }

    /**
     * Gets changes in different instants of an edit text
     *
     * @param view: View -> reference to a view component
     */
    inner class EditTextWatcher(private val view : View) : TextWatcher {
        override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

        }

        override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

        }

        /**
         * Changes focus of an edit text when after it is changed
         *
         * @param p0: Editable -> content of the edit text
         */
        override fun afterTextChanged(p0: Editable?) {
            val text = p0.toString()
            when(view.id){
                binding.otpEditText1.id -> if(text.length == 1 ) binding.otpEditText2.requestFocus() else if (text.isEmpty()) binding.otpEditText1.requestFocus()
                binding.otpEditText2.id -> if(text.length == 1 ) binding.otpEditText3.requestFocus() else if (text.isEmpty()) binding.otpEditText1.requestFocus()
                binding.otpEditText3.id -> if(text.length == 1 ) binding.otpEditText4.requestFocus() else if (text.isEmpty()) binding.otpEditText2.requestFocus()
                binding.otpEditText4.id -> if(text.length == 1 ) binding.otpEditText5.requestFocus() else if (text.isEmpty()) binding.otpEditText3.requestFocus()
                binding.otpEditText5.id -> if(text.length == 1 ) binding.otpEditText6.requestFocus() else if (text.isEmpty()) binding.otpEditText4.requestFocus()
                binding.otpEditText6.id -> if(text.isEmpty()) binding.otpEditText5.requestFocus()
            }
        }
    }
}



