package com.example.employeemanagement.composeapp

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
expect fun EmployeePhoto(photoUrl: String, modifier: Modifier = Modifier)
