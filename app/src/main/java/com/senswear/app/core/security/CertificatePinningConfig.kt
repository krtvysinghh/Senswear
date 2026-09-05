package com.senswear.app.core.security

/**
 * Defines SHA-256 public key hashes for Cloud Sync API domains to prevent MITM attacks.
 */
object CertificatePinningConfig {

    data class PinnedDomain(
        val hostname: String,
        val pins: List<String>
    )

    val pinnedDomains = listOf(
        PinnedDomain(
            hostname = "api.whoop.com",
            pins = listOf(
                "sha256/k2oTAnmA3JxR+k2oTAnmA3JxR+k2oTAnmA3JxR+k2oT=",
                "sha256/r/mIts6OE1hsCnEcYOEhmVEq78UBjChMpdCXY50D1o8="
            )
        ),
        PinnedDomain(
            hostname = "connectapi.garmin.com",
            pins = listOf(
                "sha256/WoiWRyIOVNa9ihaBciRSC7XHjliYS9VwUGOIud4PB18=",
                "sha256/FEzVOUp4dF3gI0ZVPRJhFbS1cVoQmNbdAF3UCGQkfsw="
            )
        )
    )
}
