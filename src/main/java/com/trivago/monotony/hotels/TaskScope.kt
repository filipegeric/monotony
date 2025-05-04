package com.trivago.monotony.hotels

import java.util.concurrent.StructuredTaskScope

inline fun <T> taskScope(block: StructuredTaskScope.ShutdownOnFailure.() -> T): T =
    StructuredTaskScope.ShutdownOnFailure().use { it.block() }
