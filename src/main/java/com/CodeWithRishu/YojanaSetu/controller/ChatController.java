package com.CodeWithRishu.YojanaSetu.controller;


import com.CodeWithRishu.YojanaSetu.service.ChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/chat")
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;

    @PostMapping("/ask")
    public ResponseEntity<String> askBot(@RequestParam(required = true) String message) {
        String response = chatService.getRAGResponse(message);
        return ResponseEntity.ok(response);
    }

}