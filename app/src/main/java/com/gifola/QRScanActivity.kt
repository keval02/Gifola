package com.gifola

import android.R.layout
import android.app.AlertDialog
import android.app.ProgressDialog
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothServerSocket
import android.bluetooth.BluetoothSocket
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.Point
import android.nfc.NdefMessage
import android.nfc.NdefRecord
import android.nfc.NfcAdapter
import android.nfc.NfcEvent
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import android.widget.*
import android.widget.AdapterView.OnItemClickListener
import androidmads.library.qrgenearator.QRGContents
import androidmads.library.qrgenearator.QRGEncoder
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.gifola.QRScanActivity
import com.gifola.apis.AdminAPI
import com.gifola.apis.SeriveGenerator
import com.gifola.constans.Global.displayToastMessage
import com.gifola.constans.Global.getUserMe
import com.gifola.constans.Global.setUserMe
import com.gifola.constans.SharedPreferenceHelper
import com.gifola.customfonts.MyEditText
import com.gifola.customfonts.MyTextViewMedium
import com.gifola.helper.AESEncryptionDecryptionAlgorithm
import com.gifola.helper.AESPasswordAlgo
import com.gifola.model.UserData
import kotlinx.android.synthetic.main.activity_q_r_scan.*
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.nio.charset.Charset
import java.text.SimpleDateFormat
import java.util.*

class QRScanActivity : AppCompatActivity(), NfcAdapter.CreateNdefMessageCallback {
    var toolbar: Toolbar? = null
    var txt_title: TextView? = null
    private var qrgEncoder: QRGEncoder? = null
    private var bitmap: Bitmap? = null
    var preferenceHelper: SharedPreferenceHelper? = null
    var jsonObject: JSONObject? = null
    var encryptedText = ""
    var generatedText = ""
    var password: String? = ""
    private val handler = Handler()
    var runnable: Runnable? = null
    var isGeneratedOnce = false
    var userData: UserData? = null
    var adminAPI: AdminAPI? = null
    var progressDialog: ProgressDialog? = null
    var bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
    var TAG = "QRScanActivity"
    var mConnectedThread: ConnectedThread? = null
    lateinit var mmDevice: BluetoothDevice
    lateinit var deviceUUID: UUID
    var macAddress = ""
    lateinit var mBluetoothAdapter: BluetoothAdapter
    lateinit var mPairedDevices: Set<BluetoothDevice>
    var socketConnectedOrNot = false
    private var mNFCAdapter: NfcAdapter? = null

    private inner class ConnectThread(device: BluetoothDevice, uuid: UUID) : Thread() {
        private var mmSocket: BluetoothSocket? = null
        override fun run() {
            var tmp: BluetoothSocket? = null
            Log.i(TAG, "RUN mConnectThread ")

            // Get a BluetoothSocket for a connection with the
            // given BluetoothDevice
            try {
                Log.d(TAG, "ConnectThread: Trying to create InsecureRfcommSocket using UUID: "
                        + MY_UUID_INSECURE)
                tmp = mmDevice.createRfcommSocketToServiceRecord(MY_UUID_INSECURE)
            } catch (e: IOException) {
                Log.e(TAG, "ConnectThread: Could not create InsecureRfcommSocket " + e.message)
            }
            mmSocket = tmp

            // Make a connection to the BluetoothSocket
            try {
                // This is a blocking call and will only return on a
                // successful connection or an exception
                mmSocket!!.connect()
                socketConnectedOrNot = mmSocket!!.isConnected
                runOnUiThread { Toast.makeText(this@QRScanActivity, "Bluetooth connection successful", Toast.LENGTH_SHORT).show() }
            } catch (e: IOException) {
                // Close the socket
                try {
                    mmSocket!!.close()
                    Log.d("4 ", "isConnected ? : " + mmSocket!!.isConnected)
                    socketConnectedOrNot = mmSocket!!.isConnected
                    runOnUiThread { Toast.makeText(this@QRScanActivity, "Bluetooth connection unsuccessful", Toast.LENGTH_SHORT).show() }
                    Log.d(TAG, "run: Closed Socket.")
                } catch (e1: IOException) {
                    Log.e(TAG, "mConnectThread: run: Unable to close connection in socket " + e1.message)
                    Log.d("4 ", "isConnected ? : " + mmSocket!!.isConnected)
                }
                Log.d(TAG, "run: ConnectThread: Could not connect to UUID: $MY_UUID_INSECURE")
            }
            connected(mmSocket)
        }

        fun cancel() {
            try {
                Log.d(TAG, "cancel: Closing Client Socket.")
                mmSocket!!.close()
            } catch (e: IOException) {
                Log.e(TAG, "cancel: close() of mmSocket in Connectthread failed. " + e.message)
            }
        }

        init {
            Log.d(TAG, "ConnectThread: started.")
            mmDevice = device
            deviceUUID = uuid
        }
    }

