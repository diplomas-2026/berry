package com.company.product.mobile.data.remote

data class LoginRequest(val email: String, val password: String)
data class AuthResponse(
    val token: String,
    val userId: Long,
    val email: String,
    val fullName: String,
    val role: String,
    val avatarUrl: String?
)

data class UserDto(
    val id: Long,
    val email: String,
    val fullName: String,
    val role: String,
    val active: Boolean,
    val avatarUrl: String?
)

data class DishDto(
    val id: Long,
    val name: String,
    val description: String?,
    val proteinsPer100g: Double?,
    val fatsPer100g: Double?,
    val carbsPer100g: Double?,
    val caloriesPer100g: Double?,
    val photoUrl: String?
)

data class MenuItemDto(
    val id: Long,
    val date: String,
    val mealSlot: String,
    val dish: DishDto
)

data class VoucherDto(
    val id: Long,
    val studentId: Long,
    val studentName: String,
    val issueDate: String,
    val mealSlot: String,
    val status: String,
    val redeemedAt: String?
)

data class QrPayload(val studentId: Long, val date: String)
data class ScanResultDto(
    val student: UserDto,
    val activeVouchers: List<VoucherDto>,
    val menuItems: List<MenuItemDto>
)

data class IssueVoucherRequest(
    val studentId: Long,
    val date: String,
    val slots: List<String>
)

data class RedeemRequest(val voucherId: Long)

data class CreateUserRequest(
    val email: String,
    val password: String,
    val fullName: String,
    val role: String
)
