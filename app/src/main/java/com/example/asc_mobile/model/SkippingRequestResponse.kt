package com.example.asc_mobile.model

data class SkippingRequestResponse(
    val totalCount: Int,
    val list: List<SkippingRequest>
)

data class SkippingRequest(
    val id: String,
    val student: Student,
    val startDate: String,
    val endDate: String,
    val reason: String,
    val lessons: List<Int>?,
    val status: String,
    val confirmations: List<Confirmation>?
)

data class Student(
    val id: String,
    val name: String
)

data class Confirmation(
    val id: String,
    val filename: String,
    val filePath: String
)