    private fun connected(mmSocket: BluetoothSocket?) {
        Log.d(TAG, "connected: Starting." + mmSocket!!.isConnected)

        // Start the thread to manage the connection and perform transmissions
        mConnectedThread = ConnectedThread(mmSocket)
        mConnectedThread!!.start()
    }

    inner class ConnectedThread(socket: BluetoothSocket?) : Thread() {
        private val mmSocket: BluetoothSocket?
        private val mmInStream: InputStream?
        private val mmOutStream: OutputStream?
        override fun run() {
            socketConnectedOrNot = true
            val buffer = ByteArray(1024) // buffer store for the stream
            var bytes: Int // bytes returned from read()

            // Keep listening to the InputStream until an exception occurs
            while (true) {
                // Read from the InputStream
                try {
                    bytes = mmInStream!!.read(buffer)
                    val incomingMessage = String(buffer, 0, bytes)
                    Log.d(TAG, "InputStream: $incomingMessage")
                    runOnUiThread { //                            view_data.setText(incomingMessage);
                        Toast.makeText(this@QRScanActivity, "incomingMessage $incomingMessage", Toast.LENGTH_SHORT).show()
                    }
                } catch (e: IOException) {
                    Log.e(TAG, "write: Error reading Input Stream. " + e.message)
                    break
                }
            }
        }

        fun write(bytes: ByteArray?) {
            val text = String(bytes!!, Charset.defaultCharset())
            Log.d(TAG, "write: Writing to outputstream: $text")
            try {
                mmOutStream!!.write(bytes)
            } catch (e: IOException) {
                Log.e(TAG, "write: Error writing to output stream. " + e.message)
            }
        }

        /* Call this from the main activity to shutdown the connection */
        fun cancel() {
            try {
                mmSocket!!.close()
            } catch (e: IOException) {
            }
        }

        init {
            Log.d(TAG, "ConnectedThread: Starting." + socket!!.isConnected)
            mmSocket = socket
            var tmpIn: InputStream? = null
            var tmpOut: OutputStream? = null
            try {
                tmpIn = mmSocket!!.inputStream
                tmpOut = mmSocket.outputStream
            } catch (e: IOException) {
                e.printStackTrace()
            }
            mmInStream = tmpIn
            mmOutStream = tmpOut
        }
    }

    fun SendMessage() {
        if (socketConnectedOrNot == true) {
            try {
                /*byte[] bytes = sampleSendText.getBytes(Charset.defaultCharset());
                mConnectedThread.write(bytes);*/
                val applicationUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")
                val device = bluetoothAdapter.getRemoteDevice(macAddress)
                val socket = device.createInsecureRfcommSocketToServiceRecord(applicationUUID)
                val mmout = socket.outputStream
                socket.connect()
                mmout.write(encryptedText.toByteArray(Charset.defaultCharset()))
                mmout.flush()
                mmout.close()
                socket.close()
                // Your Data is sent to  BT connected paired device ENJOY.
            } catch (e: Exception) {
                Log.e(TAG, "Exception during write", e)
            }
        } else {
            Toast.makeText(this@QRScanActivity, "Bluetooth not connected with receiver", Toast.LENGTH_SHORT).show()
        }
    }

    fun Start_Server() {
        val accept = AcceptThread()
        accept.start()
    }

