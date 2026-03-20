# Data Model: PDF Link Reader and Prompt-to-Word Agent

**Feature**: `001-pdf-to-word-agent`  
**Date**: 2026-03-19

## Overview

Logical entities support PDF ingestion, optional context coupling, prompt-driven generation, and traceable task runs. Persistence is **in-memory / request-scoped** for MVP.

**When the implementation adds a database** (recommended only when requirements need history, audit, or async state), map these entities to **PostgreSQL** tables via JPA or JDBC. Do **not** introduce a second RDBMS without an ADR.

## Entities

### 1. `PdfSource`

| Field | Type | Rules | Notes |
|-------|------|-------|-------|
| `id` | UUID | Required, immutable | Correlates logs and async steps if introduced later. |
| `url` | URI string | Required; must be `http` or `https` | Validated before fetch. |
| `contentType` | string | Optional | From response; must include `application/pdf` or magic-byte check. |
| `rawSizeBytes` | long | Optional | After download; reject if over configured max. |
| `extractedText` | string | Optional | Empty if no extractable text (scanned PDF). |
| `warnings` | list of string | Optional | e.g. truncated, partial extraction. |
| `status` | enum | `PENDING`, `READY`, `FAILED` | Drives API responses. |
| `failureReason` | string | Optional | User-safe message when `FAILED`. |

**Relationships**: Referenced by `TaskRun` when PDF flow is used.

### 2. `UserPrompt`

| Field | Type | Rules | Notes |
|-------|------|-------|-------|
| `text` | string | Required; max length configurable (e.g. 16_000 chars) | User’s natural-language instruction. |
| `usePdfContext` | boolean | Default `true` when PDF present | Maps to FR-005 default. |

### 3. `WordOutput`

| Field | Type | Rules | Notes |
|-------|------|-------|-------|
| `fileName` | string | Required; sanitized basename ending in `.docx` | Prevents path traversal. |
| `mimeType` | string | Constant `application/vnd.openxmlformats-officedocument.wordprocessingml.document` | For HTTP responses. |
| `byteSize` | long | Required | After generation. |
| `createdAt` | instant | Required | Audit / support. |

**Relationships**: Produced by a `TaskRun`; no long-term storage in MVP.

### 4. `TaskRun`

| Field | Type | Rules | Notes |
|-------|------|-------|-------|
| `id` | UUID | Required | Client correlation id. |
| `pdfSourceId` | UUID | Optional | Set when PDF URL provided. |
| `prompt` | embedded `UserPrompt` | Required for generation | |
| `status` | enum | `ACCEPTED`, `GENERATING`, `COMPLETED`, `FAILED` | |
| `resultDocument` | `WordOutput` | Optional | Present when `COMPLETED`. |
| `errorMessage` | string | Optional | Safe for clients. |

## Validation rules (cross-cutting)

- **URL**: HTTPS strongly recommended; HTTP allowed only if `document.pdf.allow-insecure-http=true` (default false) for dev.
- **Output file name**: Alphanumeric, dash, underscore, single `.`; max length 255; forced `.docx`.
- **PDF**: Reject non-PDF before extraction; password-protected → `FAILED` with clear code.

## State transitions (`TaskRun`)

```
ACCEPTED → GENERATING → COMPLETED
                      ↘ FAILED
```

## Temporary storage (FR-009)

- Downloaded PDF bytes and generated `.docx` may exist **only** in a configurable temp directory (`java.io.tmpdir` or `DOCUMENT_READER_TMP`); deleted after response completes or on JVM exit policy (immediate delete after stream for downloads).

## PostgreSQL persistence (optional, post-MVP)

Use when you need durable `TaskRun` rows, audit trails, or deferred generation. Suggested mapping:

| Logical entity | Table (example) | Notes |
|----------------|-----------------|-------|
| `TaskRun` | `task_run` | PK `id` UUID; store `status`, timestamps, `error_message`; avoid storing full PDF bytes—store URL + hash or external object key if needed. |
| `PdfSource` | `pdf_source` | Optional FK from `task_run`; `extracted_text` may be **TOAST**-able `TEXT` or truncated with pointer to object storage for large payloads. |
| `WordOutput` | `word_output` | Prefer storing **metadata** (`file_name`, `byte_size`, `created_at`) and optional **object storage** key; do not store large BLOBs in-row unless small. |

Indexes: `task_run(created_at DESC)`, `task_run(status)` for operational queries. Migrations via **Flyway** or **Liquibase** only.
