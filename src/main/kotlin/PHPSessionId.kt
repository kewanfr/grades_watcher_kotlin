package fr.kewan

import com.microsoft.playwright.BrowserType
import com.microsoft.playwright.Playwright

class PHPSessionId {
    val filePath = "./PHPSessionId.txt"
    private val sessionId: String?

    init {
        Config.load()
        if (!java.io.File(filePath).exists()) {
            java.io.File(filePath).createNewFile()
        }

        val sessionId = java.io.File(filePath).readText().trim()
        if (sessionId.isEmpty()) {
            //throw IllegalStateException("PHP Session ID is not set in $filePath. Please set it before running the application.")
            this.sessionId = fetchSessionId()
        } else {
            println("PHP Session ID loaded: $sessionId")
            this.sessionId = sessionId
        }
    }


    fun setSessionId(newSessionId: String) {
        java.io.File(filePath).writeText(newSessionId.trim())
        println("PHP Session ID updated: $newSessionId")
    }

    fun getSessionId(): String? {
        if (this.sessionId.isNullOrEmpty()) {
            return fetchSessionId()
        } else {
            return this.sessionId
        }
    }

    fun fetchSessionId(): String? {
        Playwright.create().use { playwright ->
            val browser = playwright.chromium().launch(BrowserType.LaunchOptions().setHeadless(true))
            val context = browser.newContext()
            val page = context.newPage()

            page.navigate("https://notes.iut-nantes.univ-nantes.fr/services/doAuth.php?href=https%3A%2F%2Fnotes.iut-nantes.univ-nantes.fr%2F")
            println("connexion à la page")
            page.waitForSelector("input[name=\"username\"]")
            page.waitForSelector("input[name=\"password\"]")

            page.fill("input[name=\"username\"]",
                Config.USERNAME
                    ?: throw IllegalStateException("Username is not set in the configuration. Please set it before running the application.")
            )
            page.fill("input[name=\"password\"]",
                Config.PASSWORD
                    ?: throw IllegalStateException("Password is not set in the configuration. Please set it before running the application.")
            )

            page.waitForTimeout(1000.0)
            page.click("button[name=\"submit\"]")

            page.waitForTimeout(15000.0)

            val cookies = page.context().cookies()
            val PHPSESSID = cookies.find { it.name == "PHPSESSID" }?.value
            //println("PHPSESSID: $PHPSESSID")
            //println("Cookies: $cookies")

            browser.close()

            if (PHPSESSID.isNullOrEmpty()) {
                throw IllegalStateException("Failed to fetch PHP Session ID. Please check your credentials and network connection.")
            } else {
                setSessionId(PHPSESSID)
                return PHPSESSID
            }


        }
    }
}