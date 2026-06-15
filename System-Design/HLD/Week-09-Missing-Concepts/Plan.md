# Week 09 — Critical HLD Concepts You Were Missing

> These 10 topics appear in almost every system design interview but somehow slip through
> standard prep. Each one explained in plain English with Indian analogies, ASCII diagrams,
> and real company examples.

---

## Table of Contents

1. [OAuth 2.0 / Authentication](#1-oauth-20--authentication)
2. [Reverse Proxy](#2-reverse-proxy)
3. [Distributed Transactions](#3-distributed-transactions)
4. [Leader Election & Consensus](#4-leader-election--consensus)
5. [Bloom Filters](#5-bloom-filters)
6. [Geospatial Indexing](#6-geospatial-indexing)
7. [Service Mesh](#7-service-mesh)
8. [Data Lake vs Data Warehouse](#8-data-lake-vs-data-warehouse)
9. [Checksum & Data Integrity](#9-checksum--data-integrity)
10. [Idempotency](#10-idempotency)

---
---

## 1. OAuth 2.0 / Authentication

### What Is It (Plain English)

Imagine you want to log into Zomato, but instead of creating yet another
username/password, you click "Login with Google". Zomato never sees your
Google password. Google just tells Zomato: "Haan bhai, yeh banda genuine hai,
yeh uska naam aur email hai."

OAuth 2.0 is the protocol that makes this happen. It lets a third-party app
(Zomato) access your data on another service (Google) WITHOUT ever knowing
your password.

### Real-Life Analogy — The Hotel Receptionist

```
You check into a Taj Hotel. You show your Aadhaar to the receptionist.
The receptionist gives you a KEY CARD (access token).

- The key card opens YOUR room, the gym, the pool.
- The key card does NOT open other guests' rooms.
- The key card EXPIRES after checkout.
- You never gave the hotel your Aadhaar permanently.

Aadhaar   = Your Google credentials
Key card  = Access token
Hotel     = Third-party app (Zomato, Spotify, etc.)
```

### Why It Exists

Before OAuth, if Zomato wanted to access your Google contacts, you had to
literally give Zomato your Google password. Problems:

1. Zomato could do ANYTHING with your Google account (read emails, delete files)
2. If Zomato got hacked, your Google password leaked
3. You could not revoke Zomato's access without changing your Google password
4. Every app stored your password — massive security risk

OAuth solved ALL of these. The app gets a limited, revocable, time-bound
token instead of your actual password.

### How It Works — The Full Flow

Here is what happens when you click "Login with Google" on Zomato:

```
  +---------+          +----------+          +----------+
  |  User   |          |  Zomato  |          |  Google  |
  | (You)   |          | (Client) |          | (AuthZ   |
  |         |          |          |          |  Server) |
  +---------+          +----------+          +----------+
       |                     |                     |
       | 1. Click "Login     |                     |
       |    with Google"     |                     |
       |-------------------->|                     |
       |                     |                     |
       |  2. Redirect to     |                     |
       |     Google login    |                     |
       |<--------------------|                     |
       |                     |                     |
       | 3. User logs into   |                     |
       |    Google, clicks   |                     |
       |    "Allow Zomato"   |                     |
       |------------------------------------------>|
       |                     |                     |
       |  4. Google sends    |                     |
       |     AUTH CODE back  |                     |
       |     to Zomato's     |                     |
       |     redirect URL    |                     |
       |     (via browser)   |                     |
       |<------------------------------------------|
       |-------------------->|                     |
       |                     |                     |
       |                     | 5. Zomato sends     |
       |                     |    auth code +      |
       |                     |    client_secret    |
       |                     |    to Google        |
       |                     |    (server-to-      |
       |                     |     server, secure) |
       |                     |-------------------->|
       |                     |                     |
       |                     | 6. Google verifies  |
       |                     |    and sends back   |
       |                     |    ACCESS TOKEN +   |
       |                     |    REFRESH TOKEN    |
       |                     |<--------------------|
       |                     |                     |
       |  7. Zomato uses     |                     |
       |     access token    |                     |
       |     to fetch your   |                     |
       |     profile/email   |                     |
       |                     |-------------------->|
       |                     |                     |
       |                     | 8. Google returns   |
       |                     |    user profile     |
       |                     |<--------------------|
       |                     |                     |
       |  9. Zomato logs     |                     |
       |     you in!         |                     |
       |<--------------------|                     |
```

#### Step-by-step breakdown:

**Step 1-2: The Redirect**
- You click "Login with Google" on Zomato
- Zomato redirects your browser to Google's authorization endpoint:
  ```
  https://accounts.google.com/o/oauth2/auth?
    client_id=zomato_123
    &redirect_uri=https://zomato.com/callback
    &response_type=code
    &scope=email profile
    &state=random_csrf_token
  ```

**Step 3: User Consent**
- You see Google's login page
- You enter YOUR Google credentials (Zomato never sees these!)
- Google shows: "Zomato wants to access your email and profile. Allow?"
- You click "Allow"

**Step 4: The Auth Code**
- Google redirects your browser back to Zomato's callback URL:
  ```
  https://zomato.com/callback?code=AUTH_CODE_xyz&state=random_csrf_token
  ```
- This auth code is SHORT-LIVED (usually 10 minutes) and ONE-TIME use

**Step 5-6: Token Exchange (Backend)**
- Zomato's SERVER (not browser!) sends the auth code + client_secret to Google
- This is server-to-server, so the client_secret is never exposed to the browser
- Google verifies everything and returns an access token + refresh token

**Step 7-9: Using the Token**
- Zomato uses the access token to call Google APIs:
  ```
  GET https://www.googleapis.com/oauth2/v2/userinfo
  Authorization: Bearer ACCESS_TOKEN_abc
  ```
- Google returns your name, email, profile picture
- Zomato creates/updates your account and logs you in

### OAuth 2.0 Grant Types

Different situations need different flows:

```
+-------------------------+------------------+----------------------------+
| Grant Type              | When To Use      | Example                    |
+-------------------------+------------------+----------------------------+
| Authorization Code      | Web apps with    | Zomato web app logging     |
|                         | a backend server | in with Google             |
+-------------------------+------------------+----------------------------+
| Authorization Code      | Mobile apps,     | Swiggy mobile app logging  |
| + PKCE                  | SPAs (no secret  | in with Google             |
|                         | can be stored)   | (no client_secret stored)  |
+-------------------------+------------------+----------------------------+
| Client Credentials      | Machine-to-      | Zomato server calling      |
|                         | machine, no user | Google Maps API for        |
|                         | involved         | restaurant locations       |
+-------------------------+------------------+----------------------------+
| Implicit (DEPRECATED)   | Was for SPAs,    | DON'T USE — replaced by    |
|                         | now replaced by  | Authorization Code + PKCE  |
|                         | PKCE             |                            |
+-------------------------+------------------+----------------------------+
```

#### PKCE (Proof Key for Code Exchange) — Explained Simply

Problem: Mobile apps and browser-only SPAs cannot securely store a
client_secret. Anyone can decompile your APK or read your JavaScript.

Solution: PKCE adds a one-time-use "challenge" that replaces client_secret.

```
  +------------+                    +----------+
  |  Mobile    |                    |  Google  |
  |  App       |                    |  AuthZ   |
  +------------+                    +----------+
       |                                 |
       | 1. Generate random              |
       |    code_verifier                 |
       |    (e.g., "abc123xyz")          |
       |                                 |
       | 2. Hash it to get               |
       |    code_challenge               |
       |    SHA256("abc123xyz")          |
       |    = "hashed_value"             |
       |                                 |
       | 3. Send auth request with       |
       |    code_challenge               |
       |-------------------------------->|
       |                                 |
       | 4. Google sends back            |
       |    auth_code                    |
       |<--------------------------------|
       |                                 |
       | 5. Exchange auth_code +         |
       |    code_verifier (original)     |
       |-------------------------------->|
       |                                 |
       | 6. Google hashes verifier,      |
       |    compares with stored         |
       |    challenge. MATCH!            |
       |    Returns access token         |
       |<--------------------------------|
```

Even if an attacker intercepts the auth code (step 4), they cannot
exchange it without the original code_verifier, which never left the app.

#### Client Credentials — Machine-to-Machine

```
  +------------+                    +----------+
  |  Zomato    |                    |  Google  |
  |  Server    |                    |  OAuth   |
  +------------+                    +----------+
       |                                 |
       | POST /token                     |
       | grant_type=client_credentials   |
       | client_id=zomato_123            |
       | client_secret=secret_xyz        |
       |-------------------------------->|
       |                                 |
       | Returns access_token            |
       |<--------------------------------|
       |                                 |
       | GET /maps/api/geocode           |
       | Authorization: Bearer token     |
       |-------------------------------->|
```

No user involved. No login screen. No redirect. Just machine talking to
machine. Used for: background jobs, cron tasks, server-to-server APIs.

### JWT Deep Dive (JSON Web Token)

#### What Is a JWT?

A JWT is a compact, self-contained token that carries information (claims)
about a user. It looks like this:

```
eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9.
eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IlNoZWV0YWwiLCJyb2xlIjoiYWRtaW4iLCJpYXQiOjE3MTY0MDAwMDAsImV4cCI6MTcxNjQwMzYwMH0.
SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c
```

Three parts separated by dots:

```
HEADER.PAYLOAD.SIGNATURE
```

#### JWT Structure — Decoded

```
+-----------------------------------------------------------+
|  HEADER (Algorithm + Token Type)                          |
|                                                           |
|  {                                                        |
|    "alg": "RS256",     // Algorithm used to sign          |
|    "typ": "JWT"        // Token type                      |
|  }                                                        |
+-----------------------------------------------------------+
|  PAYLOAD (Claims — the actual data)                       |
|                                                           |
|  {                                                        |
|    "sub": "user_12345",          // Subject (user ID)     |
|    "name": "Sheetal",            // User's name           |
|    "role": "admin",              // User's role           |
|    "org_id": "org_789",          // Organization          |
|    "iat": 1716400000,            // Issued At             |
|    "exp": 1716403600             // Expiry (1 hour)       |
|  }                                                        |
+-----------------------------------------------------------+
|  SIGNATURE                                                |
|                                                           |
|  RS256(                                                   |
|    base64(header) + "." + base64(payload),                |
|    PRIVATE_KEY                                            |
|  )                                                        |
+-----------------------------------------------------------+
```

#### How JWT Verification Works WITHOUT Hitting the Database

This is the magic. Traditional sessions require a database lookup on EVERY
request. JWTs do not.

```
Traditional Session:
  +--------+       +--------+       +-----------+
  | Client | ----> | Server | ----> | Session   |
  |        |       |        |       | Database  |
  +--------+       +--------+       +-----------+
  Every request hits the DB to check "is this session valid?"


JWT:
  +--------+       +--------+
  | Client | ----> | Server |       No DB call!
  |        |       |        |
  +--------+       +--------+
  Server verifies the SIGNATURE using the PUBLIC KEY.
  If signature is valid AND token hasn't expired, the user is authenticated.
  All user info (name, role, org) is RIGHT THERE in the payload.
```

**How does the server verify without a database?**

1. Server takes the header + payload from the token
2. Server computes the signature using its PUBLIC key (for RS256)
   or shared SECRET key (for HS256)
3. Server compares computed signature with the one in the token
4. If they match: nobody tampered with it, the data is trustworthy
5. Server checks `exp` claim: if current time > exp, token is expired

No database. No Redis. No session store. Just math.

#### JWT vs Session-Based Auth — Comparison

```
+-----------------------+---------------------+---------------------+
| Aspect                | Session (Cookie)    | JWT (Token)         |
+-----------------------+---------------------+---------------------+
| Storage               | Server stores       | Client stores       |
|                       | session in DB/Redis | token in memory/    |
|                       |                     | localStorage        |
+-----------------------+---------------------+---------------------+
| DB lookup per request | YES — every request | NO — self-contained |
|                       | checks session store|                     |
+-----------------------+---------------------+---------------------+
| Scalability           | Hard — sessions     | Easy — any server   |
|                       | are server-specific | can verify the JWT  |
|                       | (need sticky        | (stateless)         |
|                       | sessions or shared  |                     |
|                       | Redis)              |                     |
+-----------------------+---------------------+---------------------+
| Revocation            | Easy — delete from  | Hard — token valid  |
|                       | session store       | until it expires    |
|                       |                     | (need blocklist)    |
+-----------------------+---------------------+---------------------+
| Size                  | Small cookie ID     | Larger (carries     |
|                       | (~32 bytes)         | payload, ~300+ B)   |
+-----------------------+---------------------+---------------------+
| Cross-domain          | Hard (cookies are   | Easy (token in      |
|                       | domain-bound)       | Authorization       |
|                       |                     | header)             |
+-----------------------+---------------------+---------------------+
| Best for              | Traditional server- | Microservices,      |
|                       | rendered apps,      | SPAs, mobile apps,  |
|                       | single-domain       | cross-domain,       |
|                       |                     | APIs                |
+-----------------------+---------------------+---------------------+
```

### Refresh Tokens

Access tokens are intentionally SHORT-LIVED (15 min to 1 hour).
Why? If one gets stolen, the damage is limited.

But the user should not have to log in every 15 minutes! Enter refresh tokens.

```
  +--------+                    +---------+
  | Client |                    | Auth    |
  |        |                    | Server  |
  +--------+                    +---------+
       |                             |
       | 1. Login                    |
       |     (username + password)   |
       |---------------------------->|
       |                             |
       | 2. Here are your tokens:    |
       |    access_token  (15 min)   |
       |    refresh_token (30 days)  |
       |<----------------------------|
       |                             |
       | 3. API call with access     |
       |    token for next 15 min    |
       |---------------------------->|  (goes to Resource Server)
       |                             |
       |       ... 15 min later ...  |
       |                             |
       | 4. Access token expired!    |
       |    API returns 401          |
       |<----------------------------|
       |                             |
       | 5. Send refresh_token       |
       |    to get new access_token  |
       |---------------------------->|
       |                             |
       | 6. New access_token!        |
       |    (maybe new refresh too)  |
       |<----------------------------|
       |                             |
       | 7. Continue API calls       |
       |    with new access token    |
       |---------------------------->|
```

**Key rules for refresh tokens:**
- Stored securely (HTTP-only cookie or secure storage, NEVER localStorage)
- Much longer-lived than access tokens (days/weeks vs minutes)
- Can be revoked server-side (they ARE stored in a database)
- Some systems "rotate" them: every time you use a refresh token,
  you get a new one and the old one is invalidated

### SSO — Single Sign-On

#### What Is It?

You log into your Google account ONCE. Now you can access:
- Gmail
- YouTube
- Google Drive
- Google Maps
- Google Photos
...without logging in again for each one.

This is SSO. One login, many services.

**Indian analogy:** Think of it like your Metro Smart Card. You tap once at
the entry gate (login). Now you can ride any metro line, transfer at any
station, all on that one tap. You do not need to buy a new ticket for each
line.

```
  +---------+      +------------------+
  |  User   |----->| Identity Provider|
  |         |      | (Google/Okta/    |
  +---------+      |  ADFS)           |
       |           +------------------+
       |                 |   |   |
       |                 |   |   |
       v                 v   v   v
  +---------+    +-----+ +-----+ +-----+
  | Login   |    |Gmail| | YT  | |Drive|
  | once!   |    |     | |     | |     |
  +---------+    +-----+ +-----+ +-----+
                   All trust the Identity Provider's token
```

#### How SSO Works:

1. You go to Gmail. Gmail sees you are not logged in.
2. Gmail redirects you to Google's Identity Provider (IdP).
3. You log in at the IdP. IdP creates a SESSION for you.
4. IdP redirects you back to Gmail WITH a token. Gmail lets you in.
5. Now you go to YouTube. YouTube sees you are not logged in.
6. YouTube redirects you to the SAME IdP.
7. IdP sees you ALREADY HAVE A SESSION (from step 3). No login needed!
8. IdP redirects you back to YouTube WITH a token. YouTube lets you in.

### SAML vs OAuth vs OpenID Connect

These three get confused ALL the time. Here is the difference:

```
+-------------------+--------------------+--------------------+-------------------+
| Aspect            | SAML 2.0           | OAuth 2.0          | OpenID Connect    |
+-------------------+--------------------+--------------------+-------------------+
| Purpose           | Authentication     | Authorization      | Authentication    |
|                   | (WHO are you?)     | (WHAT can you      | (WHO are you?)    |
|                   |                    |  access?)          | + Authorization   |
+-------------------+--------------------+--------------------+-------------------+
| Token format      | XML assertion      | Access token       | JWT (ID token)    |
|                   | (big, verbose)     | (opaque or JWT)    | + access token    |
+-------------------+--------------------+--------------------+-------------------+
| Transport         | XML over HTTP POST | JSON over HTTPS    | JSON over HTTPS   |
+-------------------+--------------------+--------------------+-------------------+
| Era               | 2005 (enterprise)  | 2012               | 2014              |
|                   |                    |                    | (built ON OAuth)  |
+-------------------+--------------------+--------------------+-------------------+
| Best for          | Enterprise SSO     | API authorization  | "Login with       |
|                   | (Okta, ADFS,       | (access user's     |  Google/Facebook"  |
|                   | corporate apps)    | Google Drive,      | consumer apps     |
|                   |                    | Spotify playlists) |                   |
+-------------------+--------------------+--------------------+-------------------+
| Indian analogy    | Your company ID    | A valet parking    | Aadhaar e-KYC:    |
|                   | badge that works   | token — the valet  | proves who you    |
|                   | across all office  | can DRIVE your car | are AND gives     |
|                   | buildings in the   | but cannot SELL it | limited info      |
|                   | tech park          |                    | (name, DOB)       |
+-------------------+--------------------+--------------------+-------------------+
| Key point         | Old, XML-heavy,    | Does NOT tell you  | OAuth 2.0 +       |
|                   | but still dominant | WHO the user is!   | identity layer    |
|                   | in enterprise      | Only what they     | = OpenID Connect  |
|                   |                    | can access         |                   |
+-------------------+--------------------+--------------------+-------------------+
```

**The critical distinction:**
- OAuth 2.0 alone = "This token lets you access my photos" (authorization)
- OpenID Connect = OAuth 2.0 + "and by the way, my name is Sheetal, email
  is sheetal@example.com" (authentication + authorization)

### Companies Using It

| Company        | What They Use                                      |
|----------------|----------------------------------------------------|
| Google         | OAuth 2.0 + OpenID Connect for "Login with Google" |
| Facebook/Meta  | OAuth 2.0 for Facebook Login                       |
| Okta/Auth0     | SAML + OAuth + OIDC — enterprise SSO providers     |
| GitHub         | OAuth 2.0 for third-party app access               |
| Razorpay       | OAuth 2.0 for merchant integrations                |
| Microsoft      | ADFS (SAML) for enterprise, Azure AD (OIDC) for cloud |

### When to Use in System Design

- User mentions "Login with Google/Facebook" → OAuth 2.0 + OpenID Connect
- Enterprise SSO across internal apps → SAML or OIDC
- API-to-API communication, no user → Client Credentials grant
- Mobile app authentication → Authorization Code + PKCE
- Microservices verifying user identity → JWT passed between services

---
---

## 2. Reverse Proxy

### What Is It (Plain English)

A reverse proxy sits in FRONT of your servers and handles all incoming
traffic. Clients never talk to your actual servers directly — they talk
to the reverse proxy, which decides what to do.

### Real-Life Analogy — The Office Receptionist

```
Forward Proxy (like VPN):
  You hide behind the proxy. The internet does not know who you are.

  +------+     +-------+     +----------+
  | You  |---->| Proxy |---->| Internet |
  +------+     +-------+     +----------+
  Internet sees the proxy's IP, not yours.
  Example: VPN, corporate proxy, Jio proxy


Reverse Proxy (like receptionist):
  The servers hide behind the proxy. You do not know which server you talk to.

  +----------+     +---------+     +---------+
  | Internet |---->| Reverse |---->| Server1 |
  |  (You)   |     | Proxy   |---->| Server2 |
  +----------+     +---------+     | Server3 |
  You see the proxy's address. Servers are hidden.   +---------+
  Example: Nginx, HAProxy, Cloudflare
```

**Indian analogy:** Think of a BPO call center. When you call Airtel customer
care, you talk to a receptionist (IVR/reverse proxy) who routes your call to
the RIGHT agent. You never dial the agent directly. You do not know how many
agents are working, or which one you got. The receptionist handles everything.

### Why It Exists

Without a reverse proxy, every server is exposed directly to the internet.
Problems:
1. DDoS attacks hit your servers directly
2. Each server needs its own SSL certificate
3. No easy way to distribute traffic across servers
4. No caching layer — every request hits the backend
5. Client needs to know about every server

### What a Reverse Proxy Does

```
+------------------------------------------------------------------+
|                     REVERSE PROXY                                |
|                                                                  |
|  +------------------+  +------------------+  +-----------------+ |
|  | SSL Termination  |  | Load Balancing   |  | Caching         | |
|  |                  |  |                  |  |                 | |
|  | Handles HTTPS    |  | Distributes      |  | Stores static   | |
|  | encryption/      |  | requests across  |  | responses,      | |
|  | decryption so    |  | multiple backend |  | serves them     | |
|  | backends don't   |  | servers          |  | without hitting  | |
|  | have to          |  |                  |  | backend         | |
|  +------------------+  +------------------+  +-----------------+ |
|                                                                  |
|  +------------------+  +------------------+  +-----------------+ |
|  | Compression      |  | Rate Limiting    |  | Security        | |
|  |                  |  |                  |  |                 | |
|  | gzip/brotli      |  | Block abusive    |  | Hide server     | |
|  | compresses       |  | clients, limit   |  | details, add    | |
|  | responses to     |  | requests per     |  | security        | |
|  | save bandwidth   |  | second           |  | headers, WAF    | |
|  +------------------+  +------------------+  +-----------------+ |
+------------------------------------------------------------------+
```

### How It Works — Request Flow

```
  Client (Browser)
       |
       | HTTPS request to api.zomato.com
       v
  +------------------+
  |   NGINX          |
  |   (Reverse Proxy)|
  +------------------+
       |
       | 1. Terminates SSL (decrypts HTTPS to HTTP)
       | 2. Checks cache — if cached, return immediately
       | 3. If not cached, pick a backend server:
       |
       |--- Round Robin ----+-------+-------+
       |                    |       |       |
       v                    v       v       v
  +---------+         +---------+ +---------+
  | Server1 |         | Server2 | | Server3 |
  | :8080   |         | :8080   | | :8080   |
  +---------+         +---------+ +---------+
       |
       | 4. Backend processes request, returns response
       | 5. Nginx compresses response (gzip)
       | 6. Nginx encrypts response (HTTPS)
       | 7. Nginx caches response (if cacheable)
       | 8. Sends back to client
       v
  Client gets the response. Never knew about Server1/2/3.
```

### Nginx Configuration Example (Simplified)

```nginx
# All traffic comes to port 443 (HTTPS)
server {
    listen 443 ssl;
    server_name api.zomato.com;

    ssl_certificate     /etc/ssl/zomato.crt;
    ssl_certificate_key /etc/ssl/zomato.key;

    # Enable compression
    gzip on;
    gzip_types application/json text/html;

    # Proxy to backend servers
    location /api/ {
        proxy_pass http://backend_servers;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
    }

    # Cache static files
    location /static/ {
        proxy_pass http://backend_servers;
        proxy_cache my_cache;
        proxy_cache_valid 200 1h;
    }
}

# Define the pool of backend servers
upstream backend_servers {
    server 10.0.1.1:8080;
    server 10.0.1.2:8080;
    server 10.0.1.3:8080;
}
```

### Reverse Proxy vs Load Balancer vs API Gateway

```
+-------------------+-------------------+-------------------+-------------------+
| Feature           | Reverse Proxy     | Load Balancer     | API Gateway       |
+-------------------+-------------------+-------------------+-------------------+
| Primary job       | Sit in front of   | Distribute        | Manage API        |
|                   | servers, handle   | traffic evenly    | traffic: auth,    |
|                   | everything        | across servers    | rate limit,       |
|                   |                   |                   | transform, route  |
+-------------------+-------------------+-------------------+-------------------+
| SSL termination   | YES               | Sometimes         | YES               |
+-------------------+-------------------+-------------------+-------------------+
| Load balancing    | YES               | YES (that's its   | Sometimes         |
|                   |                   | whole job)        |                   |
+-------------------+-------------------+-------------------+-------------------+
| Caching           | YES               | NO                | Sometimes         |
+-------------------+-------------------+-------------------+-------------------+
| Auth/AuthZ        | Basic             | NO                | YES (core feature)|
+-------------------+-------------------+-------------------+-------------------+
| Rate limiting     | YES               | NO                | YES               |
+-------------------+-------------------+-------------------+-------------------+
| Request transform | Basic             | NO                | YES (header       |
|                   |                   |                   | rewrite, body     |
|                   |                   |                   | transform)        |
+-------------------+-------------------+-------------------+-------------------+
| Example           | Nginx, HAProxy    | AWS ALB/NLB,      | Kong, AWS API     |
|                   |                   | HAProxy           | Gateway, Apigee   |
+-------------------+-------------------+-------------------+-------------------+
| Analogy           | Office            | Traffic police at  | Toll booth on     |
|                   | receptionist      | a junction —       | a highway —       |
|                   | — handles         | directs cars to    | checks tag,       |
|                   | everything        | different lanes    | charges toll,     |
|                   |                   |                    | opens gate        |
+-------------------+-------------------+-------------------+-------------------+
```

**Key insight:** In practice, these overlap heavily. Nginx is both a reverse
proxy AND a load balancer. Kong is both an API gateway AND a reverse proxy.
The terms describe the PRIMARY role, not exclusive capabilities.

### Companies Using It

| Company       | Tool               | Usage                           |
|---------------|--------------------|---------------------------------|
| Netflix       | Zuul / Nginx       | Edge proxy for all traffic      |
| Cloudflare    | Their own          | CDN + reverse proxy for millions|
| Uber          | Nginx              | Route to microservices          |
| Flipkart      | Nginx + HAProxy    | SSL termination + load balance  |
| Almost every  | Nginx or Traefik   | Default choice for web apps     |
| startup       |                    |                                 |

### When to Use in System Design

- ALWAYS. Every production system should have a reverse proxy.
- When you say "traffic comes in" → draw a reverse proxy in front of servers
- When you need SSL → reverse proxy handles it
- When you need to scale backends → reverse proxy distributes traffic

---
---

## 3. Distributed Transactions

### What Is It (Plain English)

A distributed transaction is when a single business operation spans
multiple services or databases, and ALL of them must succeed or ALL
must roll back. There is no "half done."

### Real-Life Analogy — The Wedding Gift Exchange

```
Imagine two families exchanging wedding gifts simultaneously:

Family A gives gold to Family B
Family B gives property papers to Family A

If Family A gives gold but Family B doesn't give property...
that's INCONSISTENCY. Both must happen, or neither.

In software:
- Service A: Deduct payment   (Rs 5000 debited)
- Service B: Update inventory  (1 item removed)
- Service C: Create order      (order confirmed)

If payment succeeds but inventory fails...
customer is charged but gets nothing! DISASTER.
```

### Why It Exists

In a monolith with a single database, you use database transactions:
```sql
BEGIN TRANSACTION;
  UPDATE accounts SET balance = balance - 5000 WHERE user_id = 123;
  UPDATE inventory SET quantity = quantity - 1 WHERE product_id = 456;
  INSERT INTO orders (user_id, product_id, status) VALUES (123, 456, 'confirmed');
COMMIT;
```
If any step fails, the whole thing rolls back. Simple.

But in microservices, each service has its OWN database:

```
  +------------------+     +------------------+     +------------------+
  | Payment Service  |     | Inventory Service|     | Order Service    |
  | (PostgreSQL)     |     | (MongoDB)        |     | (MySQL)          |
  +------------------+     +------------------+     +------------------+

  Three databases. No single "BEGIN TRANSACTION" that spans all three.
```

Now, how do you ensure ALL three succeed or ALL three roll back?

### Approach 1: Two-Phase Commit (2PC)

**How it works:** A coordinator asks all services: "Can you commit?"
If ALL say yes, coordinator says "Commit!" If ANY says no, coordinator
says "Abort!"

**Analogy:** Think of a group lunch order at office. The team lead (coordinator)
asks everyone: "Biryani theek hai?" Phase 1: everyone says yes. Phase 2:
team lead places the order. If even ONE person says "nahi, I'm fasting" in
Phase 1, the order is cancelled for everyone.

```
  +---------------+
  | Coordinator   |
  | (Transaction  |
  |  Manager)     |
  +---------------+
        |
        | PHASE 1: PREPARE (Can you commit?)
        |
        +----------+-----------+
        |          |           |
        v          v           v
  +---------+ +---------+ +---------+
  | Payment | |Inventory| | Order   |
  | Service | | Service | | Service |
  +---------+ +---------+ +---------+
        |          |           |
        |  YES     |  YES      | YES
        +----------+-----------+
        |
        v
  +---------------+
  | Coordinator   |
  +---------------+
        |
        | PHASE 2: COMMIT (Everyone said yes, go ahead!)
        |
        +----------+-----------+
        |          |           |
        v          v           v
  +---------+ +---------+ +---------+
  | COMMIT  | | COMMIT  | | COMMIT  |
  +---------+ +---------+ +---------+

  If ANY service said NO in Phase 1:

  PHASE 2: ABORT (Roll back everything!)
        +----------+-----------+
        |          |           |
        v          v           v
  +---------+ +---------+ +---------+
  | ROLLBACK| | ROLLBACK| | ROLLBACK|
  +---------+ +---------+ +---------+
```

**The Blocking Problem with 2PC:**

```
What if the coordinator CRASHES between Phase 1 and Phase 2?

  Phase 1: All services said YES, they are holding LOCKS on their data.
  Coordinator crashes. Never sends Phase 2.

  Result: All services are STUCK. Data is LOCKED. Nothing can proceed.
  Other transactions that need the same data are BLOCKED.

  This is why 2PC is rarely used in modern microservices.
```

### Approach 2: Saga Pattern

Instead of one big transaction, break it into a CHAIN of local transactions.
Each service does its own transaction. If a later step fails, you run
COMPENSATING TRANSACTIONS to undo the earlier steps.

**Analogy:** Think of a Flipkart order:

```
  Step 1: Payment debited (Rs 5000)
  Step 2: Inventory reserved (1 iPhone)
  Step 3: Shipping arranged

  If Step 3 fails (no delivery partner available):
    Compensate Step 2: Release inventory (iPhone back in stock)
    Compensate Step 1: Refund payment (Rs 5000 credited back)

  It's like cancelling a partially-done IRCTC booking:
    Seat reserved → payment debited → but e-ticket generation fails
    → payment refunded → seat released
```

#### Choreography vs Orchestration

Two ways to coordinate a saga:

**Choreography** — Each service listens for events and reacts.
No central coordinator. Like a flash mob where each dancer knows their cue.

```
  Payment          Inventory          Order            Shipping
  Service          Service            Service          Service
     |                |                  |                |
     | PaymentDebited |                  |                |
     |--------------->|                  |                |
     |                | InventoryReserved|                |
     |                |----------------->|                |
     |                |                  | OrderCreated   |
     |                |                  |--------------->|
     |                |                  |                |
     |        If Shipping fails:         |                |
     |                |                  | ShippingFailed |
     |                |                  |<---------------|
     |                | InventoryRelease |                |
     |                |<-----------------|                |
     | PaymentRefund  |                  |                |
     |<---------------|                  |                |
```

**Orchestration** — A central Saga Orchestrator tells each service what to do.
Like a movie director giving instructions to actors.

```
  +--------------------+
  | Saga Orchestrator  |
  | (Order Saga)       |
  +--------------------+
       |           |           |           |
       | 1.Debit   |           |           |
       |---------->|           |           |
       |           |           |           |
       |  OK       | 2.Reserve |           |
       |<----------|---------->|           |
       |           |           |           |
       |           |  OK       | 3.Ship    |
       |           |<----------|---------->|
       |           |           |           |
       |           |           |  FAIL!    |
       |           |           |<----------|
       |           |           |           |
       | Orchestrator detects failure.     |
       | Triggers compensating actions:    |
       |           |           |           |
       |           | 4.Release |           |
       |           |---------->|           |
       |           |           |           |
       | 5.Refund  |           |           |
       |---------->|           |           |
```

#### Choreography vs Orchestration Comparison

```
+-------------------+------------------------+------------------------+
| Aspect            | Choreography           | Orchestration          |
+-------------------+------------------------+------------------------+
| Coordinator       | NONE — services react  | Central orchestrator   |
|                   | to events              | controls the flow      |
+-------------------+------------------------+------------------------+
| Coupling          | Loose — services only  | Tighter — orchestrator |
|                   | know about events      | knows all services     |
+-------------------+------------------------+------------------------+
| Complexity        | Simple for 2-3 steps   | Better for 5+ steps    |
|                   | messy for 10+ steps    |                        |
+-------------------+------------------------+------------------------+
| Debugging         | Hard — trace events    | Easy — one place       |
|                   | across services        | to see the whole flow  |
+-------------------+------------------------+------------------------+
| Single point      | NO                     | YES (orchestrator)     |
| of failure        |                        |                        |
+-------------------+------------------------+------------------------+
| Analogy           | Flash mob dancers      | Movie director +       |
|                   | (each knows cues)      | actors (director says  |
|                   |                        | "action!")             |
+-------------------+------------------------+------------------------+
| Real example      | Simple e-commerce:     | Flipkart order:        |
|                   | order → payment →      | payment → inventory →  |
|                   | notification           | shipping → delivery →  |
|                   |                        | notification → loyalty |
+-------------------+------------------------+------------------------+
```

### 2PC vs Saga — When to Use

```
+-------------------+----------------------------+----------------------------+
| Aspect            | Two-Phase Commit (2PC)     | Saga Pattern               |
+-------------------+----------------------------+----------------------------+
| Consistency       | STRONG (all-or-nothing at  | EVENTUAL (temporary        |
|                   | the exact same moment)     | inconsistency possible)    |
+-------------------+----------------------------+----------------------------+
| Availability      | LOW (blocking when         | HIGH (no locks held        |
|                   | coordinator is down)       | across services)           |
+-------------------+----------------------------+----------------------------+
| Scalability       | POOR (locks held across    | GOOD (each service acts    |
|                   | distributed systems)       | independently)             |
+-------------------+----------------------------+----------------------------+
| Complexity        | Simpler logic, but needs   | More complex (compensating |
|                   | transaction manager        | transactions required)     |
+-------------------+----------------------------+----------------------------+
| Use case          | Banking (transfer between  | E-commerce orders, food    |
|                   | accounts in same bank)     | delivery, travel booking   |
+-------------------+----------------------------+----------------------------+
```

### Companies Using It

| Company   | Approach     | Use Case                                  |
|-----------|--------------|-------------------------------------------|
| Flipkart  | Saga (Orch.) | Order flow: payment → inventory → shipping|
| Uber      | Saga (Orch.) | Trip: match driver → start ride → payment |
| Swiggy    | Saga (Orch.) | Order → restaurant confirm → delivery     |
| Banks     | 2PC          | Interbank NEFT/RTGS transfers             |
| Amazon    | Saga         | Order processing across 100+ services     |

---
---

## 4. Leader Election & Consensus

### What Is It (Plain English)

When you have 5 replicas of a database or service, who decides what the
"truth" is? If all 5 can write independently, you get conflicts. So you
elect ONE node as the LEADER. Only the leader writes. Others are followers
that replicate the leader's data.

But what happens when the leader CRASHES? You need to elect a new leader.
That is leader election.

### Real-Life Analogy — Class Monitor Election

```
A school class of 50 students needs a monitor.

1. Teacher says "I'm not choosing. You guys elect someone."
2. Any student can say "I want to be monitor" (CANDIDATE)
3. Students vote. Majority wins.
4. Elected student is the MONITOR (LEADER)
5. Other students follow the monitor's instructions (FOLLOWERS)

Now the monitor falls sick and doesn't come to school.
6. Students notice the monitor is absent (HEARTBEAT TIMEOUT)
7. Someone else says "I'll be the monitor" (new election)
8. New election, new monitor.

Same thing happens with servers.
```

### Why It Exists

Without leader election:

```
  Client A writes "price = 100" to Server 1
  Client B writes "price = 200" to Server 2
  (at the same time)

  Server 1 thinks price = 100
  Server 2 thinks price = 200

  CONFLICT! Which one is correct?
  This is the "split-brain" problem.
```

With leader election:

```
  ALL writes go to the Leader.
  Leader decides the order.
  Followers replicate in that order.
  Everyone agrees. No conflicts.
```

### How It Works — The Raft Consensus Algorithm

Raft is the most widely-used consensus algorithm today. It is designed to
be UNDERSTANDABLE (unlike Paxos, which is famously hard to understand).

#### Node States

Every node in a Raft cluster is in one of three states:

```
  +-------------------------------------------+
  |                                           |
  |    +----------+                           |
  |    | FOLLOWER |  (default starting state) |
  |    +----------+                           |
  |         |                                 |
  |         | Election timeout                |
  |         | (no heartbeat from leader)      |
  |         v                                 |
  |    +-----------+                          |
  |    | CANDIDATE |  (requests votes)        |
  |    +-----------+                          |
  |         |                                 |
  |         | Gets majority votes             |
  |         v                                 |
  |    +--------+                             |
  |    | LEADER |  (sends heartbeats,         |
  |    +--------+   handles all writes)       |
  |                                           |
  +-------------------------------------------+
```

#### The Election Process

```
  Node A        Node B        Node C        Node D        Node E
  (Follower)    (Follower)    (Follower)    (Follower)    (Follower)
     |             |             |             |             |
     |   ... Leader (whoever it was) crashes or is unreachable ...
     |             |             |             |             |
     | Election    |             |             |             |
     | timeout!    |             |             |             |
     |             |             |             |             |
     | I become    |             |             |             |
     | CANDIDATE   |             |             |             |
     | Term = 2    |             |             |             |
     |             |             |             |             |
     |--Vote for me? ---------->|             |             |
     |--Vote for me? ----->     |             |             |
     |--Vote for me? --------------------------------->     |
     |--Vote for me? ------------------------->             |
     |             |             |             |             |
     |<---YES------|             |             |             |
     |<----------YES------------|             |             |
     |<---------------------------YES---------|             |
     |             |             |       (Node E was slow)  |
     |             |             |             |             |
     | Got 3 votes (out of 5)   |             |             |
     | MAJORITY! I am LEADER!   |             |             |
     |             |             |             |             |
     |--Heartbeat->|             |             |             |
     |--Heartbeat-------------->|             |             |
     |--Heartbeat------------------------------>            |
     |--Heartbeat----------------------------->|            |
     |             |             |             |             |
     | (sends heartbeats every ~150ms to maintain leadership)
```

#### Key Raft Rules:

1. **Election timeout:** Each follower has a RANDOM timeout (e.g., 150-300ms).
   First one to timeout becomes a candidate. Randomness prevents everyone
   becoming candidate simultaneously.

2. **Term numbers:** Like election years. Each election increments the term.
   A node with a higher term number always wins over a lower term.

3. **Majority required:** In a cluster of N nodes, you need (N/2 + 1) votes.
   - 3 nodes → need 2 votes
   - 5 nodes → need 3 votes
   - 7 nodes → need 4 votes

4. **One vote per term:** Each node can only vote for ONE candidate per term.
   First come, first served.

5. **Heartbeats:** Leader sends heartbeats to all followers regularly.
   If followers do not get a heartbeat within the timeout, they start a new
   election.

#### Log Replication (How Writes Work After Election)

```
  Client          Leader         Follower B     Follower C
     |               |               |               |
     | Write x=5     |               |               |
     |-------------->|               |               |
     |               |               |               |
     |               | Append x=5    |               |
     |               | to MY log     |               |
     |               |               |               |
     |               | Replicate:    |               |
     |               | "Append x=5"  |               |
     |               |-------------->|               |
     |               |------------------------------>|
     |               |               |               |
     |               |   ACK         |               |
     |               |<--------------|               |
     |               |<------------------------------|
     |               |               |               |
     |               | MAJORITY acknowledged (2 of 3)|
     |               | COMMIT x=5   |               |
     |               |               |               |
     | Success!      |               |               |
     |<--------------|               |               |
```

### ZooKeeper

Apache ZooKeeper is a centralized service for:
- Leader election
- Configuration management
- Service discovery
- Distributed locking

```
  +-----------------------------------------------------+
  |                    ZooKeeper Ensemble                |
  |                                                     |
  |  +--------+     +--------+     +--------+          |
  |  | ZK     |<--->| ZK     |<--->| ZK     |          |
  |  | Node 1 |     | Node 2 |     | Node 3 |          |
  |  | (Leader|     |(Follower)    |(Follower)          |
  |  +--------+     +--------+     +--------+          |
  +-----------------------------------------------------+
        |                                   |
        v                                   v
  +------------+                     +------------+
  | Kafka      |                     | Kafka      |
  | Broker 1   |                     | Broker 2   |
  | "Am I the  |                     | "Who is    |
  |  leader?"  |                     |  leader?"  |
  +------------+                     +------------+
```

ZooKeeper uses a protocol called ZAB (ZooKeeper Atomic Broadcast), which
is similar to Raft. Kafka used ZooKeeper for years for broker leader
election, until KRaft (Kafka Raft) was introduced to remove ZooKeeper
dependency.

### Companies Using It

| System/Company | Algorithm/Tool      | Usage                              |
|----------------|---------------------|------------------------------------|
| Kafka          | ZAB → KRaft         | Partition leader election           |
| etcd           | Raft                | Kubernetes control plane state      |
| MongoDB        | Raft (since 4.x)   | Replica set primary election        |
| CockroachDB    | Raft                | Range leader election               |
| Consul         | Raft                | Service discovery + leader election |
| Redis Sentinel | Custom protocol     | Redis primary failover              |

### When to Use in System Design

- "How do you handle failover?" → Leader election with Raft/ZooKeeper
- "What happens when the primary database node goes down?" → Automatic election
- "How do distributed locks work?" → ZooKeeper/etcd ephemeral nodes
- "How does Kafka know which broker handles a partition?" → Leader election

---
---

## 5. Bloom Filters

### What Is It (Plain English)

A Bloom filter is a space-efficient data structure that tells you:
- "This element is DEFINITELY NOT in the set" — 100% accurate
- "This element is PROBABLY in the set" — might be wrong (false positive)

It NEVER says "this is NOT in the set" when it actually IS (no false negatives).

### Real-Life Analogy — The Bouncer at a Club

```
You go to a club. The bouncer has a rough memory.

If the bouncer says "You are NOT on the guest list" → you are DEFINITELY not.
    The bouncer is 100% sure about rejections.

If the bouncer says "Yeah, I think you are on the list" → you MIGHT be.
    The bouncer might be confusing you with someone else.

The bouncer NEVER accidentally kicks out a VIP (no false negatives).
But he MIGHT let in a non-VIP by mistake (false positives OK).
```

### Why It Exists

Problem: You have 1 BILLION usernames in your database. A new user wants
to sign up. You need to check "is this username taken?"

**Approach 1: Query the database**
- Works but slow. 1 billion rows. Even with an index, still a disk I/O.
- If 100 users/second are checking usernames, that is 100 DB queries/second
  just for availability checks.

**Approach 2: Load all usernames in a HashSet in memory**
- Fast! O(1) lookup. But...
- 1 billion usernames × ~20 bytes each = ~20 GB of RAM. Expensive.

**Approach 3: Bloom filter**
- 1 billion elements with 1% false positive rate = ~1.2 GB of RAM
- 10x less memory than a HashSet
- O(1) lookup
- If Bloom filter says "not taken" → definitely available, skip DB
- If Bloom filter says "probably taken" → check DB to confirm (1% false positive)
- Result: 99% of checks never hit the database!

### How It Works

#### The Data Structure

A Bloom filter is:
1. A BIT ARRAY of m bits (all initialized to 0)
2. k different hash functions

```
Bit array (m = 16 bits, all zeros initially):
+---+---+---+---+---+---+---+---+---+---+---+---+---+---+---+---+
| 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 |
+---+---+---+---+---+---+---+---+---+---+---+---+---+---+---+---+
  0   1   2   3   4   5   6   7   8   9  10  11  12  13  14  15
```

#### Adding an Element

To add "sheetal" to the Bloom filter (using k=3 hash functions):

```
hash1("sheetal") % 16 = 3
hash2("sheetal") % 16 = 7
hash3("sheetal") % 16 = 11

Set bits 3, 7, 11 to 1:

+---+---+---+---+---+---+---+---+---+---+---+---+---+---+---+---+
| 0 | 0 | 0 | 1 | 0 | 0 | 0 | 1 | 0 | 0 | 0 | 1 | 0 | 0 | 0 | 0 |
+---+---+---+---+---+---+---+---+---+---+---+---+---+---+---+---+
  0   1   2   3   4   5   6   7   8   9  10  11  12  13  14  15
                ^               ^               ^
```

Now add "rahul":

```
hash1("rahul") % 16 = 1
hash2("rahul") % 16 = 7    (collision with sheetal!)
hash3("rahul") % 16 = 14

Set bits 1, 7, 14 to 1:

+---+---+---+---+---+---+---+---+---+---+---+---+---+---+---+---+
| 0 | 1 | 0 | 1 | 0 | 0 | 0 | 1 | 0 | 0 | 0 | 1 | 0 | 0 | 1 | 0 |
+---+---+---+---+---+---+---+---+---+---+---+---+---+---+---+---+
  0   1   2   3   4   5   6   7   8   9  10  11  12  13  14  15
      ^       ^               ^               ^           ^
```

#### Checking an Element

To check if "priya" is in the set:

```
hash1("priya") % 16 = 3
hash2("priya") % 16 = 14
hash3("priya") % 16 = 9

Check bits 3, 14, 9:
  Bit 3  = 1  (set by sheetal)
  Bit 14 = 1  (set by rahul)
  Bit 9  = 0  ← NOT SET!

Since bit 9 is 0, "priya" is DEFINITELY NOT in the set.
(Even one 0 means definitely not present)
```

To check if "amit" is in the set:

```
hash1("amit") % 16 = 1
hash2("amit") % 16 = 3
hash3("amit") % 16 = 7

Check bits 1, 3, 7:
  Bit 1 = 1  (set by rahul)
  Bit 3 = 1  (set by sheetal)
  Bit 7 = 1  (set by both sheetal and rahul)

All bits are 1! Bloom filter says "PROBABLY YES."

But "amit" was NEVER added! This is a FALSE POSITIVE.
The bits were set by OTHER elements. This is the trade-off.
```

#### Visualization of the Full Process

```
           ADD "sheetal"                    ADD "rahul"
           hash → 3, 7, 11                 hash → 1, 7, 14

           +---+---+---+---+               +---+---+---+---+
Before:    |000000000000000|     After:     |010100010001010|
           +---+---+---+---+               +---+---+---+---+


           CHECK "priya"                    CHECK "amit"
           hash → 3, 14, 9                 hash → 1, 3, 7

           Bit 9 = 0                        All bits = 1
           DEFINITELY NOT IN SET            PROBABLY IN SET
                                            (false positive!)
```

### False Positive Rate

The false positive rate depends on:
- **m** = size of bit array (more bits = fewer collisions)
- **n** = number of elements added
- **k** = number of hash functions

```
Formula: FP rate ≈ (1 - e^(-kn/m))^k

Practical guidelines:
+------------------+------------------+-------------------+
| Elements (n)     | Bit array (m)    | FP Rate           |
+------------------+------------------+-------------------+
| 1 million        | 10 million bits  | ~0.82% (< 1%)    |
|                  | (1.2 MB)         |                   |
+------------------+------------------+-------------------+
| 1 billion        | 10 billion bits  | ~0.82% (< 1%)    |
|                  | (1.2 GB)         |                   |
+------------------+------------------+-------------------+

Compare: HashSet for 1 billion items = ~20 GB
         Bloom filter = ~1.2 GB (17x smaller!)
```

### Companies Using It

| Company       | Use Case                                                |
|---------------|---------------------------------------------------------|
| Google Chrome | Safe Browsing: check if a URL is malicious              |
| Akamai/CDNs  | "Is this object in cache?" — avoid disk lookup          |
| Cassandra     | "Is this key in this SSTable?" — skip reading the file  |
| Medium        | "Has this user already seen this article?" — deduplicate|
| Bitcoin       | SPV nodes filter transactions without full blockchain   |
| HBase         | Skip SSTables that definitely don't have the key        |

### When to Use in System Design

- "How to check username availability without hitting DB every time?" → Bloom filter
- "How to avoid unnecessary cache/disk lookups?" → Bloom filter as first check
- "How to deduplicate events in a stream?" → Bloom filter
- "How to check if a URL is malicious?" → Bloom filter with known-bad URLs

**Remember the golden rule:**
- False positive = "probably yes" (might be wrong, but OK — we just check DB)
- False negative = "definitely no" (NEVER wrong — if it says no, it means no)

---
---

## 6. Geospatial Indexing

### What Is It (Plain English)

You open Uber. The app needs to find all available drivers within 3 km
of your location. There are 10 million drivers in India. How do you
efficiently find the ones near you WITHOUT checking all 10 million?

Geospatial indexing organizes location data so that "find things near X"
is fast — O(log n) instead of O(n).

### Real-Life Analogy — Finding a Doctor in Your PIN Code

```
Without indexing:
  "Find doctors near me"
  → Check ALL 5 lakh doctors in India
  → Calculate distance to each one
  → Filter by < 3 km
  → Took 10 seconds. Terrible.

With PIN code (like geospatial indexing):
  Your PIN code: 400001 (South Mumbai)
  → Only look at doctors in 400001 and adjacent PINs
  → Check maybe 200 doctors instead of 5 lakh
  → Filter by < 3 km
  → Took 5 milliseconds.
```

A geohash is essentially a "PIN code for the entire world" but much more
precise, and the codes of nearby places SHARE A PREFIX (like adjacent PINs).

### Approach 1: Geohash

#### How Geohash Works

A geohash divides the world into a grid. Each cell gets a code. Longer
codes = more precise cells.

```
Step 1: Divide the world into 32 cells (1 character)

  +---+---+---+---+---+---+---+---+
  | 0 | 1 | 2 | 3 | 4 | 5 | 6 | 7 |
  +---+---+---+---+---+---+---+---+
  | 8 | 9 | b | c | d | e | f | g |
  +---+---+---+---+---+---+---+---+
  | h | j | k | m | n | p | q | r |
  +---+---+---+---+---+---+---+---+
  | s | t | u | v | w | x | y | z |
  +---+---+---+---+---+---+---+---+

India falls in cells: t, u (approximately)


Step 2: Subdivide "t" into 32 smaller cells (2 characters)

  +-----+-----+-----+-----+-----+
  | t0  | t1  | t2  | t3  | t4  |
  +-----+-----+-----+-----+-----+
  | t5  | t6  | t7  | t8  | t9  |
  +-----+-----+-----+-----+-----+
  | ... | ... | ... | ... | ... |
  +-----+-----+-----+-----+-----+

Mumbai falls in cell: te (approximately)


Step 3: Keep subdividing...

  "te"  → covers all of Mumbai       (~630 km x 630 km)
  "te7" → covers South Mumbai area   (~78 km x 78 km)
  "te7q" → covers a few localities   (~20 km x 20 km)
  "te7q3" → covers a neighborhood    (~5 km x 5 km)
  "te7q3r" → covers a few streets    (~1.2 km x 0.6 km)
```

#### Key Property: Prefix Matching = Proximity

```
  Location A:  te7q3r8m  (Churchgate station)
  Location B:  te7q3r8n  (Marine Drive, 500m away)
  Location C:  te7q3r7x  (Nariman Point, 1km away)
  Location D:  te7q1abc  (Bandra, 15km away)

  Locations A and B share prefix "te7q3r8" → VERY close
  Locations A and C share prefix "te7q3r"  → close
  Locations A and D share prefix "te7q"    → same city, not nearby
```

To find drivers within 3 km of "te7q3r8m":
1. Look up all drivers whose geohash starts with "te7q3r" (same ~1.2 km cell)
2. Also check the 8 neighboring cells (because the user might be near the edge)
3. Calculate exact distance for the small number of results
4. Filter by distance < 3 km

```
  +--------+--------+--------+
  |te7q3r7 |te7q3r8 |te7q3r9 |   ← Check these 9 cells
  | nearby | YOU ARE| nearby |
  | cell   | HERE   | cell   |
  +--------+--------+--------+
  |te7q3r4 |te7q3r5 |te7q3r6 |
  | nearby | nearby | nearby |
  | cell   | cell   | cell   |
  +--------+--------+--------+
  |te7q3r1 |te7q3r2 |te7q3r3 |
  | nearby | nearby | nearby |
  | cell   | cell   | cell   |
  +--------+--------+--------+
```

**Database query:**
```sql
SELECT * FROM drivers
WHERE geohash LIKE 'te7q3r%'   -- prefix search!
AND status = 'available'
```

This is fast because geohash is just a string, and databases are excellent
at prefix searches using B-tree indexes.

### Approach 2: Quadtree

A quadtree recursively divides a 2D space into 4 quadrants. It keeps
subdividing areas that have many points, while leaving sparse areas as
large cells.

```
Start: The whole map is one cell with 15 drivers

  +---------------------------+
  |  .  .   .    .     .      |
  |     .     .    .          |
  |  .     .      .  .       |
  |     .    .      .        |
  +---------------------------+
  15 drivers — too many! Split into 4:


  +-------------+-------------+
  |  .  .   .   |   .     .   |
  |     .       |  .          |
  | NW (5)      | NE (4)      |
  +-------------+-------------+
  |  .     .    |  .  .       |
  |     .    .  |    .        |
  | SW (4)      | SE (2)      |
  +-------------+-------------+

  NW has 5 drivers. If threshold is 4, split NW again:

  +------+------+-------------+
  | . .  |  .   |   .     .   |
  |NW-NW |NW-NE |  .          |
  | (2)  | (1)  | NE (4)      |
  +------+------+             |
  |  .   |      |             |
  |NW-SW |NW-SE |             |
  | (1)  | (1)  |             |
  +------+------+-------------+
  |  .     .    |  .  .       |
  |     .    .  |    .        |
  | SW (4)      | SE (2)      |
  +-------------+-------------+

  Dense areas → deeply split (fine-grained)
  Sparse areas → not split (coarse)
```

#### Finding Nearby Drivers with Quadtree

```
  1. Start at the root node
  2. Navigate down to the leaf node containing the user's location
  3. Check all points in that leaf
  4. If the search radius extends beyond the leaf's boundary,
     check neighboring leaves too
  5. Calculate exact distances, filter by radius

  Time complexity: O(log n) average case
```

### Geohash vs Quadtree

```
+-------------------+---------------------------+---------------------------+
| Aspect            | Geohash                   | Quadtree                  |
+-------------------+---------------------------+---------------------------+
| Structure         | String-based grid          | Tree structure in memory  |
|                   | (stored in DB as string)   | (or specialized DB)       |
+-------------------+---------------------------+---------------------------+
| Storage           | Database (Redis, MySQL,    | In-memory or custom       |
|                   | PostgreSQL, MongoDB)       | data structure            |
+-------------------+---------------------------+---------------------------+
| Grid type         | UNIFORM grid (all cells    | ADAPTIVE grid (dense      |
|                   | same size at same level)   | areas get finer cells)    |
+-------------------+---------------------------+---------------------------+
| Boundary problem  | YES — nearby points can    | Less severe — tree        |
|                   | have different prefixes    | structure handles it      |
|                   | if on cell boundary        | naturally                 |
+-------------------+---------------------------+---------------------------+
| Ease of use       | Easy — just string prefix  | Harder — need tree        |
|                   | queries on any DB          | implementation            |
+-------------------+---------------------------+---------------------------+
| Updates (moving   | Update the geohash string  | Move point in the tree    |
| drivers)          | in DB. O(log n) with index | (might need rebalancing)  |
+-------------------+---------------------------+---------------------------+
| Best for          | "Find restaurants near me" | "Find all drivers in a    |
|                   | (relatively static data)   | dynamic area" (real-time) |
+-------------------+---------------------------+---------------------------+
```

### Companies Using It

| Company   | Approach         | Usage                                    |
|-----------|------------------|------------------------------------------|
| Uber      | Google S2 (cells)| Find nearby drivers, surge pricing zones |
| Ola       | Geohash + Redis  | Driver matching within radius            |
| Swiggy    | Geohash          | Find restaurants near delivery address   |
| Google    | S2 Geometry      | Google Maps, Places API                  |
| Pokémon Go| S2 Cells         | Spawn Pokémon in geographic cells        |
| MongoDB   | 2dsphere index   | Built-in geospatial queries (uses geohash)|
| Redis     | GEOADD / GEORADIUS| In-memory geospatial with sorted sets    |

### When to Use in System Design

- "Design Uber" → Geohash or Quadtree for driver matching
- "Design Yelp/Zomato" → Geohash for nearby restaurant search
- "Design Swiggy" → Geohash for delivery radius calculation
- "Find friends nearby" → Geohash prefix matching
- Moving objects (drivers, delivery agents) → Geohash in Redis for fast updates

---
---

## 7. Service Mesh

### What Is It (Plain English)

When you have 50+ microservices all talking to each other, managing
service-to-service communication becomes a nightmare. Who talks to whom?
Is the connection encrypted? What if Service A is slow — do we retry?
How do we trace a request across 10 services?

A service mesh is a dedicated infrastructure layer that handles all of
this networking logic, so your application code does not have to.

### Real-Life Analogy — Society Watchman and Intercom System

```
Imagine a large housing society with 200 flats (microservices).

WITHOUT a service mesh:
  Every flat owner personally manages their own security,
  decides who can visit, handles complaints directly.
  Chaos. Everyone does it differently. No standard rules.

WITH a service mesh:
  The society installs:
  - A WATCHMAN at every building entrance (sidecar proxy)
  - A central INTERCOM system (control plane)

  The watchman:
  - Checks visitor ID before allowing entry (mTLS)
  - Routes visitors to the right flat (service discovery)
  - Logs every visit (observability)
  - Turns away visitors if flat is busy (circuit breaking)
  - Knows society rules centrally (control plane)

  Flat owners just... live. They don't manage security.
  The watchman handles everything transparently.
```

### Why It Exists

Without a service mesh, every microservice must implement:

```
  +-----------------------------------+
  |  Your Application Code            |
  |                                   |
  |  + Retry logic                    |
  |  + Circuit breaker                |
  |  + Timeout handling               |
  |  + Load balancing                 |
  |  + mTLS certificate management    |
  |  + Service discovery              |
  |  + Distributed tracing            |
  |  + Rate limiting                  |
  |  + Access control between services|
  +-----------------------------------+

  Every. Single. Service. Repeats this.
  In every language (Java, Python, Go, Node.js...).
  Bug in retry logic? Fix it in ALL 50 services.
```

With a service mesh, all this moves to the INFRASTRUCTURE:

```
  +-----------------------------------+
  |  Your Application Code            |
  |                                   |
  |  (just business logic!)           |
  +-----------------------------------+

  +-----------------------------------+
  |  Sidecar Proxy (Envoy)            |
  |                                   |
  |  Handles: retry, circuit breaker, |
  |  mTLS, tracing, load balancing,   |
  |  rate limiting, access control    |
  +-----------------------------------+
```

### How It Works — Sidecar Pattern

Every microservice gets a "sidecar" proxy deployed alongside it. The
sidecar intercepts ALL incoming and outgoing network traffic.

```
  Pod A                              Pod B
  +---------------------------+      +---------------------------+
  |  +--------+  +--------+  |      |  +--------+  +--------+  |
  |  | Order  |  | Envoy  |  |      |  | Envoy  |  |Payment |  |
  |  |Service |->| Proxy  |--|----->|--| Proxy  |->|Service |  |
  |  |        |  |(sidecar)|  |      |  |(sidecar)|  |        |  |
  |  +--------+  +--------+  |      |  +--------+  +--------+  |
  +---------------------------+      +---------------------------+
                    ^                          ^
                    |                          |
                    +------+------+------------+
                           |
                    +------v------+
                    | Istio       |
                    | Control     |
                    | Plane       |
                    | (istiod)    |
                    +-------------+
                    Pushes config, certificates,
                    routing rules to ALL sidecars
```

#### What the Sidecar Does:

```
  Outgoing request from Order Service to Payment Service:

  Order Service → [Envoy Sidecar A]
                       |
                       | 1. Encrypts with mTLS (mutual TLS)
                       | 2. Looks up Payment Service location (service discovery)
                       | 3. Picks one instance (load balancing)
                       | 4. Adds tracing headers (distributed tracing)
                       | 5. Applies timeout (e.g., 5 seconds)
                       | 6. If fails, retries (with backoff)
                       | 7. If too many failures, opens circuit breaker
                       |
                       v
                  [Envoy Sidecar B] → Payment Service
                       |
                       | 1. Decrypts mTLS
                       | 2. Checks access policy (is Order Service allowed?)
                       | 3. Rate limits if needed
                       | 4. Forwards to Payment Service
```

### Key Components

```
+-------------------+--------------------------------------+
| Component         | Role                                 |
+-------------------+--------------------------------------+
| Data Plane        | The actual sidecar proxies (Envoy)   |
| (Envoy proxies)   | that handle traffic. One per service.|
+-------------------+--------------------------------------+
| Control Plane     | Central management. Pushes config,   |
| (Istio/Linkerd)   | policies, and certificates to all    |
|                   | sidecars. Brain of the mesh.         |
+-------------------+--------------------------------------+
```

### What a Service Mesh Handles

```
+-----------------------+------------------------------------------+
| Capability            | How It Works                             |
+-----------------------+------------------------------------------+
| mTLS (mutual TLS)     | Every service-to-service call is         |
|                       | encrypted. Both sides verify identity.   |
|                       | Certificates auto-rotated by control     |
|                       | plane. Zero code changes needed.         |
+-----------------------+------------------------------------------+
| Traffic Management    | Canary deployments: send 5% traffic to   |
|                       | new version. A/B testing. Blue/green.    |
|                       | Retry with exponential backoff.          |
+-----------------------+------------------------------------------+
| Observability         | Automatic distributed tracing (Jaeger),  |
|                       | metrics (Prometheus), logging. You see   |
|                       | exactly which service called which, how  |
|                       | long each call took, where it failed.    |
+-----------------------+------------------------------------------+
| Circuit Breaking      | If Payment Service is failing, stop      |
|                       | sending requests to it (fail fast)       |
|                       | instead of piling up and cascading.      |
+-----------------------+------------------------------------------+
| Access Control        | "Order Service can call Payment Service, |
|                       | but NOT Database Service directly."      |
|                       | Enforced at the mesh level.             |
+-----------------------+------------------------------------------+
| Rate Limiting         | Limit requests per service per second.   |
|                       | Prevent one service from overwhelming    |
|                       | another.                                 |
+-----------------------+------------------------------------------+
```

### Popular Service Mesh Implementations

```
+----------+----------------+----------------------------------+
| Mesh     | Sidecar Proxy  | Notes                            |
+----------+----------------+----------------------------------+
| Istio    | Envoy          | Most popular. Feature-rich.      |
|          |                | Can be complex to operate.       |
+----------+----------------+----------------------------------+
| Linkerd  | linkerd2-proxy | Simpler, lighter. Written in     |
|          | (Rust)         | Rust. Less features than Istio.  |
+----------+----------------+----------------------------------+
| Consul   | Built-in or    | HashiCorp. Also does service     |
| Connect  | Envoy          | discovery and KV store.          |
+----------+----------------+----------------------------------+
```

### Companies Using It

| Company       | Service Mesh | Why                                     |
|---------------|-------------|-----------------------------------------|
| Google        | Istio (created it) | Manages 1000s of microservices   |
| Lyft          | Envoy (created it) | Built the sidecar proxy          |
| Airbnb        | Envoy/Istio  | Service-to-service security             |
| eBay          | Custom mesh  | Traffic management at scale             |
| Salesforce    | Istio        | Multi-cluster service communication     |

### When to Use in System Design

- "How do you handle service-to-service communication at scale?" → Service mesh
- "How do you secure inter-service traffic?" → mTLS via service mesh
- "How do you do canary deployments?" → Service mesh traffic splitting
- "How do you trace a request across 15 services?" → Service mesh observability

**When NOT to use:** If you have < 10 microservices, a service mesh is
overkill. Simple HTTP clients with retry libraries are sufficient.

---
---

## 8. Data Lake vs Data Warehouse

### What Is It (Plain English)

Both store large amounts of data. The difference is HOW the data is stored
and WHO uses it.

- **Data Warehouse** = An organized library. Books are catalogued, sorted,
  and shelved properly. You can find exactly what you need quickly. But you
  can only store books that fit the catalogue system.

- **Data Lake** = A storage godown (warehouse). You dump everything —
  books, furniture, electronics, old clothes — in no particular order.
  It holds EVERYTHING, but finding something specific takes more effort.

### Real-Life Analogy — Bookshelf vs Storeroom

```
Data Warehouse (Organized Bookshelf):
  +---+---+---+---+---+---+
  | A | B | C | D | E | F |  ← Organized by category
  +---+---+---+---+---+---+
  Only cleaned, structured data.
  Ask: "Total sales in Mumbai in Q3 2024?"
  Answer in 2 seconds. Fast, structured queries.


Data Lake (Storeroom):
  +----------------------------------+
  | [PDF] [CSV] [JSON] [video]       |
  | [log files] [images] [raw data]  |
  | [Parquet] [tweets] [sensor data] |
  +----------------------------------+
  Everything dumped in raw form.
  Useful when you don't know what you need yet.
  Data scientists explore and build ML models.
```

### Why They Exist

```
Traditional DB: Good for running the app (OLTP — transactions)
  "Insert this order", "Update this user", "Show cart items"
  → Row by row operations, fast for individual records

Data Warehouse: Good for analyzing the business (OLAP — analytics)
  "Total revenue by city for last 6 months"
  "Customer retention rate by age group"
  → Aggregate queries across millions of rows

Data Lake: Good for storing everything cheaply
  "We have 50TB of log files. We don't know what's useful yet."
  "Store raw data now, analyze later"
  → Cheap storage, flexible schema
```

### How They Work — Architecture

#### Data Warehouse Architecture

```
  +----------+    +----------+    +----------+
  | App DB   |    | Payment  |    | CRM      |
  | (MySQL)  |    | (Stripe) |    | (Salesforce)|
  +----------+    +----------+    +----------+
       |               |               |
       v               v               v
  +-----------------------------------------+
  |           ETL Pipeline                  |
  |  Extract → Transform → Load            |
  |  (clean, deduplicate, structure)        |
  +-----------------------------------------+
       |
       v
  +-----------------------------------------+
  |        DATA WAREHOUSE                   |
  |                                         |
  |  +--------+  +--------+  +--------+    |
  |  | sales  |  | users  |  | orders |    |
  |  | facts  |  | dim    |  | facts  |    |
  |  +--------+  +--------+  +--------+    |
  |                                         |
  |  STRUCTURED. SCHEMA-ON-WRITE.           |
  |  Data is cleaned BEFORE loading.        |
  |  Columnar storage for fast aggregation. |
  +-----------------------------------------+
       |
       v
  +-----------------+
  | BI Tools        |
  | (Tableau,       |
  |  Power BI,      |
  |  Looker)        |
  +-----------------+
```

#### Data Lake Architecture

```
  +----------+    +----------+    +----------+    +----------+
  | App DB   |    | Log Files|    | IoT      |    | Social   |
  | (MySQL)  |    | (nginx)  |    | Sensors  |    | Media    |
  +----------+    +----------+    +----------+    +----------+
       |               |               |               |
       v               v               v               v
  +------------------------------------------------------------+
  |                    DATA LAKE                               |
  |                                                            |
  |  +----------+  +----------+  +----------+  +----------+   |
  |  | Raw JSON |  | CSV files|  | Parquet  |  | Images   |   |
  |  |          |  |          |  | files    |  | Videos   |   |
  |  +----------+  +----------+  +----------+  +----------+   |
  |                                                            |
  |  RAW. SCHEMA-ON-READ.                                      |
  |  Data is stored AS-IS.                                     |
  |  Schema applied when you READ (not when you store).        |
  |  Cheap storage (S3 = Rs 1.7/GB/month)                     |
  +------------------------------------------------------------+
       |
       +---> Data Scientists (Spark, Python, ML)
       +---> Analytics (Athena, Presto)
       +---> Streaming (Kafka consumers)
```

### Comparison Table

```
+---------------------+----------------------+----------------------+
| Aspect              | Data Warehouse       | Data Lake            |
+---------------------+----------------------+----------------------+
| Data type           | STRUCTURED only      | Structured, semi-    |
|                     | (tables, rows, cols) | structured, AND      |
|                     |                      | unstructured (raw)   |
+---------------------+----------------------+----------------------+
| Schema              | Schema-on-WRITE      | Schema-on-READ       |
|                     | (define schema first,| (dump data now,      |
|                     | then load data)      | define schema later) |
+---------------------+----------------------+----------------------+
| Storage cost        | EXPENSIVE (SSD,      | CHEAP (S3, HDFS,     |
|                     | columnar storage,    | object storage)      |
|                     | compute-optimized)   |                      |
+---------------------+----------------------+----------------------+
| Query speed         | FAST (optimized for  | SLOWER (need to      |
|                     | SQL analytics)       | process raw data)    |
+---------------------+----------------------+----------------------+
| Users               | Business analysts,   | Data scientists,     |
|                     | BI tools             | ML engineers         |
+---------------------+----------------------+----------------------+
| Processing          | ETL (transform       | ELT (load first,     |
|                     | before loading)      | transform later)     |
+---------------------+----------------------+----------------------+
| Data quality        | HIGH (cleaned before | VARIABLE (raw data,  |
|                     | loading)             | may contain junk)    |
+---------------------+----------------------+----------------------+
| Flexibility         | LOW (schema must     | HIGH (store anything)|
|                     | be defined upfront)  |                      |
+---------------------+----------------------+----------------------+
| Risk                | Under-collection     | "Data swamp" — dump  |
|                     | (can't store what    | everything, nobody   |
|                     | doesn't fit schema)  | can find anything    |
+---------------------+----------------------+----------------------+
| Examples            | Amazon Redshift,     | Amazon S3, Hadoop    |
|                     | Google BigQuery,     | HDFS, Azure Data     |
|                     | Snowflake            | Lake Storage         |
+---------------------+----------------------+----------------------+
```

### The Lakehouse — Best of Both Worlds

```
  +------------------------------------------------------------+
  |                    LAKEHOUSE                               |
  |                                                            |
  |  +--------------------------------------------------+     |
  |  |  Cheap Object Storage (like a Data Lake)          |     |
  |  |  S3, ADLS, GCS                                   |     |
  |  +--------------------------------------------------+     |
  |                         +                                  |
  |  +--------------------------------------------------+     |
  |  |  Structured Query Layer (like a Data Warehouse)   |     |
  |  |  ACID transactions, schema enforcement, SQL       |     |
  |  +--------------------------------------------------+     |
  |                         =                                  |
  |  Store raw data cheaply, but query it with SQL speed.      |
  |                                                            |
  |  Technologies: Delta Lake (Databricks), Apache Iceberg,    |
  |                Apache Hudi                                 |
  +------------------------------------------------------------+
```

**The Lakehouse idea:** Keep data in cheap object storage (S3) but add
a metadata and transaction layer on top. So you get:
- Cheap storage of a Data Lake
- Fast structured queries of a Data Warehouse
- ACID transactions on top of files
- Both BI analysts and ML engineers can use the same data

### Companies Using It

| Company       | Approach     | Tools                               |
|---------------|-------------|--------------------------------------|
| Netflix       | Data Lake    | S3 + Apache Spark + Presto          |
| Uber          | Lakehouse    | Apache Hudi on S3                   |
| Flipkart      | Warehouse    | Google BigQuery / custom Hive       |
| LinkedIn      | Lakehouse    | Custom on HDFS + Spark              |
| Databricks    | Lakehouse    | Delta Lake (they invented it)       |
| Airbnb        | Warehouse    | Snowflake + custom ETL              |

### When to Use in System Design

- "Where do you store analytics data?" → Data Warehouse (for structured BI)
- "How do you handle petabytes of raw log/event data?" → Data Lake
- "How do you do ML training on historical data?" → Data Lake or Lakehouse
- "How do you run SQL analytics on cheap storage?" → Lakehouse

---
---

## 9. Checksum & Data Integrity

### What Is It (Plain English)

A checksum is a small fixed-size value computed from data. If even ONE
bit of the data changes, the checksum changes completely. It is used to
detect accidental corruption — like a "fingerprint" of your data.

### Real-Life Analogy — Parcel Weight Check

```
You send a parcel from Mumbai to Delhi via courier.

Before sending:  You weigh the parcel = 2.350 kg (write on the label)
After receiving: Receiver weighs it   = 2.350 kg (matches!)
                 → Parcel is intact!

If the weight is 2.200 kg → something fell out or was removed! CORRUPTION.

The weight is like a checksum:
- Does not tell you WHAT changed
- Only tells you WHETHER something changed
- Very quick to check
```

### Why It Exists

Data gets corrupted ALL THE TIME:
- Hard disk bit rot (bits flip over time)
- Network packet corruption (a bit flips during transmission)
- Software bugs writing wrong data
- Cosmic rays flipping RAM bits (yes, really — Google has papers on this)

Without checksums, you would never know your data was corrupted until
something breaks catastrophically.

### How It Works

#### Simple Checksum (Adding Bytes)

```
Data: "HELLO"  →  H(72) E(69) L(76) L(76) O(79)

Simple checksum = 72 + 69 + 76 + 76 + 79 = 372

Send: "HELLO" + checksum 372

Receiver gets "HELLO", computes 72+69+76+76+79 = 372. MATCH!

If one byte was corrupted: "HALLO"
  H(72) A(65) L(76) L(76) O(79) = 368. MISMATCH! Corruption detected.
```

But simple checksums are weak. "HELLO" and "OELLH" have the SAME checksum
(same letters, different order). We need something stronger.

#### Cryptographic Hash Functions (MD5, SHA-256)

```
  Input (any size)                    Hash Function               Output (fixed size)
  +---------------------------+       +----------+       +----------------------------------+
  | "Hello World"             | ----> | SHA-256  | ----> | a591a6d40bf420404a011733cfb7b190 |
  +---------------------------+       +----------+       | d62c65bf0bcda32b57b277d9ad9f146e |
  (11 bytes)                                             +----------------------------------+
                                                          (always 64 hex chars = 256 bits)

  +---------------------------+       +----------+       +----------------------------------+
  | "Hello World!"            | ----> | SHA-256  | ----> | 7f83b1657ff1fc53b92dc18148a1d65d |
  +---------------------------+       +----------+       | fc2d4b1fa3d677284addd200126d9069 |
  (12 bytes — just added "!")                            +----------------------------------+
                                                          (completely different hash!)
```

**Key properties:**
1. **Deterministic:** Same input ALWAYS gives same output
2. **Avalanche effect:** Change 1 bit of input → ~50% of output bits change
3. **Fixed size:** Output is always the same length regardless of input size
4. **One-way:** Cannot reverse-engineer the input from the hash
5. **Collision-resistant:** Extremely hard to find two different inputs
   with the same hash

#### Common Hash Functions

```
+----------+-------------+------------+-----------------------------------+
| Function | Output Size | Speed      | Usage                             |
+----------+-------------+------------+-----------------------------------+
| MD5      | 128 bits    | Very fast  | File integrity checks, checksums  |
|          | (32 hex)    |            | NOT for security (broken)         |
+----------+-------------+------------+-----------------------------------+
| SHA-1    | 160 bits    | Fast       | Git commit hashes                 |
|          | (40 hex)    |            | NOT for security (broken)         |
+----------+-------------+------------+-----------------------------------+
| SHA-256  | 256 bits    | Moderate   | Bitcoin, TLS, digital signatures  |
|          | (64 hex)    |            | Current security standard         |
+----------+-------------+------------+-----------------------------------+
| CRC32    | 32 bits     | Very fast  | Network packet checksums,         |
|          | (8 hex)     |            | zip file integrity                |
+----------+-------------+------------+-----------------------------------+
```

### Real-World Applications

```
1. FILE DOWNLOAD INTEGRITY
   Website shows: "ubuntu-24.04.iso (SHA-256: a591a6d4...)"
   You download the file, compute SHA-256 locally.
   If hashes match → file downloaded correctly!
   If different → file was corrupted during download.

2. DATABASE REPLICATION
   +----------+               +----------+
   | Primary  |  replicate    | Replica  |
   | DB       | ------------> | DB       |
   +----------+               +----------+
   Checksum of row on primary = checksum of row on replica?
   If yes → replication is correct!
   If no  → data divergence detected!

3. DISTRIBUTED FILE SYSTEMS (HDFS, S3)
   File split into 128 MB blocks.
   Each block has a checksum.
   On read, checksum is verified.
   If mismatch → block is corrupted, read from another replica.

   +--------+--------+--------+
   | Block1 | Block2 | Block3 |
   | CRC:a1 | CRC:b2 | CRC:c3 |
   +--------+--------+--------+
   Each block's checksum is verified on every read.

4. GIT VERSION CONTROL
   Every commit = SHA-1 hash of the content + metadata.
   If ANY byte changes, the hash changes.
   This is why git detects corrupted repositories.

5. BLOCKCHAIN
   Each block contains the hash of the previous block.
   If you tamper with one block, its hash changes,
   which breaks the chain for ALL subsequent blocks.

   +----------+    +----------+    +----------+
   | Block 1  |<---| Block 2  |<---| Block 3  |
   | hash:aa  |    | prev:aa  |    | prev:bb  |
   |          |    | hash:bb  |    | hash:cc  |
   +----------+    +----------+    +----------+
   Tamper with Block 1 → hash changes from aa to xx
   → Block 2's prev:aa no longer matches → chain is BROKEN
```

### Checksum vs Hash vs Digital Signature

```
+---------------------+------------------+----------------------------------+
| Mechanism           | Detects          | Example                          |
+---------------------+------------------+----------------------------------+
| Checksum (CRC32)    | Accidental       | Network packet corruption,       |
|                     | corruption       | file download errors             |
+---------------------+------------------+----------------------------------+
| Cryptographic Hash  | Accidental AND   | Password storage, file integrity,|
| (SHA-256)           | intentional      | blockchain, git                  |
|                     | tampering        |                                  |
+---------------------+------------------+----------------------------------+
| Digital Signature   | Tampering AND    | Software updates, HTTPS certs,   |
| (RSA + SHA-256)     | proves identity  | e-Aadhaar verification           |
|                     | of sender        |                                  |
+---------------------+------------------+----------------------------------+
```

### Companies Using It

| Company       | What                   | How                             |
|---------------|------------------------|---------------------------------|
| Amazon S3     | Object integrity       | MD5 checksum on every upload    |
| Git/GitHub    | Repository integrity   | SHA-1 hash for every commit     |
| Bitcoin       | Blockchain integrity   | SHA-256 hash chain              |
| WhatsApp      | Media integrity        | Checksum for images/videos      |
| HDFS (Hadoop) | Block integrity        | CRC32 for every 128 MB block    |
| Google        | Colossus (file system) | Checksums at every storage layer|

### When to Use in System Design

- "How do you ensure data integrity in replication?" → Checksums on each row/block
- "How do you verify a file downloaded correctly?" → SHA-256 hash comparison
- "How do you detect bit rot in storage?" → Periodic checksum verification
- "How does blockchain prevent tampering?" → Hash chain (each block references previous hash)

---
---

## 10. Idempotency

### What Is It (Plain English)

An operation is **idempotent** if doing it once has the same effect as
doing it multiple times. No matter how many times you repeat it, the
result is the same.

### Real-Life Analogy — Light Switch vs Water Tap

```
IDEMPOTENT (Light Switch — "Turn OFF"):
  Press "OFF" once  → light is off
  Press "OFF" again → light is still off
  Press "OFF" 100 times → light is still off
  Same result every time. Safe to retry!

NOT IDEMPOTENT (Water Tap — "Pour one glass"):
  Pour once  → 1 glass of water
  Pour twice → 2 glasses of water
  Pour 5 times → 5 glasses of water!
  Each repeat ADDS more water. NOT safe to retry blindly!

In software:
  "Set balance to 5000"  → idempotent (result is always 5000)
  "Add 100 to balance"   → NOT idempotent (each retry adds 100 more!)
```

### Why It Exists

The internet is UNRELIABLE. Requests can:
1. **Timeout:** Payment request sent, no response. Did it go through?
2. **Duplicate:** Network retry sends the same request twice.
3. **Crash:** Server crashes after processing but before responding.

```
  The NIGHTMARE Scenario:

  +--------+                    +----------+
  | User   |                    | Payment  |
  | (App)  |                    | Server   |
  +--------+                    +----------+
       |                             |
       | "Charge Rs 5000"            |
       |---------------------------->|
       |                             | Payment processed!
       |                             | Debited Rs 5000
       |         TIMEOUT!            |
       |    (no response received)   | Response sent but lost
       |                             | in the network
       |                             |
       | User doesn't know if it     |
       | worked. App RETRIES:        |
       |                             |
       | "Charge Rs 5000" (again)    |
       |---------------------------->|
       |                             | Payment processed AGAIN!
       |                             | Debited Rs 5000 MORE!
       |                             |
       | User charged Rs 10,000      |
       | instead of Rs 5000!         |
       | DISASTER!                   |
```

Idempotency prevents this. If the payment is idempotent, the second
"Charge Rs 5000" is recognized as a duplicate and NOT processed again.

### How It Works — The Idempotency Key

The most common pattern: the CLIENT generates a unique key for each
business operation. The server uses this key to detect duplicates.

```
  +--------+                          +----------+         +-------+
  | Client |                          | Payment  |         | Redis |
  |        |                          | Server   |         | (or DB)|
  +--------+                          +----------+         +-------+
       |                                    |                  |
       | Generate idempotency key:          |                  |
       | key = "pay_abc123xyz"              |                  |
       |                                    |                  |
       | POST /charge                       |                  |
       | Idempotency-Key: pay_abc123xyz     |                  |
       | Body: {amount: 5000, user: 123}    |                  |
       |----------------------------------->|                  |
       |                                    |                  |
       |                     Check: does key exist?            |
       |                                    |----------------->|
       |                                    |                  |
       |                                    |  NO (first time) |
       |                                    |<-----------------|
       |                                    |                  |
       |                     Process payment (debit Rs 5000)   |
       |                                    |                  |
       |                     Store: key → {status: "done",     |
       |                            response: {txn: "T001"}}   |
       |                                    |----------------->|
       |                                    |                  |
       |  Response: {txn: "T001", status: "success"}           |
       |<-----------------------------------|                  |
       |                                    |                  |
       |  ... TIMEOUT! Response lost ...    |                  |
       |                                    |                  |
       | RETRY with SAME key:              |                  |
       | POST /charge                       |                  |
       | Idempotency-Key: pay_abc123xyz     |                  |
       |----------------------------------->|                  |
       |                                    |                  |
       |                     Check: does key exist?            |
       |                                    |----------------->|
       |                                    |                  |
       |                                    | YES! Found it.   |
       |                                    | Return stored    |
       |                                    | response.        |
       |                                    |<-----------------|
       |                                    |                  |
       |  SAME Response: {txn: "T001", status: "success"}     |
       |<-----------------------------------|                  |
       |                                    |                  |
       | Payment NOT charged twice!         |                  |
       | User sees success.                 |                  |
```

### HTTP Methods — Natural Idempotency

```
+----------+------------+----------------------------------------------+
| Method   | Idempotent?| Why                                          |
+----------+------------+----------------------------------------------+
| GET      | YES        | Reading data never changes it.                |
|          |            | GET /users/123 → same user every time.       |
+----------+------------+----------------------------------------------+
| PUT      | YES        | "Set X to value Y" — always same result.     |
|          |            | PUT /users/123 {name: "Sheetal"}             |
|          |            | → user is "Sheetal", no matter how many times.|
+----------+------------+----------------------------------------------+
| DELETE   | YES        | "Delete user 123" — first time deletes it.   |
|          |            | Second time? Already deleted. Same result:   |
|          |            | user 123 does not exist.                     |
+----------+------------+----------------------------------------------+
| PATCH    | DEPENDS    | "Set name to Sheetal" → idempotent.          |
|          |            | "Increment age by 1" → NOT idempotent.       |
+----------+------------+----------------------------------------------+
| POST     | NO (by     | "Create a new order" — each call creates     |
|          | default)   | a NEW order. Must add idempotency key to     |
|          |            | make it safe.                                |
+----------+------------+----------------------------------------------+
```

### Implementation Patterns

#### Pattern 1: Idempotency Key in Header

```
POST /api/v1/payments
Idempotency-Key: pay_a1b2c3d4e5f6
Content-Type: application/json

{
  "amount": 5000,
  "currency": "INR",
  "customer_id": "cust_123"
}
```

Server-side logic:
```python
async def create_payment(request):
    key = request.headers["Idempotency-Key"]

    # Check if we've seen this key before
    existing = await redis.get(f"idempotency:{key}")
    if existing:
        return json.loads(existing)  # Return stored response

    # Process the payment
    result = await process_payment(request.body)

    # Store the response with the key (expire after 24 hours)
    await redis.set(
        f"idempotency:{key}",
        json.dumps(result),
        ex=86400  # 24 hours
    )

    return result
```

#### Pattern 2: Database Unique Constraint

```sql
-- Orders table with a unique constraint on client_order_id
CREATE TABLE orders (
    id UUID PRIMARY KEY,
    client_order_id VARCHAR(64) UNIQUE,  -- idempotency key!
    amount DECIMAL,
    status VARCHAR(20),
    created_at TIMESTAMP
);

-- First insert succeeds
INSERT INTO orders (id, client_order_id, amount, status)
VALUES ('uuid-1', 'order_abc123', 5000, 'created');

-- Second insert with same client_order_id FAILS
-- (unique constraint violation)
-- Application catches this and returns the existing order
```

#### Pattern 3: Conditional Updates (Optimistic Locking)

```sql
-- Only debit if balance is exactly what we expect
UPDATE accounts
SET balance = balance - 5000,
    version = version + 1
WHERE user_id = 123
  AND version = 5;          -- expected current version

-- If someone else already modified the balance (version changed),
-- this UPDATE affects 0 rows → retry with fresh data
```

### Idempotency in Real Systems

```
+-------------------+----------------------------------------------+
| System            | How They Handle Idempotency                  |
+-------------------+----------------------------------------------+
| Razorpay          | Idempotency key in request header.           |
|                   | Same key = same response, no double charge.  |
+-------------------+----------------------------------------------+
| Stripe            | Idempotency-Key header on every POST.        |
|                   | Keys expire after 24 hours.                  |
+-------------------+----------------------------------------------+
| UPI (NPCI)        | Transaction Reference ID is unique.          |
|                   | If retry with same ref ID, returns existing  |
|                   | status instead of creating new transaction.  |
+-------------------+----------------------------------------------+
| Amazon SQS        | MessageDeduplicationId for FIFO queues.      |
|                   | Same ID within 5-minute window = deduped.    |
+-------------------+----------------------------------------------+
| Kafka             | Producer idempotency with ProducerId +       |
|                   | SequenceNumber. Broker deduplicates.         |
+-------------------+----------------------------------------------+
| Google Pay        | Unique request_id per transaction.           |
|                   | Duplicates return previous result.            |
+-------------------+----------------------------------------------+
```

### Common Pitfalls

```
WRONG: Using timestamp as idempotency key
  Two requests at the same millisecond → same key → one is silently dropped!

WRONG: Using sequential IDs as idempotency key
  Predictable, can be guessed by attackers.

WRONG: Generating idempotency key on the SERVER
  Defeats the purpose! The CLIENT must generate it so retries use the same key.

WRONG: Making idempotency keys live forever
  Storage grows unbounded. Set a TTL (e.g., 24 hours).

RIGHT: UUID v4 or client-generated unique string
  Unpredictable, unique, client-controlled.
```

### When to Use in System Design

- **Payments** — "How do you prevent double charging?" → Idempotency key
- **Order creation** — "What if the user clicks 'Place Order' twice?" → Idempotency
- **Message queues** — "What if a message is delivered twice?" → Consumer idempotency
- **API design** — "How do you handle network retries safely?" → Idempotency key header
- **Distributed systems** — "What if a service processes a request but the ACK is lost?" → Idempotency

**The golden rule:** Any operation that changes state (writes money, creates
records, sends notifications) should be idempotent. Read-only operations
are naturally idempotent.

---
---

## Quick Reference — All 10 Concepts

```
+---+-------------------------+----------------------------------+-------------------+
| # | Concept                 | One-Line Summary                 | When to Mention   |
+---+-------------------------+----------------------------------+-------------------+
| 1 | OAuth 2.0 / Auth        | Login with Google without        | Any auth/login    |
|   |                         | sharing your password            | discussion        |
+---+-------------------------+----------------------------------+-------------------+
| 2 | Reverse Proxy           | Receptionist in front of your    | EVERY system has  |
|   |                         | servers (SSL, LB, cache)         | one               |
+---+-------------------------+----------------------------------+-------------------+
| 3 | Distributed Txns        | All services succeed or all      | Multi-service     |
|   |                         | roll back (Saga > 2PC)           | write operations  |
+---+-------------------------+----------------------------------+-------------------+
| 4 | Leader Election         | One node writes, others follow   | DB replication,   |
|   |                         | (Raft/ZooKeeper)                 | Kafka partitions  |
+---+-------------------------+----------------------------------+-------------------+
| 5 | Bloom Filters           | "Definitely NOT" or "probably    | Username check,   |
|   |                         | yes" (saves DB lookups)          | cache membership  |
+---+-------------------------+----------------------------------+-------------------+
| 6 | Geospatial Indexing     | Find things near a location      | Uber, Zomato,     |
|   |                         | (Geohash, Quadtree)              | any location app  |
+---+-------------------------+----------------------------------+-------------------+
| 7 | Service Mesh            | Sidecar proxy handles all        | 50+ microservices |
|   |                         | service-to-service networking    | at scale          |
+---+-------------------------+----------------------------------+-------------------+
| 8 | Lake vs Warehouse       | Warehouse = structured SQL,      | Analytics, ML,    |
|   |                         | Lake = dump everything cheaply   | big data          |
+---+-------------------------+----------------------------------+-------------------+
| 9 | Checksum/Integrity      | Hash fingerprint to detect       | Storage, transfer,|
|   |                         | data corruption                  | replication       |
+---+-------------------------+----------------------------------+-------------------+
|10 | Idempotency             | Same request N times =           | Payments, orders, |
|   |                         | same result (no double charge)   | any state change  |
+---+-------------------------+----------------------------------+-------------------+
```

---

**End of Week 09 — Missing Concepts**
