package pl.epoint.hackyeah.security

import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.core.userdetails.UserDetails
import java.util.*

/**
 * Utility class for Spring Security.
 */
object SecurityUtils {

    /**
     * Get the login of the current user.
     *
     * @return the login of the current user
     */
    @JvmStatic val currentUserLogin: Optional<String>
        get() {
            val securityContext = SecurityContextHolder.getContext()
            return Optional.ofNullable(securityContext.authentication)
                .map { authentication ->
                    if (authentication.principal is UserDetails) {
                        val springSecurityUser = authentication.principal as UserDetails
                        return@map springSecurityUser.username
                    } else if (authentication.principal is String) {
                        return@map securityContext.authentication
                            .principal as String
                    }
                    return@map null
                }
        }

    /**
     * Check if a user is authenticated.
     *
     * @return true if the user is authenticated, false otherwise
     */
    @JvmStatic val isAuthenticated: Boolean
        get() {
            val securityContext = SecurityContextHolder.getContext()
            return Optional.ofNullable(securityContext.authentication)
                .map<Boolean> { authentication ->
                    authentication.authorities
                        .stream()
                        .noneMatch { grantedAuthority -> grantedAuthority.getAuthority() == AuthoritiesConstants.ANONYMOUS }
                }
                .orElse(false)
        }

    /**
     * If the current user has a specific authority (security role).
     *
     *
     * The name of this method comes from the isUserInRole() method in the Servlet API
     *
     * @param authority the authority to check
     * @return true if the current user has the authority, false otherwise
     */
    @JvmStatic fun isCurrentUserInRole(authority: String): Boolean {
        val securityContext = SecurityContextHolder.getContext()
        return Optional.ofNullable(securityContext.authentication)
            .map<Boolean> { authentication ->
                authentication.getAuthorities().stream()
                    .anyMatch({ grantedAuthority -> grantedAuthority.getAuthority() == authority })
            }
            .orElse(false)
    }
}
