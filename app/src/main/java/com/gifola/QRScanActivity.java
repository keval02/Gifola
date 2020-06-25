package com.gifola;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Point;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.gifola.apis.AdminAPI;
import com.gifola.apis.SeriveGenerator;
import com.gifola.constans.Global;
import com.gifola.constans.SharedPreferenceHelper;
import com.gifola.customfonts.MyEditText;
import com.gifola.customfonts.MyTextViewMedium;
import com.gifola.helper.AESPasswordAlgo;
import com.gifola.helper.AESQRAlgorithm;
import com.gifola.model.UserData;
import com.google.gson.Gson;

import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

import androidmads.library.qrgenearator.QRGContents;
import androidmads.library.qrgenearator.QRGEncoder;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static android.R.layout.simple_list_item_1;

public class QRScanActivity extends AppCompatActivity {
    Toolbar toolbar;
    TextView txt_title;
    private QRGEncoder qrgEncoder;
    private Bitmap bitmap;
    ImageView qrImage;
    SharedPreferenceHelper preferenceHelper;
    MyTextViewMedium addPwdToQR;
    MyEditText edtPwd;
    LinearLayout checkInWithBLE;
    JSONObject jsonObject;
    String encryptedText = "";
    String generatedText = "";
    String sampleSendText = "Data from bluetooth";
    LinearLayout bluetoothListLayout;
    String password = "";
    private Handler handler = new Handler();
    Runnable runnable;
    Boolean isGeneratedOnce = false;
    UserData userData;
    AdminAPI adminAPI;
    Button acceptThread, connectRF, sendbtnmsg;
    ProgressDialog progressDialog;
    BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    private static final UUID MY_UUID_INSECURE =
            UUID.fromString("8ce255c0-200a-11e0-ac64-0800200c9a66");
    String TAG = "QRScanActivity";
    ConnectedThread mConnectedThread;
    private BluetoothDevice mmDevice;
    private UUID deviceUUID;
    EditText send_data;

    ListView mDeviceList;
    private BluetoothAdapter mBluetoothAdapter = null;
    private Set<BluetoothDevice> mPairedDevices;
    Boolean socketConnectedOrNot;

    public void pairDevice() {

        Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();
        Log.e("MAinActivity", "" + pairedDevices.size());
        if (pairedDevices.size() > 0) {
            Object[] devices = pairedDevices.toArray();
            BluetoothDevice device = (BluetoothDevice) devices[0];

            Log.d(TAG, "pairDevice: " + device);
//            Log.d(TAG, "pairDevice: " + device.getName());
            //ParcelUuid[] uuid = device.getUuids();
            Log.e("MAinActivity", "" + device);
            //Log.e("MAinActivity", "" + uuid)

            ConnectThread connect = new ConnectThread(device, MY_UUID_INSECURE);
            connect.start();

        }

    }

    private class ConnectThread extends Thread {
        private BluetoothSocket mmSocket;

        public ConnectThread(BluetoothDevice device, UUID uuid) {
            Log.d(TAG, "ConnectThread: started.");
            mmDevice = device;
            deviceUUID = uuid;
        }

