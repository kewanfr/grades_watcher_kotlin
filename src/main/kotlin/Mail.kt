package fr.kewan

import java.util.Properties
import javax.mail.Message
import javax.mail.Session
import javax.mail.Transport
import javax.mail.internet.InternetAddress
import javax.mail.internet.MimeMessage

class Mail {
    val MAIL_HOST = "smtp.gmail.com"
    val MAIL_PORT = 465
    val MAIL_USER = "kelectroduino@gmail.com"
    val GMAIL_CODE = Config.GMAIL_CODE ?: throw IllegalStateException("Gmail code is not set. Please set it in the config file.")


    fun sendMail(to: String, subject: String, body: String) {

        val properties = Properties().apply {
            put("mail.smtp.auth", "true")
            put("mail.smtp.socketFactory.port", "465")
            put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory")
            put("mail.smtp.host", MAIL_HOST)
            put("mail.smtp.port", "465")
        }

        val session = Session.getInstance(properties, object : javax.mail.Authenticator() {
            override fun getPasswordAuthentication(): javax.mail.PasswordAuthentication {
                return javax.mail.PasswordAuthentication(MAIL_USER, GMAIL_CODE)
            }
        })

        /*val session = Session.getInstance(properties, object : javax.mail.Authenticator() {
            override fun getPasswordAuthentication(): javax.mail.PasswordAuthentication {
                return javax.mail.PasswordAuthentication(MAIL_USER, GMAIL_CODE)
            }
        })*/
        // Implémentation de l'envoi d'email
        println("Envoi de l'email à $to avec le sujet '$subject'")
        //println("et le corps '$body'.")


        try {
            val message = MimeMessage(session).apply {
                setFrom(InternetAddress(MAIL_USER))
                setRecipients(Message.RecipientType.TO, InternetAddress.parse(to))
                setSubject(subject)
                setContent(body, "text/html; charset=utf-8")

            }

            Transport.send(message)
            println("Mail envoyé avec succès à $to")
        } catch (e: Exception) {
            println("Erreur lors de l'envoi du mail : ${e.message}")
        }
        // Ici, vous pouvez utiliser une bibliothèque d'envoi d'emails comme JavaMail ou Ktor pour envoyer l'email.
    }
}