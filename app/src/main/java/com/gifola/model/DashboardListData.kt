package com.gifola.model

data class DashboardListData(
    var AcceptStatus: Int = 0,
    val Designation: String? = "-",
    val Email: String? = "-",
    val Location: String? =  "-",
    val MobileNo: String? = "-",
    val Mode: Int,
    val Name: String? = "-",
    val Organization: String? = "-",
    val ProPic: String? = "-",
    val Purpose: String = "",
    val RequestDate: String? = "-",
    val RequestID: Int,
    val VisitDate: String? = "-",
    val VisitTimeFrom: String? = "-",
    val VisitTimeTo: String? = "-",
    val WorkAddress: String? = "-",
    var isReceieveRequest : Boolean = false
)