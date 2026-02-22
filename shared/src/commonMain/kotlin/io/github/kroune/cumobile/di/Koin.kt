package io.github.kroune.cumobile.di

import org.koin.core.context.startKoin
import org.koin.dsl.module

val koinModule = module {
}

fun initKoin() {
  startKoin {
    modules(koinModule)
  }
}
