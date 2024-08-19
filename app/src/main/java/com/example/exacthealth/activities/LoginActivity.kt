package com.example.exacthealth.activities

import android.os.Bundle
import android.os.StrictMode
import android.util.Log
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import com.example.exacthealth.R
import java.net.HttpURLConnection
import java.net.URL

class LoginActivity : AppCompatActivity()
{
    private var CSRF_TOKEN: String = ""
    private var SAVED_COOKIE: String? = ""

    companion object
    {
        const val TEST_CONNECTION_URL = "https://mayo.abdullah-mamun.com/t1d/hello/"
        const val LOGIN_URL = "https://mayo.abdullah-mamun.com/t1d/login/"
        const val SIGN_UP_URL = "https://mayo.abdullah-mamun.com/t1d/sign-up/"
    }

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        val loginButton = findViewById<Button>(R.id.login_button)
        val signupButton = findViewById<Button>(R.id.signup_button)

        val usernameEditText = findViewById<EditText>(R.id.login_username)
        val passwordEditText = findViewById<EditText>(R.id.login_password)

        testConnection()

        loginButton.setOnClickListener {
            loginConnection(username = usernameEditText.text.toString(),
                            password = passwordEditText.text.toString())
        }

        signupButton.setOnClickListener {
            signUpConnection(username = usernameEditText.text.toString(),
                             password = passwordEditText.text.toString())
        }
    }

    private fun testConnection()
    {
        val url = URL(TEST_CONNECTION_URL)
        val (responseCode, responseText) = testRequest(url)
        Log.d("Response Code: ", "$responseCode")
        Log.d("Response Text: ", responseText)
        CSRF_TOKEN = responseText.substringAfter("name=\"csrfmiddlewaretoken\" value=\"").substringBefore("\">")
    }

    private fun testRequest(url: URL): Pair<Int, String>
    {
        val policy = StrictMode.ThreadPolicy.Builder().permitAll().build()
        StrictMode.setThreadPolicy(policy)

        return try
        {
            with(url.openConnection() as HttpURLConnection) {
                requestMethod = "GET"

                // Retrieve cookies from the response header
                val cookieHeader = getHeaderField("Set-Cookie") ?: throw Exception("Failed to retrieve cookies")

                SAVED_COOKIE = cookieHeader

                val responseText = buildString {
                    append("Successful Connection\n")
                    inputStream.bufferedReader().forEachLine { append(it).append("\n") }
                }

                Pair(responseCode, responseText)
            }
        }
        catch (e: Exception)
        {
            e.printStackTrace()
            Pair(405, "Connection Failed: ${e.message}")
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
        Log.d("Response Code: ", "$responseCode")
        Log.d("Response Text: ", responseText)
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
                    append("Response Message: $responseMessage\n")
                    inputStream.bufferedReader().forEachLine { append(it).append("\n") }
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

    private fun signUpConnection(username: String, password: String)
    {
        val url = URL(SIGN_UP_URL)
        val params = ArrayList<Pair<String, String>>()
        params.add(Pair("username", username))
        params.add(Pair("userage", "30"))
        params.add(Pair("password", password))
        params.add(Pair("csrfmiddlewaretoken", CSRF_TOKEN))
        val (responseCode, responseText) = signUpRequest(url, params)
        Log.d("Response Code: ", "$responseCode")
        Log.d("Response Text: ", responseText)
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
                    append("Response Message: $responseMessage\n")
                    inputStream.bufferedReader().forEachLine { append(it).append("\n") }
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
