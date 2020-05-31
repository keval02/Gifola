package com.gifola;

import android.Manifest;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.viewpager.widget.ViewPager;

import com.daimajia.slider.library.Indicators.PagerIndicator;
import com.daimajia.slider.library.SliderLayout;
import com.daimajia.slider.library.SliderTypes.BaseSliderView;
import com.daimajia.slider.library.SliderTypes.TextSliderView;
import com.daimajia.slider.library.Tricks.ViewPagerEx;
import com.google.android.material.navigation.NavigationView;
import com.gifola.constans.Global;
import com.gifola.constans.SharedPreferenceHelper;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import static android.content.pm.PackageManager.PERMISSION_GRANTED;

public class MainActivity extends AppCompatActivity implements BaseSliderView.OnSliderClickListener, ViewPagerEx.OnPageChangeListener {

    LinearLayout containerView;
    DrawerLayout myDrawerLayout;
    NavigationView myNavigationView;

    SliderLayout slider;
    PagerIndicator custom_indicator;
    ImageView fav, home;
    ImageView imageView;
    TextView textView;
    SliderLayout lib_slider;

    String[] perms = {Manifest.permission.SYSTEM_ALERT_WINDOW, Manifest.permission.WRITE_SETTINGS, Manifest.permission.WRITE_SECURE_SETTINGS, Manifest.permission.WAKE_LOCK, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.CALL_PHONE, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.CAMERA, Manifest.permission.INTERNET, Manifest.permission.READ_SMS, Manifest.permission.RECEIVE_SMS,
            Manifest.permission.ACCESS_WIFI_STATE, Manifest.permission.ACCESS_NETWORK_STATE, Manifest.permission.CALL_PHONE, Manifest.permission.READ_CONTACTS, Manifest.permission.WRITE_CONTACTS};
    boolean isMdevice;
    boolean pstatus;
    int code = 1;
    public final static int PERM_REQUEST_CODE_DRAW_OVERLAYS = 1234;
    private static final String HASH_TYPE = "SHA-256";
    public static final int NUM_HASHED_BYTES = 9;
    public static final int NUM_BASE64_CHAR = 11;


    // slider for api 20 or lower
    private static ViewPager mPager;
    private static int currentPage = 0;
    private ArrayList<Integer> XMENArray = new ArrayList<Integer>();
    //    ArrayList<BeanLowerSlider> beanLowerSliders = new ArrayList<>();
    LinearLayout lower, higher;
    SharedPreferenceHelper preference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        preference = new SharedPreferenceHelper(getApplicationContext());
        if (preference.getBoolean(Global.INSTANCE.isLoggedIn() , false)) {
            Intent intent = new Intent(getApplicationContext(), DashboardActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            finish();
        }

        setContentView(R.layout.activity_main);
//        isMdevice=isMarshmallowPlusDevice();
//        pstatus = isPermissionRequestRequired(MainActivity.this, perms, code);
//        permissionToDrawOverlays();

        containerView = (LinearLayout) findViewById(R.id.containerView);

//        lower=(LinearLayout)findViewById(R.id.lower);
//        higher=(LinearLayout)findViewById(R.id.higher);

//            higher.setVisibility(View.VISIBLE);
//            lower.setVisibility(View.GONE);

        slider = (SliderLayout) findViewById(R.id.slider);

        // slider.setIndicatorVisibility(PagerIndicator.IndicatorVisibility.Invisible);

        custom_indicator = (PagerIndicator) findViewById(R.id.custom_indicator);
        textView = (TextView) findViewById(R.id.textid);
        textView.setVisibility(View.VISIBLE);
        textView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), SignupActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
            }
        });


        HashMap<String, Integer> file_maps = new HashMap<String, Integer>();

        file_maps.put("Mobile 3", R.drawable.splash1);
        file_maps.put("Mobile 2", R.drawable.splash2);
        file_maps.put("Mobile 1", R.drawable.splash3);


        for (String name : file_maps.keySet()) {

            TextSliderView textSliderView = new TextSliderView(this);
            // initialize a SliderLayout
            textSliderView
                    .description(name)
                    .image(file_maps.get(name))
                    .setScaleType(BaseSliderView.ScaleType.Fit)
                    .setOnSliderClickListener(this);

            //add your extra information
            textSliderView.bundle(new Bundle());
            textSliderView.getBundle()
                    .putString("extra", name);

            slider.addSlider(textSliderView);
        }
        slider.stopAutoCycle();


        imageView = (ImageView) findViewById(R.id.action_image);


