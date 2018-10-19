package pl.epoint.hackyeah.web.rest

import com.codahale.metrics.annotation.Timed
import pl.epoint.hackyeah.domain.User
import pl.epoint.hackyeah.repository.UserRepository
import pl.epoint.hackyeah.repository.search.UserSearchRepository
import pl.epoint.hackyeah.service.MailService
import pl.epoint.hackyeah.service.UserService
import pl.epoint.hackyeah.service.dto.UserDTO
import pl.epoint.hackyeah.web.rest.errors.BadRequestAlertException
import pl.epoint.hackyeah.web.rest.errors.EmailAlreadyUsedException
import pl.epoint.hackyeah.web.rest.errors.LoginAlreadyUsedException
import pl.epoint.hackyeah.web.rest.util.HeaderUtil
import pl.epoint.hackyeah.web.rest.util.PaginationUtil
import io.github.jhipster.web.util.ResponseUtil
import org.slf4j.LoggerFactory
import org.springframework.data.domain.Pageable
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

import javax.validation.Valid
import java.net.URI
import java.net.URISyntaxException
import java.util.stream.Collectors
import java.util.stream.StreamSupport

import org.elasticsearch.index.query.QueryBuilders.queryStringQuery

/**
 * REST controller for managing users.
 *
 *
 * This class accesses the User entity, and needs to fetch its collection of authorities.
 *
 *
 * For a normal use-case, it would be better to have an eager relationship between User and Authority,
 * and send everything to the client side: there would be no View Model and DTO, a lot less code, and an outer-join
 * which would be good for performance.
 *
 *
 * We use a View Model and a DTO for 3 reasons:
 *
 *  * We want to keep a lazy association between the user and the authorities, because people will
 * quite often do relationships with the user, and we don't want them to get the authorities all
 * the time for nothing (for performance reasons). This is the #1 goal: we should not impact our users'
 * application because of this use-case.
 *  *  Not having an outer join causes n+1 requests to the database. This is not a real issue as
 * we have by default a second-level cache. This means on the first HTTP call we do the n+1 requests,
 * but then all authorities come from the cache, so in fact it's much better than doing an outer join
 * (which will get lots of data from the database, for each HTTP call).
 *  *  As this manages users, for security reasons, we'd rather have a DTO layer.
 *
 *
 *
 * Another option would be to have a specific JPA entity graph to handle this case.
 */
@RestController
@RequestMapping("/api")
class UserResource(private val userService: UserService, private val userRepository: UserRepository, private val mailService: MailService, private val userSearchRepository: UserSearchRepository) {

    private val log = LoggerFactory.getLogger(UserResource::class.java)

    /**
     * @return a string list of the all of the roles
     */
    val authorities: List<String>
        @GetMapping("/users/authorities")
        @Timed
        @PreAuthorize("hasRole(\"" + "ROLE_ADMIN" + "\")")
        get() = userService.authorities

    /**
     * POST  /users  : Creates a new user.
     *
     *
     * Creates a new user if the login and email are not already used, and sends an
     * mail with an activation link.
     * The user needs to be activated on creation.
     *
     * @param userDTO the user to create
     * @return the ResponseEntity with status 201 (Created) and with body the new user, or with status 400 (Bad Request) if the login or email is already in use
     * @throws URISyntaxException if the Location URI syntax is incorrect
     * @throws BadRequestAlertException 400 (Bad Request) if the login or email is already in use
     */
    @PostMapping("/users")
    @Timed
    @PreAuthorize("hasRole(\"" + "ROLE_ADMIN" + "\")")
    @Throws(URISyntaxException::class)
    fun createUser(@Valid @RequestBody userDTO: UserDTO): ResponseEntity<User> {
        log.debug("REST request to save User : {}", userDTO)

        if (userDTO.id != null) {
            throw BadRequestAlertException("A new user cannot already have an ID", "userManagement", "idexists")
            // Lowercase the user login before comparing with database
        } else if (userRepository.findOneByLogin(userDTO.login!!.toLowerCase()).isPresent) {
            throw LoginAlreadyUsedException()
        } else if (userRepository.findOneByEmailIgnoreCase(userDTO.email!!).isPresent) {
            throw EmailAlreadyUsedException()
        } else {
            val newUser = userService.createUser(userDTO)
            mailService.sendCreationEmail(newUser)
            return ResponseEntity.created(URI("/api/users/" + newUser.login!!))
                    .headers(HeaderUtil.createAlert("userManagement.created", newUser.login!!))
                    .body(newUser)
        }
    }

