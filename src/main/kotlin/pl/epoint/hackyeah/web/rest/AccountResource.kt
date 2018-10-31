package pl.epoint.hackyeah.web.rest

import com.codahale.metrics.annotation.Timed
import pl.epoint.hackyeah.domain.PersistentToken
import pl.epoint.hackyeah.repository.PersistentTokenRepository
import pl.epoint.hackyeah.repository.PlayerRepository
import pl.epoint.hackyeah.security.SecurityUtils
import pl.epoint.hackyeah.service.MailService
import pl.epoint.hackyeah.service.UserService
import pl.epoint.hackyeah.service.dto.PasswordChangeDTO
import pl.epoint.hackyeah.service.dto.UserDTO
import pl.epoint.hackyeah.web.rest.errors.EmailAlreadyUsedException
import pl.epoint.hackyeah.web.rest.errors.EmailNotFoundException
import pl.epoint.hackyeah.web.rest.errors.InternalServerErrorException
import pl.epoint.hackyeah.web.rest.errors.InvalidPasswordException
import pl.epoint.hackyeah.web.rest.errors.LoginAlreadyUsedException
import pl.epoint.hackyeah.web.rest.vm.KeyAndPasswordVM
import pl.epoint.hackyeah.web.rest.vm.ManagedUserVM
import org.apache.commons.lang3.StringUtils
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

import javax.servlet.http.HttpServletRequest
import javax.validation.Valid
import java.io.UnsupportedEncodingException
import java.net.URLDecoder


/**
 * REST controller for managing the current user's account.
 */
