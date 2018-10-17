package hackyeah.config.audit

import hackyeah.domain.PersistentAuditEvent
import org.springframework.boot.actuate.audit.AuditEvent
import org.springframework.security.web.authentication.WebAuthenticationDetails
import org.springframework.stereotype.Component

import java.util.ArrayList
import java.util.Collections
import java.util.HashMap
import java.util.Objects

@Component
class AuditEventConverter {

    /**
     * Convert a list of PersistentAuditEvent to a list of AuditEvent
     *
     * @param persistentAuditEvents the list to convert
     * @return the converted list.
     */
    fun convertToAuditEvent(persistentAuditEvents: Iterable<PersistentAuditEvent>?): List<AuditEvent> {
        if (persistentAuditEvents == null) {
            return emptyList()
        }
        val auditEvents = ArrayList<AuditEvent>()
        for (persistentAuditEvent in persistentAuditEvents) {
            auditEvents.add(convertToAuditEvent(persistentAuditEvent))
        }
        return auditEvents
    }

    /**
     * Convert a PersistentAuditEvent to an AuditEvent
     *
     * @param persistentAuditEvent the event to convert
     * @return the converted list.
     */
    fun convertToAuditEvent(persistentAuditEvent: PersistentAuditEvent?): AuditEvent? {
        return if (persistentAuditEvent == null) {
            null
        } else AuditEvent(persistentAuditEvent.auditEventDate!!, persistentAuditEvent.principal,
                persistentAuditEvent.auditEventType!!, convertDataToObjects(persistentAuditEvent.data))
    }

    /**
     * Internal conversion. This is needed to support the current SpringBoot actuator AuditEventRepository interface
     *
     * @param data the data to convert
     * @return a map of String, Object
     */
    fun convertDataToObjects(data: Map<String, String>?): Map<String, Any> {
        val results = HashMap<String, Any>()

        if (data != null) {
            for ((key, value) in data) {
                results[key] = value
            }
        }
        return results
    }

    /**
     * Internal conversion. This method will allow to save additional data.
     * By default, it will save the object as string
     *
     * @param data the data to convert
     * @return a map of String, String
     */
    fun convertDataToStrings(data: Map<String, Any>?): Map<String, String> {
        val results = HashMap<String, String>()

        if (data != null) {
            for ((key, value) in data) {
                // Extract the data that will be saved.
                if (value is WebAuthenticationDetails) {
                    results["remoteAddress"] = value.remoteAddress
                    results["sessionId"] = value.sessionId
                } else {
                    results[key] = Objects.toString(value)
                }
            }
        }
        return results
    }
}
