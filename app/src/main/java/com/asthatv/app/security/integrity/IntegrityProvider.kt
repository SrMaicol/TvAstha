package com.asthatv.app.security.integrity

interface IntegrityProvider {
    val name: String
    suspend fun createProof(challenge: IntegrityChallenge): Result<String>
}
