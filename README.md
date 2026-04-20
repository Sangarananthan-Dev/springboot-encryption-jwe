# Encryption Backend (Spring Boot + PostgreSQL + JWE)

This backend provides:
- Student CRUD API (`/api/students`)
- Request decryption (JWE) and response encryption (JWE)
- Replay protection using `timestamp + nonce`

## Tech
- Java 21
- Spring Boot 4
- PostgreSQL
- Nimbus JOSE JWT (`RSA-OAEP-256` + `A256GCM`)

## Configure PostgreSQL
Create database:

```sql
create database encryption;
```

Default local config is in [`application.yaml`](src/main/resources/application.yaml):
- URL: `jdbc:postgresql://localhost:5432/encryption`
- User: `postgres`
- Password: `postgres`

Update if your local credentials differ.

## Run

```bash
./gradlew.bat bootRun
```

Server starts on `http://localhost:8080`.

## Endpoints
- `GET /api/crypto/public-key`: server public key for client request encryption
- `GET /api/students`
- `GET /api/students/{id}`
- `POST /api/students`
- `PUT /api/students/{id}`
- `DELETE /api/students/{id}`

`/api/students` expects encrypted transport headers/payload:
- Header `X-Use-Encryption: true`
- Header `X-Client-Public-Key: <client RSA public key base64 SPKI>`
- Body content-type `application/jose+json` for write operations
- Body shape:

```json
{
  "jwe": "eyJ..."
}
```

The decrypted inner payload format for write operations is:

```json
{
  "data": {
    "name": "Alice",
    "email": "alice@example.com",
    "age": 21
  },
  "timestamp": 1760000000000,
  "nonce": "6f7d2158-f465-4bc3-b37b-5e84f8d216cb"
}
```

## Key Notes
- If `APP_SERVER_PRIVATE_KEY_PEM` and `APP_SERVER_PUBLIC_KEY_PEM` are not provided, backend generates an ephemeral RSA keypair on startup.
- For production, load fixed keys from secure storage (KMS/HSM) and rotate regularly.
