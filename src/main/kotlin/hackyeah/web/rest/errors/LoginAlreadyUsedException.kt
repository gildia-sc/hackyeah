package hackyeah.web.rest.errors

class LoginAlreadyUsedException : BadRequestAlertException(ErrorConstants.LOGIN_ALREADY_USED_TYPE, "Login name already used!", "userManagement", "userexists") {
    companion object {

        private val serialVersionUID = 1L
    }
}
