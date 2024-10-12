package com.example.exacthealth.fragments

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import com.example.exacthealth.R
import com.example.exacthealth.activities.LoginActivity

class ProfileFragment : Fragment()
{

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View?
    {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_profile, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?)
    {
        super.onViewCreated(view, savedInstanceState)

        // Example interaction: Logout button
        val logoutButton = view.findViewById<Button>(R.id.logout_button)
        logoutButton.setOnClickListener {
            // Access shared preferences in a fragment
            val sharedPreferences: SharedPreferences = requireActivity().getSharedPreferences("user_session", android.content.Context.MODE_PRIVATE)
            val editor = sharedPreferences.edit()
            editor.clear()  // Clear all session data
            editor.apply()

            // Redirect to the login screen
            val intent = Intent(requireActivity(), LoginActivity::class.java)
            startActivity(intent)

            // Finish the current activity (logout the user)
            requireActivity().finish()
        }
    }
}
