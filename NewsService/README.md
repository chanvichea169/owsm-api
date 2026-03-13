# NewsService API

Spring Boot backend service for managing news and media assets, now standardized to a consistent REST API contract.

## Tech Stack

- Java 21
- Spring Boot 3.3.2
- Spring Web
- Spring Data JPA
- PostgreSQL
- Jakarta Bean Validation
- Lombok

## Base URL

- Default port: `8082`
- Versioned routes: `/api/v1/...`
- Backward-compatible routes are also kept for current controllers.

Examples:
- `http://localhost:8082/api/v1/news`
- `http://localhost:8082/api/v1/media-assets`

## Standard API Contract

### Success Response

```json
{
  "success": true,
  "message": "News fetched successfully",
  "data": {},
  "timestamp": "2026-03-03T10:00:00"
}
```

### Error Response

```json
{
  "success": false,
  "message": "Validation failed",
  "status": 400,
  "path": "/api/v1/news",
  "errors": {
    "title": "must not be blank"
  },
  "timestamp": "2026-03-03T10:00:00"
}
```

## Frontend Preview

- **View all news** - send `GET http://localhost:8082/api/v1/news` and use the `data` array inside the shared success response to populate cards, tables, etc. Each item mirrors `NewsResponse` fields (`id`, `slug`, `title`, `content`, `category`, `coverImage`, `author`, `status`, `isFeatured`, `viewCount`, `publishedAt`, `createdAt`, `updatedAt`).
- **Swagger UI** - open `http://localhost:8082/swagger-ui/index.html` after the app starts to explore request/response samples, test `view all` and other routes, and copy the exact payloads the backend expects.
- **Filtering guidance** - combine query strings when needed (`/media-assets?newsId=123&category=technology`) and reuse `/news/{id}` responses to resolve relational data (media assets reference `newsId`).

## Implemented Resources

## 1) News

Routes (both available):
- `/api/v1/news`
- `/api/news`

Endpoints:
- `POST /` create news (`201 Created`)
- `GET /{id}` get news by id (`200 OK`)
- `GET /` get all news (`200 OK`)
- `PUT /{id}` update news (`200 OK`)
- `PUT /{id}/publish` publish news (`200 OK`)
- `DELETE /{id}` delete news (`204 No Content`)

Behavior:
- `publish` sets:
  - `status = PUBLISHED`
  - `publishedAt = now`
- Not found returns `404`.

## 2) Media Assets

Routes (both available):
- `/api/v1/media-assets`
- `/api/media-assets`

Endpoints:
- `POST /` create media asset (`201 Created`)
- `POST /upload` upload a file (`multipart/form-data`, `201 Created`)
- `PUT /{id}` update media asset (`200 OK`)
- `GET /{id}` get media asset by id (`200 OK`)
- `GET /?newsId={newsId}` get media assets by news id (`200 OK`)
- `GET /?category={category}` get media assets by category (`200 OK`)
- `GET /?newsId={newsId}&category={category}` get media assets by news + category (`200 OK`)
- `GET /news/{newsId}` legacy path for media by news (`200 OK`)
- `DELETE /{id}` delete media asset (`204 No Content`)

Behavior:
- `getByNews` now correctly queries by `newsId` (not media id).
- Upload supports common document/image types including `pdf`, `doc`, `docx`, `txt`, `xls`, `xlsx`, `csv`, `ppt`, `pptx`, `jpg`, `png`, `gif`, `webp`.
- Uploaded file metadata includes category and original/stored filename.
- Not found returns `404`.

## Exception Handling

Global exception handling is standardized:

- `ResourceNotFoundException` -> `404 Not Found`
- Validation errors (`MethodArgumentNotValidException`, `ConstraintViolationException`) -> `400 Bad Request`
- `OwsmException` -> `400 Bad Request`
- Other runtime errors -> `500 Internal Server Error`

## Validation

Validation is enabled through:
- `spring-boot-starter-validation`

Example rules in requests:
- `NewsRequest`: required `title`, `content`, `category`
- `MediaAssetRequest`: required `newsId`, `fileUrl`, `fileType`, `category`

## Test Data and API Test

Use this section to test quickly after starting the app.

Base URL:
- `http://localhost:8082/api/v1`

### 1) Create News (test data)

```bash
curl -X POST "http://localhost:8082/api/v1/news" ^
  -H "Content-Type: application/json" ^
  -d "{\"title\":\"AI in 2026\",\"content\":\"News content for testing\",\"category\":\"technology\",\"coverImage\":\"https://example.com/cover.jpg\",\"isFeatured\":true}"
```

Expected:
- HTTP `201`
- response has `data.id` (use it as `newsId` for upload)

### 2) Upload PDF / file

Replace:
- `NEWS_ID` with real news id
- `C:\temp\sample.pdf` with your local file path

```bash
curl -X POST "http://localhost:8082/api/v1/media-assets/upload?newsId=NEWS_ID&category=technology" ^
  -H "Content-Type: multipart/form-data" ^
  -F "file=@C:\temp\sample.pdf"
```

Expected:
- HTTP `201`
- response `data` contains `fileUrl`, `fileType`, `category`, `originalFileName`, `storedFileName`

### 3) Filter by category

```bash
curl "http://localhost:8082/api/v1/media-assets?category=technology"
```

### 4) Filter by news + category

```bash
curl "http://localhost:8082/api/v1/media-assets?newsId=NEWS_ID&category=technology"
```

### 5) Get all media by news

```bash
curl "http://localhost:8082/api/v1/media-assets?newsId=NEWS_ID"
```

### 6) Publish news (optional)

```bash
curl -X PUT "http://localhost:8082/api/v1/news/NEWS_ID/publish"
```

## Database Config

Configured in `src/main/resources/application.yml`:

- URL: `jdbc:postgresql://localhost:5432/news_db`
- Username: `postgres`
- Password: `123`
- Hibernate DDL: `update`
- SQL logging: enabled

## Build and Run

### Requirements

- JDK 21 installed and active (`JAVA_HOME` must point to Java 21)
- PostgreSQL running with database `news_db`

### Commands

```bash
# Windows
.\mvnw.cmd clean install
.\mvnw.cmd spring-boot:run
```

## Current Project Notes

- API response format is now consistent for controller responses.
- `DELETE` endpoints return `204 No Content`.
- Legacy and versioned paths are both supported for current resources.
- In the current environment, build failed with:
  - `release version 21 not supported`
  - This means local JDK is below 21 and must be updated/switched.
