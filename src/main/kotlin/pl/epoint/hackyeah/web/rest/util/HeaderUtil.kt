package pl.epoint.hackyeah.web.rest.util

import org.slf4j.LoggerFactory
import org.springframework.http.HttpHeaders

/**
 * Utility class for HTTP headers creation.
 */
object HeaderUtil {

    private val log = LoggerFactory.getLogger(HeaderUtil::class.java)

    private val APPLICATION_NAME = "hackyeahApp"

    fun createAlert(message: String, param: String): HttpHeaders {
        val headers = HttpHeaders()
        headers.add("X-$APPLICATION_NAME-alert", message)
        headers.add("X-$APPLICATION_NAME-params", param)
        return headers
    }

    fun createEntityCreationAlert(entityName: String, param: String): HttpHeaders {
        return createAlert("$APPLICATION_NAME.$entityName.created", param)
    }

    fun createEntityUpdateAlert(entityName: String, param: String): HttpHeaders {
        return createAlert("$APPLICATION_NAME.$entityName.updated", param)
    }

    fun createEntityDeletionAlert(entityName: String, param: String): HttpHeaders {
        return createAlert("$APPLICATION_NAME.$entityName.deleted", param)
    }

    fun createFailureAlert(entityName: String, errorKey: String, defaultMessage: String): HttpHeaders {
        log.error("Entity processing failed, {}", defaultMessage)
        val headers = HttpHeaders()
        headers.add("X-$APPLICATION_NAME-error", "error.$errorKey")
        headers.add("X-$APPLICATION_NAME-params", entityName)
        return headers
    }
}
