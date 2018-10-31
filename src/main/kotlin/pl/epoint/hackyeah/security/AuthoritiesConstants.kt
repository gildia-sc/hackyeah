package pl.epoint.hackyeah.security

/**
 * Constants for Spring Security authorities.
 */
object AuthoritiesConstants {

    @JvmField val ADMIN = "ROLE_ADMIN"

    @JvmField val USER = "ROLE_USER"

    @JvmField val ANONYMOUS = "ROLE_ANONYMOUS"
}
