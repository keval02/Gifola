package com.gifola;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.gifola.adapter.LocationListAdapter;
import com.gifola.apis.ApiURLs;
import com.gifola.apis.ServiceHandler;
import com.gifola.constans.Global;
import com.gifola.constans.SharedPreferenceHelper;
import com.gifola.customfonts.MyTextViewBold;
import com.gifola.model.LocationDataModel;
import com.gifola.model.LocationDataModelItem;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;

public class MyLocationsActivity extends AppCompatActivity {
    Toolbar toolbar;
    TextView txt_title;
    RecyclerView rv_location;
    SharedPreferenceHelper preferenceHelper;
    ProgressDialog progressDialog;
    String usersMobileNum = "";
    List<LocationDataModelItem> locationDataModelItems = new ArrayList<>();
    LocationListAdapter locationListAdapter;
    MyTextViewBold txtNoDataFound;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_locations);
        setupToolbar();
        preferenceHelper = new SharedPreferenceHelper(this);
        progressDialog = new ProgressDialog(this);
        rv_location = findViewById(R.id.rv_location);
        txtNoDataFound = findViewById(R.id.txt_no_data_found);
        usersMobileNum = Global.INSTANCE.getUserMe(preferenceHelper).getApp_usr_mobile();
        new GetUsersLocations(usersMobileNum, this).execute();
    }

    class GetUsersLocations extends AsyncTask<Void, Void, String> {
        String mobileNum = "";
        Activity activity;

        public GetUsersLocations(String mobileNum, Activity activity) {
            this.mobileNum = mobileNum;
            this.activity = activity;
        }

        @Override
        protected void onPreExecute() {
            progressDialog.show();
        }

        @Override
        protected String doInBackground(Void... voids) {
            String json = "";
            try {
                json = new ServiceHandler().makeServiceCall(ApiURLs.BASE_URL + ApiURLs.GET_USERS_LOCATIONS + mobileNum, ServiceHandler.GET);
            } catch (Exception e) {
                Log.e("exception", e.getMessage());
            }


            return json;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            progressDialog.hide();
            try {
                if (s == null || s.isEmpty()) {
                    rv_location.setVisibility(View.GONE);
                    txtNoDataFound.setVisibility(View.VISIBLE);
                } else {
                    String jsonResponse = s;
                    Gson gson = new Gson();
                    LocationDataModel locationDataModel = gson.fromJson(jsonResponse, LocationDataModel.class);
                    locationDataModelItems.addAll(locationDataModel);
                    locationListAdapter = new LocationListAdapter(activity, locationDataModelItems);
                    LinearLayoutManager layoutManager = new LinearLayoutManager(activity, LinearLayoutManager.VERTICAL, false);
                    rv_location.setLayoutManager(layoutManager);
                    rv_location.setAdapter(locationListAdapter);
                    rv_location.setVisibility(View.VISIBLE);
                    txtNoDataFound.setVisibility(View.GONE);
                }
            } catch (Exception e) {
                txtNoDataFound.setVisibility(View.VISIBLE);
                Log.e("exception", e.getMessage());
            }
        }
    }

    private void setupToolbar() {

        toolbar = (Toolbar) findViewById(R.id.toolbarsignup);
        txt_title = (TextView) findViewById(R.id.txt_title);

        setSupportActionBar(toolbar);

        txt_title.setText("My Locations");

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
