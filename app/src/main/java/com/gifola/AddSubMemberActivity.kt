package com.gifola

import android.app.ProgressDialog
import android.content.DialogInterface
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ArrayAdapter
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import com.gifola.adapter.SubMemberListAdapter
import com.gifola.apis.AdminAPI
import com.gifola.apis.SeriveGenerator
import com.gifola.constans.Global
import com.gifola.constans.SharedPreferenceHelper
import com.gifola.model.*
import com.google.gson.JsonObject
import kotlinx.android.synthetic.main.activity_add_sub_member.*
import kotlinx.android.synthetic.main.activity_add_sub_member.btn_add
import kotlinx.android.synthetic.main.activity_add_sub_member.rv_location
import kotlinx.android.synthetic.main.activity_add_sub_member.spinner2
import kotlinx.android.synthetic.main.activity_add_sub_member.txt_no_data_found
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class AddSubMemberActivity : AppCompatActivity() {
    var member: ArrayList<String> = ArrayList()
    var toolbar: Toolbar? = null
    var txt_title: TextView? = null
    var rfLocationList: MutableList<RFLocationModel> = ArrayList()
    var bar: ProgressDialog? = null
    var adminAPI: AdminAPI? = null
    var preferenceHelper: SharedPreferenceHelper? = null
    var userData: UserData? = null
    var memberId : Int = 0
    var rfCardDataModelItems: ArrayList<SubMemberModelItem> = ArrayList()
    lateinit var rfCardAdapter : SubMemberListAdapter
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_sub_member)
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
            val mobileNo = edit_mobileno.text.toString().trim()
            val memberName = edit_submembername.text.toString().trim()
            var selectedLocationId : Int = -1
            var selectedLocationMemberId : Int = 0
            var selectedLocationAddress : String = ""

            if(rfLocationList[0].size > 0) {
                val selectedLocationPosition = spinner2.selectedItemPosition
                selectedLocationId = rfLocationList[0][selectedLocationPosition].site_id
                selectedLocationMemberId = rfLocationList[0][selectedLocationPosition].mem_id
                selectedLocationAddress = rfLocationList[0][selectedLocationPosition].site_title

            }


            when {
                mobileNo.isEmpty() || mobileNo.length != 10 -> {
                    Global.displayToastMessage(getString(R.string.message_valid_mobile_num), applicationContext)
                }
                memberName.isEmpty() -> {
                    Global.displayToastMessage(getString(R.string.message_valid_member_name), applicationContext)
                }
                selectedLocationId == -1 -> {
                    Global.displayToastMessage(getString(R.string.message_valid_card_location), applicationContext)
                }
                else -> {
                    addSubMember(mobileNo, memberName, selectedLocationId, selectedLocationMemberId, selectedLocationAddress)
                }
            }
        }

        setupToolbar()
        getSubMemberList(memberId)
        getRFLocationList()
    }


    private fun setupToolbar() {
        toolbar = findViewById<View>(R.id.toolbarsignup) as Toolbar
        txt_title = findViewById<View>(R.id.txt_title) as TextView
        setSupportActionBar(toolbar)
        txt_title!!.text = "Add/Remove Sub Member"
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportActionBar!!.setHomeButtonEnabled(true)
        toolbar!!.setNavigationIcon(R.drawable.back_1)
        toolbar!!.setNavigationOnClickListener { onBackPressed() }
    }

    private fun getRFLocationList() {
        val responseBodyCall: Call<RFLocationModel>? = adminAPI?.GetSubMemberLocations(userData?.app_usr_id!!)
        responseBodyCall?.enqueue(object : Callback<RFLocationModel?> {
            override fun onResponse(call: Call<RFLocationModel?>, response: Response<RFLocationModel?>) {
                if (response.code() == 200) {
                    try{
                        response.body()?.let { rfLocationList.add(it) }
                        if(rfLocationList.size > 0){
                            rfLocationList[0].forEachIndexed { index, rfLocationModel ->
                                member.add(rfLocationModel.site_title)
                            }

                            val spinnerArrayAdapter = ArrayAdapter(this@AddSubMemberActivity, android.R.layout.simple_spinner_item, member)
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

    private fun getSubMemberList(memberId : Int) {
        bar?.hide()
        val responseBodyCall: Call<SubMemberModel>? = adminAPI?.GetAllSubMembers(memberId)
        responseBodyCall?.enqueue(object : Callback<SubMemberModel?> {
            override fun onResponse(call: Call<SubMemberModel?>, response: Response<SubMemberModel?>) {
                bar?.hide()
                rfCardDataModelItems.clear()
                if (response.code() == 200) {
                    val rfCardDataModel: SubMemberModel? = response.body()
                    rfCardDataModel?.forEachIndexed { index, rfCardModelItem ->
                        rfCardDataModelItems.add(rfCardDataModel[index])
                    }

                    rfCardAdapter = object : SubMemberListAdapter(this@AddSubMemberActivity, rfCardDataModelItems){
                        override fun deleteRFCard(appUsrRfId: Int, memSiteId: Int) {
                            deletedSubMember(appUsrRfId, memSiteId)
                        }

                    }
                    val layoutManager = LinearLayoutManager(this@AddSubMemberActivity, LinearLayoutManager.VERTICAL, false)
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

            override fun onFailure(call: Call<SubMemberModel?>, t: Throwable) {
                bar?.hide()
                txt_no_data_found.visibility = View.VISIBLE
            }
        })
    }

    private fun addSubMember(mobileNo: String, memberName: String, selectedLocationId: Int, selectedLocationMemberId: Int, selectedLocationAddress: String) {
        val jsonRequest = JsonObject()
        jsonRequest.addProperty("mem_app_usr_id", userData?.app_usr_id)
        jsonRequest.addProperty("mem_under_id", selectedLocationMemberId)
        jsonRequest.addProperty("member_address", selectedLocationAddress)
        jsonRequest.addProperty("mem_mob", mobileNo)
        jsonRequest.addProperty("mem_cust_id", userData?.mem_cust_id)
        jsonRequest.addProperty("mem_name", memberName)
        jsonRequest.addProperty("site_id", selectedLocationId)
        bar?.show()
        val responseBodyCall: Call<ResponseBody>? = adminAPI?.AddSubMember(jsonRequest)
        responseBodyCall?.enqueue(object : Callback<ResponseBody?> {
            override fun onResponse(call: Call<ResponseBody?>, response: Response<ResponseBody?>) {
                if (response.code() == 200) {
                    edit_mobileno.setText("")
                    edit_submembername.setText("")
                    spinner2.setSelection(0)
                    Global.displayToastMessage(getString(R.string.message_member_added), applicationContext)
                    getSubMemberList(memberId)
                }else if(response.code() == 400){
                    bar?.hide()
                    Global.displayToastMessage(getString(R.string.message_mobile_no_already_exist), applicationContext)
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

    private fun deletedSubMember(appUsrRfId: Int, memSiteId: Int) {
        val alert: AlertDialog.Builder = AlertDialog.Builder(this)

        alert.setTitle("Delete Member")
        alert.setMessage("Are you sure you want to delete this member?")
        alert.setPositiveButton(android.R.string.yes, object : DialogInterface.OnClickListener {
            override fun onClick(dialog: DialogInterface, which: Int) {
                val jsonRequest = JsonObject();
                jsonRequest.addProperty("mem_id", appUsrRfId)
                jsonRequest.addProperty("mem_site_id", memSiteId)
                bar?.show()
                val responseBodyCall: Call<ResponseBody>? = adminAPI?.DeletedSubMember(jsonRequest)
                responseBodyCall?.enqueue(object : Callback<ResponseBody?> {
                    override fun onResponse(call: Call<ResponseBody?>, response: Response<ResponseBody?>) {
                        getSubMemberList(memberId)
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