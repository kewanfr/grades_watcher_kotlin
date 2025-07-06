package fr.kewan

import com.google.gson.Gson
import java.io.OutputStream
import java.net.HttpURLConnection
import java.net.URL

class Discord {
    fun sendWebhook(url: String, embeds: String) {
        try {
            val connection = URL(url).openConnection() as HttpURLConnection
            connection.requestMethod = "POST"
            connection.doOutput = true
            connection.setRequestProperty("Content-Type", "application/json")

            val payload = """
                {
                    "username": "Notes IUT",
                    "avatar_url": "https://media.discordapp.net/attachments/1356808321855787149/1356808401195241743/WRLUWNE8v0HpPBC3lTeZRcnRtyK_AO7YJ9BHNKESrr2VghLmjxPW8MnrRqUfmP780PyG0UMM5CMs900-c-k-c0x00ffffff-no-rj.png?ex=67ede9cb&is=67ec984b&hm=01f3ae23d5ef042065e086759c6e7a97e4f1de4f47874cee9a46cec90f47d637&=&format=webp&quality=lossless",
                    
                    "embeds": [$embeds]
                }
            """.trimIndent()

            //val jsonPayload = Gson().toJson(payload)

            println("Envoi du webhook à l'URL : $url")
            //println("Payload : $payload")

            connection.outputStream.use { outputStream: OutputStream ->
                outputStream.write(payload.toByteArray(Charsets.UTF_8))
            }

            val responseCode = connection.responseCode
            println("reponse: ${connection.responseMessage}")
            if (responseCode in 200..299) {
                println("Webhook envoyé avec succès. Code de réponse : $responseCode")
            } else {
                println("Échec de l'envoi du webhook. Code de réponse : $responseCode")
            }
        } catch (e: Exception) {
            println("Erreur lors de l'envoi du webhook : ${e.message}")
        }
    }
}