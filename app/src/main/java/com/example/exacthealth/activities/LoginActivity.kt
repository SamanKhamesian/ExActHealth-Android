package com.example.exacthealth.activities

import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.exacthealth.R
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import java.io.IOException

class LoginActivity : AppCompatActivity()
{
    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        val loginButton = findViewById<Button>(R.id.login_button)
        val usernameEditText = findViewById<EditText>(R.id.login_username)
        val passwordEditText = findViewById<EditText>(R.id.login_password)

        loginButton.setOnClickListener {
            sendRequest()
        }
    }


    private fun sendRequest()
    {
        val client = OkHttpClient()
        val request =
            Request.Builder()
                .url("http://mayo.abdullah-mamun.com:7000/t1d/get_users/")
                .get()
                .addHeader("Host", "<calculated when request is sent>")
                .addHeader("User-Agent", "PostmanRuntime/7.40.0")
                .addHeader("Accept", "*/*")
                .addHeader("Accept-Encoding", "gzip, deflate, br")
                .addHeader("Connection", "keep-alive")
                .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.d("Fail Request", e.toString())
                e.printStackTrace()
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful && response.code == 200) {
                    val responseData = response.body?.string()
                    Log.d("Successful", responseData.toString())
                    println(responseData)
                } else {
                    // Handle the case where the server response is not successful
                    println("Failed Response: ${response.code}")
                }
            }
        })
    }
}