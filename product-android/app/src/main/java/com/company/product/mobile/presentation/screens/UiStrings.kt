package com.company.product.mobile.presentation.screens

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.Locale

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

private val RussianDateFormatter: DateTimeFormatter = DateTimeFormatter
    .ofLocalizedDate(FormatStyle.LONG)
    .withLocale(Locale("ru", "RU"))

fun formatDate(date: String?): String {
    if (date.isNullOrBlank()) return "-"
    return runCatching { LocalDate.parse(date).format(RussianDateFormatter) }
        .getOrElse { date }
}
