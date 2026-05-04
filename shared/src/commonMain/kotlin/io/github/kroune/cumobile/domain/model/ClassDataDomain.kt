package io.github.kroune.cumobile.domain.model

data class ClassDataDomain(
    val startTime: String,
    val endTime: String,
    val room: String,
    val type: String,
    val title: String,
    val professor: String? = null,
    val link: String? = null,
    val badge: String? = null,
)
