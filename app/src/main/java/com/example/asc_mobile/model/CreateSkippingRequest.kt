package com.example.asc_mobile.model

data class CreateSkippingRequest(
    val startDate: String,
    val endDate: String,
    val reason: String,
    val lessons: List<Int>
)