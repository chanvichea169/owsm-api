# Redis Cache Setup Guide / មគ្គុទ្ទេសក៍ដំឡើង Redis Cache

This guide configures one authenticated Redis 7 container in Docker Desktop and
connects AuthService, NewsService, and AttendanceService to it. It covers
Windows PowerShell setup, cache behavior, verification, and troubleshooting.

មគ្គុទ្ទេសក៍នេះពន្យល់ពីការកំណត់ Redis 7 មួយដែលមានពាក្យសម្ងាត់ក្នុង Docker
Desktop និងភ្ជាប់ AuthService, NewsService និង AttendanceService ទៅវា។
វារួមបញ្ចូលជំហានសម្រាប់ Windows PowerShell ឥរិយាបថ cache ការផ្ទៀងផ្ទាត់
និងការដោះស្រាយបញ្ហា។

## English

### 1. Requirements

- Docker Desktop running with the Linux container engine enabled.
- Java 21 and Maven available for local builds.
- The API repository at `D:\DFRMOI\owsm-dev\owsm_api`.

### 2. Configure local secrets once

Docker Compose reads `.env` (not `.env.example`) beside `docker-compose.yml`.
The root `.gitignore` excludes `.env`; keep actual credentials there, not in
source control.

From PowerShell:

```powershell
Set-Location D:\DFRMOI\owsm-dev\owsm_api
if (-not (Test-Path .env)) { Copy-Item .env.example .env }
```

If `.env` already exists, keep it and add only missing variables; do not
overwrite existing database, mail, or JWT settings.

Generate a persistent signing key and Redis password if they are not already
configured. Save each printed value in the corresponding `.env` line; do not
regenerate either value on normal restarts.

```powershell
$bytes = New-Object byte[] 32
$rng = New-Object System.Security.Cryptography.RNGCryptoServiceProvider
$rng.GetBytes($bytes)
$rng.Dispose()
[Convert]::ToBase64String($bytes)

$bytes = New-Object byte[] 48
$rng = New-Object System.Security.Cryptography.RNGCryptoServiceProvider
$rng.GetBytes($bytes)
$rng.Dispose()
[Convert]::ToBase64String($bytes)
```

Set these entries in `.env`:

```dotenv
JWT_SECRET=<persistent Base64 value, at least 32 decoded bytes>
MAIL_USERNAME=<SMTP mailbox>
MAIL_PASSWORD=<SMTP app password>
REDIS_PASSWORD=<persistent generated Redis password>
```

The existing OTP sign-in needs working SMTP credentials. A JWT key change
invalidates previously issued JWTs, so retain the same key across restarts.

### 3. Start Redis and the three services

Make sure Docker Desktop is running, then run from the API repository root:

```powershell
docker compose config --quiet
docker compose up -d --build redis auth-service news-service attendance-service
docker compose ps
```

The first start downloads `redis:7.4-alpine`. Redis is bound to
`127.0.0.1:6379`, requires `REDIS_PASSWORD`, persists append-only data in the
`redis-data` Docker volume, and has a 256 MiB memory limit with `allkeys-lru`
eviction. The application containers connect to the internal hostname `redis`
on port `6379`; do not use `localhost` between containers.

Check Redis health without printing the password:

```powershell
docker compose exec redis sh -c 'redis-cli -a "$REDIS_PASSWORD" ping'
```

Expected output is `PONG`. Check that each application started:

```powershell
docker compose logs --tail 80 auth-service news-service attendance-service redis
```

### 4. What is cached

All three Spring services use Spring Cache backed by Redis with a five-minute
TTL and null values disabled.

| Service | Cached data | Cache invalidation |
| --- | --- | --- |
| AuthService | Province, district, commune, and village reference reads | All location caches are evicted after a location create, update, or delete |
| NewsService | News lists/details and category lists/details | Relevant news or category caches are evicted after create, publish, update, or delete |
| AttendanceService | Company list and company detail responses | Company caches are evicted after company create, update, or delete |

Responses placed in the Redis cache implement Java serialization. Login
credentials, OTP values, JWTs, login audit rows, authorization decisions,
active sessions, and logout/revocation checks are not cached. Thus a cache
hit cannot bypass authentication or session revocation.

