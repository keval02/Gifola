package com.gifola;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.gifola.customfonts.MyEditText;

public class ContactUsActivity extends AppCompatActivity {
    CardView sendcard;
    Toolbar toolbar;
    TextView txt_title;

    MyEditText edit_to,edit_subject,edit_message;
    LinearLayout btn_back,btn_send;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact_us);
        setupToolbar();
        fetchid();
        sendcard=findViewById(R.id.sendcard);
        sendcard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), DashboardActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
            }
        });
    }

    private void fetchid() {
        edit_to=findViewById(R.id.edit_to);
        edit_subject=findViewById(R.id.edit_subject);
        edit_message=findViewById(R.id.edit_message);
        btn_back=findViewById(R.id.btn_back);
        btn_send=findViewById(R.id.btn_send);
    }

    private void setupToolbar() {

        toolbar = (Toolbar) findViewById(R.id.toolbarsignup);
        txt_title = (TextView) findViewById(R.id.txt_title);

        setSupportActionBar(toolbar);

        txt_title.setText("Contact Us");

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
