package com.satsports247.fragments

import android.app.Activity
import android.app.AlertDialog
import android.app.Dialog
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.satsports247.R

abstract class BaseFragment : Fragment() {

    lateinit var alertDialog: AlertDialog
    var activity: Activity? = null

    protected abstract fun frgInternetAvailable()

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is Activity) {
            activity = context
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //        instance = this;
        alertDialog = AlertDialog.Builder(activity).create()
        alertDialog.setTitle(getString(R.string.no_internet_connection))
        alertDialog.setMessage(getString(R.string.please_check_internet_connection))
        alertDialog.setCancelable(false)
        alertDialog.setIcon(R.drawable.ic_no_internet_connection)
    }

    private val mNetworkReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            Log.e("BaseFragment", "mNetworkReceiver")
            val result: Int = getConnectionType(context)
            if (result > 0) {
                Log.e("network", " Available1")
                if (alertDialog.isShowing) {
                    alertDialog.dismiss()
                    frgInternetAvailable()
                }
            } else {
                showAlertForInternet()
            }
        }
    }

    override fun onStart() {
        super.onStart()
        registerNetworkBroadcast()
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterNetworkChanges()
    }

    private fun registerNetworkBroadcast() {
        when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.N -> {
                activity?.registerReceiver(
                    mNetworkReceiver,
                    IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION)
                )
            }
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.M -> {
                activity?.registerReceiver(
                    mNetworkReceiver,
                    IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION)
                )
            }
            else -> {
                val intentFilter = IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION)
                activity?.registerReceiver(mNetworkReceiver, intentFilter)
            }
        }
    }

    private fun unregisterNetworkChanges() {
        try {
            mNetworkReceiver.let { activity?.unregisterReceiver(it) }
        } catch (e: IllegalArgumentException) {
            e.printStackTrace()
        }
    }

    fun showAlertForInternet() {
        Log.e("BaseFragment", "showAlertForInternet")
        alertDialog.setButton(
            Dialog.BUTTON_POSITIVE, "Go to Settings"
        ) { _, _ ->
            alertDialog.dismiss()
            val intent = Intent(Settings.ACTION_SETTINGS)
            startActivityForResult(intent, 2)
        }
        if (!activity?.isFinishing!!) {
            alertDialog.show()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        Log.e("activity result ", "" + resultCode)
        if (requestCode == 2 && resultCode == 0) {
            Log.e("network", " Available2")
            if (alertDialog.isShowing) {
                alertDialog.dismiss()
                frgInternetAvailable()
            } else {
                frgInternetAvailable()
            }
        }
    }

    fun getConnectionType(context: Context): Int {
        var result = 0 // Returns connection type. 0: none; 1: mobile data; 2: wifi
        val cm = context.getSystemService(AppCompatActivity.CONNECTIVITY_SERVICE) as ConnectivityManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val capabilities = cm.getNetworkCapabilities(cm.activeNetwork)
            if (capabilities != null) {
                when {
                    capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> {
                        result = 2
                    }
                    capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> {
                        result = 1
                    }
                    capabilities.hasTransport(NetworkCapabilities.TRANSPORT_VPN) -> {
                        result = 3
                    }
                }
            }
            Log.e("Wifi", "net enable=$result")
        } else {
            val activeNetwork = cm.activeNetworkInfo
            if (activeNetwork != null) {
                // connected to the internet
                when (activeNetwork.type) {
                    ConnectivityManager.TYPE_WIFI -> {
                        result = 2
                    }
                    ConnectivityManager.TYPE_MOBILE -> {
                        result = 1
                    }
                    ConnectivityManager.TYPE_VPN -> {
                        result = 3
                    }
                }
            }
            Log.e("Wifi", "not net enable=$result")
        }
        return result
    }
}