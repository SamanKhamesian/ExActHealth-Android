package com.example.exacthealth.activities

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.ListView
import android.widget.RelativeLayout
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.example.exacthealth.R
import com.example.exacthealth.classes.FavoriteFoodListAdapter
import com.example.exacthealth.classes.FoodDetails
import com.example.exacthealth.classes.FoodSharedPreferencesManager

class FavoriteFoodActivity : AppCompatActivity()
{
    private lateinit var foodSharedPreferencesManager: FoodSharedPreferencesManager
    private lateinit var foodListView: ListView
    lateinit var startAddFavoriteFoodActivityLauncher: ActivityResultLauncher<Intent>
    private lateinit var foodList: MutableList<FoodDetails>

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_favorite_food)

        val addNewFavoriteFoodButton = findViewById<ImageView>(R.id.add_new_favorite_icon)
        val backToAddFoodActivityButton = findViewById<RelativeLayout>(R.id.back_to_food_details_layout_2)

        foodSharedPreferencesManager = FoodSharedPreferencesManager(this)
        foodListView = findViewById(R.id.favorite_foods_list_view)

        updateListView()

        startAddFavoriteFoodActivityLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult())
        { result ->
            if (result.resultCode == RESULT_OK)
            {
                updateListView()
            }
        }

        addNewFavoriteFoodButton.setOnClickListener {
            val intent = Intent(this, AddFavoriteFoodActivity::class.java)
            startAddFavoriteFoodActivityLauncher.launch(intent)
        }

        backToAddFoodActivityButton.setOnClickListener {
            finish()
        }
    }

    private fun updateListView()
    {
        foodList = foodSharedPreferencesManager.loadFoodList("None")

        if (foodList.isNotEmpty())
        {
            foodListView.visibility = View.VISIBLE

            val adapter = FavoriteFoodListAdapter(this, foodList) { item ->
                val intent = Intent(this, AddFoodActivity::class.java)
                intent.putExtra("from", "favorite_food")
                intent.putExtra("foodName", item.name)
                intent.putExtra("foodImagesPathList", item.paths)
                intent.putExtra("foodProtein", item.protein)
                intent.putExtra("foodCarbs", item.carbs)
                intent.putExtra("foodFats", item.fats)
                startActivity(intent)
            }

            foodListView.adapter = adapter
        }
        else
        {
            foodListView.visibility = View.INVISIBLE
        }
    }
}