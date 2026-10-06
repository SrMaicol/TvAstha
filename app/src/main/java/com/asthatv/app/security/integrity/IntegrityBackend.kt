package com.asthatv.app.security.integrity

/**
 * Production contract:
 * 1) request a one-time nonce from your server;
 * 2) obtain an integrity proof from the platform provider;
 * 3) send proof + challengeId back to your server;
 * 4) server verifies signature/certificate/domain/nonce and returns a short-lived session.
 *
 * Never put server signing keys or master API secrets in the APK.
 */
interface IntegrityBackend {
    suspend fun requestChallenge(): IntegrityChallenge
    suspend fun exchangeProof(proof: IntegrityProof): IntegrityResult
}
