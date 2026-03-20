# Tasks: PDF Link Reader and Prompt-to-Word Agent

**Input**: `/specs/001-pdf-to-word-agent/` (plan, spec, contracts, research, data-model, quickstart)

## Phase 1: Setup

- [x] T001 Add Maven `pom.xml` (Spring Boot 4.0.4, WebMVC, WebFlux/WebClient, validation, actuator, PDFBox, POI, springdoc 3)
- [x] T002 Add `src/main/resources/application.yml` with `document.reader.*` limits and AI settings
- [x] T003 [P] Expand `.gitignore` for Java/Maven; add `.dockerignore`

## Phase 2: Core services

- [x] T004 Add `DocumentReaderProperties` under `com.documentreader.config`
- [x] T005 Add `WebClient` bean with max in-memory size in `com.documentreader.config.WebClientConfig`
- [x] T006 Implement `PdfFetchService` + domain exceptions in `com.documentreader.pdf`
- [x] T007 Implement `PdfTextExtractionService` (PDFBox 3 `Loader.loadPDF`) in `com.documentreader.pdf`
- [x] T008 Implement `DocxWriterService` (POI `XWPFDocument`) in `com.documentreader.docx`
- [x] T009 Implement `OpenAiChatService` (JDK `HttpClient` → OpenAI chat completions) in `com.documentreader.ai`

## Phase 3: API layer

- [x] T010 Add DTOs `ExtractPdfRequest`, `ExtractPdfResponse`, `GenerateWordRequest` in `com.documentreader.api.dto`
- [x] T011 Add `GlobalExceptionHandler` returning RFC 9457 `ProblemDetail`
- [x] T012 Add `PdfExtractController` — `POST /api/v1/pdf/extract`
- [x] T013 Add `WordDocumentController` — `POST /api/v1/documents/word` (binary `.docx` + `Content-Disposition`)
- [x] T014 Add `DocumentReaderApplication` entrypoint

## Phase 4: Tests & packaging

- [x] T015 [P] `PdfExtractControllerTest` (`@WebMvcTest` + mocks)
- [x] T016 [P] `WordDocumentControllerTest` (`@WebMvcTest` + mocks)
- [x] T017 Add multi-stage `Dockerfile` (Temurin 21)
- [x] T018 Update root `README.md` with run, env vars, and curl examples
