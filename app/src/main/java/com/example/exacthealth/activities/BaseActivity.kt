package com.example.exacthealth.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity

open class BaseActivity: AppCompatActivity()
{

    companion object
    {
        var wasAppMinimized = false // Track if app was minimized
        var lastOpenedActivity: String? = null // Track last opened activity
    }

    override fun onUserLeaveHint()
    {
        super.onUserLeaveHint()
        wasAppMinimized = true
    }

    override fun onPause()
    {
        super.onPause()
        wasAppMinimized = false
    }

    override fun onResume()
    {
        super.onResume()

        val sharedPreferences = getSharedPreferences("user_session", MODE_PRIVATE)
        val isLoggedIn = sharedPreferences.getBoolean("IS_LOGGED_IN", false)

        // Ensure we only go to LoadingActivity if app was minimized, NOT when pressing back
        if (wasAppMinimized && isLoggedIn && lastOpenedActivity != this::class.java.simpleName)
        {
            wasAppMinimized = false

            val intent = Intent(this, LoadingActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }

        // Update last opened activity
        lastOpenedActivity = this::class.java.simpleName
    }
}