//        if(!GlobalFile.isOnline(getApplicationContext())){
//
////            GlobalFile.CustomToast(CategoryScreen.this,"No Internet Connection", getLayoutInflater());
//            Toast.makeText(getApplicationContext(), "No Internet Connection", Toast.LENGTH_SHORT).show();
//
//        }else
//        {
//            getSliderImages();
        //  new ImportCategory().execute();
//        }

    }

    @Override
    public void onSliderClick(BaseSliderView slider) {

    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

    }

    @Override
    public void onPageSelected(int position) {

    }

    @Override
    public void onPageScrollStateChanged(int state) {

    }

    public static boolean isMarshmallowPlusDevice() {

        return Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP_MR1;
    }

    @SuppressLint("WrongConstant")
    @TargetApi(Build.VERSION_CODES.M)
    public static boolean isPermissionRequestRequired(Activity activity, @NonNull String[] permissions, int requestCode) {
        if (isMarshmallowPlusDevice() && permissions.length > 0) {
            List<String> newPermissionList = new ArrayList<>();
            for (String permission : permissions) {
                if (PERMISSION_GRANTED != activity.checkSelfPermission(permission)) {
                    newPermissionList.add(permission);

                }
            }
            if (newPermissionList.size() > 0) {
                activity.requestPermissions(newPermissionList.toArray(new String[newPermissionList.size()]), requestCode);
                return true;
            }


        }

        return false;
    }

    public void permissionToDrawOverlays() {
        if (android.os.Build.VERSION.SDK_INT >= 23) {   //Android M Or Over
            if (!Settings.canDrawOverlays(this)) {
                Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + getPackageName()));
                startActivityForResult(intent, PERM_REQUEST_CODE_DRAW_OVERLAYS);
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PERM_REQUEST_CODE_DRAW_OVERLAYS) {
            if (Build.VERSION.SDK_INT >= 23) {   //Android M Or Over
                if (!Settings.canDrawOverlays(this)) {
                    // ADD UI FOR USER TO KNOW THAT UI for SYSTEM_ALERT_WINDOW permission was not granted earlier...
                }
            }
        }
    }

