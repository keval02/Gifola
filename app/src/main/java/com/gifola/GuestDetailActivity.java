package com.gifola;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;

import com.gifola.customfonts.MyTextView;
import com.gifola.customfonts.MyTextViewBold;
import com.gifola.customfonts.MyTextViewMedium;
import com.pkmmte.view.CircularImageView;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class GuestDetailActivity extends AppCompatActivity {
    Toolbar toolbar;
    TextView txt_title;
    String member[] = {"Select Location","Home","Office"};
    CardView card1,card2,card3;
    MyTextViewBold text1,text2,text3;
    LinearLayout schedulelayout,purposelayout;
    CardView sendcard,backcard;
    Calendar myCalendar;
    MyTextViewMedium date1,date2,time1,time2;
//    Date date;

    CircularImageView profile_image;
    MyTextViewBold text_name;
    MyTextView text_mobileno;
    ImageView img_fav;
    LinearLayout btn_back,btn_send;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_permission_detail);
        setupToolbar();
        fetchid();

    }

    private void fetchid() {

        profile_image=findViewById(R.id.profile_image);
        text_name=findViewById(R.id.text_name);
        text_mobileno=findViewById(R.id.text_mobileno);
        img_fav=findViewById(R.id.img_fav);
        btn_back=findViewById(R.id.btn_back);
        btn_send=findViewById(R.id.btn_send);

        // Selection of the spinner
        Spinner spinner = (Spinner) findViewById(R.id.spinner2);
        card1=findViewById(R.id.card1);
        card2=findViewById(R.id.card2);
        card3=findViewById(R.id.card3);
        text1=findViewById(R.id.text1);
        text2=findViewById(R.id.text2);
        text3=findViewById(R.id.text3);
        schedulelayout=findViewById(R.id.schedulelayout);
        sendcard=findViewById(R.id.sendcard);
        backcard=findViewById(R.id.backcard);
        date1=findViewById(R.id.date1);
        date2=findViewById(R.id.date2);
        time2=findViewById(R.id.time2);
        time1=findViewById(R.id.time1);
        purposelayout=findViewById(R.id.purposelayout);

        myCalendar = Calendar.getInstance();
        final DatePickerDialog.OnDateSetListener date = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear,
                                  int dayOfMonth) {
                // TODO Auto-generated method stub
                myCalendar.set(Calendar.YEAR, year);
                myCalendar.set(Calendar.MONTH, monthOfYear);
                myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
//                updateLabel();
            }

        };

        date1.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                new DatePickerDialog(GuestDetailActivity.this, date, myCalendar
                        .get(Calendar.YEAR), myCalendar.get(Calendar.MONTH),
                        myCalendar.get(Calendar.DAY_OF_MONTH)).show();
            }
        });


        date2.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                new DatePickerDialog(GuestDetailActivity.this, date, myCalendar
                        .get(Calendar.YEAR), myCalendar.get(Calendar.MONTH),
                        myCalendar.get(Calendar.DAY_OF_MONTH)).show();
            }
        });

        time1.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                Calendar mcurrentTime = Calendar.getInstance();
                int hour = mcurrentTime.get(Calendar.HOUR_OF_DAY);
                int minute = mcurrentTime.get(Calendar.MINUTE);
                TimePickerDialog mTimePicker;
                mTimePicker = new TimePickerDialog(GuestDetailActivity.this, new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker timePicker, int selectedHour, int selectedMinute) {
                        //   time1.setText( selectedHour + ":" + selectedMinute);
                    }
                }, hour, minute, true);//Yes 24 hour time
                mTimePicker.setTitle("Select Time");
                mTimePicker.show();

            }
        });

        time2.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                Calendar mcurrentTime = Calendar.getInstance();
                int hour = mcurrentTime.get(Calendar.HOUR_OF_DAY);
                int minute = mcurrentTime.get(Calendar.MINUTE);
                TimePickerDialog mTimePicker;
                mTimePicker = new TimePickerDialog(GuestDetailActivity.this, new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker timePicker, int selectedHour, int selectedMinute) {
                        //   time1.setText( selectedHour + ":" + selectedMinute);
                    }
                }, hour, minute, true);//Yes 24 hour time
                mTimePicker.setTitle("Select Time");
                mTimePicker.show();

            }
        });


        backcard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        sendcard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(getApplicationContext(),DashboardActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
            }
        });

        card1.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("ResourceAsColor")
            @Override
            public void onClick(View v) {
                card1.setCardBackgroundColor(Color.parseColor("#2196F3"));
                card2.setCardBackgroundColor(Color.parseColor("#ffffff"));
                card3.setCardBackgroundColor(Color.parseColor("#ffffff"));
                text1.setTextColor(Color.parseColor("#ffffff"));
                text2.setTextColor(Color.parseColor("#444444"));
                text3.setTextColor(Color.parseColor("#444444"));
                schedulelayout.setVisibility(View.GONE);
                purposelayout.setVisibility(View.GONE);

            }
        });

        card2.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("ResourceAsColor")
            @Override
            public void onClick(View v) {
                card1.setCardBackgroundColor(Color.parseColor("#ffffff"));
                card2.setCardBackgroundColor(Color.parseColor("#2196F3"));
                card3.setCardBackgroundColor(Color.parseColor("#ffffff"));
                text1.setTextColor(Color.parseColor("#444444"));
                text2.setTextColor(Color.parseColor("#ffffff"));
                text3.setTextColor(Color.parseColor("#444444"));
                schedulelayout.setVisibility(View.VISIBLE);
                purposelayout.setVisibility(View.GONE);

            }
        });


        card3.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("ResourceAsColor")
            @Override
            public void onClick(View v) {
                card1.setCardBackgroundColor(Color.parseColor("#ffffff"));
                card2.setCardBackgroundColor(Color.parseColor("#ffffff"));
                card3.setCardBackgroundColor(Color.parseColor("#2196F3"));
                text1.setTextColor(Color.parseColor("#444444"));
                text2.setTextColor(Color.parseColor("#444444"));
                text3.setTextColor(Color.parseColor("#ffffff"));
                schedulelayout.setVisibility(View.GONE);
                purposelayout.setVisibility(View.VISIBLE);

            }
        });

// Application of the Array to the Spinner
        ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<String>(this,   android.R.layout.simple_spinner_item, member);
        spinnerArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item); // The drop down view
        spinner.setAdapter(spinnerArrayAdapter);
    }

    private void setupToolbar() {

        toolbar = (Toolbar) findViewById(R.id.toolbarsignup);
        txt_title = (TextView) findViewById(R.id.txt_title);

        setSupportActionBar(toolbar);

        txt_title.setText("Guest Details");

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

    private void updateLabel() {
        String myFormat = "MM/dd/yy"; //In which you need put here
        SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.US);

        date1.setText(sdf.format(myCalendar.getTime()));
    }
}
