package com.example.exacthealth.activities

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.exacthealth.R
import com.example.exacthealth.fragments.CalendarFragment
import com.example.exacthealth.fragments.HealthFragment
import com.example.exacthealth.fragments.ProfileFragment
import com.google.android.material.bottomnavigation.BottomNavigationView

class CalendarActivity : AppCompatActivity()
{

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_calendar)

        // Set the default fragment (CalendarFragment)
        replaceFragment(CalendarFragment())

        // BottomNavigationView setup for switching between fragments
        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottom_navigation)
        bottomNavigationView.selectedItemId = R.id.calendar

        bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId)
            {
                R.id.health   ->
                {
                    replaceFragment(HealthFragment())
                    true
                }

                R.id.calendar ->
                {
                    replaceFragment(CalendarFragment())
                    true
                }

                R.id.profile  ->
                {
                    replaceFragment(ProfileFragment())
                    true
                }

                else          -> false
            }
        }
    }

    // Helper method to replace the current fragment
    private fun replaceFragment(fragment: Fragment)
    {
        val transaction = supportFragmentManager.beginTransaction()
        transaction.replace(R.id.fragment_container, fragment)
        transaction.commit()
    }
}
