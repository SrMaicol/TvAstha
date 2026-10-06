# AppGallery / Huawei integrity setup

The project contains the integrity architecture but deliberately does **not** contain fake Huawei credentials.

## Required production setup

1. Create/select Astha TV in AppGallery Connect with package `com.asthatv.app`.
2. Configure the SHA-256 fingerprint of the exact release signing certificate.
3. Enable the required Huawei security service.
4. Download your own `agconnect-services.json` and place it in `app/`.
5. Add the current Huawei AG Connect plugin/repository and current Safety Detect dependency according to Huawei's console/docs.
6. Implement `HuaweiSysIntegrityProvider.createProof()` using SysIntegrity and your AGC App ID.
7. Deploy `security-backend/` behind HTTPS.
8. Implement **server-side** Huawei JWS verification: signature, certificate chain, expected domain and exact one-time nonce.
9. Return a short-lived app session only after successful verification.
10. Gate sensitive server operations with that session; do not rely on a local Boolean such as `isDeviceSafe`.

## Policy / UX

Do not permanently ban a user just because an integrity call is temporarily unavailable. Distinguish:
- verified,
- rejected,
- provider/network unavailable.

For local/offline playback, decide separately whether integrity is actually required. Reserve hard authorization for server-side sensitive operations.
