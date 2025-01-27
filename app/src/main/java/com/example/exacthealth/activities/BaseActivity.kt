package com.example.exacthealth.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity

open class BaseActivity: AppCompatActivity()
{
    open class BaseActivity : AppCompatActivity() {

        companion object {
            var wasAppMinimized = false // Shared state to track app minimization
        }

        override fun onStop() {
            super.onStop()

            // If the activity is going to the background, set the app as minimized
            wasAppMinimized = true
        }

        override fun onStart() {
            super.onStart()

            // Reset the minimization state when returning to the activity normally
            wasAppMinimized = false
        }

        override fun onRestart() {
            super.onRestart()

            // Only redirect to LoadingActivity if the app was minimized
            if (wasAppMinimized) {
                wasAppMinimized = false // Reset the state

                val sharedPreferences = getSharedPreferences("user_session", MODE_PRIVATE)
                val isLoggedIn = sharedPreferences.getBoolean("IS_LOGGED_IN", false)

                if (isLoggedIn) {
                    // Redirect to LoadingActivity
                    val intent = Intent(this, LoadingActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                    finish() // Close the current activity
                }
            }
        }
    }

}
