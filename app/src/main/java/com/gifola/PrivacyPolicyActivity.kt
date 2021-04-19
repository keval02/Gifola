package com.gifola

import android.app.ProgressDialog
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.gifola.apis.AdminAPI
import com.gifola.apis.SeriveGenerator
import com.gifola.constans.Global
import com.gifola.constans.SharedPreferenceHelper
import com.gifola.model.PrivacySettingsModel
import com.gifola.model.UserData
import com.google.gson.JsonObject
import kotlinx.android.synthetic.main.activity_privacy_policy.*
import kotlinx.android.synthetic.main.custom_toolbar_back.*
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class PrivacyPolicyActivity : AppCompatActivity() {
    var bar: ProgressDialog? = null
    var adminAPI: AdminAPI? = null
    var preferenceHelper: SharedPreferenceHelper? = null
    var userData: UserData? = null
    var isAPIResponseSet: Boolean = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_privacy_policy)
        setupToolbar()
        preferenceHelper = SharedPreferenceHelper(applicationContext)
        bar = ProgressDialog(this)
        adminAPI = SeriveGenerator.getAPIClass()
        try {
            userData = Global.getUserMe(preferenceHelper!!)
        } catch (e: Exception) {
            Log.e("exception", e.message)
        }






        getPrivacySettingsData(userData?.app_usr_id)
    }

    private fun updatePrivacySettings() {
        var switchButton1: Int = 0
        var switchButton2: Int = 0
        var switchButton3: Int = 0
        var switchButton4: Int = 0

        if (switch_button1.isChecked) {
            switchButton1 = 1
        }
        if (switch_button2.isChecked) {
            switchButton2 = 1
        }
        if (switch_button3.isChecked) {
            switchButton3 = 1
        }
        if (switch_button4.isChecked) {
            switchButton4 = 1
        }

        val jsonRequest = JsonObject()
        jsonRequest.addProperty("AppUserId", userData?.app_usr_id ?: 0)
        jsonRequest.addProperty("VisitorSecret", switchButton1)
        jsonRequest.addProperty("SwitchNotify", switchButton2)
        jsonRequest.addProperty("VisitSecret", switchButton3)
        jsonRequest.addProperty("HideVisit", switchButton4)
        Log.e("request", jsonRequest.toString())
        val responseBodyCall: Call<ResponseBody>? = adminAPI?.SetPrivacySettings(jsonRequest)
        responseBodyCall?.enqueue(object : Callback<ResponseBody?> {
            override fun onResponse(call: Call<ResponseBody?>, response: Response<ResponseBody?>) {
                if (response.code() == 200) {

                }

            }

            override fun onFailure(call: Call<ResponseBody?>, t: Throwable) {
            }
        })
    }

    private fun getPrivacySettingsData(appUsrId: Int?) {
        val responseBodyCall: Call<PrivacySettingsModel>? = adminAPI?.GetPrivacyData(appUsrId ?: 0)
        Log.e("apiURL", responseBodyCall?.request()?.url().toString())
        responseBodyCall?.enqueue(object : Callback<PrivacySettingsModel?> {
            override fun onResponse(call: Call<PrivacySettingsModel?>, response: Response<PrivacySettingsModel?>) {
                if (response.code() == 200) {
                    val privacySettingsModel: PrivacySettingsModel? = response.body()
                    switch_button1.setOnCheckedChangeListener(null)
                    switch_button2.setOnCheckedChangeListener(null)
                    switch_button3.setOnCheckedChangeListener(null)
                    switch_button4.setOnCheckedChangeListener(null)

                    switch_button1.isChecked = privacySettingsModel?.VisitorSecret != 0
                    switch_button2.isChecked = privacySettingsModel?.SwitchNotify != 0
                    switch_button3.isChecked = privacySettingsModel?.VisitSecret != 0
                    switch_button4.isChecked = privacySettingsModel?.HideVisit != 0

                    switch_button1.setOnCheckedChangeListener { view, isChecked ->
                        updatePrivacySettings()
                    }

                    switch_button2.setOnCheckedChangeListener { view, isChecked ->
                        updatePrivacySettings()
                    }

                    switch_button3.setOnCheckedChangeListener { view, isChecked ->
                        updatePrivacySettings()
                    }

                    switch_button4.setOnCheckedChangeListener { view, isChecked ->
                        updatePrivacySettings()
                    }
                } else {

                }
            }

            override fun onFailure(call: Call<PrivacySettingsModel?>, t: Throwable) {

            }
        })
    }

    private fun setupToolbar() {
        setSupportActionBar(toolbarsignup)
        txt_title.setText("My Privacy Settings")
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportActionBar!!.setHomeButtonEnabled(true)
        toolbarsignup.setNavigationIcon(R.drawable.back_1)
        toolbarsignup.setNavigationOnClickListener(View.OnClickListener { onBackPressed() })
    }
}


