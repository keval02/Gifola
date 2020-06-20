package com.gifola;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.view.ContextThemeWrapper;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;
import com.gifola.apis.AdminAPI;
import com.gifola.apis.ApiURLs;
import com.gifola.apis.SeriveGenerator;
import com.gifola.apis.ServiceHandler;
import com.gifola.constans.Global;
import com.gifola.customfonts.MyTextViewBold;
import com.gifola.model.UserData;
import com.gifola.model.UserDataModel;
import com.google.android.material.navigation.NavigationView;
import com.gifola.constans.SharedPreferenceHelper;
import com.gifola.customfonts.MyTextViewMedium;
import com.gifola.customfonts.MyTextViewRegular;
import com.google.gson.Gson;

import de.hdodenhof.circleimageview.CircleImageView;

import static com.gifola.apis.ApiURLs.IMAGE_URL;

public class DashboardActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    private Fragment fragment;
    private FragmentManager fragmentManager;
    LinearLayout containerView;
    NavigationView navigationView;
    DrawerLayout myDrawerLayout;
    Toolbar toolbar;
    LinearLayout clicklayout, sublayout, menuimg;
    ImageView qrscanimg;
    MyTextViewMedium sendper, requestper;
    Dialog dialog, dialog2;
    ImageView infoclick;
    SharedPreferenceHelper appPreference;
    AdminAPI adminAPI;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);
        appPreference = new SharedPreferenceHelper(getApplicationContext());
        containerView = (LinearLayout) findViewById(R.id.containerView);
        navigationView = (NavigationView) findViewById(R.id.nav_drawer);
        navigationView.setNavigationItemSelectedListener(this);
        myDrawerLayout = (DrawerLayout) findViewById(R.id.drawerLayout);
        adminAPI = SeriveGenerator.getAPIClass();
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        sublayout = findViewById(R.id.sublayout);
        menuimg = findViewById(R.id.menuimg);
        infoclick = findViewById(R.id.infoclick);
        infoclick.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                InfoDialoge();
            }
        });

        menuimg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (sublayout.getVisibility() == View.VISIBLE) {
                    sublayout.setVisibility(View.GONE);
                } else {
                    sublayout.setVisibility(View.VISIBLE);
                }
            }
        });

        Menu m = navigationView.getMenu();
        for (int i = 0; i < m.size(); i++) {
            MenuItem mi = m.getItem(i);

            //for aapplying a font to subMenu ...
            SubMenu subMenu = mi.getSubMenu();
            if (subMenu != null && subMenu.size() > 0) {
                for (int j = 0; j < subMenu.size(); j++) {
                    MenuItem subMenuItem = subMenu.getItem(j);
                    applyFontToMenuItem(subMenuItem);
                }
            }
            //the method we have create in activity
            applyFontToMenuItem(mi);
        }


        qrscanimg = findViewById(R.id.qrscanimg);
        qrscanimg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), QRScanActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
            }
        });

        sendper = findViewById(R.id.sendper);
        sendper.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), SendPermissionActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
            }
        });

        requestper = findViewById(R.id.requestper);
        requestper.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), RequestPermissionActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
            }
        });

        View headerView = LayoutInflater.from(this).inflate(R.layout.navigation_header, navigationView, false);
