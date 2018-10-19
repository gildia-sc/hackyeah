package pl.epoint.hackyeah.security

import pl.epoint.hackyeah.config.Constants
import org.springframework.data.domain.AuditorAware
import org.springframework.stereotype.Component

import java.util.Optional

/**
 * Implementation of AuditorAware based on Spring Security.
 */
@Component
class SpringSecurityAuditorAware : AuditorAware<String> {

    override fun getCurrentAuditor(): Optional<String> {
        return Optional.of(SecurityUtils.currentUserLogin.orElse(Constants.SYSTEM_ACCOUNT))
    }
}
