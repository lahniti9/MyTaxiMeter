package com.example.mytaximeter


import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
class EditProfileActivity : AppCompatActivity() {

    private lateinit var etName: EditText
    private lateinit var etEmail: EditText
    private lateinit var etLicenseType: EditText
    private lateinit var etVehicleNumber: EditText
    private lateinit var etAddress: EditText
    private lateinit var etPhone: EditText
    private lateinit var btnSave: Button

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private var userId: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_profile)

        // Initialize Firebase Auth and Firestore
        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        // Initialize UI components
        etName = findViewById(R.id.et_name)
        etEmail = findViewById(R.id.et_email)
        etLicenseType = findViewById(R.id.et_license_type)
        etVehicleNumber = findViewById(R.id.et_vehicle_number)
        etAddress = findViewById(R.id.et_address)
        etPhone = findViewById(R.id.et_phone)
        btnSave = findViewById(R.id.btn_save)

        // Get user ID from FirebaseAuth
        userId = auth.currentUser?.uid ?: ""

        // Load user data for editing
        loadUserData()

        // Handle save button click
        btnSave.setOnClickListener {
            saveUserProfile()
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }
    }

    private fun loadUserData() {
        db.collection("drivers").document(userId).get().addOnSuccessListener { document ->
            if (document.exists()) {
                val driver = document.data
                if (driver != null) {
                    etName.setText(driver["name"] as String)
                    etEmail.setText(driver["email"] as String)
                    etLicenseType.setText(driver["licenseType"] as String)
                    etVehicleNumber.setText(driver["vehicleNumber"] as String)
                    etAddress.setText(driver["address"] as String)
                    etPhone.setText(driver["phone"] as String)
                }
            }
        }
    }

    private fun saveUserProfile() {
        val updatedData = mapOf(
            "name" to etName.text.toString(),
            "email" to etEmail.text.toString(),
            "licenseType" to etLicenseType.text.toString(),
            "vehicleNumber" to etVehicleNumber.text.toString(),
            "address" to etAddress.text.toString(),
            "phone" to etPhone.text.toString()
        )

        db.collection("drivers").document(userId).set(updatedData)
            .addOnSuccessListener {
                // Send updated data back to FragmentProfile
                val updatedDataIntent = Intent().apply {
                    putExtra("name", etName.text.toString())
                    putExtra("email", etEmail.text.toString())
                    putExtra("licenseType", etLicenseType.text.toString())
                    putExtra("vehicleNumber", etVehicleNumber.text.toString())
                    putExtra("address", etAddress.text.toString())
                    putExtra("phone", etPhone.text.toString())
                }
                setResult(RESULT_OK, updatedDataIntent)
                Toast.makeText(this, "Profile updated successfully!", Toast.LENGTH_SHORT).show()

                finish() // Close activity and return to FragmentProfile
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to update profile.", Toast.LENGTH_SHORT).show()
            }
    }
}
