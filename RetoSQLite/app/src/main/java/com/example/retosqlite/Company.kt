package com.example.retosqlite

data class Company(
    var id: Int = 0,
    var name: String,
    var website: String?,
    var phone: String?,
    var email: String?,
    var services: String?,
    var classification: String? // "Consultoría", "Desarrollo a la medida", "Fábrica de software"
)