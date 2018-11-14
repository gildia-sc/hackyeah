package pl.epoint.hackyeah.service

import pl.epoint.hackyeah.domain.Player
import io.github.jhipster.config.JHipsterProperties
import org.slf4j.LoggerFactory
import org.springframework.context.MessageSource
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.mail.javamail.MimeMessageHelper
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Service
import org.thymeleaf.context.Context
import org.thymeleaf.spring5.SpringTemplateEngine

import java.nio.charset.StandardCharsets
import java.util.Locale

/**
 * Service for sending emails.
 *
 *
 * We use the @Async annotation to send emails asynchronously.
 */
@Service
class MailService(private val jHipsterProperties: JHipsterProperties, private val javaMailSender: JavaMailSender,
                  private val messageSource: MessageSource, private val templateEngine: SpringTemplateEngine) {

    private val log = LoggerFactory.getLogger(MailService::class.java)

    @Async
    fun sendEmail(to: String?, subject: String, content: String, isMultipart: Boolean, isHtml: Boolean) {
        log.debug("Send email[multipart '{}' and html '{}'] to '{}' with subject '{}' and content={}",
                isMultipart, isHtml, to, subject, content)

        // Prepare message using a Spring helper
        val mimeMessage = javaMailSender.createMimeMessage()
        try {
            val message = MimeMessageHelper(mimeMessage, isMultipart, StandardCharsets.UTF_8.name())
            message.setTo(to!!)
            message.setFrom(jHipsterProperties.mail.from)
            message.setSubject(subject)
            message.setText(content, isHtml)
            javaMailSender.send(mimeMessage)
            log.debug("Sent email to User '{}'", to)
        } catch (e: Exception) {
            if (log.isDebugEnabled) {
                log.warn("Email could not be sent to user '{}'", to, e)
            } else {
                log.warn("Email could not be sent to user '{}': {}", to, e.message)
            }
        }

    }

    @Async
    fun sendEmailFromTemplate(player: Player, templateName: String, titleKey: String) {
        val locale = Locale.forLanguageTag(player.langKey!!)
        val context = Context(locale)
        context.setVariable(USER, player)
        context.setVariable(BASE_URL, jHipsterProperties.mail.baseUrl)
        val content = templateEngine.process(templateName, context)
        val subject = messageSource.getMessage(titleKey, null, locale)
        sendEmail(player.email, subject, content, false, true)

    }

    @Async
    fun sendActivationEmail(player: Player) {
        log.debug("Sending activation email to '{}'", player.email)
        sendEmailFromTemplate(player, "mail/activationEmail", "email.activation.title")
    }

    @Async
    fun sendCreationEmail(player: Player) {
        log.debug("Sending creation email to '{}'", player.email)
        sendEmailFromTemplate(player, "mail/creationEmail", "email.activation.title")
    }

    @Async
    fun sendPasswordResetMail(player: Player) {
        log.debug("Sending password reset email to '{}'", player.email)
        sendEmailFromTemplate(player, "mail/passwordResetEmail", "email.reset.title")
    }

    companion object {

        private val USER = "user"

        private val BASE_URL = "baseUrl"
    }
}