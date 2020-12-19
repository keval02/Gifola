package com.gifola.model

import java.io.Serializable

class MemberDetail () : Serializable {
    val MemAddress: String = ""
    val MemCusId: Int = 0
    val MemId: Int = 0
    val Status: Int = 0
    val memberSites: List<MemberSite> = ArrayList()
}