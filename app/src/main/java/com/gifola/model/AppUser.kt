package com.gifola.model

import java.io.Serializable

class AppUser() : Serializable {
    var AppUserId: Int = 0
    var AppUserName: String = ""
    var MobileNo: String = ""
    var ProPic: String = ""
    var ReqType: Int = 0
    var Status: Int = 0
    var memberDetails: List<MemberDetail> = ArrayList()
}