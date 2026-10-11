package com.localdeals.finder.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.localdeals.finder.domain.model.Role

@Entity(
    tableName = "users",
    indices = [
        Index(value = ["email"], unique = true)
    ]
)
data class UserEntity(
    @PrimaryKey
    val id: String,
    val name: String,
    val email: String,
    val passwordHash: String,
    val passwordSalt: String,
    val phone: String = "",
    val role: Role = Role.USER,
    val searchRadiusKm: Int = 5,
    val language: String = "English",
    val isBlocked: Boolean = false
)