Each service reads `SPRING_DATA_REDIS_HOST`, `SPRING_DATA_REDIS_PORT`, and
`SPRING_DATA_REDIS_PASSWORD`. Defaults support local development; Compose sets
the internal host and password explicitly.

### 5. Verify caching and restart behavior

Call one of the cached read endpoints twice, for example:

```powershell
curl.exe http://localhost:9001/api/locations/provinces
curl.exe http://localhost:9001/api/locations/provinces
curl.exe http://localhost:9001/api/categories
curl.exe http://localhost:9001/api/companies
```

The returned API JSON should be unchanged. Cache correctness can also be
checked by updating a location/category/company through its normal API and
reading it again: the next read must show the update. The five-minute TTL is
the fallback expiry if an entry is not explicitly evicted.

To inspect Redis operationally, use RedisInsight or `redis-cli` inside the
container. Key names are prefixed by Spring and are implementation details;
do not modify application cache keys manually.

### 6. Stop, clear, and troubleshoot

Stop application and Redis containers but preserve cached data:

```powershell
docker compose stop auth-service news-service attendance-service redis
```

Clear cache data without removing PostgreSQL data:

```powershell
docker compose exec redis sh -c 'redis-cli -a "$REDIS_PASSWORD" FLUSHDB'
```

Do not use `docker compose down -v` unless you intend to delete **all** named
volumes, including the PostgreSQL database.

- `NOAUTH`: confirm `REDIS_PASSWORD` in the ignored `.env` matches the running
  Redis container, then recreate Redis and the three services.
- `Connection refused`: run `docker compose ps` and wait until Redis is
  `healthy`; verify the containers share the Compose `backend` network.
- `WRONGPASS`: update `.env` and recreate Redis plus all three apps together.
- Stale read after a mutation: check application logs and verify the write
  completed successfully; all write operations should evict their cache.
- An app cannot start without Redis: check Redis health/logs and the app's
  `SPRING_DATA_REDIS_*` environment, then restart the affected app.

For production, move credentials to a managed secret store, use TLS and
network policies, monitor memory/evictions, and use a highly available Redis
deployment. The local Compose configuration is for development, not a
production HA Redis topology.

## ភាសាខ្មែរ

### ១. តម្រូវការជាមុន

- បើក Docker Desktop និងបើក Linux container engine។
- ត្រូវមាន Java 21 និង Maven សម្រាប់ build នៅលើកុំព្យូទ័រ។
- ទីតាំង API repository គឺ `D:\DFRMOI\owsm-dev\owsm_api`។

### ២. កំណត់ secret ក្នុងម៉ាស៊ីនមូលដ្ឋាន

Docker Compose អានឯកសារ `.env` ដែលនៅជាមួយ `docker-compose.yml`
ប៉ុន្តែមិនអាន `.env.example` ដោយស្វ័យប្រវត្តិទេ។ ឯកសារ `.env` ត្រូវបាន
ដាក់ក្នុង `.gitignore`។ សូមរក្សា credential ពិតនៅក្នុងឯកសារនេះ
ហើយកុំ commit ទៅ Git។

បើក PowerShell ហើយរត់៖

```powershell
Set-Location D:\DFRMOI\owsm-dev\owsm_api
if (-not (Test-Path .env)) { Copy-Item .env.example .env }
```

បើ `.env` មានរួចហើយ សូមរក្សាឯកសារនោះ ហើយបន្ថែមតែ variable ដែលខ្វះ។
កុំសរសេរជាន់លើ database, mail ឬ JWT setting ដែលមានស្រាប់។

បង្កើត key សម្រាប់ JWT និងពាក្យសម្ងាត់ Redis ប្រសិនបើមិនទាន់មាន។
រក្សាទុកតម្លៃនីមួយៗក្នុងបន្ទាត់ត្រឹមត្រូវនៅក្នុង `.env`។ កុំបង្កើតតម្លៃថ្មី
រាល់ពេល restart។

