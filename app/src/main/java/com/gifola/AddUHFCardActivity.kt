package com.gifola

import android.app.ProgressDialog
import android.content.DialogInterface
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import com.gifola.adapter.UHFCardListAdapter
import com.gifola.apis.AdminAPI
import com.gifola.apis.SeriveGenerator
import com.gifola.constans.Global
import com.gifola.constans.SharedPreferenceHelper
import com.gifola.model.*
import com.google.gson.JsonObject
import kotlinx.android.synthetic.main.activity_add_u_h_f_card.*
import kotlinx.android.synthetic.main.activity_add_u_h_f_card.btn_add
import kotlinx.android.synthetic.main.activity_add_u_h_f_card.rv_location
import kotlinx.android.synthetic.main.activity_add_u_h_f_card.spinner2
import kotlinx.android.synthetic.main.activity_add_u_h_f_card.txt_no_data_found
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class AddUHFCardActivity : AppCompatActivity() {
    var member: ArrayList<String> = ArrayList()
    var toolbar: Toolbar? = null
    var txt_title: TextView? = null
    var rfLocationList: MutableList<RFLocationModel> = ArrayList()
    var bar: ProgressDialog? = null
    var adminAPI: AdminAPI? = null
    var preferenceHelper: SharedPreferenceHelper? = null
    var userData: UserData? = null
    var memberId : Int = 0
    var rfCardDataModelItems: ArrayList<UHFCardModelItem> = ArrayList()
    lateinit var rfCardAdapter : UHFCardListAdapter
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_u_h_f_card)
        preferenceHelper = SharedPreferenceHelper(applicationContext)
        bar = ProgressDialog(this)
        adminAPI = SeriveGenerator.getAPIClass()
        try {
            userData = Global.getUserMe(preferenceHelper!!)
            memberId = userData?.mem_id ?: 0
        } catch (e: Exception) {
            Log.e("exception", e.message)
        }

        btn_add.setOnClickListener {
            val uhfTagNumber = edit_tagno.text.toString().trim()
            val uhfVehicleName = edit_vehiclename.text.toString().trim()
            val uhfVehicleNumber = edit_vehicleno.text.toString().trim()
            var isLocationAvailable : Boolean = false
            var selectedLocationId : Int = 0
            var selectedLocationMemberId : Int = 0

            when {
                uhfTagNumber.isEmpty() -> {
                    Global.displayToastMessage(getString(R.string.message_valid_tag_no), applicationContext)
                }
                uhfVehicleName.isEmpty() -> {
                    Global.displayToastMessage(getString(R.string.message_valid_vehicle_name), applicationContext)
                }
                uhfVehicleNumber.isEmpty() -> {
                    Global.displayToastMessage(getString(R.string.message_valid_vehicle_no), applicationContext)
                }
                else -> {
                    if(rfLocationList[0].size > 0) {
                        val selectedLocationPosition = spinner2.selectedItemPosition
                        selectedLocationId = rfLocationList[0][selectedLocationPosition].site_id
                        selectedLocationMemberId = rfLocationList[0][selectedLocationPosition].mem_id
                        isLocationAvailable = true
                    }
                    addUHFCard(uhfTagNumber, uhfVehicleName, uhfVehicleNumber , selectedLocationId, selectedLocationMemberId, isLocationAvailable)
                }
            }

        }

        spinner2?.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
            override fun onNothingSelected(parent: AdapterView<*>?) {

            }

            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                spinner2.setSelection(position)
            }

        }

        setupToolbar()
        getUHFCardList(memberId)
        getRFLocationList()
    }


    private fun setupToolbar() {
        toolbar = findViewById<View>(R.id.toolbarsignup) as Toolbar
        txt_title = findViewById<View>(R.id.txt_title) as TextView
        setSupportActionBar(toolbar)
        txt_title!!.text = "Add/Remove Car UHF Tag"
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportActionBar!!.setHomeButtonEnabled(true)
        toolbar!!.setNavigationIcon(R.drawable.back_1)
        toolbar!!.setNavigationOnClickListener { onBackPressed() }
    }

    private fun getRFLocationList() {
        val responseBodyCall: Call<RFLocationModel>? = adminAPI?.GetUHFLocations(userData?.app_usr_id!!)
        responseBodyCall?.enqueue(object : Callback<RFLocationModel?> {
            override fun onResponse(call: Call<RFLocationModel?>, response: Response<RFLocationModel?>) {
                if (response.code() == 200) {
                    try{
                        response.body()?.let { rfLocationList.add(it) }
                        if(rfLocationList.size > 0){
                            rfLocationList[0].forEachIndexed { index, rfLocationModel ->
                                member.add(rfLocationModel.site_title)
                            }

                            val spinnerArrayAdapter = ArrayAdapter(this@AddUHFCardActivity, android.R.layout.simple_spinner_item, member)
                            spinnerArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item) // The drop down view
                            spinner2.adapter = spinnerArrayAdapter;


                        }
                    }catch (e : Exception){
                        Log.e("exception" , e.message)
                    }

                } else {
                    Global.displayToastMessage(getString(R.string.message_something_went_wrong), applicationContext)
                }
            }

            override fun onFailure(call: Call<RFLocationModel?>, t: Throwable) {
                Global.displayToastMessage(getString(R.string.message_something_went_wrong), applicationContext)
            }
        })


    }

    private fun getUHFCardList(memberId : Int) {
        bar?.hide()
        val jsonRequest = JsonObject();
        jsonRequest.addProperty("app_usr_id", userData?.app_usr_id)
        jsonRequest.addProperty("mem_id", memberId)
        val responseBodyCall: Call<UHFCardModel>? = adminAPI?.GetAllUHFCards(jsonRequest)
        responseBodyCall?.enqueue(object : Callback<UHFCardModel?> {
            override fun onResponse(call: Call<UHFCardModel?>, response: Response<UHFCardModel?>) {
                bar?.hide()
                rfCardDataModelItems.clear()
                if (response.code() == 200) {
                    val rfCardDataModel: UHFCardModel? = response.body()
                    rfCardDataModel?.forEachIndexed { index, rfCardModelItem ->
                        rfCardDataModelItems.add(rfCardDataModel[index])
                    }

                    rfCardAdapter = object : UHFCardListAdapter(this@AddUHFCardActivity, rfCardDataModelItems){
                        override fun deleteRFCard(appUsrRfId: Int, appUhfStId: Int) {
                            deletedCard(appUsrRfId, appUhfStId)
                        }

                        override fun copyUHFCard(uhfCardModelItem: UHFCardModelItem){
                            edit_tagno.setText(uhfCardModelItem.cardNo)
                            edit_vehiclename.setText(uhfCardModelItem.app_usr_Vh_name)
                            edit_vehicleno.setText(uhfCardModelItem.app_usr_Vh_no)
                            val locationId = uhfCardModelItem.site_id
                            var spinnerPosition = 0
                            rfLocationList[0].forEachIndexed { index, rfCardModelItems ->
                                if(rfCardModelItems.site_id == locationId){
                                    spinnerPosition = index
                                }
                            }
                            spinner2.setSelection(spinnerPosition)
                        }

                    }
                    val layoutManager = LinearLayoutManager(this@AddUHFCardActivity, LinearLayoutManager.VERTICAL, false)
                    rv_location.layoutManager = layoutManager
                    rv_location.adapter = rfCardAdapter

                    if(rfCardDataModelItems.size > 0){
                        rv_location.visibility = View.VISIBLE
                        txt_no_data_found.visibility = View.GONE
                    }else {
                        txt_no_data_found.visibility = View.VISIBLE
                        rv_location.visibility = View.GONE
                    }


                } else {
                    txt_no_data_found.visibility = View.VISIBLE
                }
            }

            override fun onFailure(call: Call<UHFCardModel?>, t: Throwable) {
                bar?.hide()
                txt_no_data_found.visibility = View.VISIBLE
            }
        })
    }

    private fun addUHFCard(rfCardNumber: String, rfCardHolderName: String, vehichleNumber : String , selectedLocationId: Int, selectedLocationMemberId: Int, isLocationAvailable : Boolean) {
        val jsonRequest = JsonObject();
        jsonRequest.addProperty("app_usr_uhf_tag", rfCardNumber)
        jsonRequest.addProperty("app_usr_Vh_name", rfCardHolderName)
        jsonRequest.addProperty("app_usr_Vh_no", vehichleNumber)
        jsonRequest.addProperty("mem_site_id", selectedLocationId)
        jsonRequest.addProperty("app_usr_id", userData?.app_usr_id)
        jsonRequest.addProperty("mem_id", selectedLocationMemberId)
        jsonRequest.addProperty("isLocationAvailable", isLocationAvailable)
        jsonRequest.addProperty("isactive", true)
        jsonRequest.addProperty("dev_id", "")
        jsonRequest.addProperty("mem_cust_id", userData?.mem_cust_id)

        bar?.show()
        val responseBodyCall: Call<ResponseBody>? = adminAPI?.AddUHFCard(jsonRequest)
        responseBodyCall?.enqueue(object : Callback<ResponseBody?> {
            override fun onResponse(call: Call<ResponseBody?>, response: Response<ResponseBody?>) {
                if (response.code() == 200) {
                    edit_tagno.setText("")
                    edit_vehiclename.setText("")
                    edit_vehicleno.setText("")
                    spinner2.setSelection(0)
                    Global.displayToastMessage(getString(R.string.message_rf_card_added), applicationContext)
                    getUHFCardList(memberId)
                }else if(response.code() == 400){
                    bar?.hide()
                    Global.displayToastMessage(getString(R.string.message_card_already_exist), applicationContext)
                } else {
                    bar?.hide()
                    Global.displayToastMessage(getString(R.string.message_something_went_wrong), applicationContext)
                }
            }

            override fun onFailure(call: Call<ResponseBody?>, t: Throwable) {
                bar?.hide()
                Global.displayToastMessage(getString(R.string.message_something_went_wrong), applicationContext)
            }
        })
    }

    private fun deletedCard(appUsrRfId: Int, appUhfStId: Int) {
        val alert: AlertDialog.Builder = AlertDialog.Builder(this)

        alert.setTitle("Delete Card")
        alert.setMessage("Are you sure you want to delete this card?")
        alert.setPositiveButton(android.R.string.yes, object : DialogInterface.OnClickListener {
            override fun onClick(dialog: DialogInterface, which: Int) {
                val jsonRequest = JsonObject();
                jsonRequest.addProperty("app_usr_UHF_id", appUsrRfId)
                jsonRequest.addProperty("app_UHF_st_id", appUhfStId)
                bar?.show()
                val responseBodyCall: Call<ResponseBody>? = adminAPI?.DeletedUHFCard(jsonRequest)
                responseBodyCall?.enqueue(object : Callback<ResponseBody?> {
                    override fun onResponse(call: Call<ResponseBody?>, response: Response<ResponseBody?>) {
                        getUHFCardList(memberId)
                    }

                    override fun onFailure(call: Call<ResponseBody?>, t: Throwable) {
                        bar?.hide()
                        Global.displayToastMessage(getString(R.string.message_something_went_wrong), applicationContext)
                    }
                })
            }
        })
        alert.setNegativeButton(android.R.string.no, DialogInterface.OnClickListener { dialog, which -> // close dialog
            dialog.cancel()
        })
        alert.show()
    }
}