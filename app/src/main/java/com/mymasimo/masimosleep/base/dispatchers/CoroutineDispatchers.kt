package com.mymasimo.masimosleep.base.dispatchers

import kotlinx.coroutines.CoroutineDispatcher

/**
 * Interface for providing of [CoroutineDispatcher].
 */
interface CoroutineDispatchers {
    fun main(): CoroutineDispatcher
    fun io(): CoroutineDispatcher
    fun default(): CoroutineDispatcher
    fun unconfined(): CoroutineDispatcher
}