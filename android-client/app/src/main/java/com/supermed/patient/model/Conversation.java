package com.supermed.patient.model;

public class Conversation {
    private String participant;
    private String lastMessageTime;

    public Conversation() {}

    public Conversation(String participant, String lastMessageTime) {
        this.participant = participant;
        this.lastMessageTime = lastMessageTime;
    }

    public String getParticipant() { return participant; }
    public void setParticipant(String participant) { this.participant = participant; }

    public String getLastMessageTime() { return lastMessageTime; }
    public void setLastMessageTime(String lastMessageTime) { this.lastMessageTime = lastMessageTime; }
}
