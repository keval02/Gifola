package com.gifola.apis;

public class ApiURLs {

    public static final String BASE_URL = "http://3.6.68.243/";
    public static final String API_BASE = "mobileaccess/api/";
    public static final String SMS_BASE_URL = "http://api.msg91.com/";

    public static final String IMAGE_URL = "http://3.6.68.243/mobileaccess/Profile_Images/";
    public static final String CHECK_USERS_MOBILE_NO = API_BASE + "app_usr_master/GetDataById/";
    public static final String ADD_USERS_DETAILS = API_BASE + "WebService/Login";
    public static final String ADD_USER_DETAILS = API_BASE + "app_usr_master/Putcatagory";
    public static final String GET_USERS_LOCATIONS = API_BASE + "member_site/GetDataById/";
    public static final String GET_RF_LOCATIONS = API_BASE + "WebService/LocationForRF";
    public static final String GET_UHF_LOCATIONS = API_BASE + "WebService/LocationForUHF";
    public static final String GET_SUB_MEMBER_LOCATIONS = API_BASE + "WebService/LocationForSubMember";
    public static final String ADD_RF_CARD = API_BASE + "WebService/AddRFCard";
    public static final String GET_LIST_OF_RF_CARD = API_BASE + "WebService/RFList";
    public static final String REMOVE_RF_CARD = API_BASE + "WebService/RemoveRFCard";
    public static final String ADD_UHF_CARD = API_BASE + "WebService/AddUHFCard";
    public static final String GET_LIST_OF_UHF_CARD = API_BASE + "WebService/UHFList";
    public static final String REMOVE_UHF_CARD = API_BASE + "WebService/RemoveUHFCard";
    public static final String ADD_SUB_MEMBER = API_BASE + "WebService/AddSubMember";
    public static final String ADD_UPDATE_LOCATION_DETAILS = API_BASE + "WebService/AddSiteName";
    public static final String GET_LIST_OF_SUB_MEMBERS = API_BASE + "WebService/GetSubmemberList";
    public static final String REMOVE_SUB_MEMBERS = API_BASE + "WebService/RemoveSubmember";
    public static final String GET_CHECKIN_USER_INFO = "DeviceService/api/Visitor/GetMemberDetails";
    public static final String SEND_ALLOW_CHECKIN = "DeviceService/api/Visitor/CheckIn";
    public static final String CHECK_IN_LIST = "DeviceService/api/Visitor/RequestList";
    public static final String DASHBOARD_MAIN_LIST = "DeviceService/api/Dashboard/DashboardApi";
    public static final String UPDATE_REQUEST_STATUS = "DeviceService/api/Dashboard/StatusApi";
    public static final String FAV_PLACE_API = "DeviceService/api/Dashboard/MyFavPlacesApi";
    public static final String CONTACT_US_API = "DeviceService/api/Service/ContactUsApi";
    public static final String UPDATE_USER_TOKEN = "DeviceService/api/Home/UpdateTokenApi";
    public static final String SET_PRIVACY = "DeviceService/api/Home/SetPrivacy";
    public static final String GET_PRIVACY = "DeviceService/api/Home/GetPrivacy";
    public static final String SMS_POST_URL = "api/v5/flow/";


    /*{
        "AppUserId": 1,
            "VisitorSecret": 1,
            "SwitchNotify": 1,
            "VisitSecret": 0,
            "HideVisit": 1
    }*/
/*
    Logic :

            1 is toggle green
0 is toggle off*/
}
