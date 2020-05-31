package com.gifola;

import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Point;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

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

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import androidmads.library.qrgenearator.QRGContents;
import androidmads.library.qrgenearator.QRGEncoder;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class QRScanActivity extends AppCompatActivity {
    Toolbar toolbar;
    TextView txt_title;
    private QRGEncoder qrgEncoder;
    private Bitmap bitmap;
    ImageView qrImage;
    SharedPreferenceHelper preferenceHelper;
    MyTextViewMedium addPwdToQR;
    MyEditText edtPwd;
    JSONObject jsonObject;
    String encryptedText = "";
    String generatedText = "";
    String password = "";
    private Handler handler = new Handler();
    Runnable runnable;
    Boolean isGeneratedOnce = false;
    UserData userData;
    AdminAPI adminAPI;
    ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_q_r_scan);
        preferenceHelper = new SharedPreferenceHelper(this);
        adminAPI = SeriveGenerator.getAPIClass();
        progressDialog = new ProgressDialog(this);
        setupToolbar();
    }

    private void setupToolbar() {

        toolbar = (Toolbar) findViewById(R.id.toolbarsignup);
        txt_title = (TextView) findViewById(R.id.txt_title);
        addPwdToQR = findViewById(R.id.btn_add_pwd);
        edtPwd = findViewById(R.id.edt_pwd);
        qrImage = findViewById(R.id.qrImage);

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
                Log.e("decryptedPassword" , ""+ decryptPassword);
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
                        Log.e("encryptedPassword" , ""+ encryptPassword);
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
        Log.e("data" , "" + jsonDATA);
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
