package pl.epoint.hackyeah.security

import pl.epoint.hackyeah.domain.PersistentToken
import pl.epoint.hackyeah.repository.PersistentTokenRepository
import pl.epoint.hackyeah.repository.UserRepository
import pl.epoint.hackyeah.service.util.RandomUtil
import io.github.jhipster.config.JHipsterProperties
import io.github.jhipster.security.PersistentTokenCache
import org.slf4j.LoggerFactory
import org.springframework.dao.DataAccessException
import org.springframework.security.core.Authentication
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.security.web.authentication.rememberme.AbstractRememberMeServices
import org.springframework.security.web.authentication.rememberme.CookieTheftException
import org.springframework.security.web.authentication.rememberme.InvalidCookieException
import org.springframework.security.web.authentication.rememberme.RememberMeAuthenticationException
import org.springframework.stereotype.Service

import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import java.io.Serializable
import java.time.LocalDate
import java.util.Arrays

/**
 * Custom implementation of Spring Security's RememberMeServices.
 *
 *
 * Persistent tokens are used by Spring Security to automatically log in users.
 *
 *
 * This is a specific implementation of Spring Security's remember-me authentication, but it is much
 * more powerful than the standard implementations:
 *
 *  * It allows a user to see the list of his currently opened sessions, and invalidate them
 *  * It stores more information, such as the IP address and the user agent, for audit purposes *
 *  * When a user logs out, only his current session is invalidated, and not all of his sessions
 *
 *
 *
 * Please note that it allows the use of the same token for 5 seconds, and this value stored in a specific
 * cache during that period. This is to allow concurrent requests from the same user: otherwise, two
 * requests being sent at the same time could invalidate each other's token.
 *
 *
 * This is inspired by:
 *
 *  * [Improved Persistent Login Cookie
 * Best Practice](http://jaspan.com/improved_persistent_login_cookie_best_practice)
 *  * [GitHub's "Modeling your App's User Session"](https://github.com/blog/1661-modeling-your-app-s-user-session)
 *
 *
 *
 * The main algorithm comes from Spring Security's PersistentTokenBasedRememberMeServices, but this class
 * couldn't be cleanly extended.
 */
