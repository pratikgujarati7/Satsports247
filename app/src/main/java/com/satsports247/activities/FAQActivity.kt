package com.satsports247.activities

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.github.barteksc.pdfviewer.listener.OnLoadCompleteListener
import com.github.barteksc.pdfviewer.listener.OnPageChangeListener
import com.satsports247.databinding.ActivityFAQBinding

class FAQActivity : AppCompatActivity(), OnLoadCompleteListener, OnPageChangeListener {

    val TAG: String = "FAQActivity"
    lateinit var binding: ActivityFAQBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFAQBinding.inflate(layoutInflater)
        setContentView(binding.root)

        init()
    }

    fun init() {
        binding.ivBack.setOnClickListener { onBackPressed() }
        binding.pdfView.fromAsset("faqs.pdf").onPageChange(this)
            .swipeHorizontal(true)
            .onLoad(this).load()
    }

    override fun onBackPressed() {
        super.onBackPressed()
        finishAfterTransition()
    }

    override fun onPageChanged(page: Int, pageCount: Int) {

    }

    override fun loadComplete(nbPages: Int) {

    }
}