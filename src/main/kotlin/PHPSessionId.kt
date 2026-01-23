package fr.kewan

import com.microsoft.playwright.BrowserType
import com.microsoft.playwright.Playwright
import com.microsoft.playwright.Page
import java.nio.file.Paths

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

            // listeners de debug
            page.onConsoleMessage { msg -> println("[PAGE-CONSOLE][${'$'}{msg.type()}] ${'$'}{msg.text()}") }
            page.onRequestFailed { req -> println("[PAGE-REQ-FAILED] ${'$'}{req.method()} ${'$'}{req.url()} cause=${'$'}{req.failure()?.errorText}" ) }
            page.onResponse { res -> if (res.status() >= 400) println("[PAGE-RESP] ${'$'}{res.status()} ${'$'}{res.url()}") }

            val debugDir = "./playwright_debug"
            try { java.io.File(debugDir).mkdirs() } catch (_: Exception) { }
            fun dumpState(prefix: String) {
                val ts = System.currentTimeMillis()
                try { page.screenshot(Page.ScreenshotOptions().setPath(Paths.get("${debugDir}/${prefix}_screen_${ts}.png")).setFullPage(true)) } catch (_: Throwable) { }
                try { java.io.File("${debugDir}/${prefix}_page_${ts}.html").writeText(page.content()) } catch (_: Throwable) { }
            }

            page.navigate("https://notes.iut-nantes.univ-nantes.fr/services/doAuth.php?href=https%3A%2F%2Fnotes.iut-nantes.univ-nantes.fr%2F")
            println("connexion à la page")

            // sleep
            page.waitForTimeout(2000.0)
            dumpState("after_navigate")

            // attentes plus longues pour être tolérant aux latences réseau
            page.waitForSelector("input[name=\"username\"]", Page.WaitForSelectorOptions().setTimeout(60000.0))
            page.waitForSelector("input[name=\"password\"]", Page.WaitForSelectorOptions().setTimeout(60000.0))
            println("selectors found, filling credentials")

            page.fill("input[name=\"username\"]",
                Config.USERNAME
                    ?: throw IllegalStateException("Username is not set in the configuration. Please set it before running the application.")
            )
            page.fill("input[name=\"password\"]",
                Config.PASSWORD
                    ?: throw IllegalStateException("Password is not set in the configuration. Please set it before running the application.")
            )

            // petite attente avant la soumission
            page.waitForTimeout(1000.0)

            var navigationAttempted = false
            // tenter un click classique, sinon collecter des diagnostics et essayer des alternatives
            try {
                println("click submit")
                page.click("button[name=\"submitBtn\"]")
                navigationAttempted = true
            } catch (e: Throwable) {
                println("Primary click failed: ${'$'}{e.message}")

                val timestamp = System.currentTimeMillis()
                try {
                    // sauvegarder screenshot
                    page.screenshot(Page.ScreenshotOptions().setPath(Paths.get("$debugDir/screen_$timestamp.png")).setFullPage(true))
                } catch (s: Throwable) {
                    println("Failed to take screenshot: ${'$'}{s.message}")
                }

                try {
                    // sauvegarder le contenu HTML
                    java.io.File("$debugDir/page_$timestamp.html").writeText(page.content())
                } catch (w: Throwable) {
                    println("Failed to save page content: ${'$'}{w.message}")
                }

                // essayer d'appuyer sur Enter dans le champ password
                try {
                    page.press("input[name=\"password\"]", "Enter")
                    navigationAttempted = true
                    println("Tried pressing Enter on password field as fallback")
                } catch (p: Throwable) {
                    println("press Enter failed: ${'$'}{p.message}")
                    try {
                        // dernier recours : soumettre le premier formulaire via JS
                        page.evaluate("() => { const f = document.querySelector('form'); if(f) f.submit(); }")
                        navigationAttempted = true
                        println("Tried JS form.submit() as last fallback")
                    } catch (j: Throwable) {
                        println("JS form.submit() failed: ${'$'}{j.message}")
                    }
                }

                // après les alternatives, attendre un peu pour laisser la navigation se faire
                page.waitForTimeout(5000.0)
            }

            // si on a tenté une navigation, attendre la navigation explicite
            if (navigationAttempted) {
                try {
                    page.waitForLoadState()
                    page.waitForTimeout(60000.0)
                } catch (n: Throwable) {
                    println("waitForLoadState failed or timed out: ${'$'}{n.message}")
                }
            }

            // attendre la navigation / cookies
            page.waitForTimeout(2000.0)

            dumpState("before_cookie_check")

            // Polling des cookies pendant un timeout total (20s) pour récupérer PHPSESSID
            val maxPollMs = 20000L
            val pollIntervalMs = 1000L
            var elapsed = 0L
            var PHPSESSID: String? = null
            while (elapsed < maxPollMs) {
                val cookies = page.context().cookies()
                PHPSESSID = cookies.find { it.name == "PHPSESSID" }?.value
                if (!PHPSESSID.isNullOrEmpty()) break
                Thread.sleep(pollIntervalMs)
                elapsed += pollIntervalMs
            }

            // fallback: lire une dernière fois
            if (PHPSESSID.isNullOrEmpty()) {
                val cookies = page.context().cookies()
                PHPSESSID = cookies.find { it.name == "PHPSESSID" }?.value
            }

            browser.close()

            if (PHPSESSID.isNullOrEmpty()) {
                throw IllegalStateException("Failed to fetch PHP Session ID. Please check your credentials and network connection. Debug artifacts (if any) are in ./playwright_debug/")
            } else {
                setSessionId(PHPSESSID)
                return PHPSESSID
            }


        }
    }
}