//    public void getSliderImages() {
//        Log.e("11","11");
//        // appPref=new AppPref(getContext());
//        final Custom_ProgressDialog loadingView = new Custom_ProgressDialog(SliderActivity.this, "");
//        loadingView.setCancelable(false);
//        loadingView.show();
//        Log.e("22","22");
//        //layout_login.setVisibility(View.GONE);
//        StringRequest request = new StringRequest(GlobalFile.POST, GlobalFile.server_link + "Slider/App_GetSlider",
//                new Response.Listener<String>() {
//                    @Override
//                    public void onResponse(String response) {
//
//
//                        Log.e("JSON", response);
//
//                        try {
//                            JSONObject jObj = new JSONObject(response);
//
//                            Log.e("response",""+response.toString());
//
//                            boolean date = jObj.getBoolean("status");
//
//                            Log.e("status",""+date);
//                            if (date==false) {
//                                Log.e("ff","ff");
//
//                                String Message = jObj.getString("message");
//                                Toast.makeText(getApplicationContext(), "" + Message, Toast.LENGTH_LONG).show();
//                                // GlobalFile.CustomToast(Activity_Login.this,""+Message, getLayoutInflater());
//                                loadingView.dismiss();
//                                //layout_login.setVisibility(View.VISIBLE);
//                                //avi.hide();
//                            }
//                            else
//                            {
//
//                                try {
//
//                                    final JSONArray dataArray = jObj.getJSONArray("data");
//                                    int num = 0;
//                                    String textname = "";
//
//                                    if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//
//
//
//                                        for (int i = 0; i < dataArray.length(); i++) {
//
//                                            JSONObject userObject = dataArray.getJSONObject(i);
//
//                                            BeanLowerSlider lowerSlider=new BeanLowerSlider();
//                                            lowerSlider.setImage(userObject.getString("app_image"));
//
//                                            num = num + 1;
//                                            Log.e("num", "" + num);
//                                            Log.e("size", "" + dataArray.length());
//
//
////                                        if(num == dataArray.length())
////                                        {
////                                            textView.setText("Enter");
//////                                            textname="Enter";
////                                        }
////                                        else {
////                                            textView.setText("Skip");
//////                                            textname="Skip";
////                                        }
//
//
//                                            slider.addOnPageChangeListener(new ViewPagerEx.OnPageChangeListener() {
//                                                @Override
//                                                public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
//
//                                                }
//
//                                                @Override
//                                                public void onPageSelected(int position) {
//
//                                                    // addBottomDots(position);
//
//                                                    // changing the next button text 'NEXT' / 'GOT IT'
//                                                    if (position == dataArray.length() - 1) {
//                                                        // last page. make button text to GOT IT
////                                                    btnNext.setText(getString(R.string.start));
////                                                    btnSkip.setVisibility(View.GONE);
//                                                        textView.setText("Enter");
//                                                    } else {
//                                                        // still pages are left
////                                                    btnNext.setText(getString(R.string.next));
////                                                    btnSkip.setVisibility(View.VISIBLE);
//                                                        textView.setText("Skip");
//                                                    }
//
//                                                }
//
//                                                @Override
//                                                public void onPageScrollStateChanged(int state) {
//
//                                                }
//                                            });
//
//
//                                            TextSliderView textSliderView = new TextSliderView(SliderActivity.this);
//                                            // initialize a SliderLayout
//                                            textSliderView
//                                                    // .description(textname)
//                                                    .image(GlobalFile.image_link + "" + userObject.getString("app_image"))
//                                                    .setScaleType(BaseSliderView.ScaleType.Fit)
////                                                            .setOnImageLoadListener(ImageLoadListener)
//                                                    .setOnSliderClickListener(new BaseSliderView.OnSliderClickListener() {
//                                                        @Override
//                                                        public void onSliderClick(BaseSliderView slider) {
////                                                                    Intent intent=new Intent(getApplicationContext(),MainActivity.class);
////                                                                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
////                                                                    startActivity(intent);
//
//                                                        }
//                                                    });
//
//
//                                            //add your extra information
//                                            textSliderView.bundle(new Bundle());
//                                            textSliderView.getBundle()
//                                                    .putString("extra", textname);
//
//                                            slider.addSlider(textSliderView);
//
//
////                                                }
//                                            beanLowerSliders.add(lowerSlider);
//
//                                        }
//
//
//
//                                    }
//                                    else {
//
//                                        for (int i = 0; i < dataArray.length(); i++) {
//
//                                            JSONObject userObject = dataArray.getJSONObject(i);
//
//                                            BeanLowerSlider lowerSlider = new BeanLowerSlider();
//                                            lowerSlider.setImage(userObject.getString("app_image"));
//                                            beanLowerSliders.add(lowerSlider);
//                                        }
//
//                                        new MyAdapter(SliderActivity.this, beanLowerSliders).notifyDataSetChanged();
//
//
//
//                                        //for slider api 20 or lower
//                                        mPager.setAdapter(new MyAdapter(SliderActivity.this, beanLowerSliders));
//                                        CircleIndicator indicator = (CircleIndicator) findViewById(R.id.indicator);
//                                        indicator.setViewPager(mPager);
//
//                                        // Auto start of viewpager
//                                        final Handler handler = new Handler();
//                                        final Runnable Update = new Runnable() {
//                                            public void run() {
//                                                if (currentPage == dataArray.length()) {
//                                                    currentPage = 0;
//                                                }
//                                                mPager.setCurrentItem(currentPage++, true);
//                                            }
//                                        };
//                                        Timer swipeTimer = new Timer();
//                                        swipeTimer.schedule(new TimerTask() {
//                                            @Override
//                                            public void run() {
//                                                handler.post(Update);
//                                            }
//                                        }, 2500, 2500);
//
//                                    }
//
//                                }
//
//                                catch (JSONException e) {
//                                    Log.e("Exception",e.getMessage());
//                                }
//                                finally {
//                                    loadingView.dismiss();
//                                }
//
//                            }
//                        } catch (JSONException j) {
//                            j.printStackTrace();
//                            loadingView.dismiss();
//                            Log.e("Exception",""+j.getMessage());
//                        }
//                    }
//                },
//                new Response.ErrorListener() {
//                    @Override
//                    public void onErrorResponse(VolleyError error) {
//                        //  Log.e("ERROR",error.getMessage());
//                        if (error instanceof NetworkError) {
//                            noInternet(getApplicationContext());
//                        } else
//                        {
//                            serverError(getApplicationContext());
//                        }
//                    }
//                }) {
//            @Override
//            protected Map<String, String> getParams() {
//
//                Map<String, String> params = new HashMap<String, String>();
//
//                return params;
//            }
//        };
//        Exxaro.getInstance().addToRequestQueue(request);
//    }




}

