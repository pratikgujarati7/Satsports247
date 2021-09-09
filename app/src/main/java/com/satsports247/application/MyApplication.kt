package com.satsports247.application

import android.app.Application
import android.content.Context
import android.content.ContextWrapper
import android.content.res.Configuration
import android.content.res.Resources
import android.graphics.Typeface
import android.os.Build
import android.os.LocaleList
import android.util.Log
import com.google.android.gms.common.GooglePlayServicesNotAvailableException
import com.google.android.gms.common.GooglePlayServicesRepairableException
import com.google.android.gms.security.ProviderInstaller
import com.satsports247.constants.AppConstants
import com.satsports247.constants.Config
import com.satsports247.constants.PreferenceKeys
import io.realm.Realm
import java.security.NoSuchAlgorithmException
import java.util.*
import javax.net.ssl.SSLContext

class MyApplication : Application() {

    private var appContext: Context? = null
    private var typeface: Typeface? = null

    override fun onCreate() {
        super.onCreate()
        appContext = applicationContext
        typeface = Typeface.createFromAsset(assets, "fonts/Montserrat-Medium.ttf")
//        contextWrapLanguage(applicationContext)
        Realm.init(this)
        initializeSSLContext()

        /*var c = RealmConfiguration.Builder()
        c.name("Betskey")
        c.deleteRealmIfMigrationNeeded()
        Realm.setDefaultConfiguration(c.build())*/
    }

     private fun initializeSSLContext() {
         try {
             SSLContext.getInstance("TLSv1.2")
         } catch (e: NoSuchAlgorithmException) {
             e.printStackTrace()
         }
         try {
             ProviderInstaller.installIfNeeded(appContext?.applicationContext)
         } catch (e: GooglePlayServicesRepairableException) {
             e.printStackTrace()
         } catch (e: GooglePlayServicesNotAvailableException) {
             e.printStackTrace()
         }
     }

    private fun contextWrapLanguage(context: Context): ContextWrapper? {
        var context = context
        val appLang: String = Locale.getDefault().language
        if (appLang.equals(AppConstants.hi, ignoreCase = true))
            Config.saveSharedPreferences(context, PreferenceKeys.Language, AppConstants.hi)
        else
            Config.saveSharedPreferences(context, PreferenceKeys.Language, AppConstants.en)

        val newLocale = Locale(Config.getSharedPreferences(context, PreferenceKeys.Language))
        val res: Resources = context.resources
        val configuration: Configuration = res.configuration
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            configuration.setLocale(newLocale)
            val localeList = LocaleList(newLocale)
            LocaleList.setDefault(localeList)
            configuration.setLocales(localeList)
            context = context.createConfigurationContext(configuration)
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            configuration.setLocale(newLocale)
            context = context.createConfigurationContext(configuration)
        } else {
            configuration.locale = newLocale
            res.updateConfiguration(configuration, res.getDisplayMetrics())
        }
        Log.e(
            "Application",
            "language: " + Config.getSharedPreferences(context, PreferenceKeys.Language)
        )
        return ContextWrapper(context)
    }
}