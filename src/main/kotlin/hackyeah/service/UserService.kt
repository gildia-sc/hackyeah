package hackyeah.service

import hackyeah.config.Constants
import hackyeah.domain.Authority
import hackyeah.domain.User
import hackyeah.repository.AuthorityRepository
import hackyeah.repository.PersistentTokenRepository
import hackyeah.repository.UserRepository
import hackyeah.repository.search.UserSearchRepository
import hackyeah.security.AuthoritiesConstants
import hackyeah.security.SecurityUtils
import hackyeah.service.dto.UserDTO
import hackyeah.service.util.RandomUtil
import hackyeah.web.rest.errors.EmailAlreadyUsedException
import hackyeah.web.rest.errors.InvalidPasswordException
import hackyeah.web.rest.errors.LoginAlreadyUsedException
import org.slf4j.Logger
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
class UserService(private val userRepository: UserRepository, private val passwordEncoder: PasswordEncoder, private val userSearchRepository: UserSearchRepository, private val persistentTokenRepository: PersistentTokenRepository, private val authorityRepository: AuthorityRepository) {

    private val log = LoggerFactory.getLogger(UserService::class.java)

    val userWithAuthorities: Optional<User>
        @Transactional(readOnly = true)
        get() = SecurityUtils.currentUserLogin.flatMap(Function<String, Optional<User>> { userRepository.findOneWithAuthoritiesByLogin(it) })

    /**
     * @return a list of all the authorities
     */
    val authorities: List<String>
        get() = authorityRepository.findAll().stream().map<String>(Function<Authority, String> { it.getName() }).collect<List<String>, Any>(Collectors.toList())

    fun activateRegistration(key: String): Optional<User> {
        log.debug("Activating user for activation key {}", key)
        return userRepository.findOneByActivationKey(key)
                .map { user ->
                    // activate given user for the registration key.
                    user.activated = true
                    user.activationKey = null
                    userSearchRepository.save(user)
                    log.debug("Activated user: {}", user)
                    user
                }
    }

    fun completePasswordReset(newPassword: String, key: String): Optional<User> {
        log.debug("Reset user password for reset key {}", key)
        return userRepository.findOneByResetKey(key)
                .filter { user -> user.resetDate!!.isAfter(Instant.now().minusSeconds(86400)) }
                .map { user ->
                    user.password = passwordEncoder.encode(newPassword)
                    user.resetKey = null
                    user.resetDate = null
                    user
                }
    }

    fun requestPasswordReset(mail: String): Optional<User> {
        return userRepository.findOneByEmailIgnoreCase(mail)
                .filter(Predicate<User> { it.getActivated() })
                .map { user ->
                    user.resetKey = RandomUtil.generateResetKey()
                    user.resetDate = Instant.now()
                    user
                }
    }

    fun registerUser(userDTO: UserDTO, password: String): User {
        userRepository.findOneByLogin(userDTO.login!!.toLowerCase()).ifPresent { existingUser ->
            val removed = removeNonActivatedUser(existingUser)
            if (!removed) {
                throw LoginAlreadyUsedException()
            }
        }
        userRepository.findOneByEmailIgnoreCase(userDTO.email).ifPresent { existingUser ->
            val removed = removeNonActivatedUser(existingUser)
            if (!removed) {
                throw EmailAlreadyUsedException()
            }
        }
        val newUser = User()
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
        authorityRepository.findById(AuthoritiesConstants.USER).ifPresent(Consumer<Authority> { authorities.add(it) })
        newUser.authorities = authorities
        userRepository.save(newUser)
        userSearchRepository.save(newUser)
        log.debug("Created Information for User: {}", newUser)
        return newUser
    }

    private fun removeNonActivatedUser(existingUser: User): Boolean {
        if (existingUser.activated) {
            return false
        }
        userRepository.delete(existingUser)
        userRepository.flush()
        return true
    }

