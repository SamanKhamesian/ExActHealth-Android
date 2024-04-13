package com.example.exacthealth

import android.net.Uri
import java.text.SimpleDateFormat
import java.util.Locale

class Food {
    var name: String = ""
    var date: String = ""
    var time: String = ""
    var images: ArrayList<Uri>? = null
    var protein: Double? = null
    var carb: Double? = null
    var fat: Double? = null

    // Constructor with required attributes
    constructor(name: String, date: String, time: String) {
        this.name = name
        this.date = convertDateFormat(date)
        this.time = time
    }

    // Constructor with optional images attribute
    constructor(name: String, date: String, time: String, images: ArrayList<Uri>) {
        this.name = name
        this.date = convertDateFormat(date)
        this.time = time
        this.images = images
    }

    // Constructor with optional nutrition attributes
    constructor(name: String, date: String, time: String, protein: Double?, carb: Double?, fat: Double?) {
        this.name = name
        this.date = convertDateFormat(date)
        this.time = time
        this.protein = protein
        this.carb = carb
        this.fat = fat
    }

    // Constructor with optional nutrition attributes
    constructor(name: String, date: String, time: String, images: ArrayList<Uri>, protein: Double?, carb: Double?, fat: Double?) {
        this.name = name
        this.date = convertDateFormat(date)
        this.time = time
        this.images = images
        this.protein = protein
        this.carb = carb
        this.fat = fat
    }

    private fun convertDateFormat(inputDate: String): String
    {
        val inputFormat = SimpleDateFormat("MMM dd, yyyy", Locale.US)
        val outputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)

        val newDate = inputFormat.parse(inputDate)
        return outputFormat.format(newDate!!)
    }
}