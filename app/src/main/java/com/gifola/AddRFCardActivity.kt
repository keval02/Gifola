package com.gifola

import android.app.Dialog
import android.app.ProgressDialog
import android.content.Context
import android.content.DialogInterface
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.Point
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.view.WindowManager
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.TextView
import androidmads.library.qrgenearator.QRGContents
import androidmads.library.qrgenearator.QRGEncoder
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.view.ContextThemeWrapper
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import com.gifola.adapter.RFCardListAdapter
import com.gifola.apis.AdminAPI
import com.gifola.apis.SeriveGenerator
import com.gifola.constans.Global
import com.gifola.constans.Global.displayToastMessage
import com.gifola.constans.SharedPreferenceHelper
import com.gifola.helper.AESEncryptionDecryptionAlgorithm
import com.gifola.model.RFCardModel
import com.gifola.model.RFCardModelItem
import com.gifola.model.RFLocationModel
import com.gifola.model.UserData
import com.google.gson.JsonObject
import kotlinx.android.synthetic.main.activity_add_r_f_card.*
import kotlinx.android.synthetic.main.layout_qr_dialog.*
import okhttp3.ResponseBody
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList


class AddRFCardActivity : AppCompatActivity() {
    var member: ArrayList<String> = ArrayList()
    var toolbar: Toolbar? = null
    var txt_title: TextView? = null
    var rfLocationList: MutableList<RFLocationModel> = ArrayList()
    var bar: ProgressDialog? = null
    var adminAPI: AdminAPI? = null
    var preferenceHelper: SharedPreferenceHelper? = null
    var userData: UserData? = null
    var rfCardDataModelItems: ArrayList<RFCardModelItem> = ArrayList()
    lateinit var rfCardAdapter: RFCardListAdapter
    var memberId: Int = 0
    var jsonObject: JSONObject? = null
    var generatedText = ""
    var encryptedText = ""
    private var qrgEncoder: QRGEncoder? = null
    private var bitmap: Bitmap? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_r_f_card)
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
            val rfCardNumber: String = edit_rdcardno.text.toString().trim()
            val rfCardHolderName: String = edit_rdcardholdername.text.toString().trim()
            var isLocationAvailable: Boolean = false
            var selectedLocationId: Int = 0
            var selectedLocationMemberId: Int = 0



            when {
                rfCardNumber.isEmpty() -> {
                    displayToastMessage(getString(R.string.message_valid_card_no), applicationContext)
                }
                rfCardHolderName.isEmpty() -> {
                    displayToastMessage(getString(R.string.message_valid_card_holder_name), applicationContext)
                }
                else -> {
                    var totalCardOnSelectedLocation = 0
                    if (rfLocationList[0].size > 0) {
                        val selectedLocationPosition = spinner2.selectedItemPosition
                        selectedLocationId = rfLocationList[0][selectedLocationPosition].site_id
                        selectedLocationMemberId = rfLocationList[0][selectedLocationPosition].mem_id
                        isLocationAvailable = true
                    }

                    rfCardDataModelItems.forEachIndexed { index, rfLocationDataModel ->
                        if (rfLocationDataModel.site_id == selectedLocationId) {
                            totalCardOnSelectedLocation += 1
                        }
                    }

                    if (totalCardOnSelectedLocation >= 4) {
                        displayToastMessage(getString(R.string.message_rf_card_limit_exceed), applicationContext)
                    } else {
                        addRFCard(rfCardNumber, rfCardHolderName, selectedLocationId, selectedLocationMemberId, isLocationAvailable)
                    }
                }
            }
        }

        spinner2?.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {

            }

            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                spinner2.setSelection(position)
            }

        }

        btnReadCard.setOnClickListener {
            openQRCodeDialog()
        }


        setupToolbar()
        getRFCardList(memberId)
        getRFLocationList()
        generateQRCodeData()
    }

    private fun addRFCard(rfCardNumber: String, rfCardHolderName: String, selectedLocationId: Int, selectedLocationMemberId: Int, isLocationAvailable: Boolean) {
        val jsonRequest = JsonObject();
        jsonRequest.addProperty("app_usr_rf_name", rfCardHolderName)
        jsonRequest.addProperty("app_usr_rf_no", rfCardNumber)
        jsonRequest.addProperty("app_usr_id", userData?.app_usr_id)
        jsonRequest.addProperty("app_site_id", selectedLocationId)
        jsonRequest.addProperty("isLocationAvailable", isLocationAvailable)
        jsonRequest.addProperty("mem_id", selectedLocationMemberId)
        jsonRequest.addProperty("mem_cust_id", userData?.mem_cust_id)
        bar?.show()
        val responseBodyCall: Call<ResponseBody>? = adminAPI?.AddRFCard(jsonRequest)
        responseBodyCall?.enqueue(object : Callback<ResponseBody?> {
            override fun onResponse(call: Call<ResponseBody?>, response: Response<ResponseBody?>) {
                if (response.code() == 200) {
                    edit_rdcardno.setText("")
                    edit_rdcardholdername.setText("")
                    spinner2.setSelection(0)
                    displayToastMessage(getString(R.string.message_rf_card_added), applicationContext)
                    getRFCardList(memberId)
                } else if (response.code() == 400) {
                    bar?.hide()
                    displayToastMessage(getString(R.string.message_card_already_exist), applicationContext)
                } else {
                    bar?.hide()
                    displayToastMessage(getString(R.string.message_something_went_wrong), applicationContext)
                }
            }

            override fun onFailure(call: Call<ResponseBody?>, t: Throwable) {
                bar?.hide()
                displayToastMessage(getString(R.string.message_something_went_wrong), applicationContext)
            }
        })
    }

    private fun getRFLocationList() {
        val responseBodyCall: Call<RFLocationModel>? = adminAPI?.GetRFLocations(userData?.app_usr_id!!)
        responseBodyCall?.enqueue(object : Callback<RFLocationModel?> {
            override fun onResponse(call: Call<RFLocationModel?>, response: Response<RFLocationModel?>) {
                if (response.code() == 200) {
                    try {
                        response.body()?.let { rfLocationList.add(it) }
                        if (rfLocationList.size > 0) {
                            rfLocationList[0].forEachIndexed { index, rfLocationModel ->
                                member.add(rfLocationModel.site_title)
                            }

                            val spinnerArrayAdapter = ArrayAdapter(this@AddRFCardActivity, android.R.layout.simple_spinner_item, member)
                            spinnerArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item) // The drop down view
                            spinner2.adapter = spinnerArrayAdapter;


                        }
                    } catch (e: Exception) {
                        Log.e("exception", e.message)
                    }

                } else {
                    displayToastMessage(getString(R.string.message_something_went_wrong), applicationContext)
                }
            }

            override fun onFailure(call: Call<RFLocationModel?>, t: Throwable) {
                displayToastMessage(getString(R.string.message_something_went_wrong), applicationContext)
            }
        })


    }

    private fun getRFCardList(memberId: Int) {
        bar?.hide()
        val jsonRequest = JsonObject();
        jsonRequest.addProperty("app_usr_id", userData?.app_usr_id)
        jsonRequest.addProperty("mem_id", memberId)
        val responseBodyCall: Call<RFCardModel>? = adminAPI?.GetAllRFCards(jsonRequest)
        responseBodyCall?.enqueue(object : Callback<RFCardModel?> {
            override fun onResponse(call: Call<RFCardModel?>, response: Response<RFCardModel?>) {
                bar?.hide()
                rfCardDataModelItems.clear()
                if (response.code() == 200) {
                    val rfCardDataModel: RFCardModel? = response.body()
                    rfCardDataModel?.forEachIndexed { index, rfCardModelItem ->
                        rfCardDataModelItems.add(rfCardDataModel[index])
                    }

                    rfCardAdapter = object : RFCardListAdapter(this@AddRFCardActivity, rfCardDataModelItems) {
                        override fun deleteRFCard(appUsrRfId: Int, appRfStId: Int) {
                            deletedCard(appUsrRfId, appRfStId)
                        }

                        override fun copyRFCard(rfCardModelItem: RFCardModelItem) {
                            edit_rdcardno.setText(rfCardModelItem.cardNo)
                            edit_rdcardholdername.setText(rfCardModelItem.name)
                            val locationId = rfCardModelItem.site_id
                            var spinnerPosition = 0
                            rfLocationList[0].forEachIndexed { index, rfCardModelItems ->
                                if (rfCardModelItems.site_id == locationId) {
                                    spinnerPosition = index
                                }
                            }
                            spinner2.setSelection(spinnerPosition)
                        }
                    }
                    val layoutManager = LinearLayoutManager(this@AddRFCardActivity, LinearLayoutManager.VERTICAL, false)
                    rv_location.layoutManager = layoutManager
                    rv_location.adapter = rfCardAdapter

                    if (rfCardDataModelItems.size > 0) {
                        rv_location.visibility = View.VISIBLE
                        txt_no_data_found.visibility = View.GONE
                    } else {
                        txt_no_data_found.visibility = View.VISIBLE
                        rv_location.visibility = View.GONE
                    }


                } else {
                    txt_no_data_found.visibility = View.VISIBLE
                }
            }

            override fun onFailure(call: Call<RFCardModel?>, t: Throwable) {
                bar?.hide()
                txt_no_data_found.visibility = View.VISIBLE
            }
        })
    }

    private fun deletedCard(appUsrRfId: Int, appRfStId: Int) {
        val alert: AlertDialog.Builder = AlertDialog.Builder(this)

        alert.setTitle("Delete Card")
        alert.setMessage("Are you sure you want to delete this card?")
        alert.setPositiveButton(android.R.string.yes, object : DialogInterface.OnClickListener {
            override fun onClick(dialog: DialogInterface, which: Int) {
                val jsonRequest = JsonObject();
                jsonRequest.addProperty("app_usr_rf_id", appUsrRfId)
                jsonRequest.addProperty("app_rf_st_id", appRfStId)
                bar?.show()
                val responseBodyCall: Call<ResponseBody>? = adminAPI?.DeletedRFCard(jsonRequest)
                responseBodyCall?.enqueue(object : Callback<ResponseBody?> {
                    override fun onResponse(call: Call<ResponseBody?>, response: Response<ResponseBody?>) {
                        getRFCardList(memberId)
                    }

                    override fun onFailure(call: Call<ResponseBody?>, t: Throwable) {
                        bar?.hide()
                        displayToastMessage(getString(R.string.message_something_went_wrong), applicationContext)
                    }
                })
            }
        })
        alert.setNegativeButton(android.R.string.no, DialogInterface.OnClickListener { dialog, which -> // close dialog
            dialog.cancel()
        })
        alert.show()
    }

    private fun setupToolbar() {
        toolbar = findViewById<View>(R.id.toolbarsignup) as Toolbar
        txt_title = findViewById<View>(R.id.txt_title) as TextView
        setSupportActionBar(toolbar)
        txt_title!!.text = "Add/Remove RF Card"
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportActionBar!!.setHomeButtonEnabled(true)
        toolbar!!.setNavigationIcon(R.drawable.back_1)
        toolbar!!.setNavigationOnClickListener { onBackPressed() }
    }

    private fun generateQRCodeData() {
        val name = Global.getUserMe(preferenceHelper!!)!!.app_usr_name
        val mobile = Global.getUserMe(preferenceHelper!!)!!.app_usr_mobile
        val timeStamp = SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.getDefault()).format(Date())
        try {
            jsonObject = JSONObject()
            jsonObject!!.put("name", name)
            jsonObject!!.put("mobile_number", mobile)
            jsonObject!!.put("time", timeStamp)
            generatedText = "R: " + jsonObject.toString()
        } catch (e: Exception) {
            Log.e("exceptionJson", e.message)
        }
        try {
            val key = "this is my key"
            encryptedText = AESEncryptionDecryptionAlgorithm.getInstance(key).encrypt_string(generatedText)
            Log.e("textEncrypt", "" + encryptedText)
            Log.e("textDecrypy", "" + AESEncryptionDecryptionAlgorithm.getInstance(key).decrypt_string(encryptedText))
        } catch (e: Exception) {
            e.printStackTrace()
        }
        if (!encryptedText.isEmpty()) {
            generatedText = encryptedText
        }
    }

    private fun openQRCodeDialog() {
        val dialog: Dialog = Dialog(ContextThemeWrapper(this, R.style.AppTheme))
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(true)
        dialog.setContentView(R.layout.layout_qr_dialog)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)

        var isGeneratedOnce = false
        val manager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val display = manager.defaultDisplay
        val point = Point()
        display.getSize(point)
        val width = point.x
        val height = point.y
        var smallerDimension = if (width < height) width else height
        smallerDimension = smallerDimension * 3 / 4
        qrgEncoder = QRGEncoder(
                generatedText, null,
                QRGContents.Type.TEXT,
                smallerDimension)
        qrgEncoder!!.colorBlack = Color.BLACK
        qrgEncoder!!.colorWhite = Color.WHITE
        try {
            bitmap = qrgEncoder!!.bitmap
            dialog.qrImage!!.setImageBitmap(bitmap)
        } catch (e: Exception) {
            e.printStackTrace()
        }

        dialog.ivClose.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }
}



