# HCMCYU Backend Integration Test Report

Generated: 2026-09-06 17:29:31 +07:00

## Command

```bash
mvn clean test
```

## Result

BUILD SUCCESS

## Maven reactor summary

| Module | Result | Time |
| --- | --- | --- |
| hcmcyu-backend | SUCCESS | 0.264 s |
| api-gateway | SUCCESS | 56.377 s |
| auth-service | SUCCESS | 01:00 min |
| member-service | SUCCESS | 02:25 min |
| event-service | SUCCESS | 01:11 min |
| content-service | SUCCESS | 52.827 s |
| chat-service | SUCCESS | 01:00 min |
| notification-service | SUCCESS | 52.525 s |
| audit-service | SUCCESS | 47.502 s |

Total time: 09:07 min

## Test count summary

| Module | Tests | Status |
| --- | ---: | --- |
| api-gateway | 6 | PASS |
| auth-service | 12 | PASS |
| member-service | 46 | PASS |
| event-service | 18 | PASS |
| content-service | 7 | PASS |
| chat-service | 7 | PASS |
| notification-service | 7 | PASS |
| audit-service | 5 | PASS |

Total counted integration/unit tests: 108

## Requested flow coverage

| Flow | Coverage | Test classes | Status |
| --- | --- | --- | --- |
| FLOW 1 | Register, login, me, update profile, avatar, banking info, banking QR | `AuthServiceIntegrationTest`, `MemberProfileIntegrationTest`, `MemberBankingIntegrationTest` | PASS |
| FLOW 2 | WARD_SECRETARY login, view organizations, manage member, assign MEMBER to TDP_SECRETARY | `OrganizationIntegrationTest`, `MemberManagementIntegrationTest`, `RoleManagementIntegrationTest` | PASS |
| FLOW 3 | WARD_DEPUTY_SECRETARY CRUD member, role update rejected with 403 | `MemberManagementIntegrationTest`, `RoleManagementIntegrationTest` | PASS |
| FLOW 4 | TDP_SECRETARY TDP 1 CRUD own TDP, read/update TDP 2 rejected with 403 | `MemberManagementIntegrationTest`, `OrganizationIntegrationTest`, `MemberBankingIntegrationTest` | PASS |
| FLOW 5 | TDP_DEPUTY_SECRETARY has same scoped CRUD permissions as TDP_SECRETARY | `MemberManagementIntegrationTest` | PASS |
| FLOW 6 | Admin create event, member view/vote, admin view participants | `EventIntegrationTest` | PASS |
| FLOW 7 | TDP admin create activity report, upload image, member view published content | `PostIntegrationTest` | PASS |
| FLOW 8 | Member A sends realtime chat to Member B; outsider subscribe/send rejected | `ChatIntegrationTest` | PASS |
| FLOW 9 | Notification list, unread count, mark read | `NotificationIntegrationTest` | PASS |
| FLOW 10 | TDP_SECRETARY views QR in own TDP, cross-TDP rejected with 403, WARD_SECRETARY can view ward-wide | `MemberBankingIntegrationTest` | PASS |

## Security assertions exercised

- JWT protected endpoints reject missing or invalid tokens.
- Expired JWT is rejected.
- Role escalation is rejected for WARD_DEPUTY_SECRETARY, TDP_SECRETARY, and MEMBER.
- Only WARD_SECRETARY can assign roles.
- Organization scope is enforced on member, organization, event, post, and banking endpoints.
- TDP_SECRETARY from TDP 1 cannot access TDP 2 resources by ID.
- MEMBER A cannot access MEMBER B profile or QR banking data.
- Banking data is exposed only through banking endpoints, not default member list responses.
- File upload validation covers accepted image types, invalid type, oversize, random server filename, and non-client storage path.
- WebSocket handshake uses JWT identity; sender is derived from the token.
- Non-members of a conversation cannot subscribe or send messages.
- Error handling returns controlled HTTP status responses.

## Changes made during this test pass

No business logic was added or changed.

Added integration regression tests:

- `member-service/src/test/java/com/hcmcyu/member/MemberManagementIntegrationTest.java`
  - `tdpDeputySecretaryCanCrudMemberInOwnTdpWithSameScopeAsTdpSecretary`
- `chat-service/src/test/java/com/hcmcyu/chat/ChatIntegrationTest.java`
  - `outsiderCannotSubscribeOrSendMessagesToConversation`

## Notes

- Tests run against service test configurations, including in-memory databases where configured.
- Upload tests use local test storage.
- Chat realtime tests use the Spring WebSocket/STOMP client.
- No frontend code was created or modified for this test pass.
