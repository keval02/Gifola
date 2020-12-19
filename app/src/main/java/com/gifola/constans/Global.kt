package com.gifola.constans

import android.content.Context
import android.widget.Toast
import com.gifola.model.UserData
import com.gifola.model.UserDataModel

object Global {
    // Strings

    var smsProviderAuthKey = "395d698295167ab3c06cfeabaccb8829"
    var smsProviderSender = "GIFOLA"
    var smsProviderRoute = "B."
    var smsText = " is The One Time Password(OTP) for activating your GIFOLA Account and will be valid for only 2 minutes"
    var otpString = "otp"
    var mobileNumberText = "mobileNumber"
    var isAlreadyRegistered = "isAlreadyRegistered"
    var checkedInUserInfo = "checkedInUserInfo"
    var keyIsFromFavourite = "isFavorite"

    // preference Strings

    var isLoggedIn = "isLoggedIn"
    var userData = "userData"
    var isEnteredFirstTime = "isEnteredFirstTime"


    // WebView URLS

    const val terms_and_conditions_url = "https://gifola.tech/terms-and-conditions/"
    const val privacy_policy_url = "https://gifola.tech/privacy-policy-2/"
    const val about_us_url = "https://gifola.tech/about-2/"
    const val contact_us_url = "https://gifola.tech/contact/"
    const val disclaimer_us_url = "https://gifola.tech/disclaimer/"


    fun displayToastMessage(message: String?, context: Context?) {
        Toast.makeText(context, message, Toast.LENGTH_LONG).show()
    }

    fun setUserMe(user: UserData? , sharedPreferenceHelper : SharedPreferenceHelper) {
        sharedPreferenceHelper.setObject(userData, user)
    }

    fun getUserMe(sharedPreferenceHelper: SharedPreferenceHelper): UserData? {
        return sharedPreferenceHelper.getObject(userData, UserData::class.java)
    }
}