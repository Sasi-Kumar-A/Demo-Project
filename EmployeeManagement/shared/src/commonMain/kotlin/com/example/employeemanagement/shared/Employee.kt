package com.example.employeemanagement.shared

import kotlinx.serialization.Serializable

@Serializable
data class Employee(
    val id: Int = 0,
    val firstName: String,
    val lastName: String,
    val email: String,
    val dob: String,
    val photoUrl: String? = null
)
