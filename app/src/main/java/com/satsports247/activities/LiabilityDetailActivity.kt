package com.satsports247.activities

import android.content.Intent
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.satsports247.constants.AppConstants
import com.satsports247.constants.IntentKeys
import com.satsports247.R
import com.satsports247.adapters.LiabilityListAdapter
import com.satsports247.constants.Config
import com.satsports247.constants.PreferenceKeys
import com.satsports247.dataModels.LiabilityDataModel
import com.satsports247.databinding.ActivityLiabilityDetailBinding

class LiabilityDetailActivity : AppCompatActivity(), LiabilityListAdapter.ItemClickListener {
    var TAG: String = "LiabilityDetailActivity"
    private lateinit var binding: ActivityLiabilityDetailBinding
    var data = ArrayList<LiabilityDataModel>()
    lateinit var adapter: LiabilityListAdapter
    var marketIDs = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLiabilityDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        init()
    }

    private fun init() {
        binding.refresh.setProgressBackgroundColorSchemeColor(
            ContextCompat.getColor(
                this,
                R.color.colorBlack
            )
        )
        binding.refresh.setColorSchemeColors(Color.YELLOW)
        binding.ivBack.setOnClickListener { finishAfterTransition() }
        if (intent.hasExtra(IntentKeys.data))
            marketIDs = intent.getStringExtra(IntentKeys.data)!!
        data =
            intent.getSerializableExtra(IntentKeys.liabilityData) as ArrayList<LiabilityDataModel>
        binding.recyclerLiabilityData.layoutManager = LinearLayoutManager(
            this, RecyclerView.VERTICAL, false
        )
        adapter = LiabilityListAdapter(data, this)
        binding.recyclerLiabilityData.adapter = adapter
        adapter.setClickListener(this)

        var liability = 0.0
        if (data.size > 0) {
            for (i in 0 until data.size) {
                liability += data[i].Liability
            }
        }
        binding.tvTotalLiability.text = DashboardActivity.decimalFormat0_00.format(
            liability.toBigDecimal()
        ).toString()

        binding.refresh.setOnRefreshListener {
            binding.refresh.isRefreshing = false
        }
    }

    override fun onItemClick(view: View?, position: Int) {
        val model = data[position]
        AppConstants.matchClickedMarket = false
        AppConstants.liabilityClickedMarket = true
        Config.saveSharedPreferences(this, PreferenceKeys.liabilityMarketList, model.MarketId)
        startActivity(Intent(this, SingleMarketActivity::class.java))
    }

    override fun onBackPressed() {
        AppConstants.matchClickedMarket = true
        AppConstants.liabilityClickedMarket = false
        super.onBackPressed()
        finishAfterTransition()
    }
}