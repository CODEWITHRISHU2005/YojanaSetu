package com.CodeWithRishu.YojanaSetu.service;

import com.CodeWithRishu.YojanaSetu.utils.Helper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor;
import org.springframework.ai.document.Document;
import org.springframework.ai.reader.pdf.PagePdfDocumentReader;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.filter.FilterExpressionBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import javax.annotation.PostConstruct;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class ChatService {

    @Value("classpath:schemes/*.pdf")
    private Resource[] pdfResource;

    private final VectorStore vectorStore;
    private final ChatClient chatClient;

    public ChatService(ChatClient.Builder chatClientBuilder, VectorStore vectorStore) {
        this.vectorStore = vectorStore;
        this.chatClient = chatClientBuilder
                .defaultAdvisors(
                        QuestionAnswerAdvisor.builder(vectorStore).build()
                )
                .build();
    }

    @PostConstruct
    public void loadSchemes() {
        if (pdfResource == null || pdfResource.length == 0) {
            log.warn("No PDF resources found in classpath:schemes/*.pdf");
            return;
        }

        Arrays.stream(pdfResource)
                .filter(r -> r.getFilename() != null && !r.getFilename().isEmpty())
                .forEach(resource -> {
                    var filterBuilder = new FilterExpressionBuilder();
                    String fileName = resource.getFilename();

                    List<Document> existing = vectorStore.similaritySearch(
                            SearchRequest.builder()
                                    .query(fileName)
                                    .topK(1)
                                    .similarityThreshold(0.0d)
                                    .filterExpression(filterBuilder.eq("source", fileName).build())
                                    .build()
                    );

                    if (!existing.isEmpty()) {
                        log.info("Skipping this PDF: {}", fileName);
                        return;
                    }

                    this.loadSinglePdfDocs(resource);
                });
    }

    public Flux<String> getRAGResponse(String userQuery) {
        if (userQuery == null || userQuery.isBlank()) {
            return Flux.just("Please provide a valid question.");
        }
        String query = userQuery.trim();
        List<Document> docs;
        try {
            docs = vectorStore.similaritySearch(
                    SearchRequest.builder()
                            .query(query)
                            .topK(6)
                            .similarityThreshold(0.65d)
                            .build()
            );
        } catch (Exception e) {
            log.error("Vector store search failed for query: {}", query, e);
            return Flux.just("I'm having trouble searching the knowledge base. Please try again.");
        }

        if (docs.isEmpty())
            return Flux.just("I couldn't find relevant information. Please try rephrasing your question.");

        String context = docs.stream()
                .map(Document::getFormattedContent)
                .collect(Collectors.joining("\n\n"));

        try {
            return chatClient.prompt()
                    .system(Helper.systemPrompt)
                    .user("Context:\n" + context + "\n\nQuestion: " + query)
                    .stream()
                    .content();
        } catch (Exception e) {
            log.error("Chat client failed for query: {}", query, e);
            return Flux.just("I'm having trouble generating a response. Please try again.");
        }
    }

    private void loadSinglePdfDocs(Resource resource) {
        try {
            var reader = new PagePdfDocumentReader(resource);
            var splitter = TokenTextSplitter.builder()
                    .withChunkSize(512)
                    .withMinChunkSizeChars(100)
                    .withMinChunkLengthToEmbed(100)
                    .withKeepSeparator(true)
                    .build();

            var docs = splitter.split(reader.get());
            docs.forEach(doc -> doc.getMetadata().put("source", resource.getFilename()));
            vectorStore.add(docs);
            log.info("Loaded: {} {} pages", resource.getFilename(), docs.size());
        } catch (Exception e) {
            log.error("Failed to load PDF: {}", resource.getFilename(), e);
        }
    }

}