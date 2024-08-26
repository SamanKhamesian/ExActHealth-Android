package com.example.exacthealth.activities

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.StrictMode
import android.util.Log
import android.view.View.GONE
import android.view.View.VISIBLE
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.exacthealth.R
import com.example.exacthealth.classes.isInternetAvailable
import com.example.exacthealth.classes.showNoInternetDialog
import com.google.android.material.textfield.TextInputEditText
import java.net.HttpURLConnection
import java.net.URL

class LoginActivity : AppCompatActivity()
{
    private var CSRF_TOKEN: String = ""
    private var SAVED_COOKIE: String? = ""
    lateinit var errorTextView: TextView

    companion object
    {
        const val TEST_CONNECTION_URL = "https://mayo.abdullah-mamun.com/t1d/testConnection/"
        const val LOGIN_URL = "https://mayo.abdullah-mamun.com/t1d/login/"
        const val SIGN_UP_URL = "https://mayo.abdullah-mamun.com/t1d/sign-up/"
    }

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        val loginButton = findViewById<Button>(R.id.login_button)
        val signupButton = findViewById<Button>(R.id.signup_button)

        val usernameEditText = findViewById<TextInputEditText>(R.id.login_username)
        val passwordEditText = findViewById<TextInputEditText>(R.id.login_password)
        errorTextView = findViewById<TextView>(R.id.login_error_message)

        testConnection()

        loginButton.setOnClickListener {
            testConnection()
            loginConnection(username = usernameEditText.text.toString(), password = passwordEditText.text.toString())
        }