    /**
     * PUT /users : Updates an existing User.
     *
     * @param userDTO the user to update
     * @return the ResponseEntity with status 200 (OK) and with body the updated user
     * @throws EmailAlreadyUsedException 400 (Bad Request) if the email is already in use
     * @throws LoginAlreadyUsedException 400 (Bad Request) if the login is already in use
     */
    @PutMapping("/users")
    @Timed
    @PreAuthorize("hasRole(\"" + "ROLE_ADMIN" + "\")")
    fun updateUser(@Valid @RequestBody userDTO: UserDTO): ResponseEntity<UserDTO> {
        log.debug("REST request to update User : {}", userDTO)
        var existingUser = userRepository.findOneByEmailIgnoreCase(userDTO.email!!)
        if (existingUser.isPresent && existingUser.get().id != userDTO.id) {
            throw EmailAlreadyUsedException()
        }
        existingUser = userRepository.findOneByLogin(userDTO.login!!.toLowerCase())
        if (existingUser.isPresent && existingUser.get().id != userDTO.id) {
            throw LoginAlreadyUsedException()
        }
        val updatedUser = userService.updateUser(userDTO)

        return ResponseUtil.wrapOrNotFound(updatedUser,
                HeaderUtil.createAlert("userManagement.updated", userDTO.login!!))
    }

    /**
     * GET /users : get all users.
     *
     * @param pageable the pagination information
     * @return the ResponseEntity with status 200 (OK) and with body all users
     */
    @GetMapping("/users")
    @Timed
    fun getAllUsers(pageable: Pageable): ResponseEntity<List<UserDTO>> {
        val page = userService.getAllManagedUsers(pageable)
        val headers = PaginationUtil.generatePaginationHttpHeaders(page, "/api/users")
        return ResponseEntity(page.content, headers, HttpStatus.OK)
    }

    /**
     * GET /users/:login : get the "login" user.
     *
     * @param login the login of the user to find
     * @return the ResponseEntity with status 200 (OK) and with body the "login" user, or with status 404 (Not Found)
     */
    @GetMapping("/users/{login:" + "^[_.@A-Za-z0-9-]*$" + "}")
    @Timed
    fun getUser(@PathVariable login: String): ResponseEntity<UserDTO> {
        log.debug("REST request to get User : {}", login)
        return ResponseUtil.wrapOrNotFound(
                userService.getUserWithAuthoritiesByLogin(login)
                        .map { UserDTO(it) })
    }

    /**
     * DELETE /users/:login : delete the "login" User.
     *
     * @param login the login of the user to delete
     * @return the ResponseEntity with status 200 (OK)
     */
    @DeleteMapping("/users/{login:" + "^[_.@A-Za-z0-9-]*$" + "}")
    @Timed
    @PreAuthorize("hasRole(\"" + "ROLE_ADMIN" + "\")")
    fun deleteUser(@PathVariable login: String): ResponseEntity<Void> {
        log.debug("REST request to delete User: {}", login)
        userService.deleteUser(login)
        return ResponseEntity.ok().headers(HeaderUtil.createAlert("userManagement.deleted", login)).build()
    }

    /**
     * SEARCH /_search/users/:query : search for the User corresponding
     * to the query.
     *
     * @param query the query to search
     * @return the result of the search
     */
    @GetMapping("/_search/users/{query}")
    @Timed
    fun search(@PathVariable query: String): List<User> {
        return StreamSupport
                .stream(userSearchRepository.search(queryStringQuery(query)).spliterator(), false)
                .collect(Collectors.toList())
    }
}