        public void run() {
            BluetoothSocket tmp = null;
            Log.i(TAG, "RUN mConnectThread ");

            // Get a BluetoothSocket for a connection with the
            // given BluetoothDevice
            try {
                Log.d(TAG, "ConnectThread: Trying to create InsecureRfcommSocket using UUID: "
                        + MY_UUID_INSECURE);
                tmp = mmDevice.createRfcommSocketToServiceRecord(MY_UUID_INSECURE);
            } catch (IOException e) {
                Log.e(TAG, "ConnectThread: Could not create InsecureRfcommSocket " + e.getMessage());
            }

            mmSocket = tmp;

            // Make a connection to the BluetoothSocket

            try {
                // This is a blocking call and will only return on a
                // successful connection or an exception
                mmSocket.connect();
                socketConnectedOrNot = mmSocket.isConnected();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(QRScanActivity.this, "Bluetooth connection successful", Toast.LENGTH_SHORT).show();
                    }
                });
            } catch (IOException e) {
                // Close the socket
                try {
                    mmSocket.close();
                    Log.d("4 ", "isConnected ? : " + mmSocket.isConnected());
                    socketConnectedOrNot = mmSocket.isConnected();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(QRScanActivity.this, "Bluetooth connection unsuccessful", Toast.LENGTH_SHORT).show();
                        }
                    });
                    Log.d(TAG, "run: Closed Socket.");
                } catch (IOException e1) {
                    Log.e(TAG, "mConnectThread: run: Unable to close connection in socket " + e1.getMessage());
                    Log.d("4 ", "isConnected ? : " + mmSocket.isConnected());
                }
                Log.d(TAG, "run: ConnectThread: Could not connect to UUID: " + MY_UUID_INSECURE);
            }

            connected(mmSocket);
        }

        public void cancel() {
            try {
                Log.d(TAG, "cancel: Closing Client Socket.");
                mmSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "cancel: close() of mmSocket in Connectthread failed. " + e.getMessage());
            }
        }
    }

    private void connected(BluetoothSocket mmSocket) {
        Log.d(TAG, "connected: Starting." + mmSocket.isConnected());

        // Start the thread to manage the connection and perform transmissions
        mConnectedThread = new ConnectedThread(mmSocket);
        mConnectedThread.start();

    }

    private class ConnectedThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;

        public ConnectedThread(BluetoothSocket socket) {
            Log.d(TAG, "ConnectedThread: Starting." + socket.isConnected());

            mmSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;


            try {
                tmpIn = mmSocket.getInputStream();
                tmpOut = mmSocket.getOutputStream();
            } catch (IOException e) {
                e.printStackTrace();
            }

            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }

        public void run() {
            socketConnectedOrNot = true;
            byte[] buffer = new byte[1024];  // buffer store for the stream

            int bytes; // bytes returned from read()

            // Keep listening to the InputStream until an exception occurs
            while (true) {
                // Read from the InputStream
                try {
                    bytes = mmInStream.read(buffer);
                    final String incomingMessage = new String(buffer, 0, bytes);
                    Log.d(TAG, "InputStream: " + incomingMessage);

                    runOnUiThread(new Runnable() {

                        @Override
                        public void run() {
//                            view_data.setText(incomingMessage);
                            Toast.makeText(QRScanActivity.this, "incomingMessage " + incomingMessage, Toast.LENGTH_SHORT).show();
                        }
                    });


                } catch (IOException e) {
                    Log.e(TAG, "write: Error reading Input Stream. " + e.getMessage());
                    break;
                }
            }
        }


        public void write(byte[] bytes) {
            String text = new String(bytes, Charset.defaultCharset());
            Log.d(TAG, "write: Writing to outputstream: " + text);
            try {
                mmOutStream.write(bytes);
            } catch (IOException e) {
                Log.e(TAG, "write: Error writing to output stream. " + e.getMessage());
            }
        }

        /* Call this from the main activity to shutdown the connection */
        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) {
            }
        }
    }

    public void SendMessage() {
//        Toast.makeText(this, "socketConnectedOrNot " + socketConnectedOrNot, Toast.LENGTH_SHORT).show();
        try{
            if (socketConnectedOrNot == true) {
                byte[] bytes = sampleSendText.getBytes(Charset.defaultCharset());
                mConnectedThread.write(bytes);
            } else {
                Toast.makeText(QRScanActivity.this, "Bluetooth not connected with receiver", Toast.LENGTH_SHORT).show();
            }
        }catch (Exception e){
            Toast.makeText(QRScanActivity.this, "Please paired with one device first!", Toast.LENGTH_SHORT).show();
        }

    }

    public void Start_Server() {

        AcceptThread accept = new AcceptThread();
        accept.start();

    }

    private class AcceptThread extends Thread {

        // The local server socket
        private final BluetoothServerSocket mmServerSocket;

        public AcceptThread() {
            BluetoothServerSocket tmp = null;

            // Create a new listening server socket
            try {
                tmp = bluetoothAdapter.listenUsingInsecureRfcommWithServiceRecord("appname", MY_UUID_INSECURE);

                Log.d(TAG, "AcceptThread: Setting up Server using: " + MY_UUID_INSECURE);
            } catch (IOException e) {
                Log.e(TAG, "AcceptThread: IOException: " + e.getMessage());
            }

            mmServerSocket = tmp;
        }

        public void run() {
            Log.d(TAG, "run: AcceptThread Running.");

            BluetoothSocket socket = null;

            try {
                // This is a blocking call and will only return on a
                // successful connection or an exception
                Log.d(TAG, "run: RFCOM server socket start.....");

                socket = mmServerSocket.accept();

                Log.d(TAG, "run: RFCOM server socket accepted connection.");

            } catch (IOException e) {
                Log.e(TAG, "AcceptThread: IOException: " + e.getMessage());
            }

            //talk about this is in the 3rd
            if (socket != null) {
                connected(socket);
            }

            Log.i(TAG, "END mAcceptThread ");
        }

        public void cancel() {
            Log.d(TAG, "cancel: Canceling AcceptThread.");
            try {
                mmServerSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "cancel: Close of AcceptThread ServerSocket failed. " + e.getMessage());
            }
        }

    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_q_r_scan);
        preferenceHelper = new SharedPreferenceHelper(this);
        adminAPI = SeriveGenerator.getAPIClass();
        progressDialog = new ProgressDialog(this);
        setupToolbar();
        mDeviceList = (ListView) findViewById(R.id.listView);

        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        if (mBluetoothAdapter == null) {
            // Device does not support Bluetooth
            Toast.makeText(getApplicationContext(), "Bluetooth Device Not Available", Toast.LENGTH_LONG).show();
        } else if (!mBluetoothAdapter.isEnabled()) {
            // Bluetooth is not enabled :)
            Intent turnBTon = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(turnBTon, 1);
        } else {
            // Bluetooth is enabled
            listPairedDevices();
            Start_Server();
        }
