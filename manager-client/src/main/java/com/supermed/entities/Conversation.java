package com.supermed.entities;

public class Conversation {
    private String participant; // Имя пользователя, с которым ведется диалог
    private String lastMessageTime; // Время последнего сообщения

    public Conversation(String participant, String lastMessageTime) {
        this.participant = participant;
        this.lastMessageTime = lastMessageTime;
    }

    // Геттеры
    public String getParticipant() {
        return participant;
    }

    public String getLastMessageTime() {
        return lastMessageTime;
    }

    // Сеттеры (если нужны)
    public void setParticipant(String participant) {
        this.participant = participant;
    }

    public void setLastMessageTime(String lastMessageTime) {
        this.lastMessageTime = lastMessageTime;
    }

    @Override
    public String toString() {
        return participant + " (Last: " + lastMessageTime + ")";
    }
}