@RestController
@RequestMapping("/api")
class AccountResource(private val playerRepository: PlayerRepository,
                      private val userService: UserService,
                      private val mailService: MailService,
                      private val persistentTokenRepository: PersistentTokenRepository) {

    private val log = LoggerFactory.getLogger(AccountResource::class.java)

    /**
     * GET  /account : get the current user.
     *
     * @return the current user
     * @throws RuntimeException 500 (Internal Server Error) if the user couldn't be returned
     */
    val account: UserDTO
        @GetMapping("/account")
        @Timed
        get() = userService.playerWithAuthorities
                .map<UserDTO> { UserDTO(it) }
                .orElseThrow { InternalServerErrorException("User could not be found") }

    /**
     * GET  /account/sessions : get the current open sessions.
     *
     * @return the current open sessions
     * @throws RuntimeException 500 (Internal Server Error) if the current open sessions couldn't be retrieved
     */
    val currentSessions: List<PersistentToken>
        @GetMapping("/account/sessions")
        @Timed
        get() = persistentTokenRepository.findByPlayer(
                playerRepository.findOneByLogin(SecurityUtils.currentUserLogin
                        .orElseThrow({ InternalServerErrorException("Current user login not found") }))
                        .orElseThrow { InternalServerErrorException("User could not be found") }
        )

    /**
     * POST  /register : register the user.
     *
     * @param managedUserVM the managed user View Model
     * @throws InvalidPasswordException 400 (Bad Request) if the password is incorrect
     * @throws EmailAlreadyUsedException 400 (Bad Request) if the email is already used
     * @throws LoginAlreadyUsedException 400 (Bad Request) if the login is already used
     */
    @PostMapping("/register")
    @Timed
    @ResponseStatus(HttpStatus.CREATED)
    fun registerAccount(@Valid @RequestBody managedUserVM: ManagedUserVM) {
        if (!checkPasswordLength(managedUserVM.password)) {
            throw InvalidPasswordException()
        }
        val user = userService.registerUser(managedUserVM, managedUserVM.password!!)
        mailService.sendActivationEmail(user)
    }

    /**
     * GET  /activate : activate the registered user.
     *
     * @param key the activation key
     * @throws RuntimeException 500 (Internal Server Error) if the user couldn't be activated
     */
    @GetMapping("/activate")
    @Timed
    fun activateAccount(@RequestParam(value = "key") key: String) {
        val user = userService.activateRegistration(key)
        if (!user.isPresent) {
            throw InternalServerErrorException("No user was found for this activation key")
        }
    }

    /**
     * GET  /authenticate : check if the user is authenticated, and return its login.
     *
     * @param request the HTTP request
     * @return the login if the user is authenticated
     */
    @GetMapping("/authenticate")
    @Timed
    fun isAuthenticated(request: HttpServletRequest): String {
        log.debug("REST request to check if the current user is authenticated")
        return request.remoteUser
    }

    /**
     * POST  /account : update the current user information.
     *
     * @param userDTO the current user information
     * @throws EmailAlreadyUsedException 400 (Bad Request) if the email is already used
     * @throws RuntimeException 500 (Internal Server Error) if the user login wasn't found
     */
    @PostMapping("/account")
    @Timed
    fun saveAccount(@Valid @RequestBody userDTO: UserDTO) {
        val userLogin = SecurityUtils.currentUserLogin.orElseThrow({ InternalServerErrorException("Current user login not found") })
        val existingUser = playerRepository.findOneByEmailIgnoreCase(userDTO.email!!)
        if (existingUser.isPresent && !existingUser.get().login!!.equals(userLogin, ignoreCase = true)) {
            throw EmailAlreadyUsedException()
        }
        val user = playerRepository.findOneByLogin(userLogin)
        if (!user.isPresent) {
            throw InternalServerErrorException("User could not be found")
        }
        userService.updateUser(userDTO.firstName!!, userDTO.lastName!!, userDTO.email!!,
                userDTO.langKey!!, userDTO.imageUrl!!)
    }

    /**
     * POST  /account/change-password : changes the current user's password
     *
     * @param passwordChangeDto current and new password
     * @throws InvalidPasswordException 400 (Bad Request) if the new password is incorrect
     */
    @PostMapping(path = arrayOf("/account/change-password"))
    @Timed
    fun changePassword(@RequestBody passwordChangeDto: PasswordChangeDTO) {
        if (!checkPasswordLength(passwordChangeDto.newPassword)) {
            throw InvalidPasswordException()
        }
        userService.changePassword(passwordChangeDto.currentPassword!!, passwordChangeDto.newPassword!!)
    }

    /**
     * DELETE  /account/sessions?series={series} : invalidate an existing session.
     *
     * - You can only delete your own sessions, not any other user's session
     * - If you delete one of your existing sessions, and that you are currently logged in on that session, you will
     * still be able to use that session, until you quit your browser: it does not work in real time (there is
     * no API for that), it only removes the "remember me" cookie
     * - This is also true if you invalidate your current session: you will still be able to use it until you close
     * your browser or that the session times out. But automatic login (the "remember me" cookie) will not work
     * anymore.
     * There is an API to invalidate the current session, but there is no API to check which session uses which
     * cookie.
     *
     * @param series the series of an existing session
     * @throws UnsupportedEncodingException if the series couldnt be URL decoded
     */
    @DeleteMapping("/account/sessions/{series}")
    @Timed
    @Throws(UnsupportedEncodingException::class)
    fun invalidateSession(@PathVariable series: String) {
        val decodedSeries = URLDecoder.decode(series, "UTF-8")
        SecurityUtils.currentUserLogin
                .flatMap{ playerRepository.findOneByLogin(it) }
                .ifPresent { u ->
                    persistentTokenRepository.findByPlayer(u).stream()
                        .filter { persistentToken -> StringUtils.equals(persistentToken.series, decodedSeries) }
                        .findAny().ifPresent { t -> persistentTokenRepository.deleteById(decodedSeries) }
                }
    }

    /**
     * POST   /account/reset-password/init : Send an email to reset the password of the user
     *
     * @param mail the mail of the user
     * @throws EmailNotFoundException 400 (Bad Request) if the email address is not registered
     */
    @PostMapping(path = arrayOf("/account/reset-password/init"))
    @Timed
    fun requestPasswordReset(@RequestBody mail: String) {
        mailService.sendPasswordResetMail(
                userService.requestPasswordReset(mail)
                        .orElseThrow { EmailNotFoundException() }
        )
    }

    /**
     * POST   /account/reset-password/finish : Finish to reset the password of the user
     *
     * @param keyAndPassword the generated key and the new password
     * @throws InvalidPasswordException 400 (Bad Request) if the password is incorrect
     * @throws RuntimeException 500 (Internal Server Error) if the password could not be reset
     */
    @PostMapping(path = arrayOf("/account/reset-password/finish"))
    @Timed
    fun finishPasswordReset(@RequestBody keyAndPassword: KeyAndPasswordVM) {
        if (!checkPasswordLength(keyAndPassword.newPassword)) {
            throw InvalidPasswordException()
        }
        val user = userService.completePasswordReset(keyAndPassword.newPassword!!, keyAndPassword.key!!)

        if (!user.isPresent) {
            throw InternalServerErrorException("No user was found for this reset key")
        }
    }

    private fun checkPasswordLength(password: String?): Boolean {
        return !StringUtils.isEmpty(password) &&
                password!!.length >= ManagedUserVM.PASSWORD_MIN_LENGTH &&
                password.length <= ManagedUserVM.PASSWORD_MAX_LENGTH
    }
}
