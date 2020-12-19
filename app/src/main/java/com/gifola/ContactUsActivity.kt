package com.gifola

import android.app.ProgressDialog
import android.content.Intent
import android.database.Cursor
import android.os.Bundle
import android.provider.ContactsContract
import android.util.Log
import android.util.Patterns
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentActivity
import com.gifola.apis.AdminAPI
import com.gifola.apis.SeriveGenerator
import com.gifola.constans.Global
import com.gifola.constans.SharedPreferenceHelper
import com.gifola.model.FavLocationModel
import com.gifola.model.UserData
import com.google.gson.JsonObject
import kotlinx.android.synthetic.main.activity_contact_us.*
import kotlinx.android.synthetic.main.custom_toolbar_back.*
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class ContactUsActivity : AppCompatActivity() {

    var appPreference: SharedPreferenceHelper? = null
    var adminAPI: AdminAPI? = null
    var bar: ProgressDialog? = null
    var userData: UserData? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_contact_us)
        appPreference = SharedPreferenceHelper(applicationContext)
        bar = ProgressDialog(this)
        adminAPI = SeriveGenerator.getAPIClass()

        try {
            userData = Global.getUserMe(appPreference!!)
        } catch (e: Exception) {

        }


        sendcard.setOnClickListener {
            val subject = edit_subject.text.toString().trim()
            val message = edit_message.text.toString().trim()
            val emailId = edit_to.text.toString().trim()

            if (emailId.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(emailId).matches())
                Global.displayToastMessage(getString(R.string.message_valid_email_id), applicationContext)
            else if (subject.isEmpty())
                Global.displayToastMessage(getString(R.string.message_valid_subject), applicationContext)
            else if (message.isEmpty())
                Global.displayToastMessage(getString(R.string.message_valid_message), applicationContext)
            else
                sendEmailInSupport(subject, message, emailId)


        }

        setupToolbar()
    }

    private fun sendEmailInSupport(subject: String, message: String, emailId: String) {
        val jsonRequest = JsonObject();
        jsonRequest.addProperty("AppUserId", userData?.app_usr_id ?: 0)
        jsonRequest.addProperty("MobileNo", userData?.app_usr_mobile ?: "")
        jsonRequest.addProperty("To", emailId)
        jsonRequest.addProperty("Subject", subject)
        jsonRequest.addProperty("Message", message)
        Log.e("request", jsonRequest.toString())
        val responseBodyCall: Call<ResponseBody>? = adminAPI?.SendContactRequestForm(jsonRequest)
        responseBodyCall?.enqueue(object : Callback<ResponseBody?> {
            override fun onResponse(call: Call<ResponseBody?>, response: Response<ResponseBody?>) {
                if (response.code() == 200) {
                    Global.displayToastMessage(getString(R.string.message_email_sent), applicationContext)
                    val intent = Intent(this@ContactUsActivity, DashboardActivity::class.java)
                    startActivity(intent)
                    finish()
                }

            }

            override fun onFailure(call: Call<ResponseBody?>, t: Throwable) {
            }
        })
    }

    private fun setupToolbar() {
        setSupportActionBar(toolbarsignup)
        txt_title!!.text = "Contact Us"
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportActionBar!!.setHomeButtonEnabled(true)
        toolbarsignup!!.setNavigationIcon(R.drawable.back_1)
        toolbarsignup!!.setNavigationOnClickListener { onBackPressed() }
    }
}