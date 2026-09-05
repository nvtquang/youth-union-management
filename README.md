# HCMCYU Backend Skeleton

Backend skeleton for the Ho Chi Minh Communist Youth Union member management system of Thuong Cat Ward.

## Modules

| Module | Port |
| --- | ---: |
| api-gateway | 8080 |
| auth-service | 8081 |
| member-service | 8082 |
| event-service | 8083 |
| content-service | 8084 |
| chat-service | 8085 |
| notification-service | 8086 |
| audit-service | 8087 |

## Build

```bash
mvn clean test
```

## Run With Maven

```bash
mvn -pl api-gateway spring-boot:run
mvn -pl auth-service spring-boot:run
mvn -pl member-service spring-boot:run
mvn -pl event-service spring-boot:run
mvn -pl content-service spring-boot:run
mvn -pl chat-service spring-boot:run
mvn -pl notification-service spring-boot:run
mvn -pl audit-service spring-boot:run
```

Run development seed data with the `dev` profile:

```bash
mvn -pl auth-service spring-boot:run -Dspring-boot.run.profiles=dev
mvn -pl member-service spring-boot:run -Dspring-boot.run.profiles=dev
mvn -pl event-service spring-boot:run -Dspring-boot.run.profiles=dev
mvn -pl content-service spring-boot:run -Dspring-boot.run.profiles=dev
mvn -pl chat-service spring-boot:run -Dspring-boot.run.profiles=dev
mvn -pl notification-service spring-boot:run -Dspring-boot.run.profiles=dev
```

## Development Demo Accounts

All demo accounts use this password:

```text
Demo@12345
```

| Role | Username |
| --- | --- |
| WARD_SECRETARY | ward.secretary |
| WARD_DEPUTY_SECRETARY | ward.deputy |
| TDP_SECRETARY | tdp1.secretary, tdp2.secretary, tdp3.secretary, tdp4.secretary, tdp5.secretary |
| TDP_DEPUTY_SECRETARY | tdp1.deputy, tdp2.deputy, tdp3.deputy, tdp4.deputy, tdp5.deputy |
| MEMBER | tdp1.member1 to tdp1.member5, ... , tdp5.member1 to tdp5.member5 |

Seed data is profile-scoped with `dev` and must not run in `prod`.

## Health Checks

Each service exposes:

```text
GET /api/health
```

Gateway route examples:

```text
GET http://localhost:8080/api/health
GET http://localhost:8080/api/auth/health
GET http://localhost:8080/api/members/health
GET http://localhost:8080/api/organizations/health
GET http://localhost:8080/api/events/health
GET http://localhost:8080/api/posts/health
GET http://localhost:8080/api/chat/health
GET http://localhost:8080/api/notifications/health
```

## OpenAPI / Swagger

Each backend module exposes OpenAPI JSON and Swagger UI:

| Module | Swagger UI | OpenAPI JSON |
| --- | --- | --- |
| api-gateway | http://localhost:8080/swagger-ui.html | http://localhost:8080/v3/api-docs |
| auth-service | http://localhost:8081/swagger-ui.html | http://localhost:8081/v3/api-docs |
| member-service | http://localhost:8082/swagger-ui.html | http://localhost:8082/v3/api-docs |
| event-service | http://localhost:8083/swagger-ui.html | http://localhost:8083/v3/api-docs |
| content-service | http://localhost:8084/swagger-ui.html | http://localhost:8084/v3/api-docs |
| chat-service | http://localhost:8085/swagger-ui.html | http://localhost:8085/v3/api-docs |
| notification-service | http://localhost:8086/swagger-ui.html | http://localhost:8086/v3/api-docs |
| audit-service | http://localhost:8087/swagger-ui.html | http://localhost:8087/v3/api-docs |

Swagger UI supports JWT Bearer authentication. Click `Authorize`, enter the access token returned by `POST /api/auth/login`, then call protected APIs. Request/response DTOs, Jakarta Validation constraints, JWT authentication, role/scope notes, and the common error response schema are documented in the generated specs.

## Postman

Import these files:

```text
postman/HCMCYU.postman_collection.json
postman/HCMCYU.postman_environment.json
```

Select the `HCMCYU Local` environment. The environment uses:

```text
baseUrl=http://localhost:8080
accessToken=
```

Run `Auth / Login` first. Its test script stores `accessToken` and `refreshToken` automatically from the response. The remaining requests call through `api-gateway` and use `Authorization: Bearer {{accessToken}}`.

## Development Infrastructure

Start MySQL:

```bash
docker compose up -d mysql
```

# youth-union-management
