package com.mymasimo.masimosleep.base.dispatchers

import kotlinx.coroutines.Dispatchers

/**
 * Implementation of [CoroutineDispatchers] for the Application.
 */
class AppDispatchers : CoroutineDispatchers {
    override fun main() = Dispatchers.Main
    override fun io() = Dispatchers.IO
    override fun default() = Dispatchers.Default
    override fun unconfined() = Dispatchers.Unconfined
}