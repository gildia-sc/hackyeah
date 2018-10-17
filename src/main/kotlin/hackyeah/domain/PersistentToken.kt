package hackyeah.domain

import com.fasterxml.jackson.annotation.JsonIgnore
import hackyeah.security.PersistentTokenRememberMeServices

import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Id
import javax.persistence.ManyToOne
import javax.persistence.Table
import javax.validation.constraints.NotNull
import javax.validation.constraints.Size
import java.io.Serializable
import java.time.LocalDate


/**
 * Persistent tokens are used by Spring Security to automatically log in users.
 *
 * @see PersistentTokenRememberMeServices
 */
@Entity
@Table(name = "jhi_persistent_token")
class PersistentToken : Serializable {

    @Id
    var series: String? = null

    @JsonIgnore
    @NotNull
    @Column(name = "token_value", nullable = false)
    var tokenValue: String? = null

    @Column(name = "token_date")
    var tokenDate: LocalDate? = null

    //an IPV6 address max length is 39 characters
    @Size(min = 0, max = 39)
    @Column(name = "ip_address", length = 39)
    var ipAddress: String? = null

    @Column(name = "user_agent")
    var userAgent: String? = null
        set(userAgent) = if (userAgent.length >= MAX_USER_AGENT_LEN) {
            field = userAgent.substring(0, MAX_USER_AGENT_LEN - 1)
        } else {
            field = userAgent
        }


    @JsonIgnore
    @ManyToOne
    var user: User? = null

    override fun equals(o: Any?): Boolean {
        if (this === o) {
            return true
        }
        if (o == null || javaClass != o.javaClass) {
            return false
        }

        val that = o as PersistentToken?

        return if (series != that!!.series) {
            false
        } else true

    }

    override fun hashCode(): Int {
        return series!!.hashCode()
    }

    override fun toString(): String {
        return "PersistentToken{" +
                "series='" + series + '\''.toString() +
                ", tokenValue='" + tokenValue + '\''.toString() +
                ", tokenDate=" + tokenDate +
                ", ipAddress='" + ipAddress + '\''.toString() +
                ", userAgent='" + this.userAgent + '\''.toString() +
                "}"
    }

    companion object {

        private const val serialVersionUID = 1L

        private val MAX_USER_AGENT_LEN = 255
    }
}