        signupButton.setOnClickListener {
            testConnection()
            signUpConnection(username = usernameEditText.text.toString(), password = passwordEditText.text.toString())
        }
    }

    private fun testConnection()
    {
        val url = URL(TEST_CONNECTION_URL)
        val (responseCode, responseText) = testRequest(url)
        Log.d("Response Code: ", "$responseCode")
        Log.d("Response Text: ", responseText)

        when (responseCode)
        {
            200  ->
            {
                CSRF_TOKEN = responseText.substringAfter("name=\"csrfmiddlewaretoken\" value=\"").substringBefore("\">")
                errorTextView.visibility = GONE
            }

            503  ->
            {
                showNoInternetDialog(this) { testConnection() }
                errorTextView.visibility = GONE
            }

            else ->
            {
                errorTextView.text = responseText
                errorTextView.visibility = VISIBLE
            }
        }
    }

    private fun testRequest(url: URL): Pair<Int, String>
    {
        val policy = StrictMode.ThreadPolicy.Builder().permitAll().build()
        StrictMode.setThreadPolicy(policy)

        if (!isInternetAvailable(this))
        {
            return Pair(503,
                        "Service Unavailable: Unable to connect to the server. Please check your internet connection.")
        }

        return try
        {
            // Use the main request to check for connectivity and server availability
            with(url.openConnection() as HttpURLConnection) {
                requestMethod = "GET"

                // Retrieve cookies from the response header
                val cookieHeader = getHeaderField("Set-Cookie") ?: throw Exception("Failed to retrieve cookies")

                SAVED_COOKIE = cookieHeader

                val responseText = buildString {
                    append("Successful Connection\n")
                    inputStream.bufferedReader().forEachLine { append(it).append("\n") }
                }

                // If the request is successful, return the server's response
                Pair(responseCode, responseText)
            }
        }
        catch (e: java.net.UnknownHostException)
        {
            // This exception indicates that the URL could not be resolved, likely due to no internet
            Pair(404, "Unknown Host: Unable to resolve the server's hostname. Please check your internet connection.")
        }
        catch (e: java.net.SocketTimeoutException)
        {
            // This exception indicates that the server did not respond within the expected time frame
            Pair(408, "Request Timeout: The server took too long to respond. Please try again later.")
        }
        catch (e: java.net.ConnectException)
        {
            // This exception indicates that there was a problem connecting to the server, possibly due to no internet
            Pair(503, "Service Unavailable: Unable to connect to the server. Please check your internet connection.")
        }
        catch (e: Exception)
        {
            // Handle any other exceptions that might occur
            e.printStackTrace()
            Pair(500, "Connection Failed: Please try again later.")
        }
    }

    private fun loginConnection(username: String, password: String)
    {
        val url = URL(LOGIN_URL)
        val params = ArrayList<Pair<String, String>>()
        params.add(Pair("username", username))
        params.add(Pair("password", password))
        params.add(Pair("csrfmiddlewaretoken", CSRF_TOKEN))
        val (responseCode, responseText) = loginRequest(url, params)

        errorTextView.text = responseText
        errorTextView.visibility = VISIBLE

        Log.d("Response Code: ", "$responseCode")
        Log.d("Response Text: ", responseText)

        if (responseCode == 200)
        {
            Handler(Looper.getMainLooper()).postDelayed({
                                                            val intent = Intent(this, LoadingActivity::class.java)
                                                            intent.putExtra("username", username)
                                                            startActivity(intent)
                                                            finish()
                                                        }, 2000)
        }
    }

    private fun loginRequest(url: URL, params: List<Pair<String, String>>): Pair<Int, String>
    {
        val policy = StrictMode.ThreadPolicy.Builder().permitAll().build()
        StrictMode.setThreadPolicy(policy)

        return try
        {
            with(url.openConnection() as HttpURLConnection) {
                requestMethod = "POST"
                doOutput = true

                // Check if SAVED_COOKIE is null and throw an exception if it is
                val cookie = SAVED_COOKIE ?: throw Exception("SAVED_COOKIE is null")

                addRequestProperty("Cookie", cookie)
                addRequestProperty("Content-Type", "application/x-www-form-urlencoded")

                // Prepare POST parameters
                val postData = params.joinToString("&") { "${it.first}=${it.second}" }

                outputStream.bufferedWriter().use { it.write(postData) }

                val responseText = buildString {
                    // append("Response Message: $responseMessage\n")
                    inputStream.bufferedReader().forEachLine { append(it) }
                }

                Pair(responseCode, responseText)
            }
        }
        catch (e: Exception)
        {
            e.printStackTrace()
            Pair(405, "There is an unknown error. Please try again later.")
        }
    }

    private fun signUpConnection(username: String, password: String)
    {
        val url = URL(SIGN_UP_URL)
        val params = ArrayList<Pair<String, String>>()
        params.add(Pair("username", username))
        params.add(Pair("userage", "30"))
        params.add(Pair("password", password))
        params.add(Pair("csrfmiddlewaretoken", CSRF_TOKEN))
        val (responseCode, responseText) = signUpRequest(url, params)

        errorTextView.text = responseText
        errorTextView.visibility = VISIBLE

        Log.d("Response Code: ", "$responseCode")
        Log.d("Response Text: ", responseText)

        if (responseCode == 200)
        {
            Handler(Looper.getMainLooper()).postDelayed({
                val intent = Intent(this, LoadingActivity::class.java)
                intent.putExtra("username", username)
                startActivity(intent)
                finish() }, 2000)
        }
    }

    private fun signUpRequest(url: URL, params: List<Pair<String, String>>): Pair<Int, String>
    {
        val policy = StrictMode.ThreadPolicy.Builder().permitAll().build()
        StrictMode.setThreadPolicy(policy)

        return try
        {
            with(url.openConnection() as HttpURLConnection) {
                requestMethod = "POST"
                doOutput = true

                // Check if SAVED_COOKIE is null and throw an exception if it is
                val cookie = SAVED_COOKIE ?: throw Exception("SAVED_COOKIE is null")

                addRequestProperty("Cookie", cookie)
                addRequestProperty("Content-Type", "application/x-www-form-urlencoded")

                // Prepare POST parameters
                val postData = params.joinToString("&") { "${it.first}=${it.second}" }

                outputStream.bufferedWriter().use { it.write(postData) }

                val responseText = buildString {
                    // append("Response Message: $responseMessage\n")
                    inputStream.bufferedReader().forEachLine { append(it) }
                }

                Pair(responseCode, responseText)
            }
        }
        catch (e: Exception)
        {
            e.printStackTrace()
            Pair(405, "Exception Failed: ${e.message}")
        }
    }
}
