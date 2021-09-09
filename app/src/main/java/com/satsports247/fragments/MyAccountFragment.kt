package com.satsports247.fragments

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.ActivityOptionsCompat
import androidx.fragment.app.Fragment
import com.google.gson.Gson
import com.satsports247.BuildConfig
import com.satsports247.R
import com.satsports247.activities.*
import com.satsports247.constants.Config
import com.satsports247.constants.PreferenceKeys
import com.satsports247.databinding.ActivityMyAccountBinding
import com.satsports247.responseModels.Common
import com.satsports247.retrofit.RetrofitApiClient
import io.realm.Realm
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*

class MyAccountFragment : Fragment(), Config.OkButtonClicklistner {

    val TAG: String = "MyAccountFragment"
    lateinit var binding: ActivityMyAccountBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = ActivityMyAccountBinding.inflate(inflater, container, false)
        init()
        return binding.root
    }

    private fun init() {
        setOnClickListeners()
    }

    @SuppressLint("SetTextI18n")
    private fun setOnClickListeners() {
        binding.llProfile.setOnClickListener {
            val options = ActivityOptionsCompat.makeSceneTransitionAnimation(
                requireActivity(), binding.llProfile, "profile_transition"
            )
            startActivity(
                Intent(requireContext(), ProfileActivity::class.java), options.toBundle()
            )
        }
        binding.llChip.setOnClickListener {
            val options = ActivityOptionsCompat.makeSceneTransitionAnimation(
                requireActivity(), binding.llChip, "chip_transition"
            )
            startActivity(
                Intent(requireContext(), ChipSettingActivity::class.java), options.toBundle()
            )
        }
        binding.llStatement.setOnClickListener {
            val options = ActivityOptionsCompat.makeSceneTransitionAnimation(
                requireActivity(), binding.llStatement, "account_transition"
            )
            startActivity(
                Intent(requireContext(), StatementActivity::class.java), options.toBundle()
            )
        }
        binding.llProfitLoss.setOnClickListener {
            val options = ActivityOptionsCompat.makeSceneTransitionAnimation(
                requireActivity(), binding.llProfitLoss, "pl_transition"
            )
            startActivity(
                Intent(requireContext(), PLActivity::class.java), options.toBundle()
            )
        }
        binding.llResults.setOnClickListener {
            val options = ActivityOptionsCompat.makeSceneTransitionAnimation(
                requireActivity(), binding.llResults, "result_transition"
            )
            startActivity(
                Intent(requireContext(), ResultsActivity::class.java), options.toBundle()
            )
        }
        binding.llPassword.setOnClickListener {
            val options = ActivityOptionsCompat.makeSceneTransitionAnimation(
                requireActivity(), binding.llPassword, "password_transition"
            )
            startActivity(
                Intent(requireContext(), ChangePasswordActivity::class.java), options.toBundle()
            )
        }
        binding.llFaq.setOnClickListener {
            /*val options = ActivityOptionsCompat.makeSceneTransitionAnimation(
                requireActivity(), binding.llFaq, "faq_transition"
            )
            startActivity(
                Intent(requireContext(), FAQActivity::class.java), options.toBundle()
            )*/
        }
        binding.llDeposit.setOnClickListener {
            val options = ActivityOptionsCompat.makeSceneTransitionAnimation(
                requireActivity(), binding.llDeposit, "deposit_transition"
            )
            startActivity(
                Intent(requireContext(), DepositActivity::class.java), options.toBundle()
            )
        }
        binding.llWithdraw.setOnClickListener {
            val options = ActivityOptionsCompat.makeSceneTransitionAnimation(
                requireActivity(), binding.llWithdraw, "withdraw_transition"
            )
            startActivity(
                Intent(requireContext(), WithdrawActivity::class.java), options.toBundle()
            )
        }
        binding.llLogout.setOnClickListener {
            Config.showLogoutConfirmationDialog(
                requireContext(), this
            )
        }

        binding.tvUserName.text =
            Config.getSharedPreferences(requireContext(), PreferenceKeys.Username)
        val timeZone = TimeZone.getDefault()
        binding.tvVersion.text = getString(R.string.v) + " " + BuildConfig.VERSION_NAME
        binding.tvTimezone.text = getString(R.string.time_zone) + " " +
                timeZone.getDisplayName(false, TimeZone.SHORT)
    }

    companion object {
        @JvmStatic
        fun newInstance(): MyAccountFragment {
            return MyAccountFragment()
        }
    }

    override fun OkButtonClick() {
        callLogoutApi()
    }

    private fun callLogoutApi() {
        if (Config.isInternetAvailable(requireContext())) {
            val call: Call<Common> = RetrofitApiClient.getClient.logout(
                Config.getSharedPreferences(requireContext(), PreferenceKeys.AuthToken)!!
            )
            call.enqueue(object : Callback<Common> {
                override fun onResponse(call: Call<Common>?, response: Response<Common>?) {
                    val common: Common? = response?.body()
                    val message: String? = common?.status?.returnMessage
                    Log.e(TAG, "logout response: " + Gson().toJson(common))
                    if (response != null && response.isSuccessful) {
                        Config.toast(requireContext(), message)
                    }
                    Config.clearAllPreferences(requireContext())
                    val realm = Realm.getDefaultInstance()
                    realm.executeTransaction { realm.deleteAll() }
                    startActivity(Intent(requireContext(), LoginActivity::class.java))
                    activity!!.finish()
                }

                override fun onFailure(call: Call<Common>?, t: Throwable?) {
                    Config.hideSmallProgressDialog()
                    Log.e(TAG, "logout failed: " + t.toString())
                }
            })
        }
    }
}