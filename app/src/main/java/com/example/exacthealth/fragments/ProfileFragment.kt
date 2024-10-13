package com.example.exacthealth.fragments

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.example.exacthealth.R
import com.example.exacthealth.activities.LoginActivity

class ProfileFragment : Fragment()
{
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View?
    {
        return inflater.inflate(R.layout.fragment_profile, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?)
    {
        super.onViewCreated(view, savedInstanceState)
        val sharedPreferences: SharedPreferences = requireActivity().getSharedPreferences("user_session", android.content.Context.MODE_PRIVATE)

        val username = view.findViewById<TextView>(R.id.profile_username_text_view)
        val password = view.findViewById<TextView>(R.id.profile_password_text_view)

        val savedUsername = sharedPreferences.getString("USERNAME", "") ?: ""
        val savedPassword = sharedPreferences.getString("PASSWORD", "") ?: ""

        username.text = savedUsername
        password.text = savedPassword

        val logoutButton = view.findViewById<Button>(R.id.logout_button)

        logoutButton.setOnClickListener {
            val editor = sharedPreferences.edit()
            editor.clear()
            editor.apply()

            val intent = Intent(requireActivity(), LoginActivity::class.java)
            startActivity(intent)

            requireActivity().finish()
        }
    }
}
