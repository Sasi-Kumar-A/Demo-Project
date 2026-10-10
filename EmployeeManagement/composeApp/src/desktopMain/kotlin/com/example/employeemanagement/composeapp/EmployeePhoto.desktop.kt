package com.example.employeemanagement.composeapp

import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toComposeImageBitmap
import androidx.compose.ui.layout.ContentScale
import org.jetbrains.skia.Image.Companion.makeFromEncoded
import java.io.File

@Composable
actual fun EmployeePhoto(photoUrl: String, modifier: Modifier) {
    val imageBitmap = remember(photoUrl) {
        try {
            val file = File(photoUrl)
            if (file.exists()) {
                makeFromEncoded(file.readBytes()).toComposeImageBitmap()
            } else null
        } catch (e: Exception) {
            null
        }
    }
    
    if (imageBitmap != null) {
        Image(bitmap = imageBitmap, contentDescription = "Employee Photo", modifier = modifier, contentScale = ContentScale.Crop)
    }
}
