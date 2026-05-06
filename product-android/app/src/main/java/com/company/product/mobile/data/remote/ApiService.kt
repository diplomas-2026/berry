package com.company.product.mobile.data.remote

import okhttp3.MultipartBody
import okhttp3.RequestBody
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

    @GET("api/student/menu")
    suspend fun studentMenu(@retrofit2.http.Query("date") date: String): List<MenuItemDto>

    @GET("api/student/menu/dates")
    suspend fun studentMenuDates(): List<String>

    @GET("api/student/vouchers")
    suspend fun studentVouchers(): List<VoucherDto>

    @GET("api/student/qr")
    suspend fun studentQr(): QrPayload

    @GET("api/curator/students")
    suspend fun curatorStudents(): List<UserDto>

    @GET("api/curator/groups")
    suspend fun curatorGroups(): List<GroupDto>

    @GET("api/curator/groups/{id}/students")
    suspend fun curatorGroupStudents(@Path("id") id: Long): List<UserDto>

    @GET("api/curator/students/{id}/vouchers")
    suspend fun curatorStudentVouchers(@Path("id") id: Long): List<VoucherDto>

    @POST("api/curator/vouchers/issue")
    suspend fun issueVoucher(@Body body: IssueVoucherRequest): List<VoucherDto>

    @DELETE("api/curator/vouchers/{id}")
    suspend fun deleteCuratorVoucher(@Path("id") id: Long)

    @GET("api/chef/dishes")
    suspend fun chefDishes(): List<DishDto>

    @GET("api/dishes/{id}")
    suspend fun dish(@Path("id") id: Long): DishDto

    @Multipart
    @POST("api/chef/dishes")
    suspend fun chefCreateDish(
        @Part("name") name: RequestBody,
        @Part("description") description: RequestBody?,
        @Part("proteinsPer100g") proteinsPer100g: RequestBody?,
        @Part("fatsPer100g") fatsPer100g: RequestBody?,
        @Part("carbsPer100g") carbsPer100g: RequestBody?,
        @Part("caloriesPer100g") caloriesPer100g: RequestBody?,
        @Part file: MultipartBody.Part?
    ): DishDto

    @Multipart
    @PATCH("api/chef/dishes/{id}")
    suspend fun chefUpdateDish(
        @Path("id") id: Long,
        @Part("name") name: RequestBody,
        @Part("description") description: RequestBody?,
        @Part("proteinsPer100g") proteinsPer100g: RequestBody?,
        @Part("fatsPer100g") fatsPer100g: RequestBody?,
        @Part("carbsPer100g") carbsPer100g: RequestBody?,
        @Part("caloriesPer100g") caloriesPer100g: RequestBody?,
        @Part file: MultipartBody.Part?
    ): DishDto

    @GET("api/chef/menu")
    suspend fun chefMenu(@retrofit2.http.Query("date") date: String): List<MenuItemDto>

    @GET("api/chef/menu/dates")
    suspend fun chefMenuDates(): List<String>

    @POST("api/chef/menu/items")
    suspend fun chefAddMenuItem(@Body body: AddMenuItemRequest): MenuItemDto

    @PATCH("api/chef/menu/items/{id}")
    suspend fun chefUpdateMenuItem(@Path("id") id: Long, @Body body: UpdateMenuItemRequest): MenuItemDto

    @DELETE("api/chef/menu/items/{id}")
    suspend fun chefDeleteMenuItem(@Path("id") id: Long)

    @POST("api/chef/scan")
    suspend fun chefScan(@Body body: QrPayload): ScanResultDto

    @POST("api/chef/redeem")
    suspend fun chefRedeem(@Body body: RedeemRequest): VoucherDto

    @GET("api/admin/users")
    suspend fun adminUsers(): List<UserDto>

    @POST("api/admin/users")
    suspend fun adminCreateUser(@Body body: CreateUserRequest): UserDto

    @PATCH("api/admin/users/{id}")
    suspend fun adminUpdateUser(@Path("id") id: Long, @Body body: UpdateUserRequest): UserDto

    @GET("api/admin/groups")
    suspend fun adminGroups(): List<GroupDto>

    @POST("api/admin/groups")
    suspend fun adminCreateGroup(@Body body: CreateGroupRequest): GroupDto

    @PATCH("api/admin/groups/{id}")
    suspend fun adminUpdateGroup(@Path("id") id: Long, @Body body: UpdateGroupRequest): GroupDto

    @DELETE("api/admin/groups/{id}")
    suspend fun adminDeleteGroup(@Path("id") id: Long)

    @Multipart
    @POST("api/profile/avatar")
    suspend fun uploadAvatar(@Part file: MultipartBody.Part): UserDto

    @DELETE("api/profile/avatar")
    suspend fun deleteAvatar(): UserDto
}
