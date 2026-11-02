# Aegis Assurance Group — Insurance Claims Adjudication Platform

A Java 21 / Spring Boot microservices starter covering policy validation, claim submission, approval decisions, JWT authentication, Eureka discovery, API Gateway routing, load-balanced inter-service calls, tests, and Docker deployment.

## Services and ports

| Service | Port | Responsibility |
|---|---:|---|
| API Gateway | 8080 | Single entry point, JWT verification, `lb://` routing |
| Auth Service | 8081 | Demo login and signed JWT issuance |
| Policy Service | 8082 | Create/read policies and validate active coverage |
| Claim Service | 8083 | Claim submission, policy validation call, approval call, status lookup |
| Approval Service | 8084 | Record an adjudication decision |
| Eureka Server | 8761 | Service registration and discovery |
| PostgreSQL | 5432 | Persistent storage |

## Requirements checklist

- [x] Policy Service, Claim Service, Approval Service
- [x] Auth Service, API Gateway, Eureka Server
- [x] JWT token issuance and verification at Gateway
- [x] Policy validation before claim processing
- [x] Claim -> Policy -> Approval service calls using service discovery
- [x] Load-balanced Gateway routes (`lb://...`) and Claim Service WebClient
- [x] Bean Validation on API request records
- [x] JPA persistence with PostgreSQL
- [x] Dockerfiles and Docker Compose deployment scaffold
- [ ] Add production user persistence/BCrypt, role-specific authorization, TLS, rate limiting, retries/circuit breakers, and observability before real use
- [ ] Expand unit/integration test coverage before production use

## Local setup (Windows / STS)

Requirements: JDK 21, Maven 3.9+, PostgreSQL 16+, STS or IntelliJ.

Create three databases in PostgreSQL:
```sql
CREATE DATABASE aegis_policy;
CREATE DATABASE aegis_claim;
CREATE DATABASE aegis_approval;
```
Defaults in local configuration: username `aegis`, password `aegis123`. Update environment variables if your local PostgreSQL credentials differ. The `auth-service` and Gateway must use the same `JWT_SECRET` (at least 32 bytes).

From the project root, open CMD:
```bat
mvn clean package
```
Run each main class in this order:
1. `EurekaServerApplication` — `http://localhost:8761`
2. `AuthServiceApplication`
3. `PolicyServiceApplication`
4. `ApprovalServiceApplication`
5. `ClaimServiceApplication`
6. `ApiGatewayApplication`

Or use Docker Desktop from the project root:
```bat
docker compose up --build
```
The compose file starts PostgreSQL, Eureka, and all services. For a non-demo deployment, set a strong random `JWT_SECRET` via environment variables and use secrets management instead of committed credentials.

## Demo credentials

- Policyholder: `policyholder` / `Pass123!`
- Adjuster: `adjuster` / `Pass123!`

These are intentionally hard-coded demo credentials and must be replaced by persistent user storage and BCrypt password hashes for production.

## Postman walkthrough

### 1. Login (no token required)
`POST http://localhost:8080/auth/login`
```json
{"username":"policyholder","password":"Pass123!"}
```
Copy `accessToken` from the response. Use the header `Authorization: Bearer YOUR_TOKEN` for all requests except login.

### 2. Create an active policy
`POST http://localhost:8080/policies`
```json
{"policyNumber":"POL-1001","holderUsername":"policyholder","status":"ACTIVE","validUntil":"2027-12-31"}
```

### 3. Validate policy
`GET http://localhost:8080/policies/POL-1001/validate`

### 4. Submit claim
`POST http://localhost:8080/claims`
```json
{"policyNumber":"POL-1001","description":"Vehicle damage after a road incident","amount":25000.00}
```
Claim Service validates the policy, stores the claim, and calls Approval Service with an initial `PENDING` decision. This is a demo workflow, not a rules-based automated settlement engine.

### 5. Read claim / status
`GET http://localhost:8080/claims/1`

### 6. Read approval
`GET http://localhost:8080/approvals/1`

### 7. Adjuster records a final approval decision
Log in as `adjuster` to obtain an adjuster JWT, then call:
`PUT http://localhost:8080/approvals/1`
```json
{"claimId":1,"decision":"APPROVED","notes":"Documents verified"}
```
The Gateway restricts `/approvals/**` to JWTs with the `ADJUSTER` role. The claim status endpoint below is a demo convenience and must be restricted to adjusters in a production version.

### 8. Change claim status (demo endpoint)
`PUT http://localhost:8080/claims/1/status`
```json
{"status":"APPROVED"}
```
**Important:** this endpoint is not yet restricted to adjusters. Add role-based authorization before any real deployment.

## Notes / limitations

- The current login uses demo credentials. A production-ready Auth Service should store users, hash passwords with BCrypt, issue short-lived access tokens and refresh tokens, and apply account lockout policies.
- Gateway JWT verification trusts a shared HMAC secret. The downstream services should also validate tokens or be reachable only from a trusted internal network; do not expose service ports publicly.
- The initial decision is `PENDING`; an adjuster must make the final decision through a controlled approval workflow.
- For resilience at emergency claim volumes, add Resilience4j timeouts/retries/circuit breakers, queues for asynchronous adjudication, idempotency keys, metrics, tracing, and database connection-pool tuning.
- Automated tests are a required follow-up: add unit tests for policy eligibility, JWT validation, claim workflow and integration tests with Testcontainers. Build and run tests in your environment before submission.
