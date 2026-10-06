package com.asthatv.app.security.integrity

import java.util.concurrent.atomic.AtomicReference

class IntegrityGate(
    private val backend: IntegrityBackend,
    private val provider: IntegrityProvider,
    private val now: () -> Long = System::currentTimeMillis
) {
    private val session = AtomicReference<AuthorizedSession?>(null)

    suspend fun authorize(): IntegrityResult {
        session.get()?.takeIf { it.expiresAtEpochMs > now() + 15_000 }?.let {
            return IntegrityResult.Authorized(it)
        }

        val challenge = backend.requestChallenge()
        if (challenge.expiresAtEpochMs <= now()) {
            return IntegrityResult.Rejected("expired_challenge")
        }
        if (challenge.nonce.size !in 16..66) {
            return IntegrityResult.Rejected("invalid_nonce")
        }

        val token = provider.createProof(challenge).getOrElse {
            return IntegrityResult.Unavailable("integrity_provider_unavailable")
        }
        if (token.isBlank()) return IntegrityResult.Rejected("empty_proof")

        return backend.exchangeProof(
            IntegrityProof(challenge.challengeId, provider.name, token)
        ).also { result ->
            if (result is IntegrityResult.Authorized) session.set(result.session)
        }
    }

    fun clearSession() = session.set(null)
}
