# Week 7 · Class 2 — Sunday · Spring AI + RAG + Project Complete

> **[← Week 7 Index](README.md)** · **Previous → [Class 1](Class-1.md)**  
> **Coding folder:** `week-06-booking-service` (+ `ai-search-service`)

---

## CLASS 2 — TOPICS

| # | Topic | Code? |
|---|-------|-------|
| 1 | Kafka saga recap | Talk / run |
| 2 | ai-search-service module (:8083) | Code |
| 3 | EmbeddingModel — text → vectors | Code |
| 4 | SimpleVectorStore — index slot descriptions | Code |
| 5 | Semantic search API | Code |
| 6 | Basic RAG — ask over retrieved slots | Code |
| 7 | Full-stack Docker demo | Run |
| 8 | Portfolio wrap — Project #3 complete | Talk |

**Session goal:** Search by meaning + RAG answers. Bootcamp Project #3 done.

---

# TOPIC 1 — Recap

### SAY

> "Yesterday: events across services. Today: AI that **retrieves** then **generates** — not hallucinated slot lists."

---

# TOPIC 2 — ai-search-service

### YOU DO

```
week-06-booking-service/
└── ai-search-service/     ← port 8083
```

Dependencies: `web`, `actuator`, Spring AI (OpenAI-compatible → Groq for chat; embeddings via configured model).

For class simplicity, ai-search can:

- Pull slots via HTTP from booking-service `GET /api/slots`, **or**
- Accept admin `POST /api/ai/index` with slot payloads, **or**
- Share DB read-only (avoid if teaching bounded contexts)

**Preferred for teaching:** booking calls ai-search to reindex on slot create; search stays on `:8083`.

---

# TOPIC 3 — EmbeddingModel

### SAY

> "Embedding = list of floats capturing meaning. Similar text → similar vectors."

### YOU DO

```java
@Service
public class SlotEmbeddingService {
    private final EmbeddingModel embeddingModel;

    public float[] embed(String text) {
        return embeddingModel.embed(text);
    }
}
```

Wire Spring AI embedding client (document exact starter + env in live class — Groq/Gemini/OpenAI embedding endpoint as available).

---

# TOPIC 4 — SimpleVectorStore

### YOU DO

On startup / index API:

```java
Document doc = new Document(
    slot.description(),
    Map.of("slotId", slot.id().toString(), "title", slot.title())
);
vectorStore.add(List.of(doc));
```

Persist file-backed store for demo restarts if time allows.

---

# TOPIC 5 — Semantic Search

### YOU DO

```http
POST /api/ai/search
{ "query": "quiet room for one-on-one near downtown", "topK": 5 }
```

```java
List<Document> hits = vectorStore.similaritySearch(
    SearchRequest.query(query).withTopK(topK)
);
```

Return slot ids + titles + scores — **no LLM yet**.

### RUN

Keyword `quiet` may miss; semantic query still finds "peaceful private meeting space".

---

# TOPIC 6 — Basic RAG

### SAY

> "RAG = Retrieval Augmented Generation.
> Retrieve slots → stuff into prompt → LLM answers **only** from context."

### YOU DO

```http
POST /api/ai/ask
{ "question": "Which doctor slots are good for a follow-up this week?" }
```

Flow:

1. Embed question / similarity search  
2. Build context from top documents  
3. `ChatClient` system prompt: *Answer only using the slot context. If unknown, say so.*  
4. Return `{ answer, sources: [...] }`

### RUN

Ask something answerable from seeded slots → grounded reply.  
Ask about missing data → model admits unknown.

---

# TOPIC 7 — Full Demo

### Script (record for portfolio)

1. `docker compose up -d` (Postgres + Kafka)  
2. Start booking · notification · ai-search  
3. Provider creates rich slot descriptions  
4. Index / auto-embed  
5. Semantic search  
6. RAG ask  
7. Book with Idempotency-Key → notification log  
8. Cancel → compensation event  

---

# TOPIC 8 — Project #3 Complete

### WEEK 7 — Deliverables

| # | Deliverable |
|---|-------------|
| 1 | notification-service + Kafka |
| 2 | Choreography saga + compensation |
| 3 | Idempotent consumers |
| 4 | Embeddings + vector store |
| 5 | Semantic search + RAG ask |
| 6 | Docker end-to-end demo |

### Portfolio one-liner

> "Built a booking platform with JWT, idempotent booking APIs, AOP audit, Kafka-based saga across microservices, and Spring AI RAG for semantic slot search."

### SAY

> "**Three projects done:** EMS → Expense → Booking.
> Booking is the one you lead with in interviews."

---

## Interview Quick Reference

| Question | Answer |
|----------|--------|
| Embedding? | Vector for meaning |
| Why vector search? | Similarity, not keyword equality |
| RAG vs fine-tune? | Retrieve context at runtime; cheaper, fresher |
| Hallucination control? | Prompt + citations from retrieved docs only |

---

*CodeKerdos.in · Week 7 Class 2 · Project #3 complete*
