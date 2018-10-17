package hackyeah.web.rest.errors

class EmailAlreadyUsedException : BadRequestAlertException(ErrorConstants.EMAIL_ALREADY_USED_TYPE, "Email is already in use!", "userManagement", "emailexists") {
    companion object {

        private val serialVersionUID = 1L
    }
}
