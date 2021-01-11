package com.gifola

import android.app.DatePickerDialog
import android.app.DatePickerDialog.OnDateSetListener
import android.app.ProgressDialog
import android.app.TimePickerDialog
import android.app.TimePickerDialog.OnTimeSetListener
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ArrayAdapter
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.gifola.adapter.ScheduleDaysAdapter
import com.gifola.apis.AdminAPI
import com.gifola.apis.ApiURLs
import com.gifola.apis.SeriveGenerator
import com.gifola.constans.Global
import com.gifola.constans.Global.checkedInUserInfo
import com.gifola.constans.SharedPreferenceHelper
import com.gifola.model.*
import com.google.gson.Gson
import com.google.gson.JsonObject
import kotlinx.android.synthetic.main.activity_permission_detail.*
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class GuestDetailActivity : AppCompatActivity() {
    var toolbar: Toolbar? = null
    var txt_title: TextView? = null
    var member: ArrayList<String> = ArrayList()
    var myCalendar: Calendar = Calendar.getInstance()
    var validDateCalender : Calendar = Calendar.getInstance()
    var checkInUserInfoModel: CheckInUserInfoModel = CheckInUserInfoModel()
    var bar: ProgressDialog? = null
    var adminAPI: AdminAPI? = null
    var preferenceHelper: SharedPreferenceHelper? = null
    var rfLocationList: MutableList<MemberSite> = ArrayList()
    var userData: UserData? = null
    var isAllowNow: Boolean = false
    var isScheduleCheckIn: Boolean = false
    var isFavorite: Int = 0
    var scheduleDays: List<String> = Arrays.asList("S", "M", "T", "W", "T", "F", "S")
    var scheduleDaysModel: ArrayList<ScheduleDaysModel> = ArrayList()
    lateinit var scheduleDaysAdapter: ScheduleDaysAdapter
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_permission_detail)
        preferenceHelper = SharedPreferenceHelper(applicationContext)
        bar = ProgressDialog(this)
        adminAPI = SeriveGenerator.getAPIClass()
        try {
            checkInUserInfoModel = Gson().fromJson(intent.getStringExtra(checkedInUserInfo), CheckInUserInfoModel::class.java)
            userData = Global.getUserMe(preferenceHelper!!)
            isFavorite = intent.getIntExtra(Global.keyIsFromFavourite, 0)

            if (isFavorite == 1)
                img_fav.setColorFilter(this.resources.getColor(R.color.blue))

            Log.e("inActivityData", Gson().toJson(checkInUserInfoModel))
        } catch (e: Exception) {

        }
        setupToolbar()
        getRFLocationList()
        fetchid()
    }

    private fun fetchid() {
        text_name.text = checkInUserInfoModel.appUser.AppUserName
        text_mobileno.text = checkInUserInfoModel.appUser.MobileNo
        if (checkInUserInfoModel.Status == 1) {
            if (checkInUserInfoModel.appUser.ProPic.isNotEmpty()) {
                Glide.with(applicationContext)
                        .load(ApiURLs.IMAGE_URL + checkInUserInfoModel.appUser.ProPic)
                        .apply(RequestOptions().error(R.drawable.user_placeholder).placeholder(R.drawable.user_placeholder))
                        .into(profile_image)
            }
        }

        scheduleDays.forEach {
            val scheduleDay = ScheduleDaysModel()
            scheduleDay.days = it
            scheduleDaysModel.add(scheduleDay)
        }

        scheduleDaysAdapter = ScheduleDaysAdapter(this, scheduleDaysModel)
        val layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        rvDays.layoutManager = layoutManager
        rvDays.adapter = scheduleDaysAdapter

        val c: Calendar = Calendar.getInstance()
        val df = SimpleDateFormat("yyyy-MM-dd")
        val dt = SimpleDateFormat("HH:mm:ss")
        val currentDate = df.format(c.time)
        val currentTime = dt.format(c.time)
        c.add(Calendar.HOUR, 25)
        val toTime = dt.format(c.time)

        date1.text = currentDate
        date2.text = currentDate
        time1.text = currentTime
        time2.text = toTime

        val date = OnDateSetListener { view, year, monthOfYear, dayOfMonth ->
            myCalendar.set(Calendar.YEAR, year)
            myCalendar.set(Calendar.MONTH, monthOfYear)
            myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
            date1.text = "$year-${monthOfYear + 1}-$dayOfMonth"
            //                updateLabel();
        }
        val validDate = OnDateSetListener { view, year, monthOfYear, dayOfMonth ->
            validDateCalender.set(Calendar.YEAR, year)
            validDateCalender.set(Calendar.MONTH, monthOfYear)
            validDateCalender.set(Calendar.DAY_OF_MONTH, dayOfMonth)
            date2.text = "$year-${monthOfYear + 1}-$dayOfMonth"
            //                updateLabel();
        }

        date1.setOnClickListener(View.OnClickListener { // TODO Auto-generated method stub
            val dpd: DatePickerDialog = DatePickerDialog(this@GuestDetailActivity, date, myCalendar
                    .get(Calendar.YEAR), myCalendar.get(Calendar.MONTH),
                    myCalendar.get(Calendar.DAY_OF_MONTH))

            dpd.datePicker.minDate = Calendar.getInstance().timeInMillis
            dpd.show()


        })
        validDateLL.setOnClickListener(View.OnClickListener { // TODO Auto-generated method stub
            val dpd: DatePickerDialog = DatePickerDialog(this@GuestDetailActivity, validDate, validDateCalender
                    .get(Calendar.YEAR), validDateCalender.get(Calendar.MONTH),
                    validDateCalender.get(Calendar.DAY_OF_MONTH))

            dpd.datePicker.minDate = myCalendar.timeInMillis
            dpd.show()
        })
        time1.setOnClickListener(View.OnClickListener { // TODO Auto-generated method stub
            val mcurrentTime = Calendar.getInstance()
            val hour = mcurrentTime[Calendar.HOUR_OF_DAY]
            val minute = mcurrentTime[Calendar.MINUTE]
            val mTimePicker: TimePickerDialog
            mTimePicker = TimePickerDialog(this@GuestDetailActivity, OnTimeSetListener { timePicker, selectedHour, selectedMinute ->
                time1.text = "$selectedHour:$selectedMinute"
            }, hour, minute, true) //Yes 24 hour time
            mTimePicker.setTitle("Select Time")
            mTimePicker.show()
        })
        time2.setOnClickListener(View.OnClickListener { // TODO Auto-generated method stub
            val mcurrentTime = Calendar.getInstance()
            val hour = mcurrentTime[Calendar.HOUR_OF_DAY]
            val minute = mcurrentTime[Calendar.MINUTE]
            val mTimePicker: TimePickerDialog
            mTimePicker = TimePickerDialog(this@GuestDetailActivity, OnTimeSetListener { timePicker, selectedHour, selectedMinute ->
                time2.text = "$selectedHour:$selectedMinute";
            }, hour, minute, true) //Yes 24 hour time
            mTimePicker.setTitle("Select Time")
            mTimePicker.show()
        })
        backcard.setOnClickListener(View.OnClickListener { onBackPressed() })
        card1.setOnClickListener(View.OnClickListener {
            card1.setCardBackgroundColor(Color.parseColor("#2196F3"))
            card2.setCardBackgroundColor(Color.parseColor("#ffffff"))
            card3.setCardBackgroundColor(Color.parseColor("#ffffff"))
            text1.setTextColor(Color.parseColor("#ffffff"))
            text2.setTextColor(Color.parseColor("#444444"))
            text3.setTextColor(Color.parseColor("#444444"))
            schedulelayout.visibility = View.GONE
            purposelayout.visibility = View.GONE
            isAllowNow = true
            isScheduleCheckIn = false
        })
        card2.setOnClickListener(View.OnClickListener {
            card1.setCardBackgroundColor(Color.parseColor("#ffffff"))
            card2.setCardBackgroundColor(Color.parseColor("#2196F3"))
            card3.setCardBackgroundColor(Color.parseColor("#ffffff"))
            text1.setTextColor(Color.parseColor("#444444"))
            text2.setTextColor(Color.parseColor("#ffffff"))
            text3.setTextColor(Color.parseColor("#444444"))
            schedulelayout.visibility = View.VISIBLE
            purposelayout.visibility = View.GONE
            isAllowNow = false
            isScheduleCheckIn = true
        })
        card3.setOnClickListener(View.OnClickListener {
            card1.setCardBackgroundColor(Color.parseColor("#ffffff"))
            card2.setCardBackgroundColor(Color.parseColor("#ffffff"))
            card3.setCardBackgroundColor(Color.parseColor("#2196F3"))
            text1.setTextColor(Color.parseColor("#444444"))
            text2.setTextColor(Color.parseColor("#444444"))
            text3.setTextColor(Color.parseColor("#ffffff"))
            schedulelayout.setVisibility(View.GONE)
            purposelayout.setVisibility(View.VISIBLE)
        })

        img_fav.setOnClickListener {
            if (isFavorite == 1) {
                isFavorite = 0
                img_fav.setColorFilter(this.resources.getColor(R.color.grey))
            } else {
                isFavorite = 1
                img_fav.setColorFilter(this.resources.getColor(R.color.blue))
            }
        }

        val spinnerArrayAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, member)
        spinnerArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item) // The drop down view
        spinner2.adapter = spinnerArrayAdapter
        sendcard.setOnClickListener(View.OnClickListener {
            if (rfLocationList.isEmpty() || rfLocationList.size <= 0) {
                Global.displayToastMessage(getString(R.string.message_valid_card_location), applicationContext)
            } else if (!isAllowNow && !isScheduleCheckIn) {
                Global.displayToastMessage(getString(R.string.message_valid_time_zone), applicationContext)
            } else {
                val Mem_id: Int = userData?.mem_id ?: 0
                val Mem_cus_id: Int = userData?.mem_cust_id ?: 0
                var App_usr_id: Int = 0
                val Mob_no: String = checkInUserInfoModel.appUser.MobileNo
                var Mem_site_id: Int = 0
                val Req_type: Int = 1
                var Req_mode: Int = 1
                var Visit_date: String = ""
                var Time_from: String = ""
                var Time_to: String = ""
                var Req_repeat: Int = 1
                var Repeat_days: String = ""
                var Repeat_upto: String = ""
                val Accept_status: Int = 1
                val Purpose: String = etPurpose.text.toString().trim()
                val Is_fav: Int = isFavorite
                var isNotValidTime = false

                if (checkInUserInfoModel.Status == 1) {
                    App_usr_id = checkInUserInfoModel.appUser.AppUserId
                }

                if (rfLocationList.size > 0) {
                    val selectedLocationPosition = spinner2.selectedItemPosition
                    Mem_site_id = rfLocationList[selectedLocationPosition].MemSiteId
                }

                if (isAllowNow) {
                    Req_mode = 0
                    Req_repeat = 0
                    val c: Calendar = Calendar.getInstance()
                    val df = SimpleDateFormat("yyyy-MM-dd")
                    val dt = SimpleDateFormat("HH:mm:ss")
                    val currentDate = df.format(c.time)
                    val currentTime = dt.format(c.time)
                    c.add(Calendar.MINUTE, 10)
                    val toTime = dt.format(c.time)
                    Visit_date = currentDate
                    Time_from = currentTime
                    Time_to = toTime
                } else {
                    Req_mode = 1
                    Req_repeat = 1
                    Visit_date = date1.text.toString()
                    Time_from = time1.text.toString()
                    Time_to = time2.text.toString()
                    Repeat_upto = date2.text.toString()

                    val selectedDate = SimpleDateFormat("yyyy-MM-dd").parse(Visit_date)
                    val currentDate = SimpleDateFormat("yyyy-MM-dd").format(Calendar.getInstance().time)
                    val currentTime = SimpleDateFormat("HH:mm:ss").format(Calendar.getInstance().time)
                    val currentParseDate = SimpleDateFormat("yyyy-MM-dd").parse(currentDate)
                    val selectedTime = SimpleDateFormat("HH").parse(Time_from)
                    val selectedToTime  = SimpleDateFormat("HH").parse(Time_to)
                    val currentParseTime = SimpleDateFormat("HH").parse(currentTime)

                    if (selectedDate == currentParseDate) {
                        if (selectedTime < currentParseTime) {
                            isNotValidTime = true
                        }
                    }

                    if(selectedToTime < selectedTime){
                        isNotValidTime = true
                    }


                    Req_repeat = if (scheduleDaysAdapter.getSelectedDays().size > 0) {
                        1
                    } else {
                        0
                    }
                    var selectedDays = scheduleDaysAdapter.getSelectedDays().toString()
                    selectedDays = selectedDays.replace("[", "{")
                    selectedDays = selectedDays.replace("]", "}")
                    Repeat_days = selectedDays
                }


                if (!isNotValidTime) {
                    sendAllowCheckInRequest(Mem_id, Mem_cus_id, App_usr_id, Mob_no, Mem_site_id, Req_type, Req_mode, Visit_date, Time_from, Time_to,
                            Req_repeat, Repeat_days, Repeat_upto, Accept_status, Purpose, Is_fav)
                } else {
                    Global.displayToastMessage(getString(R.string.message_valid_time_zone), applicationContext)
                }

            }
        })
    }

    private fun sendAllowCheckInRequest(memId: Int, memCusId: Int, appUsrId: Int, mobNo: String, memSiteId: Int, reqType: Int, reqMode: Int, visitDate: String, timeFrom: String, timeTo: String, reqRepeat: Int,
                                        repeatDays: String, repeatUpto: String, acceptStatus: Int, purpose: String, isFav: Int) {
        val jsonRequest = JsonObject()
        jsonRequest.addProperty("Mem_id", memId)
        jsonRequest.addProperty("Mem_cus_id", memCusId)
        jsonRequest.addProperty("App_usr_id", appUsrId)
        jsonRequest.addProperty("Mob_no", mobNo)
        jsonRequest.addProperty("Mem_site_id", memSiteId)
        jsonRequest.addProperty("Req_type", reqType)
        jsonRequest.addProperty("Req_mode", reqMode)
        jsonRequest.addProperty("Visit_date", visitDate)
        jsonRequest.addProperty("Time_from", timeFrom)
        jsonRequest.addProperty("Time_to", timeTo)
        jsonRequest.addProperty("Req_repeat", reqRepeat)
        jsonRequest.addProperty("Repeat_days", repeatDays)
        jsonRequest.addProperty("Repeat_upto", repeatUpto)
        jsonRequest.addProperty("Accept_status", acceptStatus)
        jsonRequest.addProperty("Purpose", purpose)
        jsonRequest.addProperty("Is_fav", isFav)

        Log.e("json", jsonRequest.toString())

        bar?.show()
        val responseBodyCall: Call<ResponseBody>? = adminAPI?.SendAllowCheckInRequest(jsonRequest)
        responseBodyCall?.enqueue(object : Callback<ResponseBody?> {
            override fun onResponse(call: Call<ResponseBody?>, response: Response<ResponseBody?>) {
                when {
                    response.code() == 200 -> {
                        Global.displayToastMessage("Request Submitted successfully", applicationContext)
                        val intent = Intent(applicationContext, DashboardActivity::class.java)
                        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                        startActivity(intent)
                        finish()
                    }
                    response.code() == 400 -> {
                        bar?.hide()
                        Global.displayToastMessage(getString(R.string.message_mobile_no_already_exist), applicationContext)
                    }
                    else -> {
                        bar?.hide()
                        Global.displayToastMessage(getString(R.string.message_something_went_wrong), applicationContext)
                    }
                }
            }

            override fun onFailure(call: Call<ResponseBody?>, t: Throwable) {
                bar?.hide()
                Global.displayToastMessage(getString(R.string.message_something_went_wrong), applicationContext)
            }
        })
    }

    private fun setupToolbar() {
        toolbar = findViewById<View>(R.id.toolbarsignup) as Toolbar
        txt_title = findViewById<View>(R.id.txt_title) as TextView
        setSupportActionBar(toolbar)
        txt_title!!.text = "Guest Details"
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportActionBar!!.setHomeButtonEnabled(true)
        toolbar!!.setNavigationIcon(R.drawable.back_1)
        toolbar!!.setNavigationOnClickListener { onBackPressed() }
    }


    private fun getRFLocationList() {
        val responseBodyCall: Call<CheckInUserInfoModel>? = adminAPI?.GetCheckedInUserInfo(userData?.app_usr_mobile!!, 1)
        responseBodyCall?.enqueue(object : Callback<CheckInUserInfoModel?> {
            override fun onResponse(call: Call<CheckInUserInfoModel?>, response: Response<CheckInUserInfoModel?>) {
                if (response.code() == 200) {
                    try {
                        val userDataModel: CheckInUserInfoModel? = response.body()
                        userDataModel?.appUser?.memberDetails?.forEachIndexed { index, memberDetail ->
                            memberDetail.memberSites.forEach {
                                rfLocationList.add(it)
                            }
                        }
                        if (rfLocationList.size > 0) {
                            rfLocationList.forEachIndexed { index, rfLocationModel ->
                                member.add(rfLocationModel.MemSiteTitle)
                            }

                            val spinnerArrayAdapter = ArrayAdapter(this@GuestDetailActivity, android.R.layout.simple_spinner_item, member)
                            spinnerArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item) // The drop down view
                            spinner2.adapter = spinnerArrayAdapter;


                        }
                    } catch (e: Exception) {
                        Log.e("exception", e.message)
                    }

                } else {
                    Global.displayToastMessage(getString(R.string.message_something_went_wrong), applicationContext)
                }
            }

            override fun onFailure(call: Call<CheckInUserInfoModel?>, t: Throwable) {
                Global.displayToastMessage(getString(R.string.message_something_went_wrong), applicationContext)
            }
        })


    }


}