    private inner class AcceptThread : Thread() {
        // The local server socket
        private val mmServerSocket: BluetoothServerSocket?
        override fun run() {
            Log.d(TAG, "run: AcceptThread Running.")
            var socket: BluetoothSocket? = null
            try {
                // This is a blocking call and will only return on a
                // successful connection or an exception
                Log.d(TAG, "run: RFCOM server socket start.....")
                socket = mmServerSocket!!.accept()
                Log.d(TAG, "run: RFCOM server socket accepted connection.")
            } catch (e: IOException) {
                Log.e(TAG, "AcceptThread: IOException: " + e.message)
            }

            //talk about this is in the 3rd
            socket?.let { connected(it) }
            Log.i(TAG, "END mAcceptThread ")
        }

        fun cancel() {
            Log.d(TAG, "cancel: Canceling AcceptThread.")
            try {
                mmServerSocket!!.close()
            } catch (e: IOException) {
                Log.e(TAG, "cancel: Close of AcceptThread ServerSocket failed. " + e.message)
            }
        }

        init {
            var tmp: BluetoothServerSocket? = null

            // Create a new listening server socket
            try {
                tmp = bluetoothAdapter.listenUsingInsecureRfcommWithServiceRecord("appname", MY_UUID_INSECURE)
                Log.d(TAG, "AcceptThread: Setting up Server using: $MY_UUID_INSECURE")
            } catch (e: IOException) {
                Log.e(TAG, "AcceptThread: IOException: " + e.message)
            }
            mmServerSocket = tmp
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_q_r_scan)
        preferenceHelper = SharedPreferenceHelper(this)
        adminAPI = SeriveGenerator.getAPIClass()
        progressDialog = ProgressDialog(this)
        setupToolbar()
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
        if (mBluetoothAdapter == null) {
            // Device does not support Bluetooth
            Toast.makeText(applicationContext, "Bluetooth Device Not Available", Toast.LENGTH_LONG).show()
        } else if (!mBluetoothAdapter!!.isEnabled) {
            // Bluetooth is not enabled :)
            val turnBTon = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            startActivityForResult(turnBTon, 1)
        } else {
            // Bluetooth is enabled
            listPairedDevices()
            Start_Server()
        }

        try{
            mNFCAdapter = NfcAdapter.getDefaultAdapter(this).apply {
                setNdefPushMessageCallback(this@QRScanActivity, this@QRScanActivity)
            }
        }catch (e : Exception){

        }

        if (mNFCAdapter == null) {
            nfcTV.setText(R.string.nfc_unavailable)
            checkInWithNFC.isEnabled = false
        }


        checkInWithBLE!!.setOnClickListener { SendMessage() }
        sendbtnmsg!!.setOnClickListener { SendMessage() }
    }

    override fun createNdefMessage(event: NfcEvent?): NdefMessage {
        val packageName = applicationContext.packageName
        val payload = encryptedText
        val mimeType = "application/$packageName.payload"
        return NdefMessage(
                arrayOf<NdefRecord>(
                        NdefRecord(
                                NdefRecord.TNF_MIME_MEDIA,
                                mimeType.toByteArray(Charsets.UTF_8),
                                ByteArray(0),
                                payload.toByteArray(Charsets.UTF_8)
                        ),
                        NdefRecord.createApplicationRecord(packageName)
                )
        )
    }


    private fun listPairedDevices() {
        mPairedDevices = mBluetoothAdapter.bondedDevices
        val list = ArrayList<Any?>()
        if (mPairedDevices.size > 0) {
            for (bt in mPairedDevices) {
                list.add(bt.name + "\n" + bt.address)
            }
        } else {
            Toast.makeText(applicationContext, "No Paired Bluetooth Devices Found.", Toast.LENGTH_LONG).show()
        }
        val adapter: ArrayAdapter<*> = ArrayAdapter(this, layout.simple_list_item_1, list)
        listView!!.adapter = adapter
        listView!!.onItemClickListener = myListClickListener //Method called when the device from the list is clicked
    }

    private val myListClickListener = OnItemClickListener { av, v, arg2, arg3 ->
        // Get the device MAC address, the last 17 chars in the View
        val info = (v as TextView).text.toString()
        val address = info.substring(info.length - 17)
        macAddress = address
        Log.d("TAG", "onItemClick: $address")
        val device = mBluetoothAdapter.getRemoteDevice(address)
        AlertDialog.Builder(this@QRScanActivity)
                .setTitle("Connect to device")
                .setMessage("Do you want to connect bluetooth with $info") // Specifying a listener allows you to take an action before dismissing the dialog.
                // The dialog is automatically dismissed when a dialog button is clicked.
                .setPositiveButton(android.R.string.yes) { dialog, which ->
                    // Continue with delete operation
                    val connect = ConnectThread(device, MY_UUID_INSECURE)
                    connect.start()
                    //                            bluetoothListLayout.setVisibility(View.GONE);
                } // A null listener allows the button to dismiss the dialog and take no further action.
                .setNegativeButton(android.R.string.no, null)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show()
    }

