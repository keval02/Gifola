package com.gifola

import android.app.ProgressDialog
import android.content.Intent
import android.os.AsyncTask
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import com.gifola.adapter.AllowCheckInListAdapter
import com.gifola.adapter.CallLogsAdapter
import com.gifola.adapter.ContactsAdapter
import com.gifola.apis.AdminAPI
import com.gifola.apis.SeriveGenerator
import com.gifola.constans.Global
import com.gifola.constans.Global.checkedInUserInfo
import com.gifola.constans.Global.keyIsFromFavourite
import com.gifola.constans.SharedPreferenceHelper
import com.gifola.model.*
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.wickerlabs.logmanager.LogObject
import com.wickerlabs.logmanager.LogsManager
import jagerfield.mobilecontactslibrary.ImportContactsAsync
import jagerfield.mobilecontactslibrary.ImportContactsAsync.ICallback
import kotlinx.android.synthetic.main.activity_send_permission.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class SendPermissionActivity : AppCompatActivity() {
    var toolbar: Toolbar? = null
    var txt_title: TextView? = null
    var bar: ProgressDialog? = null
    var adminAPI: AdminAPI? = null
    var preferenceHelper: SharedPreferenceHelper? = null
    var userData: UserData? = null
    var listType: Int = 0
    var checkInList: ArrayList<CheckInListModelItem> = ArrayList()
    var contactList: ArrayList<ContactsModel> = ArrayList()
    var checkInListAll: ArrayList<CheckInListModelItem> = ArrayList()
    var callLogsListAll: ArrayList<LogObject> = ArrayList()
    var contactListAll: ArrayList<ContactsModel> = ArrayList()
    lateinit var checkInListAdapter: AllowCheckInListAdapter
    lateinit var callLogsAdapter: CallLogsAdapter
    lateinit var contactsAdapter: ContactsAdapter
    lateinit var callLogs: MutableList<LogObject>
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_send_permission)
        preferenceHelper = SharedPreferenceHelper(applicationContext)
        bar = ProgressDialog(this)
        adminAPI = SeriveGenerator.getAPIClass()
        try {
            userData = Global.getUserMe(preferenceHelper!!)
        } catch (e: Exception) {
            Log.e("exception", e.message)
        }
        setupToolbar()
        fetchid()
    }

    private fun fetchid() {
        checkInListAdapter = object : AllowCheckInListAdapter(this, checkInList) {
            override fun onCheckedList(number: String, contactName: String) {
                refactorUsersMobileNum(number, contactName, true)
            }

        }
        val layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        rv_permission_checkin.layoutManager = layoutManager
        rv_permission_checkin.adapter = checkInListAdapter

        contactsAdapter = object : ContactsAdapter(this, contactList) {
            override fun selectNumber(number: String, contactName: String) {
                refactorUsersMobileNum(number, contactName)
            }
        }


        val contactLayoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        rv_contacts.layoutManager = contactLayoutManager
        rv_contacts.adapter = contactsAdapter

        try {
            val logsManager = LogsManager(this)
            callLogs = logsManager.getLogs(LogsManager.ALL_CALLS)
            callLogs.reverse()
            val recentCallLogs: MutableList<LogObject> = ArrayList()
            for (i in 0 until 100) {
                recentCallLogs.add(callLogs[i])
            }
            callLogs.clear()
            callLogs.addAll(recentCallLogs)
            callLogsListAll.addAll(recentCallLogs)
            callLogsAdapter = object : CallLogsAdapter(this, callLogs) {
                override fun selectNumber(number: String, contactName: String) {
                    refactorUsersMobileNum(number, contactName)
                }

            }
            val linearLayoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
            rv_call_logs.layoutManager = linearLayoutManager
            rv_call_logs.adapter = callLogsAdapter

            displaySelectedListView()
        } catch (e: Exception) {
            Log.e("exception", e.message)
        }

        qrscanimg.setOnClickListener(View.OnClickListener {
            val intent = Intent(applicationContext, QRScanActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            startActivity(intent)
        })

        requestper.setOnClickListener(View.OnClickListener {
            val intent = Intent(applicationContext, RequestPermissionActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            startActivity(intent)
        })
        img_history.setOnClickListener {
            listType = 0
            displaySelectedListView()
        }

        img_fav.setOnClickListener {
            listType = 1
            getCheckInList(listType, "0")
        }

        img_add.setOnClickListener {
            listType = 2
            displaySelectedListView()
        }

        edit_search.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val textLength = edit_search.text?.length
                if (textLength != null && textLength >= 3) {
                    val searchedText = edit_search.text.toString().trim()
                    when (listType) {
                        0 -> {
                            val searchLogs = callLogsAdapter.searchResult(searchedText)
                            callLogs.clear()
                            callLogs.addAll(searchLogs)
                        }
                        1 -> {
                            val searchCheckInList = checkInListAdapter.searchResult(searchedText)
                            checkInList.clear()
                            checkInList.addAll(searchCheckInList)
                        }
                        2 -> {
                            val searchContactList = contactsAdapter.searchResult(searchedText)
                            contactList.clear()
                            contactList.addAll(searchContactList)
                        }
                    }
                    displaySelectedListView()
                }

                if (edit_search.text.toString().trim().isEmpty()) {
                    when (listType) {
                        0 -> {
                            callLogs.clear()
                            callLogs.addAll(callLogsListAll)
                        }
                        1 -> {
                            checkInList.clear()
                            checkInList.addAll(checkInListAll)
                        }
                        2 -> {
                            contactList.clear()
                            contactList.addAll(contactListAll)
                        }
                    }
                    displaySelectedListView()
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }

        })

        cancel.setOnClickListener {
            if (edit_search.text.toString().trim().isNotEmpty()) {
                edit_search.setText("")
            }

        }

        GetUserContacts().execute()
    }

    private fun setupToolbar() {
        toolbar = findViewById<View>(R.id.toolbarsignup) as Toolbar
        txt_title = findViewById<View>(R.id.txt_title) as TextView
        setSupportActionBar(toolbar)
        txt_title!!.text = "Allow Permisssion/CHECKIN"
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportActionBar!!.setHomeButtonEnabled(true)
        toolbar!!.setNavigationIcon(R.drawable.back_1)
        toolbar!!.setNavigationOnClickListener {
            val intent = Intent(applicationContext, DashboardActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            startActivity(intent)
            finish()
        }
    }

    private fun getUserInfo(number: String, name: String, isFavourite: Int) {
        bar?.hide()
        val responseBodyCall: Call<CheckInUserInfoModel>? = adminAPI?.GetCheckedInUserInfo(number, 1)
        Log.e("apiURL", responseBodyCall?.request()?.url().toString())
        responseBodyCall?.enqueue(object : Callback<CheckInUserInfoModel?> {
            override fun onResponse(call: Call<CheckInUserInfoModel?>, response: Response<CheckInUserInfoModel?>) {
                bar?.hide()
                if (response.code() == 200) {
                    val userDataModel: CheckInUserInfoModel? = response.body()
                    if (userDataModel?.Status == 0) {
                        userDataModel.appUser = AppUser()
                        userDataModel.appUser.MobileNo = number
                        userDataModel.appUser.AppUserName = name
                    }
                    val userData = Gson().toJson(userDataModel)
                    val intent = Intent(applicationContext, GuestDetailActivity::class.java)
                    intent.putExtra(checkedInUserInfo, userData)
                    intent.putExtra(keyIsFromFavourite, isFavourite)
                    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                    startActivity(intent)
                } else {
                    txt_no_data_found.visibility = View.VISIBLE
                }
            }

            override fun onFailure(call: Call<CheckInUserInfoModel?>, t: Throwable) {
                bar?.hide()
                txt_no_data_found.visibility = View.VISIBLE
            }
        })
    }

    private fun getCheckInList(type: Int, lastId: String) {
        bar?.show()
        val jsonRequest = JsonObject();
        jsonRequest.addProperty("app_usr_id", userData?.app_usr_id)
        jsonRequest.addProperty("Mob_no", userData?.app_usr_mobile)
        jsonRequest.addProperty("Req_type", 1)
        jsonRequest.addProperty("Type", type)
        jsonRequest.addProperty("LastId", lastId)
        Log.e("request", jsonRequest.toString())
        val responseBodyCall: Call<CheckInListModel>? = adminAPI?.GetCheckInList(jsonRequest)
        responseBodyCall?.enqueue(object : Callback<CheckInListModel?> {
            override fun onResponse(call: Call<CheckInListModel?>, response: Response<CheckInListModel?>) {
                bar?.hide()
                checkInList.clear()
                if (response.code() == 200) {
                    val checkInListResponse = response.body()
                    checkInListResponse?.let { checkInList.addAll(it) }
                    checkInListResponse?.let { checkInListAll.addAll(it) }
                }
                displaySelectedListView()
            }

            override fun onFailure(call: Call<CheckInListModel?>, t: Throwable) {
                bar?.hide()
                displaySelectedListView()
            }
        })
    }

    private fun displaySelectedListView() {
        if (listType == 0) {
            flFavorite.visibility = View.GONE
            flRecent.visibility = View.VISIBLE
            flContacts.visibility = View.GONE
            img_history.setColorFilter(this.resources.getColor(R.color.blue))
            img_fav.setColorFilter(this.resources.getColor(R.color.black))
            img_add.setColorFilter(this.resources.getColor(R.color.black))
            if (callLogs.size > 0) {
                rv_call_logs.visibility = View.VISIBLE
                txt_no_call_logs_found.visibility = View.GONE
                callLogsAdapter.notifyDataSetChanged()
            } else {
                rv_call_logs.visibility = View.GONE
                txt_no_call_logs_found.visibility = View.VISIBLE
            }
        } else if (listType == 1) {
            flFavorite.visibility = View.VISIBLE
            flRecent.visibility = View.GONE
            flContacts.visibility = View.GONE
            img_fav.setColorFilter(this.resources.getColor(R.color.blue))
            img_history.setColorFilter(this.resources.getColor(R.color.black))
            img_add.setColorFilter(this.resources.getColor(R.color.black))
            if (checkInList.size > 0) {
                rv_permission_checkin.visibility = View.VISIBLE
                txt_no_data_found.visibility = View.GONE
                checkInListAdapter.notifyDataSetChanged()
            } else {
                rv_permission_checkin.visibility = View.GONE
                txt_no_data_found.visibility = View.VISIBLE
            }
        } else if (listType == 2) {
            flFavorite.visibility = View.GONE
            flRecent.visibility = View.GONE
            flContacts.visibility = View.VISIBLE
            img_add.setColorFilter(this.resources.getColor(R.color.blue))
            img_history.setColorFilter(this.resources.getColor(R.color.black))
            img_fav.setColorFilter(this.resources.getColor(R.color.black))
            if (contactList.size > 0) {
                rv_contacts.visibility = View.VISIBLE
                txt_no_contacts_found.visibility = View.GONE
                contactsAdapter.notifyDataSetChanged()
            } else {
                rv_contacts.visibility = View.GONE
                txt_no_contacts_found.visibility = View.VISIBLE
            }
        }
    }


    internal inner class GetUserContacts() : AsyncTask<Void?, Void?, String?>() {
        override fun onPreExecute() {
            super.onPreExecute()
        }


        override fun onPostExecute(result: String?) {
            super.onPostExecute(result)
        }


        override fun doInBackground(vararg params: Void?): String? {
            var json: String? = ""

            ImportContactsAsync(this@SendPermissionActivity, ICallback {


                it.forEach {contacts ->
                    contacts.numbers.forEach { numbers ->
                        val contactsModel = ContactsModel()
                        contactsModel.contactNum = numbers.normalizedNumber
                        contactsModel.contactName = contacts.firstName + " " + contacts.lastName

                        contactList.add(contactsModel)
                        contactListAll.add(contactsModel)
                    }
                }


            }).execute()



            return json
        }
    }


    private fun refactorUsersMobileNum(number: String, name: String, isFavourite : Boolean = false) {
        var selectedNumber = number.replace("(", "")
        selectedNumber = number.replace(")", "")
        selectedNumber = number.replace("-", "")
        selectedNumber = number.replace(" ", "")
        selectedNumber = number.replace("+91", "")
        var favorite = 0
        if(isFavourite)
            favorite = 1

        if (selectedNumber.isNotEmpty() && userData?.app_usr_mobile != selectedNumber)
            getUserInfo(selectedNumber, name, favorite)
    }
}