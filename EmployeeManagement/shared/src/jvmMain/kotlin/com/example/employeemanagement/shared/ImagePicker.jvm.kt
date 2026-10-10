package com.example.employeemanagement.shared

import java.awt.FileDialog
import java.awt.Frame

actual fun pickImage(): String? {
    val fileDialog = FileDialog(null as Frame?, "Select Photo", FileDialog.LOAD)
    fileDialog.isVisible = true
    return if (fileDialog.directory != null && fileDialog.file != null) {
        fileDialog.directory + fileDialog.file
    } else {
        null
    }
}
