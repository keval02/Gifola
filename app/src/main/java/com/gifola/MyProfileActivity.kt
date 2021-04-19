package com.gifola

import android.Manifest
import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.app.*
import android.app.DatePickerDialog.OnDateSetListener
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.util.Patterns
import android.widget.DatePicker
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.FileProvider
import androidx.fragment.app.DialogFragment
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.Target
import com.gifola.apis.AdminAPI
import com.gifola.apis.ApiURLs.IMAGE_URL
import com.gifola.apis.SeriveGenerator
import com.gifola.constans.Global
import com.gifola.constans.Global.displayToastMessage
import com.gifola.constans.Global.getUserMe
import com.gifola.constans.Global.setUserMe
import com.gifola.constans.SharedPreferenceHelper
import com.gifola.customfonts.MyEditText
import com.gifola.helper.ImageFileFilter
import com.gifola.helper.ImagePath
import com.gifola.model.UserData
import com.google.gson.Gson
import kotlinx.android.synthetic.main.activity_my_profile.*
import kotlinx.android.synthetic.main.custom_toolbar_back.*
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class MyProfileActivity : AppCompatActivity() {
    companion object {
        private const val REQUEST_CAMERA = 0
        var perms = arrayOf(
                Manifest.permission.CAMERA,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE
        )
        val SELECT_PICTURE = 1
        val SELECT_PICTURE_KITKAT = 2

    }

    var imagePath = ""
    var userChoosenTask = ""
    var mobileNo: String? = ""
    var name: String? = ""
    var organizationName: String? = ""
    var designationName: String? = ""
    var emailId: String? = ""
    var workAddress: String? = ""
    var homeAddress: String? = ""
    var dob: String? = ""
    var preferenceHelper: SharedPreferenceHelper? = null
    var userData: UserData? = null
    var adminAPI: AdminAPI? = null
    var progressDialog: ProgressDialog? = null
    var selectedDate : String = ""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_profile)
        preferenceHelper = SharedPreferenceHelper(applicationContext)
        adminAPI = SeriveGenerator.getAPIClass()
        progressDialog = ProgressDialog(this)
        setupToolbar()

        try {
            userData = getUserMe(preferenceHelper!!)
        } catch (e: Exception) {
            Log.e("exception", e.message)
        }
        if (userData!!.app_usr_mobile != null && userData!!.app_usr_mobile != "") {
            mobileNo = userData!!.app_usr_mobile
        }
        if (userData!!.app_usr_name != null && userData!!.app_usr_name != "") {
            name = userData!!.app_usr_name
        }
        if (userData!!.app_usr_Organization != null && userData!!.app_usr_Organization != "") {
            organizationName = userData!!.app_usr_Organization
        }
        if (userData!!.app_usr_designation != null && userData!!.app_usr_designation != "") {
            designationName = userData!!.app_usr_designation
        }
        if (userData!!.app_usr_email != null && userData!!.app_usr_email != "") {
            emailId = userData!!.app_usr_email
        }
        if (userData!!.app_usr_work_address != null && userData!!.app_usr_work_address != "") {
            workAddress = userData!!.app_usr_work_address
        }
        if (userData!!.app_usr_home_address != null && userData!!.app_usr_home_address != "") {
            homeAddress = userData!!.app_usr_home_address
        }
        if (userData!!.app_usr_dob != null && userData!!.app_usr_dob != "") {
            dob = userData!!.app_usr_dob
            val splitDob = dob?.split("T")
            val getDateFromDOB = splitDob?.get(0)
            dob = getDateFromDOB

        }

        if (userData!!.app_pic != null && userData!!.app_pic != "") {
            imagePath = IMAGE_URL + userData?.app_pic
            Glide.with(this)
                    .load(imagePath)
                    .apply(
                            RequestOptions().placeholder(R.drawable.user_placeholder).error(
                                    R.drawable.user_placeholder
                            )
                    )
                    .listener(object : RequestListener<Drawable> {
                        override fun onLoadFailed(e: GlideException?, model: Any?, target: Target<Drawable>?, isFirstResource: Boolean): Boolean {
                            Log.e("excep", "" + e?.message)
                            return false
                        }

                        override fun onResourceReady(resource: Drawable?, model: Any?, target: Target<Drawable>?, dataSource: DataSource?, isFirstResource: Boolean): Boolean {
                            profile_image.setImageDrawable(resource)
                            return true
                        }


                    }).submit()

        }

        mobile_no_tv.text = "+91 $mobileNo"
        name_edt.setText(name)
        organization_edt.setText(organizationName)
        designation_edt.setText(designationName)
        email_edt.setText(emailId)
        work_address_edt.setText(workAddress)
        home_address_edt.setText(homeAddress)
        dob_edt.setText(dob)


        profilephoto.setOnClickListener {
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
                selectImage()
            } else {
                isPermissionRequestRequired(this, perms, 1)
            }
        }
        dob_edt.setOnClickListener {
            val newFragment: DialogFragment = DatePickerFragment(dob_edt)
            newFragment.show(supportFragmentManager, "datePicker")
        }
        backcard.setOnClickListener({ onBackPressed() })
        sendcard.setOnClickListener {
            val name = name_edt.getText().toString().trim { it <= ' ' }
            val enteredOrg = organization_edt.text.toString().trim { it <= ' ' }
            val enteredDesi = designation_edt.text.toString().trim { it <= ' ' }
            val enteredEmail = email_edt.text.toString().trim { it <= ' ' }
            val enteredWorkAddress = work_address_edt.text.toString().trim { it <= ' ' }
            val enteredHomeAddress = home_address_edt.text.toString().trim { it <= ' ' }
            val enteredDOB = dob_edt.text.toString().trim { it <= ' ' }
            if (enteredEmail.isNotEmpty() && !Patterns.EMAIL_ADDRESS.matcher(enteredEmail).matches()) {
                displayToastMessage(getString(R.string.message_valid_email_id), applicationContext)
            } else {
                userData!!.app_usr_name = name
                userData!!.app_usr_Organization = enteredOrg
                userData!!.app_usr_designation = enteredDesi
                userData!!.app_usr_email = enteredEmail
                userData!!.app_usr_work_address = enteredWorkAddress
                userData!!.app_usr_home_address = enteredHomeAddress
                userData!!.app_usr_dob = enteredDOB
                saveUserProfile(userData)
            }
        }
    }

    class DatePickerFragment(var editText: MyEditText?) : DialogFragment(), OnDateSetListener {
        override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
            val c = Calendar.getInstance()
            val year = c[Calendar.YEAR]
            val month = c[Calendar.MONTH]
            val day = c[Calendar.DAY_OF_MONTH]
            val dialog = DatePickerDialog(activity!!, this, year, month, day)
            dialog.datePicker.maxDate = c.timeInMillis
            return dialog
        }

        override fun onDateSet(view: DatePicker, year: Int, month: Int, day: Int) {
            val date = "$year-${month + 1}-$day"
            editText!!.setText(date)
        }

    }

    private fun saveUserProfile(userData: UserData?) {
        val file = File(imagePath)
        val requestFile = RequestBody.create(MediaType.get("multipart/form-data"), file)
        val multipartBody = MultipartBody.Part.createFormData("", file.name, requestFile)
        var responseBodyCall : Call<String>? = null
        responseBodyCall = if (imagePath.isEmpty() || imagePath.startsWith("http")) {
            adminAPI!!.UpdateUserDetailsWithImage(userData?.app_usr_id!!, userData.app_usr_Organization, userData.app_usr_designation, userData.app_usr_dob, userData.app_usr_email, userData.app_usr_home_address, userData.app_usr_mobile, userData.app_usr_name, userData.app_usr_password, userData.app_usr_reg_date, userData.app_usr_status!!, userData.app_usr_work_address, userData.isactive!!, userData.app_pic, null)
        } else {
            adminAPI!!.UpdateUserDetailsWithImage(userData?.app_usr_id!!, userData.app_usr_Organization, userData.app_usr_designation, userData.app_usr_dob, userData.app_usr_email, userData.app_usr_home_address, userData.app_usr_mobile, userData.app_usr_name, userData.app_usr_password, userData.app_usr_reg_date, userData.app_usr_status!!, userData.app_usr_work_address, userData.isactive!!, null, multipartBody)
        }
        Log.e("data", "" + responseBodyCall.request().body())
        progressDialog!!.show()
        responseBodyCall.enqueue(object : Callback<String> {
            override fun onFailure(call: Call<String>, t: Throwable) {
                progressDialog!!.hide()
                displayToastMessage(getString(R.string.message_something_went_wrong), applicationContext)
            }

            override fun onResponse(call: Call<String>, response: Response<String>) {
                progressDialog!!.hide()
                if (response.code() == 200) {
                    val responseData = response.body();
                    val gson = Gson()
                    val updatedUserData = gson.fromJson<UserData>(responseData , UserData::class.java)
                    setUserMe(updatedUserData, preferenceHelper!!)
                    val intent = Intent( applicationContext , DashboardActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                    startActivity(intent)
                    finish()
                } else {
                    displayToastMessage(getString(R.string.message_something_went_wrong), applicationContext)
                }
            }

        })
    }



    private fun setupToolbar() {
        setSupportActionBar(toolbarsignup)
        txt_title!!.text = "Edit Profile"
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportActionBar!!.setHomeButtonEnabled(true)
        toolbarsignup!!.setNavigationIcon(R.drawable.back_1)
        toolbarsignup!!.setNavigationOnClickListener { onBackPressed() }
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

    private fun selectImage() {
        val items = arrayOf<CharSequence>("Take Photo", "Choose from Gallery")
        val builder = AlertDialog.Builder(this@MyProfileActivity)
        builder.setTitle("Upload Profile Photo!")
        builder.setItems(items) { dialog, item ->
            val result = Utility.checkPermission(this@MyProfileActivity)
            if (items[item] == "Take Photo") {
                userChoosenTask = "Take Photo"
                if (result)
                    cameraIntent(this)
            } else if (items[item] == "Choose from Gallery") {
                userChoosenTask = "Choose from Gallery"
                if (result)
                    galleryIntent()
            }
        }
        builder.show()
    }

    private fun cameraIntent(context: Context) {
        takeimage(REQUEST_CAMERA, context)
    }

    @SuppressLint("ObsoleteSdkInt")
    private fun galleryIntent() {
        if (Build.VERSION.SDK_INT < 19) {
            val intent = Intent()
            intent.type = "image/*"
            intent.action = Intent.ACTION_GET_CONTENT
            intent.addCategory(Intent.CATEGORY_OPENABLE)
            startActivityForResult(Intent.createChooser(intent, "Select Picture"), SELECT_PICTURE_KITKAT)
        } else {
            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
            intent.type = "image/*"
            startActivityForResult(intent, SELECT_PICTURE)
        }
    }


    private fun takeimage(requestCode: Int, context: Context) {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        if (intent.resolveActivity(context.packageManager) != null) {
            var photoFile: File? = null
            try {
                photoFile = createImageFile()
            } catch (e: IOException) {
                e.printStackTrace()
            }

            if (photoFile != null) {
                val uri = FileProvider.getUriForFile(context, context.packageName + ".provider", photoFile)
                intent.putExtra(MediaStore.EXTRA_OUTPUT, uri)
                startActivityForResult(intent, REQUEST_CAMERA)
            } else {
                Global.displayToastMessage("Unable to access storage!", context)
            }

        } else {
            Global.displayToastMessage("Camera couldn't started!", context)
        }
    }

    @Throws(IOException::class)
    private fun createImageFile(): File {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val imageFileName = "JPEG_" + timeStamp + "_"
        val storageDir = this.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        val image = File.createTempFile(
                imageFileName, /* prefix */
                ".jpg", /* suffix */
                storageDir      /* directory */
        )

        // Save a file: path for use with ACTION_VIEW intents
        imagePath = image.absolutePath
        return image
    }

    public override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CAMERA && resultCode == Activity.RESULT_OK) {

            val originalPath = imagePath
            Log.e("imagePath", originalPath)
            if (!originalPath.isEmpty()) {
                imagePath = originalPath
                if (ImageFileFilter.accept(File(imagePath))) {
                    Glide.with(this)
                            .applyDefaultRequestOptions(
                                    RequestOptions().placeholder(R.drawable.user_placeholder).error(
                                            R.drawable.user_placeholder
                                    )
                            )
                            .load(imagePath)
                            .into(profile_image)

                } else {

                    Global.displayToastMessage("Please Select Image File Only!", this)
                }

            } else {
                Global.displayToastMessage("Sorry, Image not found on this device", this)
            }


        } else if (requestCode == SELECT_PICTURE && resultCode == Activity.RESULT_OK) run {
            if (data != null) {
                var originalUri: Uri? = null
                originalUri = data.data
                val originalPath = ImagePath.getPath(this, originalUri)!!
                Log.e("imagePath", originalPath)

                if (!originalPath.isEmpty()) {
                    imagePath = originalPath
                    if (ImageFileFilter.accept(File(imagePath))) {

                        try {
                            Glide.with(this)
                                    .load(imagePath)
                                    .apply(
                                            RequestOptions().placeholder(R.drawable.user_placeholder).error(
                                                    R.drawable.user_placeholder
                                            )
                                    )
                                    .listener(object : RequestListener<Drawable> {
                                        override fun onLoadFailed(e: GlideException?, model: Any?, target: Target<Drawable>?, isFirstResource: Boolean): Boolean {
                                            Log.e("excep", "" + e?.message)
                                            return false
                                        }

                                        override fun onResourceReady(resource: Drawable?, model: Any?, target: Target<Drawable>?, dataSource: DataSource?, isFirstResource: Boolean): Boolean {
                                            profile_image.setImageDrawable(resource)
                                            return true
                                        }


                                    }).submit()
                        } catch (e: Exception) {
                            Log.e("ex", e.message)
                        }


                    } else {
                        Global.displayToastMessage("Please Select Image File Only!", this)
                    }
                }
            }
        }
        else if (requestCode == SELECT_PICTURE_KITKAT && resultCode == Activity.RESULT_OK) {
            if (data != null) {
                var originalUri: Uri? = null
                originalUri = data.data
                this.contentResolver?.takePersistableUriPermission(
                        originalUri!!,
                        Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                )
                val originalPath = ImagePath.getPath(this, originalUri)!!

                if (!originalPath.isEmpty()) {
                    imagePath = originalPath

                    if (ImageFileFilter.accept(File(originalPath))) {

                        if (ImageFileFilter.getFileSize(originalPath) > 0 && ImageFileFilter.getFileSize(
                                        originalPath
                                ) < 3072
                        ) {
                            Glide.with(this)
                                    .applyDefaultRequestOptions(
                                            RequestOptions().placeholder(R.drawable.user_placeholder).error(
                                                    R.drawable.user_placeholder
                                            )
                                    )
                                    .load(originalPath)
                                    .into(profile_image)

                        } else {
                            Global.displayToastMessage(

                                    "Please Select Valid Image with Size Max. 3 MB!", this
                            )
                        }

                    } else {
                        Global.displayToastMessage("Please Select Image File Only!", this)
                    }
                }
            }

        }
    }
}