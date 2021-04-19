package com.gifola.model

data class PrivacySettingsModel(
    val VisitorSecret : Int? = 0,
    val SwitchNotify : Int? = 1,
    val VisitSecret : Int? = 0,
    val HideVisit : Int? = 0
)