    fun createUser(userDTO: UserDTO): User {
        val user = User()
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
                    .map<Optional<Authority>>(Function<String, Optional<Authority>> { authorityRepository.findById(it) })
                    .filter(Predicate<Optional<Authority>> { it.isPresent() })
                    .map<Authority>(Function<Optional<Authority>, Authority> { it.get() })
                    .collect<Set<Authority>, Any>(Collectors.toSet())
            user.authorities = authorities
        }
        userRepository.save(user)
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
                .flatMap(Function<String, Optional<User>> { userRepository.findOneByLogin(it) })
                .ifPresent({ user ->
                    user.firstName = firstName
                    user.lastName = lastName
                    user.email = email.toLowerCase()
                    user.langKey = langKey
                    user.imageUrl = imageUrl
                    userSearchRepository.save<User>(user)
                    log.debug("Changed Information for User: {}", user)
                })
    }

    /**
     * Update all information for a specific user, and return the modified user.
     *
     * @param userDTO user to update
     * @return updated user
     */
    fun updateUser(userDTO: UserDTO): Optional<UserDTO> {
        return Optional.of(userRepository
                .findById(userDTO.id!!))
                .filter(Predicate<Optional<User>> { it.isPresent() })
                .map<User>(Function<Optional<User>, User> { it.get() })
                .map { user ->
                    user.login = userDTO.login!!.toLowerCase()
                    user.firstName = userDTO.firstName
                    user.lastName = userDTO.lastName
                    user.email = userDTO.email!!.toLowerCase()
                    user.imageUrl = userDTO.imageUrl
                    user.activated = userDTO.isActivated
                    user.langKey = userDTO.langKey
                    val managedAuthorities = user.authorities
                    managedAuthorities.clear()
                    userDTO.authorities!!.stream()
                            .map<Optional<Authority>>(Function<String, Optional<Authority>> { authorityRepository.findById(it) })
                            .filter(Predicate<Optional<Authority>> { it.isPresent() })
                            .map<Authority>(Function<Optional<Authority>, Authority> { it.get() })
                            .forEach(Consumer<Authority> { managedAuthorities.add(it) })
                    userSearchRepository.save(user)
                    log.debug("Changed Information for User: {}", user)
                    user
                }
                .map(Function<User, UserDTO> { UserDTO(it) })
    }

    fun deleteUser(login: String) {
        userRepository.findOneByLogin(login).ifPresent { user ->
            userRepository.delete(user)
            userSearchRepository.delete(user)
            log.debug("Deleted User: {}", user)
        }
    }

    fun changePassword(currentClearTextPassword: String, newPassword: String) {
        SecurityUtils.currentUserLogin
                .flatMap(Function<String, Optional<User>> { userRepository.findOneByLogin(it) })
                .ifPresent({ user ->
                    val currentEncryptedPassword = user.password
                    if (!passwordEncoder.matches(currentClearTextPassword, currentEncryptedPassword)) {
                        throw InvalidPasswordException()
                    }
                    val encryptedPassword = passwordEncoder.encode(newPassword)
                    user.password = encryptedPassword
                    log.debug("Changed password for User: {}", user)
                })
    }

    @Transactional(readOnly = true)
    fun getAllManagedUsers(pageable: Pageable): Page<UserDTO> {
        return userRepository.findAllByLoginNot(pageable, Constants.ANONYMOUS_USER).map(Function<User, UserDTO> { UserDTO(it) })
    }

    @Transactional(readOnly = true)
    fun getUserWithAuthoritiesByLogin(login: String): Optional<User> {
        return userRepository.findOneWithAuthoritiesByLogin(login)
    }

    @Transactional(readOnly = true)
    fun getUserWithAuthorities(id: Long?): Optional<User> {
        return userRepository.findOneWithAuthoritiesById(id)
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
            val user = token.user
            user!!.persistentTokens.remove(token)
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
        userRepository
                .findAllByActivatedIsFalseAndCreatedDateBefore(Instant.now().minus(3, ChronoUnit.DAYS))
                .forEach { user ->
                    log.debug("Deleting not activated user {}", user.login)
                    userRepository.delete(user)
                    userSearchRepository.delete(user)
                }
    }
}