//        View headerView = LayoutInflater.from(this).inflate(R.layout.drawer_header_servicecenter, navigationView, false);
//        headerView.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Intent i = new Intent(MainActivity.this, ProfileActivity.class);
//                i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
//                startActivity(i);
//            }
//        });
        MyTextViewMedium editprofile = (MyTextViewMedium) headerView.findViewById(R.id.editprofile);
        MyTextViewBold userName = (MyTextViewBold) headerView.findViewById(R.id.txt_user_name);
        final CircleImageView userProfile = (CircleImageView) headerView.findViewById(R.id.profile_image);
        userName.setText(Global.INSTANCE.getUserMe(appPreference).getApp_usr_name());

        String imagePath = Global.INSTANCE.getUserMe(appPreference).getApp_pic();
        if(imagePath != null || !imagePath.equals("null") || imagePath.equals("")){
            imagePath = IMAGE_URL + imagePath;
            Glide.with(this)
                    .load(imagePath)
                    .apply(RequestOptions.placeholderOf(R.drawable.user_placeholder).error(R.drawable.user_placeholder))
                    .addListener(new RequestListener<Drawable>() {
                        @Override
                        public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                            Log.e("imageException" , e.getMessage());
                            return false;
                        }

                        @Override
                        public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                            userProfile.setImageDrawable(resource);
                            return true;
                        }
                    }).submit();
        }


        editprofile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), MyProfileActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
            }
        });
        navigationView.addHeaderView(headerView);


        ActionBarDrawerToggle mDrawerToggle = new ActionBarDrawerToggle(this, myDrawerLayout, toolbar, R.string.app_name,
                R.string.app_name) {
            @Override
            public void onDrawerSlide(View drawerView, float slideOffset) {
                super.onDrawerSlide(drawerView, slideOffset);
                float moveFactor = (navigationView.getWidth() * slideOffset);

                containerView.setTranslationX(slideOffset * drawerView.getWidth());
                myDrawerLayout.bringChildToFront(drawerView);
                myDrawerLayout.requestLayout();
                //below line used to remove shadow of drawer
                myDrawerLayout.setScrimColor(Color.TRANSPARENT);

                /*if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
                {
                    containerView.setTranslationX(moveFactor);
                }
                else
                {
                    TranslateAnimation anim = new TranslateAnimation(lastTranslate, moveFactor, 0.0f, 0.0f);
                    anim.setDuration(0);
                    anim.setFillAfter(true);
                    containerView.startAnimation(anim);

                    lastTranslate = moveFactor;
                }*/
            }
        };

        myDrawerLayout.setDrawerListener(mDrawerToggle);

        mDrawerToggle.syncState();
        if(appPreference.getBoolean(Global.INSTANCE.isEnteredFirstTime() , false)){
            String mobileNo = Global.INSTANCE.getUserMe(appPreference).getApp_usr_mobile();
            new CheckUserMobileNumber(mobileNo).execute();
        }
    }

    class CheckUserMobileNumber extends AsyncTask<Void, Void, String> {
        String mobileNo = "";

        public CheckUserMobileNumber(String mobileNo) {
            this.mobileNo = mobileNo;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

        }

        @Override
        protected String doInBackground(Void... voids) {
            String json = "";
            try {
                String postURL = ApiURLs.CHECK_USERS_MOBILE_NO + mobileNo;
                json = new ServiceHandler().makeServiceCall(ApiURLs.BASE_URL + postURL, ServiceHandler.GET);
            } catch (Exception e) {
                Log.e("exception", e.getMessage());
            }

            return json;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            try {
                if (result == null || result.isEmpty()) {
                    Global.INSTANCE.displayToastMessage(getString(R.string.message_something_went_wrong), getApplicationContext());
                } else {
                    String jsonResponse = result;
                    if (!jsonResponse.startsWith("{")) {
                        Gson gson = new Gson();
                        UserDataModel dataModel = gson.fromJson(jsonResponse, UserDataModel.class);
                        UserData userData = dataModel.get(0);
                        Global.INSTANCE.setUserMe(userData, appPreference);
                        appPreference.setBoolean(Global.INSTANCE.isEnteredFirstTime() , false);
                    }

                }
            } catch (Exception e) {
                Log.e("exception", e.getMessage());
            }
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.mylocation) {
            Intent intent = new Intent(getApplicationContext(), MyLocationsActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
        } else if (id == R.id.addmember) {
            Intent intent = new Intent(getApplicationContext(), AddSubMemberActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
        } else if (id == R.id.addrfcard) {
            Intent intent = new Intent(getApplicationContext(), AddRFCardActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
        } else if (id == R.id.adduhfcard) {
            Intent intent = new Intent(getApplicationContext(), AddUHFCardActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
        } else if (id == R.id.mylogs) {
            Intent intent = new Intent(getApplicationContext(), MyLogsActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
        } else if (id == R.id.privacysettings) {
            Intent intent = new Intent(getApplicationContext(), PrivacyPolicyActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
        } else if (id == R.id.contatcus) {
            Intent intent = new Intent(getApplicationContext(), ContactUsActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
        } else if (id == R.id.rateus) {
        } else if (id == R.id.terms) {
        } else if (id == R.id.aboutus) {
        } else if (id == R.id.logout) {
            LogoutDialoge();
        } else if (id == R.id.faq) {
            Intent intent = new Intent(getApplicationContext(), FAQActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
        }
//        else if (id == R.id.logout) {
//        dialog = new Dialog(new ContextThemeWrapper(this, R.style.AppTheme));
//        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
//        dialog.setCancelable(false);
//        dialog.setContentView(R.layout.dialoge_logout);
//        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
//        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
//
////        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
////        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
//
//        txtyes = (MyTextViewRegular) dialog.findViewById(R.id.logout);
//        txtno = (MyTextViewRegular) dialog.findViewById(R.id.cancel);
//
//
//        txtyes.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Intent navigate = new Intent(MainActivity.this, LoginActivity.class);
//                navigate.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
//                appPref.clearData(getApplicationContext());
//                startActivity(navigate);
//                finish();
//            }
//        });
//
//        txtno.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                dialog.dismiss();
//            }
//        });
//
//        dialog.show();
//    }
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawerLayout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {

    }

    private void applyFontToMenuItem(MenuItem mi) {
        Typeface font = Typeface.createFromAsset(getAssets(), "fonts/Quicksand-Medium.ttf");
        SpannableString mNewTitle = new SpannableString(mi.getTitle());
        mNewTitle.setSpan(new CustomTypefaceSpan("", font), 0, mNewTitle.length(), Spannable.SPAN_INCLUSIVE_INCLUSIVE);
        mi.setTitle(mNewTitle);
    }

    public void LogoutDialoge() {
        dialog = new Dialog(new ContextThemeWrapper(this, R.style.AppTheme));
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(true);
        dialog.setContentView(R.layout.dialoge_logout);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);

//        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
//        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));

        MyTextViewRegular submit = (MyTextViewRegular) dialog.findViewById(R.id.submit);

        ImageView img_no = (ImageView) dialog.findViewById(R.id.img_no);


        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                appPreference.clearAll();
                dialog.dismiss();
                Intent intent = new Intent(getApplicationContext(), SignupActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                finish();
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


    public void InfoDialoge() {
        dialog2 = new Dialog(new ContextThemeWrapper(this, R.style.AppTheme));
        dialog2.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog2.setCancelable(true);
        dialog2.setContentView(R.layout.dialoge_info);
        dialog2.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog2.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);

//        dialog2.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
//        dialog2.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));

        MyTextViewRegular submit = (MyTextViewRegular) dialog2.findViewById(R.id.submit);

        ImageView img_no = (ImageView) dialog2.findViewById(R.id.img_no);


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
