package fr.kewan

import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

fun main() {
    val phpSessionIdClass = PHPSessionId()
    val sessionId = phpSessionIdClass.getSessionId()
        ?: throw IllegalStateException("PHP Session ID is not set. Please set it before running the application.")

    val releve = Releve(phpSessionIdClass)

    val executor = Executors.newScheduledThreadPool(1)

    val task = Runnable {
        try {
            // Appeler votre fonction principale
            releve.fetchData(Config.SEMESTER_KEYS!![0])
        } catch (e: Exception) {
            println("Erreur lors de l'exécution de la tâche : ${e.message}")
        }
    }

    // 10 minutes
    val interval = 10 * 60L
    executor.scheduleAtFixedRate(task, 0, interval, TimeUnit.SECONDS)
}
