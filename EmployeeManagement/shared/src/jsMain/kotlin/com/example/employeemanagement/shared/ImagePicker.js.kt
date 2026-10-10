package com.example.employeemanagement.shared

actual fun pickImage(): String? {
    // In a real WebApp, you would use HTML <input type="file"> here.
    return "photo_url_placeholder.jpg" 
}
