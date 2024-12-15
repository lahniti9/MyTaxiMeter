package com.example.mytaximeter

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.util.Patterns
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputLayout
import com.google.android.material.textview.MaterialTextView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore

class SignupActivity : AppCompatActivity() {

    // Firebase Authentication and Firestore
    private lateinit var auth: FirebaseAuth
    private lateinit var user: FirebaseUser

    // UI Components
    private lateinit var etNameSignup: TextInputLayout
    private lateinit var etEmailIdSignup: TextInputLayout
    private lateinit var etPasswordSignup: TextInputLayout
    private lateinit var etRePasswordSignup: TextInputLayout
    private lateinit var etDriverLicenseType: TextInputLayout
    private lateinit var etVehicleNumber: TextInputLayout
    private lateinit var etDriverAddress: TextInputLayout
    private lateinit var etDriverPhone: TextInputLayout
    private lateinit var btnSignup: MaterialButton
    private lateinit var textviewReferLogin: MaterialTextView

    companion object {
        private const val TAG = "SignupActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signup)

        // Initialize Firebase Auth
        auth = FirebaseAuth.getInstance()
        auth.currentUser?.let {
            user = it
        }

        // Initialize UI Components
        initializeUIComponents()

        // Setup Button Listeners
        btnSignup.setOnClickListener { signUpUser() }
        textviewReferLogin.setOnClickListener { navigateToLogin() }
    }

    private fun initializeUIComponents() {
        etNameSignup = findViewById(R.id.et_name_signup)
        etEmailIdSignup = findViewById(R.id.et_email_id_signup)
        etPasswordSignup = findViewById(R.id.et_password_signup)
        etRePasswordSignup = findViewById(R.id.et_re_password_signup)
        etDriverLicenseType = findViewById(R.id.et_license_type)
        etVehicleNumber = findViewById(R.id.et_vehicle_number)
        etDriverAddress = findViewById(R.id.et_driver_address)
        etDriverPhone = findViewById(R.id.et_phone_number)
        btnSignup = findViewById(R.id.btn_signup)
        textviewReferLogin = findViewById(R.id.textview_refer_login)
    }

    private fun signUpUser() {
        if (!validateInputs()) return

        // Create user account
        auth.createUserWithEmailAndPassword(
            etEmailIdSignup.editText?.text.toString(),
            etPasswordSignup.editText?.text.toString()
        ).addOnCompleteListener(this) { task ->
            if (task.isSuccessful) {
                Log.d(TAG, "createUserWithEmail:success")
                user = auth.currentUser!!

                // Now save driver data after user registration is successful
                val driverData = hashMapOf(
                    "name" to etNameSignup.editText?.text.toString(),
                    "email" to etEmailIdSignup.editText?.text.toString(),
                    "licenseType" to etDriverLicenseType.editText?.text.toString(),
                    "vehicleNumber" to etVehicleNumber.editText?.text.toString(),
                    "address" to etDriverAddress.editText?.text.toString(),
                    "phone" to etDriverPhone.editText?.text.toString()
                )

                // Save driver data in Firestore
                saveDriverData(driverData)

                // Send email verification
                sendEmailVerification()

                // Navigate to MainActivity
                navigateToMainActivity()
            } else {
                Log.w(TAG, "createUserWithEmail:failure", task.exception)
                Toast.makeText(this, "Authentication failed.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun validateInputs(): Boolean {
        return when {
            etNameSignup.editText?.text.toString().isEmpty() -> {
                etNameSignup.error = "Please enter your Name"
                false
            }
            etEmailIdSignup.editText?.text.toString().isEmpty() -> {
                etEmailIdSignup.error = "Please enter your Email"
                false
            }
            !Patterns.EMAIL_ADDRESS.matcher(etEmailIdSignup.editText?.text.toString()).matches() -> {
                etEmailIdSignup.error = "Please enter a valid Email"
                false
            }
            etPasswordSignup.editText?.text.toString().isEmpty() -> {
                etPasswordSignup.error = "Please enter your Password"
                false
            }
            etPasswordSignup.editText?.text.toString().length !in 6..12 -> {
                etPasswordSignup.error = "Password must be 6-12 characters long"
                false
            }
            etRePasswordSignup.editText?.text.toString().isEmpty() -> {
                etRePasswordSignup.error = "Please re-enter your Password"
                false
            }
            etPasswordSignup.editText?.text.toString() != etRePasswordSignup.editText?.text.toString() -> {
                etRePasswordSignup.error = "Passwords do not match"
                false
            }
            etDriverLicenseType.editText?.text.toString().isEmpty() -> {
                etDriverLicenseType.error = "Please enter your license type"
                false
            }
            etVehicleNumber.editText?.text.toString().isEmpty() -> {
                etVehicleNumber.error = "Please enter your vehicle number"
                false
            }
            etDriverAddress.editText?.text.toString().isEmpty() -> {
                etDriverAddress.error = "Please enter your address"
                false
            }
            etDriverPhone.editText?.text.toString().isEmpty() -> {
                etDriverPhone.error = "Please enter your phone number"
                false
            }
            else -> true
        }
    }

    private fun saveDriverData(driverData: HashMap<String, String>) {
        val db = FirebaseFirestore.getInstance()
        db.collection("drivers").document(user.uid).set(driverData)
            .addOnSuccessListener {
                Log.d(TAG, "Driver data saved successfully")
                Toast.makeText(this, "SignUp Successfully!", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Log.w(TAG, "Error saving driver data", e)
                Toast.makeText(this, "Failed to save driver data.", Toast.LENGTH_SHORT).show()
            }
    }

    private fun sendEmailVerification() {
        user.let {
            it.sendEmailVerification().addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(
                        this,
                        "Verification email sent to ${etEmailIdSignup.editText?.text}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    private fun navigateToMainActivity() {
        val intent = Intent(this, MainActivity::class.java).apply {
            putExtra("USER_NAME_KEY", etNameSignup.editText?.text.toString())
        }
        startActivity(intent)
        finish()
    }

    private fun navigateToLogin() {
        startActivity(Intent(this, LoginActivity::class.java))
        finish()
    }

    override fun onStart() {
        super.onStart()
        auth.currentUser?.let { reload() }
    }

    private fun reload() {
        navigateToMainActivity()
        Toast.makeText(this, "Welcome Back!", Toast.LENGTH_SHORT).show()
    }
}
