package pl.epoint.hackyeah.extension

import java.util.*

fun <T> Optional<T>.unwrap(): T? {
    return this.orElse(null)
}

fun <T> Optional<T>.unwrapOrThrow(exceptionSupplier: () -> Throwable): T {
    return this.orElseThrow(exceptionSupplier)
}
