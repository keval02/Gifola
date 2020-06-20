package com.gifola.model

import java.io.Serializable

class LocationDataModelItem() : Serializable{
    var mem_id: Int? = 0
    var mem_rf_per: Int? = 0
    var mem_sub_mem_per: Int? = 0
    var mem_uhf_per: Int? = 0
    var mem_vistor_allow: Int? = 0
    var site_id: Int? = 0
    var site_title: String? = ""
}