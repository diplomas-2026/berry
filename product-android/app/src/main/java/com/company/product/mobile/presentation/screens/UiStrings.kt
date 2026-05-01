package com.company.product.mobile.presentation.screens

fun roleLabel(role: String?): String = when (role?.uppercase()) {
    "ADMIN" -> "Администратор"
    "CURATOR" -> "Куратор"
    "CHEF" -> "Повар"
    "STUDENT" -> "Студент"
    else -> role ?: "-"
}

fun mealSlotLabel(slot: String?): String = when (slot?.uppercase()) {
    "BREAKFAST" -> "Завтрак"
    "LUNCH" -> "Обед"
    "DINNER" -> "Ужин"
    else -> slot ?: "-"
}

fun voucherStatusLabel(status: String?): String = when (status?.uppercase()) {
    "ISSUED" -> "Выдан"
    "REDEEMED" -> "Погашен"
    "EXPIRED" -> "Просрочен"
    "CANCELLED" -> "Отменён"
    else -> status ?: "-"
}
