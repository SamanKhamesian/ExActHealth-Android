package com.example.exacthealth.classes

import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.os.StrictMode
import android.util.Log
import java.net.HttpURLConnection
import java.net.URL

class ServerRequestHandler(context: Context)
{
    // Retrieve username and token from SharedPreferences
    private val sharedPreferences = context.getSharedPreferences("user_session", MODE_PRIVATE)
    private val savedUsername = sharedPreferences.getString("USERNAME", "") ?: ""
    private val csrfToken = sharedPreferences.getString("CSRF_TOKEN", "") ?: ""

    private companion object
    {
        const val UPDATE_FOOD_LIST_URL = "https://mayo.abdullah-mamun.com/t1d/update-food-list/"
    }

    fun sendUpdatedList(date: String, jsonFoodList: String, isInFavoriteList: Boolean)
    {
        // Prepare POST parameters
        val params = ArrayList<Pair<String, String>>()
        params.add(Pair("username", savedUsername))
        params.add(Pair("csrfmiddlewaretoken", csrfToken))
        params.add(Pair("food_date", date))
        params.add(Pair("food_list", jsonFoodList))

        // Make the HTTP POST request
        val (responseCode, responseText) = sendUpdatedListRequest(URL(UPDATE_FOOD_LIST_URL), params)

        // Handle the server's response
        if (responseCode == 200)
        {
            Log.d("ServerRequestHandler", "Food list updated successfully!")
        }
        else
        {
            Log.e("ServerRequestHandler", "Failed to update food list: $responseText")
        }
    }

    private fun sendUpdatedListRequest(url: URL, params: List<Pair<String, String>>): Pair<Int, String>
    {
        val policy = StrictMode.ThreadPolicy.Builder().permitAll().build()
        StrictMode.setThreadPolicy(policy)

        return try
        {
            with(url.openConnection() as HttpURLConnection) {
                requestMethod = "POST"
                doOutput = true

                // Add necessary headers (including any saved cookies if needed)
                val savedCookie = sharedPreferences.getString("SAVED_COOKIE", "") ?: ""
                addRequestProperty("Cookie", savedCookie)
                addRequestProperty("Content-Type", "application/x-www-form-urlencoded")

                // Prepare the POST parameters in the correct format
                val postData = params.joinToString("&") { "${it.first}=${it.second}" }

                // Send the POST request
                outputStream.bufferedWriter().use { it.write(postData) }

                // Read the server's response
                val responseText = buildString {
                    inputStream.bufferedReader().forEachLine { append(it) }
                }

                // Return the server's response code and the response text
                Pair(responseCode, responseText)
            }
        }
        catch (e: Exception)
        {
            e.printStackTrace()
            Pair(500, "Failed to send request: ${e.message}")
        }
    }
}