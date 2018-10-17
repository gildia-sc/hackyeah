package hackyeah.web.rest.errors

import org.zalando.problem.AbstractThrowableProblem
import org.zalando.problem.Status

class EmailNotFoundException : AbstractThrowableProblem(ErrorConstants.EMAIL_NOT_FOUND_TYPE, "Email address not registered", Status.BAD_REQUEST) {
    companion object {

        private val serialVersionUID = 1L
    }
}