@Service
class PersistentTokenRememberMeServices(jHipsterProperties: JHipsterProperties,
                                        userDetailsService: org.springframework.security.core.userdetails.UserDetailsService,
                                        private val persistentTokenRepository: PersistentTokenRepository, private val userRepository: UserRepository) : AbstractRememberMeServices(jHipsterProperties.security.rememberMe.key, userDetailsService) {

    private val log = LoggerFactory.getLogger(PersistentTokenRememberMeServices::class.java)

    private val upgradedTokenCache: PersistentTokenCache<UpgradedRememberMeToken>

    init {
        upgradedTokenCache = PersistentTokenCache(UPGRADED_TOKEN_VALIDITY_MILLIS)
    }

    override fun processAutoLoginCookie(cookieTokens: Array<String>, request: HttpServletRequest,
                                        response: HttpServletResponse): UserDetails {

        synchronized(this) {
            // prevent 2 authentication requests from the same user in parallel
            var login: String? = null
            val upgradedToken = upgradedTokenCache.get(cookieTokens[0])
            if (upgradedToken != null) {
                login = upgradedToken.getUserLoginIfValid(cookieTokens)
                log.debug("Detected previously upgraded login token for user '{}'", login)
            }

            if (login == null) {
                val token = getPersistentToken(cookieTokens)
                login = token.user!!.login

                // Token also matches, so login is valid. Update the token value, keeping the *same* series number.
                log.debug("Refreshing persistent login token for user '{}', series '{}'", login, token.series)
                token.tokenDate = LocalDate.now()
                token.tokenValue = RandomUtil.generateTokenData()
                token.ipAddress = request.remoteAddr
                token.userAgent = request.getHeader("User-Agent")
                try {
                    persistentTokenRepository.saveAndFlush(token)
                } catch (e: DataAccessException) {
                    log.error("Failed to update token: ", e)
                    throw RememberMeAuthenticationException("Autologin failed due to data access problem", e)
                }

                addCookie(token, request, response)
                upgradedTokenCache.put(cookieTokens[0], UpgradedRememberMeToken(cookieTokens, login!!))
            }
            return userDetailsService.loadUserByUsername(login)
        }
    }

    override fun onLoginSuccess(request: HttpServletRequest, response: HttpServletResponse, successfulAuthentication: Authentication) {

        val login = successfulAuthentication.name

        log.debug("Creating new persistent login for user {}", login)
        val token = userRepository.findOneByLogin(login).map { u ->
            val t = PersistentToken()
            t.series = RandomUtil.generateSeriesData()
            t.user = u
            t.tokenValue = RandomUtil.generateTokenData()
            t.tokenDate = LocalDate.now()
            t.ipAddress = request.remoteAddr
            t.userAgent = request.getHeader("User-Agent")
            t
        }.orElseThrow { UsernameNotFoundException("User $login was not found in the database") }
        try {
            persistentTokenRepository.saveAndFlush(token)
            addCookie(token, request, response)
        } catch (e: DataAccessException) {
            log.error("Failed to save persistent token ", e)
        }

    }

    /**
     * When logout occurs, only invalidate the current token, and not all user sessions.
     *
     *
     * The standard Spring Security implementations are too basic: they invalidate all tokens for the
     * current user, so when he logs out from one browser, all his other sessions are destroyed.
     */
    override fun logout(request: HttpServletRequest, response: HttpServletResponse, authentication: Authentication?) {
        val rememberMeCookie = extractRememberMeCookie(request)
        if (rememberMeCookie != null && rememberMeCookie.length != 0) {
            try {
                val cookieTokens = decodeCookie(rememberMeCookie)
                val token = getPersistentToken(cookieTokens)
                persistentTokenRepository.delete(token)
            } catch (ice: InvalidCookieException) {
                log.info("Invalid cookie, no persistent token could be deleted", ice)
            } catch (rmae: RememberMeAuthenticationException) {
                log.debug("No persistent token found, so no token could be deleted", rmae)
            }

        }
        super.logout(request, response, authentication)
    }

    /**
     * Validate the token and return it.
     */
    private fun getPersistentToken(cookieTokens: Array<String>): PersistentToken {
        if (cookieTokens.size != 2) {
            throw InvalidCookieException("Cookie token did not contain " + 2 +
                    " tokens, but contained '" + Arrays.asList(*cookieTokens) + "'")
        }
        val presentedSeries = cookieTokens[0]
        val presentedToken = cookieTokens[1]
        val optionalToken = persistentTokenRepository.findById(presentedSeries)
        if (!optionalToken.isPresent) {
            // No series match, so we can't authenticate using this cookie
            throw RememberMeAuthenticationException("No persistent token found for series id: $presentedSeries")
        }
        val token = optionalToken.get()
        // We have a match for this user/series combination
        log.info("presentedToken={} / tokenValue={}", presentedToken, token.tokenValue)
        if (presentedToken != token.tokenValue) {
            // Token doesn't match series value. Delete this session and throw an exception.
            persistentTokenRepository.delete(token)
            throw CookieTheftException("Invalid remember-me token (Series/token) mismatch. Implies previous " + "cookie theft attack.")
        }

        if (token.tokenDate!!.plusDays(TOKEN_VALIDITY_DAYS.toLong()).isBefore(LocalDate.now())) {
            persistentTokenRepository.delete(token)
            throw RememberMeAuthenticationException("Remember-me login has expired")
        }
        return token
    }

    private fun addCookie(token: PersistentToken, request: HttpServletRequest, response: HttpServletResponse) {
        setCookie(
                arrayOf(token.series, token.tokenValue),
                TOKEN_VALIDITY_SECONDS, request, response)
    }

    private class UpgradedRememberMeToken internal constructor(private val upgradedToken: Array<String>, private val userLogin: String) : Serializable {

        internal fun getUserLoginIfValid(currentToken: Array<String>): String? {
            return if (currentToken[0] == this.upgradedToken[0] && currentToken[1] == this.upgradedToken[1]) {
                this.userLogin
            } else null
        }

        companion object {

            private const val serialVersionUID = 1L
        }
    }

    companion object {

        // Token is valid for one month
        private val TOKEN_VALIDITY_DAYS = 31

        private val TOKEN_VALIDITY_SECONDS = 60 * 60 * 24 * TOKEN_VALIDITY_DAYS

        private val UPGRADED_TOKEN_VALIDITY_MILLIS = 5000L
    }
}
