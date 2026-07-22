# Week 6–7 · Project #3 — Booking Service

> Full teaching scripts: [`docs/WEEK-6`](../docs/WEEK-6/README.md) · [`docs/WEEK-7`](../docs/WEEK-7/README.md)

## Quick start

```bash
cp .env.example .env
# set JWT_SECRET (and later GROQ_API_KEY for Week 7 RAG)

./mvnw spring-boot:run
# or F5 → "Week 6 Booking — Run (H2)"
```

- API: http://localhost:8081  
- Swagger: http://localhost:8081/swagger-ui.html  
- Health: http://localhost:8081/actuator/health  

### Seeded users

| Email | Password | Role |
|-------|----------|------|
| customer@codekerdos.in | cust123 | CUSTOMER |
| provider@codekerdos.in | prov123 | PROVIDER |
| admin@codekerdos.in | adm123 | ADMIN |

### Postgres (optional)

```bash
docker compose up -d postgres
# run with SPRING_PROFILES_ACTIVE=postgres
```

## Roadmap

| Week | Focus |
|------|--------|
| 6 Sat | Domain + JWT + REST + OpenAPI |
| 6 Sun | Idempotency · AOP · N+1 · tests · Docker |
| 7 Sat | notification-service · Kafka · saga |
| 7 Sun | ai-search-service · embeddings · RAG |