//        if (!mBluetoothAdapter.isEnabled()) {
//            Intent turnBTon = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
//            startActivityForResult(turnBTon, 1);
//        }
//
//        if (mBluetoothAdapter == null) {
//            Toast.makeText(getApplicationContext(), "Bluetooth Device Not Available", Toast.LENGTH_LONG).show();
//            finish();
//        } else {
//            if (mBluetoothAdapter.isEnabled()) {
//                listPairedDevices();
//            } else {
//                //Ask to the user turn the bluetooth on
//                Toast.makeText(this, "Please turn on your Bluetooth and pair with device", Toast.LENGTH_SHORT).show();
//            }
//        }


        checkInWithBLE.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {


                SendMessage();
//                final Handler handler = new Handler();
//                handler.postDelayed(new Runnable() {
//                    @Override
//                    public void run() {
//                        pairDevice();
//                    }
//                }, 2000);
            }
        });

        sendbtnmsg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SendMessage();
            }
        });

    }

    private void listPairedDevices() {
        mPairedDevices = mBluetoothAdapter.getBondedDevices();
        ArrayList list = new ArrayList();

        if (mPairedDevices.size() > 0) {
            for (BluetoothDevice bt : mPairedDevices) {
                list.add(bt.getName() + "\n" + bt.getAddress());
            }
        } else {
            Toast.makeText(getApplicationContext(), "No Paired Bluetooth Devices Found.", Toast.LENGTH_LONG).show();
        }

        final ArrayAdapter adapter = new ArrayAdapter(this, simple_list_item_1, list);
        mDeviceList.setAdapter(adapter);
        mDeviceList.setOnItemClickListener(myListClickListener); //Method called when the device from the list is clicked

    }

    private AdapterView.OnItemClickListener myListClickListener = new AdapterView.OnItemClickListener() {
        public void onItemClick(AdapterView av, View v, int arg2, long arg3) {
            // Get the device MAC address, the last 17 chars in the View
            final String info = ((TextView) v).getText().toString();


            final String address = info.substring(info.length() - 17);
            Log.d("TAG", "onItemClick: " + address);
            final BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);

            new AlertDialog.Builder(QRScanActivity.this)
                    .setTitle("Connect to device")
                    .setMessage("Do you want to connect bluetooth with " + info)

                    // Specifying a listener allows you to take an action before dismissing the dialog.
                    // The dialog is automatically dismissed when a dialog button is clicked.
                    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            // Continue with delete operation
                            ConnectThread connect = new ConnectThread(device, MY_UUID_INSECURE);
                            connect.start();
//                            bluetoothListLayout.setVisibility(View.GONE);
                        }
                    })

                    // A null listener allows the button to dismiss the dialog and take no further action.
                    .setNegativeButton(android.R.string.no, null)
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .show();


        }
    };

    private void setupToolbar() {

        toolbar = (Toolbar) findViewById(R.id.toolbarsignup);
        txt_title = (TextView) findViewById(R.id.txt_title);
        addPwdToQR = findViewById(R.id.btn_add_pwd);
        edtPwd = findViewById(R.id.edt_pwd);
        qrImage = findViewById(R.id.qrImage);
        checkInWithBLE = findViewById(R.id.checkInWithBLE);
//        send_data = (EditText) findViewById(R.id.send_data);
//        acceptThread = (Button) findViewById(R.id.acceptThread);
//        connectRF = (Button) findViewById(R.id.connectRF);
        sendbtnmsg = (Button) findViewById(R.id.sendbtnmsg);
        bluetoothListLayout = (LinearLayout) findViewById(R.id.bluetoothListLayout);

        setSupportActionBar(toolbar);

        txt_title.setText("TOUCHFREE CHECKIN");

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        toolbar.setNavigationIcon(R.drawable.back_1);

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        userData = Global.INSTANCE.getUserMe(preferenceHelper);
        password = userData.getApp_usr_password();
        if (!password.equals("")) {
            try {
                String decryptPassword = AESPasswordAlgo.decrypt(password);
                Log.e("decryptedPassword", "" + decryptPassword);
                edtPwd.setText(decryptPassword);
            } catch (Exception e) {
                e.printStackTrace();
            }

        }

        runnable = new Runnable() {
            @Override
            public void run() {
                if (isGeneratedOnce) {
                    generateQRCodeData();
                }
            }
        };

        addPwdToQR.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String getEnteredPasswordText = edtPwd.getText().toString().trim();
                if (getEnteredPasswordText != password) {
                    try {
                        String encryptPassword = AESPasswordAlgo.encrypt(getEnteredPasswordText);
                        Log.e("encryptedPassword", "" + encryptPassword);
                        userData.setApp_usr_password(encryptPassword);
                        saveUserProfile(userData);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        });

        generateQRCodeData();

    }


    private void generateQRCodeData() {
        String name = Global.INSTANCE.getUserMe(preferenceHelper).getApp_usr_name();
        String mobile = Global.INSTANCE.getUserMe(preferenceHelper).getApp_usr_mobile();
        String timeStamp = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.getDefault()).format(new Date());
        String password = edtPwd.getText().toString().trim();
        try {
            jsonObject = new JSONObject();
            jsonObject.put("name", name);
            jsonObject.put("mobile_number", mobile);
            jsonObject.put("time", timeStamp);
            jsonObject.put("password", password);
            generatedText = jsonObject.toString();
        } catch (Exception e) {
            Log.e("exceptionJson", e.getMessage());
        }

        try {
            encryptedText = AESQRAlgorithm.encrypt(generatedText);
            Log.e("textEncrypt", "" + encryptedText);
            Log.e("textDecrypy", "" + AESQRAlgorithm.decrypt(encryptedText));
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (!encryptedText.isEmpty()) {
            generatedText = encryptedText;
        }

        generateQRCode(generatedText);

    }


    private void generateQRCode(String generatedText) {
        WindowManager manager = (WindowManager) getSystemService(WINDOW_SERVICE);
        Display display = manager.getDefaultDisplay();
        Point point = new Point();
        display.getSize(point);
        int width = point.x;
        int height = point.y;
        int smallerDimension = width < height ? width : height;
        smallerDimension = smallerDimension * 3 / 4;

        qrgEncoder = new QRGEncoder(
                generatedText, null,
                QRGContents.Type.TEXT,
                smallerDimension);
        qrgEncoder.setColorBlack(Color.BLACK);
        qrgEncoder.setColorWhite(Color.WHITE);
        try {
            bitmap = qrgEncoder.getBitmap();
            qrImage.setImageBitmap(bitmap);

            if (!isGeneratedOnce)
                isGeneratedOnce = true;

            handler.postDelayed(runnable, 30000);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }


    private void saveUserProfile(final UserData userData) {
        progressDialog.show();
        Gson gson = new Gson();
        String jsonDATA = gson.toJson(userData);
        Log.e("data", "" + jsonDATA);
        Call<ResponseBody> responseBodyCall = adminAPI.UpdateUserDetails(jsonDATA);
        responseBodyCall.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                progressDialog.hide();
                if (response.code() == 200) {
                    Global.INSTANCE.setUserMe(userData, preferenceHelper);
                    generateQRCodeData();
                } else {
                    Global.INSTANCE.displayToastMessage(getString(R.string.message_something_went_wrong), getApplicationContext());
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                progressDialog.hide();
                Global.INSTANCE.displayToastMessage(getString(R.string.message_something_went_wrong), getApplicationContext());
            }
        });
    }

    @Override
    public void onBackPressed() {
        handler.removeCallbacks(runnable);
        super.onBackPressed();
    }

    @Override
    protected void onDestroy() {
        handler.removeCallbacks(runnable);
        super.onDestroy();
    }
}
