# Shortlist: Smart Job Portal with Resume Matching

Candidates register, upload a resume, browse jobs and get jobs ranked by how well they match their skills.
Recruiters post and manage jobs.

**Stack:** Spring Boot 3 (Java 17), Spring Security + JWT, JPA/Hibernate, MySQL, React 18 (Vite).

```
smart-job-portal/
├── backend/            Spring Boot API (Dockerfile included)
├── frontend/           React app (Vercel-ready)
└── docker-compose.yml  MySQL + API for local use
```

## How the matching works

1. **Keyword extraction:** resume text (PDF, DOCX or TXT) is scanned for known skills (`MatchingService.SKILLS`).
2. **Similarity scoring:** TF-IDF cosine similarity between the resume text and each job's title and description.
3. **Final score (0-100):** 60% skill overlap with the job's required skills + 40% text similarity.

The response also lists matched and missing skills per job. Add skills by editing the `SKILLS` set.

## Run locally

Requirements: Java 17, Maven 3.9+, Node 18+, MySQL 8 (or Docker).

**Option A: Docker for MySQL + API**
```bash
docker compose up --build        # API on http://localhost:8080
cd frontend && cp .env.example .env && npm install && npm run dev   # http://localhost:5173
```

**Option B: run everything yourself**
```bash
# 1. MySQL running locally (user root / password root, or set DB_USER and DB_PASS)
cd backend && mvn spring-boot:run

# 2. Frontend
cd frontend && cp .env.example .env && npm install && npm run dev
```
Six sample jobs are inserted on first start. Run backend tests with `mvn test`.

## API

| Method | Path | Access | Purpose |
|---|---|---|---|
| POST | /api/auth/register | public | Create account (role CANDIDATE or RECRUITER) |
| POST | /api/auth/login | public | Returns a JWT |
| GET | /api/jobs?q= | public | List or search jobs |
| GET | /api/jobs/{id} | public | One job |
| GET | /api/jobs/mine | recruiter | Jobs you posted |
| POST | /api/jobs | recruiter | Create a job |
| DELETE | /api/jobs/{id} | recruiter | Delete your own job |
| POST | /api/resume/upload | candidate | Multipart field `file` |
| GET | /api/resume/me | candidate | Latest resume and extracted skills |
| GET | /api/resume/recommendations | candidate | Top 10 matching jobs |

Send `Authorization: Bearer <token>` on protected routes.

## Deploy

Free tiers change often, so check each provider's current limits.

### 1. MySQL
Create a managed MySQL database (Aiven, Railway, or any provider with MySQL 8). Note the host, port, database, user and password. Your JDBC URL will look like:
```
jdbc:mysql://HOST:PORT/DBNAME?sslMode=REQUIRED
```

### 2. Backend on Render
1. Push this repo to GitHub.
2. Render → **New → Web Service** → pick the repo.
3. **Root Directory:** `backend` · **Runtime:** Docker.
4. Environment variables:

| Name | Value |
|---|---|
| `DB_URL` | the JDBC URL above |
| `DB_USER` / `DB_PASS` | database credentials |
| `JWT_SECRET` | a random string of 32+ characters |
| `CORS_ORIGIN` | your Vercel URL, e.g. `https://your-app.vercel.app` (comma-separate several) |

5. **Health Check Path:** `/actuator/health`.
6. Deploy. Your API base URL is `https://<service>.onrender.com/api`.

Free web services on Render can sleep when idle, so the first request after a pause is slow.

### 3. Frontend on Vercel
1. Vercel → **Add New → Project** → pick the repo.
2. **Root Directory:** `frontend` · Framework: Vite (auto-detected).
3. Environment variable: `VITE_API_URL` = `https://<service>.onrender.com/api`.
4. Deploy, then copy the Vercel URL into the backend's `CORS_ORIGIN` and redeploy the backend.

`frontend/vercel.json` rewrites all routes to `index.html` so page refreshes work.

## Production checklist
- Use a strong, private `JWT_SECRET`.
- Change `spring.jpa.hibernate.ddl-auto` from `update` to `validate` and manage the schema with Flyway.
- Store resume files in object storage (S3) if you need the originals; only extracted text is stored now.
- Limit public registration as RECRUITER if recruiters should be approved first.

## Ideas to extend
Job applications and status tracking, saved jobs, email notifications, pagination, embeddings-based matching (sentence-transformers service) in place of TF-IDF.
