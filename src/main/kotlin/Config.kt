package fr.kewan

import com.google.gson.GsonBuilder
import java.io.File

data class ConfigData(
    val GMAIL_CODE: String? = null, // Code de vérification Gmail pour l'authentification
    val LOGIN_URL: String = "https://notes.iut-nantes.univ-nantes.fr/services/doAuth.php?href=https%3A%2F%2Fnotes.iut-nantes.univ-nantes.fr%2F",
    val USERNAME: String? = null,
    val PASSWORD: String? = null,
    val DATA_URL: String = "https://notes.iut-nantes.univ-nantes.fr/services/data.php?q=relev%C3%A9Etudiant&semestre=",
    val SEMESTER_KEYS: List<String>? = null,
    val WEBHOOK_URL_1: String? = null, // URL du webhook pour les notifications
    val WEBHOOK_URL_2: String? = null, // URL du deuxième webhook pour les
    val WEBHOOK_URL_3: String? = null // URL du troisième webhook pour les notifications
)

class Config {
    companion object {
        private const val filePath = "./config.json"
        private var config: ConfigData = ConfigData()
        private val gson = GsonBuilder().setPrettyPrinting().create()

        fun load() {
            val file = File(filePath)
            config = if (file.exists()) {
                try {
                    gson.fromJson(file.readText(), ConfigData::class.java)
                } catch (e: Exception) {
                    println("Erreur de lecture, valeurs par défaut utilisées.")
                    ConfigData().also { save() }
                }
            } else {
                ConfigData().also { save() }
            }
        }

        fun save() {
            File(filePath).writeText(gson.toJson(config))
        }

        var GMAIL_CODE: String?
            get() = config.GMAIL_CODE
            set(value) {
                config = config.copy(GMAIL_CODE = value); save()
            }

        var WEBHOOK_URL_1: String?
            get() = config.WEBHOOK_URL_1
            set(value) {
                config = config.copy(WEBHOOK_URL_1 = value); save()
            }

        var WEBHOOK_URL_2: String?
            get() = config.WEBHOOK_URL_2
            set(value) {

            config = config.copy(WEBHOOK_URL_2 = value); save()
            }

        var WEBHOOK_URL_3: String?
            get() = config.WEBHOOK_URL_3
            set(value) {
                config = config.copy(WEBHOOK_URL_3 = value); save()
            }


        var LOGIN_URL: String
            get() = config.LOGIN_URL
            set(value) {
                config = config.copy(LOGIN_URL = value); save()
            }

        var USERNAME: String?
            get() = config.USERNAME
            set(value) {
                config = config.copy(USERNAME = value); save()
            }

        var PASSWORD: String?
            get() = config.PASSWORD
            set(value) {
                config = config.copy(PASSWORD = value); save()
            }

        var DATA_URL: String
            get() = config.DATA_URL
            set(value) {
                config = config.copy(DATA_URL = value); save()
            }

        var SEMESTER_KEYS: List<String>?
            get() = config.SEMESTER_KEYS
            set(value) {
                config = config.copy(SEMESTER_KEYS = value); save()
            }
    }
}