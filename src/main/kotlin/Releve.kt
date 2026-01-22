package fr.kewan

import com.google.gson.Gson
import java.io.File
import java.net.HttpURLConnection

class Releve(phpSessionIdClass: PHPSessionId) {
    val filePath = "./releve.json"
    val oldFilePath = "./old_releve.json"
    val oldFolderPath = "./releves/"
    private var phpSessionId: String? = phpSessionIdClass.getSessionId()
        ?: throw IllegalStateException("PHP Session ID is not set. Please set it before running the application.")

    var data = ReleveData(
        ressources = emptyMap(),
        saes = emptyMap(),
        semestre = Semestre(
            notes = Note(value = "~", min = "~", max = "~", moy = "~"),
            rang = Rang(value = "~", total = "~")
        )
    )
        private set

    var oldData = ReleveData(
        ressources = emptyMap(),
        saes = emptyMap(),
        semestre = Semestre(
            notes = Note(value = "~", min = "~", max = "~", moy = "~"),
            rang = Rang(value = "~", total = "~")
        )
    )
        private set


    init {
        if (!File(filePath).exists()) {
            File(filePath).createNewFile()
        } else {
            // Charger les données précédentes si elles existent
            val dataJson = File(filePath).readText().trim()
            if (dataJson.isNotEmpty()) {
                data = Gson().fromJson(dataJson, ReleveData::class.java)
                println("Previous data loaded from $filePath")
            } else {
                println("No previous data found in $filePath")
            }
        }

        if (!File(oldFilePath).exists()) {
            File(oldFilePath).createNewFile()
        } else {
            // Charger les données précédentes si elles existent
            val oldDataJson = File(oldFilePath).readText().trim()
            if (oldDataJson.isNotEmpty()) {
                oldData = Gson().fromJson(oldDataJson, ReleveData::class.java)
                println("Previous data loaded from $oldFilePath")
            } else {
                println("No previous data found in $oldFilePath")
            }
        }

        if (!File(oldFolderPath).exists()) {
            File(oldFolderPath).mkdirs()
        }

        val oldData = File(filePath).readText().trim()
        if (oldData.isNotEmpty()) {
            println("Relevé loaded from $filePath")
            data = Gson().fromJson(oldData, ReleveData::class.java)
        } else {
            println("No previous data found in $filePath, fetching new data.")
        }
        //fetchData(Config.SEMESTER_KEYS?.get(0) ?: "1156")
    }

    private fun saveData(newData: ReleveData) {
        if (newData != this.data) {
            var noteChanges : List<NoteDifference>? = null
            try {
                noteChanges = CompareReleve.findNoteChanges(this.data, newData)
            } catch (e: Exception) {
                println("Error finding note changes: ${e.message}")
            }

            val currentDataJson = Gson().toJson(this.data)
            Gson().toJson(newData)
            oldData = this.data

            // Sauvegarder également une copie avec horodatage
            val timestamp = java.time.LocalDateTime.now().toString().replace(":", "-")
            File("$oldFolderPath/releve_$timestamp.json").writeText(currentDataJson)

            // Mettre à jour data avec les nouvelles valeurs
            this.data = newData
            // Sauvegarder le nouveau relevé dans releve.json
            File(filePath).writeText(Gson().toJson(newData))
            // Sauvegarder le relevé précédent dans old_releve.json
            File(oldFilePath).writeText(Gson().toJson(oldData))

            println("Relevé saved to $filePath and old data saved to $oldFilePath")
            println("Old data saved to $oldFolderPath/releve_$timestamp.json")


            println("Relevé saved to $filePath and old data saved to $oldFilePath")
            println("Old data saved to $oldFolderPath/releve_$timestamp.json")

            val mailer = Mail()
            val discord = Discord()

            // Afficher les changements de notes détectés
            if (noteChanges?.isNotEmpty() == true) {
                println("\n=== CHANGEMENTS DE NOTES DÉTECTÉS ===")
                noteChanges?.forEach {
                    println(it)
                    val mailDat = CompareReleve.textMailNoteDiff(it)
                    mailer.sendMail("mail@kewan.fr", mailDat.first, mailDat.second)
                    val userEmbed = CompareReleve.discordEmbedNoteDiffUser(it)
                    val globalEmbed = CompareReleve.discordEmbedNoteDiffPromo(it)
                    discord.sendWebhook(Config.WEBHOOK_URL_2 ?: "", userEmbed)
                    discord.sendWebhook(
                        Config.WEBHOOK_URL_3 ?: "",
                        globalEmbed,
                        "<@&1447522923836346408&> Nouvelle note pour la promotion 2A !"
                    )

                }
                println("=======================================")
            }


        } else {
            println("No changes detected, not saving.")
        }
    }



