package com.satsports247.activities

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityOptionsCompat
import com.satsports247.constants.Config
import com.satsports247.constants.PreferenceKeys
import com.satsports247.constants.UrlConstants
import com.satsports247.databinding.ActivitySplashBinding

class SplashActivity : AppCompatActivity() {

    var TAG: String = "SplashActivity"
    private lateinit var binding: ActivitySplashBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val handler = Handler()
        val runnable = Runnable { init() }
        handler.postDelayed(runnable, 3000)
    }

    private fun init() {
        Log.e(TAG, "Api EndPoint: " + UrlConstants.webUrl)
        val options = ActivityOptionsCompat.makeSceneTransitionAnimation(
            this,
            binding.ivAppLogo, "splash_transition"
        )
        if (Config.getUserLoggedIn(this, PreferenceKeys.userLoggedIn)) {
            startActivity(
                Intent(this@SplashActivity, DashboardActivity::class.java),
                options.toBundle()
            )
        } else {
            startActivity(Intent(this@SplashActivity, ConfirmationActivity::class.java))
        }
        finishAffinity()
    }
}