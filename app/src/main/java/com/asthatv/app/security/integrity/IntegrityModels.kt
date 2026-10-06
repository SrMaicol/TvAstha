package com.asthatv.app.security.integrity

data class IntegrityChallenge(
    val challengeId: String,
    val nonce: ByteArray,
    val expiresAtEpochMs: Long
)

data class IntegrityProof(
    val challengeId: String,
    val provider: String,
    val token: String
)

data class AuthorizedSession(
    val accessToken: String,
    val expiresAtEpochMs: Long
)

sealed interface IntegrityResult {
    data class Authorized(val session: AuthorizedSession) : IntegrityResult
    data class Rejected(val reason: String) : IntegrityResult
    data class Unavailable(val reason: String) : IntegrityResult
}
