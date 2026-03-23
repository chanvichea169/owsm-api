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

### 1) Create News (JSON data)

Request body:

```json
{
  "title": "AI in 2026",
  "content": "News content for testing",
  "category": "technology",
  "coverImage": "cover.jpg",
  "isFeatured": true
}
```

Example:

```bash
curl -X POST "http://localhost:8082/api/v1/news" ^
  -H "Content-Type: application/json" ^
  -d "{\"title\":\"AI in 2026\",\"content\":\"News content for testing\",\"category\":\"technology\",\"coverImage\":\"cover.jpg\",\"isFeatured\":true}"
```

Expected:
- HTTP `201`
- response has `data.id` (use it as `newsId` for upload)

Sample response data:

```json
{
  "id": 1,
  "title": "AI in 2026",
  "slug": "ai-in-2026",
  "content": "News content for testing",
  "category": null,
  "coverImage": "cover.jpg",
  "author": null,
  "status": "DRAFT",
  "isFeatured": true,
  "viewCount": 0,
  "publishedAt": null,
  "createdAt": "2026-03-22T10:00:00",
  "updatedAt": null
}
```

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

Sample response data:

```json
{
  "id": 10,
  "newsId": 1,
  "fileUrl": "550e8400-e29b-41d4-a716-446655440000_sample.pdf",
  "fileType": "application/pdf",
  "fileSize": 12345,
  "category": "technology",
  "originalFileName": "sample.pdf",
  "storedFileName": "550e8400-e29b-41d4-a716-446655440000_sample.pdf",
  "createdAt": "2026-03-22T10:05:00",
  "updatedAt": "2026-03-22T10:05:00"
}
```

### 3) Upload news image

News-specific images are stored under the existing `uploads/news` folder.

Replace:
- `NEWS_ID` with real news id
- `C:\temp\news-image.jpg` with your local file path

```bash
curl -X POST "http://localhost:8082/api/v1/media-assets/upload/news?newsId=NEWS_ID&category=news" ^
  -H "Content-Type: multipart/form-data" ^
  -F "file=@C:\temp\news-image.jpg"
```

Expected:
- HTTP `201`
- response `data` includes the same metadata and the file is stored in `uploads/news`
- `news.coverImage` is updated with the generated image filename

Sample response data:

```json
{
  "id": 11,
  "newsId": 1,
  "fileUrl": "550e8400-e29b-41d4-a716-446655440001_news-image.jpg",
  "fileType": "image/jpeg",
  "fileSize": 45678,
  "category": "news",
  "originalFileName": "news-image.jpg",
  "storedFileName": "550e8400-e29b-41d4-a716-446655440001_news-image.jpg",
  "createdAt": "2026-03-22T10:06:00",
  "updatedAt": "2026-03-22T10:06:00"
}
```

### 4) Upload multiple photos

Send as many `files` fields as needed; each will produce a media asset record.

Replace:
- `NEWS_ID` with real news id
- `C:\temp\photo1.jpg` / `C:\temp\photo2.jpg` with your photo paths

```bash
curl -X POST "http://localhost:8082/api/v1/media-assets/upload/photos?newsId=NEWS_ID&category=gallery" ^
  -H "Content-Type: multipart/form-data" ^
  -F "files=@C:\temp\photo1.jpg" ^
  -F "files=@C:\temp\photo2.jpg"
```

Expected:
- HTTP `201`
- response `data` is a list of metadata objects describing each photo

Sample response data:

```json
[
  {
    "id": 12,
    "newsId": 1,
    "fileUrl": "550e8400-e29b-41d4-a716-446655440002_photo1.jpg",
    "fileType": "image/jpeg",
    "fileSize": 11111,
    "category": "gallery",
    "originalFileName": "photo1.jpg",
    "storedFileName": "550e8400-e29b-41d4-a716-446655440002_photo1.jpg",
    "createdAt": "2026-03-22T10:07:00",
    "updatedAt": "2026-03-22T10:07:00"
  },
  {
    "id": 13,
    "newsId": 1,
    "fileUrl": "550e8400-e29b-41d4-a716-446655440003_photo2.jpg",
    "fileType": "image/jpeg",
    "fileSize": 22222,
    "category": "gallery",
    "originalFileName": "photo2.jpg",
    "storedFileName": "550e8400-e29b-41d4-a716-446655440003_photo2.jpg",
    "createdAt": "2026-03-22T10:07:30",
    "updatedAt": "2026-03-22T10:07:30"
  }
]
```

### 5) Update News (JSON data)

Request body:

```json
{
  "title": "AI in 2026 Updated",
  "content": "Updated news content",
  "category": "technology",
  "coverImage": "550e8400-e29b-41d4-a716-446655440001_news-image.jpg",
  "isFeatured": false
}
```

Example:

```bash
curl -X PUT "http://localhost:8082/api/v1/news/NEWS_ID" ^
  -H "Content-Type: application/json" ^
  -d "{\"title\":\"AI in 2026 Updated\",\"content\":\"Updated news content\",\"category\":\"technology\",\"coverImage\":\"550e8400-e29b-41d4-a716-446655440001_news-image.jpg\",\"isFeatured\":false}"
```

### 6) Filter by category

```bash
curl "http://localhost:8082/api/v1/media-assets?category=technology"
```

### 7) Filter by news + category

```bash
curl "http://localhost:8082/api/v1/media-assets?newsId=NEWS_ID&category=technology"
```

### 8) Get all media by news

```bash
curl "http://localhost:8082/api/v1/media-assets?newsId=NEWS_ID"
```

### 9) Publish news (optional)

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
