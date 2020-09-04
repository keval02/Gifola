package com.gifola.model

import okhttp3.MultipartBody
import java.io.Serializable

class UserData() : Serializable{
    var app_usr_id : Int? = 0
    var app_pic: String? = ""
    var app_usr_Organization: String? = ""
    var app_usr_designation: String? = ""
    var app_usr_dob: String? = ""
    var app_usr_email: String? = ""
    var app_usr_home_address: String? = ""
    var app_usr_mobile: String? = ""
    var app_usr_name: String? = ""
    var app_usr_password: String? = ""
    var app_usr_reg_date: String? = ""
    var app_usr_status: Int? = 0
    var app_usr_work_address: String? = ""
    var isactive: Boolean? = false
    var isSubMember: Boolean? = false
    var isOnlyAppUser: Boolean? = false
    var mem_id : Int?=0
    var mem_cust_id : Int?=0
    var profilePic : MultipartBody.Part? = null
}