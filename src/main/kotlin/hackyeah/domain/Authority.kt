package hackyeah.domain

import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Id
import javax.persistence.Table
import javax.validation.constraints.NotNull
import javax.validation.constraints.Size
import java.io.Serializable

/**
 * An authority (a security role) used by Spring Security.
 */
@Entity
@Table(name = "jhi_authority")
class Authority : Serializable {

    @NotNull
    @Size(max = 50)
    @Id
    @Column(length = 50)
    var name: String? = null

    override fun equals(o: Any?): Boolean {
        if (this === o) {
            return true
        }
        if (o == null || javaClass != o.javaClass) {
            return false
        }

        val authority = o as Authority?

        return !if (name != null) name != authority!!.name else authority!!.name != null
    }

    override fun hashCode(): Int {
        return if (name != null) name!!.hashCode() else 0
    }

    override fun toString(): String {
        return "Authority{" +
                "name='" + name + '\''.toString() +
                "}"
    }

    companion object {

        private const val serialVersionUID = 1L
    }
}
