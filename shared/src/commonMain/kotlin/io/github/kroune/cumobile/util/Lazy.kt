package io.github.kroune.cumobile.util

operator fun <T> Lazy<T>.invoke(): T =
    value
