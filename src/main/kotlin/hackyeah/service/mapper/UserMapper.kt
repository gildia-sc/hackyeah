package hackyeah.service.mapper

import hackyeah.domain.Authority
import hackyeah.domain.User
import hackyeah.service.dto.UserDTO
import org.springframework.stereotype.Service
import java.util.Objects
import java.util.stream.Collectors

/**
 * Mapper for the entity User and its DTO called UserDTO.
 *
 * Normal mappers are generated using MapStruct, this one is hand-coded as MapStruct
 * support is still in beta, and requires a manual step with an IDE.
 */
@Service
class UserMapper {

    fun userToUserDTO(user: User): UserDTO {
        return UserDTO(user)
    }

    fun usersToUserDTOs(users: List<User>): List<UserDTO> {
        return users.stream()
                .filter(Predicate<User> { Objects.nonNull(it) })
                .map<UserDTO>(Function<User, UserDTO> { this.userToUserDTO(it) })
                .collect<List<UserDTO>, Any>(Collectors.toList())
    }

    fun userDTOToUser(userDTO: UserDTO?): User? {
        if (userDTO == null) {
            return null
        } else {
            val user = User()
            user.id = userDTO.id
            user.login = userDTO.login
            user.firstName = userDTO.firstName
            user.lastName = userDTO.lastName
            user.email = userDTO.email
            user.imageUrl = userDTO.imageUrl
            user.activated = userDTO.isActivated
            user.langKey = userDTO.langKey
            val authorities = this.authoritiesFromStrings(userDTO.authorities)
            if (authorities != null) {
                user.authorities = authorities
            }
            return user
        }
    }

    fun userDTOsToUsers(userDTOs: List<UserDTO>): List<User> {
        return userDTOs.stream()
                .filter(Predicate<UserDTO> { Objects.nonNull(it) })
                .map<User>(Function<UserDTO, User> { this.userDTOToUser(it) })
                .collect<List<User>, Any>(Collectors.toList())
    }

    fun userFromId(id: Long?): User? {
        if (id == null) {
            return null
        }
        val user = User()
        user.id = id
        return user
    }

    fun authoritiesFromStrings(strings: Set<String>?): Set<Authority>? {
        return strings!!.stream().map { string ->
            val auth = Authority()
            auth.name = string
            auth
        }.collect<Set<Authority>, Any>(Collectors.toSet())
    }
}
