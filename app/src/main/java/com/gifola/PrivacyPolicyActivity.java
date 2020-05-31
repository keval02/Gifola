package com.gifola;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.suke.widget.SwitchButton;

public class PrivacyPolicyActivity extends AppCompatActivity {
    Toolbar toolbar;
    TextView txt_title;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_privacy_policy);
        setupToolbar();
//        final com.suke.widget.SwitchButton switchButton = (com.suke.widget.SwitchButton)
//                findViewById(R.id.switch_button);
//
//        switchButton.setChecked(true);
//        switchButton.isChecked();
//        switchButton.toggle();     //switch state
//        switchButton.toggle(false);//switch without animation
//        switchButton.setShadowEffect(true);//disable shadow effect
//        switchButton.setEnabled(false);//disable button
//        switchButton.setEnableEffect(false);//disable the switch animation
//        switchButton.setOnCheckedChangeListener(new SwitchButton.OnCheckedChangeListener() {
////            @SuppressLint("ResourceAsColor")
//            @Override
//            public void onCheckedChanged(SwitchButton view, boolean isChecked) {
//                //TODO do your job
//
//                Log.e("fff", String.valueOf(isChecked));
//
//                if(isChecked){
//                    switchButton.setBackgroundColor(ContextCompat.getColor(PrivacyPolicyActivity.this, R.color.green));//
//                }
//                else {
//                    switchButton.setBackgroundColor(ContextCompat.getColor(PrivacyPolicyActivity.this, R.color.blue));//
//                }
//            }
//        });
    }

    private void setupToolbar() {

        toolbar = (Toolbar) findViewById(R.id.toolbarsignup);
        txt_title = (TextView) findViewById(R.id.txt_title);

        setSupportActionBar(toolbar);

        txt_title.setText("My Privacy Settings");

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        toolbar.setNavigationIcon(R.drawable.back_1);

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
    }
}
