package pl.epoint.hackyeah.service

import pl.epoint.hackyeah.config.Constants
import pl.epoint.hackyeah.domain.Authority
import pl.epoint.hackyeah.domain.Player
import pl.epoint.hackyeah.repository.AuthorityRepository
import pl.epoint.hackyeah.repository.PersistentTokenRepository
import pl.epoint.hackyeah.repository.PlayerRepository
import pl.epoint.hackyeah.repository.search.UserSearchRepository
import pl.epoint.hackyeah.security.AuthoritiesConstants
import pl.epoint.hackyeah.security.SecurityUtils
import pl.epoint.hackyeah.service.dto.UserDTO
import pl.epoint.hackyeah.service.util.RandomUtil
import pl.epoint.hackyeah.web.rest.errors.EmailAlreadyUsedException
import pl.epoint.hackyeah.web.rest.errors.InvalidPasswordException
import pl.epoint.hackyeah.web.rest.errors.LoginAlreadyUsedException
import org.slf4j.LoggerFactory
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

import java.time.Instant
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import java.util.HashSet
import java.util.Optional
import java.util.stream.Collectors

/**
 * Service class for managing users.
 */
@Service
@Transactional
class UserService(val playerRepository: PlayerRepository,
                  val passwordEncoder: PasswordEncoder,
                  val userSearchRepository: UserSearchRepository,
                  val persistentTokenRepository: PersistentTokenRepository,
                  val authorityRepository: AuthorityRepository) {

    private val log = LoggerFactory.getLogger(UserService::class.java)

    val playerWithAuthorities: Optional<Player>
        @Transactional(readOnly = true)
        get() = SecurityUtils.currentUserLogin.flatMap { playerRepository.findOneWithAuthoritiesByLogin(it) }

    /**
     * @return a list of all the authorities
     */
    val authorities: List<String>
        get() = authorityRepository.findAll()
            .map { it.name!! }

    fun activateRegistration(key: String): Optional<Player> {
        log.debug("Activating user for activation key {}", key)
        return playerRepository.findOneByActivationKey(key)
            .map { user ->
                // activate given user for the registration key.
                user.activated = true
                user.activationKey = null
                userSearchRepository.save(user)
                log.debug("Activated user: {}", user)
                user
            }
    }

    fun completePasswordReset(newPassword: String, key: String): Optional<Player> {
        log.debug("Reset user password for reset key {}", key)
        return playerRepository.findOneByResetKey(key)
            .filter { user -> user.resetDate!!.isAfter(Instant.now().minusSeconds(86400)) }
            .map { user ->
                user.password = passwordEncoder.encode(newPassword)
                user.resetKey = null
                user.resetDate = null
                user
            }
    }

    fun requestPasswordReset(mail: String): Optional<Player> {
        return playerRepository.findOneByEmailIgnoreCase(mail)
            .filter { it.activated }
            .map { user ->
                user.resetKey = RandomUtil.generateResetKey()
                user.resetDate = Instant.now()
                user
            }
    }

    fun registerUser(userDTO: UserDTO, password: String): Player {
        playerRepository.findOneByLogin(userDTO.login!!.toLowerCase()).ifPresent { existingUser ->
            val removed = removeNonActivatedUser(existingUser)
            if (!removed) {
                throw LoginAlreadyUsedException()
            }
        }
        playerRepository.findOneByEmailIgnoreCase(userDTO.email!!).ifPresent { existingUser ->
            val removed = removeNonActivatedUser(existingUser)
            if (!removed) {
                throw EmailAlreadyUsedException()
            }
        }
        val newUser = Player()
        val encryptedPassword = passwordEncoder.encode(password)
        newUser.login = userDTO.login!!.toLowerCase()
        // new user gets initially a generated password
        newUser.password = encryptedPassword
        newUser.firstName = userDTO.firstName
        newUser.lastName = userDTO.lastName
        newUser.email = userDTO.email!!.toLowerCase()
        newUser.imageUrl = userDTO.imageUrl
        newUser.langKey = userDTO.langKey
        // new user is not active
        newUser.activated = false
        // new user gets registration key
        newUser.activationKey = RandomUtil.generateActivationKey()
        val authorities = HashSet<Authority>()
        authorityRepository.findById(AuthoritiesConstants.USER).ifPresent { authorities.add(it) }
        newUser.authorities = authorities
        playerRepository.save(newUser)
        userSearchRepository.save(newUser)
        log.debug("Created Information for User: {}", newUser)
        return newUser
    }

    private fun removeNonActivatedUser(existingPlayer: Player): Boolean {
        if (existingPlayer.activated) {
            return false
        }
        playerRepository.delete(existingPlayer)
        playerRepository.flush()
        return true
    }

    fun createUser(userDTO: UserDTO): Player {
        val user = Player()
        user.login = userDTO.login!!.toLowerCase()
        user.firstName = userDTO.firstName
        user.lastName = userDTO.lastName
        user.email = userDTO.email!!.toLowerCase()
        user.imageUrl = userDTO.imageUrl
        if (userDTO.langKey == null) {
            user.langKey = Constants.DEFAULT_LANGUAGE // default language
        } else {
            user.langKey = userDTO.langKey
        }
        val encryptedPassword = passwordEncoder.encode(RandomUtil.generatePassword())
        user.password = encryptedPassword
        user.resetKey = RandomUtil.generateResetKey()
        user.resetDate = Instant.now()
        user.activated = true
        if (userDTO.authorities != null) {
            val authorities = userDTO.authorities!!.stream()
                .map { authorityRepository.findById(it) }
                .filter { it.isPresent }
                .map { it.get() }
                .collect(Collectors.toSet())
            user.authorities = authorities
        }
        playerRepository.save(user)
        userSearchRepository.save(user)
        log.debug("Created Information for User: {}", user)
        return user
    }

    /**
     * Update basic information (first name, last name, email, language) for the current user.
     *
     * @param firstName first name of user
     * @param lastName last name of user
     * @param email email id of user
     * @param langKey language key
     * @param imageUrl image URL of user
     */
    fun updateUser(firstName: String, lastName: String, email: String, langKey: String, imageUrl: String) {
        SecurityUtils.currentUserLogin
            .flatMap { playerRepository.findOneByLogin(it) }
            .ifPresent { user ->
                user.firstName = firstName
                user.lastName = lastName
                user.email = email.toLowerCase()
                user.langKey = langKey
                user.imageUrl = imageUrl
                userSearchRepository.save<Player>(user)
                log.debug("Changed Information for User: {}", user)
            }
    }

    /**
     * Update all information for a specific user, and return the modified user.
     *
     * @param userDTO user to update
     * @return updated user
     */
    fun updateUser(userDTO: UserDTO): Optional<UserDTO> {
        return Optional.of(playerRepository
            .findById(userDTO.id!!))
            .filter { it.isPresent }
            .map { it.get() }
            .map { user ->
                user.login = userDTO.login!!.toLowerCase()
                user.firstName = userDTO.firstName
                user.lastName = userDTO.lastName
                user.email = userDTO.email!!.toLowerCase()
                user.imageUrl = userDTO.imageUrl
                user.activated = userDTO.isActivated
                user.langKey = userDTO.langKey
                val managedAuthorities = user.authorities
                (managedAuthorities as MutableSet).clear()
                userDTO.authorities!!.stream()
                    .map { authorityRepository.findById(it) }
                    .filter { it.isPresent }
                    .map { it.get() }
                    .forEach { managedAuthorities.add(it) }
                userSearchRepository.save(user)
                log.debug("Changed Information for User: {}", user)
                user
            }
            .map { UserDTO(it) }
    }

    fun deleteUser(login: String) {
        playerRepository.findOneByLogin(login).ifPresent { user ->
            playerRepository.delete(user)
            userSearchRepository.delete(user)
            log.debug("Deleted User: {}", user)
        }
    }

    fun changePassword(currentClearTextPassword: String, newPassword: String) {
        SecurityUtils.currentUserLogin
            .flatMap { playerRepository.findOneByLogin(it) }
            .ifPresent { user ->
                val currentEncryptedPassword = user.password
                if (!passwordEncoder.matches(currentClearTextPassword, currentEncryptedPassword)) {
                    throw InvalidPasswordException()
                }
                val encryptedPassword = passwordEncoder.encode(newPassword)
                user.password = encryptedPassword
                log.debug("Changed password for User: {}", user)
            }
    }

    @Transactional(readOnly = true)
    fun getAllManagedUsers(pageable: Pageable): Page<UserDTO> {
        return playerRepository.findAllByLoginNot(pageable, Constants.ANONYMOUS_USER).map { UserDTO(it) }
    }

    @Transactional(readOnly = true)
    fun getUserWithAuthoritiesByLogin(login: String): Optional<Player> {
        return playerRepository.findOneWithAuthoritiesByLogin(login)
    }

    @Transactional(readOnly = true)
    fun getUserWithAuthorities(id: Long?): Optional<Player> {
        return playerRepository.findOneWithAuthoritiesById(id)
    }

    /**
     * Persistent Token are used for providing automatic authentication, they should be automatically deleted after
     * 30 days.
     *
     *
     * This is scheduled to get fired everyday, at midnight.
     */
    @Scheduled(cron = "0 0 0 * * ?")
    fun removeOldPersistentTokens() {
        val now = LocalDate.now()
        persistentTokenRepository.findByTokenDateBefore(now.minusMonths(1)).forEach { token ->
            log.debug("Deleting token {}", token.series)
            val user = token.player
            val persistentTokens = user!!.persistentTokens as MutableSet
            persistentTokens.remove(token)
            persistentTokenRepository.delete(token)
        }
    }

    /**
     * Not activated users should be automatically deleted after 3 days.
     *
     *
     * This is scheduled to get fired everyday, at 01:00 (am).
     */
    @Scheduled(cron = "0 0 1 * * ?")
    fun removeNotActivatedUsers() {
        playerRepository
            .findAllByActivatedIsFalseAndCreatedDateBefore(Instant.now().minus(3, ChronoUnit.DAYS))
            .forEach { user ->
                log.debug("Deleting not activated user {}", user.login)
                playerRepository.delete(user)
                userSearchRepository.delete(user)
            }
    }
}
