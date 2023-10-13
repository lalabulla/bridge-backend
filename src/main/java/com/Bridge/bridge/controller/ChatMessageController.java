package com.Bridge.bridge.controller;

import com.Bridge.bridge.dto.request.ChatMessageRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@Slf4j
public class ChatMessageController {

    private final SimpMessagingTemplate simpMessagingTemplate;

    /**
     * 채팅방에 들어오는 경우
     * 모집자가 채팅방을 만듦과 동시에 지원자 모집자 둘 다 입장해야함(구독)
     */
    @MessageMapping("/chat/enter")
    public void enter(@Payload ChatMessageRequest chatMessageRequest, SimpMessageHeaderAccessor headerAccessor) {

        chatMessageRequest.setMessage(chatMessageRequest.getSender() + "이 입장하셨습니다.");

        simpMessagingTemplate.convertAndSend("/sub/chat/room/" + chatMessageRequest.getChatRoomId(), chatMessageRequest);
    }

    /**
     * 채팅방에 메세지 보내는 경우
     */
    @MessageMapping("/chat/message")
    public void sendMessage(ChatMessageRequest chatMessageRequest) {
        log.info("message = {}", chatMessageRequest.getMessage());
        simpMessagingTemplate.convertAndSend("/sub/chat/room/" + chatMessageRequest.getChatRoomId(), chatMessageRequest);
    }

    /**
     * 채팅방 나가기
     */
}