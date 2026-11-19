# AI Study Assistant (Spring Boot + Spring AI + Ollama + React)

A learning project with:
- Local Ollama chat model
- Conversation memory (in-memory for this starter)
- Java calculator tools
- RAG using Ollama embeddings + SimpleVectorStore
- React + Vite frontend

## Versions
- Java 17+
- Spring Boot 4.0.x
- Spring AI 2.0.2
- Node.js 20+
- Ollama

Spring AI 2.0.x targets Spring Boot 4.0.x/4.1.x. If you already have a Boot 3.x project, use a matching Spring AI 1.1.x line instead of mixing versions.

## 1. Install and download Ollama models

Install Ollama from https://ollama.com/download

Open PowerShell and run:

```powershell
ollama pull qwen3:4b
ollama pull nomic-embed-text
ollama list
```

Ensure Ollama is running. Its default API address is http://localhost:11434.

The chat model and embedding model are separate:
- qwen3:4b: chat and tool calling
- nomic-embed-text: document/query embeddings for RAG

If your computer has limited RAM, use a smaller chat model supported by Ollama and update `spring.ai.ollama.chat.model` accordingly.

## 2. Open backend in Spring Tool Suite (STS)

1. Extract this ZIP.
2. Open Spring Tool Suite.
3. Select `File > Import`.
4. Choose `Maven > Existing Maven Projects`.
5. Browse to `ai-study-assistant/backend`.
6. Select `pom.xml`, then Finish.
7. Wait for Maven dependencies to download.
8. Right-click the project > `Maven > Update Project`.
9. Confirm Project JRE is Java 17 or newer:
   `Window > Preferences > Java > Installed JREs`.
10. Run `AiStudyAssistantApplication.java` using `Run As > Spring Boot App`.

Backend URL: http://localhost:8080

## 3. Run frontend

Open a separate terminal in `frontend`:

```powershell
npm install
npm run dev
```

Open the URL printed by Vite, usually http://localhost:5173.

## 4. Test APIs with Postman

### Chat
POST http://localhost:8080/api/chat

```json
{
  "conversationId": "demo-chat-1",
  "message": "I am learning Java. Explain HashMap simply."
}
```

Use the same conversationId for follow-up messages to retain context.

### RAG
POST http://localhost:8080/api/rag/ask

```json
{
  "question": "What does the Java collections note say about HashMap?"
}
```

### Add a knowledge note
POST http://localhost:8080/api/rag/documents

```json
{
  "content": "Spring Boot makes it easier to create stand-alone Spring applications.",
  "topic": "Spring Boot"
}
```

### Calculator tool
POST http://localhost:8080/api/tools/ask

```json
{
  "question": "What is 125 multiplied by 8?"
}
```

### Health
GET http://localhost:8080/api/health

## 5. Project structure

```text
ai-study-assistant/
  backend/
    pom.xml
    src/main/java/com/example/aistudyassistant/
      AiStudyAssistantApplication.java
      config/
        AiConfig.java
        CorsConfig.java
      controller/
        ChatController.java
        RagController.java
        ToolController.java
        HealthController.java
      dto/
        ChatRequest.java
        ChatResponse.java
        RagRequest.java
        AddDocumentRequest.java
        ToolRequest.java
      service/
        ChatService.java
        RagService.java
        ToolService.java
      tools/
        CalculatorTools.java
    src/main/resources/application.properties
  frontend/
    package.json
    index.html
    src/
      main.jsx
      App.jsx
      styles.css
```

## Notes and limitations

- The starter uses in-memory chat memory and SimpleVectorStore. They are for learning/demo use. Restarting the backend clears these data.
- SimpleVectorStore is not a production vector database. For production, use PGvector, Qdrant, Redis, or another supported persistent vector store.
- The example does not include user login. Do not expose conversation IDs or private documents publicly in a real deployment. Add authentication and verify conversation/document ownership.
- Tool methods are application code. The model requests a tool; Spring AI/application executes it. Do not expose destructive or financial actions without authorization and human approval.
- A successful Maven build still depends on having a supported Java/Maven setup and access to Maven Central. No generated project can guarantee zero environment-specific errors.
