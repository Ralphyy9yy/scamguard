package com.example.scamguard.util

import android.util.Patterns

/**
 * Lightweight rule engine that classifies text content using heuristic rules.
 */
object ScamDetectorUtil {

    private val urgentPhrases = listOf(
        "urgent",
        "immediately",
        "final notice",
        "action required",
        "act now",
        "respond now",
        "limited time",
        "click now"
    )

    private val sensitiveInfoPatterns = listOf(
        Regex("verify(?: your)? (?:account|identity|bank|wallet)", RegexOption.IGNORE_CASE),
        Regex("update (?:atm|bank|payment) pin", RegexOption.IGNORE_CASE),
        Regex("otp\\s?code", RegexOption.IGNORE_CASE),
        Regex("confirm(?:ation)? (?:number|code)", RegexOption.IGNORE_CASE)
    )

    private val financialPatterns = listOf(
        Regex("(?:\\$|usd|eur|gbp)\\s?\\d{2,}", RegexOption.IGNORE_CASE),
        Regex("pay(?:ment)? (?:fee|charge)", RegexOption.IGNORE_CASE),
        Regex("transfer (?:funds|money)", RegexOption.IGNORE_CASE),
        Regex("prize|lottery|reward|bonus", RegexOption.IGNORE_CASE)
    )

    private val riskyDomains = listOf(".xyz", ".top", ".tk", ".gq", ".ml")
    private val shortenerHosts = listOf("bit.ly", "tinyurl", "t.co", "goo.gl", "ow.ly", "is.gd")
    private val urlPattern = Regex("https?://[^\\s]+", RegexOption.IGNORE_CASE)

    fun detect(content: String): ScamDetectorResult {
        val normalized = content.trim()
        if (normalized.isEmpty()) {
            return ScamDetectorResult(ScamVerdict.SAFE, "Empty content")
        }

        val onlyLink = Patterns.WEB_URL.matcher(normalized).matches()
        if (onlyLink) {
            return ScamDetectorResult(ScamVerdict.DANGER, "Message is only a link")
        }

        val lowercase = normalized.lowercase()
        val signals = mutableListOf<ScamSignal>()

        val containsUrgentLanguage = urgentPhrases.any { lowercase.contains(it) }
        if (containsUrgentLanguage) {
            signals += ScamSignal("Uses urgent call-to-action", severity = 1)
        }

        val asksSensitiveInfo = sensitiveInfoPatterns.any { it.containsMatchIn(normalized) }
        if (asksSensitiveInfo) {
            signals += ScamSignal("Requests sensitive account information", severity = 2)
        }

        val mentionsMoney = financialPatterns.any { it.containsMatchIn(normalized) }
        if (mentionsMoney) {
            signals += ScamSignal("Mentions money transfer or prize", severity = 1)
        }

        val containsRiskyDomain = riskyDomains.any { lowercase.contains(it) }
        if (containsRiskyDomain) {
            signals += ScamSignal("Mentions unusual domain extension", severity = 2)
        }

        val urlsInMessage = urlPattern.findAll(normalized).map { it.value }.toList()
        val containsShortUrl = urlsInMessage.any { url ->
            shortenerHosts.any { host -> url.contains(host, ignoreCase = true) }
        }
        if (containsShortUrl) {
            signals += ScamSignal("Link uses URL shortener", severity = 2)
        }

        val containsSuspiciousUrl = urlsInMessage.any { url ->
            riskyDomains.any { url.contains(it, ignoreCase = true) }
        }
        if (containsSuspiciousUrl) {
            signals += ScamSignal("Link redirects to risky domain", severity = 2)
        }

        if (signals.isEmpty()) {
            return ScamDetectorResult(ScamVerdict.SAFE, "No scam patterns detected")
        }

        val totalSeverity = signals.sumOf { it.severity }
        val verdict = when {
            totalSeverity >= 4 -> ScamVerdict.DANGER
            else -> ScamVerdict.SUSPICIOUS
        }

        val reason = signals.joinToString(separator = "; ") { it.reason }
        return ScamDetectorResult(verdict, reason)
    }
}

private data class ScamSignal(
    val reason: String,
    val severity: Int
)

data class ScamDetectorResult(
    val verdict: ScamVerdict,
    val reason: String
)

enum class ScamVerdict(val firestoreValue: String, val displayName: String) {
    SAFE("safe", "Safe"),
    SUSPICIOUS("suspicious", "Suspicious"),
    DANGER("danger", "Danger");

    companion object {
        fun fromFirestoreValue(raw: String): ScamVerdict {
            return entries.firstOrNull { it.firestoreValue == raw } ?: SAFE
        }
    }
}

