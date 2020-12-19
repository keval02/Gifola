package com.gifola.model

data class DashboardMainListModel(
    val AllowVisitor: List<DashboardListData>,
    val ReceiveRequest: List<DashboardListData>,
    val SentRequest: List<DashboardListData>
)