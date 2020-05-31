package com.gifola;

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

import com.gifola.constans.AESenc;
import com.gifola.constans.Global;
import com.gifola.constans.SharedPreferenceHelper;
import com.gifola.customfonts.MyEditText;
import com.gifola.customfonts.MyTextViewMedium;

import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;


import androidmads.library.qrgenearator.QRGContents;
import androidmads.library.qrgenearator.QRGEncoder;

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
    private Handler handler = new Handler();
    Runnable runnable;
    Boolean isGeneratedOnce = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_q_r_scan);
        preferenceHelper = new SharedPreferenceHelper(this);
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

        toolbar.setNavigationIcon(R.drawable.back_1);

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

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
                generateQRCodeData();
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
            encryptedText = AESenc.encrypt(generatedText);
            Log.e("textEncrypt", "" + encryptedText);
            Log.e("textDecrypy", "" + AESenc.decrypt(encryptedText));
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
