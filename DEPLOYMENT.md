# YojanaSetu deployment and pilot guide

## Run locally

Requirements: Java 21, a MongoDB Atlas cluster configured for the Spring AI vector store, and a Gemini API key.

PowerShell:

```powershell
$env:GEMINI_API_KEY = "<your-key>"
$env:MONGODB_URI = "<your-mongodb-connection-string>"
./mvnw.cmd spring-boot:run
```

Open `http://localhost:8080/frontend.html`. Keep credentials in environment variables or a managed secret store. Never commit them to source control.

## Before inviting pilot users

- Put the app behind HTTPS and a reverse proxy or gateway. Apply per-user or per-IP rate limits to `POST /api/chat/ask`, set a request-body limit, and restrict concurrent/long-running streams.
- Restrict MongoDB network access to the application, use a dedicated least-privilege database user, and configure backups with a tested restore procedure.
- Tell users that their question and any OCR text they attach are sent to the configured AI provider. Do not request identity numbers or other sensitive details for general scheme questions.
- Add a privacy notice, a support contact, and a way for users to report incorrect or outdated scheme information.
- Pin and self-host the frontend libraries before relying on this UI for a public production service. The current page loads React, Babel, animation, OCR, and font assets from third-party CDNs.
- Monitor application errors, provider failures, latency, and MongoDB availability. Do not log question text or OCR content.

## Scheme information quality

The current knowledge base is a set of PDFs. Chat answers now name the retrieved PDF files, but those names are not clickable official citations and do not prove that a PDF is current. Before using the service to guide applications, maintain a reviewed scheme catalog that records, for every scheme:

- The official source URL and the date it was checked.
- State or national coverage, eligibility rules, benefits, required documents, deadlines, and application steps.
- A named reviewer and a date for the next review.

Have a human verify each record against the linked official source. Show users when a record was last checked. When the available source does not answer a question, say so and direct the user to the official source or a local service center. Never describe an AI-generated match as a confirmed eligibility decision.

## Launch gate

Run a small supervised pilot with one audience and a small, reviewed scheme set. Review incorrect matches and stale information with a domain expert before expanding. The project does not yet include a maintainer workflow for scheme review, clickable official references, user authentication, or distributed rate limiting; deploy those controls at the gateway and build the review workflow before a broad public launch.
