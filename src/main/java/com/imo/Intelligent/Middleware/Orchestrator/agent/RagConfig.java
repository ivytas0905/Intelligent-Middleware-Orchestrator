package com.imo.Intelligent.Middleware.Orchestrator.agent;

import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.DocumentSplitter;
import dev.langchain4j.data.document.splitter.DocumentSplitters;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.embedding.EmbeddingModel;

import dev.langchain4j.model.embedding.onnx.allminilml6v2.AllMiniLmL6V2EmbeddingModel;
import dev.langchain4j.rag.content.retriever.ContentRetriever;
import dev.langchain4j.rag.content.retriever.EmbeddingStoreContentRetriever;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.EmbeddingStoreIngestor;
import dev.langchain4j.store.embedding.inmemory.InMemoryEmbeddingStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Configuration
public class RagConfig {

    private static final Logger log = LoggerFactory.getLogger(RagConfig.class);

    @Value("${imo.rag.chunk-size}")
    private int chunkSize;
    @Value("${imo.rag.chunk-overlap}")
    private int chunkOverlap;
    @Value("${imo.rag.max-results}")
    private int maxResults;
    @Value("${imo.rag.min-score}")
    private double minScore;

    @Bean
    public EmbeddingModel embeddingModel() {
        log.info("Loading AllMiniLmL6V2 embedding model from classpath...");
        return new AllMiniLmL6V2EmbeddingModel();
            
    }
    @Bean
    public EmbeddingStore<TextSegment> embeddingStore(EmbeddingModel embeddingModel) throws IOException {
         log.info("Ingesting policy document with chunk size: {} and overlap:{}",chunkSize, chunkOverlap);
         ClassPathResource resource = new ClassPathResource("policy.txt");
         String content = new String(resource.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
         Document document = Document.from(content);
         DocumentSplitter splitter = DocumentSplitters.recursive(chunkSize, chunkOverlap);
         InMemoryEmbeddingStore<TextSegment> store = new InMemoryEmbeddingStore<>();
         EmbeddingStoreIngestor ingestor = EmbeddingStoreIngestor.builder()
          .documentSplitter(splitter)
          .embeddingModel(embeddingModel)
          .embeddingStore(store)
          .build();
          
         ingestor.ingest(document);
         log.info("Policy document ingested successfully");
         
         return store;
    }

    @Bean
    public ContentRetriever contentRetriever(EmbeddingStore<TextSegment> embeddingStore,EmbeddingModel embeddingModel){
          return EmbeddingStoreContentRetriever.builder()
            .embeddingStore(embeddingStore)
            .embeddingModel(embeddingModel)
            .maxResults(maxResults)
            .minScore(minScore)
            .build();
}
    @Bean
    public MessageWindowChatMemory chatMemory(){
         return MessageWindowChatMemory.withMaxMessages(10);
    }
}