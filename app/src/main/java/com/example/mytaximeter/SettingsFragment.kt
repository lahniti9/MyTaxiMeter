package com.example.mytaximeter

import android.content.Intent
import android.content.SharedPreferences
import android.content.res.Configuration
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Switch
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.Fragment
import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.auth.FirebaseAuth
import java.util.Locale

class SettingsFragment : Fragment() {

    // Define SharedPreferences for language storage
    private lateinit var sharedPreferences: SharedPreferences
    private val languageKey = "language_key" // Key for saving the language preference

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Bind user interface
        val view = inflater.inflate(R.layout.fragment_settings, container, false)

        // Initialize SharedPreferences
        sharedPreferences = requireActivity().getSharedPreferences("app_preferences", 0)

        // Set saved language when the fragment is created
        val savedLanguage = sharedPreferences.getString(languageKey, "en")
        savedLanguage?.let {
            setLanguage(it)
        }

        // Interface elements
        val switchDarkMode = view.findViewById<Switch>(R.id.switch_dark_mode)
        val btnChangeLanguage = view.findViewById<Button>(R.id.btn_change_language)
        val btnLogout: MaterialButton = view.findViewById(R.id.btn_logout)

        // Enable dark mode
        switchDarkMode.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            }
        }

        // Change language
        btnChangeLanguage.setOnClickListener {
            showLanguageDialog()
        }

        // Log out from the app
        val user = FirebaseAuth.getInstance().currentUser
        btnLogout.setOnClickListener {
            // Show confirmation dialog for logging out
            val dialog = MaterialAlertDialogBuilder(requireContext())
            dialog.setTitle("LogOut")
            dialog.setMessage("Do you really want to logout?")
            dialog.setIcon(R.drawable.ic_baseline_login_24)

            dialog.setPositiveButton("Yeah") { _, _ ->
                // Sign out
                FirebaseAuth.getInstance().signOut()

                // Redirect to login page
                val intent = Intent(requireActivity(), LoginActivity::class.java)
                startActivity(intent)
                Toast.makeText(requireContext(), "Signed Out", Toast.LENGTH_SHORT).show()
                requireActivity().finish() // Finish the current Activity
            }

            dialog.setNeutralButton("Cancel") { _, _ ->
                Toast.makeText(requireContext(), "Cancelled", Toast.LENGTH_SHORT).show()
            }

            dialog.create()
            dialog.setCancelable(false)  // It's better to make the user choose either "Yes" or "Cancel"
            dialog.show()
        }

        return view
    }

    private fun showLanguageDialog() {
        val languages = arrayOf("Arabic", "English", "Français")
        AlertDialog.Builder(requireContext())
            .setTitle(getString(R.string.change_language))
            .setItems(languages) { _, which ->
                when (which) {
                    0 -> setLanguage("ar") // Arabic
                    1 -> setLanguage("en") // English
                    2 -> setLanguage("fr") // French
                }
            }.show()
    }

    private fun setLanguage(languageCode: String) {
        // Save the selected language to SharedPreferences
        sharedPreferences.edit().putString(languageKey, languageCode).apply()

        val locale = Locale(languageCode)
        Locale.setDefault(locale)
        val config = Configuration(requireContext().resources.configuration)
        config.setLocale(locale)

        // Update context with the new language
        val context = requireContext().createConfigurationContext(config)
        requireActivity().apply {
            resources.updateConfiguration(config, resources.displayMetrics)
        }

        // Remove the call to recreate the activity to avoid going back to home page
    }
}
