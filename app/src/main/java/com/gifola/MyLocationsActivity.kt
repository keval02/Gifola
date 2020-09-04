package com.gifola

import android.app.Activity
import android.app.ProgressDialog
import android.os.AsyncTask
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.gifola.adapter.LocationListAdapter
import com.gifola.apis.AdminAPI
import com.gifola.apis.ApiURLs
import com.gifola.apis.SeriveGenerator
import com.gifola.apis.ServiceHandler
import com.gifola.constans.Global
import com.gifola.constans.Global.getUserMe
import com.gifola.constans.SharedPreferenceHelper
import com.gifola.customfonts.MyTextViewBold
import com.gifola.model.LocationDataModel
import com.gifola.model.LocationDataModelItem
import com.google.gson.Gson
import com.google.gson.JsonObject
import kotlinx.android.synthetic.main.activity_add_u_h_f_card.*
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*

class MyLocationsActivity : AppCompatActivity() {
    var toolbar: Toolbar? = null
    var txt_title: TextView? = null
    var rv_location: RecyclerView? = null
    var preferenceHelper: SharedPreferenceHelper? = null
    var progressDialog: ProgressDialog? = null
    var usersMobileNum: String? = ""
    var locationDataModelItems: MutableList<LocationDataModelItem> = ArrayList()
    var locationListAdapter: LocationListAdapter? = null
    var txtNoDataFound: MyTextViewBold? = null
    var adminAPI: AdminAPI? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_locations)
        setupToolbar()
        preferenceHelper = SharedPreferenceHelper(this)
        progressDialog = ProgressDialog(this)
        adminAPI = SeriveGenerator.getAPIClass()
        rv_location = findViewById(R.id.rv_location)
        txtNoDataFound = findViewById(R.id.txt_no_data_found)
        usersMobileNum = getUserMe(preferenceHelper!!)!!.app_usr_mobile
        GetUsersLocations(usersMobileNum, this).execute()
    }

    internal inner class GetUsersLocations(mobileNum: String?, activity: Activity) : AsyncTask<Void?, Void?, String?>() {
        var mobileNum: String? = ""
        var activity: Activity
        override fun onPreExecute() {
            progressDialog!!.show()
        }

         override fun doInBackground(vararg params: Void?): String? {
            var json: String? = ""
            try {
                json = ServiceHandler().makeServiceCall(ApiURLs.BASE_URL + ApiURLs.GET_USERS_LOCATIONS + mobileNum, ServiceHandler.GET)
            } catch (e: Exception) {
                Log.e("exception", e.message)
            }
            return json
        }

        override fun onPostExecute(s: String?) {
            super.onPostExecute(s)
            progressDialog!!.hide()
            try {
                if (s == null || s.isEmpty()) {
                    rv_location!!.visibility = View.GONE
                    txtNoDataFound!!.visibility = View.VISIBLE
                } else {
                    val jsonResponse: String = s
                    val gson = Gson()
                    val locationDataModel = gson.fromJson(jsonResponse, LocationDataModel::class.java)
                    locationDataModelItems.addAll(locationDataModel)
                    locationListAdapter = object : LocationListAdapter(activity, locationDataModelItems) {
                        override fun updateLocationDetails(locationTitle: String, locationAddress: String, locationID: String) {
                            updateLocationDetail(locationTitle, locationAddress, locationID)
                        }
                    }
                    val layoutManager = LinearLayoutManager(activity, LinearLayoutManager.VERTICAL, false)
                    rv_location!!.layoutManager = layoutManager
                    rv_location!!.adapter = locationListAdapter
                    rv_location!!.visibility = View.VISIBLE
                    txtNoDataFound!!.visibility = View.GONE
                }
            } catch (e: Exception) {
                txtNoDataFound!!.visibility = View.VISIBLE
                Log.e("exception", e.message)
            }
        }

        init {
            this.mobileNum = mobileNum
            this.activity = activity
        }
    }

    private fun setupToolbar() {
        toolbar = findViewById<View>(R.id.toolbarsignup) as Toolbar
        txt_title = findViewById<View>(R.id.txt_title) as TextView
        setSupportActionBar(toolbar)
        txt_title!!.text = "My Locations"
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportActionBar!!.setHomeButtonEnabled(true)
        toolbar!!.setNavigationIcon(R.drawable.back_1)
        toolbar!!.setNavigationOnClickListener { onBackPressed() }
    }

    private fun updateLocationDetail(locationTitle: String, locationAddress: String, locationID: String) {
        val jsonRequest = JsonObject();
        jsonRequest.addProperty("site_title", locationTitle)
        jsonRequest.addProperty("site_address", locationAddress)
        jsonRequest.addProperty("mem_site_id", locationID)
        val responseBodyCall: Call<ResponseBody>? = adminAPI?.AddLocationDetails(jsonRequest)
        responseBodyCall?.enqueue(object : Callback<ResponseBody?> {
            override fun onResponse(call: Call<ResponseBody?>, response: Response<ResponseBody?>) {

            }

            override fun onFailure(call: Call<ResponseBody?>, t: Throwable) {

            }
        })
    }
}