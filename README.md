# documentReader

This project uses **[GitHub Spec Kit](https://github.com/github/spec-kit)** for spec-driven development: constitution → specify → plan → tasks → implement, with Cursor slash commands under `.cursor/commands/`.

## Application (feature `001-pdf-to-word-agent`)

**Stack:** Spring Boot **4.0.4**, Java **21** (see `pom.xml`; bump to **25** when your JDK/toolchain supports it), **Maven**, **OpenAPI 3.2** contract in [`specs/001-pdf-to-word-agent/contracts/openapi.yaml`](specs/001-pdf-to-word-agent/contracts/openapi.yaml). OpenAI is called via JDK **`HttpClient`** (compatible with Spring Boot 4; Spring AI can be added later when BOM alignment is clear).

### Configuration (.env)

1. Copy the template and add your key: `cp .env.example .env`
2. Edit **`.env`** and set `OPENAI_API_KEY` (never commit `.env` — it is listed in `.gitignore`; **`.env.example`** is safe to commit and has no secrets).
3. (Optional) If you are using an OpenAI-compatible proxy, set `DOCUMENT_READER_AI_CHAT_COMPLETIONS_URL` to the full `/v1/chat/completions` endpoint URL.
4. (Optional) Storage defaults to `src/main/resources/local-storage/pdfs` and `src/main/resources/local-storage/output-docx`. You can override via `DOCUMENT_READER_STORAGE_PDF_DIR` and `DOCUMENT_READER_STORAGE_WORD_DIR`.
5. Run from the repo root so `dotenv-java` can load `./.env`. Existing shell environment variables always override values from the file.

### Run locally

```bash
# after configuring .env
mvn spring-boot:run
```

- Health: `GET http://localhost:8080/actuator/health`
- Swagger UI: `http://localhost:8080/swagger-ui.html` (springdoc)
- OpenAPI JSON: `http://localhost:8080/v3/api-docs`

### API examples

```bash
# Extract text from a PDF URL
# The controller also persists the downloaded PDF into:
# - src/main/resources/local-storage/pdfs/
curl -s -X POST http://localhost:8080/api/v1/pdf/extract \
  -H 'Content-Type: application/json' \
  -d '{"url":"https://example.com/sample.pdf"}'

# Generate a .docx (download + server-side persist)
# The controller also writes the generated file into:
# - src/main/resources/local-storage/output-docx/
curl -s -D hdr.txt -o out.docx -X POST http://localhost:8080/api/v1/documents/word \
  -H 'Content-Type: application/json' \
  -d '{"prompt":"Write two short paragraphs about teamwork.","fileName":"team.docx"}'

# Generate a .docx using the extracted PDF text as context (two-step)
# Step A: extract
curl -s -o extract.json -X POST http://localhost:8080/api/v1/pdf/extract \
  -H 'Content-Type: application/json' \
  -d '{"url":"https://example.com/sample.pdf"}'

# Step B: feed extractedText into the generator
PDF_TEXT="$(python3 -c 'import json; print(json.load(open("extract.json"))["extractedText"])')"
curl -s -D hdr.txt -o out.docx -X POST http://localhost:8080/api/v1/documents/word \
  -H 'Content-Type: application/json' \
  -d "{\"prompt\":\"Write one paragraph summarizing the PDF.\",\"pdfContextText\":\"$PDF_TEXT\",\"usePdfContext\":true,\"fileName\":\"summary.docx\"}"
```

Errors return **RFC 9457 Problem+JSON** where applicable (`400`, `413`, `502`, `503`, `422`).

### Docker

```bash
docker build -t document-reader:local .
docker run --rm -p 8080:8080 --env-file .env document-reader:local
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
