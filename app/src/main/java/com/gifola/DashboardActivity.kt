package com.gifola

import android.Manifest
import android.annotation.TargetApi
import android.app.Activity
import android.app.Dialog
import android.app.ProgressDialog
import android.bluetooth.BluetoothAdapter
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.AsyncTask
import android.os.Build
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.util.Log
import android.view.*
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.view.ContextThemeWrapper
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityCompat
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.Target
import com.gifola.adapter.DashboardFrequentVisitorListAdapter
import com.gifola.adapter.DashboardMainListAdapter
import com.gifola.adapter.FavoritePlacesListAdapter
import com.gifola.apis.AdminAPI
import com.gifola.apis.ApiURLs
import com.gifola.apis.SeriveGenerator
import com.gifola.apis.ServiceHandler
import com.gifola.constans.Global
import com.gifola.constans.Global.displayToastMessage
import com.gifola.constans.Global.getUserMe
import com.gifola.constans.Global.isEnteredFirstTime
import com.gifola.constans.Global.setUserMe
import com.gifola.constans.SharedPreferenceHelper
import com.gifola.customfonts.MyTextViewBold
import com.gifola.customfonts.MyTextViewMedium
import com.gifola.customfonts.MyTextViewRegular
import com.gifola.model.*
import com.google.android.material.navigation.NavigationView
import com.google.gson.Gson
import com.google.gson.JsonObject
import de.hdodenhof.circleimageview.CircleImageView
import kotlinx.android.synthetic.main.activity_dashboard.*
import kotlinx.android.synthetic.main.inside_main_activity.*
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class DashboardActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    companion object {
        var perms = arrayOf(
                Manifest.permission.READ_CALL_LOG,
                Manifest.permission.WRITE_CALL_LOG,
                Manifest.permission.READ_CONTACTS
        )
    }

    var navigationView: NavigationView? = null
    var myDrawerLayout: DrawerLayout? = null
    var toolbar: Toolbar? = null
    var clicklayout: LinearLayout? = null
    var dialog: Dialog? = null
    var dialog2: Dialog? = null
    var appPreference: SharedPreferenceHelper? = null
    var adminAPI: AdminAPI? = null
    var mBluetoothAdapter: BluetoothAdapter? = null
    var isMember = true
    var bar: ProgressDialog? = null
    lateinit var dashboardMainListAdapter: DashboardMainListAdapter
    lateinit var visitorListAdapter: DashboardFrequentVisitorListAdapter
    lateinit var favoritePlacesListAdapter: FavoritePlacesListAdapter
    var dashboardListData: ArrayList<DashboardListData> = ArrayList()
    var checkInList: ArrayList<CheckInListModelItem> = ArrayList()
    var favplaceList: ArrayList<FavLocationModelItem> = ArrayList()
    val tempFavPlaceList: ArrayList<FavLocationModelItem> = ArrayList()
    var totalListItems : Int = 1
    var isPaginationRequired : Boolean = true
    var pastVisibleItems: Int = -1
    var visibleItemCount: Int = -1
    var lastListReceiveReqId : Int = 0
    var lastListSentReqId : Int = 0
    var lastListAllowVisitorReqId : Int = 0
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)
        appPreference = SharedPreferenceHelper(applicationContext)
        bar = ProgressDialog(this)
        navigationView = findViewById<View>(R.id.nav_drawer) as NavigationView
        navigationView!!.setNavigationItemSelectedListener(this)
        myDrawerLayout = findViewById<View>(R.id.drawerLayout) as DrawerLayout
        adminAPI = SeriveGenerator.getAPIClass()
        toolbar = findViewById<View>(R.id.toolbar) as Toolbar
        val mobileNo = getUserMe(appPreference!!)!!.app_usr_mobile
        CheckUserMobileNumber(mobileNo!!).execute()
        setSupportActionBar(toolbar)

        val m = navigationView!!.menu
        for (i in 0 until m.size()) {
            val mi = m.getItem(i)

            //for aapplying a font to subMenu ...
            val subMenu = mi.subMenu
            if (subMenu != null && subMenu.size() > 0) {
                for (j in 0 until subMenu.size()) {
                    val subMenuItem = subMenu.getItem(j)
                    applyFontToMenuItem(subMenuItem)
                }
            }
            //the method we have create in activity
            applyFontToMenuItem(mi)
        }

        visitorListAdapter = object : DashboardFrequentVisitorListAdapter(this, checkInList) {
            override fun selectedFrequentUser(number: String, memName: String) {
                var selectedNumber = number.replace("(", "")
                selectedNumber = number.replace(")", "")
                selectedNumber = number.replace("-", "")
                selectedNumber = number.replace(" ", "")
                selectedNumber = number.replace("+91", "")

                if (selectedNumber.isNotEmpty() && selectedNumber != mobileNo)
                    getUserInfo(selectedNumber, memName)
            }

        }
        val layoutManager = LinearLayoutManager(this@DashboardActivity, LinearLayoutManager.HORIZONTAL, false)
        rv_visitor_dashboard.layoutManager = layoutManager
        rv_visitor_dashboard.adapter = visitorListAdapter

        favoritePlacesListAdapter = object : FavoritePlacesListAdapter(this, favplaceList) {
            override fun frequentList(number: String, memName: String) {
                var selectedNumber = number.replace("(", "")
                selectedNumber = number.replace(")", "")
                selectedNumber = number.replace("-", "")
                selectedNumber = number.replace(" ", "")
                selectedNumber = number.replace("+91", "")

                if (selectedNumber.isNotEmpty() && selectedNumber != mobileNo)
                    getUserInfoForRequest(selectedNumber, memName, 0)
            }

        }
        val placeLayoutManager = LinearLayoutManager(this@DashboardActivity, LinearLayoutManager.HORIZONTAL, false)
        rv_favorite_places.layoutManager = placeLayoutManager
        rv_favorite_places.adapter = favoritePlacesListAdapter



        qrscanimg.setOnClickListener(View.OnClickListener {
            mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
            if (mBluetoothAdapter!!.isEnabled()) {
                val intent = Intent(applicationContext, QRScanActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                startActivity(intent)
            } else {
                val turnBTon = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                startActivityForResult(turnBTon, 1)
            }
        })
        sendper.setOnClickListener(View.OnClickListener {
            if (ActivityCompat.checkSelfPermission(
                            this,
                            Manifest.permission.READ_CALL_LOG
                    ) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                            this,
                            Manifest.permission.WRITE_CALL_LOG
                    ) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                            this,
                            Manifest.permission.READ_CONTACTS
                    ) == PackageManager.PERMISSION_GRANTED
            ) {
                val intent = Intent(applicationContext, SendPermissionActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                startActivity(intent)
            } else {
                isPermissionRequestRequired(this, perms, 1)
            }

        })
        requestper.setOnClickListener(View.OnClickListener {
            if (ActivityCompat.checkSelfPermission(
                            this,
                            Manifest.permission.READ_CALL_LOG
                    ) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                            this,
                            Manifest.permission.WRITE_CALL_LOG
                    ) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                            this,
                            Manifest.permission.READ_CONTACTS
                    ) == PackageManager.PERMISSION_GRANTED
            ) {
                val intent = Intent(applicationContext, RequestPermissionActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                startActivity(intent)
            } else {
                isPermissionRequestRequired(this, perms, 1)
            }

        })

        dashboardMainListAdapter = object : DashboardMainListAdapter(this@DashboardActivity, dashboardListData) {
            override fun updateRequestStatus(reqId: Int, status: Int) {
                updateRequest(reqId, status)
            }
        }
        val dashBoardLayoutManager = LinearLayoutManager(this@DashboardActivity, LinearLayoutManager.VERTICAL, false)
        rv_main_dashboard.layoutManager = dashBoardLayoutManager
        rv_main_dashboard.adapter = dashboardMainListAdapter

        rv_main_dashboard.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                if (dy > 0) //check for scroll down
                {
                    visibleItemCount = dashBoardLayoutManager.childCount;
                    totalListItems = dashBoardLayoutManager.itemCount;
                    pastVisibleItems = dashBoardLayoutManager.findFirstVisibleItemPosition();

                    if ((visibleItemCount + pastVisibleItems) >= totalListItems) {
                        getDashboardList(getUserMe(appPreference!!)?.app_usr_id ?: 0, mobileNo, lastListReceiveReqId, lastListSentReqId, lastListAllowVisitorReqId, false)
                    }
                }
            }
        })

        mainListSRL.setOnRefreshListener {
            getDashboardList(getUserMe(appPreference!!)?.app_usr_id ?: 0, mobileNo, 0, 0, 0, true)
        }

        val headerView = LayoutInflater.from(this).inflate(R.layout.navigation_header, navigationView, false)
        val editprofile = headerView.findViewById<View>(R.id.editprofile) as MyTextViewMedium
        val userName = headerView.findViewById<View>(R.id.txt_user_name) as MyTextViewBold
        val userProfile = headerView.findViewById<View>(R.id.profile_image) as CircleImageView
        userName.text = getUserMe(appPreference!!)!!.app_usr_name
        var imagePath = getUserMe(appPreference!!)!!.app_pic
        if (imagePath != null || imagePath != "null" || imagePath == "") {
            imagePath = ApiURLs.IMAGE_URL + imagePath
            Glide.with(this)
                    .load(imagePath)
                    .apply(RequestOptions.errorOf(R.drawable.user_placeholder))
                    .addListener(object : RequestListener<Drawable?> {
                        override fun onLoadFailed(e: GlideException?, model: Any, target: Target<Drawable?>, isFirstResource: Boolean): Boolean {
                            return false
                        }

                        override fun onResourceReady(resource: Drawable?, model: Any, target: Target<Drawable?>, dataSource: DataSource, isFirstResource: Boolean): Boolean {
                            userProfile.setImageDrawable(resource)
                            return true
                        }
                    }).submit()
        }
        editprofile.setOnClickListener {
            val intent = Intent(applicationContext, MyProfileActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            startActivity(intent)
        }
        navigationView!!.addHeaderView(headerView)
        val mDrawerToggle: ActionBarDrawerToggle = object : ActionBarDrawerToggle(this, myDrawerLayout, toolbar, R.string.app_name,
                R.string.app_name) {
            override fun onDrawerSlide(drawerView: View, slideOffset: Float) {
                super.onDrawerSlide(drawerView, slideOffset)
                val moveFactor = navigationView!!.width * slideOffset
                containerView!!.translationX = slideOffset * drawerView.width
                myDrawerLayout!!.bringChildToFront(drawerView)
                myDrawerLayout!!.requestLayout()
                //below line used to remove shadow of drawer
                myDrawerLayout!!.setScrimColor(Color.TRANSPARENT)
            }
        }
        myDrawerLayout!!.setDrawerListener(mDrawerToggle)
        mDrawerToggle.syncState()
    }

    internal inner class CheckUserMobileNumber(mobileNo: String) : AsyncTask<Void?, Void?, String?>() {
        var mobileNo = ""
        override fun onPreExecute() {
            super.onPreExecute()
        }


        override fun onPostExecute(result: String?) {
            super.onPostExecute(result)
            try {
                if (result == null || result.isEmpty()) {
                    displayToastMessage(getString(R.string.message_something_went_wrong), applicationContext)
                } else {
                    val jsonResponse: String = result
                    if (!jsonResponse.startsWith("{")) {
                        val gson = Gson()
                        val dataModel = gson.fromJson(jsonResponse, UserDataModel::class.java)
                        val userData = dataModel[0]
                        setUserMe(userData, appPreference!!)
                        appPreference!!.setBoolean(isEnteredFirstTime, false)
                        val nav_Menu = navigationView!!.menu
                        if (userData.isOnlyAppUser == true) {
                            isMember = false
                            //nav_Menu.findItem(R.id.addmember).isVisible = false
                        }

                        getFavoritePlaces(userData.app_usr_id ?: 0, mobileNo)
                        getDashboardList(userData.app_usr_id ?: 0, mobileNo, 0, 0, 0, true)
                    }
                }
            } catch (e: Exception) {
                Log.e("exception", e.message)
            }
        }

        init {
            this.mobileNo = mobileNo
        }

        override fun doInBackground(vararg params: Void?): String? {
            var json: String? = ""
            try {
                val postURL = ApiURLs.CHECK_USERS_MOBILE_NO + mobileNo
                json = ServiceHandler().makeServiceCall(ApiURLs.BASE_URL + postURL, ServiceHandler.GET)
            } catch (e: Exception) {
                Log.e("exception", e.message)
            }
            return json
        }
    }


    @TargetApi(Build.VERSION_CODES.M)
    fun isPermissionRequestRequired(activity: Activity, permissions: Array<String>, requestCode: Int): Boolean {
        if (isMarshmallowPlusDevice() && permissions.size > 0) {
            val newPermissionList = java.util.ArrayList<String>()
            for (permission in permissions) {
                if (PackageManager.PERMISSION_GRANTED != activity.checkSelfPermission(permission)) {
                    newPermissionList.add(permission)
                }
            }
            if (newPermissionList.size > 0) {
                activity.requestPermissions(newPermissionList.toTypedArray(), requestCode)
                return true
            }
        }

        return false
    }

    private fun isMarshmallowPlusDevice(): Boolean {
        return Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP_MR1
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        val id = item.itemId
        if (id == R.id.mylocation) {
            val intent = Intent(applicationContext, MyLocationsActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            startActivity(intent)
        } else if (id == R.id.addmember) {
            val intent = Intent(applicationContext, AddSubMemberActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            startActivity(intent)
        } else if (id == R.id.addrfcard) {
            val intent = Intent(applicationContext, AddRFCardActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            startActivity(intent)
        } else if (id == R.id.adduhfcard) {
            val intent = Intent(applicationContext, AddUHFCardActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            startActivity(intent)
        } else if (id == R.id.mylogs) {
            val intent = Intent(applicationContext, MyLogsActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            startActivity(intent)
        } else if (id == R.id.privacysettings) {
            val intent = Intent(applicationContext, PrivacyPolicyActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            startActivity(intent)
        } else if (id == R.id.contatcus) {
            val intent = Intent(applicationContext, ContactUsActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            startActivity(intent)
        } else if (id == R.id.rateus) {
        } else if (id == R.id.disclaimer) {
            val termsIntent = Intent(Intent.ACTION_VIEW)
            termsIntent.data = Uri.parse(Global.disclaimer_us_url)
            startActivity(termsIntent)
        } else if (id == R.id.terms) {
            val termsIntent = Intent(Intent.ACTION_VIEW)
            termsIntent.data = Uri.parse(Global.terms_and_conditions_url)
            startActivity(termsIntent)
        } else if (id == R.id.privacypolicy) {
            val termsIntent = Intent(Intent.ACTION_VIEW)
            termsIntent.data = Uri.parse(Global.privacy_policy_url)
            startActivity(termsIntent)
        } else if (id == R.id.aboutus) {
            val termsIntent = Intent(Intent.ACTION_VIEW)
            termsIntent.data = Uri.parse(Global.about_us_url)
            startActivity(termsIntent)
        } else if (id == R.id.logout) {
            LogoutDialoge()
        } else if (id == R.id.faq) {
            val intent = Intent(applicationContext, FAQActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            startActivity(intent)
        }
        val drawer = findViewById<View>(R.id.drawerLayout) as DrawerLayout
        drawer.closeDrawer(GravityCompat.START)
        return true
    }

    override fun onPointerCaptureChanged(hasCapture: Boolean) {}
    private fun applyFontToMenuItem(mi: MenuItem) {
        val font = Typeface.createFromAsset(assets, "fonts/Quicksand-Medium.ttf")
        val mNewTitle = SpannableString(mi.title)
        mNewTitle.setSpan(CustomTypefaceSpan("", font), 0, mNewTitle.length, Spannable.SPAN_INCLUSIVE_INCLUSIVE)
        mi.title = mNewTitle
    }

    fun LogoutDialoge() {
        dialog = Dialog(ContextThemeWrapper(this, R.style.AppTheme))
        dialog!!.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog!!.setCancelable(true)
        dialog!!.setContentView(R.layout.dialoge_logout)
        dialog!!.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog!!.window!!.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)

//        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
//        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        val submit = dialog!!.findViewById<View>(R.id.submit) as MyTextViewRegular
        val img_no = dialog!!.findViewById<View>(R.id.img_no) as ImageView
        submit.setOnClickListener {
            appPreference!!.clearAll()
            dialog!!.dismiss()
            val intent = Intent(applicationContext, SignupActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            startActivity(intent)
            finish()
        }
        img_no.setOnClickListener { dialog!!.dismiss() }
        dialog!!.show()
    }

    private fun getDashboardList(userId: Int, mobileNum: String, lastReceiveReqId: Int, lastSentReqId: Int, lastAllowVisitorReqId: Int, isFirstTime: Boolean) {
        mainListSRL.isRefreshing = true
        if(isFirstTime){
            dashboardListData.clear()
        }
        val jsonRequest = JsonObject();
        jsonRequest.addProperty("AppUserId", userId)
        jsonRequest.addProperty("MobileNo", mobileNum)
        jsonRequest.addProperty("LastReceiveRequestId", lastReceiveReqId)
        jsonRequest.addProperty("LatSentRequestId", lastSentReqId)
        jsonRequest.addProperty("LastAllowVisitorId", lastAllowVisitorReqId)
        val responseBodyCall: Call<DashboardMainListModel>? = adminAPI?.GetDashboardMainList(jsonRequest)
        responseBodyCall?.enqueue(object : Callback<DashboardMainListModel?> {
            override fun onResponse(call: Call<DashboardMainListModel?>, response: Response<DashboardMainListModel?>) {
                mainListSRL.isRefreshing = false
                var apiResponse = response.body()
                if (apiResponse?.AllowVisitor != null && apiResponse.AllowVisitor.isNotEmpty()) {
                    dashboardListData.addAll(apiResponse.AllowVisitor)
                    lastListAllowVisitorReqId = apiResponse.AllowVisitor.last().RequestID
                }

                if (apiResponse?.ReceiveRequest != null && apiResponse.ReceiveRequest.isNotEmpty()) {
                    apiResponse.ReceiveRequest.forEach {
                        it.isReceieveRequest = true
                    }
                    dashboardListData.addAll(apiResponse.ReceiveRequest)
                    lastListReceiveReqId = apiResponse.ReceiveRequest.last().RequestID
                }

                if (apiResponse?.SentRequest != null && apiResponse.SentRequest.isNotEmpty()) {
                    dashboardListData.addAll(apiResponse.SentRequest)
                    lastListSentReqId = apiResponse.SentRequest.last().RequestID
                }

                if((apiResponse?.AllowVisitor == null || apiResponse.AllowVisitor.isEmpty()) && (apiResponse?.ReceiveRequest == null || apiResponse.ReceiveRequest.isEmpty()) && (apiResponse?.SentRequest == null || apiResponse.SentRequest.isEmpty())){
                    isPaginationRequired = false
                }


                if (dashboardListData.size > 0) {
                    totalListItems = dashboardListData.size
                    dashboardMainListAdapter.notifyDataSetChanged()
                    rv_main_dashboard.visibility = View.VISIBLE
                    txt_no_data_found.visibility = View.GONE

                } else {
                    rv_main_dashboard.visibility = View.GONE
                    txt_no_data_found.visibility = View.VISIBLE
                    isPaginationRequired = false
                }

                allowPermissionIfNeeded()
            }

            override fun onFailure(call: Call<DashboardMainListModel?>, t: Throwable) {
                mainListSRL.isRefreshing = false
                txt_no_data_found.visibility = View.VISIBLE
                allowPermissionIfNeeded()
            }
        })

        if(isFirstTime){
            getAllowCheckInList(0, "0", userId, mobileNum)
        }



    }

    private fun allowPermissionIfNeeded() {
        if (ActivityCompat.checkSelfPermission(
                        this,
                        Manifest.permission.CAMERA
                ) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                        this,
                        Manifest.permission.READ_EXTERNAL_STORAGE
                ) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                        this,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                ) == PackageManager.PERMISSION_GRANTED
        ) {
            // Do nothing
        } else {
            isPermissionRequestRequired(this, perms, 1)
        }
    }

    private fun updateRequest(reqId: Int, status: Int) {
        val jsonRequest = JsonObject();
        jsonRequest.addProperty("RequestID", reqId)
        jsonRequest.addProperty("AcceptStatus", status)
        bar?.show()
        val responseBodyCall: Call<ResponseBody>? = adminAPI?.UpdateRequestStatus(jsonRequest)
        responseBodyCall?.enqueue(object : Callback<ResponseBody?> {
            override fun onResponse(call: Call<ResponseBody?>, response: Response<ResponseBody?>) {
                bar?.hide()
            }

            override fun onFailure(call: Call<ResponseBody?>, t: Throwable) {
                bar?.hide()
            }
        })
    }

    private fun getAllowCheckInList(type: Int, lastId: String, userId: Int, mobileNum: String) {
        val jsonRequest = JsonObject();
        jsonRequest.addProperty("app_usr_id", userId)
        jsonRequest.addProperty("Mob_no", mobileNum)
        jsonRequest.addProperty("Req_type", 0)
        jsonRequest.addProperty("Type", type)
        jsonRequest.addProperty("LastId", lastId)
        val responseBodyCall: Call<CheckInListModel>? = adminAPI?.GetCheckInList(jsonRequest)
        responseBodyCall?.enqueue(object : Callback<CheckInListModel?> {
            override fun onResponse(call: Call<CheckInListModel?>, response: Response<CheckInListModel?>) {
                checkInList.clear()
                if (response.code() == 200) {
                    val checkInListResponse = response.body()
                    checkInListResponse?.let { checkInList.addAll(it) }
                }
                getRequestCheckInList(type, lastId, userId, mobileNum)
            }

            override fun onFailure(call: Call<CheckInListModel?>, t: Throwable) {
                getRequestCheckInList(type, lastId, userId, mobileNum)
            }
        })


    }

    private fun getRequestCheckInList(type: Int, lastId: String, userId: Int, mobileNum: String) {
        val jsonRequest = JsonObject();
        jsonRequest.addProperty("app_usr_id", userId)
        jsonRequest.addProperty("Mob_no", mobileNum)
        jsonRequest.addProperty("Req_type", 1)
        jsonRequest.addProperty("Type", type)
        jsonRequest.addProperty("LastId", lastId)
        Log.e("request", jsonRequest.toString())
        val responseBodyCall: Call<CheckInListModel>? = adminAPI?.GetCheckInList(jsonRequest)
        responseBodyCall?.enqueue(object : Callback<CheckInListModel?> {
            override fun onResponse(call: Call<CheckInListModel?>, response: Response<CheckInListModel?>) {
                if (response.code() == 200) {
                    val checkInListResponse = response.body()
                    checkInList.forEach { allowedList ->
                        checkInListResponse.let {
                            it?.forEach { response ->
                                if (response.MobileNo == allowedList.MobileNo)
                                    checkInList.remove(allowedList)
                            }
                        }
                    }
                    checkInListResponse?.let { checkInList.addAll(it) }
                }
                showVisitorList()
            }

            override fun onFailure(call: Call<CheckInListModel?>, t: Throwable) {
                showVisitorList()
            }
        })
    }

    private fun getFavoritePlaces(userId: Int, mobileNum: String) {
        val jsonRequest = JsonObject();
        jsonRequest.addProperty("AppUserId", userId)
        jsonRequest.addProperty("MobileNo", mobileNum)
        Log.e("request", jsonRequest.toString())
        val responseBodyCall: Call<FavLocationModel>? = adminAPI?.GetAllFavoritePlace(jsonRequest)
        responseBodyCall?.enqueue(object : Callback<FavLocationModel?> {
            override fun onResponse(call: Call<FavLocationModel?>, response: Response<FavLocationModel?>) {
                if (response.code() == 200) {
                    val responseData = response.body()
                    val mobileNoList : ArrayList<String> = ArrayList()
                    responseData?.let {
                        it.forEach {
                            if(!mobileNoList.contains(it.MobileNo)){
                                favplaceList.add(it)
                                mobileNoList.add(it.MobileNo)
                            }
                        }
                    }


                    if (favplaceList.size > 0) {
                        rv_favorite_places.visibility = View.VISIBLE
                        favoritePlacesListAdapter.notifyDataSetChanged()
                    } else {
                        rv_favorite_places.visibility = View.GONE
                    }
                }


            }

            override fun onFailure(call: Call<FavLocationModel?>, t: Throwable) {
                rv_favorite_places.visibility = View.GONE
            }
        })
    }


    fun showVisitorList() {
        if (checkInList.size > 0) {
            rv_visitor_dashboard.visibility = View.VISIBLE
            visitorListAdapter.notifyDataSetChanged()
        } else {
            rv_visitor_dashboard.visibility = View.GONE
        }
    }

    private fun getUserInfo(number: String, name: String) {
        val responseBodyCall: Call<CheckInUserInfoModel>? = adminAPI?.GetCheckedInUserInfo(number, 1)
        Log.e("apiURL", responseBodyCall?.request()?.url().toString())
        responseBodyCall?.enqueue(object : Callback<CheckInUserInfoModel?> {
            override fun onResponse(call: Call<CheckInUserInfoModel?>, response: Response<CheckInUserInfoModel?>) {
                if (response.code() == 200) {
                    val userDataModel: CheckInUserInfoModel? = response.body()
                    if (userDataModel?.Status == 0) {
                        userDataModel.appUser = AppUser()
                        userDataModel.appUser.MobileNo = number
                        userDataModel.appUser.AppUserName = name
                    }
                    val userData = Gson().toJson(userDataModel)
                    val intent = Intent(applicationContext, GuestDetailActivity::class.java)
                    intent.putExtra(Global.checkedInUserInfo, userData)
                    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                    startActivity(intent)
                } else {
                    txt_no_data_found.visibility = View.VISIBLE
                }
            }

            override fun onFailure(call: Call<CheckInUserInfoModel?>, t: Throwable) {
                txt_no_data_found.visibility = View.VISIBLE
            }
        })
    }


    private fun getUserInfoForRequest(number: String, name: String, favorite: Int) {
        bar?.hide()
        val responseBodyCall: Call<CheckInUserInfoModel>? = adminAPI?.GetCheckedInUserInfo(number, 0)
        responseBodyCall?.enqueue(object : Callback<CheckInUserInfoModel?> {
            override fun onResponse(call: Call<CheckInUserInfoModel?>, response: Response<CheckInUserInfoModel?>) {
                bar?.hide()
                if (response.code() == 200) {
                    val userDataModel: CheckInUserInfoModel? = response.body()
                    Log.e("requestResponse ", Gson().toJson(userDataModel))
                    if (userDataModel?.Status == 0) {
                        displayToastMessage(getString(R.string.message_selected_not_a_member), applicationContext)
                    } else {
                        val userData = Gson().toJson(userDataModel)
                        val intent = Intent(applicationContext, HostDetailActivity::class.java)
                        intent.putExtra(Global.checkedInUserInfo, userData)
                        intent.putExtra(Global.keyIsFromFavourite, favorite)
                        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                        startActivity(intent)
                    }

                }
            }

            override fun onFailure(call: Call<CheckInUserInfoModel?>, t: Throwable) {
                bar?.hide()
            }
        })
    }
}