import http from "node:http";
import crypto from "node:crypto";

const challenges = new Map();
const TTL_MS = 2 * 60_000;

function json(res, status, body) {
  const data = Buffer.from(JSON.stringify(body));
  res.writeHead(status, {
    "content-type": "application/json",
    "cache-control": "no-store",
    "content-length": data.length
  });
  res.end(data);
}

async function readJson(req) {
  let raw = "";
  for await (const chunk of req) {
    raw += chunk;
    if (raw.length > 256_000) throw new Error("body_too_large");
  }
  return JSON.parse(raw || "{}");
}

function newChallenge() {
  const id = crypto.randomUUID();
  const nonce = crypto.randomBytes(24);
  challenges.set(id, {
    nonceHash: crypto.createHash("sha256").update(nonce).digest("hex"),
    expires: Date.now() + TTL_MS,
    used: false
  });
  return { id, nonce: nonce.toString("base64url"), expires: Date.now() + TTL_MS };
}

async function verifyHuaweiJws(_jws, _expectedNonce) {
  // FAIL CLOSED.
  // Implement Huawei signature + certificate-chain + domain validation here
  // using the current official server-verification requirements.
  return false;
}

const server = http.createServer(async (req, res) => {
  try {
    if (req.method === "POST" && req.url === "/integrity/challenge") {
      const c = newChallenge();
      return json(res, 200, {
        challengeId: c.id,
        nonce: c.nonce,
        expiresAtEpochMs: c.expires
      });
    }

    if (req.method === "POST" && req.url === "/integrity/verify") {
      const body = await readJson(req);
      const c = challenges.get(body.challengeId);
      if (!c || c.used || c.expires < Date.now()) return json(res, 401, { error: "invalid_challenge" });

      // Mark before expensive verification to make the challenge one-use even on failure.
      c.used = true;
      challenges.delete(body.challengeId);

      const ok = await verifyHuaweiJws(body.token, c.nonceHash);
      if (!ok) return json(res, 401, { error: "integrity_rejected" });

      // Replace this opaque sample with a signed server-side session/JWT in production.
      return json(res, 200, { authorized: true, expiresInSeconds: 600 });
    }

    json(res, 404, { error: "not_found" });
  } catch {
    json(res, 400, { error: "bad_request" });
  }
});

server.listen(process.env.PORT || 8080);
