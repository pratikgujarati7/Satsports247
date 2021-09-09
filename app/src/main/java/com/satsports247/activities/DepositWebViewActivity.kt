package com.satsports247.activities

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.webkit.*
import androidx.appcompat.app.AppCompatActivity
import com.satsports247.R
import com.satsports247.constants.Config
import com.satsports247.constants.IntentKeys
import com.satsports247.databinding.ActivityDepositWebviewBinding

class DepositWebViewActivity : AppCompatActivity() {

    val TAG: String = "DepositWebViewActivity"
    lateinit var binding: ActivityDepositWebviewBinding
    var url = ""
    var statusResponse = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDepositWebviewBinding.inflate(layoutInflater)
        setContentView(binding.root)

        init()
    }

    companion object {
        @SuppressLint("StaticFieldLeak")
        lateinit var webView: WebView
    }

    fun init() {
        binding.ivBack.setOnClickListener { onBackPressed() }
        webView = binding.webview
        if (intent.hasExtra(IntentKeys.depositUrl) && intent.getStringExtra(IntentKeys.depositUrl) != null) {
            url = intent.getStringExtra(IntentKeys.depositUrl)!!
            Config.showSmallProgressDialog(this)
            webView.settings.javaScriptEnabled = true
            webView.addJavascriptInterface(MyJavaScriptInterface(), "HTMLOUT")
            webView.settings.domStorageEnabled = true
            webView.webViewClient = object : WebViewClient() {
                override fun onPageFinished(view: WebView, url: String) {
                    Config.hideSmallProgressDialog()
                    Log.e(TAG, "onPageFinished: $url")
                    webView.loadUrl("javascript:window.HTMLOUT.processHTML(document.getElementsByTagName('h1')[0].innerHTML);")
                    statusResponse = url
//                    if (url.contains("success")) {
//                        setResult(RESULT_OK, Intent())
//                        finish()
//                    }
                }

                override fun onReceivedError(
                    view: WebView?,
                    request: WebResourceRequest?,
                    error: WebResourceError?
                ) {
                    Config.hideSmallProgressDialog()
                    super.onReceivedError(view, request, error)
                }
            }
            Log.e(TAG, "url: $url")
            webView.loadUrl(url)
        }
    }

    inner class MyJavaScriptInterface {
        @JavascriptInterface
        fun processHTML(html: String) {
//            here we get all contains string which display on web page like "Congratulation"
            statusResponse = " $html"
            Log.e(TAG, "deposit html response: $html")
        }
    }

    override fun onBackPressed() {
        if (statusResponse.contains("success")) {
            setResult(RESULT_OK, Intent())
            finish()
        } else {
            Config.showConfirmationDialog(
                this,
                getString(R.string.are_you_sure_you_want_to_leave_this_page),
                object : Config.OkButtonClicklistner {
                    override fun OkButtonClick() {
                        finish()
                    }
                })
        }
    }
}