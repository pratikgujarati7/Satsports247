package com.satsports247.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.core.app.ActivityOptionsCompat
import androidx.core.util.Pair
import com.satsports247.R
import com.satsports247.databinding.ActivityTermsAndConditionsBinding

class TermsAndConditionsActivity : AppCompatActivity() {

    val TAG: String = "TermsnConditionsActivity"
    lateinit var binding: ActivityTermsAndConditionsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTermsAndConditionsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        init()
    }

    private fun init() {
        binding.btnAccept.setOnClickListener {
            val btnAccept = Pair.create<View, String>(binding.btnAccept, "splash_transition")
            val options = ActivityOptionsCompat.makeSceneTransitionAnimation(this, btnAccept)
//            startActivity(Intent(this, LoginActivity::class.java), options.toBundle())
            val intent = Intent(this, DashboardActivity::class.java)
            startActivity(intent, options.toBundle())
            finish()
        }
    }
}