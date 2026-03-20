# documentReader

This project uses **[GitHub Spec Kit](https://github.com/github/spec-kit)** for spec-driven development: constitution → specify → plan → tasks → implement, with Cursor slash commands under `.cursor/commands/`.

## Application (feature `001-pdf-to-word-agent`)

**Stack:** Spring Boot **4.0.4**, Java **21** (see `pom.xml`; bump to **25** when your JDK/toolchain supports it), **Maven**, **OpenAPI 3.2** contract in [`specs/001-pdf-to-word-agent/contracts/openapi.yaml`](specs/001-pdf-to-word-agent/contracts/openapi.yaml). OpenAI is called via JDK **`HttpClient`** (compatible with Spring Boot 4; Spring AI can be added later when BOM alignment is clear).

### Run locally

```bash
export OPENAI_API_KEY="sk-..."   # required for POST /api/v1/documents/word
mvn spring-boot:run
```

- Health: `GET http://localhost:8080/actuator/health`
- Swagger UI: `http://localhost:8080/swagger-ui.html` (springdoc)
- OpenAPI JSON: `http://localhost:8080/v3/api-docs`

### API examples

```bash
# Extract text from a PDF URL
curl -s -X POST http://localhost:8080/api/v1/pdf/extract \
  -H 'Content-Type: application/json' \
  -d '{"url":"https://example.com/sample.pdf"}'

# Generate a .docx (save response locally)
curl -s -D hdr.txt -o out.docx -X POST http://localhost:8080/api/v1/documents/word \
  -H 'Content-Type: application/json' \
  -d '{"prompt":"Write two short paragraphs about teamwork.","fileName":"team.docx"}'
```

Errors return **RFC 9457 Problem+JSON** where applicable (`400`, `413`, `502`, `503`, `422`).

### Docker

```bash
docker build -t document-reader:local .
docker run --rm -p 8080:8080 -e OPENAI_API_KEY="$OPENAI_API_KEY" document-reader:local
```

### Spec Kit CLI

```bash
uv tool install specify-cli --from git+https://github.com/github/spec-kit.git
# or
.venv/bin/specify check
```

## Design artifacts

| Artifact | Path |
|----------|------|
| Spec | [`specs/001-pdf-to-word-agent/spec.md`](specs/001-pdf-to-word-agent/spec.md) |
| Plan | [`specs/001-pdf-to-word-agent/plan.md`](specs/001-pdf-to-word-agent/plan.md) |
| Tasks | [`specs/001-pdf-to-word-agent/tasks.md`](specs/001-pdf-to-word-agent/tasks.md) |
| Research | [`specs/001-pdf-to-word-agent/research.md`](specs/001-pdf-to-word-agent/research.md) |

**Database:** MVP is stateless. If you add persistence, use **PostgreSQL** (see `research.md` §7).

Update this README when endpoints, env vars, or run instructions change.
