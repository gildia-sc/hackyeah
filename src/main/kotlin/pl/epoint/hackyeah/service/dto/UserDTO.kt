package pl.epoint.hackyeah.service.dto

import pl.epoint.hackyeah.domain.Player

import javax.validation.constraints.Email
import javax.validation.constraints.NotBlank
import javax.validation.constraints.Pattern
import javax.validation.constraints.Size
import java.time.Instant
import java.util.stream.Collectors

/**
 * A DTO representing a user, with his authorities.
 */
open class UserDTO {

    var id: Long? = null

    @NotBlank
    @Pattern(regexp = "^[_.@A-Za-z0-9-]*$")
    @Size(min = 1, max = 50)
    var login: String? = null

    @Size(max = 50)
    var firstName: String? = null

    @Size(max = 50)
    var lastName: String? = null

    @Email
    @Size(min = 5, max = 254)
    var email: String? = null

    var image: String? = null

    var isActivated = false

    @Size(min = 2, max = 6)
    var langKey: String? = null

    var createdBy: String? = null

    var createdDate: Instant? = null

    var lastModifiedBy: String? = null

    var lastModifiedDate: Instant? = null

    var authorities: Set<String>? = null

    constructor() {
        // Empty constructor needed for Jackson.
    }

    constructor(player: Player) {
        this.id = player.id
        this.login = player.login
        this.firstName = player.firstName
        this.lastName = player.lastName
        this.email = player.email
        this.isActivated = player.activated
        this.image = player.image
        this.langKey = player.langKey
        this.createdBy = player.createdBy
        this.createdDate = player.createdDate
        this.lastModifiedBy = player.lastModifiedBy
        this.lastModifiedDate = player.lastModifiedDate
        this.authorities = player.authorities.stream()
                .map<String> { it.name }
                .collect (Collectors.toSet())
    }

    override fun toString(): String {
        return "UserDTO{" +
                "login='" + login + '\''.toString() +
                ", firstName='" + firstName + '\''.toString() +
                ", lastName='" + lastName + '\''.toString() +
                ", email='" + email + '\''.toString() +
                ", image='" + image + '\''.toString() +
                ", activated=" + isActivated +
                ", langKey='" + langKey + '\''.toString() +
                ", createdBy=" + createdBy +
                ", createdDate=" + createdDate +
                ", lastModifiedBy='" + lastModifiedBy + '\''.toString() +
                ", lastModifiedDate=" + lastModifiedDate +
                ", authorities=" + authorities +
                "}"
    }
}
