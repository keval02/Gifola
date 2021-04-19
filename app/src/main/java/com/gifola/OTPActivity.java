package com.gifola;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
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
import com.gifola.model.UserDataModel;
import com.gifola.timer.CircularTimerListener;
import com.gifola.timer.CircularTimerView;
import com.gifola.timer.TimeFormatEnum;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.phone.SmsRetriever;
import com.google.android.gms.auth.api.phone.SmsRetrieverClient;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.google.gson.Gson;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import com.gifola.helper.OtpEditText;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import java.util.ArrayList;
import java.util.List;

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
    AdminAPI smsAPI;
    SharedPreferenceHelper preferenceHelper;
    Boolean isAlreadyRegistered = false;
    OtpEditText et_otp;
    String token = "";
    Integer userId = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_o_t_p);
        progressBar = findViewById(R.id.progress_circular);
        resendtxt = findViewById(R.id.resendtxt);
        resendlayout = findViewById(R.id.resendlayout);
        changenumber = findViewById(R.id.changenumber);
        mobileNumberTextView = findViewById(R.id.emailmobile);
        et_otp = findViewById(R.id.et_otp);
        mobileNum = getIntent().getStringExtra(Global.INSTANCE.getMobileNumberText());
        isAlreadyRegistered = getIntent().getBooleanExtra(Global.INSTANCE.isAlreadyRegistered() , false);
        if(isAlreadyRegistered){
            userId = getIntent().getIntExtra(Global.INSTANCE.getUserID(), 0);
        }
        preferenceHelper = new SharedPreferenceHelper(getApplicationContext());
        adminAPI = SeriveGenerator.getAPIClass();
        smsAPI = SeriveGenerator.getSMSAPIClass();
        bar = new ProgressDialog(this);
        if (!mobileNum.equals("")) {
            mobileNumberTextView.setText("send to +91" + mobileNum);
        }

        FirebaseInstanceId.getInstance().getInstanceId().addOnCompleteListener(new OnCompleteListener<InstanceIdResult>() {
            @Override
            public void onComplete(@NonNull Task<InstanceIdResult> task) {
                token = task.getResult().getToken();
            }
        });

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
        }, 90, TimeFormatEnum.SECONDS, 90);

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
        et_otp.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                if(et_otp.getText().length() == 4){
                    String otp = et_otp.getText().toString().trim();
                    if (generatedOTP.equals(otp)) {
                        if(isAlreadyRegistered){
                            bar.show();
                            UpdateUserToken(false);
                        }else {
                            SendUserMobileNumber(mobileNum);
                        }

                    }
                }
            }
        });

        smsListener();
        sendOTPRequest();

    }

    private void sendOTPRequest() {
        SimpleOTPGenerator simpleOTPGenerator = new SimpleOTPGenerator();
        generatedOTP = simpleOTPGenerator.random(4);
        String message = generatedOTP + Global.INSTANCE.getSmsText();

        Log.e("mobileNumber" , mobileNum);
        SendSMSToUser("91" + mobileNum, generatedOTP);
        //new SendOTPToUser(mobileNum, message, generatedOTP).execute();
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
        et_otp.setText(otp);
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
            /*builder.scheme("http")
                    .authority("sms.bulksmsserviceproviders.com")
                    .appendPath("api")
                    .appendPath("send_http.php")
                    .appendQueryParameter("authkey", "395d698295167ab3c06cfeabaccb8829")
                    .appendQueryParameter("mobiles", mobileNumber)
                    .appendQueryParameter("route", "B")
                    .appendQueryParameter("sender", "GIFOLA")
                    .appendQueryParameter("message", otpMessage);*/

            builder.scheme("http")
                    .authority("api.msg91.com")
                    .appendPath("api")
                    .appendPath("v5")
                    .appendPath("otp")
                    .appendQueryParameter("authkey", "292349A2F1YiBtk5d6e2ab0")
                    .appendQueryParameter("template_id", "5ef44d32d6fc050a177e90c7")
                    .appendQueryParameter("extra_param", "{%22COMPANY_NAME%22:%22GIFOLA%20TECH%22}")
                    .appendQueryParameter("mobile", mobileNumber)
                    .appendQueryParameter("invisible", "1")
                    .appendQueryParameter("otp", otp);

            Log.e("sendOtp" , builder.build().toString());
            String apiURL = "https://api.msg91.com/api/v5/flow/?authkey=292349A2F1YiBtk5d6e2ab0";
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("flow_id" , "6066d07b366b7638164ff3f7");
            jsonObject.addProperty("sender" , "GIFOLA");
            JsonArray jsonArray = new JsonArray();
            JsonObject arrayObject = new JsonObject();
            arrayObject.addProperty("mobiles" , mobileNumber);
            arrayObject.addProperty("OTP" , otp);
            arrayObject.addProperty("PlayCode" , "2abc567");
            jsonArray.add(arrayObject);
            jsonObject.add("recipients" , jsonArray);


            List<NameValuePair> parameters = new ArrayList<NameValuePair>();
            parameters.add(new BasicNameValuePair("",jsonObject.toString()));



            String json = "";

            try {
                json = new ServiceHandler().makeServiceCall(builder.build().toString(), ServiceHandler.GET);
               // json = new ServiceHandler().makeServiceCall(apiURL, ServiceHandler.POST, parameters);
            } catch (Exception e) {
                Log.e("exception", e.getMessage());
            }


            return json;
        }

        @Override
        protected void onPostExecute(String s) {
            Log.e("resposne" , s);
            super.onPostExecute(s);


        }
    }

    void SendSMSToUser(String mobileNum, String otp) {
        bar.show();
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("flow_id" , "6066d07b366b7638164ff3f7");
        jsonObject.addProperty("sender" , "GIFOLA");
        JsonArray jsonArray = new JsonArray();
        JsonObject arrayObject = new JsonObject();
        arrayObject.addProperty("mobiles" , mobileNum);
        arrayObject.addProperty("OTP" , otp);
        arrayObject.addProperty("PlayCode" , "2gc81RJaZG6");
        jsonArray.add(arrayObject);
        jsonObject.add("recipients" , jsonArray);

        Call<ResponseBody> responseBodyCall = smsAPI.SendSMS("292349A2F1YiBtk5d6e2ab0", jsonObject);
        responseBodyCall.enqueue(new Callback<ResponseBody>() {

            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                bar.hide();
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                bar.hide();
            }
        });
    }



    void SendUserMobileNumber(String mobileNum) {
        bar.show();
        final UserData userData = new UserData();
        userData.setIsactive(true);
        userData.setApp_usr_mobile(mobileNum);
        Gson gson = new Gson();
        String jsonDATA = gson.toJson(userData);
        Call<UserData> responseBodyCall = adminAPI.RegisterUserMobileNumber(userData.getApp_usr_mobile()/*, token*/);
        responseBodyCall.enqueue(new Callback<UserData>() {
            @Override
            public void onResponse(Call<UserData> call, Response<UserData> response) {
                if (response.code() == 200) {
                    Global.INSTANCE.setUserMe(userData, preferenceHelper);
                    userId = response.body().getApp_usr_id();
                    UpdateUserToken(true);
                } else {
                    bar.hide();
                    Global.INSTANCE.displayToastMessage(getString(R.string.message_something_went_wrong), getApplicationContext());
                }
            }

            @Override
            public void onFailure(Call<UserData> call, Throwable t) {
                bar.hide();
                Global.INSTANCE.displayToastMessage(getString(R.string.message_something_went_wrong), getApplicationContext());
            }
        });
    }

    void UpdateUserToken(final Boolean isForFirstTime) {
        Call<ResponseBody> responseBodyCall = adminAPI.UpdateUserToken(userId, token);
        responseBodyCall.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                bar.hide();
                if (response.code() == 200) {
                    preferenceHelper.setBoolean(Global.INSTANCE.isLoggedIn(), true);
                    if(isForFirstTime){
                        preferenceHelper.setBoolean(Global.INSTANCE.isEnteredFirstTime(), true);
                    }
                    Intent intent = new Intent(getApplicationContext(), DashboardActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                    finishAffinity();
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
