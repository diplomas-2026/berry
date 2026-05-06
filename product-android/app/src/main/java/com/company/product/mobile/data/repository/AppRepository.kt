package com.company.product.mobile.data.repository

import android.content.Context
import android.net.Uri
import com.company.product.mobile.data.remote.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.time.LocalDate

class AppRepository(
    private val api: ApiService,
    private val sessionStore: com.company.product.mobile.data.local.SessionStore
) {
    suspend fun login(email: String, password: String): AuthResponse {
        val result = api.login(LoginRequest(email, password))
        sessionStore.saveToken(result.token)
        return result
    }

    fun logout() = sessionStore.clear()

    suspend fun me(): UserDto = api.me()

    suspend fun studentMenu() = api.studentTodayMenu()
    suspend fun studentVouchers() = api.studentVouchers()
    suspend fun studentQr() = api.studentQr()

    suspend fun curatorStudents() = api.curatorStudents()
    suspend fun curatorGroups() = api.curatorGroups()
    suspend fun curatorGroupStudents(groupId: Long) = api.curatorGroupStudents(groupId)
    suspend fun curatorStudentVouchers(studentId: Long) = api.curatorStudentVouchers(studentId)
    suspend fun issueVouchers(studentId: Long, date: String, slots: List<String>) =
        api.issueVoucher(IssueVoucherRequest(studentId, date, slots))
    suspend fun deleteCuratorVoucher(voucherId: Long) = api.deleteCuratorVoucher(voucherId)

    suspend fun chefDishes() = api.chefDishes()
    suspend fun dish(id: Long) = api.dish(id)
    suspend fun chefCreateDish(
        context: Context,
        name: String,
        description: String?,
        proteinsPer100g: String?,
        fatsPer100g: String?,
        carbsPer100g: String?,
        caloriesPer100g: String?,
        photoUri: Uri?
    ): DishDto = withContext(Dispatchers.IO) {
        api.chefCreateDish(
            name = textPart(name)!!,
            description = textPart(description),
            proteinsPer100g = numericPart(proteinsPer100g),
            fatsPer100g = numericPart(fatsPer100g),
            carbsPer100g = numericPart(carbsPer100g),
            caloriesPer100g = numericPart(caloriesPer100g),
            file = photoPart(context, photoUri)
        )
    }

    suspend fun chefUpdateDish(
        context: Context,
        id: Long,
        name: String,
        description: String?,
        proteinsPer100g: String?,
        fatsPer100g: String?,
        carbsPer100g: String?,
        caloriesPer100g: String?,
        photoUri: Uri?
    ): DishDto = withContext(Dispatchers.IO) {
        api.chefUpdateDish(
            id = id,
            name = textPart(name)!!,
            description = textPart(description),
            proteinsPer100g = numericPart(proteinsPer100g),
            fatsPer100g = numericPart(fatsPer100g),
            carbsPer100g = numericPart(carbsPer100g),
            caloriesPer100g = numericPart(caloriesPer100g),
            file = photoPart(context, photoUri)
        )
    }

    suspend fun chefMenuDates() = api.chefMenuDates()
    suspend fun chefMenu(date: String) = api.chefMenu(date)
    suspend fun chefAddMenuItem(date: String, mealSlot: String, dishId: Long) =
        api.chefAddMenuItem(AddMenuItemRequest(date, mealSlot, dishId))
    suspend fun chefUpdateMenuItem(id: Long, date: String, mealSlot: String, dishId: Long) =
        api.chefUpdateMenuItem(id, UpdateMenuItemRequest(date, mealSlot, dishId))
    suspend fun chefDeleteMenuItem(id: Long) = api.chefDeleteMenuItem(id)
    suspend fun chefScan(studentId: Long, date: String = LocalDate.now().toString()) =
        api.chefScan(QrPayload(studentId, date))
    suspend fun chefRedeem(voucherId: Long) = api.chefRedeem(RedeemRequest(voucherId))

    suspend fun adminUsers() = api.adminUsers()
    suspend fun adminCreateUser(email: String, password: String, firstName: String, lastName: String, middleName: String, role: String) =
        api.adminCreateUser(CreateUserRequest(email, password, firstName, lastName, middleName, role))
    suspend fun adminUpdateUser(id: Long, firstName: String, lastName: String, middleName: String, active: Boolean, role: String) =
        api.adminUpdateUser(id, UpdateUserRequest(firstName, lastName, middleName, active, role))
    suspend fun adminGroups() = api.adminGroups()
    suspend fun adminCreateGroup(name: String, curatorId: Long?) = api.adminCreateGroup(CreateGroupRequest(name, curatorId))
    suspend fun adminUpdateGroup(id: Long, name: String, curatorId: Long?) = api.adminUpdateGroup(id, UpdateGroupRequest(name, curatorId))
    suspend fun adminDeleteGroup(id: Long) = api.adminDeleteGroup(id)

    suspend fun deleteAvatar() = api.deleteAvatar()

    suspend fun uploadAvatar(context: Context, uri: Uri): UserDto = withContext(Dispatchers.IO) {
        val bytes = context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
            ?: error("Не удалось прочитать файл")
        val body = bytes.toRequestBody("image/*".toMediaTypeOrNull())
        val part = MultipartBody.Part.createFormData("file", "avatar.jpg", body)
        api.uploadAvatar(part)
    }

    private fun textPart(value: String?): RequestBody? {
        val normalized = value?.trim().orEmpty()
        if (normalized.isBlank()) return null
        return normalized.toRequestBody("text/plain".toMediaTypeOrNull())
    }

    private fun numericPart(value: String?): RequestBody? {
        val normalized = value?.trim().orEmpty().replace(',', '.')
        if (normalized.isBlank()) return null
        return normalized.toRequestBody("text/plain".toMediaTypeOrNull())
    }

    private fun photoPart(context: Context, uri: Uri?): MultipartBody.Part? {
        if (uri == null) return null
        val bytes = context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
            ?: error("Не удалось прочитать файл")
        val body = bytes.toRequestBody("image/*".toMediaTypeOrNull())
        return MultipartBody.Part.createFormData("file", "dish.jpg", body)
    }
}
