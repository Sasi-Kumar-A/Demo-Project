package com.example.employeemanagement.composeapp

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application

fun main() = application {
    Window(onCloseRequest = ::exitApplication, title = "Employee Management") {
        App()
    }
}
