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

## Health Checks

Each service exposes:

```text
GET /api/health
```

Gateway route examples:

```text
GET http://localhost:8080/auth/api/health
GET http://localhost:8080/members/api/health
GET http://localhost:8080/events/api/health
GET http://localhost:8080/content/api/health
GET http://localhost:8080/chat/api/health
GET http://localhost:8080/notifications/api/health
```

## Development Infrastructure

Start MySQL:

```bash
docker compose up -d mysql
```

# youth-union-management
