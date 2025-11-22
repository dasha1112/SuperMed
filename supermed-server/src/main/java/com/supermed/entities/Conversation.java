package com.supermed.entities;

public class Conversation {
    private String participant;
    private String lastMessageTime;

    // Конструктор по умолчанию
    public Conversation() {}

    // Конструктор со всеми полями
    public Conversation(String participant, String lastMessageTime) {
        this.participant = participant;
        this.lastMessageTime = lastMessageTime;
    }

    // Геттеры и сеттеры
    public String getParticipant() {
        return participant;
    }

    public void setParticipant(String participant) {
        this.participant = participant;
    }

    public String getLastMessageTime() {
        return lastMessageTime;
    }

    public void setLastMessageTime(String lastMessageTime) {
        this.lastMessageTime = lastMessageTime;
    }
}