```powershell
$bytes = New-Object byte[] 32
$rng = New-Object System.Security.Cryptography.RNGCryptoServiceProvider
$rng.GetBytes($bytes)
$rng.Dispose()
[Convert]::ToBase64String($bytes)

$bytes = New-Object byte[] 48
$rng = New-Object System.Security.Cryptography.RNGCryptoServiceProvider
$rng.GetBytes($bytes)
$rng.Dispose()
[Convert]::ToBase64String($bytes)
```

កំណត់បន្ទាត់ខាងក្រោមក្នុង `.env`៖

```dotenv
JWT_SECRET=<តម្លៃ Base64 អចិន្ត្រៃយ៍ ដែល decode បានយ៉ាងតិច 32 bytes>
MAIL_USERNAME=<អ៊ីមែល SMTP>
MAIL_PASSWORD=<ពាក្យសម្ងាត់កម្មវិធី SMTP>
REDIS_PASSWORD=<ពាក្យសម្ងាត់ Redis ដែលបានបង្កើត>
```

ការចូលដោយ OTP ត្រូវការការកំណត់ SMTP ដែលដំណើរការ។ បើប្ដូរ JWT key
token ដែលបានចេញពីមុននឹងមិនមានសុពលភាពទៀតទេ ដូច្នេះត្រូវរក្សា key ដដែល។

### ៣. ចាប់ផ្តើម Redis និង service ទាំងបី

ធានាថា Docker Desktop កំពុងដំណើរការ រួចរត់ពី root នៃ API repository៖

```powershell
docker compose config --quiet
docker compose up -d --build redis auth-service news-service attendance-service
docker compose ps
```

ពេលចាប់ផ្តើមលើកដំបូង Docker នឹងទាញយក `redis:7.4-alpine`។
Redis ចងភ្ជាប់តែ `127.0.0.1:6379` ត្រូវការពាក្យសម្ងាត់
រក្សាទុក append-only data ក្នុង Docker volume `redis-data` និងកំណត់
memory អតិបរមា 256 MiB ជាមួយ eviction policy `allkeys-lru`។
Application container តភ្ជាប់តាម hostname ខាងក្នុង `redis` port `6379`។
កុំប្រើ `localhost` ដើម្បីតភ្ជាប់រវាង container។

ពិនិត្យសុខភាព Redis ដោយមិនបង្ហាញពាក្យសម្ងាត់៖

```powershell
docker compose exec redis sh -c 'redis-cli -a "$REDIS_PASSWORD" ping'
```

លទ្ធផលដែលរំពឹងទុកគឺ `PONG`។ ពិនិត្យថា service ទាំងអស់បានចាប់ផ្តើម៖

```powershell
docker compose logs --tail 80 auth-service news-service attendance-service redis
```

### ៤. ទិន្នន័យដែលត្រូវបាន cache

Spring Cache ក្នុង service ទាំងបីប្រើ Redis ជា backend មាន TTL ប្រាំនាទី
និងមិនរក្សាទុកតម្លៃ null ទេ។

| Service | ទិន្នន័យដែលបាន cache | ការសម្អាត cache |
| --- | --- | --- |
| AuthService | ការអានទិន្នន័យយោង ខេត្ត ស្រុក ឃុំ និងភូមិ | សម្អាត cache ទីតាំងទាំងអស់ក្រោយបង្កើត កែប្រែ ឬលុបទិន្នន័យទីតាំង |
| NewsService | បញ្ជី/ព័ត៌មានលម្អិតព័ត៌មាន និងបញ្ជី/ព័ត៌មានលម្អិតប្រភេទ | សម្អាត cache ដែលពាក់ព័ន្ធក្រោយបង្កើត ផ្សាយ កែប្រែ ឬលុប |
| AttendanceService | បញ្ជីក្រុមហ៊ុន និងព័ត៌មានលម្អិតក្រុមហ៊ុន | សម្អាត cache ក្រុមហ៊ុនក្រោយបង្កើត កែប្រែ ឬលុប |

