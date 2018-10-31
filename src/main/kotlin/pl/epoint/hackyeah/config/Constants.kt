package pl.epoint.hackyeah.config

/**
 * Application constants.
 */
object Constants {

    // Regex for acceptable logins
    val LOGIN_REGEX = "^[_.@A-Za-z0-9-]*$"

    @JvmField val SYSTEM_ACCOUNT = "system"
    @JvmField val ANONYMOUS_USER = "anonymoususer"
    @JvmField val DEFAULT_LANGUAGE = "pl"
}
