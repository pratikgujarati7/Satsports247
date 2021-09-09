package com.satsports247.activities

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityOptionsCompat
import androidx.core.util.Pair
import com.satsports247.databinding.ActivityConfirmationBinding

class ConfirmationActivity : AppCompatActivity() {

    val TAG: String = "TermsnConditionActivity"
    private lateinit var binding: ActivityConfirmationBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityConfirmationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        init()
    }

    private fun init() {
        binding.btnConfirm.setOnClickListener {
            val btnConfirm = Pair.create<View, String>(binding.btnConfirm, "login_transition")
            val options = ActivityOptionsCompat.makeSceneTransitionAnimation(this, btnConfirm)
            startActivity(Intent(this, LoginActivity::class.java), options.toBundle())
            finish()
//            throw RuntimeException("Test Crash")
        }
        binding.btnExit.setOnClickListener { finish() }
    }
}