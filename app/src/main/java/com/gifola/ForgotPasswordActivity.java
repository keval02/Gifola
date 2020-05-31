package com.gifola;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.LinearLayout;

import com.gifola.customfonts.MyEditText;
import com.gifola.customfonts.MyTextViewMedium;
import com.gifola.timer.CircularTimerView;

public class ForgotPasswordActivity extends AppCompatActivity {

    Button btnReset,btnResend;
    CircularTimerView progressBar;
    LinearLayout secondlayout;
    MyTextViewMedium timertext;
    CardView btn_login;
    MyTextViewMedium resend;

    MyEditText edit_mobileno,edit_password,edit_confirmpassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);
        fetchid();



    }

    private void fetchid() {
        edit_mobileno=findViewById(R.id.edit_mobileno);
        edit_password=findViewById(R.id.edit_password);
        edit_confirmpassword=findViewById(R.id.edit_confirmpassword);

        timertext=findViewById(R.id.timertext);
        btn_login=findViewById(R.id.btn_login);
        resend=findViewById(R.id.resend);

        btn_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(getApplicationContext(),OTPActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
//                timertext.setVisibility(View.VISIBLE);
//                resend.setVisibility(View.GONE);
//                        new CountDownTimer(30000, 1000) {
////                new CountDownTimer(3000, 1000) {
//
//                    public void onTick(long millisUntilFinished) {
//                        timertext.setText("Seconds Remaining: " + millisUntilFinished / 1000);
////                        timertext.setText("" + millisUntilFinished / 1000);
//                    }
//
//                    public void onFinish() {
//                        timertext.setText("Time Out!");
//                        resend.setVisibility(View.VISIBLE);
//                    }
//                }.start();
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
