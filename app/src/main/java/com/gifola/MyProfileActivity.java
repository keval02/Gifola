package com.gifola;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.DatePicker;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.DialogFragment;

import com.gifola.apis.AdminAPI;
import com.gifola.apis.SeriveGenerator;
import com.gifola.constans.Global;
import com.gifola.constans.SharedPreferenceHelper;
import com.gifola.customfonts.MyEditText;
import com.gifola.customfonts.MyTextView;
import com.gifola.customfonts.MyTextViewMedium;
import com.gifola.model.UserData;
import com.google.gson.Gson;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MyProfileActivity extends AppCompatActivity {
    private static final int REQUEST_CAMERA = 0;
    String imagePath = "";
    String userChoosenTask = "";
    int FILE_PICK_CODE = 1;
    String json = new String();
    String pathToBeSend = "";
    Bitmap bm;

    MyTextViewMedium profilephoto;
    MyEditText edtName, edtOrganization, edtDesignation, edtEmail, edtWorkAddress, edtHomeAddress, edtDob;
    MyTextView  edtMobileNo;

    String mobileNo = "", name = "", organizationName = "", designationName = "", emailId = "", workAddress = "", homeAddress = "", dob = "";

    CardView saveCV , backcard;
    Toolbar toolbar;
    TextView txt_title;
    SharedPreferenceHelper preferenceHelper;
    UserData userData;
    AdminAPI adminAPI;
    ProgressDialog progressDialog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_profile);

        preferenceHelper = new SharedPreferenceHelper(getApplicationContext());
        adminAPI = SeriveGenerator.getAPIClass();
        progressDialog = new ProgressDialog(this);

        setupToolbar();

        profilephoto = findViewById(R.id.profilephoto);
        edtMobileNo = findViewById(R.id.mobile_no_tv);
        edtName = findViewById(R.id.name_edt);
        edtOrganization = findViewById(R.id.organization_edt);
        edtDesignation = findViewById(R.id.designation_edt);
        edtEmail = findViewById(R.id.email_edt);
        edtWorkAddress = findViewById(R.id.work_address_edt);
        edtHomeAddress = findViewById(R.id.home_address_edt);
        edtDob = findViewById(R.id.dob_edt);
        saveCV = findViewById(R.id.sendcard);
        backcard = findViewById(R.id.backcard);

        try{
            userData = Global.INSTANCE.getUserMe(preferenceHelper);
        }catch (Exception e){
            Log.e("exception" , e.getMessage());
        }


        if (userData.getApp_usr_mobile() != null && !userData.getApp_usr_mobile().equals("")) {
            mobileNo = userData.getApp_usr_mobile();
        }

        if (userData.getApp_usr_name() != null && !userData.getApp_usr_name().equals("")) {
            name = userData.getApp_usr_name();
        }

        if (userData.getApp_usr_Organization() != null && !userData.getApp_usr_Organization().equals("")) {
            organizationName = userData.getApp_usr_Organization();
        }

        if (userData.getApp_usr_designation() != null && !userData.getApp_usr_designation().equals("")) {
            designationName = userData.getApp_usr_designation();
        }

        if (userData.getApp_usr_email() != null && !userData.getApp_usr_email().equals("")) {
            emailId = userData.getApp_usr_email();
        }

        if (userData.getApp_usr_work_address() != null && !userData.getApp_usr_work_address().equals("")) {
            workAddress = userData.getApp_usr_work_address();
        }

        if (userData.getApp_usr_home_address() != null && !userData.getApp_usr_home_address().equals("")) {
            homeAddress = userData.getApp_usr_home_address();
        }

        if (userData.getApp_usr_dob() != null && !userData.getApp_usr_dob().equals("")) {
            dob = userData.getApp_usr_dob();
        }

        edtMobileNo.setText("+91 " + mobileNo);
        edtName.setText(name);
        edtOrganization.setText(organizationName);
        edtDesignation.setText(designationName);
        edtEmail.setText(emailId);
        edtWorkAddress.setText(workAddress);
        edtHomeAddress.setText(homeAddress);
        edtDob.setText(dob);

        profilephoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectImage();
            }
        });

        edtDob.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DialogFragment newFragment = new DatePickerFragment(edtDob);
                newFragment.show(getSupportFragmentManager(), "datePicker");
            }
        });

        backcard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        saveCV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String name = edtName.getText().toString().trim();
                String enteredOrg = edtOrganization.getText().toString().trim();
                String enteredDesi = edtDesignation.getText().toString().trim();
                String enteredEmail = edtEmail.getText().toString().trim();
                String enteredWorkAddress = edtWorkAddress.getText().toString().trim();
                String enteredHomeAddress = edtHomeAddress.getText().toString().trim();
                String enteredDOB = edtDob.getText().toString().trim();

                if (enteredEmail.length() > 0 && !Patterns.EMAIL_ADDRESS.matcher(enteredEmail).matches()) {
                    Global.INSTANCE.displayToastMessage(getString(R.string.message_valid_email_id), getApplicationContext());
                } else {
                    userData.setApp_usr_name(name);
                    userData.setApp_usr_Organization(enteredOrg);
                    userData.setApp_usr_designation(enteredDesi);
                    userData.setApp_usr_email(enteredEmail);
                    userData.setApp_usr_work_address(enteredWorkAddress);
                    userData.setApp_usr_home_address(enteredHomeAddress);
                    userData.setApp_usr_dob(enteredDOB);
                    saveUserProfile(userData);
                }


            }
        });

    }

    public static class DatePickerFragment extends DialogFragment
            implements DatePickerDialog.OnDateSetListener {

        MyEditText editText;
        public DatePickerFragment(MyEditText editText){
            this.editText = editText;
        }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            final Calendar c = Calendar.getInstance();
            int year = c.get(Calendar.YEAR);
            int month = c.get(Calendar.MONTH);
            int day = c.get(Calendar.DAY_OF_MONTH);
            DatePickerDialog dialog = new DatePickerDialog(getActivity(), this, year, month, day);
            dialog.getDatePicker().setMaxDate(c.getTimeInMillis());
            return  dialog;
        }

        public void onDateSet(DatePicker view, int year, int month, int day) {
            String date = "" + day + "/" + month + "/" + year;
            editText.setText(date);
        }
    }

    private void saveUserProfile(final UserData userData) {
        progressDialog.show();
        Gson gson = new Gson();
        String jsonDATA = gson.toJson(userData);
        Log.e("data" , "" + jsonDATA);
        Call<ResponseBody> responseBodyCall = adminAPI.UpdateUserDetails(jsonDATA);
        responseBodyCall.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                progressDialog.hide();
                if (response.code() == 200) {
                    Global.INSTANCE.setUserMe(userData, preferenceHelper);
                    onBackPressed();
                } else {
                    Global.INSTANCE.displayToastMessage(getString(R.string.message_something_went_wrong), getApplicationContext());
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                progressDialog.hide();
                Global.INSTANCE.displayToastMessage(getString(R.string.message_something_went_wrong), getApplicationContext());
            }
        });
    }

    public void selectImage() {
//        final CharSequence[] items = {"Take Photo", "Choose Photo from Gallery","Choose Other from Gallery",
//                "Cancel"};
        final CharSequence[] items = {"Take Photo", "Choose from Gallery"};
        final AlertDialog.Builder builder = new AlertDialog.Builder(MyProfileActivity.this);
        builder.setTitle("Upload Profile Photo!");
        builder.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int item) {
                boolean result = Utility.checkPermission(MyProfileActivity.this);
                if (items[item].equals("Take Photo")) {
                    userChoosenTask = "Take Photo";
//                    if (result)
//                        cameraIntent();

                } else if (items[item].equals("Choose from Gallery")) {
                    userChoosenTask = "Choose from Gallery";
//                    if (result)
//                        getDocument();
                }

            }
        });
        builder.show();
    }

    private void setupToolbar() {

        toolbar = (Toolbar) findViewById(R.id.toolbarsignup);
        txt_title = (TextView) findViewById(R.id.txt_title);

        setSupportActionBar(toolbar);

        txt_title.setText("Edit Profile");

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
