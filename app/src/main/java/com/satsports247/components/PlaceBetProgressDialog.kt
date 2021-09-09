package com.satsports247.components

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.Gravity
import android.widget.ProgressBar
import com.satsports247.R


class PlaceBetProgressDialog constructor(context: Context) : Dialog(context) {
    init {
        setContentView(R.layout.place_bet_progress)
        window!!.setGravity(Gravity.CENTER)
        window!!.setDimAmount(0.0f)
        val progressBar = findViewById<ProgressBar>(R.id.progress_circular)
        window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        progressBar.setBackgroundColor(context.resources.getColor(android.R.color.transparent))
    }
}