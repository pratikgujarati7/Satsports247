package com.satsports247.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import android.widget.RelativeLayout
import androidx.databinding.ViewDataBinding
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.satsports247.R
import com.satsports247.activities.DashboardActivity
import com.satsports247.activities.LoginActivity
import com.satsports247.activities.SingleMarketActivity
import com.satsports247.constants.AppConstants
import com.satsports247.constants.Config
import com.satsports247.constants.JsonKeys
import com.satsports247.constants.PreferenceKeys
import com.satsports247.dataModels.ChipSettingsDataModel
import com.satsports247.dataModels.MarketModel
import com.satsports247.dataModels.RunnerModel
import com.satsports247.databinding.LayoutRunnerItemBinding
import com.satsports247.fragments.singleMarketFragments.SingleMarketFragment
import com.satsports247.responseModels.Common
import com.satsports247.retrofit.RetrofitApiClient
import io.realm.Realm
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class SingleRunnerViewHolder(val binding: ViewDataBinding) : RecyclerView.ViewHolder(binding.root) {
    val relKeyboard1: RelativeLayout = itemView.findViewById(R.id.rel_keyboard1)
    val llValue1: LinearLayout = itemView.findViewById(R.id.ll_value1)
    val llStakeValue1: LinearLayout = itemView.findViewById(R.id.ll_stake_value1)
    val edtRateValue1: EditText = itemView.findViewById(R.id.edt_rate_value_1)
    val edtStkValue1: EditText = itemView.findViewById(R.id.edt_stk_value_1)
    val tvBack1: TextView = itemView.findViewById(R.id.tv_back_1)
    val tvLay1: TextView = itemView.findViewById(R.id.tv_lay_1)
    val llBlue1: LinearLayout = itemView.findViewById(R.id.ll_blue1)
    val llPink1: LinearLayout = itemView.findViewById(R.id.ll_pink1)

    val btnCancel: Button = itemView.findViewById(R.id.btn_cancel)
    val btnPlaceBet: Button = itemView.findViewById(R.id.btn_place_bet)
    val ivRateMinus: ImageView = itemView.findViewById(R.id.iv_bl_minus)
    val ivRatePlus: ImageView = itemView.findViewById(R.id.iv_bl_plus)
    val ivStakeMinus: ImageView = itemView.findViewById(R.id.iv_stake_minus)
    val ivStakePlus: ImageView = itemView.findViewById(R.id.iv_stake_plus)
    val tvStakeLimit: TextView = itemView.findViewById(R.id.tv_stake_limit_value)
    val tvMaxProfit: TextView = itemView.findViewById(R.id.tv_max_profit_value)
    val tvProfit: TextView = itemView.findViewById(R.id.tv_team_profit)

    //chip textviews
    val tv1Chip1: TextView = itemView.findViewById(R.id.tv1_chip_1)
    val tv1Chip2: TextView = itemView.findViewById(R.id.tv1_chip_2)
    val tv1Chip3: TextView = itemView.findViewById(R.id.tv1_chip_3)
    val tv1Chip4: TextView = itemView.findViewById(R.id.tv1_chip_4)
    val tv1Chip5: TextView = itemView.findViewById(R.id.tv1_chip_5)
    val tv1Chip6: TextView = itemView.findViewById(R.id.tv1_chip_6)

    //Keyboard keys
    val key1T1: TextView = itemView.findViewById(R.id.key_1_t1)
    val key2T1: TextView = itemView.findViewById(R.id.key_2_t1)
    val key3T1: TextView = itemView.findViewById(R.id.key_3_t1)
    val key4T1: TextView = itemView.findViewById(R.id.key_4_t1)
    val key5T1: TextView = itemView.findViewById(R.id.key_5_t1)
    val key6T1: TextView = itemView.findViewById(R.id.key_6_t1)
    val key7T1: TextView = itemView.findViewById(R.id.key_7_t1)
    val key8T1: TextView = itemView.findViewById(R.id.key_8_t1)
    val key9T1: TextView = itemView.findViewById(R.id.key_9_t1)
    val key0T1: TextView = itemView.findViewById(R.id.key_0_t1)
    val key00T1: TextView = itemView.findViewById(R.id.key_00_t1)
    val key000T1: TextView = itemView.findViewById(R.id.key_000_t1)
    val keyDotT1: TextView = itemView.findViewById(R.id.key_dot_t1)
    val keyBackT1: TextView = itemView.findViewById(R.id.key_back_t1)
    val keyClearT1: TextView = itemView.findViewById(R.id.key_clear_t1)
    val ivGraph: ImageView = itemView.findViewById(R.id.iv_graph)
    val tvFancyRate: TextView = itemView.findViewById(R.id.tv_fancy_rate)
}

class SingleRunnerAdapter : ListAdapter<RunnerModel, SingleRunnerViewHolder>(Companion) {

