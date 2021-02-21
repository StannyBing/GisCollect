package com.gt.base.manager

import org.json.JSONObject
import java.io.Serializable

/**
 * Updated by dell on 2020-01-02
 */
data class UserBean(
    val admin: Boolean,
    val companyId: String,
    val companyName: String,
    val empId: String,
    val partAdmin: Boolean,
    val rnCode: String,
    val rnName: String,
    val userId: String,
    val userName: String,
    val businesses : Any,
    val roleId : String
) : Serializable