package com.gifola.model

import java.io.Serializable

class CheckInUserInfoModel() : Serializable {
    val Status: Int = 0
    var appUser: AppUser = AppUser()
}
