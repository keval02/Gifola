package com.gifola;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.gifola.apis.AdminAPI;
import com.gifola.apis.ApiURLs;
import com.gifola.apis.SeriveGenerator;
import com.gifola.apis.ServiceHandler;
import com.gifola.constans.GetOtpInterface;
import com.gifola.constans.Global;
import com.gifola.constans.SharedPreferenceHelper;
import com.gifola.customfonts.MyTextView;
import com.gifola.customfonts.MyTextViewMedium;
import com.gifola.helper.MySMSBroadCastReceiver;
import com.gifola.helper.SimpleOTPGenerator;
import com.gifola.helper.SmsHashCodeHelper;
import com.gifola.model.UserData;
import com.gifola.timer.CircularTimerListener;
import com.gifola.timer.CircularTimerView;
import com.gifola.timer.TimeFormatEnum;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.phone.SmsRetriever;
import com.google.android.gms.auth.api.phone.SmsRetrieverClient;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.gson.Gson;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import java.util.ArrayList;
import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class OTPActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks,
        GetOtpInterface, GoogleApiClient.OnConnectionFailedListener {
    CircularTimerView progressBar;
    MyTextViewMedium resendtxt, changenumber;
    MyTextView mobileNumberTextView;
    LinearLayout resendlayout;
    String mobileNum = "";
    GoogleApiClient mGoogleApiClient;
    MySMSBroadCastReceiver mySMSBroadCastReceiver;
    private int RESOLVE_HINT = 2;
    String generatedOTP = "";
    ProgressDialog bar;
    AdminAPI adminAPI;
    SharedPreferenceHelper preferenceHelper;
    Boolean isAlreadyRegistered = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_o_t_p);
        progressBar = findViewById(R.id.progress_circular);
        resendtxt = findViewById(R.id.resendtxt);
        resendlayout = findViewById(R.id.resendlayout);
        changenumber = findViewById(R.id.changenumber);
        mobileNumberTextView = findViewById(R.id.emailmobile);
        mobileNum = getIntent().getStringExtra(Global.INSTANCE.getMobileNumberText());
        isAlreadyRegistered = getIntent().getBooleanExtra(Global.INSTANCE.isAlreadyRegistered() , false);
        preferenceHelper = new SharedPreferenceHelper(getApplicationContext());
        adminAPI = SeriveGenerator.getAPIClass();
        bar = new ProgressDialog(this);
        if (!mobileNum.equals("")) {
            mobileNumberTextView.setText("send to +91" + mobileNum);
        }

        SmsHashCodeHelper smsHashCodeHelper = new SmsHashCodeHelper(this);
        smsHashCodeHelper.getAppHashCode();
        mySMSBroadCastReceiver = new MySMSBroadCastReceiver();

        //set google api client for hint request
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .enableAutoManage(this, this)
                .addApi(Auth.CREDENTIALS_API)
                .build();
        mySMSBroadCastReceiver.setOnOtpListeners(this);
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(SmsRetriever.SMS_RETRIEVED_ACTION);
        getApplicationContext().registerReceiver(mySMSBroadCastReceiver, intentFilter);//        progressBar.setStrokeWidthDimension(100); 		// set stroke width
