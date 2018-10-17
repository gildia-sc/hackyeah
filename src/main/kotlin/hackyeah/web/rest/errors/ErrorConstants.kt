package hackyeah.web.rest.errors

import java.net.URI

object ErrorConstants {

    val ERR_CONCURRENCY_FAILURE = "error.concurrencyFailure"
    val ERR_VALIDATION = "error.validation"
    val PROBLEM_BASE_URL = "https://www.jhipster.tech/problem"
    val DEFAULT_TYPE = URI.create("$PROBLEM_BASE_URL/problem-with-message")
    val CONSTRAINT_VIOLATION_TYPE = URI.create("$PROBLEM_BASE_URL/constraint-violation")
    val PARAMETERIZED_TYPE = URI.create("$PROBLEM_BASE_URL/parameterized")
    val ENTITY_NOT_FOUND_TYPE = URI.create("$PROBLEM_BASE_URL/entity-not-found")
    val INVALID_PASSWORD_TYPE = URI.create("$PROBLEM_BASE_URL/invalid-password")
    val EMAIL_ALREADY_USED_TYPE = URI.create("$PROBLEM_BASE_URL/email-already-used")
    val LOGIN_ALREADY_USED_TYPE = URI.create("$PROBLEM_BASE_URL/login-already-used")
    val EMAIL_NOT_FOUND_TYPE = URI.create("$PROBLEM_BASE_URL/email-not-found")
}
