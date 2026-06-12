# Groq Setup — Quick Reference

## 1. Get API Key (free, 2 minutes)

1. Go to [https://console.groq.com](https://console.groq.com)
2. Sign up with Google or GitHub
3. **API Keys** → **Create API Key**
4. Copy key (starts with `gsk_`)

## 2. Set Environment Variable

**Never paste the key in `application.yml` and commit to Git.**

### Mac / Linux (terminal)

```bash
export GROQ_API_KEY=gsk_your_key_here
```

### Windows PowerShell

```powershell
$env:GROQ_API_KEY="gsk_your_key_here"
```

### IntelliJ IDEA (recommended for class)

1. Run → Edit Configurations
2. Select `EmployeeManagementApplication`
3. Environment variables → add `GROQ_API_KEY=gsk_your_key_here`
4. Apply → Run

## 3. Verify

Start the app, then:

```http
GET http://localhost:8080/api/ai/greet?name=Test
```

## Troubleshooting

| Error | Fix |
|-------|-----|
| 401 Unauthorized | Wrong or missing API key |
| 429 Too Many Requests | Wait 30 seconds (free tier limit) |
| Bean creation error for ChatClient | Check `spring-ai-openai-spring-boot-starter` in pom.xml |
| Model not found | Update model name in `application.yml` at [Groq docs](https://console.groq.com/docs/models) |
