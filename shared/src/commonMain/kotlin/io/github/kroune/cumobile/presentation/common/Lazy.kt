package io.github.kroune.cumobile.presentation.common

operator fun <T> Lazy<T>.invoke(): T =
    value