    val TAG: String = "SingleRunnerAdapter"
    private var stakeValue: String = "0"
    private var rate: String = ""
    var finalStakeValue: Int = 0
    var selectedPosition: Int = -1
    var finalRateValue: Double = 0.0
    var rateFocus = false
    var stakeFocus = true
    var backLayout = false
    var layLayout = false
    lateinit var itemBinding: LayoutRunnerItemBinding
    lateinit var chipList: ArrayList<ChipSettingsDataModel>
    var totalProfit1 = 0.0
    var totalProfit2 = 0.0
    lateinit var context: Context
    lateinit var marketModel: MarketModel
    var matchedMarketModel = MarketModel()

    companion object : DiffUtil.ItemCallback<RunnerModel>() {
        override fun areItemsTheSame(oldItem: RunnerModel, newItem: RunnerModel): Boolean {
            return oldItem === newItem
        }

        override fun areContentsTheSame(oldItem: RunnerModel, newItem: RunnerModel): Boolean {
            return oldItem.RunnerId == newItem.RunnerId
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SingleRunnerViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = LayoutRunnerItemBinding.inflate(inflater, parent, false)

        return SingleRunnerViewHolder(binding)
    }

    override fun onBindViewHolder(holder: SingleRunnerViewHolder, position: Int) {
        val runnerData = getItem(position)
        itemBinding = holder.binding as LayoutRunnerItemBinding
        itemBinding.runner = runnerData
        itemBinding.executePendingBindings()
        context = itemBinding.relTop.context
        holder.edtRateValue1.showSoftInputOnFocus = false
        holder.edtStkValue1.showSoftInputOnFocus = false
        if (runnerData.backRate == runnerData.layRate)
            holder.tvBack1.text = "-"
        for (i in 0 until SingleMarketFragment.marketList.size) {
            for (j in 0 until SingleMarketFragment.marketList[i].Market.size) {
                if (SingleMarketFragment.marketList[i].Market[j].MarketId == runnerData.MarketId) {
                    marketModel = SingleMarketFragment.marketList[i].Market[j]
                    break
                }
            }
        }
        setProfitValues(position, runnerData, holder)
        holder.llBlue1.setOnClickListener {
            if (backLayout && runnerData.keyboardOpen.get()!!) {
                resetCalculatedValue(runnerData)
                holder.relKeyboard1.visibility = View.GONE
                backLayout = false
                layLayout = false
            } else {
                holder.relKeyboard1.visibility = View.VISIBLE
                stakeFocus = true
                setLayoutBackground(holder, holder.llBlue1, position, 1, runnerData)
            }
        }
        holder.llPink1.setOnClickListener {
            if (layLayout && runnerData.keyboardOpen.get()!!) {
                resetCalculatedValue(runnerData)
                holder.relKeyboard1.visibility = View.GONE
                backLayout = false
                layLayout = false
            } else {
                holder.relKeyboard1.visibility = View.VISIBLE
                stakeFocus = true
                setLayoutBackground(holder, holder.llPink1, position, 2, runnerData)
            }
        }
        val downlineData = SingleMarketFragment.DownLineData
        var minStake = downlineData.MinStake.toString()
        var maxStake = downlineData.MaxStake.toString()
        var maxProfit = downlineData.MaxProfit.toString()
        /*if (minStake.contains("-"))
            minStake = minStake.replace("-", "")
        if (maxStake.contains("-"))
            maxStake = maxStake.replace("-", "")
        if (maxProfit.contains("-"))
            maxProfit = maxProfit.replace("-", "")*/

        if (minStake == "-1")
            minStake = context.getString(R.string.unlimited)
        if (maxStake == "-1")
            maxStake = context.getString(R.string.unlimited)
        if (maxProfit == "-1")
            maxProfit = context.getString(R.string.unlimited)

        holder.tvMaxProfit.text = maxProfit
        holder.tvStakeLimit.text = "$minStake - $maxStake"
        if (minStake == context.getString(R.string.unlimited) &&
            maxStake == context.getString(R.string.unlimited)
        )
            holder.tvStakeLimit.text = context.getString(R.string.unlimited)


        chipList = Config.getChipSettings(holder.tv1Chip1.context)!!
        setChipNames(holder)

        holder.edtRateValue1.setOnFocusChangeListener { v, hasFocus ->
            rateFocus = hasFocus
            stakeFocus = !hasFocus
        }
        holder.edtStkValue1.setOnFocusChangeListener { v, hasFocus ->
            stakeFocus = hasFocus
            rateFocus = !hasFocus
        }

        holder.btnCancel.setOnClickListener {
            backLayout = false
            layLayout = false
            holder.relKeyboard1.visibility = View.GONE
            resetCalculatedValue(runnerData)
        }

        holder.btnPlaceBet.setOnClickListener {
            checkValidationsAndPlaceBet(
                holder,
                runnerData,
                position
            )
        }
        holder.ivRateMinus.setOnClickListener {
            if (finalRateValue > 0.01) {
                finalRateValue += -0.01
                rate = "" + DashboardActivity.decimalFormat0_00.format(finalRateValue)
                holder.edtRateValue1.setText(rate)
            }
            holder.edtRateValue1.setSelection(holder.edtRateValue1.text.toString().length)
            setCalculatedValue(position, runnerData, holder)
        }
        holder.ivRatePlus.setOnClickListener {
            if (finalRateValue >= 0) {
                finalRateValue += +0.01
                rate = "" + DashboardActivity.decimalFormat0_00.format(finalRateValue)
                holder.edtRateValue1.setText(rate)
            }
            holder.edtRateValue1.setSelection(holder.edtRateValue1.text.toString().length)
            setCalculatedValue(position, runnerData, holder)
        }
        holder.ivStakeMinus.setOnClickListener {
            if (finalStakeValue > 0) {
                finalStakeValue += -1
                stakeValue = finalStakeValue.toString()
                holder.edtStkValue1.setText(stakeValue)
            }
            holder.edtStkValue1.setSelection(holder.edtStkValue1.text.toString().length)
            setCalculatedValue(position, runnerData, holder)
        }
        holder.ivStakePlus.setOnClickListener {
            if (finalStakeValue >= 0) {
                finalStakeValue += 1
                stakeValue = finalStakeValue.toString()
                holder.edtStkValue1.setText(stakeValue)
            }
            holder.edtStkValue1.setSelection(holder.edtStkValue1.text.toString().length)
            setCalculatedValue(position, runnerData, holder)
        }

        setKeyboardOnClick(holder, position, runnerData)
        setChipOnClick(holder, position, runnerData)

        val leftGravityParam: RelativeLayout.LayoutParams = RelativeLayout.LayoutParams(
            context.resources.getDimensionPixelSize(R.dimen._60sdp),
            ViewGroup.LayoutParams.MATCH_PARENT
        )
        val rightGravityParam: RelativeLayout.LayoutParams = RelativeLayout.LayoutParams(
            context.resources.getDimensionPixelSize(R.dimen._62sdp),
            ViewGroup.LayoutParams.MATCH_PARENT
        )
        leftGravityParam.addRule(RelativeLayout.ALIGN_PARENT_LEFT)
        rightGravityParam.addRule(RelativeLayout.ALIGN_PARENT_RIGHT)

        //Graph icon -- only for Fancy
        if (!marketModel.IsFancy || marketModel.MarketType.equals(AppConstants.Bookmakers, true) ||
            marketModel.MarketType.equals(AppConstants.ManualOdds, true)
        ) {
            holder.llBlue1.layoutParams = leftGravityParam
            holder.llPink1.layoutParams = rightGravityParam
            holder.tvProfit.visibility = View.VISIBLE
            holder.ivGraph.visibility = View.GONE
        } else {
            holder.llBlue1.layoutParams = rightGravityParam
            holder.llPink1.layoutParams = leftGravityParam
            holder.tvProfit.visibility = View.GONE
            holder.ivGraph.visibility = View.VISIBLE
            holder.ivGraph.setOnClickListener {
                for (i in 0 until SingleMarketActivity.matchedList.size) {
                    for (j in 0 until SingleMarketActivity.matchedList[i].Market.size) {
                        if (SingleMarketActivity.matchedList[i].Market[j].MarketId == runnerData.MarketId) {
                            matchedMarketModel = SingleMarketActivity.matchedList[i].Market[j]
                            break
                        }
                    }
                }
                if (matchedMarketModel.Bet.size > 0)
                    Config.showRunPositionDialog(context, matchedMarketModel.Bet)
                else
                    Config.toast(context, context.getString(R.string.no_bet_placed_yet))
            }
        }
        if (marketModel.IsFancy && !marketModel.MarketType.equals(
                AppConstants.Bookmakers,
                true
            )
        ) {
            holder.tvFancyRate.visibility = View.VISIBLE
            holder.llValue1.visibility = View.GONE
        } else {
            holder.tvFancyRate.visibility = View.GONE
            holder.llValue1.visibility = View.VISIBLE
        }
    }

    private fun setNewProfitValues() {
        if (marketModel.RunnerPLList.size > 0) {
            for (k in 0 until marketModel.RunnerPLList.size) {
                marketModel.RunnerPLList[k].RunnerPL =
                    marketModel.Runner[k].ProfitValue.get().toString().toDouble()
            }
        }
    }

    private fun setProfitValues(
        position: Int,
        model: RunnerModel,
        holder: SingleRunnerViewHolder
    ) {
        if (marketModel.RunnerPLList.size > 0) {
            for (k in 0 until marketModel.RunnerPLList.size) {
                if (marketModel.RunnerPLList[k].RunnerId == model.RunnerId) {
                    var profitValue = marketModel.RunnerPLList[k].RunnerPL.toString()
                    if (profitValue.contains("-")) {
                        profitValue = profitValue.replace("-", "")
                        holder.tvProfit.setTextColor(context.resources.getColor(R.color.colorRed))
                    } else
                        holder.tvProfit.setTextColor(context.resources.getColor(R.color.colorGreen))
                    if (profitValue.contains(".0")) {
                        profitValue = profitValue.replace(".0", "")
                    }
                    holder.tvProfit.text = profitValue
                }
            }
        }
    }

    private fun setChipOnClick(
        holder: SingleRunnerViewHolder, position: Int, model: RunnerModel
    ) {
        holder.tv1Chip1.setOnClickListener { onChipClick(it, holder, 0, position, model) }
        holder.tv1Chip2.setOnClickListener { onChipClick(it, holder, 1, position, model) }
        holder.tv1Chip3.setOnClickListener { onChipClick(it, holder, 2, position, model) }
        holder.tv1Chip4.setOnClickListener { onChipClick(it, holder, 3, position, model) }
        holder.tv1Chip5.setOnClickListener { onChipClick(it, holder, 4, position, model) }
        holder.tv1Chip6.setOnClickListener { onChipClick(it, holder, 5, position, model) }
    }

    private fun onChipClick(
        view: View, holder: SingleRunnerViewHolder, chipPosition: Int, position: Int,
        model: RunnerModel
    ) {
        val tvChip = view as TextView
        if (stakeFocus) {
            if (stakeValue == "")
                stakeValue = "0"
            finalStakeValue = stakeValue.toInt()
            finalStakeValue += chipList[chipPosition].value
//            finalStakeValue += tvChip.text.toString().toInt()
            stakeValue = finalStakeValue.toString()
            holder.edtStkValue1.setText(stakeValue)
            holder.edtStkValue1.setSelection(holder.edtStkValue1.text.toString().length)
        } else if (rateFocus) {
            finalRateValue = rate.toDouble()
            finalRateValue += tvChip.text.toString().toDouble()
            rate = finalRateValue.toString()
            holder.edtRateValue1.setText(rate)
            holder.edtRateValue1.setSelection(holder.edtRateValue1.text.toString().length)
        }
        setCalculatedValue(position, model, holder)
    }

    var mClickListener: UpdateDataListener? = null

    // parent activity will implement this method to respond to click events
    interface UpdateDataListener {
        fun onUpdateData(common: Common)
    }

    private fun checkValidationsAndPlaceBet(
        holder: SingleRunnerViewHolder,
        model: RunnerModel,
        position: Int
    ) {
        Log.e(TAG, "$finalRateValue  &&  $finalStakeValue")
        Log.e(TAG, "$totalProfit1  &&  $totalProfit2")
        when {
            finalRateValue <= 0 -> {
                Config.toast(context, context.getString(R.string.please_enter_valid_rate))
            }
            finalStakeValue <= 0 -> {
                Config.toast(context, context.getString(R.string.please_enter_valid_stake))
            }
            SingleMarketFragment.DownLineData.MinStake != -1 && SingleMarketFragment.DownLineData.MinStake > finalStakeValue -> {
                Config.toast(context, context.getString(R.string.stake_must_greater))
            }
            SingleMarketFragment.DownLineData.MaxStake != -1 && SingleMarketFragment.DownLineData.MaxStake < finalStakeValue -> {
                Config.toast(context, context.getString(R.string.stake_must_be_less))
            }
            position == 0 && SingleMarketFragment.DownLineData.MaxProfit != -1 &&
                    SingleMarketFragment.DownLineData.MaxProfit < totalProfit1 -> {
                Config.toast(
                    context,
                    context.getString(R.string.you_have_reached_max_profit_limit)
                )
            }
            position == 1 && SingleMarketFragment.DownLineData.MaxProfit != -1 &&
                    SingleMarketFragment.DownLineData.MaxProfit < totalProfit2 -> {
                Config.toast(
                    context,
                    context.getString(R.string.you_have_reached_max_profit_limit)
                )
            }
            else -> {
                val jsonObject = JsonObject()
                jsonObject.addProperty(JsonKeys.MarketId, model.MarketId)
                jsonObject.addProperty(JsonKeys.MarketType, marketModel.MarketType)
                jsonObject.addProperty(
                    JsonKeys.UserId,
                    Config.getSharedPreferences(context, PreferenceKeys.UserId)
                )
                if (marketModel.IsFancy && !marketModel.MarketType.equals(
                        AppConstants.Bookmakers,
                        true
                    ) && !marketModel.MarketType.equals(
                        AppConstants.ManualOdds,
                        true
                    )
                ) {
                    jsonObject.addProperty(JsonKeys.Run, rate)
                    if (backLayout)
                        jsonObject.addProperty(JsonKeys.Rate, model.backBFVolume.get())
                    else
                        jsonObject.addProperty(JsonKeys.Rate, model.layBFVolume.get())
                } else
                    jsonObject.addProperty(JsonKeys.Rate, rate)
                jsonObject.addProperty(JsonKeys.Stake, finalStakeValue)
                jsonObject.addProperty(JsonKeys.IsBack, backLayout)
                jsonObject.addProperty(JsonKeys.IsFancy, marketModel.IsFancy)
                jsonObject.addProperty(JsonKeys.RunnerId, model.RunnerId)
                jsonObject.addProperty(JsonKeys.BfRunnerId, model.BfRunnerId)
                jsonObject.addProperty(JsonKeys.Runner, model.Name)
                jsonObject.addProperty(JsonKeys.CentralId, marketModel.CentralId)
                jsonObject.addProperty(JsonKeys.IsCashOutBet, marketModel.IsCashoutBet)
                jsonObject.addProperty(
                    JsonKeys.BetToken,
                    Config.getSharedPreferences(context, PreferenceKeys.BetToken)
                )
                Log.e(TAG, "Place bet: $jsonObject")
                try {
                    //api call
                    if (Config.isInternetAvailable(context)) {
                        Config.showPlaceBetProgress(context)
                        val call: Call<Common> = RetrofitApiClient.getMarketApiClient.placeBet(
                            Config.getSharedPreferences(context, PreferenceKeys.AuthToken),
                            jsonObject
                        )
                        call.enqueue(object : Callback<Common> {
                            override fun onResponse(
                                call: Call<Common>?,
                                response: Response<Common>?
                            ) {
                                val common: Common? = response?.body()
                                Log.e(TAG, "placeBet: " + Gson().toJson(common))
                                holder.btnPlaceBet.isClickable = true
                                if (response != null && response.isSuccessful) {
                                    Config.hidePlaceBetProgress()
                                    when (common?.status?.code) {
                                        0 -> {
                                            Config.saveSharedPreferences(
                                                context,
                                                PreferenceKeys.BetToken, common.BetToken
                                            )

                                            setMatchedData(common)
//                                            if (!common.UnMatchedBetData.isNullOrEmpty())
//                                                SingleMarketActivity.unMatchedList =
//                                                    common.UnMatchedBetData
//                                            SingleMarketActivity.viewPagerAdapter.notifyDataSetChanged()
                                            holder.relKeyboard1.visibility = View.GONE
                                            backLayout = false
                                            layLayout = false

                                            setNewProfitValues()
//                                            resetCalculatedValue(model)
//                                            Config.toast(
//                                                context,
//                                                context.getString(R.string.bet_placed_successfully)
//                                            )
                                            Config.betSuccessToast(
                                                context,
                                                context.getString(R.string.bet_placed_successfully)
                                            )
                                            mClickListener?.onUpdateData(common)
                                            Config.saveBalAndLiabilityInSingle(
                                                common.RunningBalance,
                                                common.Liability,
                                                context
                                            )
                                        }
                                        401 -> {
                                            val realm = Realm.getDefaultInstance()
                                            realm.executeTransaction { realm -> realm.deleteAll() }
                                            Config.clearAllPreferences(context)
                                            context.startActivity(
                                                Intent(
                                                    context,
                                                    LoginActivity::class.java
                                                )
                                            )
                                        }
                                        else -> {
                                            backLayout = false
                                            layLayout = false
                                            resetCalculatedValue(model)
                                            holder.relKeyboard1.visibility = View.GONE
//                                            Config.toast(context, common?.status?.returnMessage)
                                            Config.betErrorToast(
                                                context,
                                                common?.status?.returnMessage!!
                                            )
                                        }
                                    }
                                } else {
                                    holder.btnPlaceBet.isClickable = true
                                    Config.hidePlaceBetProgress()
                                    backLayout = false
                                    layLayout = false
                                    Config.toast(
                                        context,
                                        context.resources.getString(R.string.something_went_wrong)
                                    )
                                    /*val realm = Realm.getDefaultInstance()
                                    realm.executeTransaction { realm -> realm.deleteAll() }
                                    Config.clearAllPreferences(context)
                                    context.startActivity(
                                        Intent(
                                            context,
                                            LoginActivity::class.java
                                        )
                                    )*/
                                }
                            }

                            override fun onFailure(call: Call<Common>?, t: Throwable?) {
                                holder.btnPlaceBet.isClickable = true
                                Config.hidePlaceBetProgress()
                                backLayout = false
                                layLayout = false
                                resetCalculatedValue(model)
                                Log.e(TAG, "placeBet: " + t.toString())
                            }
                        })
                    } else {
                        holder.btnPlaceBet.isClickable = true
                        Config.hidePlaceBetProgress()
                        Config.toast(
                            context,
                            context.resources.getString(R.string.please_check_internet_connection)
                        )
                    }
                } catch (e: Exception) {
                    holder.btnPlaceBet.isClickable = true
                    Config.toast(context, "" + e)
                    Log.e(TAG, "" + e)
                }
                holder.btnPlaceBet.isClickable = true
            }
        }
    }

    private fun setMatchedData(common: Common) {
        if (SingleMarketActivity.matchedList.size > 0) {
            for (i in 0 until SingleMarketActivity.matchedList.size) {
                for (k in 0 until SingleMarketActivity.matchedList[i].Market.size) {
                    if (SingleMarketActivity.matchedList[i].Market[k].MarketId ==
                        common.placeBetList.MatchBetData[0].Market[0].MarketId
                    ) {
                        SingleMarketActivity.matchedList[i].Market[k] =
                            common.placeBetList.MatchBetData[0].Market[0]
                        break
                    } else if (k == SingleMarketActivity.matchedList[i].Market.size - 1) {
                        SingleMarketActivity.matchedList[i].Market.addAll(
                            common.placeBetList.MatchBetData[0].Market
                        )
                        break
                    }
                }
            }
        } else {
            SingleMarketActivity.matchedList =
                common.placeBetList.MatchBetData
        }
    }

    private fun setCalculatedValue(
        position: Int,
        model: RunnerModel,
        holder: SingleRunnerViewHolder
    ) {
//        if back -- then add profit,,if lay --  then minus profit
        for (k in 0 until marketModel.Runner.size) {
//            val decimalFormat = DecimalFormat("0.0")
            var profitValue = "0"
            var profitInt = 0.0
            if (marketModel.RunnerPLList.size > 0) {
                profitValue = marketModel.RunnerPLList[k].RunnerPL.toString()
                profitInt = marketModel.RunnerPLList[k].RunnerPL
                if (profitValue.contains("-")) {
                    profitValue = profitValue.replace("-", "")
                }
            }
            if (marketModel.Runner[k].RunnerId == model.RunnerId && backLayout) {
                if (!marketModel.MarketType.equals(AppConstants.Bookmakers, true) &&
                    !marketModel.MarketType.equals(AppConstants.AdvanceSession, true)
//                    && !marketModel.MarketType.equals(AppConstants.ManualOdds, true)
                ) {
                    var value =
                        (profitInt + ((rate.toDouble() * finalStakeValue) - finalStakeValue)).toString()
                    if (value.contains("-")) {
                        value = value.replace("-", "")
                        marketModel.Runner[k].profitTextColor.set(R.color.colorRed)
                    } else
                        marketModel.Runner[k].profitTextColor.set(R.color.colorGreen)
                    marketModel.Runner[k].ProfitValue.set(
                        DashboardActivity.decimalFormat0_0.format(
                            value.toDouble().toBigDecimal()
                        )
                    )
                } else {
                    var value = (profitInt +
                            ((rate.toDouble() * finalStakeValue) / 100)).toString()
                    if (value.contains("-")) {
                        value = value.replace("-", "")
                        marketModel.Runner[k].profitTextColor.set(R.color.colorRed)
                    } else
                        marketModel.Runner[k].profitTextColor.set(R.color.colorGreen)
                    marketModel.Runner[k].ProfitValue.set(
                        DashboardActivity.decimalFormat0_0.format(
                            value.toDouble().toBigDecimal()
                        )
                    )
                }
                totalProfit1 += holder.tvProfit.text.toString().toDouble()
            } else if (backLayout && marketModel.Runner[k].RunnerId != model.RunnerId) {
                var value = (profitInt - stakeValue.toDouble()).toString()
                if (value.contains("-")) {
                    value = value.replace("-", "")
                    marketModel.Runner[k].profitTextColor.set(R.color.colorRed)
                } else
                    marketModel.Runner[k].profitTextColor.set(R.color.colorGreen)
                marketModel.Runner[k].ProfitValue.set(
                    DashboardActivity.decimalFormat0_0.format(
                        value.toDouble().toBigDecimal()
                    )
                )
            } else if (layLayout && marketModel.Runner[k].RunnerId == model.RunnerId) {
                if (!marketModel.MarketType.equals("bookmakers", true) &&
                    !marketModel.MarketType.equals("advancesession", true)
//                    && !marketModel.MarketType.equals("manualodds", true)
                ) {
                    var value =
                        (profitInt - ((rate.toDouble() * finalStakeValue) - finalStakeValue)).toString()
                    if (value.contains("-")) {
                        value = value.replace("-", "")
                        marketModel.Runner[k].profitTextColor.set(R.color.colorRed)
                    } else
                        marketModel.Runner[k].profitTextColor.set(R.color.colorGreen)
                    marketModel.Runner[k].ProfitValue.set(
                        DashboardActivity.decimalFormat0_0.format(
                            value.toDouble().toBigDecimal()
                        )
                    )
                } else {
                    var value = (profitInt - ((rate.toDouble() * finalStakeValue) / 100)).toString()
                    if (value.contains("-")) {
                        value = value.replace("-", "")
                        marketModel.Runner[k].profitTextColor.set(R.color.colorRed)
                    } else
                        marketModel.Runner[k].profitTextColor.set(R.color.colorGreen)
                    marketModel.Runner[k].ProfitValue.set(
                        DashboardActivity.decimalFormat0_0.format(
                            value.toDouble().toBigDecimal()
                        )
                    )
                }
            } else if (layLayout && marketModel.Runner[k].RunnerId != model.RunnerId) {
                var value = (profitInt + stakeValue.toDouble()).toString()
                if (value.contains("-")) {
                    value = value.replace("-", "")
                    marketModel.Runner[k].profitTextColor.set(R.color.colorRed)
                } else
                    marketModel.Runner[k].profitTextColor.set(R.color.colorGreen)
                marketModel.Runner[k].ProfitValue.set(
                    DashboardActivity.decimalFormat0_0.format(
                        value.toDouble().toBigDecimal()
                    )
                )
            }
        }
        for (i in 0 until SingleMarketFragment.marketList.size) {
            for (j in 0 until SingleMarketFragment.marketList[i].Market.size) {
                if (SingleMarketFragment.marketList[i].Market[j].MarketId == model.MarketId) {
                    SingleMarketFragment.marketList[i].Market[j] = marketModel
                    break
                }
            }
        }
    }

    private fun resetCalculatedValue(
        model: RunnerModel
    ) {
        for (k in 0 until marketModel.Runner.size) {
            var profitValue = "0"
            marketModel.Runner[k].profitTextColor.set(R.color.colorGreen)
            if (marketModel.RunnerPLList.size > 0) {
                profitValue = marketModel.RunnerPLList[k].RunnerPL.toString()
                if (profitValue.contains("-")) {
                    profitValue = profitValue.replace("-", "")
                    marketModel.Runner[k].profitTextColor.set(R.color.colorRed)
                }
                if (profitValue.contains(".0")) {
                    profitValue = profitValue.replace(".0", "")
                }
            }
            marketModel.Runner[k].ProfitValue.set(profitValue)
        }
        for (i in 0 until SingleMarketFragment.marketList.size) {
            for (j in 0 until SingleMarketFragment.marketList[i].Market.size) {
                if (SingleMarketFragment.marketList[i].Market[j].MarketId == model.MarketId) {
                    SingleMarketFragment.marketList[i].Market[j] = marketModel
                    break
                }
            }
        }
    }

    private fun setKeyboardStatus(
        model: RunnerModel
    ) {
        for (k in 0 until marketModel.Runner.size) {
            if (k == selectedPosition)
                marketModel.Runner[k].keyboardOpen.set(true)
            else
                marketModel.Runner[k].keyboardOpen.set(false)
        }
        for (i in 0 until SingleMarketFragment.marketList.size) {
            for (j in 0 until SingleMarketFragment.marketList[i].Market.size) {
                if (SingleMarketFragment.marketList[i].Market[j].MarketId == model.MarketId) {
                    SingleMarketFragment.marketList[i].Market[j] = marketModel
                    break
                }
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun setKeyboardOnClick(
        holder: SingleRunnerViewHolder,
        position: Int,
        model: RunnerModel
    ) {
        holder.key1T1.setOnClickListener {
            onKeyClick(it, holder, position, model)
        }
        holder.key2T1.setOnClickListener {
            onKeyClick(it, holder, position, model)
        }
        holder.key3T1.setOnClickListener {
            onKeyClick(it, holder, position, model)
        }
        holder.key4T1.setOnClickListener {
            onKeyClick(it, holder, position, model)
        }
        holder.key5T1.setOnClickListener {
            onKeyClick(it, holder, position, model)
        }
        holder.key6T1.setOnClickListener {
            onKeyClick(it, holder, position, model)
        }
        holder.key7T1.setOnClickListener {
            onKeyClick(it, holder, position, model)
        }
        holder.key8T1.setOnClickListener {
            onKeyClick(it, holder, position, model)
        }
        holder.key9T1.setOnClickListener {
            onKeyClick(it, holder, position, model)
        }
        holder.key0T1.setOnClickListener {
            onKeyClick(it, holder, position, model)
        }
        holder.key00T1.setOnClickListener {
            onKeyClick(it, holder, position, model)
        }
        holder.key000T1.setOnClickListener {
            onKeyClick(it, holder, position, model)
        }
        holder.keyDotT1.setOnClickListener {
            onDotKeyClick(it, holder, position, model)
        }
        holder.keyBackT1.setOnClickListener {
            if (rateFocus) {
                if (holder.edtRateValue1.text.length > 1) {
                    holder.edtRateValue1.text.delete(
                        holder.edtRateValue1.text.length - 1,
                        holder.edtRateValue1.text.length
                    )
                    rate = holder.edtRateValue1.text.toString()
                    finalRateValue = rate.toDouble()
                } else {
                    rate = "0.0"
                    finalRateValue = rate.toDouble()
                    holder.edtRateValue1.setText(rate)
                }
                holder.edtRateValue1.setSelection(holder.edtRateValue1.text.toString().length)
            } else if (stakeFocus) {
                if (holder.edtStkValue1.text.length > 1) {
                    holder.edtStkValue1.text.delete(
                        holder.edtStkValue1.text.length - 1,
                        holder.edtStkValue1.text.length
                    )
                    stakeValue = holder.edtStkValue1.text.toString()
                    finalStakeValue = stakeValue.toInt()
                } else {
                    stakeValue = "0"
                    finalStakeValue = stakeValue.toInt()
                    holder.edtStkValue1.setText(stakeValue)
                }
                holder.edtStkValue1.setSelection(holder.edtStkValue1.text.toString().length)
            }
            setCalculatedValue(position, model, holder)
        }

        holder.keyClearT1.setOnClickListener {
            clearStackValue(holder)
            resetCalculatedValue(model)
        }
    }

    private fun clearStackValue(holder: SingleRunnerViewHolder) {
        if (rateFocus) {
            finalRateValue = 0.0
            rate = finalRateValue.toString()
            holder.edtRateValue1.setText(rate)
            holder.edtRateValue1.setSelection(holder.edtRateValue1.text.toString().length)
        } else if (stakeFocus) {
            finalStakeValue = 0
            stakeValue = "0"
            holder.edtStkValue1.setText(stakeValue)
            holder.edtStkValue1.setSelection(holder.edtStkValue1.text.toString().length)
        }
    }

    private fun onKeyClick(
        view: View,
        holder: SingleRunnerViewHolder,
        position: Int,
        model: RunnerModel
    ) {
        val tvKey = view as TextView
        if (stakeFocus && holder.edtStkValue1.text.toString().length < 6) {
            stakeValue += tvKey.text
            holder.edtStkValue1.setText(stakeValue)
            finalStakeValue = stakeValue.toInt()
            holder.edtStkValue1.setSelection(holder.edtStkValue1.text.toString().length)
        } else if (rateFocus && holder.edtRateValue1.text.toString().length < 6
        ) {
            rate += tvKey.text
            holder.edtRateValue1.setText(rate)
            finalRateValue = rate.toDouble()
            holder.edtRateValue1.setSelection(holder.edtRateValue1.text.toString().length)
        }
        setCalculatedValue(position, model, holder)
    }

    private fun onDotKeyClick(
        view: View,
        holder: SingleRunnerViewHolder,
        position: Int,
        model: RunnerModel
    ) {
        val tvKey = view as TextView
        if (stakeFocus && holder.edtStkValue1.text.toString().length < 6 &&
            !holder.edtStkValue1.text.contains(".")
        ) {
            stakeValue += tvKey.text
            holder.edtStkValue1.setText(stakeValue)
            finalStakeValue = stakeValue.toInt()
            holder.edtStkValue1.setSelection(holder.edtStkValue1.text.toString().length)
        } else if (rateFocus && holder.edtRateValue1.text.toString().length < 6 &&
            !holder.edtRateValue1.text.contains(".")
        ) {
            rate += tvKey.text
            holder.edtRateValue1.setText(rate)
            finalRateValue = rate.toDouble()
            holder.edtRateValue1.setSelection(holder.edtRateValue1.text.toString().length)
        }
        setCalculatedValue(position, model, holder)
    }


    private fun setChipNames(holder: SingleRunnerViewHolder) {
        holder.tv1Chip1.text = chipList[0].name
        holder.tv1Chip2.text = chipList[1].name
        holder.tv1Chip3.text = chipList[2].name
        holder.tv1Chip4.text = chipList[3].name
        holder.tv1Chip5.text = chipList[4].name
        holder.tv1Chip6.text = chipList[5].name
    }

    private fun setLayoutBackground(
        holder: SingleRunnerViewHolder, view: View, position: Int, type: Int,
        teamModel: RunnerModel
    ) {
        for (i in 0 until SingleMarketFragment.marketList.size) {
            for (j in 0 until SingleMarketFragment.marketList[i].Market.size) {
                if (SingleMarketFragment.marketList[i].Market[j].MarketId == teamModel.MarketId) {
                    marketModel = SingleMarketFragment.marketList[i].Market[j]
                    break
                }
            }
        }
        selectedPosition = position
        setKeyboardStatus(teamModel)
        holder.btnPlaceBet.isClickable = true
        resetCalculatedValue(teamModel)
        if (type == 1) {
            backLayout = true
            layLayout = false
            holder.relKeyboard1.setBackgroundColor(view.context.resources.getColor(R.color.colorLightBlue))
            holder.llValue1.background =
                view.context.resources.getDrawable(R.drawable.bg_plus_minus_blue)
            holder.llStakeValue1.background =
                view.context.resources.getDrawable(R.drawable.bg_plus_minus_blue)
            rate = if (!holder.tvBack1.text.contains("-")) {
                holder.edtRateValue1.setText(holder.tvBack1.text)
                holder.tvFancyRate.text = holder.tvBack1.text
                holder.tvBack1.text.toString()
            } else {
                holder.edtRateValue1.setText("0")
                holder.tvFancyRate.text = "0"
                "0"
            }
            holder.edtStkValue1.setText("0")
            finalRateValue = rate.toDouble()
            stakeValue = "0"
            finalStakeValue = stakeValue.toInt()
        } else {
            backLayout = false
            layLayout = true
            holder.relKeyboard1.setBackgroundColor(view.context.resources.getColor(R.color.colorLightPink))
            holder.llValue1.background =
                view.context.resources.getDrawable(R.drawable.bg_plus_minus_pink)
            holder.llStakeValue1.background =
                view.context.resources.getDrawable(R.drawable.bg_plus_minus_pink)
            rate = if (!holder.tvLay1.text.contains("-")) {
                holder.edtRateValue1.setText(holder.tvLay1.text)
                holder.tvFancyRate.text = holder.tvLay1.text
                holder.tvLay1.text.toString()
            } else {
                holder.edtRateValue1.setText("0")
                holder.tvFancyRate.text = "0"
                "0"
            }
            holder.edtStkValue1.setText("0")
            finalRateValue = rate.toDouble()
            stakeValue = "0"
            finalStakeValue = stakeValue.toInt()
        }
        holder.edtRateValue1.setSelection(holder.edtRateValue1.text.toString().length)
        holder.edtStkValue1.setSelection(holder.edtStkValue1.text.toString().length)
    }
}