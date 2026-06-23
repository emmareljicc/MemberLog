package com.fidit.memberlog.model

data class Member(
    val id: Int,
    val name: String,
    val role: String,
    val joinDate: String,
    val isPaid: Boolean,
    val email: String,
    val phone: String
)
