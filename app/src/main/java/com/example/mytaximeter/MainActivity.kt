package com.example.mytaximeter

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // شريط التنقل السفلي
        val bottomNavigation = findViewById<BottomNavigationView>(R.id.bottom_navigation)
        bottomNavigation.setOnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    loadFragment(HomeFragment())  // تحميل Fragment الصفحة الرئيسية
                    true
                }
                R.id.nav_history -> {
                    loadFragment(HistoryFragment())  // تحميل Fragment التاريخ
                    true
                }
                R.id.nav_settings -> {
                    loadFragment(SettingsFragment())  // تحميل Fragment الإعدادات
                    true
                }
                R.id.nav_profile -> {
                    loadFragment(FragmentProfile())  // تحميل Fragment الملف الشخصي
                    true
                }

                else -> false
            }
        }


        // تحديد الشاشة الافتراضية
        bottomNavigation.selectedItemId = R.id.nav_home
    }

    private fun loadFragment(fragment: Fragment) {
        val transaction = supportFragmentManager.beginTransaction()
        transaction.replace(R.id.fragment_container, fragment) // R.id.fragment_container هو الـ container الذي ستضع فيه الـ fragment
        transaction.commit()
    }

}


