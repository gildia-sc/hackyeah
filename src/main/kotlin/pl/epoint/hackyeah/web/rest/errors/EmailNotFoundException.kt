package pl.epoint.hackyeah.web.rest.errors

import org.zalando.problem.AbstractThrowableProblem
import org.zalando.problem.Exceptional
import org.zalando.problem.Status

class EmailNotFoundException : AbstractThrowableProblem(ErrorConstants.EMAIL_NOT_FOUND_TYPE, "Email address not registered", Status.BAD_REQUEST) {
    override fun getCause(): Exceptional {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    companion object {

        private val serialVersionUID = 1L
    }
}
