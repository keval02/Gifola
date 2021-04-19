package com.gifola;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.gifola.apis.AdminAPI;
import com.gifola.apis.ApiURLs;
import com.gifola.apis.SeriveGenerator;
import com.gifola.apis.ServiceHandler;
import com.gifola.constans.Global;
import com.gifola.constans.SharedPreferenceHelper;
import com.gifola.customfonts.MyTextViewMedium;
import com.gifola.model.UserData;
import com.gifola.model.UserDataModel;
import com.google.gson.Gson;

public class SignupActivity extends AppCompatActivity {

    Button btnReset, btnResend;
    ProgressDialog progressBar;
    LinearLayout secondlayout;
    MyTextViewMedium timertext;
    CardView btn_login;
    MyTextViewMedium resend, txt_forget_password, txt_signup;
    EditText edtMobileNo;
    AdminAPI adminAPI;
    SharedPreferenceHelper preference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);
        timertext = findViewById(R.id.timertext);
        btn_login = findViewById(R.id.btn_login);
        resend = findViewById(R.id.resend);
        txt_forget_password = findViewById(R.id.txt_forget_password);
        txt_signup = findViewById(R.id.txt_signup);
        edtMobileNo = findViewById(R.id.edt_mobile_no);
        adminAPI = SeriveGenerator.getAPIClass();
        preference = new SharedPreferenceHelper(getApplicationContext());
        progressBar = new ProgressDialog(this);



        btn_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String enteredMobileNumber = edtMobileNo.getText().toString().trim();
                if (edtMobileNo.length() != 10) {
                    Global.INSTANCE.displayToastMessage(getString(R.string.message_valid_mobile_num), getApplicationContext());
                } else {
                    new CheckUserMobileNumber(enteredMobileNumber).execute();
                }
            }
        });

        resend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                timertext.setVisibility(View.VISIBLE);
                resend.setVisibility(View.GONE);
                new CountDownTimer(30000, 1000) {

                    public void onTick(long millisUntilFinished) {
                        timertext.setText("seconds remaining: " + millisUntilFinished / 1000);
                    }

                    public void onFinish() {
                        timertext.setText("Time Out!");
                        resend.setVisibility(View.VISIBLE);
                    }
                }.start();
            }
        });

        txt_forget_password.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), ForgotPasswordActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
            }
        });

        txt_signup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
            }
        });
    }


    public static void hideKeyboard(Activity activity) {
        InputMethodManager imm = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        //Find the currently focused view, so we can grab the correct window token from it.
        View view = activity.getCurrentFocus();
        //If no view currently has focus, create a new one, just so we can grab a window token from it
        if (view == null) {
            view = new View(activity);
        }
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    class CheckUserMobileNumber extends AsyncTask<Void, Void, String> {
        String mobileNo = "";

        public CheckUserMobileNumber(String mobileNo) {
            this.mobileNo = mobileNo;
        }

        @Override
        protected void onPreExecute() {
            progressBar.show();
            super.onPreExecute();

        }

        @Override
        protected String doInBackground(Void... voids) {
            String json = "";
            try {
                String postURL = ApiURLs.CHECK_USERS_MOBILE_NO  +  mobileNo;
                json = new ServiceHandler().makeServiceCall(ApiURLs.BASE_URL + postURL, ServiceHandler.GET);
            } catch (Exception e) {
                Log.e("exception", e.getMessage());
            }

            return json;
        }

        @Override
        protected void onPostExecute(String result) {
            progressBar.hide();
            super.onPostExecute(result);
            try {
                if (result == null || result.isEmpty()) {
                    Global.INSTANCE.displayToastMessage(getString(R.string.message_something_went_wrong), getApplicationContext());
                } else {
                    String jsonResponse = result;
                    Intent intent = new Intent(getApplicationContext(), OTPActivity.class);
                    intent.putExtra(Global.INSTANCE.getMobileNumberText(), mobileNo);
                    if (!jsonResponse.startsWith("{")) {
                        Gson gson = new Gson();
                        UserDataModel dataModel = gson.fromJson(jsonResponse, UserDataModel.class);
                        UserData userData = dataModel.get(0);
                        Global.INSTANCE.setUserMe(userData, preference);
                        intent.putExtra(Global.INSTANCE.isAlreadyRegistered(), true);
                        intent.putExtra(Global.INSTANCE.getUserID(), userData.getApp_usr_id());
                    }
                    startActivity(intent);

                }
            } catch (Exception e) {
                Log.e("exception", e.getMessage());
            }
        }
    }
}
