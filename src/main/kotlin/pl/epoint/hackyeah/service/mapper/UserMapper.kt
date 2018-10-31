package pl.epoint.hackyeah.service.mapper

import pl.epoint.hackyeah.domain.Authority
import pl.epoint.hackyeah.domain.Player
import pl.epoint.hackyeah.service.dto.UserDTO
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

    fun userToUserDTO(player: Player): UserDTO {
        return UserDTO(player)
    }

    fun usersToUserDTOs(players: List<Player>): List<UserDTO> {
        return players.stream()
                .filter { Objects.nonNull(it) }
                .map { this.userToUserDTO(it) }
                .collect(Collectors.toList())
    }

    fun userDTOToUser(userDTO: UserDTO?): Player? {
        if (userDTO == null) {
            return null
        } else {
            val user = Player()
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

    fun userDTOsToUsers(userDTOs: List<UserDTO>): List<Player> {
        return userDTOs.stream()
                .filter { Objects.nonNull(it) }
                .map<Player> { this.userDTOToUser(it) }
                .collect(Collectors.toList())
    }

    fun userFromId(id: Long?): Player? {
        if (id == null) {
            return null
        }
        val user = Player()
        user.id = id
        return user
    }

    fun authoritiesFromStrings(strings: Set<String>?): Set<Authority>? {
        return strings!!.stream().map { string ->
            val auth = Authority()
            auth.name = string
            auth
        }.collect(Collectors.toSet())
    }
}
