package com.gifola.apis;

import com.gifola.model.UserDataModel;
import com.google.gson.JsonObject;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import retrofit2.http.PUT;
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

}