//        progressBar.setBackgroundWidth(100);


        progressBar.setCircularTimerListener(new CircularTimerListener() {
            @Override
            public String updateDataOnTick(long remainingTimeInMs) {
                return String.valueOf((int) Math.ceil((remainingTimeInMs / 1000.f)));
            }

            @Override
            public void onTimerFinished() {
//                Toast.makeText(LoginActivity.this, "FINISHED", Toast.LENGTH_SHORT).show();
                progressBar.setPrefix("");
                progressBar.setSuffix("");
                progressBar.setText("TimeOut");
                resendlayout.setVisibility(View.VISIBLE);
            }
        }, 30, TimeFormatEnum.SECONDS, 30);

        progressBar.setProgress(0);
        progressBar.startTimer();
        resendlayout.setVisibility(View.INVISIBLE);

        resendtxt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                progressBar.setProgress(0);
                progressBar.startTimer();
                progressBar.setVisibility(View.VISIBLE);
                resendlayout.setVisibility(View.INVISIBLE);
                sendOTPRequest();
            }
        });

        changenumber.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });


        smsListener();
        sendOTPRequest();

    }

    private void sendOTPRequest() {
        SimpleOTPGenerator simpleOTPGenerator = new SimpleOTPGenerator();
        generatedOTP = simpleOTPGenerator.random(4);
        String message = generatedOTP + Global.INSTANCE.getSmsText();

        new SendOTPToUser(mobileNum, message, generatedOTP).execute();
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onOtpReceived(String otp) {
        if (generatedOTP.equals(otp)) {
            if(isAlreadyRegistered){
                preferenceHelper.setBoolean(Global.INSTANCE.isLoggedIn(), true);
                Intent intent = new Intent(getApplicationContext(), DashboardActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                finish();
            }else {
                SendUserMobileNumber(mobileNum);
            }

        } else {
            Global.INSTANCE.displayToastMessage(getString(R.string.resend_otp), getApplicationContext());
        }
    }

    @Override
    public void onOtpTimeout() {
        Toast.makeText(this, "Time out, please resend", Toast.LENGTH_LONG).show();
    }

    private class SendOTPToUser extends AsyncTask<Void, Void, String> {

        String mobileNumber = "";
        String message = "";
        String otp = "";

        public SendOTPToUser(String mobileNumber, String message, String otp) {
            this.mobileNumber = mobileNumber;
            this.message = message;
            this.otp = otp;
        }

        @Override
        protected String doInBackground(Void... voids) {
            String otpMessage = "# Your otp code is : " + otp + " 2gc81RJaZG6.";
            Uri.Builder builder = new Uri.Builder();
            builder.scheme("http")
                    .authority("sms.bulksmsserviceproviders.com")
                    .appendPath("api")
                    .appendPath("send_http.php")
                    .appendQueryParameter("authkey", "395d698295167ab3c06cfeabaccb8829")
                    .appendQueryParameter("mobiles", mobileNumber)
                    .appendQueryParameter("route", "B")
                    .appendQueryParameter("sender", "GIFOLA")
                    .appendQueryParameter("message", otpMessage);


            String json = "";

            try {
                json = new ServiceHandler().makeServiceCall(builder.build().toString(), ServiceHandler.GET);
            } catch (Exception e) {
                Log.e("exception", e.getMessage());
            }


            return json;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);


        }
    }

    void SendUserMobileNumber(String mobileNum) {
        bar.show();
        final UserData userData = new UserData();
        userData.setApp_usr_id(100);
        userData.setIsactive(true);
        userData.setApp_usr_mobile(mobileNum);
        Gson gson = new Gson();
        String jsonDATA = gson.toJson(userData);
        Call<ResponseBody> responseBodyCall = adminAPI.RegisterUserMobileNumber(jsonDATA);
        responseBodyCall.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                bar.hide();
                if (response.code() == 200) {
                    Global.INSTANCE.setUserMe(userData, preferenceHelper);
                    preferenceHelper.setBoolean(Global.INSTANCE.isLoggedIn(), true);
                    preferenceHelper.setBoolean(Global.INSTANCE.isEnteredFirstTime(), true);
                    Intent intent = new Intent(getApplicationContext(), DashboardActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                    finish();
                } else {
                    Global.INSTANCE.displayToastMessage(getString(R.string.message_something_went_wrong), getApplicationContext());
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                bar.hide();
                Global.INSTANCE.displayToastMessage(getString(R.string.message_something_went_wrong), getApplicationContext());
            }
        });
    }

    public void smsListener() {
        SmsRetrieverClient mClient = SmsRetriever.getClient(this);
        Task mTask = mClient.startSmsRetriever();
        mTask.addOnSuccessListener(new OnSuccessListener() {
            @Override
            public void onSuccess(Object o) {
            }
        });
        mTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
            }
        });
    }
}
