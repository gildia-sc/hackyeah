package pl.epoint.hackyeah.service.dto

/**
 * A DTO representing a password change required data - current and new password.
 */
class PasswordChangeDTO {
    var currentPassword: String? = null
    var newPassword: String? = null

    constructor() {
        // Empty constructor needed for Jackson.
    }

    constructor(currentPassword: String, newPassword: String) {
        this.currentPassword = currentPassword
        this.newPassword = newPassword
    }
}
