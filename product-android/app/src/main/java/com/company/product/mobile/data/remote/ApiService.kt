package com.company.product.mobile.data.remote

import okhttp3.MultipartBody
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path

interface ApiService {
    @POST("api/auth/login")
    suspend fun login(@Body body: LoginRequest): AuthResponse

    @GET("api/auth/me")
    suspend fun me(): UserDto

    @GET("api/student/menu/today")
    suspend fun studentTodayMenu(): List<MenuItemDto>

    @GET("api/student/vouchers")
    suspend fun studentVouchers(): List<VoucherDto>

    @GET("api/student/qr")
    suspend fun studentQr(): QrPayload

    @GET("api/curator/students")
    suspend fun curatorStudents(): List<UserDto>

    @POST("api/curator/vouchers/issue")
    suspend fun issueVoucher(@Body body: IssueVoucherRequest): List<VoucherDto>

    @GET("api/chef/menu/current")
    suspend fun chefMenuCurrent(): List<MenuItemDto>

    @POST("api/chef/scan")
    suspend fun chefScan(@Body body: QrPayload): ScanResultDto

    @POST("api/chef/redeem")
    suspend fun chefRedeem(@Body body: RedeemRequest): VoucherDto

    @GET("api/admin/users")
    suspend fun adminUsers(): List<UserDto>

    @POST("api/admin/users")
    suspend fun adminCreateUser(@Body body: CreateUserRequest): UserDto

    @PATCH("api/admin/users/{id}")
    suspend fun adminUpdateUser(@Path("id") id: Long, @Body body: Map<String, Boolean>): UserDto

    @Multipart
    @POST("api/profile/avatar")
    suspend fun uploadAvatar(@Part file: MultipartBody.Part): UserDto

    @DELETE("api/profile/avatar")
    suspend fun deleteAvatar(): UserDto
}
