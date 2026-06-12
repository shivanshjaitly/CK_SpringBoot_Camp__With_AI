# VS Code + Continue + Groq Setup Guide

## Prerequisites

* VS Code installed
* Continue Extension installed
* Groq Account
* Groq API Key

---

## Step 1: Generate Groq API Key

1. Login to Groq Console
2. Navigate to **API Keys**
3. Create a new API Key
4. Copy the generated key

Example:

```text
gsk_xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx
```

---

## Step 2: Install Continue Extension

1. Open VS Code
2. Open Extensions (`Cmd + Shift + X`)
3. Search for:

```text
Continue
```

4. Install the Continue extension

---

## Step 3: Configure Continue

Open Continue and navigate to:

```text
Configs
```

Create or update your configuration file:

```yaml
name: Groq
version: 1.0.0
schema: v1

models:
  - name: llama-3.3
    provider: groq
    model: llama-3.3-70b-versatile
    apiKey: gsk_your_groq_api_key_here
```

> Never commit your actual API key to GitHub.

---

## Step 4: Save and Reload

After saving the configuration:

1. Reload VS Code
2. Open Continue Chat
3. Verify that `llama-3.3` appears in the model dropdown

---

## Step 5: Test the Model

Open any Java file and ask:

```text
Explain this Spring Boot Controller
```

or

```text
Generate a REST API for Student Management
```

If Continue returns a response, Groq is successfully configured.

---

## Recommended Usage

### Code Explanation

```text
Explain this code line by line.
```

### Refactoring

```text
Refactor this code using Java 21 best practices.
```

### Unit Testing

```text
Generate JUnit 5 test cases for this service.
```

### Spring Boot

```text
Create a CRUD REST API using Spring Boot and JPA.
```

### Production Issue Analysis

```text
Analyze this code and identify potential production bugs.
```

---

## Troubleshooting

### Invalid API Key

Verify:

```text
Groq Console → API Keys
```

### Model Not Found

Verify:

```yaml
model: llama-3.3-70b-versatile
```

### Continue Not Responding

Check:

* Internet connection
* API key validity
* Continue configuration
* Model selection

---

## Success Criteria

You should be able to:

* Chat with AI inside VS Code
* Generate Java code
* Create Spring Boot applications
* Refactor code
* Generate unit tests
* Debug production issues
* Analyze large codebases