    fun fetchData(semesterKey: String) {
        val Data_URL = Config.DATA_URL + semesterKey
        val url = java.net.URL(Data_URL)
        val connection = url.openConnection() as HttpURLConnection
        connection.requestMethod = "GET"
        connection.setRequestProperty("Cookie", "PHPSESSID=$phpSessionId")
        connection.connectTimeout = 10000 // 10 seconds
        connection.readTimeout = 10000 // 10 seconds

        val responseCode: Int = connection.responseCode
        println("Response Code: $responseCode")

        if (responseCode == HttpURLConnection.HTTP_OK) {
            var response = connection.inputStream.bufferedReader().use { it.readText() }
            if (response.isNotEmpty()) {

                // SI la réponse à un élément "redirect"
                if (response.contains("redirect")) {
                    println("Redirect detected, fetching new session ID.")
                    phpSessionId = PHPSessionId().fetchSessionId()
                        ?: throw IllegalStateException("Failed to fetch new PHP Session ID.")
                    fetchData(semesterKey)
                } else {
                   val jsonObject = Gson().fromJson(response, Map::class.java)
                    val releveJson = Gson().toJson(jsonObject["relev\u00e9"])
                    val newData = Gson().fromJson(releveJson, ReleveData::class.java)
                    println("Data fetched successfully from $Data_URL")
                    //println(newData)
                    saveData(newData)
                }
            } else {
                println("No data received from the server.")
                //println(response)
            }
        } else {
            println("Failed to fetch data: $responseCode - ${connection.responseMessage}")
        }


    }
}

/*{
    ressources: {
        "R2.01": {
            id: "R2.01",
            titre: "DevObj",
            evaluations: [
                {
                    id: "E1",
                    description: "Devoir 1",
                    coef: "1.0",
                    note: {
                        value: "15",
                        min: "0",
                        max: "20",
                        moy: "15.0",
                    }

                }
            ]
        },
    },
    saes: {
        "P2.01": {
            id: "SAE1",
            titre: "Portfolio"
            evaluations: [
                {
                    id: "E1",
                    description: "Devoir 1",
                    coef: "1.0",
                    note: {
                        value: "~",
                        min: "~",
                        max: "~",
                        moy: "~",
                    }

                }
            ]
        },
    },
    semestre: {
        notes: {
            value: "15.0",
            min: "0",
            max: "20",
            moy: "15.0",
        },
        rang: {
            value: "1",
            total: "2",
        },
    }
}*/

data class ReleveData(
    val ressources: Map<String, Ressource>,
    val saes: Map<String, Sae>,
    val semestre: Semestre
)

data class Ressource(
    val id: String,
    val titre: String,
    val evaluations: List<Evaluation>
)

data class Sae(
    val id: String,
    val titre: String,
    val evaluations: List<Evaluation>
)

data class Evaluation(
    val id: String,
    val description: String,
    val coef: String,
    val note: Note
)

data class Note(
    val value: String,
    val min: String,
    val max: String,
    val moy: String
)

data class Semestre(
    val notes: Note,
    val rang: Rang
)

data class Rang(
    val value: String,
    val total: String
)