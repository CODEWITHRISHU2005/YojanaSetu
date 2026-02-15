package com.CodeWithRishu.YojanaSetu.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.document.Document;
import org.springframework.ai.reader.pdf.PagePdfDocumentReader;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
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
        this.chatClient = chatClientBuilder.build();
    }

    @PostConstruct
    public void loadSchemes() {
        boolean hasDocs = vectorStore.similaritySearch(
                        SearchRequest.builder().query(".").topK(1).build()
                ).stream()
                .findAny()
                .isPresent();

        if (hasDocs) {
            log.info("Vector store populated. Skipping PDF load.");
            return;
        }
        this.loadPdfDocs();
    }

    public String getRAGResponse(String userQuery) {
        return Optional.ofNullable(userQuery)
                .filter(query -> !query.trim().isEmpty())
                .map(query -> {
                    try {
                        List<Document> docs = vectorStore.similaritySearch(
                                SearchRequest.builder()
                                        .query(query)
                                        .topK(6)
                                        .similarityThreshold(0.5d)
                                        .build()
                        );

                        String context = docs.stream()
                                .map(Document::getFormattedContent)
                                .collect(Collectors.joining("\n\n"));

                        if (context.isEmpty()) {
                            return "I couldn't find relevant information. Please try rephrasing your question.";
                        }

                        String response = chatClient.prompt()
                                .system(systemPrompt)
                                .user(context + "\n\nQUESTION: " + query)
                                .call()
                                .content();

                        return Optional.ofNullable(response)
                                .map(r -> r.replaceAll("\\n\\* ", "\n- "))
                                .map(r -> r.replaceAll("^\\* ", "- "))
                                .orElse("क्षमा करें, कोई जवाब नहीं मिला। / Sorry, no response received.");

                    } catch (Exception e) {
                        log.error("Error processing query: {}", query, e);
                        return "I'm having trouble processing your request. Please try again.";
                    }
                })
                .orElse("Please provide a valid question.");
    }

    private void loadPdfDocs() {
        Optional.ofNullable(pdfResource)
                .stream()
                .flatMap(Arrays::stream)
                .forEach(resource -> {
                    try {
                        List<Document> documents = new TokenTextSplitter()
                                .apply(new PagePdfDocumentReader(resource).get());

                        vectorStore.add(documents);
                        log.info("Loaded scheme from {} with {} chunks",
                                resource.getFilename(), documents.size());
                    } catch (Exception e) {
                        log.error("Unable to load PDF: {}", resource.getFilename(), e);
                    }
                });
    }

    private static final String systemPrompt = """
            You are Yojana-Setu, a helpful government scheme assistant.
            
            RULES:
            1. RESPOND IN THE SAME LANGUAGE as the user's question
            2. Use SIMPLE words (avoid complex terminology)
            3. Use SHORT SENTENCES (max 15 words per sentence)
            4. Use DASHES (-) for bullet points, NEVER asterisks (*)
            5. Add ✅/❌/📝 emojis for visual appeal
            6. End with source: "स्रोत: [PDF Name]" (in Hindi) or "Source: [PDF Name]" (in English)
            7. If no info available, say so politely in user's language
            
            FORMAT:
            **Scheme Name / योजना नाम**
            ✅ Eligibility / पात्रता:
            - [point 1]
            - [point 2]
            - [point 3]
            
            💰 Benefits / लाभ:
            - [point 1]
            - [point 2]
            - [point 3]
            
            📋 Application / आवेदन:
            - [how to apply]
            
            Source/स्रोत: [PDF]
            
            CRITICAL: 
            - Detect user's language (Hindi/English/Hinglish) and respond in that language
            - Use dash (-) for lists, NOT asterisk (*)
            """;
}