package com.gifola;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.gifola.customfonts.MyEditText;

public class AddSubMemberActivity extends AppCompatActivity {
    String member[] = {"Select Location","Home","Office"};
    Toolbar toolbar;
    TextView txt_title;
    MyEditText edit_mobileno,edit_submembername;
    LinearLayout btn_add;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_sub_member);

        setupToolbar();
        fetchid();

        Spinner spinner = (Spinner) findViewById(R.id.spinner2);
        ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<String>(this,   android.R.layout.simple_spinner_item, member);
        spinnerArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item); // The drop down view
        spinner.setAdapter(spinnerArrayAdapter);
    }

    private void fetchid() {
        edit_mobileno=findViewById(R.id.edit_mobileno);
        edit_submembername=findViewById(R.id.edit_submembername);
        btn_add=findViewById(R.id.btn_add);
    }

    private void setupToolbar() {

        toolbar = (Toolbar) findViewById(R.id.toolbarsignup);
        txt_title = (TextView) findViewById(R.id.txt_title);

        setSupportActionBar(toolbar);

        txt_title.setText("Add/Remove Sub Member");

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
