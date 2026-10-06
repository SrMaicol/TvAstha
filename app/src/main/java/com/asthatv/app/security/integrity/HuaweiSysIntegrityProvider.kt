package com.asthatv.app.security.integrity

import android.content.Context

/**
 * Wiring point for Huawei Safety Detect SysIntegrity.
 *
 * Keep this class provider-neutral in the base ZIP so the project still builds before
 * you add your real AppGallery Connect project. After adding agconnect-services.json
 * and the Huawei Safety Detect dependency, implement createProof with:
 *
 * SafetyDetect.getClient(context).sysIntegrity(challenge.nonce, YOUR_AGC_APP_ID)
 *
 * Return response.result as the token. YOUR_AGC_APP_ID is a public app identifier,
 * not a server secret. The server must verify the returned JWS and nonce.
 */
class HuaweiSysIntegrityProvider(
    @Suppress("UNUSED_PARAMETER") private val context: Context
) : IntegrityProvider {
    override val name: String = "huawei-sysintegrity"

    override suspend fun createProof(challenge: IntegrityChallenge): Result<String> =
        Result.failure(
            IllegalStateException(
                "Huawei SysIntegrity is not configured. Add your AGC project and Safety Detect SDK."
            )
        )
}
