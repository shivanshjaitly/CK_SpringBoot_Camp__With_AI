package in.codekerdos.booking.config;

import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.transformers.TransformersEmbeddingModel;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * RAG building blocks. Embeddings run fully locally via an ONNX model
 * (all-MiniLM-L6-v2) — Groq's OpenAI-compatible endpoint (used for chat, see
 * application.yml) has no embeddings API, so embeddings and chat
 * deliberately come from two different providers.
 * <p>
 * spring-ai-transformers' own autoconfiguration is excluded (see
 * BookingServiceApplication) in favor of this explicit bean because its
 * default model/tokenizer URIs point at a GitHub raw/LFS redirect that some
 * JVM trust stores fail to validate. Hugging Face's CDN is used instead —
 * same model weights, a plainer TLS chain. Cached under ~/.cache after the
 * first successful download; spring.ai.openai.embedding.enabled=false in
 * application.yml stops the OpenAI starter from also trying to configure an
 * EmbeddingModel bean and colliding with this one.
 */
@Configuration
public class AiConfig {

    private static final String HF_BASE = "https://huggingface.co/sentence-transformers/all-MiniLM-L6-v2/resolve/main/";

    @Bean
    public EmbeddingModel embeddingModel() {
        TransformersEmbeddingModel model = new TransformersEmbeddingModel();
        model.setModelResource(HF_BASE + "onnx/model.onnx");
        model.setTokenizerResource(HF_BASE + "tokenizer.json");
        return model;
    }

    @Bean
    public VectorStore vectorStore(EmbeddingModel embeddingModel) {
        return SimpleVectorStore.builder(embeddingModel).build();
    }
}
