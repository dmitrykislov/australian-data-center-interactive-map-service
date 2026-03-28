# Australian Data Center Interactive Map

Spring Boot + Redis application with an interactive map frontend served from the backend.

## Run Locally With One Command (Docker)

### Prerequisites

- Docker Desktop (or Docker Engine + Compose plugin)

### Start

```bash
docker compose up --build
```

### Open

- App UI: `http://localhost:8080/`
- API sample: `http://localhost:8080/api/v1/datacenters`
- Login is enabled by Spring Security. Username is `user`, and the generated password is shown in app logs:

```bash
docker compose logs app | grep "Using generated security password"
```

## Useful Commands

Show logs:

```bash
docker compose logs -f app
```

Stop containers:

```bash
docker compose down
```

Stop and remove Redis data volume:

```bash
docker compose down -v
```

Rebuild after code changes:

```bash
docker compose up --build
```

## Notes

- `docker-compose.yml` starts two services: `app` and `redis`.
- Local Docker startup runs over HTTP on port `8080` for convenience.
- Redis is internal to Compose (no host `6379` binding), which avoids conflicts with any local Redis instance.
- Compose sets `REDIS_HOST=redis` so the app connects to the Redis container.
- Existing deployment docs are available in `docs/DEPLOYMENT.md`.

## Maven Dependency Caching During Docker Builds

- The Docker build uses a BuildKit cache mount for Maven home, so repeated `docker compose up --build` runs reuse dependencies instead of redownloading everything.
- Maven still decides the local repo location from its own defaults/settings (no hardcoded `maven.repo.local` flag).
- First build downloads dependencies; later builds should be much faster and show far fewer `Downloading from` lines.
- If needed, ensure BuildKit is enabled:

```bash
export DOCKER_BUILDKIT=1
```
