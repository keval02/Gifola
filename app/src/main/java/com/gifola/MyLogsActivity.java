package com.gifola;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.view.ContextThemeWrapper;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.TimePicker;

import com.gifola.customfonts.MyEditText;
import com.gifola.customfonts.MyTextViewMedium;
import com.gifola.customfonts.MyTextViewRegular;

import java.util.Calendar;

public class MyLogsActivity extends AppCompatActivity {
    LinearLayout sublayout,menuimg;
    Toolbar toolbar;
    TextView txt_title;
    Dialog dialog,dialog2;
    MyTextViewMedium date1,date2,time1,time2;
    Calendar myCalendar;
    CardView filtercard;
    ImageView infoclick;

    MyEditText edit_search;
    ImageView cancel;
    LinearLayout btn_back,btn_filter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_logs);

        setupToolbar();

        fetchid();

    }

    private void fetchid() {
        edit_search=findViewById(R.id.edit_search);
        cancel=findViewById(R.id.cancel);
        btn_back=findViewById(R.id.btn_back);
        btn_filter=findViewById(R.id.btn_filter);

        sublayout=findViewById(R.id.sublayout);
        menuimg=findViewById(R.id.menuimg);
        filtercard=findViewById(R.id.filtercard);

        infoclick=findViewById(R.id.infoclick);
        infoclick.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                InfoDialoge();
            }
        });

        filtercard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FilterDialoge();
            }
        });

        menuimg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(sublayout.getVisibility()==View.VISIBLE)
                {
                    sublayout.setVisibility(View.GONE);
                }
                else {
                    sublayout.setVisibility(View.VISIBLE);
                }
            }
        });
    }

    private void setupToolbar() {

        toolbar = (Toolbar) findViewById(R.id.toolbarsignup);
        txt_title = (TextView) findViewById(R.id.txt_title);

        setSupportActionBar(toolbar);

        txt_title.setText("My Logs");

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

    public void FilterDialoge()
    {
        dialog = new Dialog(new ContextThemeWrapper(this, R.style.AppTheme));
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(true);
        dialog.setContentView(R.layout.dialoge_filter);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);

//        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
//        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));

        MyTextViewRegular submit = (MyTextViewRegular) dialog.findViewById(R.id.submit);
        date1=dialog.findViewById(R.id.date1);
        date2=dialog.findViewById(R.id.date2);
        time2=dialog.findViewById(R.id.time2);
        time1=dialog.findViewById(R.id.time1);

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
                new DatePickerDialog(MyLogsActivity.this, date, myCalendar
                        .get(Calendar.YEAR), myCalendar.get(Calendar.MONTH),
                        myCalendar.get(Calendar.DAY_OF_MONTH)).show();
            }
        });


        date2.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                new DatePickerDialog(MyLogsActivity.this, date, myCalendar
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
                mTimePicker = new TimePickerDialog(MyLogsActivity.this, new TimePickerDialog.OnTimeSetListener() {
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
                mTimePicker = new TimePickerDialog(MyLogsActivity.this, new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker timePicker, int selectedHour, int selectedMinute) {
                        //   time1.setText( selectedHour + ":" + selectedMinute);
                    }
                }, hour, minute, true);//Yes 24 hour time
                mTimePicker.setTitle("Select Time");
                mTimePicker.show();

            }
        });

        ImageView img_no=(ImageView)dialog.findViewById(R.id.img_no);


        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();

            }
        });

        img_no.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();

            }
        });

        dialog.show();

    }


    public void InfoDialoge()
    {
        dialog2 = new Dialog(new ContextThemeWrapper(this, R.style.AppTheme));
        dialog2.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog2.setCancelable(true);
        dialog2.setContentView(R.layout.dialoge_info);
        dialog2.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog2.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);

//        dialog2.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
//        dialog2.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));

        MyTextViewRegular submit = (MyTextViewRegular) dialog2.findViewById(R.id.submit);

        ImageView img_no=(ImageView)dialog2.findViewById(R.id.img_no);


        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog2.dismiss();

            }
        });

        img_no.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog2.dismiss();

            }
        });

        dialog2.show();

    }
}
