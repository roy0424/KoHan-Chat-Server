package com.kohan.message.rest.util

import java.time.LocalDateTime

fun <T1, T2> transformList(
    list: List<T1>,
    transform: (T1) -> T2,
): List<T2> = list.map { transform(it) }

fun <T> addItemInList(
    list: List<T>,
    item: T,
): List<T> = list + item

fun isExpiredAfterDuration(
    target: LocalDateTime,
    duration: Long,
): Boolean = target.plusMinutes(duration).isBefore(LocalDateTime.now())
