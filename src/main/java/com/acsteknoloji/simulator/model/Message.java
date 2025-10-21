package com.acsteknoloji.simulator.model;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class Message {
    private long id;
    private String senderUnit;
    private String receiverUnit;
    private String originalText;
    private String encryptedText;
    private String messageHash;
    private LocalDateTime timestamp;
    private boolean integrityStatus;
    private MessageStatus status;
}
