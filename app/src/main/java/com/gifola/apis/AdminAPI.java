package com.gifola.apis;

import com.gifola.model.UserData;
import com.gifola.model.UserDataModel;
import com.google.gson.JsonObject;

import org.json.JSONObject;

import okhttp3.MultipartBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Headers;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Part;
import retrofit2.http.Query;

public interface AdminAPI {

    @Headers({"Content-Type:application/json"})
    @GET(ApiURLs.CHECK_USERS_MOBILE_NO)
    Call<ResponseBody> CheckUserMobile(@Query("id")  String mobileNumber);

    @Headers({"Content-Type:application/json"})
    @POST(ApiURLs.ADD_USERS_DETAILS)
    Call<ResponseBody> RegisterUserMobileNumber(@Body String jsonRequest);

    @Headers({"Content-Type:application/json"})
    @PUT(ApiURLs.ADD_USERS_DETAILS)
    Call<ResponseBody> UpdateUserDetails(@Body String jsonRequest);

    @Multipart
    @PUT(ApiURLs.ADD_USERS_DETAILS)
    Call<String> UpdateUserDetailsWithImage(@Part("app_usr_id") int userId, @Part("app_usr_Organization") String org, @Part("app_usr_designation") String designation, @Part("app_usr_dob") String dob, @Part("app_usr_email") String email, @Part("app_usr_home_address") String homeAdd, @Part("app_usr_mobile") String mobile, @Part("app_usr_name") String name, @Part("app_usr_password") String password, @Part("app_usr_reg_date") String regDate, @Part("app_usr_status") int status, @Part("app_usr_work_address") String workAdd, @Part("isactive") boolean isActive , @Part("app_pic") String imgPath, @Part MultipartBody.Part profilePicture);
}