Response ដែលរក្សាទុកក្នុង Redis ប្រើ Java serialization។ ព័ត៌មានចូលប្រើ
OTP, JWT, កំណត់ត្រា audit ការចូល, សេចក្តីសម្រេច authorization,
active session និងការត្រួតពិនិត្យ logout/revocation មិនត្រូវបាន cache ទេ។
ដូច្នេះ cache មិនអាចរំលងការផ្ទៀងផ្ទាត់ ឬការដកសុពលភាព session បានឡើយ។

Service នីមួយៗអាន `SPRING_DATA_REDIS_HOST`,
`SPRING_DATA_REDIS_PORT` និង `SPRING_DATA_REDIS_PASSWORD`។
Compose កំណត់ hostname ខាងក្នុង និងពាក្យសម្ងាត់ដោយផ្ទាល់។

### ៥. ផ្ទៀងផ្ទាត់ cache និងការចាប់ផ្តើមឡើងវិញ

ហៅ endpoint ដែល cache បានពីរដង ឧទាហរណ៍៖

```powershell
curl.exe http://localhost:9001/api/locations/provinces
curl.exe http://localhost:9001/api/locations/provinces
curl.exe http://localhost:9001/api/categories
curl.exe http://localhost:9001/api/companies
```

JSON ដែល API ត្រឡប់មកគួរតែដូចគ្នា។ អាចពិនិត្យភាពត្រឹមត្រូវដោយកែប្រែ
ទីតាំង/ប្រភេទ/ក្រុមហ៊ុនតាម API ធម្មតា រួចអានម្ដងទៀត៖ ការអានបន្ទាប់
ត្រូវបង្ហាញការកែប្រែ។ TTL ប្រាំនាទីជាការផុតកំណត់បម្រុង ប្រសិនបើមិនបាន
សម្អាត entry ដោយផ្ទាល់។

ដើម្បីពិនិត្យ Redis ប្រើ RedisInsight ឬ `redis-cli` នៅក្នុង container។
ឈ្មោះ key ជា implementation detail របស់ Spring។ កុំកែ key cache ដោយដៃ។

### ៦. បញ្ឈប់ សម្អាត និងដោះស្រាយបញ្ហា

បញ្ឈប់ application និង Redis ប៉ុន្តែរក្សាទុកទិន្នន័យ៖

```powershell
docker compose stop auth-service news-service attendance-service redis
```

សម្អាត Redis cache ដោយមិនលុបទិន្នន័យ PostgreSQL៖

```powershell
docker compose exec redis sh -c 'redis-cli -a "$REDIS_PASSWORD" FLUSHDB'
```

កុំប្រើ `docker compose down -v` លុះត្រាតែចង់លុប volume ទាំងអស់
រួមទាំង database PostgreSQL។

- `NOAUTH`: ពិនិត្យថា `REDIS_PASSWORD` ក្នុង `.env` ត្រូវគ្នានឹង Redis
  container បច្ចុប្បន្ន រួចបង្កើត Redis និង service ទាំងបីឡើងវិញ។
- `Connection refused`: ពិនិត្យ `docker compose ps` ហើយរង់ចាំឱ្យ Redis
  មានស្ថានភាព `healthy`។ ពិនិត្យថា container ទាំងអស់នៅ network `backend`។
- `WRONGPASS`: កែ `.env` រួចបង្កើត Redis និង app ទាំងបីឡើងវិញជាមួយគ្នា។
- ទទួលបានទិន្នន័យចាស់ក្រោយកែប្រែ៖ ពិនិត្យ log និងធានាថាការសរសេរទិន្នន័យ
  បានជោគជ័យ ព្រោះ operation កែប្រែទាំងអស់គួរសម្អាត cache។
- App មិនអាចចាប់ផ្តើមដោយគ្មាន Redis៖ ពិនិត្យសុខភាព/log របស់ Redis និង
  environment `SPRING_DATA_REDIS_*` រួច restart app ដែលមានបញ្ហា។

សម្រាប់ production ត្រូវរក្សា credential ក្នុង managed secret store ប្រើ
TLS និង network policy តាមដាន memory/eviction ហើយប្រើ Redis ដែលមាន
high availability។ Compose ក្នុង repository នេះសម្រាប់ development មិនមែន
ជាការដំឡើង Redis HA សម្រាប់ production ទេ។
