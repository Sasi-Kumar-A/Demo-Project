package com.example.employeemanagement.composeapp

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.example.employeemanagement.shared.Employee
import com.example.employeemanagement.composeapp.EmployeePhoto
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

val httpClient = HttpClient {
    install(ContentNegotiation) {
        json(Json {
            ignoreUnknownKeys = true
            prettyPrint = true
        })
    }
}

// Ensure your backend is running here. Note for Android Emulator you may need to use 10.0.2.2 instead of localhost
val serverUrl = "http://localhost:8081/employees"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun App() {
    val coroutineScope = rememberCoroutineScope()
    var employees by remember { mutableStateOf(emptyList<Employee>()) }
    var searchQuery by remember { mutableStateOf("") }
    var employeeToEdit by remember { mutableStateOf<Employee?>(null) }
    var showAddDialog by remember { mutableStateOf(false) }

    fun fetchEmployees(query: String = "") {
        coroutineScope.launch {
            try {
                val url = if (query.isNotBlank()) "$serverUrl?search=$query" else serverUrl
                employees = httpClient.get(url).body()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    LaunchedEffect(Unit) {
        fetchEmployees()
    }

    MaterialTheme {
        Scaffold(
            floatingActionButton = {
                FloatingActionButton(onClick = { showAddDialog = true }) {
                    Text("+")
                }
            }
        ) { padding ->
            Column(
                modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(text = "Employee Dashboard", style = MaterialTheme.typography.headlineMedium)
                Spacer(modifier = Modifier.height(16.dp))
                
                Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        modifier = Modifier.weight(1f),
                        label = { Text("Search Employees") }
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(onClick = { fetchEmployees(searchQuery) }) {
                        Text("Search")
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))

                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(employees) { employee ->
                        Card(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp).fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    employee.photoUrl?.let { photoUrl ->
                                        EmployeePhoto(
                                            photoUrl = photoUrl, 
                                            modifier = Modifier.size(50.dp).clip(CircleShape)
                                        )
                                        Spacer(modifier = Modifier.width(16.dp))
                                    }
                                    Column {
                                        Text(text = "${employee.firstName} ${employee.lastName}", style = MaterialTheme.typography.titleMedium)
                                        Text(text = employee.email, style = MaterialTheme.typography.bodyMedium)
                                        Text(text = "DOB: ${employee.dob}", style = MaterialTheme.typography.bodySmall)
                                    }
                                }
                                Row {
                                    IconButton(onClick = { employeeToEdit = employee }) {
                                        Icon(Icons.Default.Edit, contentDescription = "Edit")
                                    }
                                    IconButton(onClick = {
                                        coroutineScope.launch {
                                            try {
                                                httpClient.delete("$serverUrl/${employee.id}")
                                                fetchEmployees(searchQuery)
                                            } catch (e: Exception) {
                                                e.printStackTrace()
                                            }
                                        }
                                    }) {
                                        Icon(Icons.Default.Delete, contentDescription = "Delete")
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        if (showAddDialog || employeeToEdit != null) {
            EmployeeDialog(
                employee = employeeToEdit,
                onDismiss = { 
                    showAddDialog = false
                    employeeToEdit = null
                },
                onSave = { savedEmployee ->
                    coroutineScope.launch {
                        try {
                            if (savedEmployee.id != null && savedEmployee.id != 0) {
                                httpClient.put("$serverUrl/${savedEmployee.id}") {
                                    contentType(ContentType.Application.Json)
                                    setBody(savedEmployee)
                                }
                            } else {
                                httpClient.post(serverUrl) {
                                    contentType(ContentType.Application.Json)
                                    setBody(savedEmployee)
                                }
                            }
                            fetchEmployees(searchQuery)
                            showAddDialog = false
                            employeeToEdit = null
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EmployeeDialog(employee: Employee?, onDismiss: () -> Unit, onSave: (Employee) -> Unit) {
    var firstName by remember { mutableStateOf(employee?.firstName ?: "") }
    var lastName by remember { mutableStateOf(employee?.lastName ?: "") }
    var email by remember { mutableStateOf(employee?.email ?: "") }
    var showDatePicker by remember { mutableStateOf(false) }
    var selectedDate by remember { mutableStateOf(employee?.dob ?: "Select Date") }
    var selectedPhotoUrl by remember { mutableStateOf<String?>(employee?.photoUrl) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (employee == null) "Add Employee" else "Edit Employee") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = firstName, onValueChange = { firstName = it }, label = { Text("First Name") })
                OutlinedTextField(value = lastName, onValueChange = { lastName = it }, label = { Text("Last Name") })
                OutlinedTextField(value = email, onValueChange = { email = it }, label = { Text("Email") })
                
                Button(onClick = { showDatePicker = true }, modifier = Modifier.fillMaxWidth()) {
                    Text("DOB: $selectedDate")
                }
                
                Button(
                    onClick = { 
                        val photoPath = com.example.employeemanagement.shared.pickImage()
                        if (photoPath != null) {
                            selectedPhotoUrl = photoPath
                        }
                    }, 
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(if (selectedPhotoUrl == null) "Upload Photo" else "Photo Selected!")
                }
            }
        },
        confirmButton = {
            Button(onClick = {
                onSave(Employee(
                    id = employee?.id ?: 0,
                    firstName = firstName, 
                    lastName = lastName, 
                    email = email, 
                    dob = selectedDate, 
                    photoUrl = selectedPhotoUrl
                ))
            }) {
                Text("Save")
            }
        },
        dismissButton = {
            Button(onClick = onDismiss) { Text("Cancel") }
        }
    )

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState()
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        val date = Instant.fromEpochMilliseconds(millis).toLocalDateTime(TimeZone.UTC).date
                        selectedDate = date.toString()
                    }
                    showDatePicker = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("Cancel") }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}
