package com.example.employeemanagement.server

import com.example.employeemanagement.shared.Employee
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.plugins.cors.routing.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq

fun main(args: Array<String>): Unit = EngineMain.main(args)

object Employees : Table() {
    val id = integer("id").autoIncrement()
    val firstName = varchar("first_name", 50)
    val lastName = varchar("last_name", 50)
    val email = varchar("email", 100)
    val dob = varchar("dob", 20)
    val photoUrl = varchar("photo_url", 255).nullable()
    override val primaryKey = PrimaryKey(id)
}

fun Application.module() {
    Database.connect(
        url = "jdbc:postgresql://localhost:5432/employeedb",
        driver = "org.postgresql.Driver",
        user = "postgres", // default PostgreSQL username
        password = "root"
    )
    
    transaction {
        SchemaUtils.create(Employees)
    }

    install(ContentNegotiation) {
        json()
    }
    
    install(CORS) {
        allowMethod(HttpMethod.Options)
        allowMethod(HttpMethod.Get)
        allowMethod(HttpMethod.Post)
        allowMethod(HttpMethod.Put)
        allowMethod(HttpMethod.Delete)
        allowHeader(io.ktor.http.HttpHeaders.Authorization)
        allowHeader(io.ktor.http.HttpHeaders.ContentType)
        anyHost()
    }

    routing {
        route("/employees") {
            // GET: Fetch all employees
            get {
                val searchQuery = call.request.queryParameters["search"]
                println("Fetching employees... Search Query: $searchQuery")
                
                val employees = newSuspendedTransaction {
                    val query = if (!searchQuery.isNullOrBlank()) {
                        Employees.select { 
                            (Employees.firstName like "%$searchQuery%") or 
                            (Employees.lastName like "%$searchQuery%") or 
                            (Employees.email like "%$searchQuery%") 
                        }
                    } else {
                        Employees.selectAll()
                    }
                    
                    query.map {
                        Employee(
                            id = it[Employees.id],
                            firstName = it[Employees.firstName],
                            lastName = it[Employees.lastName],
                            email = it[Employees.email],
                            dob = it[Employees.dob],
                            photoUrl = it[Employees.photoUrl]
                        )
                    }
                }
                call.respond(employees)
            }
            
            // POST: Add new employee
            post {
                val employee = call.receive<Employee>()
                println("Adding new employee: ${employee.firstName} ${employee.lastName}")
                newSuspendedTransaction {
                    Employees.insert {
                        it[firstName] = employee.firstName
                        it[lastName] = employee.lastName
                        it[email] = employee.email
                        it[dob] = employee.dob
                        it[photoUrl] = employee.photoUrl
                    }
                }
                call.respond(HttpStatusCode.Created)
            }
            
            // DELETE: Remove employee
            delete("/{id}") {
                val id = call.parameters["id"]?.toIntOrNull() ?: return@delete call.respond(HttpStatusCode.BadRequest)
                println("Deleting employee with ID: $id")
                newSuspendedTransaction {
                    Employees.deleteWhere { Employees.id eq id }
                }
                call.respond(HttpStatusCode.OK)
            }
            
            // PUT: Update employee
            put("/{id}") {
                val id = call.parameters["id"]?.toIntOrNull() ?: return@put call.respond(HttpStatusCode.BadRequest)
                val employee = call.receive<Employee>()
                println("Updating employee with ID: $id - New Name: ${employee.firstName} ${employee.lastName}")
                newSuspendedTransaction {
                    Employees.update({ Employees.id eq id }) {
                        it[firstName] = employee.firstName
                        it[lastName] = employee.lastName
                        it[email] = employee.email
                        it[dob] = employee.dob
                        it[photoUrl] = employee.photoUrl
                    }
                }
                call.respond(HttpStatusCode.OK)
            }
        }
    }
}
