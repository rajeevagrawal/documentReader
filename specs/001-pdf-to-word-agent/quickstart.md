# Quickstart: Document Reader API (feature `001-pdf-to-word-agent`)

**Date**: 2026-03-19

## Prerequisites

- JDK **25**
- **Maven** 3.9+
- An **OpenAI-compatible API key** (or other Spring AI–supported provider) for text generation

## Configure

Create or export environment variables (example names—align with Spring AI 4.x properties at implementation):

```bash
export SPRING_AI_OPENAI_API_KEY="sk-..."
# Optional:
export SPRING_AI_OPENAI_BASE_URL="https://api.openai.com"
```

Tune PDF limits (defaults set in `application.yml` at implementation):

| Property (illustrative) | Purpose |
|-------------------------|---------|
| `document.pdf.max-bytes` | Max download size (default 25 MB) |
| `document.pdf.max-pages` | Max pages to parse (default 200) |
| `document.pdf.fetch-timeout` | WebClient timeout |

## PostgreSQL (when you enable a database)

If the service gains durable storage, use **PostgreSQL** only (see `research.md` §7). Local example with Docker:

```bash
docker run --name document-reader-pg -e POSTGRES_PASSWORD=dev -e POSTGRES_DB=documentreader -p 5432:5432 -d postgres:16
export SPRING_DATASOURCE_URL='jdbc:postgresql://localhost:5432/documentreader'
export SPRING_DATASOURCE_USERNAME='postgres'
export SPRING_DATASOURCE_PASSWORD='dev'
```

Point `SPRING_DATASOURCE_URL` at **RDS / Aurora / Cloud SQL** (or in-cluster Postgres) in EKS/ECS. Run **Flyway/Liquibase** migrations on startup or as an init container.

## Run locally

```bash
./mvnw -q spring-boot:run
```

Health check (once Actuator is enabled):

```bash
curl -s http://localhost:8080/actuator/health
```

## Try the API

**1. Extract text from a PDF URL**

```bash
curl -s -X POST http://localhost:8080/api/v1/pdf/extract \
  -H 'Content-Type: application/json' \
  -d '{"url":"https://www.w3.org/WAI/WCAG21/working-examples/pdf-img/not-a-pdf.pdf"}'
```

(Replace with a known-good public PDF URL for success paths.)

**2. Generate a `.docx` and save locally**

```bash
curl -s -D headers.txt -o my-output.docx -X POST http://localhost:8080/api/v1/documents/word \
  -H 'Content-Type: application/json' \
  -d '{"prompt":"Write three short paragraphs summarizing agile retrospectives.","fileName":"retro.docx"}'
```

Inspect `headers.txt` for `Content-Disposition` filename.

## Docker image (EKS / ECS)

Build (after `pom.xml` and `Dockerfile` exist at repo root):

```bash
docker build -t document-reader-api:local .
docker run --rm -p 8080:8080 \
  -e SPRING_AI_OPENAI_API_KEY="$SPRING_AI_OPENAI_API_KEY" \
  document-reader-api:local
```

Kubernetes: mount secrets for API keys; set CPU/memory requests; use `/actuator/health` for probes.

## OpenAPI contract

Authoritative contract: `specs/001-pdf-to-word-agent/contracts/openapi.yaml`.  
Implementation SHOULD expose the same operations and media types (Springdoc optional for `/v3/api-docs`).

## README

Update root **`README.md`** whenever public endpoints, env vars, or Docker run instructions change (project documentation rule).
