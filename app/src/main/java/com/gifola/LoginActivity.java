package com.gifola;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.dhims.timerview.TimerTextView;
import com.gifola.customfonts.MyEditText;
import com.gifola.customfonts.MyTextViewMedium;
import com.gifola.timer.CircularTimerListener;
import com.gifola.timer.CircularTimerView;
import com.gifola.timer.TimeFormatEnum;


public class LoginActivity extends AppCompatActivity {
    Button btnReset,btnResend;
    CircularTimerView progressBar;
    LinearLayout secondlayout;
    MyTextViewMedium timertext;
    CardView btn_login;
    MyTextViewMedium resend,txt_forget_password,txt_signup;

    MyEditText edit_mobileno,edit_password;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        fetchid();




    }

    private void fetchid() {
        edit_mobileno=findViewById(R.id.edit_mobileno);
        edit_password=findViewById(R.id.edit_password);

        timertext=findViewById(R.id.timertext);
        btn_login=findViewById(R.id.btn_login);
        resend=findViewById(R.id.resend);
        txt_forget_password=findViewById(R.id.txt_forget_password);
        txt_signup=findViewById(R.id.txt_signup);

//        btnReset = findViewById(R.id.btnRestart);
//        btnResend = findViewById(R.id.btnResend);
//        secondlayout = findViewById(R.id.secondlayout);
//        progressBar = findViewById(R.id.progress_circular);
//        progressBar.setProgress(0);


//        progressBar.setCircularTimerListener(new CircularTimerListener() {
//            @Override
//            public String updateDataOnTick(long remainingTimeInMs) {
//                return String.valueOf((int)Math.ceil((remainingTimeInMs / 1000.f)));
//            }
//
//            @Override
//            public void onTimerFinished() {
////                Toast.makeText(LoginActivity.this, "FINISHED", Toast.LENGTH_SHORT).show();
//                progressBar.setPrefix("");
//                progressBar.setSuffix("");
//                progressBar.setText("Time Out ...");
//                btnResend.setVisibility(View.VISIBLE);
//            }
//        }, 2, TimeFormatEnum.SECONDS, 2);


//        btnReset.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                progressBar.setProgress(0);
//                progressBar.startTimer();
//                progressBar.setVisibility(View.VISIBLE);
//                btnResend.setVisibility(View.GONE);
//                hideKeyboard(LoginActivity.this);
//                secondlayout.setVisibility(View.VISIBLE);
//            }
//        });
//
//        btnResend.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                progressBar.setProgress(0);
//                progressBar.startTimer();
//                btnResend.setVisibility(View.GONE);
//                hideKeyboard(LoginActivity.this);
////                Intent intent=new Intent(getApplicationContext(),DashboardActivity.class);
////                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
////                startActivity(intent);
//            }
//        });
////        long futureTimestamp = System.currentTimeMillis() + (10 * 60 * 60 * 1000);
//        long futureTimestamp = System.currentTimeMillis() + (30000);
//        TimerTextView timerText = (TimerTextView) findViewById(R.id.timerText);
//        timerText.setEndTime(futureTimestamp);
//        Log.e("futureTimestamp", String.valueOf(futureTimestamp));

        btn_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(getApplicationContext(),DashboardActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
            }
        });

        resend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                timertext.setVisibility(View.VISIBLE);
                resend.setVisibility(View.GONE);
                new CountDownTimer(30000, 1000) {
//                new CountDownTimer(3000, 1000) {

                    public void onTick(long millisUntilFinished) {
                        timertext.setText("seconds remaining: " + millisUntilFinished / 1000);
//                        timertext.setText("" + millisUntilFinished / 1000);
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
                Intent intent=new Intent(getApplicationContext(),ForgotPasswordActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
            }
        });

        txt_signup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(getApplicationContext(),SignupActivity.class);
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
}
