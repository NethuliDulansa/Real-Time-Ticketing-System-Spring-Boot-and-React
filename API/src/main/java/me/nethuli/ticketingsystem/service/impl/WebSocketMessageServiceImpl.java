package me.nethuli.ticketingsystem.service.impl;

import lombok.RequiredArgsConstructor;
import me.nethuli.ticketingsystem.service.WebSocketMessageService;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class WebSocketMessageServiceImpl implements WebSocketMessageService {
    private final SimpMessagingTemplate messagingTemplate;

    @Override
    public void sendLogMessage(String message) {
        messagingTemplate.convertAndSend("/topic/log", message);
    }
}
