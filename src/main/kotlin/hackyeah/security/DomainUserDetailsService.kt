package hackyeah.security

import hackyeah.domain.User
import hackyeah.repository.UserRepository
import org.hibernate.validator.internal.constraintvalidators.hv.EmailValidator
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.util.Locale
import java.util.stream.Collectors

/**
 * Authenticate a user from the database.
 */
@Component("userDetailsService")
class DomainUserDetailsService(private val userRepository: UserRepository) : UserDetailsService {

    private val log = LoggerFactory.getLogger(DomainUserDetailsService::class.java)

    @Transactional
    override fun loadUserByUsername(login: String): UserDetails {
        log.debug("Authenticating {}", login)

        if (EmailValidator().isValid(login, null)) {
            return userRepository.findOneWithAuthoritiesByEmail(login)
                    .map<User> { user -> createSpringSecurityUser(login, user) }
                    .orElseThrow { UsernameNotFoundException("User with email $login was not found in the database") }
        }

        val lowercaseLogin = login.toLowerCase(Locale.ENGLISH)
        return userRepository.findOneWithAuthoritiesByLogin(lowercaseLogin)
                .map<User> { user -> createSpringSecurityUser(lowercaseLogin, user) }
                .orElseThrow { UsernameNotFoundException("User $lowercaseLogin was not found in the database") }

    }

    private fun createSpringSecurityUser(lowercaseLogin: String, user: User): org.springframework.security.core.userdetails.User {
        if (!user.activated) {
            throw UserNotActivatedException("User $lowercaseLogin was not activated")
        }
        val grantedAuthorities = user.authorities.stream()
                .map { authority -> SimpleGrantedAuthority(authority.name) }
                .collect<List<GrantedAuthority>, Any>(Collectors.toList())
        return org.springframework.security.core.userdetails.User(user.login!!,
                user.password!!,
                grantedAuthorities)
    }
}
