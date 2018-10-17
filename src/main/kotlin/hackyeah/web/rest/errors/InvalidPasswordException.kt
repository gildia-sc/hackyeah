package hackyeah.web.rest.errors

import org.zalando.problem.AbstractThrowableProblem
import org.zalando.problem.Status

class InvalidPasswordException : AbstractThrowableProblem(ErrorConstants.INVALID_PASSWORD_TYPE, "Incorrect password", Status.BAD_REQUEST) {
    companion object {

        private val serialVersionUID = 1L
    }
}