    private fun setupToolbar() {
        toolbar = findViewById<View>(R.id.toolbarsignup) as Toolbar
        txt_title = findViewById<View>(R.id.txt_title) as TextView
        setSupportActionBar(toolbar)
        txt_title!!.text = "TOUCHFREE CHECKIN"
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportActionBar!!.setHomeButtonEnabled(true)
        supportActionBar!!.setHomeButtonEnabled(true)
        toolbar!!.setNavigationIcon(R.drawable.back_1)
        toolbar!!.setNavigationOnClickListener { onBackPressed() }
        userData = getUserMe(preferenceHelper!!)
        password = userData!!.app_usr_password
        if (password != "") {
            try {
                val decryptPassword = AESPasswordAlgo.decrypt(password)
                Log.e("decryptedPassword", "" + decryptPassword)
                edtPwd.setText(decryptPassword)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        runnable = Runnable {
            if (isGeneratedOnce) {
                generateQRCodeData()
            }
        }
        addPwdToQR.setOnClickListener(View.OnClickListener {
            val getEnteredPasswordText = edtPwd.getText().toString().trim { it <= ' ' }
            if (getEnteredPasswordText !== password) {
                try {
                    val encryptPassword = AESPasswordAlgo.encrypt(getEnteredPasswordText)
                    Log.e("encryptedPassword", "" + encryptPassword)
                    userData!!.app_usr_password = encryptPassword
                    saveUserProfile(userData)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        })
        generateQRCodeData()
        goToBluetoothSettings.setOnClickListener(View.OnClickListener {
            val intent = Intent(Intent.ACTION_MAIN, null)
            intent.addCategory(Intent.CATEGORY_LAUNCHER)
            val cn = ComponentName("com.android.settings",
                    "com.android.settings.bluetooth.BluetoothSettings")
            intent.component = cn
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
        })
    }

    private fun generateQRCodeData() {
        val name = getUserMe(preferenceHelper!!)!!.app_usr_name
        val mobile = getUserMe(preferenceHelper!!)!!.app_usr_mobile
        val timeStamp = SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.getDefault()).format(Date())
        val password = edtPwd!!.text.toString().trim { it <= ' ' }
        try {
            jsonObject = JSONObject()
            jsonObject!!.put("name", name)
            jsonObject!!.put("mobile_number", mobile)
            jsonObject!!.put("time", timeStamp)
            jsonObject!!.put("password", password)
            generatedText = jsonObject.toString()
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
        generateQRCode(generatedText)
    }

    private fun generateQRCode(generatedText: String) {
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
            qrImage!!.setImageBitmap(bitmap)
            if (!isGeneratedOnce) isGeneratedOnce = true
            handler.postDelayed(runnable, 30000)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun saveUserProfile(userData: UserData?) {
        progressDialog!!.show()
        val responseBodyCall = adminAPI!!.UpdateUserDetailsWithImage(userData!!.app_usr_id!!, userData.app_usr_Organization, userData.app_usr_designation, userData.app_usr_dob, userData.app_usr_email, userData.app_usr_home_address, userData.app_usr_mobile, userData.app_usr_name, userData.app_usr_password, userData.app_usr_reg_date, userData.app_usr_status!!, userData.app_usr_work_address, userData.isactive!!, userData.app_pic, null)
        responseBodyCall.enqueue(object : Callback<String?> {
            override fun onResponse(call: Call<String?>, response: Response<String?>) {
                progressDialog!!.hide()
                if (response.code() == 200) {
                    setUserMe(userData, preferenceHelper!!)
                    generateQRCodeData()
                } else {
                    displayToastMessage(getString(R.string.message_something_went_wrong), applicationContext)
                }
            }

            override fun onFailure(call: Call<String?>, t: Throwable) {
                progressDialog!!.hide()
                displayToastMessage(getString(R.string.message_something_went_wrong), applicationContext)
            }
        })
    }

    override fun onBackPressed() {
        handler.removeCallbacks(runnable)
        super.onBackPressed()
    }

    override fun onDestroy() {
        handler.removeCallbacks(runnable)
        super.onDestroy()
    }

    override fun onResume() {
        super.onResume()
        when (intent.action) {
            Intent.ACTION_SEND -> {
                if (intent.type == "text/plain") {
                    val sharedText = intent.getStringExtra(Intent.EXTRA_TEXT)
                    Toast.makeText(this , sharedText, Toast.LENGTH_LONG).show()
                }
            }
            NfcAdapter.ACTION_NDEF_DISCOVERED -> {
                val messages = intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES)
                val payload = (messages[0] as NdefMessage).records[0].payload
                Toast.makeText(this , payload.toString(Charsets.UTF_8), Toast.LENGTH_LONG).show()
            }
        }
    }


    companion object {
        private val MY_UUID_INSECURE = UUID.fromString("8ce255c0-200a-11e0-ac64-0800200c9a66")
    }
}