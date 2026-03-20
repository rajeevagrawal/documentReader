# Research: PDF Link Reader and Prompt-to-Word Agent

**Feature**: `001-pdf-to-word-agent`  
**Date**: 2026-03-19

## 1. Platform and runtime

| Topic | Decision | Rationale | Alternatives considered |
|-------|----------|-----------|-------------------------|
| Language / JDK | **Java 25** (LTS track per project policy) | Aligns with Spring Boot 4 first-class support and project constitution intent. | Java 21 LTS only — rejected: user mandated Java 25. |
| Framework | **Spring Boot 4.0.x** | REST APIs, dependency injection, production-ready ops; pairs with Spring ecosystem. | Quarkus, Micronaut — rejected: smaller community for Spring AI integration path chosen. |
| Build | **Maven** | Required by project; reproducible CI and Docker multi-stage builds. | Gradle — rejected by constitution. |
| API description | **OpenAPI 3.2.0** | Single contract for clients, gateway, and EKS/ECS ingress docs. | Protobuf/gRPC — rejected: spec centers on HTTP document workflows. |

## 2. PDF retrieval and text extraction

| Topic | Decision | Rationale | Alternatives considered |
|-------|----------|-----------|-------------------------|
| HTTP fetch | **Spring WebClient** (non-blocking) with bounded timeouts | Consistent with Spring stack; easy to configure max size and connect/read timeouts. | HttpURLConnection — rejected: harder to tune and test. |
| PDF parsing | **Apache PDFBox** | Mature Apache license; text extraction for non-scanned PDFs matches FR-003. | iText — rejected: licensing friction for some deployments. |
| Limits | **Max download 25 MB**, **read timeout 60s**, **max pages 200** (configurable) | Matches edge-case guidance; prevents unbounded memory/time. | Stricter limits — deferred to tuning after load tests. |

## 3. Word generation and file delivery

| Topic | Decision | Rationale | Alternatives considered |
|-------|----------|-----------|-------------------------|
| `.docx` creation | **Apache POI** (`XWPFDocument`) | Standard Java library for Word-compatible OOXML output (FR-006). | docx4j — viable; PDFBox+POI pairing is common. |
| “Local folder” (FR-007) | **Primary**: HTTP response with `Content-Disposition: attachment` so the **client** saves to the user’s local folder (browser, curl, script). **Optional** (dev/single-node): configurable `document.output.allow-path` for server-side write to a mounted volume. | Cloud (EKS/ECS) has no access to end-user laptop paths; download is the portable contract. | SMB/NFS to user desktop — rejected: out of scope. |

## 4. AI-assisted text generation

| Topic | Decision | Rationale | Alternatives considered |
|-------|----------|-----------|-------------------------|
| Integration | **JDK `HttpClient`** calling OpenAI **Chat Completions** JSON API | Spring AI 1.0.x BOM targets Spring Boot 3.4; Boot 4 uses this thin client until Spring AI aligns. Same env shape via `document.reader.ai.*`. | Spring AI `ChatClient` — adopt when Boot 4 + Spring AI BOM is verified. |
| Provider (default) | **OpenAI** (`OPENAI_API_KEY`, `document.reader.ai.chat-completions-url`, `document.reader.ai.model`) | Uses a configurable *full* chat-completions endpoint URL so OpenAI-compatible proxies can be swapped. | Azure OpenAI — same pattern with endpoint URL + key. |
| Safety | System prompt + **refusal** for disallowed content; log decision id | Meets edge-case policy without blocking MVP. | Custom moderation service — future hardening. |

## 5. Operations (Docker / EKS / ECS)

| Topic | Decision | Rationale | Alternatives considered |
|-------|----------|-----------|-------------------------|
| Container | **Multi-stage Dockerfile**: Maven build → distroless or Eclipse Temurin **jlink**/**jre** runtime image | Smaller attack surface for EKS/ECS. | Fat JAR only in final image — acceptable variant documented in quickstart. |
| Health | **Spring Boot Actuator** `/actuator/health` | Required for Kubernetes liveness/readiness. | None. |
| Config | **Environment variables** + `application.yml` for limits and keys | Twelve-factor friendly on ECS/EKS. | Vault integration — future. |

## 6. Testing

| Topic | Decision | Rationale | Alternatives considered |
|-------|----------|-----------|-------------------------|
| Unit | **JUnit 5** + **Mockito** | Spring Boot default. | TestNG — rejected. |
| Slice tests | **`@WebMvcTest`** for controllers, **`WebClient`**/`MockWebServer` for PDF fetch | Fast feedback on OpenAPI-implemented controllers. | Full-stack only — rejected: too slow for CI. |
| Contract | **OpenAPI file** checked into `contracts/`; optional **Springdoc** generation diff in CI | Keeps API aligned with constitution. | Pact — optional later for consumer-driven contracts. |

## 7. Durable storage (when a database is needed)

| Topic | Decision | Rationale | Alternatives considered |
|-------|----------|-----------|-------------------------|
| RDBMS | **PostgreSQL** (current stable major, e.g. 16+) | Portable on EKS/ECS (RDS, Aurora, Cloud SQL–compatible patterns, self-managed); strong JSON support if needed later. | MySQL/MariaDB — rejected as default per project preference. |
| Access | **Spring Data JPA** or **Spring JDBC** | JPA for entity-centric features; JDBC for thin audit tables—team choice per feature. | NoSQL primary store — rejected unless a later spec requires it. |
| Schema | **Flyway** or **Liquibase** migrations in `src/main/resources/db/migration` | Repeatable deploys on Kubernetes. | Ad-hoc DDL — rejected. |
| MVP default | **No PostgreSQL required** for first slice | Spec does not mandate persistence; add DB only when implementing async jobs, retention, or audit. | — |

## 8. Resolved clarifications (formerly NEEDS CLARIFICATION)

- **Local path in cloud**: Resolved by download-first contract; server path only for controlled deployments.
- **LLM vendor**: Resolved by Spring AI + OpenAI-compatible default with swappable configuration.
- **Spring Boot 4 / Java 25**: Spring Boot 4.0.x supports Java 25; use current 4.0.x patch (e.g. 4.0.4) per BOM at implementation time.
- **Database choice if added**: Use **PostgreSQL** with versioned migrations (see §7).
