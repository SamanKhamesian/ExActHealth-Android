package com.example.exacthealth.activities

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import com.example.exacthealth.R

class FavoriteFoodActivity : AppCompatActivity()
{
    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_favorite_food)

        val addNewFavoriteFoodButton = findViewById<ImageView>(R.id.add_new_favorite_icon)

        addNewFavoriteFoodButton.setOnClickListener {
            val intent = Intent(this, AddFavoriteFoodActivity::class.java)
            startActivity(intent)
        }
    }
}