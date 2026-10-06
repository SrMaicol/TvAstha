# Astha TV integrity backend reference

This folder is a deployment reference, not code embedded in the APK.

Flow:

1. `POST /integrity/challenge` creates 24 random nonce bytes, stores only a hash with a 2 minute TTL, and returns challenge ID + nonce.
2. The TV requests Huawei SysIntegrity using that exact nonce.
3. `POST /integrity/verify` accepts the Huawei JWS.
4. Production verification MUST validate Huawei's signature, certificate chain and domain, then compare the nonce to the one-time challenge.
5. Only after successful verification issue a short-lived application session (recommended 5–15 minutes).

Do not copy a server private key, signing secret, Debrid master credential, or Trakt client secret into the Android app.

`server.mjs` intentionally leaves Huawei JWS cryptographic verification behind `verifyHuaweiJws()` because it requires the current Huawei trust-chain requirements and your deployment environment. It fails closed until implemented.
