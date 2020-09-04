package com.gifola.apis;

import com.gifola.model.RFCardModel;
import com.gifola.model.RFLocationDataModel;
import com.gifola.model.RFLocationModel;
import com.gifola.model.SubMemberModel;
import com.gifola.model.UHFCardModel;
import com.google.gson.JsonObject;


import okhttp3.MultipartBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Part;
import retrofit2.http.Query;

public interface AdminAPI {

    @Multipart
    @POST(ApiURLs.ADD_USERS_DETAILS)
    Call<ResponseBody> RegisterUserMobileNumber(@Part("app_usr_mobile") String mobile);

    @Multipart
    @PUT(ApiURLs.ADD_USER_DETAILS)
    Call<String> UpdateUserDetailsWithImage(@Part("app_usr_id") int userId, @Part("app_usr_Organization") String org, @Part("app_usr_designation") String designation, @Part("app_usr_dob") String dob, @Part("app_usr_email") String email, @Part("app_usr_home_address") String homeAdd, @Part("app_usr_mobile") String mobile, @Part("app_usr_name") String name, @Part("app_usr_password") String password, @Part("app_usr_reg_date") String regDate, @Part("app_usr_status") int status, @Part("app_usr_work_address") String workAdd, @Part("isactive") boolean isActive , @Part("app_pic") String imgPath, @Part MultipartBody.Part profilePicture);

    @POST(ApiURLs.GET_RF_LOCATIONS)
    Call<RFLocationModel> GetRFLocations(@Query("app_usr_id") int userId);

    @POST(ApiURLs.GET_UHF_LOCATIONS)
    Call<RFLocationModel> GetUHFLocations(@Query("app_usr_id") int userId);

    @POST(ApiURLs.GET_SUB_MEMBER_LOCATIONS)
    Call<RFLocationModel> GetSubMemberLocations(@Query("app_usr_id") int userId);

    @Headers({"Content-Type:application/json"})
    @POST(ApiURLs.GET_LIST_OF_RF_CARD)
    Call<RFCardModel> GetAllRFCards(@Body JsonObject jsonRequest);

    @Headers({"Content-Type:application/json"})
    @POST(ApiURLs.ADD_RF_CARD)
    Call<ResponseBody> AddRFCard(@Body JsonObject jsonRequest);

    @Headers({"Content-Type:application/json"})
    @POST(ApiURLs.REMOVE_RF_CARD)
    Call<ResponseBody> DeletedRFCard(@Body JsonObject jsonRequest);

    @Headers({"Content-Type:application/json"})
    @POST(ApiURLs.GET_LIST_OF_UHF_CARD)
    Call<UHFCardModel> GetAllUHFCards(@Body JsonObject jsonRequest);

    @Headers({"Content-Type:application/json"})
    @POST(ApiURLs.ADD_UHF_CARD)
    Call<ResponseBody> AddUHFCard(@Body JsonObject jsonRequest);

    @Headers({"Content-Type:application/json"})
    @POST(ApiURLs.ADD_UPDATE_LOCATION_DETAILS)
    Call<ResponseBody> AddLocationDetails(@Body JsonObject jsonRequest);

    @Headers({"Content-Type:application/json"})
    @POST(ApiURLs.REMOVE_UHF_CARD)
    Call<ResponseBody> DeletedUHFCard(@Body JsonObject jsonRequest);

    @GET(ApiURLs.GET_LIST_OF_SUB_MEMBERS)
    Call<SubMemberModel> GetAllSubMembers(@Query("mem_id") Integer memberId);

    @Headers({"Content-Type:application/json"})
    @POST(ApiURLs.ADD_SUB_MEMBER)
    Call<ResponseBody> AddSubMember(@Body JsonObject jsonRequest);

    @Headers({"Content-Type:application/json"})
    @POST(ApiURLs.REMOVE_SUB_MEMBERS)
    Call<ResponseBody> DeletedSubMember(@Body JsonObject jsonRequest);
}
