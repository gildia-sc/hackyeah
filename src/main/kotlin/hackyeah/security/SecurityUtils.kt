package hackyeah.security

import org.springframework.security.core.context.SecurityContext
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.core.userdetails.UserDetails

import java.util.Optional

/**
 * Utility class for Spring Security.
 */
object SecurityUtils {

    /**
     * Get the login of the current user.
     *
     * @return the login of the current user
     */
    val currentUserLogin: Optional<String>
        get() {
            val securityContext = SecurityContextHolder.getContext()
            return Optional.ofNullable<Authentication>(securityContext.authentication)
                    .map { authentication ->
                        if (authentication.getPrincipal() is UserDetails) {
                            val springSecurityUser = authentication.getPrincipal() as UserDetails
                            return@Optional.ofNullable(securityContext.authentication)
                                    .map springSecurityUser . getUsername ()
                        } else if (authentication.getPrincipal() is String) {
                            return@Optional.ofNullable(securityContext.authentication)
                                    .map authentication . getPrincipal () as String
                        }
                        null
                    }
        }

    /**
     * Check if a user is authenticated.
     *
     * @return true if the user is authenticated, false otherwise
     */
    val isAuthenticated: Boolean
        get() {
            val securityContext = SecurityContextHolder.getContext()
            return Optional.ofNullable<Authentication>(securityContext.authentication)
                    .map<Boolean> { authentication ->
                        authentication.getAuthorities().stream()
                                .noneMatch({ grantedAuthority -> grantedAuthority.getAuthority() == AuthoritiesConstants.ANONYMOUS })
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
    fun isCurrentUserInRole(authority: String): Boolean {
        val securityContext = SecurityContextHolder.getContext()
        return Optional.ofNullable<Authentication>(securityContext.authentication)
                .map<Boolean> { authentication ->
                    authentication.getAuthorities().stream()
                            .anyMatch({ grantedAuthority -> grantedAuthority.getAuthority() == authority })
                }
                .orElse(false)
    }
}
