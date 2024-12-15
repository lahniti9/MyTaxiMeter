package com.example.mytaximeter
import android.app.Activity
import android.app.Activity.RESULT_OK
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.activity.result.contract.ActivityResultContracts
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.zxing.BarcodeFormat
import com.google.zxing.common.BitMatrix
import android.graphics.Bitmap
import com.example.mytaximeter.EditProfileActivity
import com.google.zxing.qrcode.QRCodeWriter

class FragmentProfile : Fragment() {

    private lateinit var nameTextView: TextView
    private lateinit var emailTextView: TextView
    private lateinit var licenseTypeTextView: TextView
    private lateinit var vehicleNumberTextView: TextView
    private lateinit var addressTextView: TextView
    private lateinit var phoneTextView: TextView
    private lateinit var btnEdit: Button
    private lateinit var qrCodeImageView: ImageView

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    private var userId: String = ""
    private val REQUEST_CODE_EDIT = 1

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val binding = inflater.inflate(R.layout.fragment_profile, container, false)

        // Initialize Firebase Auth and Firestore
        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        // Initialize UI components
        nameTextView = binding.findViewById(R.id.textview_name)
        emailTextView = binding.findViewById(R.id.textview_email)
        licenseTypeTextView = binding.findViewById(R.id.textview_license_type)
        vehicleNumberTextView = binding.findViewById(R.id.textview_vehicle_number)
        addressTextView = binding.findViewById(R.id.textview_address)
        phoneTextView = binding.findViewById(R.id.textview_phone)
        btnEdit = binding.findViewById(R.id.btn_edit)
        qrCodeImageView = binding.findViewById(R.id.qr_code_image)

        // Get user ID from FirebaseAuth
        userId = auth.currentUser?.uid ?: ""

        // Load user data from Firestore
        loadUserProfile()

        // Handle edit button click
        btnEdit.setOnClickListener {
            val intent = Intent(requireContext(), EditProfileActivity::class.java)
            startActivityForResult(intent, REQUEST_CODE_EDIT)
        }

        return binding
    }

    private fun loadUserProfile() {
        db.collection("drivers").document(userId).get().addOnSuccessListener { document ->
            if (document.exists()) {
                val driver = document.data
                if (driver != null) {
                    nameTextView.text = "Name: ${driver["name"]}"
                    emailTextView.text = "Email: ${driver["email"]}"
                    licenseTypeTextView.text = "License Type: ${driver["licenseType"]}"
                    vehicleNumberTextView.text = "Vehicle Number: ${driver["vehicleNumber"]}"
                    addressTextView.text = "Address: ${driver["address"]}"
                    phoneTextView.text = "Phone: ${driver["phone"]}"

                    // Generate QR Code with driver's data
                    val qrData = driver.values.joinToString(", ")
                    generateQRCode(qrData)
                }
            }
        }
    }

    private fun generateQRCode(data: String) {
        val qrCodeWriter = QRCodeWriter()
        try {
            val bitMatrix: BitMatrix = qrCodeWriter.encode(data, BarcodeFormat.QR_CODE, 512, 512)
            val bitmap = Bitmap.createBitmap(512, 512, Bitmap.Config.RGB_565)
            for (x in 0 until 512) {
                for (y in 0 until 512) {
                    bitmap.setPixel(x, y, if (bitMatrix[x, y]) 0xFF000000.toInt() else -1)
                }
            }
            qrCodeImageView.setImageBitmap(bitmap)
        } catch (e: Exception) {
            Toast.makeText(requireContext(), "Error generating QR code", Toast.LENGTH_SHORT).show()
        }
    }

    // Handle the result from EditProfileActivity
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == REQUEST_CODE_EDIT && resultCode == RESULT_OK) {
            data?.let {
                val updatedName = it.getStringExtra("name") ?: ""
                val updatedEmail = it.getStringExtra("email") ?: ""
                val updatedLicenseType = it.getStringExtra("licenseType") ?: ""
                val updatedVehicleNumber = it.getStringExtra("vehicleNumber") ?: ""
                val updatedAddress = it.getStringExtra("address") ?: ""
                val updatedPhone = it.getStringExtra("phone") ?: ""

                // Update the profile with new data
                nameTextView.text = "Name: $updatedName"
                emailTextView.text = "Email: $updatedEmail"
                licenseTypeTextView.text = "License Type: $updatedLicenseType"
                vehicleNumberTextView.text = "Vehicle Number: $updatedVehicleNumber"
                addressTextView.text = "Address: $updatedAddress"
                phoneTextView.text = "Phone: $updatedPhone"

                // Generate new QR Code
                val qrData = "$updatedName, $updatedEmail, $updatedLicenseType, $updatedVehicleNumber, $updatedAddress, $updatedPhone"
                generateQRCode(qrData)
            }
        }
    }
}
