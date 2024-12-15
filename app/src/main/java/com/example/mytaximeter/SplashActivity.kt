package com.example.mytaximeter


import android.animation.ObjectAnimator
import android.animation.PropertyValuesHolder
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.splash_screen)

        // Find views by ID
        val logoImageView: ImageView = findViewById(R.id.logoImageView)
        val appNameTextView: TextView = findViewById(R.id.appNameTextView)

        // Logo Animation: Fade-In + Zoom
        ObjectAnimator.ofPropertyValuesHolder(
            logoImageView,
            PropertyValuesHolder.ofFloat("alpha", 0f, 1f), // Fade-In
            PropertyValuesHolder.ofFloat("scaleX", 0.5f, 1f), // Zoom-In (X)
            PropertyValuesHolder.ofFloat("scaleY", 0.5f, 1f)  // Zoom-In (Y)
        ).apply {
            duration = 1000
            interpolator = AccelerateDecelerateInterpolator()
            start()
        }

        // App Name Animation: Fade-In + Slide-Up
        ObjectAnimator.ofPropertyValuesHolder(
            appNameTextView,
            PropertyValuesHolder.ofFloat("alpha", 0f, 1f), // Fade-In
            PropertyValuesHolder.ofFloat("translationY", 50f, 0f) // Slide-Up
        ).apply {
            duration = 1000
            interpolator = AccelerateDecelerateInterpolator()
            start()
        }

        // Transition to MainActivity after 3 seconds
        Handler(Looper.getMainLooper()).postDelayed({
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }, 2000)
    }
}
