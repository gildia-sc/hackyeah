package hackyeah.domain

import com.fasterxml.jackson.annotation.JsonIgnore
import hackyeah.config.Constants
import org.apache.commons.lang3.StringUtils
import org.hibernate.annotations.BatchSize

import javax.persistence.CascadeType
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.JoinColumn
import javax.persistence.JoinTable
import javax.persistence.ManyToMany
import javax.persistence.OneToMany
import javax.persistence.SequenceGenerator
import javax.persistence.Table
import javax.validation.constraints.Email
import javax.validation.constraints.NotNull
import javax.validation.constraints.Pattern
import javax.validation.constraints.Size
import java.io.Serializable
import java.time.Instant
import java.util.HashSet
import java.util.Locale
import java.util.Objects

/**
 * A user.
 */
@Entity
@Table(name = "jhi_user")
@org.springframework.data.elasticsearch.annotations.Document(indexName = "user")
class User : AbstractAuditingEntity(), Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sequenceGenerator")
    @SequenceGenerator(name = "sequenceGenerator")
    var id: Long? = null

    // Lowercase the login before saving it in database
    @NotNull
    @Pattern(regexp = Constants.LOGIN_REGEX)
    @Size(min = 1, max = 50)
    @Column(length = 50, unique = true, nullable = false)
    var login: String? = null
        set(login) {
            field = StringUtils.lowerCase(login, Locale.ENGLISH)
        }

    @JsonIgnore
    @NotNull
    @Size(min = 60, max = 60)
    @Column(name = "password_hash", length = 60, nullable = false)
    var password: String? = null

    @Size(max = 50)
    @Column(name = "first_name", length = 50)
    var firstName: String? = null

    @Size(max = 50)
    @Column(name = "last_name", length = 50)
    var lastName: String? = null

    @Email
    @Size(min = 5, max = 254)
    @Column(length = 254, unique = true)
    var email: String? = null

    @NotNull
    @Column(nullable = false)
    var activated = false

    @Size(min = 2, max = 6)
    @Column(name = "lang_key", length = 6)
    var langKey: String? = null

    @Size(max = 256)
    @Column(name = "image_url", length = 256)
    var imageUrl: String? = null

    @Size(max = 20)
    @Column(name = "activation_key", length = 20)
    @JsonIgnore
    var activationKey: String? = null

    @Size(max = 20)
    @Column(name = "reset_key", length = 20)
    @JsonIgnore
    var resetKey: String? = null

    @Column(name = "reset_date")
    var resetDate: Instant? = null

    @JsonIgnore
    @ManyToMany
    @JoinTable(name = "jhi_user_authority", joinColumns = arrayOf(JoinColumn(name = "user_id", referencedColumnName = "id")), inverseJoinColumns = arrayOf(JoinColumn(name = "authority_name", referencedColumnName = "name")))
    @BatchSize(size = 20)
    var authorities: Set<Authority> = HashSet()

    @JsonIgnore
    @OneToMany(cascade = arrayOf(CascadeType.ALL), orphanRemoval = true, mappedBy = "user")
    var persistentTokens: Set<PersistentToken> = HashSet()

    override fun equals(o: Any?): Boolean {
        if (this === o) {
            return true
        }
        if (o == null || javaClass != o.javaClass) {
            return false
        }

        val user = o as User?
        return !(user!!.id == null || id == null) && id == user.id
    }

    override fun hashCode(): Int {
        return Objects.hashCode(id)
    }

    override fun toString(): String {
        return "User{" +
                "login='" + this.login + '\''.toString() +
                ", firstName='" + firstName + '\''.toString() +
                ", lastName='" + lastName + '\''.toString() +
                ", email='" + email + '\''.toString() +
                ", imageUrl='" + imageUrl + '\''.toString() +
                ", activated='" + activated + '\''.toString() +
                ", langKey='" + langKey + '\''.toString() +
                ", activationKey='" + activationKey + '\''.toString() +
                "}"
    }

    companion object {

        private const val serialVersionUID = 1L
    }
}
